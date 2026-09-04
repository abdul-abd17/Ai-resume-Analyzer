export interface ParsedResume {
  id: number;
  resumeId: number;
  fullName?: string;
  email?: string;
  phone?: string;
  linkedinUrl?: string;
  githubUrl?: string;
  portfolioUrl?: string;
  location?: string;
  summary?: string;
  skillsList?: string[];
  skills?: string;
  education?: string;
  experience?: string;
  projects?: string;
  certifications?: String;
  languages?: string;
  achievements?: string;
  rawText?: string;
  parsedAt?: string;
}
