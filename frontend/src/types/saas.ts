export interface JobItem {
  id: number;
  jobType: string;
  jobStatus: 'QUEUED' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  startedAt: string;
  completedAt?: string;
  errorMessage?: string;
  createdBy: string;
  resumeId?: number;
  resumeFileName?: string;
}

export interface UserNotification {
  id: number;
  userId: number;
  title: string;
  message: string;
  category: 'PARSING' | 'ATS' | 'GRAMMAR' | 'AI' | 'REPORT' | 'SYSTEM';
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
  isRead: boolean;
  createdAt: string;
}
