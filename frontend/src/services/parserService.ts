import api from './api';
import { ParsedResume } from '../types/parser';

export const parserService = {
  parseResume: async (resumeId: number): Promise<ParsedResume> => {
    const response = await api.post<ParsedResume>(`/parser/parse/${resumeId}`);
    return response.data;
  },

  getParsedResume: async (resumeId: number): Promise<ParsedResume> => {
    const response = await api.get<ParsedResume>(`/parser/${resumeId}`);
    return response.data;
  },

  getRawText: async (resumeId: number): Promise<string> => {
    const response = await api.get<{ rawText: string }>(`/parser/raw/${resumeId}`);
    return response.data.rawText;
  },
};
