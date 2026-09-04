import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Sparkles,
  ArrowLeft,
  RefreshCw,
  Trash2,
  Copy,
  Download,
  Check,
  ChevronDown,
  ChevronUp,
  FileText,
  Briefcase,
  Code,
  Award,
  BookOpen,
  MessageSquare,
  TrendingUp,
  AlertCircle,
  Zap,
} from 'lucide-react';
import { AIRecommendation } from '../types/ai';
import { aiService } from '../services/aiService';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

export const AIRecommendationPage: React.FC = () => {
  const { resumeId } = useParams<{ resumeId: string }>();
  const navigate = useNavigate();

  const [aiData, setAiData] = useState<AIRecommendation | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isGenerating, setIsGenerating] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Copied states
  const [copiedKey, setCopiedKey] = useState<string | null>(null);

  // Section collapse states
  const [collapsedSections, setCollapsedSections] = useState<Record<string, boolean>>({});

  const fetchAIRecommendations = async () => {
    if (!resumeId) return;
    setIsLoading(true);
    setError(null);
    try {
      const id = Number(resumeId);
      try {
        const data = await aiService.getAIRecommendations(id);
        setAiData(data);
      } catch {
        // Auto-run if not generated yet
        const data = await aiService.generateAIRecommendations(id);
        setAiData(data);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to generate AI recommendations.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchAIRecommendations();
  }, [resumeId]);

  const handleGenerate = async () => {
    if (!resumeId) return;
    setIsGenerating(true);
    setError(null);
    try {
      const data = await aiService.generateAIRecommendations(Number(resumeId));
      setAiData(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to generate AI suggestions.');
    } finally {
      setIsGenerating(false);
    }
  };

  const handleDelete = async () => {
    if (!aiData) return;
    if (!window.confirm('Delete this AI recommendation report?')) return;
    try {
      await aiService.deleteAIRecommendation(aiData.id);
      navigate(`/dashboard/resumes/${resumeId}`);
    } catch {
      alert('Failed to delete report.');
    }
  };

  const handleCopyText = (text: string, key: string) => {
    navigator.clipboard.writeText(text);
    setCopiedKey(key);
    setTimeout(() => setCopiedKey(null), 2000);
  };

  const handleDownloadReport = () => {
    if (!aiData) return;
    const content = `[AI-GENERATED CONTENT REPORT]\n` +
      `DISCLAIMER: This report contains AI-generated suggestions. Verify all metrics, dates, and claims against your actual work experience prior to submitting to employers.\n\n` +
      `AI RESUME IMPROVEMENT REPORT\nResume: ${aiData.resumeFileName}\nDate: ${new Date(aiData.createdAt).toLocaleString()}\n\n` +
      `=======================================\n1. PROFESSIONAL SUMMARY\n=======================================\n${aiData.professionalSummary}\n\n` +
      `=======================================\n2. IMPROVED EXPERIENCE BULLETS\n=======================================\n${aiData.improvedExperience}\n\n` +
      `=======================================\n3. IMPROVED PROJECTS\n=======================================\n${aiData.improvedProjects}\n\n` +
      `=======================================\n4. SKILLS & KEYWORD STRATEGY\n=======================================\n${aiData.improvedSkills}\n${aiData.keywordRecommendations}\n\n` +
      `=======================================\n5. ATS & GRAMMAR GUIDANCE\n=======================================\n${aiData.atsRecommendations}\n${aiData.grammarRecommendations}\n\n` +
      `=======================================\n6. INTERVIEW PREPARATION SUITE\n=======================================\n${aiData.interviewPreparation}\n\n` +
      `=======================================\n7. CAREER GROWTH & OVERALL FEEDBACK\n=======================================\n${aiData.careerSuggestions}\n${aiData.overallFeedback}\n`;

    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `AI_Improvement_Report_${aiData.resumeId}.txt`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const toggleSection = (key: string) => {
    setCollapsedSections((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  if (isLoading) {
    return (
      <div className="py-20 flex justify-center">
        <LoadingSpinner size="lg" label="Synthesizing Parsed Resume, ATS, Grammar & Match Reports into AI Recommendations..." />
      </div>
    );
  }

  if (error || !aiData) {
    return (
      <div className="max-w-2xl mx-auto py-12 text-center space-y-4">
        <Card className="p-8 space-y-4">
          <AlertCircle className="w-12 h-12 text-red-500 mx-auto" />
          <h3 className="text-lg font-bold text-gray-900 dark:text-white">AI Generation Required</h3>
          <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400">
            {error || 'No AI suggestions generated yet.'}
          </p>
          <div className="flex justify-center space-x-3 pt-2">
            <Button variant="outline" size="sm" onClick={() => navigate(`/dashboard/resumes/${resumeId}`)}>
              Back to Resume Details
            </Button>
            <Button variant="primary" size="sm" isLoading={isGenerating} onClick={handleGenerate}>
              Generate AI Suggestions Now
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  const sections = [
    {
      key: 'summary',
      title: 'Professional Summary Rewrite',
      icon: FileText,
      iconColor: 'text-primary-500',
      content: aiData.professionalSummary,
    },
    {
      key: 'experience',
      title: 'Quantified Experience Bullet Points',
      icon: Briefcase,
      iconColor: 'text-emerald-500',
      content: aiData.improvedExperience,
    },
    {
      key: 'projects',
      title: 'Project & Tech Stack Enhancements',
      icon: Code,
      iconColor: 'text-indigo-500',
      content: aiData.improvedProjects,
    },
    {
      key: 'skills',
      title: 'Skills & Keyword Placement Strategy',
      icon: Award,
      iconColor: 'text-amber-500',
      content: `${aiData.improvedSkills}\n\n${aiData.keywordRecommendations}`,
    },
    {
      key: 'ats_grammar',
      title: 'ATS & Grammar Optimization',
      icon: BookOpen,
      iconColor: 'text-purple-500',
      content: `${aiData.atsRecommendations}\n\n${aiData.grammarRecommendations}`,
    },
    {
      key: 'interview',
      title: 'Interview Preparation Suite (Technical & Behavioral)',
      icon: MessageSquare,
      iconColor: 'text-blue-500',
      content: aiData.interviewPreparation,
    },
    {
      key: 'career',
      title: 'Career Growth & Strategic Feedback',
      icon: TrendingUp,
      iconColor: 'text-emerald-500',
      content: `${aiData.careerSuggestions}\n\n${aiData.overallFeedback}`,
    },
  ];

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-6xl mx-auto space-y-8 pb-16"
    >
      {/* Top Bar Navigation */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <button
          onClick={() => navigate(`/dashboard/resumes/${resumeId}`)}
          className="flex items-center space-x-2 text-xs font-semibold text-gray-600 dark:text-gray-400 hover:text-primary-600 dark:hover:text-primary-400 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Resume Details</span>
        </button>

        <div className="flex items-center space-x-3">
          <Button
            variant="outline"
            size="sm"
            onClick={fetchAIRecommendations}
            leftIcon={<RefreshCw className="w-4 h-4" />}
          >
            Refresh
          </Button>

          <Button
            variant="primary"
            size="sm"
            isLoading={isGenerating}
            onClick={handleGenerate}
            leftIcon={<Zap className="w-4 h-4" />}
          >
            Regenerate AI Suggestions
          </Button>

          <Button
            variant="outline"
            size="sm"
            onClick={handleDownloadReport}
            leftIcon={<Download className="w-4 h-4 text-indigo-500" />}
          >
            Download Report (.TXT)
          </Button>

          <Button
            variant="danger"
            size="sm"
            onClick={handleDelete}
            leftIcon={<Trash2 className="w-4 h-4" />}
          >
            Delete
          </Button>
        </div>
      </div>

      {/* AI Grounding Disclaimer Banner */}
      <div className="p-3.5 rounded-2xl bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-800 flex items-center justify-between gap-3 text-xs text-amber-800 dark:text-amber-300">
        <div className="flex items-center space-x-2.5">
          <AlertCircle className="w-4 h-4 text-amber-600 dark:text-amber-400 shrink-0" />
          <span>
            <strong>AI-Generated Content:</strong> All recommendations are generated using evidence-grounded AI models. Please verify any metric placeholders (e.g. <code className="bg-amber-150 dark:bg-amber-900 px-1 py-0.5 rounded font-mono text-[11px]">[VERIFY METRIC]</code>) against your actual work experience before sending to recruiters.
          </span>
        </div>
        <Badge variant="warning" size="sm" className="shrink-0">
          AI Generated
        </Badge>
      </div>

      {/* Main Hero Card */}
      <Card className="p-6 sm:p-8 bg-gradient-to-r from-slate-900 via-indigo-950 to-primary-950 text-white rounded-3xl relative overflow-hidden shadow-2xl">
        <div className="absolute right-0 top-0 w-80 h-80 bg-primary-500/10 rounded-full blur-3xl pointer-events-none" />

        <div className="relative z-10 flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex items-center space-x-4">
            <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-primary-500 to-indigo-500 text-white flex items-center justify-center shadow-lg">
              <Sparkles className="w-8 h-8 animate-pulse" />
            </div>

            <div>
              <div className="flex items-center space-x-2">
                <h1 className="text-xl sm:text-2xl font-bold">
                  AI Resume Improvement Engine
                </h1>
                <Badge variant="purple" size="sm">
                  🤖 AI Generated
                </Badge>
              </div>
              <p className="text-xs text-gray-300 mt-1">
                Synthesizing Parsed Resume, ATS, Grammar & Job Match Reports for <span className="font-semibold text-primary-300">{aiData.resumeFileName}</span>
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <Button
              variant="outline"
              size="sm"
              className="bg-white/10 border-white/20 text-white hover:bg-white/20"
              onClick={() => handleCopyText(JSON.stringify(aiData, null, 2), 'all')}
              leftIcon={copiedKey === 'all' ? <Check className="w-4 h-4 text-emerald-400" /> : <Copy className="w-4 h-4" />}
            >
              {copiedKey === 'all' ? 'Copied Full Report!' : 'Copy Full JSON'}
            </Button>
          </div>
        </div>
      </Card>

      {/* AI Recommendation Sections Grid */}
      <div className="space-y-6">
        {sections.map((sec) => {
          const Icon = sec.icon;
          const isCollapsed = collapsedSections[sec.key];

          return (
            <Card key={sec.key} className="space-y-4 border-gray-200/60 dark:border-dark-border">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <Icon className={`w-5 h-5 ${sec.iconColor}`} />
                  <h3 className="text-base font-bold text-gray-900 dark:text-white">
                    {sec.title}
                  </h3>
                  <Badge variant="purple" size="sm">
                    AI Suggestion
                  </Badge>
                </div>

                <div className="flex items-center space-x-2">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleCopyText(sec.content, sec.key)}
                    leftIcon={copiedKey === sec.key ? <Check className="w-3.5 h-3.5 text-emerald-500" /> : <Copy className="w-3.5 h-3.5" />}
                  >
                    {copiedKey === sec.key ? 'Copied' : 'Copy'}
                  </Button>

                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => toggleSection(sec.key)}
                    rightIcon={isCollapsed ? <ChevronDown className="w-4 h-4" /> : <ChevronUp className="w-4 h-4" />}
                  >
                    {isCollapsed ? 'Expand' : 'Collapse'}
                  </Button>
                </div>
              </div>

              {!isCollapsed && (
                <motion.div
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: 'auto' }}
                  exit={{ opacity: 0, height: 0 }}
                  className="pt-2 border-t border-gray-100 dark:border-dark-border"
                >
                  <div className="p-4 rounded-2xl bg-gray-50 dark:bg-dark-hover text-xs sm:text-sm text-gray-800 dark:text-gray-200 whitespace-pre-line leading-relaxed font-sans">
                    {sec.content || 'No specific recommendations generated for this section.'}
                  </div>
                </motion.div>
              )}
            </Card>
          );
        })}
      </div>
    </motion.div>
  );
};
