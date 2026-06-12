import React from 'react';
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { NumberBalls } from '../components/NumberBalls';
import { useApp } from '../context/AppContext';
import type { Draw } from '../models/types';
import { colors } from '../theme/colors';

export function LookupScreen() {
  const {
    strings,
    lookupState,
    setLookupSearch,
    loadLookupPage,
    formatLookupTotal,
    formatPageInfo,
  } = useApp();

  const renderItem = ({ item }: { item: Draw }) => (
    <View style={styles.card}>
      <Text style={styles.round}>{item.round}</Text>
      <Text style={styles.date}>{item.date}</Text>
      <NumberBalls numbers={item.nums} ballSize={32} />
    </View>
  );

  return (
    <View style={styles.container}>
      <Text style={styles.title}>{strings.lookupTitle}</Text>
      <Text style={styles.total}>{formatLookupTotal(lookupState.totalCount)}</Text>
      <TextInput
        style={styles.search}
        value={lookupState.searchQuery}
        onChangeText={setLookupSearch}
        placeholder={strings.lookupSearchHint}
      />

      {lookupState.isLoading ? (
        <ActivityIndicator style={styles.loader} color={colors.lottoPrimary} />
      ) : lookupState.items.length === 0 ? (
        <View style={styles.empty}>
          <Text style={styles.emptyText}>{strings.lookupEmpty}</Text>
        </View>
      ) : (
        <>
          <FlatList
            data={lookupState.items}
            keyExtractor={(item) => item.round}
            renderItem={renderItem}
            contentContainerStyle={styles.list}
          />
          <View style={styles.pagination}>
            <Pressable
              style={[styles.pageBtn, lookupState.currentPage === 0 && styles.pageBtnDisabled]}
              onPress={() => loadLookupPage(lookupState.currentPage - 1)}
              disabled={lookupState.currentPage === 0}
            >
              <Text style={styles.pageBtnText}>{strings.prevPage}</Text>
            </Pressable>
            <Text style={styles.pageInfo}>
              {formatPageInfo(lookupState.currentPage + 1, lookupState.totalPages)}
            </Text>
            <Pressable
              style={[
                styles.pageBtnPrimary,
                lookupState.currentPage >= lookupState.totalPages - 1 && styles.pageBtnDisabled,
              ]}
              onPress={() => loadLookupPage(lookupState.currentPage + 1)}
              disabled={lookupState.currentPage >= lookupState.totalPages - 1}
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
  title: { fontSize: 16, fontWeight: 'bold' },
  total: { fontSize: 12, color: colors.onSurfaceVariant, marginVertical: 8 },
  search: {
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 8,
    padding: 10,
    backgroundColor: colors.lottoSurface,
    marginBottom: 12,
  },
  loader: { marginTop: 40 },
  empty: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  emptyText: { color: colors.onSurfaceVariant },
  list: { gap: 8, paddingBottom: 8 },
  card: {
    backgroundColor: colors.lottoSurface,
    borderRadius: 12,
    padding: 14,
    marginBottom: 8,
    gap: 6,
  },
  round: { fontWeight: 'bold' },
  date: { fontSize: 12, color: colors.onSurfaceVariant },
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
