# Importador de OneNote a Notes Pro

Convierte exportaciones de OneNote (`.docx`, una por sección) en un archivo
`notas.json` importable directamente en Notes Pro.

## Cómo exportar desde OneNote (escritorio)

1. Clic derecho sobre una sección → **Exportar...** (o **Archivo → Exportar**)
2. Alcance: **Sección** — Formato: **Word (.docx)**
3. Guarda el archivo dentro de la carpeta `input/` de esta herramienta.
4. Repite para cada sección que quieras traer — **cada archivo se convierte en
   una categoría** en Notes Pro, nombrada igual que el archivo.

## Uso

```bash
cd tools/onenote-import
npm install
npm run convert
```

Genera `notas.json` en esta misma carpeta.

## Cómo funciona

OneNote incluye en cada página, al exportar, un encabezado de 3 líneas:

```
<Título de la página>
<fecha larga en español, ej. "sábado, 15 de marzo de 2025">
<hora, ej. "11:41 a. m.">
```

El script usa ese patrón para separar automáticamente el documento en páginas
individuales, cada una convertida en una nota de texto (con negritas, cursivas
y viñetas preservadas en formato markdown-lite).

## Importar en el teléfono

1. Copia `notas.json` a tu teléfono (por USB, Google Drive, correo, etc.).
2. Abre Notes Pro → **Configuración → Respaldo → Importar y combinar**.
3. Selecciona el archivo `notas.json`.

Las notas nuevas se agregan sin borrar las que ya tengas en la app.
