import React, { useEffect } from 'react';
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useApp } from '../context/AppContext';
import type { GenerationHistory } from '../models/types';
import { colors } from '../theme/colors';

export function HistoryScreen() {
  const { strings, historyState, loadHistoryPage, formatHistoryLine, formatPageInfo } = useApp();

  useEffect(() => {
    loadHistoryPage(0);
  }, [loadHistoryPage]);

  const renderItem = ({ item }: { item: GenerationHistory }) => {
    const nums = item.numbers.split(',').map((n) => parseInt(n.trim(), 10)).filter((n) => !isNaN(n));
    return (
      <View style={styles.card}>
        <Text style={styles.line}>{formatHistoryLine(item.createdAt, nums)}</Text>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>{strings.historyTitle}</Text>

      {historyState.isLoading ? (
        <ActivityIndicator style={styles.loader} color={colors.lottoPrimary} />
      ) : historyState.items.length === 0 ? (
        <View style={styles.empty}>
          <Text style={styles.emptyText}>{strings.historyEmpty}</Text>
        </View>
      ) : (
        <>
          <FlatList
            data={historyState.items}
            keyExtractor={(item) => String(item.id)}
            renderItem={renderItem}
            contentContainerStyle={styles.list}
          />
          <View style={styles.pagination}>
            <Pressable
              style={[styles.pageBtn, historyState.currentPage === 0 && styles.pageBtnDisabled]}
              onPress={() => loadHistoryPage(historyState.currentPage - 1)}
              disabled={historyState.currentPage === 0}
            >
              <Text style={styles.pageBtnText}>{strings.prevPage}</Text>
            </Pressable>
            <Text style={styles.pageInfo}>
              {formatPageInfo(historyState.currentPage + 1, historyState.totalPages)}
            </Text>
            <Pressable
              style={[
                styles.pageBtnPrimary,
                historyState.currentPage >= historyState.totalPages - 1 && styles.pageBtnDisabled,
              ]}
              onPress={() => loadHistoryPage(historyState.currentPage + 1)}
              disabled={historyState.currentPage >= historyState.totalPages - 1}
            >
              <Text style={styles.pageBtnPrimaryText}>{strings.nextPage}</Text>
            </Pressable>
          </View>
        </>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.lottoBackground, padding: 16 },
  title: { fontSize: 16, fontWeight: 'bold', marginBottom: 12 },
  loader: { marginTop: 40 },
  empty: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  emptyText: { color: colors.onSurfaceVariant },
  list: { gap: 8, paddingBottom: 8 },
  card: {
    backgroundColor: colors.lottoSurface,
    borderRadius: 8,
    padding: 12,
    marginBottom: 8,
  },
  line: { fontFamily: 'monospace', fontSize: 14 },
  pagination: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 12,
  },
  pageBtn: {
    borderWidth: 1,
    borderColor: colors.lottoPrimary,
    borderRadius: 8,
    paddingHorizontal: 16,
    paddingVertical: 8,
  },
  pageBtnPrimary: {
    backgroundColor: colors.lottoPrimary,
    borderRadius: 8,
    paddingHorizontal: 16,
    paddingVertical: 8,
  },
  pageBtnDisabled: { opacity: 0.4 },
  pageBtnText: { color: colors.lottoPrimary },
  pageBtnPrimaryText: { color: '#fff' },
  pageInfo: { fontSize: 14 },
});
