import api from './api';
import { GrammarAnalysis } from '../types/grammar';

export const grammarService = {
  analyzeGrammar: async (resumeId: number): Promise<GrammarAnalysis> => {
    const response = await api.post<GrammarAnalysis>(`/grammar/analyze/${resumeId}`);
    return response.data;
  },

  getGrammarAnalysis: async (resumeId: number): Promise<GrammarAnalysis> => {
    const response = await api.get<GrammarAnalysis>(`/grammar/${resumeId}`);
    return response.data;
  },

  deleteGrammarAnalysis: async (id: number): Promise<void> => {
    await api.delete(`/grammar/${id}`);
  },
};
