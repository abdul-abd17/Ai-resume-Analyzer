import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  SpellCheck,
  CheckCircle2,
  AlertTriangle,
  ArrowLeft,
  RefreshCw,
  Trash2,
  Sparkles,
  PieChart as PieIcon,
  BarChart3,
  FileText,
  AlertCircle,
  Zap,
  ChevronRight,
  BookOpen,
  Edit3,
} from 'lucide-react';
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
} from 'recharts';
import { GrammarAnalysis } from '../types/grammar';
import { grammarService } from '../services/grammarService';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

const COLORS = ['#ef4444', '#f59e0b', '#6366f1', '#ec4899', '#8b5cf6', '#14b8a6'];

export const GrammarAnalysisPage: React.FC = () => {
  const { resumeId } = useParams<{ resumeId: string }>();
  const navigate = useNavigate();

  const [grammarData, setGrammarData] = useState<GrammarAnalysis | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isAnalyzing, setIsAnalyzing] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const fetchGrammarAnalysis = async () => {
    if (!resumeId) return;
    setIsLoading(true);
    setError(null);
    try {
      const id = Number(resumeId);
      try {
        const data = await grammarService.getGrammarAnalysis(id);
        setGrammarData(data);
      } catch {
        // Auto-run if not analyzed yet
        const data = await grammarService.analyzeGrammar(id);
        setGrammarData(data);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to analyze grammar & readability.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchGrammarAnalysis();
  }, [resumeId]);

  const handleRunAnalysis = async () => {
    if (!resumeId) return;
    setIsAnalyzing(true);
    setError(null);
    try {
      const data = await grammarService.analyzeGrammar(Number(resumeId));
      setGrammarData(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to run grammar engine.');
    } finally {
      setIsAnalyzing(false);
    }
  };

  const handleDeleteReport = async () => {
    if (!grammarData) return;
    if (!window.confirm('Delete this grammar report?')) return;
    try {
      await grammarService.deleteGrammarAnalysis(grammarData.id);
      navigate(`/dashboard/resumes/${resumeId}`);
    } catch {
      alert('Failed to delete report.');
    }
  };

  if (isLoading) {
    return (
      <div className="py-20 flex justify-center">
        <LoadingSpinner size="lg" label="Running LanguageTool Grammar & Readability Engine..." />
      </div>
    );
  }

  if (error || !grammarData) {
    return (
      <div className="max-w-2xl mx-auto py-12 text-center space-y-4">
        <Card className="p-8 space-y-4">
          <AlertCircle className="w-12 h-12 text-red-500 mx-auto" />
          <h3 className="text-lg font-bold text-gray-900 dark:text-white">Analysis Required</h3>
          <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400">
            {error || 'No grammar report found.'}
          </p>
          <div className="flex justify-center space-x-3 pt-2">
            <Button variant="outline" size="sm" onClick={() => navigate(`/dashboard/resumes/${resumeId}`)}>
              Back to Resume Details
            </Button>
            <Button variant="primary" size="sm" isLoading={isAnalyzing} onClick={handleRunAnalysis}>
              Run Grammar Analysis Now
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  const pieData = [
    { name: 'Spelling Typos', value: grammarData.spellingErrorCount },
    { name: 'Grammar Issues', value: grammarData.grammarErrorCount },
    { name: 'Passive Voice', value: grammarData.passiveVoiceCount },
    { name: 'Weak Verbs', value: grammarData.weakVerbCount },
    { name: 'Long Sentences', value: grammarData.longSentenceCount },
  ];

  const barData = [
    { section: 'Grammar (40%)', score: grammarData.grammarScore },
    { section: 'Readability (30%)', score: grammarData.readabilityScore },
    { section: 'Formatting (15%)', score: grammarData.formattingScore },
    { section: 'Prof. Writing (15%)', score: grammarData.professionalWritingScore },
  ];

  const getScoreColor = (score: number) => {
    if (score >= 80) return 'text-emerald-500 border-emerald-500';
    if (score >= 60) return 'text-amber-500 border-amber-500';
    return 'text-red-500 border-red-500';
  };

  const getScoreBadge = (score: number) => {
    if (score >= 80) return { label: 'Publication Ready', variant: 'success' as const };
    if (score >= 60) return { label: 'Standard Quality', variant: 'warning' as const };
    return { label: 'Needs Grammar Polish', variant: 'purple' as const };
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-6xl mx-auto space-y-8 pb-16"
    >
      {/* Navigation Header */}
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
            onClick={fetchGrammarAnalysis}
            leftIcon={<RefreshCw className="w-4 h-4" />}
          >
            Refresh
          </Button>

          <Button
            variant="primary"
            size="sm"
            isLoading={isAnalyzing}
            onClick={handleRunAnalysis}
            leftIcon={<Zap className="w-4 h-4" />}
          >
            Re-Analyze Grammar
          </Button>

          <Button
            variant="danger"
            size="sm"
            onClick={handleDeleteReport}
            leftIcon={<Trash2 className="w-4 h-4" />}
          >
            Delete
          </Button>
        </div>
      </div>

      {/* Main Score Hero Card */}
      <Card className="p-6 sm:p-8 bg-gradient-to-r from-slate-900 via-indigo-950 to-primary-950 text-white rounded-3xl relative overflow-hidden shadow-2xl">
        <div className="absolute right-0 top-0 w-80 h-80 bg-primary-500/10 rounded-full blur-3xl pointer-events-none" />

        <div className="relative z-10 grid grid-cols-1 md:grid-cols-3 gap-8 items-center">
          {/* Circular Score Gauge */}
          <div className="flex flex-col items-center justify-center text-center space-y-3">
            <div className="relative w-36 h-36 flex items-center justify-center">
              <svg className="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
                <circle
                  cx="50"
                  cy="50"
                  r="42"
                  stroke="currentColor"
                  strokeWidth="8"
                  className="text-gray-800"
                  fill="transparent"
                />
                <circle
                  cx="50"
                  cy="50"
                  r="42"
                  stroke="currentColor"
                  strokeWidth="8"
                  className={getScoreColor(grammarData.overallWritingScore)}
                  strokeDasharray={264}
                  strokeDashoffset={264 - (264 * grammarData.overallWritingScore) / 100}
                  strokeLinecap="round"
                  fill="transparent"
                  style={{ transition: 'stroke-dashoffset 1s ease-in-out' }}
                />
              </svg>
              <div className="absolute inset-0 flex flex-col items-center justify-center">
                <span className="text-3xl font-black tracking-tight">
                  {grammarData.overallWritingScore}
                </span>
                <span className="text-[10px] text-gray-400 font-semibold uppercase">Writing Score</span>
              </div>
            </div>
            <Badge variant={getScoreBadge(grammarData.overallWritingScore).variant} size="md">
              {getScoreBadge(grammarData.overallWritingScore).label}
            </Badge>
          </div>

          {/* Grammar Overview Info */}
          <div className="md:col-span-2 space-y-4">
            <div>
              <h1 className="text-xl sm:text-2xl font-bold">
                Grammar & Readability Evaluation for <span className="text-primary-300">{grammarData.resumeFileName}</span>
              </h1>
              <p className="text-xs text-gray-300 mt-1">
                Analyzed via LanguageTool Java Engine & Flesch Readability Heuristics
              </p>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2 text-center">
              <div className="p-3 rounded-2xl bg-white/10 border border-white/10">
                <span className="text-base font-bold text-red-400">{grammarData.spellingErrorCount}</span>
                <p className="text-[10px] text-gray-300">Spelling Typos</p>
              </div>

              <div className="p-3 rounded-2xl bg-white/10 border border-white/10">
                <span className="text-base font-bold text-amber-400">{grammarData.grammarErrorCount}</span>
                <p className="text-[10px] text-gray-300">Grammar Issues</p>
              </div>

              <div className="p-3 rounded-2xl bg-white/10 border border-white/10">
                <span className="text-base font-bold text-indigo-400">{grammarData.weakVerbCount}</span>
                <p className="text-[10px] text-gray-300">Weak Verbs</p>
              </div>

              <div className="p-3 rounded-2xl bg-white/10 border border-white/10">
                <span className="text-base font-bold text-purple-400">{grammarData.passiveVoiceCount}</span>
                <p className="text-[10px] text-gray-300">Passive Voice</p>
              </div>
            </div>
          </div>
        </div>
      </Card>

      {/* 4-Part Weighted Breakdown Progress Grid */}
      <div className="space-y-4">
        <h2 className="text-lg font-bold text-gray-900 dark:text-white flex items-center space-x-2">
          <BookOpen className="w-5 h-5 text-primary-500" />
          <span>Writing Quality Component Breakdown (100% Total)</span>
        </h2>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {[
            { label: 'Grammar & Punctuation (40%)', score: grammarData.grammarScore, color: 'bg-emerald-500' },
            { label: 'Readability Index (30%)', score: grammarData.readabilityScore, color: 'bg-indigo-500' },
            { label: 'Formatting & Spacing (15%)', score: grammarData.formattingScore, color: 'bg-amber-500' },
            { label: 'Prof. Action Verbs (15%)', score: grammarData.professionalWritingScore, color: 'bg-purple-500' },
          ].map((sec, idx) => (
            <Card key={idx} className="p-4 space-y-2">
              <span className="text-[11px] font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider block truncate">
                {sec.label}
              </span>
              <div className="flex items-center justify-between">
                <span className="text-lg font-extrabold text-gray-900 dark:text-white">
                  {sec.score} <span className="text-xs text-gray-400">/ 100</span>
                </span>
              </div>
              <div className="w-full bg-gray-200 dark:bg-dark-hover h-2 rounded-full overflow-hidden">
                <div
                  className={`h-full ${sec.color} transition-all duration-700`}
                  style={{ width: `${sec.score}%` }}
                />
              </div>
            </Card>
          ))}
        </div>
      </div>

      {/* Visualizations Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Pie Chart: Error Distribution */}
        <Card className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <PieIcon className="w-5 h-5 text-primary-500" />
            <span>Writing Error Categories Distribution</span>
          </h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={pieData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={80}
                  paddingAngle={5}
                  dataKey="value"
                >
                  {pieData.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </Card>

        {/* Bar Chart: Quality Section Scores */}
        <Card className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <BarChart3 className="w-5 h-5 text-indigo-500" />
            <span>Writing Quality Component Comparison</span>
          </h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={barData}>
                <XAxis dataKey="section" stroke="#888888" fontSize={11} />
                <YAxis domain={[0, 100]} stroke="#888888" fontSize={11} />
                <Tooltip />
                <Bar dataKey="score" fill="#6366f1" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </div>

      {/* Detailed Issue Breakdown Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Weak Verbs & Strong Replacements */}
        <Card className="space-y-4 border-l-4 border-l-amber-500">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <Edit3 className="w-5 h-5 text-amber-500" />
            <span>Weak Action Verbs & Recommendations ({grammarData.weakVerbCount})</span>
          </h3>
          <div className="space-y-2">
            {grammarData.weakVerbs.length > 0 ? (
              grammarData.weakVerbs.map((item, idx) => (
                <div key={idx} className="p-3 rounded-xl bg-amber-50/50 dark:bg-amber-950/20 text-xs text-amber-900 dark:text-amber-200">
                  {item}
                </div>
              ))
            ) : (
              <p className="text-xs text-emerald-500 font-semibold">100% Strong Action Verbs!</p>
            )}
          </div>
        </Card>

        {/* Passive Voice Instances */}
        <Card className="space-y-4 border-l-4 border-l-purple-500">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <AlertTriangle className="w-5 h-5 text-purple-500" />
            <span>Passive Voice Instances ({grammarData.passiveVoiceCount})</span>
          </h3>
          <div className="space-y-2">
            {grammarData.passiveVoiceInstances.length > 0 ? (
              grammarData.passiveVoiceInstances.map((item, idx) => (
                <div key={idx} className="p-3 rounded-xl bg-purple-50/50 dark:bg-purple-950/20 text-xs text-purple-900 dark:text-purple-200">
                  {item}
                </div>
              ))
            ) : (
              <p className="text-xs text-emerald-500 font-semibold">No passive voice detected!</p>
            )}
          </div>
        </Card>
      </div>

      {/* LanguageTool Spelling & Grammar Matches */}
      {(grammarData.spellingErrors.length > 0 || grammarData.grammarErrors.length > 0) && (
        <Card className="space-y-4 border-l-4 border-l-red-500">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <SpellCheck className="w-5 h-5 text-red-500" />
            <span>LanguageTool Grammar & Spelling Matches</span>
          </h3>
          <div className="space-y-2 max-h-72 overflow-y-auto pr-2">
            {grammarData.spellingErrors.map((err, idx) => (
              <div key={`sp-${idx}`} className="p-3 rounded-xl bg-red-50/50 dark:bg-red-950/20 text-xs text-red-900 dark:text-red-200 font-mono">
                🔴 {err}
              </div>
            ))}

            {grammarData.grammarErrors.map((err, idx) => (
              <div key={`gr-${idx}`} className="p-3 rounded-xl bg-amber-50/50 dark:bg-amber-950/20 text-xs text-amber-900 dark:text-amber-200 font-mono">
                🟠 {err}
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Priority Action Suggestions */}
      <Card className="space-y-4">
        <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
          <Sparkles className="w-5 h-5 text-primary-500" />
          <span>Priority Professional Writing Suggestions</span>
        </h3>
        <div className="space-y-3">
          {grammarData.suggestions.map((sug, idx) => (
            <div
              key={idx}
              className="p-4 rounded-2xl bg-gray-50 dark:bg-dark-hover border border-gray-200/60 dark:border-dark-border text-xs sm:text-sm text-gray-800 dark:text-gray-200 flex items-start space-x-3"
            >
              <ChevronRight className="w-4 h-4 text-primary-500 mt-0.5 flex-shrink-0" />
              <span>{sug}</span>
            </div>
          ))}
        </div>
      </Card>
    </motion.div>
  );
};
