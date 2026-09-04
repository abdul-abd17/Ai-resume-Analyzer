export interface QuickStats {
  averageAtsScore: number;
  averageGrammarScore: number;
  latestJobMatchPercentage: number;
  overallResumeStrengthScore: number;
  totalAnalysesCount: number;
  totalUploadedResumes: number;
  latestResumeId?: number;
  latestResumeFileName: string;
}

export interface RecentActivityItem {
  id: string;
  title: string;
  type: 'RESUME_UPLOAD' | 'ATS_ANALYSIS' | 'GRAMMAR_ANALYSIS' | 'JOB_MATCH' | 'AI_RECOMMENDATION';
  timestamp: string;
  statusBadge: string;
  resumeId?: number;
}

export interface NotificationItem {
  id: string;
  title: string;
  message: string;
  timestamp: string;
  read: boolean;
  type: 'INFO' | 'SUCCESS' | 'WARNING';
}

export interface DashboardData {
  quickStats: QuickStats;
  chartsData: Record<string, any>;
  skillAnalytics: Record<string, any>;
  grammarAnalytics: Record<string, any>;
  jobMatchAnalytics: Record<string, any>;
  aiAnalytics: Record<string, any>;
  recentActivity: RecentActivityItem[];
  notifications: NotificationItem[];
}
