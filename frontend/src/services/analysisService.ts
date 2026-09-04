import api from './api';
import { AnalysisReport } from '../types/analysis';

export const analysisService = {
  analyzeResume: async (resumeId: number): Promise<AnalysisReport> => {
    const response = await api.post<AnalysisReport>(`/analysis/analyze/${resumeId}`);
    return response.data;
  },

  getAnalysis: async (resumeId: number): Promise<AnalysisReport> => {
    const response = await api.get<AnalysisReport>(`/analysis/${resumeId}`);
    return response.data;
  },

  getAnalysisHistory: async (): Promise<AnalysisReport[]> => {
    const response = await api.get<AnalysisReport[]>('/analysis/history');
    return response.data;
  },

  deleteAnalysis: async (id: number): Promise<void> => {
    await api.delete(`/analysis/${id}`);
  },
};
