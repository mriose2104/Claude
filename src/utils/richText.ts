export interface InlineSegment {
  text: string;
  bold: boolean;
  italic: boolean;
}

export interface FormattedLine {
  bullet: boolean;
  segments: InlineSegment[];
}

const INLINE_REGEX = /(\*\*([^*]+)\*\*)|(\*([^*]+)\*)/g;

export function parseInlineSegments(line: string): InlineSegment[] {
  const segments: InlineSegment[] = [];
  let lastIndex = 0;
  let match: RegExpExecArray | null;
  INLINE_REGEX.lastIndex = 0;

  while ((match = INLINE_REGEX.exec(line))) {
    if (match.index > lastIndex) {
      segments.push({ text: line.slice(lastIndex, match.index), bold: false, italic: false });
    }
    if (match[1]) {
      segments.push({ text: match[2], bold: true, italic: false });
    } else if (match[3]) {
      segments.push({ text: match[4], bold: false, italic: true });
    }
    lastIndex = INLINE_REGEX.lastIndex;
  }
  if (lastIndex < line.length) {
    segments.push({ text: line.slice(lastIndex), bold: false, italic: false });
  }
  return segments.length > 0 ? segments : [{ text: line, bold: false, italic: false }];
}

export function parseFormattedLines(content: string): FormattedLine[] {
  return content.split('\n').map((line) => {
    const bullet = line.startsWith('- ');
    const rest = bullet ? line.slice(2) : line;
    return { bullet, segments: parseInlineSegments(rest) };
  });
}

/** Strips markdown-lite tokens for use in plain-text previews and search matching. */
export function stripFormatting(content: string): string {
  return content
    .split('\n')
    .map((line) => (line.startsWith('- ') ? line.slice(2) : line))
    .join(' ')
    .replace(/\*\*([^*]+)\*\*/g, '$1')
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
