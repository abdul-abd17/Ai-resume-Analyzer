import api from './api';
import { Resume, PagedResumeResponse } from '../types/resume';
import { AxiosProgressEvent } from 'axios';

export const resumeService = {
  uploadResume: async (
    file: File,
    onProgress?: (progressEvent: AxiosProgressEvent) => void
  ): Promise<Resume> => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await api.post<Resume>('/resumes/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: onProgress,
    });
    return response.data;
  },

  getAllResumes: async (
    page = 0,
    size = 10,
    search = '',
    sortBy = 'uploadedAt',
    sortDir = 'desc'
  ): Promise<PagedResumeResponse> => {
    const params: Record<string, any> = { page, size, sortBy, sortDir };
    if (search && search.trim() !== '') {
      params.search = search.trim();
    }
    const response = await api.get<PagedResumeResponse>('/resumes', { params });
    return response.data;
  },

  getResumeDetails: async (id: number): Promise<Resume> => {
    const response = await api.get<Resume>(`/resumes/${id}`);
    return response.data;
  },

  deleteResume: async (id: number): Promise<void> => {
    await api.delete(`/resumes/${id}`);
  },

  downloadResume: async (id: number, originalFileName: string): Promise<void> => {
    const response = await api.get(`/resumes/download/${id}`, {
      responseType: 'blob',
    });

    // Create a URL for the blob and trigger download
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', originalFileName);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },
};
