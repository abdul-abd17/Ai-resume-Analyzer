import api from './api';
import { AdminStats, UserItem, RecruiterItem, ActivityLogItem, CandidateItem } from '../types/admin';

export const adminService = {
  getAdminDashboard: async (): Promise<AdminStats> => {
    const response = await api.get<AdminStats>('/admin/dashboard');
    return response.data;
  },

  getAdminUsers: async (): Promise<UserItem[]> => {
    const response = await api.get<UserItem[]>('/admin/users');
    return response.data;
  },

  updateUserStatus: async (id: number, status: string): Promise<UserItem> => {
    const response = await api.put<UserItem>(`/admin/users/${id}?status=${status}`);
    return response.data;
  },

  changeUserRole: async (userId: number, newRole: string): Promise<UserItem> => {
    const response = await api.post<UserItem>('/admin/roles', { userId, newRole });
    return response.data;
  },

  deleteUser: async (id: number): Promise<void> => {
    await api.delete(`/admin/users/${id}`);
  },

  getAdminRecruiters: async (): Promise<RecruiterItem[]> => {
    const response = await api.get<RecruiterItem[]>('/admin/recruiters');
    return response.data;
  },

  getAdminLogs: async (): Promise<ActivityLogItem[]> => {
    const response = await api.get<ActivityLogItem[]>('/admin/logs');
    return response.data;
  },

  searchCandidates: async (skill?: string, minAtsScore?: number): Promise<CandidateItem[]> => {
    const params = new URLSearchParams();
    if (skill) params.append('skill', skill);
    if (minAtsScore) params.append('minAtsScore', minAtsScore.toString());
    const response = await api.get<CandidateItem[]>(`/recruiter/candidates?${params.toString()}`);
    return response.data;
  },
};
