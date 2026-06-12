import React from 'react';
import {
  ActivityIndicator,
  FlatList,
  Modal,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { NumberBalls } from '../components/NumberBalls';
import { useApp } from '../context/AppContext';
import type { WinningNumber } from '../models/types';
import { colors } from '../theme/colors';

export function WinningScreen() {
  const {
    strings,
    winningState,
    openWinningDialog,
    closeWinningDialog,
    updateRoundInput,
    updateDateInput,
    updateNumbersInput,
    saveWinning,
    requestDeleteWinning,
    cancelDeleteWinning,
    confirmDeleteWinning,
    formatLookupTotal,
  } = useApp();

  const renderItem = ({ item }: { item: WinningNumber }) => (
    <View style={styles.card}>
      <View style={styles.cardHeader}>
        <View style={styles.cardInfo}>
          <Text style={styles.round}>{item.roundLabel}</Text>
          <Text style={styles.date}>{item.drawDate}</Text>
        </View>
        <Pressable onPress={() => openWinningDialog(item)} style={styles.iconBtn}>
          <Text style={styles.iconText}>✎</Text>
        </Pressable>
        <Pressable onPress={() => requestDeleteWinning(item)} style={styles.iconBtn}>
          <Text style={styles.iconText}>🗑</Text>
        </Pressable>
      </View>
      <NumberBalls
        numbers={item.numbers.split(',').map((n) => parseInt(n.trim(), 10)).filter((n) => !isNaN(n))}
      />
    </View>
  );

  return (
    <View style={styles.container}>
      {winningState.isSyncing && (
        <ActivityIndicator style={styles.syncBar} color={colors.lottoPrimary} />
      )}
      {winningState.totalCount > 0 && (
        <Text style={styles.total}>{formatLookupTotal(winningState.totalCount)}</Text>
      )}
      {winningState.items.length === 0 ? (
        <View style={styles.empty}>
          <Text style={styles.emptyText}>{strings.emptyWinning}</Text>
        </View>
      ) : (
        <FlatList
          data={winningState.items}
          keyExtractor={(item) => String(item.id)}
          renderItem={renderItem}
          contentContainerStyle={styles.list}
        />
      )}

      <Pressable style={styles.fab} onPress={() => openWinningDialog()}>
        <Text style={styles.fabText}>+</Text>
      </Pressable>

      <Modal visible={winningState.showDialog} transparent animationType="fade">
        <View style={styles.modalOverlay}>
          <View style={styles.modal}>
            <Text style={styles.modalTitle}>
              {winningState.editing ? strings.editWinning : strings.addWinning}
            </Text>
            <TextInput
              style={styles.input}
              value={winningState.roundInput}
              onChangeText={updateRoundInput}
              placeholder={strings.roundLabel}
            />
            <TextInput
              style={styles.input}
              value={winningState.dateInput}
              onChangeText={updateDateInput}
              placeholder={strings.drawDateLabel}
            />
            <TextInput
              style={styles.input}
              value={winningState.numbersInput}
              onChangeText={updateNumbersInput}
              placeholder="01 05 12 17 23 28 34"
            />
            {winningState.errorMessage && (
              <Text style={styles.error}>{strings.invalidNumbers}</Text>
            )}
            <View style={styles.modalActions}>
              <Pressable onPress={closeWinningDialog}>
                <Text style={styles.cancelText}>{strings.cancel}</Text>
              </Pressable>
              <Pressable onPress={saveWinning}>
                <Text style={styles.saveText}>{strings.save}</Text>
              </Pressable>
            </View>
          </View>
        </View>
      </Modal>

      <Modal visible={winningState.confirmDelete != null} transparent animationType="fade">
        <View style={styles.modalOverlay}>
          <View style={styles.modal}>
            <Text style={styles.modalTitle}>{strings.deleteWinning}</Text>
            <Text>{strings.confirmDelete}</Text>
            <View style={styles.modalActions}>
              <Pressable onPress={cancelDeleteWinning}>
                <Text style={styles.cancelText}>{strings.cancel}</Text>
              </Pressable>
              <Pressable onPress={confirmDeleteWinning}>
                <Text style={styles.deleteText}>{strings.delete}</Text>
              </Pressable>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.lottoBackground },
  syncBar: { paddingVertical: 4 },
  total: { paddingHorizontal: 16, paddingVertical: 8, color: colors.onSurfaceVariant },
  list: { padding: 16, gap: 10, paddingBottom: 80 },
  empty: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  emptyText: { color: colors.onSurfaceVariant },
  card: {
    backgroundColor: colors.lottoSurface,
    borderRadius: 12,
    padding: 14,
    marginBottom: 10,
  },
  cardHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 8 },
  cardInfo: { flex: 1 },
  round: { fontWeight: 'bold', fontSize: 15 },
  date: { fontSize: 12, color: colors.onSurfaceVariant },
  iconBtn: { padding: 8 },
  iconText: { fontSize: 18 },
  fab: {
    position: 'absolute',
    right: 20,
    bottom: 20,
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: colors.lottoPrimary,
    alignItems: 'center',
    justifyContent: 'center',
    elevation: 4,
  },
  fabText: { color: '#fff', fontSize: 28, lineHeight: 32 },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    padding: 24,
  },
  modal: { backgroundColor: '#fff', borderRadius: 12, padding: 20, gap: 10 },
  modalTitle: { fontSize: 18, fontWeight: 'bold', marginBottom: 4 },
  input: {
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 8,
    padding: 10,
    fontSize: 15,
  },
  error: { color: colors.error, fontSize: 13 },
  modalActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 20, marginTop: 8 },
  cancelText: { color: colors.onSurfaceVariant, fontSize: 15 },
  saveText: { color: colors.lottoPrimary, fontWeight: '600', fontSize: 15 },
  deleteText: { color: colors.error, fontWeight: '600', fontSize: 15 },
});
