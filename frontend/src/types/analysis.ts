export interface TopSkillItem {
  keyword: string;
  category: string;
  frequency: number;
  power: number;
}

export interface AnalysisReport {
  id: number;
  resumeId: number;
  resumeFileName: string;
  overallScore: number;
  contactScore: number;
  summaryScore: number;
  skillsScore: number;
  experienceScore: number;
  educationScore: number;
  projectsScore: number;
  certificationScore: number;
  keywordCoverage: number;
  missingKeywordCount: number;
  duplicateKeywordCount: number;
  strengths: string[];
  weaknesses: string[];
  suggestions: string[];
  topSkills: TopSkillItem[];
  missingKeywords: string[];
  duplicateKeywords: string[];
  categorySkillCounts?: Record<string, number>;
  analysisDate: string;
}
