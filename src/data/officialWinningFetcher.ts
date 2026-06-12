import type { FetchedWinning } from '../models/types';

const USER_AGENT =
  'Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36';

const MAIN_PAGE_URL =
  'https://www.mizuhobank.co.jp/takarakuji/check/loto/loto7/index.html';

export function backnumberUrl(round: number): string {
  return `https://www.mizuhobank.co.jp/takarakuji/check/loto/backnumber/detail.html?fromto=${round}&loto=7`;
}

export function parseRoundNumber(label: string): number {
  const match = label.match(/第(\d+)回/);
  return match ? parseInt(match[1], 10) : 0;
}

async function fetchHtml(url: string): Promise<string | null> {
  try {
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'User-Agent': USER_AGENT,
        Accept: 'text/html,application/xhtml+xml',
        'Accept-Language': 'ja-JP,ja;q=0.9,en;q=0.8',
      },
    });
    if (response.status < 200 || response.status >= 300) return null;
    return await response.text();
  } catch {
    return null;
  }
}

function extractMainNumbers(text: string): number[] {
  const cleaned = text
    .replace(/<script[\s\S]*?<\/script>/gi, ' ')
    .replace(/<style[\s\S]*?<\/style>/gi, ' ')
    .replace(/<[^>]+>/g, ' ')
    .replace(/&nbsp;/g, ' ');

  const candidates = [...cleaned.matchAll(/\b([0-9]{1,2})\b/g)]
    .map((m) => parseInt(m[1], 10))
    .filter((n) => n >= 1 && n <= 37);

  const mainSection = cleaned.match(/本数字[\s\S]{0,400}/);
  if (mainSection) {
    const fromSection = [
      ...new Set(
        [...mainSection[0].matchAll(/\b([0-9]{1,2})\b/g)]
          .map((m) => parseInt(m[1], 10))
          .filter((n) => n >= 1 && n <= 37)
      ),
    ].slice(0, 7);
    if (fromSection.length === 7) return fromSection.sort((a, b) => a - b);
  }

  return [...new Set(candidates)].slice(0, 7);
}

export function parseFromHtml(html: string): FetchedWinning[] {
  if (html.includes('Access Denied')) return [];

  const results: FetchedWinning[] = [];
  const roundPattern = /第(\d+)回/g;
  const datePattern = /(\d{4})年(\d{1,2})月(\d{1,2})日/g;

  const roundMatches = [...new Set([...html.matchAll(roundPattern)].map((m) => m[0]))].slice(0, 5);
  for (const roundLabel of roundMatches) {
    const roundIndex = html.indexOf(roundLabel);
    if (roundIndex < 0) continue;
    const chunk = html.substring(roundIndex, Math.min(roundIndex + 2500, html.length));
    const dateMatch = chunk.match(/(\d{4})年(\d{1,2})月(\d{1,2})日/);
    if (!dateMatch) continue;
    const date = dateMatch[0];
    const numbers = extractMainNumbers(chunk);
    if (numbers.length === 7) {
      results.push({ roundLabel, drawDate: date, numbers: numbers.sort((a, b) => a - b) });
    }
  }

  if (results.length > 0) {
    const seen = new Set<string>();
    return results.filter((r) => {
      if (seen.has(r.roundLabel)) return false;
      seen.add(r.roundLabel);
      return true;
    });
  }

  const dateMatch = html.match(/(\d{4})年(\d{1,2})月(\d{1,2})日/);
  const roundMatch = html.match(/第(\d+)回/);
  if (!dateMatch || !roundMatch) return [];
  const numbers = extractMainNumbers(html);
  if (numbers.length === 7) {
    return [{ roundLabel: roundMatch[0], drawDate: dateMatch[0], numbers: numbers.sort((a, b) => a - b) }];
  }
  return [];
}

function guessLatestRound(html: string): number {
  const matches = [...html.matchAll(/第(\d+)回/g)];
  const nums = matches.map((m) => parseInt(m[1], 10)).filter((n) => !isNaN(n));
  return nums.length > 0 ? Math.max(...nums) : 0;
}

export async function fetchLatest(): Promise<FetchedWinning[]> {
  const html = await fetchHtml(MAIN_PAGE_URL);
  if (!html) throw new Error('fetch_failed');
  const parsed = parseFromHtml(html);
  if (parsed.length > 0) return parsed;

  const latestRound = guessLatestRound(html);
  if (latestRound > 0) {
    const detailHtml = await fetchHtml(backnumberUrl(latestRound));
    if (!detailHtml) throw new Error('fetch_failed');
    const detailParsed = parseFromHtml(detailHtml);
    if (detailParsed.length > 0) return detailParsed;
  }
  throw new Error('parse_failed');
}

export async function fetchMissingAfter(lastKnownRound: number): Promise<FetchedWinning[]> {
  if (lastKnownRound <= 0) {
    try {
      return await fetchLatest();
    } catch {
      return [];
    }
  }

  const results: FetchedWinning[] = [];
  const mainHtml = await fetchHtml(MAIN_PAGE_URL);
  if (mainHtml && !mainHtml.includes('Access Denied')) {
    results.push(
      ...parseFromHtml(mainHtml).filter((item) => parseRoundNumber(item.roundLabel) > lastKnownRound)
    );
  }

  const latestRound =
    results.length > 0
      ? Math.max(...results.map((r) => parseRoundNumber(r.roundLabel)))
      : mainHtml
        ? guessLatestRound(mainHtml)
        : lastKnownRound;

  if (latestRound <= lastKnownRound) {
    const seen = new Set<string>();
    return results.filter((r) => {
      if (seen.has(r.roundLabel)) return false;
      seen.add(r.roundLabel);
      return true;
    });
  }

  for (let round = lastKnownRound + 1; round <= latestRound; round++) {
    if (results.some((r) => parseRoundNumber(r.roundLabel) === round)) continue;
    const detailHtml = await fetchHtml(backnumberUrl(round));
    if (!detailHtml || detailHtml.includes('Access Denied')) continue;
    for (const item of parseFromHtml(detailHtml)) {
      if (parseRoundNumber(item.roundLabel) > lastKnownRound) {
        results.push(item);
      }
    }
  }

  const seen = new Set<string>();
  return results.filter((r) => {
    if (seen.has(r.roundLabel)) return false;
    seen.add(r.roundLabel);
    return true;
  });
}
