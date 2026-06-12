import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { colors } from '../theme/colors';

interface Props {
  label: string;
  value: string;
}

export function SummaryRow({ label, value }: Props) {
  return (
    <View style={styles.row}>
      <Text style={styles.label}>{label}</Text>
      <Text style={styles.value}>{value}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    paddingVertical: 3,
  },
  label: {
    color: colors.onSurfaceVariant,
    marginRight: 8,
    fontSize: 14,
  },
  value: {
    flex: 1,
    fontSize: 14,
  },
});
