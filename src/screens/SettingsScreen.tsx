import React from 'react';
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useApp } from '../context/AppContext';
import { LANGUAGE_OPTIONS } from '../i18n/localizedStrings';
import type { AppLanguage } from '../i18n/localizedStrings';
import { colors } from '../theme/colors';

export function SettingsScreen() {
  const {
    strings,
    language,
    settingsState,
    setLanguage,
    autoRegisterFromExcel,
    fetchFromOfficialSite,
  } = useApp();

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title}>{strings.settingsTitle}</Text>

      {settingsState.isImporting && (
        <ActivityIndicator color={colors.lottoPrimary} style={styles.progress} />
      )}

      {settingsState.importMessage && (
        <View
          style={[
            styles.messageCard,
            settingsState.importError ? styles.messageError : styles.messageSuccess,
          ]}
        >
          <Text
            style={[
              styles.messageText,
              settingsState.importError ? styles.messageTextError : styles.messageTextSuccess,
            ]}
          >
            {settingsState.importMessage}
          </Text>
        </View>
      )}

      <Text style={styles.sectionLabel}>{strings.languageLabel}</Text>
      <View style={styles.chipRow}>
        {LANGUAGE_OPTIONS.map((opt) => (
          <Pressable
            key={opt.code}
            style={[styles.chip, language === opt.code && styles.chipSelected]}
            onPress={() => setLanguage(opt.code as AppLanguage)}
          >
            <Text style={[styles.chipText, language === opt.code && styles.chipTextSelected]}>
              {opt.label}
            </Text>
          </Pressable>
        ))}
      </View>

      <Text style={styles.sectionTitle}>{strings.autoRegisterExcel}</Text>
      <Text style={styles.sectionDesc}>{strings.autoRegisterExcelDesc}</Text>
      <Pressable
        style={[styles.primaryButton, settingsState.isImporting && styles.buttonDisabled]}
        onPress={autoRegisterFromExcel}
        disabled={settingsState.isImporting}
      >
        <Text style={styles.primaryButtonText}>{strings.autoRegisterExcel}</Text>
      </Pressable>

      <Text style={styles.sectionTitle}>{strings.fetchOfficial}</Text>
      <Text style={styles.sectionDesc}>{strings.fetchOfficialDesc}</Text>
      <Pressable
        style={[styles.outlineButton, settingsState.isImporting && styles.buttonDisabled]}
        onPress={fetchFromOfficialSite}
        disabled={settingsState.isImporting}
      >
        <Text style={styles.outlineButtonText}>{strings.fetchOfficial}</Text>
      </Pressable>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.lottoBackground },
  content: { padding: 16, gap: 12, paddingBottom: 32 },
  title: { fontSize: 16, fontWeight: 'bold' },
  progress: { marginVertical: 4 },
  messageCard: { borderRadius: 8, padding: 12 },
  messageSuccess: { backgroundColor: colors.primaryContainer },
  messageError: { backgroundColor: colors.errorContainer },
  messageText: { fontSize: 14 },
  messageTextSuccess: { color: colors.onPrimaryContainer },
  messageTextError: { color: colors.onErrorContainer },
  sectionLabel: { fontSize: 14, color: colors.onSurfaceVariant, marginTop: 8 },
  sectionTitle: { fontSize: 14, fontWeight: '600', marginTop: 8 },
  sectionDesc: { fontSize: 12, color: colors.onSurfaceVariant },
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
  chipText: { fontSize: 13 },
  chipTextSelected: { color: '#fff' },
  primaryButton: {
    backgroundColor: colors.lottoPrimary,
    borderRadius: 8,
    padding: 14,
    alignItems: 'center',
  },
  outlineButton: {
    borderWidth: 1,
    borderColor: colors.lottoPrimary,
    borderRadius: 8,
    padding: 14,
    alignItems: 'center',
  },
  buttonDisabled: { opacity: 0.5 },
  primaryButtonText: { color: '#fff', fontWeight: '600' },
  outlineButtonText: { color: colors.lottoPrimary, fontWeight: '500' },
});
