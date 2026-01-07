/**
 * Language-related utility functions
 */

/**
 * Language code → English name mapping
 *
 * Real-world pattern: Constants named in UPPER_SNAKE_CASE
 */
export const LANGUAGE_NAMES: Record<string, string> = {
  en: 'English',
  ko: 'Korean',
  ja: 'Japanese',
  zh: 'Chinese',
  de: 'German',
  fr: 'French',
  es: 'Spanish',
  ru: 'Russian',
  pt: 'Portuguese',
  it: 'Italian',
  ar: 'Arabic',
  hi: 'Hindi',
  nl: 'Dutch',
  pl: 'Polish',
  tr: 'Turkish',
  sv: 'Swedish',
  vi: 'Vietnamese',
  th: 'Thai',
  id: 'Indonesian',
  he: 'Hebrew',
  // Special wikimedia projects
  commons: 'Commons',
  wikidata: 'Wikidata',
  wikifunctions: 'Wikifunctions',
  species: 'Species',
  unknown: 'Other',
};

// LANGUAGE_COLORS: Currently unused (components use local definitions)

/**
 * Convert language code to English name
 *
 * @param code - Language code (e.g. 'en', 'ko')
 * @param fallback - Default value for unmapped languages (default: code as-is)
 * @returns English language name
 *
 * @example
 * getLanguageName('en') // 'English'
 * getLanguageName('unknown') // 'unknown'
 * getLanguageName('unknown', 'Other') // 'Other'
 */
export function getLanguageName(code: string, fallback?: string): string {
  return LANGUAGE_NAMES[code] ?? fallback ?? code;
}

/**
 * Return badge color class for language code
 *
 * @param code - Language code
 * @returns Tailwind CSS class string
 *
 * @example
 * getLanguageBadgeColor('en') // 'bg-blue-100 text-blue-700...'
 */
export function getLanguageBadgeColor(code: string): string {
  const colorMap: Record<string, string> = {
    en: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300',
    ko: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300',
    zh: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300',
    ja: 'bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-300',
    fr: 'bg-pink-100 text-pink-700 dark:bg-pink-900/30 dark:text-pink-300',
    de: 'bg-indigo-100 text-indigo-700 dark:bg-indigo-900/30 dark:text-indigo-300',
    es: 'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-300',
    commons: 'bg-indigo-100 text-indigo-700 dark:bg-indigo-900/30 dark:text-indigo-300',
  };
  return colorMap[code] || 'bg-gray-100 text-gray-700 dark:bg-gray-700 dark:text-gray-300';
}
