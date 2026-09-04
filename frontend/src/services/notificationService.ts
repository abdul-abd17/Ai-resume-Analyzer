import api from './api';
import { UserNotification } from '../types/saas';

export const notificationService = {
  getNotifications: async (): Promise<UserNotification[]> => {
    const response = await api.get<UserNotification[]>('/notifications');
    return response.data;
  },

  markAsRead: async (id: number): Promise<UserNotification> => {
    const response = await api.put<UserNotification>(`/notifications/${id}/read`);
    return response.data;
  },

  deleteNotification: async (id: number): Promise<void> => {
    await api.delete(`/notifications/${id}`);
  },

  clearAllNotifications: async (): Promise<void> => {
    await api.delete('/notifications/clear-all');
  },
};
