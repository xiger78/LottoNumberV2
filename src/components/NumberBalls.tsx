import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { colors } from '../theme/colors';

interface Props {
  numbers: number[];
  ballSize?: number;
}

export function NumberBalls({ numbers, ballSize = 36 }: Props) {
  return (
    <View style={styles.row}>
      {numbers.map((num) => (
        <View
          key={num}
          style={[
            styles.ball,
            { width: ballSize, height: ballSize, borderRadius: ballSize / 2 },
          ]}
        >
          <Text style={[styles.ballText, { fontSize: ballSize * 0.36 }]}>
            {String(num).padStart(2, '0')}
          </Text>
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  ball: {
    backgroundColor: colors.ballColor,
    alignItems: 'center',
    justifyContent: 'center',
  },
  ballText: {
    color: colors.ballText,
    fontWeight: 'bold',
  },
});
