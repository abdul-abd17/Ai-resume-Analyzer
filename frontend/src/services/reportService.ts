import api from './api';
import { Report, ReportPreview } from '../types/report';

export const reportService = {
  generateReport: async (resumeId: number): Promise<Report> => {
    const response = await api.post<Report>(`/reports/generate/${resumeId}`);
    return response.data;
  },

  getReports: async (): Promise<Report[]> => {
    const response = await api.get<Report[]>('/reports');
    return response.data;
  },

  getReportPreview: async (id: number): Promise<ReportPreview> => {
    const response = await api.get<ReportPreview>(`/reports/${id}`);
    return response.data;
  },

  downloadReport: async (id: number, fileName: string): Promise<void> => {
    const response = await api.get(`/reports/download/${id}`, {
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', fileName.endsWith('.pdf') ? fileName : `${fileName}.pdf`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  deleteReport: async (id: number): Promise<void> => {
    await api.delete(`/reports/${id}`);
  },
};
