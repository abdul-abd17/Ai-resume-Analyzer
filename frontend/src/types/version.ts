export interface ResumeVersion {
  id: number;
  resumeId: number;
  versionNumber: number;
  versionName: string;
  fileName: string;
  createdAt: string;
  createdBy: string;
  changeSummary: string;
  atsScore: number;
  grammarScore: number;
  jobMatchScore: number;
  status: 'ACTIVE' | 'ARCHIVED';
  isCurrent: boolean;
}

export interface ResumeComparison {
  id: number;
  oldVersion: ResumeVersion;
  newVersion: ResumeVersion;
  atsDifference: number;
  grammarDifference: number;
  jobMatchDifference: number;
  addedSkills: string[];
  removedSkills: string[];
  addedProjects: string[];
  removedProjects: string[];
  addedCertifications: string[];
  removedCertifications: string[];
  summary: string;
  comparisonDate: string;
}
