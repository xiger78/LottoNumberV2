import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { NavigationContainer } from '@react-navigation/native';
import { StatusBar } from 'expo-status-bar';
import React, { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';
import { LottoBanner } from './src/components/LottoBanner';
import { AppProvider, useApp } from './src/context/AppContext';
import { HistoryScreen } from './src/screens/HistoryScreen';
import { LookupScreen } from './src/screens/LookupScreen';
import { LottoScreen } from './src/screens/LottoScreen';
import { SettingsScreen } from './src/screens/SettingsScreen';
import { WinningScreen } from './src/screens/WinningScreen';
import { colors } from './src/theme/colors';

const Tab = createBottomTabNavigator();

function TabIcon({ label, focused }: { label: string; focused: boolean }) {
  const icons: Record<string, string> = {
    Lotto: '🎲',
    Winning: '⭐',
    Lookup: '🔍',
    History: '📋',
    Settings: '⚙️',
  };
  return (
    <Text style={{ fontSize: 18, opacity: focused ? 1 : 0.6 }}>{icons[label] ?? '•'}</Text>
  );
}

function MainTabs() {
  const { strings, refreshWinningNumbers, loadHistoryPage, loadLookupPage } = useApp();
  const [headerTitle, setHeaderTitle] = useState(strings.navLotto);

  return (
    <SafeAreaView style={styles.safe} edges={['top']}>
      <StatusBar style="light" />
      <View style={styles.header}>
        <Text style={styles.headerTitle}>{headerTitle}</Text>
      </View>
      <LottoBanner />
      <Tab.Navigator
        screenOptions={({ route }) => ({
          headerShown: false,
          tabBarActiveTintColor: colors.lottoPrimary,
          tabBarInactiveTintColor: colors.onSurfaceVariant,
          tabBarStyle: styles.tabBar,
          tabBarIcon: ({ focused }) => <TabIcon label={route.name} focused={focused} />,
        })}
      >
        <Tab.Screen
          name="Lotto"
          component={LottoScreen}
          options={{ title: strings.navLotto }}
          listeners={{ focus: () => setHeaderTitle(strings.navLotto) }}
        />
        <Tab.Screen
          name="Winning"
          component={WinningScreen}
          options={{ title: strings.navWinning }}
          listeners={{
            focus: () => setHeaderTitle(strings.navWinning),
            tabPress: () => refreshWinningNumbers(),
          }}
        />
        <Tab.Screen
          name="Lookup"
          component={LookupScreen}
          options={{ title: strings.navLookup }}
          listeners={{
            focus: () => setHeaderTitle(strings.navLookup),
            tabPress: () => loadLookupPage(0),
          }}
        />
        <Tab.Screen
          name="History"
          component={HistoryScreen}
          options={{ title: strings.navHistory }}
          listeners={{
            focus: () => setHeaderTitle(strings.navHistory),
            tabPress: () => loadHistoryPage(0),
          }}
        />
        <Tab.Screen
          name="Settings"
          component={SettingsScreen}
          options={{ title: strings.navSettings }}
          listeners={{ focus: () => setHeaderTitle(strings.navSettings) }}
        />
      </Tab.Navigator>
    </SafeAreaView>
  );
}

export default function App() {
  return (
    <SafeAreaProvider>
      <AppProvider>
        <NavigationContainer>
          <MainTabs />
        </NavigationContainer>
      </AppProvider>
    </SafeAreaProvider>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.lottoPrimary },
  header: {
    backgroundColor: colors.lottoPrimary,
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  headerTitle: {
    color: colors.lottoSurface,
    fontSize: 18,
    fontWeight: 'bold',
  },
  tabBar: {
    backgroundColor: colors.lottoSurface,
    borderTopColor: '#e0e0e0',
  },
});
