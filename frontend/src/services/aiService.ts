import api from './api';
import { AIRecommendation } from '../types/ai';

export const aiService = {
  generateAIRecommendations: async (resumeId: number): Promise<AIRecommendation> => {
    const response = await api.post<AIRecommendation>(`/ai/analyze/${resumeId}`);
    return response.data;
  },

  getAIRecommendations: async (resumeId: number): Promise<AIRecommendation> => {
    const response = await api.get<AIRecommendation>(`/ai/${resumeId}`);
    return response.data;
  },

  deleteAIRecommendation: async (id: number): Promise<void> => {
    await api.delete(`/ai/${id}`);
  },
};
