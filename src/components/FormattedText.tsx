import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { findMatchLineIndex, parseFormattedLines, splitByHighlight } from '@/utils/richText';
import { useAppTheme } from '@/theme/ThemeContext';

interface Props {
  content: string;
  color?: string;
  fontSize?: number;
  numberOfLines?: number;
  /** Search term to highlight; also used to pick which lines to show when numberOfLines truncates the preview. */
  highlight?: string;
}

export function FormattedText({ content, color, fontSize, numberOfLines, highlight }: Props) {
  const { colors, fontSizes } = useAppTheme();
  const textColor = color ?? colors.text;
  const size = fontSize ?? fontSizes.md;
  const lines = parseFormattedLines(content);

  let visibleLines = lines;
  if (numberOfLines && lines.length > numberOfLines) {
    const matchLine = highlight ? findMatchLineIndex(content, highlight) : -1;
    const start = matchLine > 0 ? Math.min(matchLine, lines.length - numberOfLines) : 0;
    visibleLines = lines.slice(start, start + numberOfLines);
  }

  return (
    <View>
      {visibleLines.map((line, index) => {
        const lineSize = line.heading ? size * 1.15 : size;
        const prefix = line.bullet ? '•  ' : line.numbered !== null ? `${line.numbered}.  ` : null;

        return (
          <View
            key={index}
            style={[styles.lineRow, { marginBottom: line.heading ? 6 : 3, marginTop: line.heading && index > 0 ? 6 : 0 }]}
          >
            {prefix && <Text style={{ color: textColor, fontSize: lineSize, fontWeight: line.heading ? '800' : '400' }}>{prefix}</Text>}
            <Text style={{ flexShrink: 1 }} numberOfLines={numberOfLines ? 1 : undefined}>
              {line.segments.map((segment, segIndex) =>
                splitByHighlight(segment.text, highlight).map((part, partIndex) => {
                  const textDecorationLine =
                    segment.underline && segment.strikethrough
                      ? ('underline line-through' as const)
                      : segment.underline
                        ? ('underline' as const)
                        : segment.strikethrough
                          ? ('line-through' as const)
                          : ('none' as const);

                  return (
                    <Text
                      key={`${segIndex}-${partIndex}`}
                      style={{
                        color: part.highlighted ? colors.text : textColor,
                        fontSize: lineSize,
                        fontWeight: segment.bold || line.heading ? '700' : '400',
                        fontStyle: segment.italic ? 'italic' : 'normal',
                        textDecorationLine,
                        backgroundColor: part.highlighted ? colors.warning + '66' : 'transparent',
                      }}
                    >
                      {part.text}
                    </Text>
                  );
                })
              )}
            </Text>
          </View>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  lineRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
  },
});
