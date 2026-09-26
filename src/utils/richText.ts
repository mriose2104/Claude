export interface InlineSegment {
  text: string;
  bold: boolean;
  italic: boolean;
  underline: boolean;
  strikethrough: boolean;
}

export interface FormattedLine {
  bullet: boolean;
  heading: boolean;
  numbered: number | null;
  segments: InlineSegment[];
}

const INLINE_REGEX = /(\*\*([^*]+)\*\*)|(~~([^~]+)~~)|(__([^_]+)__)|(\*([^*]+)\*)/g;
const NUMBERED_LINE_REGEX = /^(\d+)\.\s(.*)$/;

export function parseInlineSegments(line: string): InlineSegment[] {
  const segments: InlineSegment[] = [];
  let lastIndex = 0;
  let match: RegExpExecArray | null;
  INLINE_REGEX.lastIndex = 0;

  const plain = (text: string): InlineSegment => ({ text, bold: false, italic: false, underline: false, strikethrough: false });

  while ((match = INLINE_REGEX.exec(line))) {
    if (match.index > lastIndex) {
      segments.push(plain(line.slice(lastIndex, match.index)));
    }
    if (match[1]) {
      segments.push({ ...plain(match[2]), bold: true });
    } else if (match[3]) {
      segments.push({ ...plain(match[4]), strikethrough: true });
    } else if (match[5]) {
      segments.push({ ...plain(match[6]), underline: true });
    } else if (match[7]) {
      segments.push({ ...plain(match[8]), italic: true });
    }
    lastIndex = INLINE_REGEX.lastIndex;
  }
  if (lastIndex < line.length) {
    segments.push(plain(line.slice(lastIndex)));
  }
  return segments.length > 0 ? segments : [plain(line)];
}

export function parseFormattedLines(content: string): FormattedLine[] {
  return content.split('\n').map((line) => {
    if (line.startsWith('# ')) {
      return { bullet: false, heading: true, numbered: null, segments: parseInlineSegments(line.slice(2)) };
    }
    const numberedMatch = line.match(NUMBERED_LINE_REGEX);
    if (numberedMatch) {
      return {
        bullet: false,
        heading: false,
        numbered: parseInt(numberedMatch[1], 10),
        segments: parseInlineSegments(numberedMatch[2]),
      };
    }
    if (line.startsWith('- ')) {
      return { bullet: true, heading: false, numbered: null, segments: parseInlineSegments(line.slice(2)) };
    }
    return { bullet: false, heading: false, numbered: null, segments: parseInlineSegments(line) };
  });
}

export interface HighlightSegment {
  text: string;
  highlighted: boolean;
}

/** Splits text into segments so a search query can be rendered with a highlight background. */
export function splitByHighlight(text: string, query?: string): HighlightSegment[] {
  if (!query || !query.trim()) return [{ text, highlighted: false }];
  const lower = text.toLowerCase();
  const q = query.toLowerCase();
  const segments: HighlightSegment[] = [];
  let idx = 0;
  while (idx < text.length) {
    const found = lower.indexOf(q, idx);
    if (found === -1) {
      segments.push({ text: text.slice(idx), highlighted: false });
      break;
    }
    if (found > idx) segments.push({ text: text.slice(idx, found), highlighted: false });
    segments.push({ text: text.slice(found, found + q.length), highlighted: true });
    idx = found + q.length;
  }
  return segments;
}

/** Index (in `content.split('\n')`) of the first line containing `query`, or -1. */
export function findMatchLineIndex(content: string, query: string): number {
  if (!query.trim()) return -1;
  const q = query.toLowerCase();
  return content.split('\n').findIndex((line) => line.toLowerCase().includes(q));
}

/** First case-insensitive occurrence of `query` within `content`, as a selection range. */
export function findMatchRange(content: string, query: string): { start: number; end: number } | null {
  if (!query.trim()) return null;
  const idx = content.toLowerCase().indexOf(query.toLowerCase());
  if (idx === -1) return null;
  return { start: idx, end: idx + query.length };
}

/** Strips markdown-lite tokens for use in plain-text previews and search matching. */
export function stripFormatting(content: string): string {
  return content
    .split('\n')
    .map((line) => {
      if (line.startsWith('# ')) return line.slice(2);
      const numberedMatch = line.match(NUMBERED_LINE_REGEX);
      if (numberedMatch) return numberedMatch[2];
      return line.startsWith('- ') ? line.slice(2) : line;
    })
    .join(' ')
    .replace(/\*\*([^*]+)\*\*/g, '$1')
    .replace(/~~([^~]+)~~/g, '$1')
    .replace(/__([^_]+)__/g, '$1')
    .replace(/\*([^*]+)\*/g, '$1');
}

export function wrapSelection(
  text: string,
  selStart: number,
  selEnd: number,
  token: string
): { text: string; selStart: number; selEnd: number } {
  const before = text.slice(0, selStart);
  const selected = text.slice(selStart, selEnd);
  const after = text.slice(selEnd);
  const next = `${before}${token}${selected}${token}${after}`;
  return { text: next, selStart: selStart + token.length, selEnd: selEnd + token.length };
}

export function prefixLines(
  text: string,
  selStart: number,
  selEnd: number,
  prefix: string
): { text: string; selStart: number; selEnd: number } {
  const before = text.slice(0, selStart);
  const lineStart = before.lastIndexOf('\n') + 1;
  const head = text.slice(0, lineStart);
  const affected = text.slice(lineStart, selEnd);
  const tail = text.slice(selEnd);
  const prefixedAffected = affected
    .split('\n')
    .map((line) => (line.startsWith(prefix) ? line : prefix + line))
    .join('\n');
  const next = head + prefixedAffected + tail;
  const delta = prefixedAffected.length - affected.length;
  return { text: next, selStart: selStart + prefix.length, selEnd: selEnd + delta };
}

/** Turns each line touched by the selection into a sequential numbered-list item ("1. ", "2. ", ...). */
export function numberLines(
  text: string,
  selStart: number,
  selEnd: number
): { text: string; selStart: number; selEnd: number } {
  const before = text.slice(0, selStart);
  const lineStart = before.lastIndexOf('\n') + 1;
  const head = text.slice(0, lineStart);
  const affected = text.slice(lineStart, selEnd);
  const tail = text.slice(selEnd);
  const lines = affected.split('\n');
  const renumbered = lines.map((line, i) => `${i + 1}. ${line.replace(NUMBERED_LINE_REGEX, '$2')}`).join('\n');
  const next = head + renumbered + tail;
  const delta = renumbered.length - affected.length;
  const firstLineDelta = (renumbered.split('\n')[0]?.length ?? 0) - (lines[0]?.length ?? 0);
  return { text: next, selStart: selStart + firstLineDelta, selEnd: selEnd + delta };
}
