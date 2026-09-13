import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { parseFormattedLines } from '@/utils/richText';
import { useAppTheme } from '@/theme/ThemeContext';

interface Props {
  content: string;
  color?: string;
  fontSize?: number;
  numberOfLines?: number;
}

export function FormattedText({ content, color, fontSize, numberOfLines }: Props) {
  const { colors, fontSizes } = useAppTheme();
  const textColor = color ?? colors.text;
  const size = fontSize ?? fontSizes.md;
  const lines = parseFormattedLines(content);
  const visibleLines = numberOfLines ? lines.slice(0, numberOfLines) : lines;

  return (
    <View>
      {visibleLines.map((line, index) => (
        <View key={index} style={styles.lineRow}>
          {line.bullet && <Text style={{ color: textColor, fontSize: size }}>{'•  '}</Text>}
          <Text style={{ flexShrink: 1 }} numberOfLines={numberOfLines ? 1 : undefined}>
            {line.segments.map((segment, segIndex) => (
              <Text
                key={segIndex}
                style={{
                  color: textColor,
                  fontSize: size,
                  fontWeight: segment.bold ? '700' : '400',
                  fontStyle: segment.italic ? 'italic' : 'normal',
                }}
              >
                {segment.text}
              </Text>
            ))}
          </Text>
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  lineRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
  },
});
