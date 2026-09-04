import api from './api';
import { JobItem } from '../types/saas';
import { JobDescription, MatchAnalysis } from '../types/job';

export const jobService = {
  // Job Queue APIs (Step 12)
  getJobs: async (): Promise<JobItem[]> => {
    const response = await api.get<JobItem[]>('/jobs');
    return response.data;
  },

  getJobById: async (id: number): Promise<JobItem> => {
    const response = await api.get<JobItem>(`/jobs/${id}`);
    return response.data;
  },

  retryJob: async (id: number): Promise<JobItem> => {
    const response = await api.post<JobItem>(`/jobs/${id}/retry`);
    return response.data;
  },

  cancelJob: async (id: number): Promise<JobItem> => {
    const response = await api.post<JobItem>(`/jobs/${id}/cancel`);
    return response.data;
  },

  // Job Description Matching APIs (Step 5)
  uploadJobDescription: async (formData: FormData): Promise<JobDescription> => {
    const response = await api.post<JobDescription>('/job-description/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  getJobDescriptions: async (): Promise<JobDescription[]> => {
    const response = await api.get<JobDescription[]>('/job-description');
    return response.data;
  },

  getJobDescriptionById: async (id: number): Promise<JobDescription> => {
    const response = await api.get<JobDescription>(`/job-description/${id}`);
    return response.data;
  },

  compareResume: async (resumeId: number, jobDescriptionId: number): Promise<MatchAnalysis> => {
    const response = await api.post<MatchAnalysis>(
      `/job-description/compare/${resumeId}?jobDescriptionId=${jobDescriptionId}`
    );
    return response.data;
  },

  deleteJobDescription: async (id: number): Promise<void> => {
    await api.delete(`/job-description/${id}`);
  },
};
