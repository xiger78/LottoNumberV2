import * as Linking from 'expo-linking';
import React from 'react';
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { LottoBanner } from '../components/LottoBanner';
import { NumberBalls } from '../components/NumberBalls';
import { SummaryRow } from '../components/SummaryRow';
import { useApp } from '../context/AppContext';
import { OFFICIAL_LOTO7_URL } from '../models/types';
import { colors } from '../theme/colors';

export function LottoScreen() {
  const { strings, lottoState, regenerate, setMonth } = useApp();

  if (lottoState.isLoading) {
    return (
      <View style={styles.center}>
        {lottoState.errorMessage ? (
          <Text style={styles.error}>{lottoState.errorMessage}</Text>
        ) : (
          <ActivityIndicator color={colors.lottoPrimary} size="large" />
        )}
      </View>
    );
  }

  const summary = lottoState.summary;
  if (!summary) return null;

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Pressable style={styles.outlineButton} onPress={() => Linking.openURL(OFFICIAL_LOTO7_URL)}>
        <Text style={styles.outlineButtonText}>{strings.openOfficialSite}</Text>
      </Pressable>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>{strings.patternAnalysis}</Text>
        <SummaryRow label={strings.analysisDraws} value={`${summary.totalDraws}${strings.times}`} />
        <SummaryRow
          label={strings.latestRound}
          value={`${summary.latestRound} / ${summary.latestDate}`}
        />
        <SummaryRow
          label={strings.latestWinning}
          value={summary.latestNums.map((n) => String(n).padStart(2, '0')).join(' ')}
        />
        <SummaryRow label={strings.hotNumbers} value={summary.hotNumbers.join(', ')} />
        <SummaryRow label={strings.coldNumbers} value={summary.coldNumbers.join(', ')} />
        <SummaryRow
          label={strings.sumAverage}
          value={`${summary.sumAverage.toFixed(1)} (${summary.sumMin}~${summary.sumMax})`}
        />
        <SummaryRow
          label={strings.oddDistribution}
          value={summary.topOddPatterns.map(([k, v]) => `${k}(${v})`).join(', ')}
        />
        {lottoState.savedWinningCount > 0 && (
          <Text style={styles.hint}>
            {strings.winningConsidered} ({lottoState.savedWinningCount})
          </Text>
        )}
      </View>

      <Text style={styles.sectionTitle}>{strings.monthPattern}</Text>
      <View style={styles.chipRow}>
        {Array.from({ length: 12 }, (_, i) => i + 1).map((month) => (
          <Pressable
            key={month}
            style={[styles.chip, lottoState.selectedMonth === month && styles.chipSelected]}
            onPress={() => setMonth(month)}
          >
            <Text
              style={[
                styles.chipText,
                lottoState.selectedMonth === month && styles.chipTextSelected,
              ]}
            >
              {strings.monthLabel(month)}
            </Text>
          </Pressable>
        ))}
      </View>

      <Pressable style={styles.primaryButton} onPress={regenerate}>
        <Text style={styles.primaryButtonText}>{strings.generateButton}</Text>
      </Pressable>

      {lottoState.combos.length > 0 && (
        <>
          <Text style={styles.cardTitle}>
            {strings.recommendedCombos} {lottoState.combos.length}
          </Text>
          {lottoState.combos.map((combo) => (
            <View key={combo.index} style={styles.comboCard}>
              <Text style={styles.comboMeta}>
                {combo.index}. {strings.sumLabel}:{combo.sum} {strings.oddLabel}:{combo.oddCount}{' '}
                {strings.kouLabel}:{combo.kouDistribution.join(',')}
              </Text>
              <NumberBalls numbers={combo.numbers} />
            </View>
          ))}
        </>
      )}

      <Text style={styles.disclaimer}>{strings.disclaimer}</Text>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.lottoBackground },
  content: { padding: 16, gap: 12, paddingBottom: 32 },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  error: { color: colors.error, textAlign: 'center', padding: 16 },
  card: {
    backgroundColor: colors.lottoSurface,
    borderRadius: 12,
    padding: 16,
    elevation: 2,
    shadowColor: '#000',
    shadowOpacity: 0.1,
    shadowRadius: 4,
    shadowOffset: { width: 0, height: 2 },
  },
  cardTitle: { fontSize: 16, fontWeight: 'bold', marginBottom: 8 },
  sectionTitle: { fontSize: 14, fontWeight: '600' },
  hint: { fontSize: 12, color: colors.lottoPrimary, marginTop: 4 },
  chipRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  chip: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#ccc',
    backgroundColor: colors.lottoSurface,
  },
  chipSelected: { backgroundColor: colors.lottoPrimary, borderColor: colors.lottoPrimary },
  chipText: { fontSize: 13, color: '#333' },
  chipTextSelected: { color: '#fff' },
  primaryButton: {
    backgroundColor: colors.lottoPrimary,
    borderRadius: 8,
    padding: 14,
    alignItems: 'center',
  },
  primaryButtonText: { color: '#fff', fontWeight: '600', fontSize: 15 },
  outlineButton: {
    borderWidth: 1,
    borderColor: colors.lottoPrimary,
    borderRadius: 8,
    padding: 12,
    alignItems: 'center',
  },
  outlineButtonText: { color: colors.lottoPrimary, fontWeight: '500' },
  comboCard: {
    backgroundColor: colors.lottoSurface,
    borderRadius: 12,
    padding: 14,
    gap: 10,
  },
  comboMeta: { fontSize: 12, color: colors.onSurfaceVariant },
  disclaimer: { fontSize: 12, color: colors.onSurfaceVariant, marginTop: 8 },
});
