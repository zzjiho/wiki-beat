/**
 * Recharts Tooltip type definitions
 */

/**
 * Recharts Tooltip Props common interface
 *
 * @template T - Payload data type (Generic)
 */
export interface TooltipProps<T = unknown> {
  /**
   * Whether the tooltip is currently active
   */
  active?: boolean;

  /**
   * Array of data to display in tooltip
   */
  payload?: Array<{
    /**
     * Data point name (displayed in legend)
     */
    name: string;

    /**
     * Data point value
     */
    value: number;

    /**
     * Complete data object (for accessing additional information)
     */
    payload: T;

    /**
     * Data point color
     */
    color?: string;

    /**
     * Data key (specified in chart configuration)
     */
    dataKey?: string;
  }>;

  /**
   * X-axis label (time, category, etc.)
   */
  label?: string;
}

/**
 * Edits per minute chart data type
 */
export interface EditsPerMinuteData {
  minute: string;
  editCount: number;
  timestamp: string;
}

/**
 * Language distribution chart data type
 */
export interface LanguageDistributionData {
  language: string;
  count: number;
  percentage: number;
}

/**
 * Bot ratio chart data type
 */
export interface BotRatioData {
  minute: string;
  botEditRatio: number;
  timestamp: string;
}
