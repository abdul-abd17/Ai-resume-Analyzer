export interface JobDescription {
  id: number;
  title: string;
  companyName?: string;
  location?: string;
  employmentType?: string;
  description: string;
  requiredSkills?: string;
  preferredSkills?: string;
  minimumExperience?: number;
  educationRequirement?: string;
  createdAt: string;
  status: string;
  userId?: number;
}

export interface MatchAnalysis {
  id: number;
  resumeId: number;
  resumeFileName: string;
  jobDescriptionId: number;
  jobTitle: string;
  companyName: string;
  overallMatchPercentage: number;
  skillMatchPercentage: number;
  experienceMatchPercentage: number;
  educationMatchPercentage: number;
  projectMatchPercentage: number;
  keywordCoveragePercentage: number;
  matchedSkills: string[];
  missingSkills: string[];
  extraSkills: string[];
  matchedKeywords: string[];
  missingKeywords: string[];
  priorityRecommendations: string[];
  hiringRecommendation: string;
  createdAt: string;
}
