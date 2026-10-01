#!/usr/bin/env node
/**
 * Convierte exportaciones de OneNote (un .docx por sección, exportado con
 * "Archivo > Exportar > Sección > Word (.docx)") en un notas.json importable
 * por Notes Pro (Configuración > Respaldo > Importar y combinar).
 *
 * OneNote incluye en cada página, al exportar, un encabezado de 3 líneas:
 *   Título de la página
 *   <fecha larga en español, ej. "sábado, 15 de marzo de 2025">
 *   <hora, ej. "11:41 a. m.">
 * Ese patrón es lo que usamos para separar automáticamente cada página en
 * una nota individual.
 *
 * Uso:
 *   1. Coloca uno o más .docx exportados dentro de la carpeta "input"
 *      (cada archivo se convierte en una categoría con el nombre del archivo).
 *   2. npm install
 *   3. npm run convert
 *   4. Copia el notas.json resultante al teléfono e impórtalo en Notes Pro.
 */
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const mammoth = require('mammoth');
const cheerio = require('cheerio');

const INPUT_DIR = path.join(__dirname, 'input');
const OUTPUT_FILE = path.join(__dirname, 'notas.json');

const CATEGORY_COLORS = ['#6C5CE7', '#0984E3', '#00B894', '#FDCB6E', '#E17055', '#D63031', '#00CEC9', '#E84393'];

const DATE_RE = /^(lunes|martes|mi[ée]rcoles|jueves|viernes|s[áa]bado|domingo),\s+\d{1,2}\s+de\s+[a-záéíóúñ]+\s+de\s+\d{4}$/i;
const TIME_RE = /^\d{1,2}:\d{2}\s*(a\.?\s*m\.?|p\.?\s*m\.?)$/i;

/** Convierte el contenido inline de un <p> a nuestro markdown-lite (negrita/cursiva). */
function inlineToMarkup($, el) {
  let out = '';
  $(el)
    .contents()
    .each((_, node) => {
      if (node.type === 'text') {
        out += node.data;
      } else if (node.tagName === 'strong' || node.tagName === 'b') {
        out += `**${$(node).text()}**`;
      } else if (node.tagName === 'em' || node.tagName === 'i') {
        out += `*${$(node).text()}*`;
      } else {
        out += $(node).text();
      }
    });
  return out.replace(/\s+/g, ' ').trim();
}

/** Aplana el documento en una lista de párrafos/ítems de lista, en orden. */
function extractBlocks($) {
  const blocks = [];
  function walk(elements) {
    elements.each((_, el) => {
      const tag = el.tagName;
      if (tag === 'p') {
        const text = inlineToMarkup($, el);
        if (text) blocks.push({ type: 'p', text });
      } else if (tag === 'ul' || tag === 'ol') {
        // OneNote exporta viñetas anidadas como <li><ul><li>...</li></ul></li>;
        // el <li> exterior no tiene texto propio, solo envuelve la sublista,
        // así que solo tomamos los <li> "hoja" (sin una lista hija) para evitar duplicados.
        $(el)
          .find('li')
          .filter((__, li) => $(li).children('ul, ol').length === 0)
          .each((__, li) => {
            const text = inlineToMarkup($, li);
            if (text) blocks.push({ type: 'li', text });
          });
      } else {
        const children = $(el).children();
        if (children.length) walk(children);
      }
    });
  }
  walk($('body').children());
  return blocks;
}

/** Divide los bloques en páginas usando el patrón título/fecha/hora de OneNote. */
function splitIntoPages(blocks) {
  const boundaries = [];
  for (let i = 0; i < blocks.length - 1; i++) {
    const isDate = blocks[i].type === 'p' && DATE_RE.test(blocks[i].text);
    const isTimeNext = blocks[i + 1].type === 'p' && TIME_RE.test(blocks[i + 1].text);
    if (isDate && isTimeNext) {
      const titleIdx = i - 1;
      if (titleIdx >= 0 && blocks[titleIdx].type === 'p') {
        boundaries.push({ titleIdx, timeIdx: i + 1 });
      }
    }
  }

  if (boundaries.length === 0) {
    // No se encontró el patrón título/fecha/hora: exporta todo como una sola nota.
    const content = blocks.map((b) => (b.type === 'li' ? `- ${b.text}` : b.text)).join('\n');
    return [{ title: path.basename(INPUT_DIR), content }];
  }

  const pages = [];
  for (let b = 0; b < boundaries.length; b++) {
    const { titleIdx, timeIdx } = boundaries[b];
    const nextTitleIdx = b + 1 < boundaries.length ? boundaries[b + 1].titleIdx : blocks.length;
    const contentBlocks = blocks.slice(timeIdx + 1, nextTitleIdx);
    const content = contentBlocks.map((blk) => (blk.type === 'li' ? `- ${blk.text}` : blk.text)).join('\n');
    pages.push({ title: blocks[titleIdx].text, content });
  }
  return pages;
}

function buildNote(title, content, categoryId) {
  const now = Date.now();
  return {
    id: crypto.randomUUID(),
    type: 'text',
    title: title || 'Sin título',
    content: content.trim(),
    checklist: [],
    color: 'default',
    categoryId,
    tags: [],
    favorite: false,
    pinned: false,
    archived: false,
    deleted: false,
    locked: false,
    reminderAt: null,
    reminderRepeat: 'none',
    notificationId: null,
    createdAt: now,
    updatedAt: now,
    deletedAt: null,
  };
}

async function main() {
  if (!fs.existsSync(INPUT_DIR)) {
    fs.mkdirSync(INPUT_DIR, { recursive: true });
    console.error('Creé la carpeta "input". Coloca ahí tus .docx exportados de OneNote y vuelve a correr el script.');
    process.exit(1);
  }

  const files = fs.readdirSync(INPUT_DIR).filter((f) => f.toLowerCase().endsWith('.docx'));
  if (files.length === 0) {
    console.error('No se encontraron archivos .docx dentro de la carpeta "input".');
    process.exit(1);
  }

  const categories = [];
  const notes = [];

  for (const file of files) {
    const sectionName = path.basename(file, '.docx');
    const categoryId = crypto.randomUUID();
    categories.push({
      id: categoryId,
      name: sectionName,
      color: CATEGORY_COLORS[categories.length % CATEGORY_COLORS.length],
      icon: 'folder',
      isDefault: false,
      createdAt: Date.now(),
    });

    const filePath = path.join(INPUT_DIR, file);
    const { value: html } = await mammoth.convertToHtml({ path: filePath });
    const $ = cheerio.load(html);
    const blocks = extractBlocks($);
    const pages = splitIntoPages(blocks);

    for (const page of pages) {
      notes.push(buildNote(page.title, page.content, categoryId));
    }

    console.log(`OK ${file} -> ${pages.length} notas (categoria "${sectionName}")`);
  }

  const payload = { version: 1, exportedAt: Date.now(), notes, categories };
  fs.writeFileSync(OUTPUT_FILE, JSON.stringify(payload, null, 2));

  console.log(`\nListo. ${notes.length} notas escritas en ${OUTPUT_FILE}`);
  console.log('Copia ese archivo a tu telefono e importalo en Notes Pro: Configuracion > Respaldo > Importar y combinar.');
}

main().catch((err) => {
  console.error('Error:', err);
  process.exit(1);
});
