import api from './api';
import { ResumeVersion, ResumeComparison } from '../types/version';

export const versionService = {
  getVersions: async (): Promise<ResumeVersion[]> => {
    const response = await api.get<ResumeVersion[]>('/versions');
    return response.data;
  },

  getVersionById: async (id: number): Promise<ResumeVersion> => {
    const response = await api.get<ResumeVersion>(`/versions/${id}`);
    return response.data;
  },

  compareVersions: async (oldVersionId: number, newVersionId: number): Promise<ResumeComparison> => {
    const response = await api.post<ResumeComparison>('/versions/compare', {
      oldVersionId,
      newVersionId,
    });
    return response.data;
  },

  renameVersion: async (id: number, versionName: string): Promise<ResumeVersion> => {
    const response = await api.put<ResumeVersion>(`/versions/${id}/rename`, { versionName });
    return response.data;
  },

  restoreVersion: async (id: number): Promise<ResumeVersion> => {
    const response = await api.post<ResumeVersion>(`/versions/${id}/restore`);
    return response.data;
  },

  deleteVersion: async (id: number): Promise<void> => {
    await api.delete(`/versions/${id}`);
  },
};
