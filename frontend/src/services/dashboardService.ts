import api from './api';
import { DashboardData, QuickStats, RecentActivityItem } from '../types/dashboard';

export const dashboardService = {
  getDashboardData: async (): Promise<DashboardData> => {
    const response = await api.get<DashboardData>('/dashboard');
    return response.data;
  },

  getDashboardStats: async (): Promise<QuickStats> => {
    const response = await api.get<QuickStats>('/dashboard/stats');
    return response.data;
  },

  getDashboardCharts: async (): Promise<Record<string, any>> => {
    const response = await api.get<Record<string, any>>('/dashboard/charts');
    return response.data;
  },

  getDashboardActivity: async (): Promise<RecentActivityItem[]> => {
    const response = await api.get<RecentActivityItem[]>('/dashboard/activity');
    return response.data;
  },
};
