export interface Report {
  id: number;
  resumeId: number;
  resumeFileName: string;
  reportName: string;
  reportType: string;
  generatedBy: string;
  generatedDate: string;
  fileSize: number;
  status: string;
  downloadCount: number;
  sharedCount: number;
  atsScore: number | null;
  grammarScore: number | null;
  jobMatchScore: number | null;
}

export interface ReportPreview {
  reportInfo: Report;
  candidateName: string;
  candidateEmail: string;
  summary: string;
  atsScore: number | null;
  grammarScore: number | null;
  jobMatchPercentage: number | null;
  topSkills: string[];
  missingKeywords: string[];
  grammarIssues: string[];
  aiProfessionalSummary: string;
  aiImprovedExperience: string;
  aiInterviewPreparation: string;
  htmlContent: string;
}
