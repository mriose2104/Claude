import React from 'react';
import { Text, TextProps } from 'react-native';
import { splitByHighlight } from '@/utils/richText';
import { useAppTheme } from '@/theme/ThemeContext';

interface Props extends TextProps {
  text: string;
  highlight?: string;
  color: string;
}

/** Renders plain (non-markdown) text with the matching part of `highlight` given a colored background. */
export function HighlightedInlineText({ text, highlight, color, style, ...rest }: Props) {
  const { colors } = useAppTheme();
  const parts = splitByHighlight(text, highlight);

  return (
    <Text style={style} {...rest}>
      {parts.map((part, index) => (
        <Text
          key={index}
          style={{
            color,
            backgroundColor: part.highlighted ? colors.warning + '66' : 'transparent',
          }}
        >
          {part.text}
        </Text>
      ))}
    </Text>
  );
}
