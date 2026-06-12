import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { useApp } from '../context/AppContext';
import { colors } from '../theme/colors';

const BANNER_BALLS = [
  { color: '#E53935', label: '07' },
  { color: '#FB8C00', label: '15' },
  { color: '#FDD835', label: '23' },
  { color: '#43A047', label: '31' },
  { color: '#1E88E5', label: '37' },
];

export function LottoBanner() {
  const { strings } = useApp();

  return (
    <View style={styles.banner}>
      <View style={styles.content}>
        <View>
          <Text style={styles.lotoTitle}>LOTO 7</Text>
          <Text style={styles.appName}>{strings.appName}</Text>
        </View>
        <View style={styles.ballsRow}>
          {BANNER_BALLS.map((ball, index) => (
            <View
              key={ball.label}
              style={[
                styles.bannerBall,
                { backgroundColor: ball.color, marginLeft: index > 0 ? -6 : 0 },
              ]}
            >
              <Text style={styles.bannerBallText}>{ball.label}</Text>
            </View>
          ))}
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  banner: {
    backgroundColor: colors.lottoPrimary,
    height: 88,
    justifyContent: 'center',
  },
  content: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 10,
  },
  lotoTitle: {
    color: colors.lottoSecondary,
    fontWeight: '900',
    fontSize: 22,
    letterSpacing: 2,
  },
  appName: {
    color: '#FFFFFF',
    fontWeight: 'bold',
    fontSize: 14,
  },
  ballsRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  bannerBall: {
    width: 34,
    height: 34,
    borderRadius: 17,
    alignItems: 'center',
    justifyContent: 'center',
  },
  bannerBallText: {
    color: '#FFFFFF',
    fontWeight: 'bold',
    fontSize: 11,
  },
});
