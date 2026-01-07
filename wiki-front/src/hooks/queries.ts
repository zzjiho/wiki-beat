import { useQuery } from '@tanstack/react-query';
import {
  fetchStats,
  fetchPopularDocuments,
  fetchNewDocuments,
  fetchVandalismStats,
} from '../services/api';
import { REFRESH_INTERVALS } from '../utils/constants';

/**
 * Statistics data query hook
 */
export const useStats = () => {
  return useQuery({
    queryKey: ['stats'],
    queryFn: fetchStats,
    refetchInterval: REFRESH_INTERVALS.STATS,
  });
};

/**
 * Popular documents query hook
 */
export const usePopularDocuments = () => {
  return useQuery({
    queryKey: ['popularDocuments'],
    queryFn: fetchPopularDocuments,
    refetchInterval: REFRESH_INTERVALS.POPULAR_DOCUMENTS,
  });
};

/**
 * New documents query hook
 */
export const useNewDocuments = () => {
  return useQuery({
    queryKey: ['newDocuments'],
    queryFn: fetchNewDocuments,
    refetchInterval: REFRESH_INTERVALS.NEW_DOCUMENTS,
  });
};

/**
 * Vandalism statistics query hook
 */
export const useVandalismStats = () => {
  return useQuery({
    queryKey: ['vandalismStats'],
    queryFn: fetchVandalismStats,
    refetchInterval: REFRESH_INTERVALS.VANDALISM_STATS,
  });
};
