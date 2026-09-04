export interface AdminStats {
  totalUsers: number;
  totalRecruiters: number;
  totalResumes: number;
  totalReports: number;
  averageAtsScore: number;
  averageJobMatchPercentage: number;
  averageGrammarScore: number;
  totalAiRequests: number;
  storageUsed: string;
}

export interface UserItem {
  id: number;
  fullName: string;
  email: string;
  role: 'ROLE_USER' | 'ROLE_RECRUITER' | 'ROLE_ADMIN';
  joinedDate: string;
  status: 'ACTIVE' | 'SUSPENDED';
  atsAverage: number;
  resumeCount: number;
}

export interface RecruiterItem {
  id: number;
  companyName: string;
  companyEmail: string;
  industry: string;
  website: string;
  createdAt: string;
  status: 'ACTIVE' | 'SUSPENDED';
}

export interface ActivityLogItem {
  id: number;
  userId: number;
  userEmail: string;
  action: string;
  description: string;
  timestamp: string;
  ipAddress: string;
}

export interface CandidateItem {
  resumeId: number;
  candidateName: string;
  email: string;
  fileName: string;
  atsScore: number;
  matchPercentage: number;
  topSkills: string[];
  uploadedAt: string;
}
