export interface GrammarAnalysis {
  id: number;
  resumeId: number;
  resumeFileName: string;
  grammarScore: number;
  readabilityScore: number;
  formattingScore: number;
  professionalWritingScore: number;
  overallWritingScore: number;
  spellingErrorCount: number;
  grammarErrorCount: number;
  passiveVoiceCount: number;
  repeatedWordCount: number;
  longSentenceCount: number;
  weakVerbCount: number;
  formattingIssueCount: number;
  spellingErrors: string[];
  grammarErrors: string[];
  weakVerbs: string[];
  passiveVoiceInstances: string[];
  suggestions: string[];
  analysisDate: string;
}
