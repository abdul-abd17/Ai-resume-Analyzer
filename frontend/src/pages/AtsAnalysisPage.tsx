import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  PieChart as PieIcon,
  BarChart3,
  TrendingUp,
  Activity,
  Award,
  CheckCircle2,
  AlertTriangle,
  ArrowLeft,
  RefreshCw,
  Trash2,
  Sparkles,
  Zap,
  ShieldCheck,
  FileText,
  Search,
  ChevronRight,
  AlertCircle,
  X,
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
  LineChart,
  Line,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar,
} from 'recharts';
import { AnalysisReport } from '../types/analysis';
import { analysisService } from '../services/analysisService';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

const COLORS = ['#0284c7', '#6366f1', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#14b8a6'];

export type AtsState = 'NO_ANALYSIS' | 'ANALYZING' | 'ANALYZED' | 'ERROR';

export const AtsAnalysisPage: React.FC = () => {
  const { resumeId } = useParams<{ resumeId: string }>();
  const navigate = useNavigate();

  const [atsState, setAtsState] = useState<AtsState>('NO_ANALYSIS');
  const [report, setReport] = useState<AnalysisReport | null>(null);
  const [history, setHistory] = useState<AnalysisReport[]>([]);
  const [isLoadingInitial, setIsLoadingInitial] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fetchReportAndHistory = async () => {
    if (!resumeId) return;
    setIsLoadingInitial(true);
    setErrorMessage(null);
    try {
      const id = Number(resumeId);

      try {
        const hist = await analysisService.getAnalysisHistory();
        setHistory(hist);
      } catch {
        // Non-blocking history fetch
      }

      // Strictly read-only GET call
      const data = await analysisService.getAnalysis(id);
      setReport(data);
      setAtsState('ANALYZED');
    } catch (err: any) {
      setReport(null);
      setAtsState('NO_ANALYSIS');
      if (err.response?.status !== 404) {
        setErrorMessage(err.response?.data?.message || 'No ATS analysis has been run yet.');
      }
    } finally {
      setIsLoadingInitial(false);
    }
  };

  useEffect(() => {
    fetchReportAndHistory();
  }, [resumeId]);

  const handleRunAnalysis = async () => {
    if (!resumeId || atsState === 'ANALYZING') return;

    setAtsState('ANALYZING');
    setErrorMessage(null);
    try {
      const data = await analysisService.analyzeResume(Number(resumeId));
      setReport(data);
      setAtsState('ANALYZED');

      try {
        const hist = await analysisService.getAnalysisHistory();
        setHistory(hist);
      } catch {
        // Non-blocking history update
      }
    } catch (err: any) {
      setErrorMessage(err.response?.data?.message || 'Failed to run ATS analysis.');
      setAtsState('ERROR');
    }
  };

  const handleDeleteReport = async () => {
    if (!report) return;
    if (!window.confirm('Are you sure you want to delete this ATS report?')) return;
    try {
      await analysisService.deleteAnalysis(report.id);
      navigate(`/dashboard/resumes/${resumeId}`);
    } catch {
      alert('Failed to delete analysis report.');
    }
  };

  if (isLoadingInitial) {
    return (
      <div className="py-20 flex justify-center">
        <LoadingSpinner size="lg" label="Checking ATS Analysis status..." />
      </div>
    );
  }

  if (atsState === 'NO_ANALYSIS') {
    return (
      <div className="max-w-2xl mx-auto py-12 text-center space-y-4">
        <Card className="p-8 space-y-5 shadow-lg border border-gray-200 dark:border-dark-border">
          <div className="w-16 h-16 rounded-2xl bg-primary-100 dark:bg-primary-950/60 text-primary-600 dark:text-primary-400 flex items-center justify-center mx-auto shadow-inner">
            <BarChart3 className="w-8 h-8" />
          </div>
          <div className="space-y-2">
            <h3 className="text-xl font-bold text-gray-900 dark:text-white">No ATS Analysis Found</h3>
            <p className="text-sm text-gray-500 dark:text-gray-400 max-w-md mx-auto">
              No ATS analysis has been run yet.
            </p>
          </div>
          {errorMessage && (
            <div className="p-3 bg-red-50 dark:bg-red-950/30 text-red-600 dark:text-red-400 text-xs rounded-xl">
              {errorMessage}
            </div>
          )}
          <div className="flex justify-center space-x-3 pt-2">
            <Button variant="outline" size="sm" onClick={() => navigate(`/dashboard/resumes/${resumeId}`)}>
              Back to Resume Details
            </Button>
            <Button
              id="run-ats-analysis-btn"
              variant="primary"
              size="md"
              disabled={(atsState as string) === 'ANALYZING'}
              onClick={handleRunAnalysis}
              leftIcon={<Sparkles className="w-4 h-4" />}
            >
              Run ATS Analysis
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  if (atsState === 'ANALYZING') {
    return (
      <div className="max-w-2xl mx-auto py-16 text-center space-y-4">
        <Card className="p-8 space-y-6">
          <LoadingSpinner size="lg" label="Running Java DSA ATS Analysis Engine..." />
          <p className="text-xs text-gray-500 dark:text-gray-400">
            Analyzing skill frequency, keyword coverage, and scoring sections...
          </p>
        </Card>
      </div>
    );
  }

  if (atsState === 'ERROR') {
    return (
      <div className="max-w-2xl mx-auto py-12 text-center space-y-4">
        <Card className="p-8 space-y-4">
          <AlertCircle className="w-12 h-12 text-red-500 mx-auto" />
          <h3 className="text-lg font-bold text-gray-900 dark:text-white">Analysis Failed</h3>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            {errorMessage || 'Failed to complete ATS analysis. Please try again.'}
          </p>
          <div className="flex justify-center space-x-3 pt-2">
            <Button variant="outline" size="sm" onClick={() => navigate(`/dashboard/resumes/${resumeId}`)}>
              Back to Resume Details
            </Button>
            <Button
              variant="primary"
              size="sm"
              disabled={(atsState as string) === 'ANALYZING'}
              onClick={handleRunAnalysis}
              leftIcon={<RefreshCw className="w-4 h-4" />}
            >
              Retry ATS Analysis
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  if (!report) {
    return null;
  }

  // Data formatting for Recharts
  const pieData = [
    { name: 'Contact', value: report.contactScore },
    { name: 'Summary', value: report.summaryScore },
    { name: 'Skills', value: report.skillsScore },
    { name: 'Experience', value: report.experienceScore },
    { name: 'Education', value: report.educationScore },
    { name: 'Projects', value: report.projectsScore },
    { name: 'Certifications', value: report.certificationScore },
  ];

  const parseJsonArray = (val: any) => {
    if (!val) return [];
    if (Array.isArray(val)) return val;
    try {
      return JSON.parse(val);
    } catch {
      return [];
    }
  };

  const topSkillsList = parseJsonArray(report.topSkills);

  const barData = topSkillsList.map((s: any) => ({
    name: typeof s === 'string' ? s : s.keyword || s.name || '',
    frequency: typeof s === 'object' && s.frequency ? s.frequency : 1,
    power: typeof s === 'object' && s.power ? s.power : 5,
  }));

  const strengthsList = parseJsonArray(report.strengths || (report as any).resumeStrength);
  const weaknessesList = parseJsonArray(report.weaknesses || (report as any).resumeWeakness);
  const suggestionsList = parseJsonArray(report.suggestions);
  const missingKeywordsList = parseJsonArray(report.missingKeywords);
  const duplicateKeywordsList = parseJsonArray(report.duplicateKeywords);

  const lineData = history.map((h, i) => ({
    attempt: `Run ${history.length - i}`,
    score: h.overallScore,
    date: new Date(h.analysisDate).toLocaleDateString([], { month: 'short', day: 'numeric' }),
  })).reverse();

  const radarData = [
    { subject: 'Contact', score: report.contactScore * 10 },
    { subject: 'Summary', score: report.summaryScore * 10 },
    { subject: 'Skills', score: Math.round((report.skillsScore / 25) * 100) },
    { subject: 'Experience', score: Math.round((report.experienceScore / 20) * 100) },
    { subject: 'Education', score: report.educationScore * 10 },
    { subject: 'Projects', score: report.projectsScore * 10 },
  ];

  const getScoreColor = (score: number) => {
    if (score >= 80) return 'text-emerald-500 border-emerald-500';
    if (score >= 60) return 'text-amber-500 border-amber-500';
    return 'text-red-500 border-red-500';
  };

  const getScoreBadge = (score: number) => {
    if (score >= 80) return { label: 'ATS Ready', variant: 'success' as const };
    if (score >= 60) return { label: 'Moderate Match', variant: 'warning' as const };
    return { label: 'Needs Optimization', variant: 'purple' as const };
  };

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
            onClick={fetchReportAndHistory}
            leftIcon={<RefreshCw className="w-4 h-4" />}
          >
            Refresh
          </Button>

          <Button
            variant="primary"
            size="sm"
            isLoading={(atsState as string) === 'ANALYZING'}
            onClick={handleRunAnalysis}
            leftIcon={<Zap className="w-4 h-4" />}
          >
            Re-Analyze ATS Score
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

      {/* Main Score Hero Banner */}
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
                  className={getScoreColor(report.overallScore)}
                  strokeDasharray={264}
                  strokeDashoffset={264 - (264 * report.overallScore) / 100}
                  strokeLinecap="round"
                  fill="transparent"
                  style={{ transition: 'stroke-dashoffset 1s ease-in-out' }}
                />
              </svg>
              <div className="absolute inset-0 flex flex-col items-center justify-center">
                <span className="text-3xl font-black tracking-tight">{report.overallScore}</span>
                <span className="text-[10px] text-gray-400 font-semibold uppercase">Out of 100</span>
              </div>
            </div>
            <Badge variant={getScoreBadge(report.overallScore).variant} size="md">
              {getScoreBadge(report.overallScore).label}
            </Badge>
          </div>

          {/* Quick Metrics */}
          <div className="md:col-span-2 space-y-4">
            <div>
              <h1 className="text-xl sm:text-2xl font-bold">
                ATS Evaluation for <span className="text-primary-300">{report.resumeFileName}</span>
              </h1>
              <p className="text-xs text-gray-300 mt-1">
                Analyzed via Java DSA Engine (HashMap, HashSet, PriorityQueue Max-Heap, Greedy Scoring)
              </p>
            </div>

            <div className="grid grid-cols-3 gap-4 pt-2">
              <div className="p-3 rounded-2xl bg-white/10 border border-white/10 text-center">
                <span className="text-lg font-bold text-emerald-400">{report.keywordCoverage}%</span>
                <p className="text-[10px] text-gray-300">Keyword Coverage</p>
              </div>

              <div className="p-3 rounded-2xl bg-white/10 border border-white/10 text-center">
                <span className="text-lg font-bold text-amber-400">{report.missingKeywordCount}</span>
                <p className="text-[10px] text-gray-300">Missing Keywords</p>
              </div>

              <div className="p-3 rounded-2xl bg-white/10 border border-white/10 text-center">
                <span className="text-lg font-bold text-purple-400">{report.duplicateKeywordCount}</span>
                <p className="text-[10px] text-gray-300">Overused Words</p>
              </div>
            </div>
          </div>
        </div>
      </Card>

      {/* Section Breakdown Score Cards */}
      <div className="space-y-4">
        <h2 className="text-lg font-bold text-gray-900 dark:text-white flex items-center space-x-2">
          <Activity className="w-5 h-5 text-primary-500" />
          <span>Section Scoring Breakdown</span>
        </h2>

        <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-4">
          {[
            { label: 'Contact', score: report.contactScore, max: 10 },
            { label: 'Summary', score: report.summaryScore, max: 10 },
            { label: 'Skills', score: report.skillsScore, max: 25 },
            { label: 'Experience', score: report.experienceScore, max: 20 },
            { label: 'Education', score: report.educationScore, max: 10 },
            { label: 'Projects', score: report.projectsScore, max: 10 },
            { label: 'Certs', score: report.certificationScore, max: 5 },
          ].map((sec, idx) => (
            <Card key={idx} className="p-4 text-center space-y-1 hover:border-primary-500 transition-colors">
              <span className="text-[11px] font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {sec.label}
              </span>
              <p className="text-lg font-extrabold text-gray-900 dark:text-white">
                {sec.score} <span className="text-xs font-medium text-gray-400">/ {sec.max}</span>
              </p>
            </Card>
          ))}
        </div>
      </div>

      {/* Data Visualization Charts Grid (Recharts) */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Pie Chart: Section Scores */}
        <Card className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <PieIcon className="w-5 h-5 text-primary-500" />
            <span>Section Contribution Pie Chart</span>
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

        {/* Bar Chart: Skill Frequency */}
        <Card className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <BarChart3 className="w-5 h-5 text-indigo-500" />
            <span>Max-Heap Ranked Skills Frequency</span>
          </h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={barData}>
                <XAxis dataKey="name" stroke="#888888" fontSize={11} />
                <YAxis stroke="#888888" fontSize={11} />
                <Tooltip />
                <Bar dataKey="frequency" fill="#6366f1" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>

        {/* Radar Chart: Resume Quality */}
        <Card className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <ShieldCheck className="w-5 h-5 text-emerald-500" />
            <span>Multi-Axis Quality Radar</span>
          </h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <RadarChart data={radarData}>
                <PolarGrid />
                <PolarAngleAxis dataKey="subject" fontSize={11} />
                <PolarRadiusAxis angle={30} domain={[0, 100]} fontSize={10} />
                <Radar name="Quality Score" dataKey="score" stroke="#10b981" fill="#10b981" fillOpacity={0.4} />
              </RadarChart>
            </ResponsiveContainer>
          </div>
        </Card>

        {/* Line Chart: Analysis History */}
        <Card className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <TrendingUp className="w-5 h-5 text-amber-500" />
            <span>Analysis Score History (LinkedList Queue)</span>
          </h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={lineData}>
                <XAxis dataKey="attempt" stroke="#888888" fontSize={11} />
                <YAxis domain={[0, 100]} stroke="#888888" fontSize={11} />
                <Tooltip />
                <Line type="monotone" dataKey="score" stroke="#f59e0b" strokeWidth={3} dot={{ r: 5 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </div>

      {/* Keyword Analysis Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Top Skills (Max-Heap) */}
        <Card className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <Award className="w-5 h-5 text-primary-500" />
            <span>Top Ranked Skills</span>
          </h3>
          <div className="space-y-2">
            {report.topSkills && report.topSkills.length > 0 ? (
              report.topSkills.map((s, idx) => (
                <div
                  key={idx}
                  className="flex items-center justify-between p-3 rounded-xl bg-gray-50 dark:bg-dark-hover text-xs"
                >
                  <span className="font-semibold text-gray-900 dark:text-white">{s.keyword}</span>
                  <Badge variant="primary" size="sm">
                    {s.frequency}x
                  </Badge>
                </div>
              ))
            ) : (
              <p className="text-xs text-gray-500">No top skills detected</p>
            )}
          </div>
        </Card>

        {/* Missing Keywords */}
        <Card className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <AlertTriangle className="w-5 h-5 text-amber-500" />
            <span>Missing Keywords</span>
          </h3>
          <div className="flex flex-wrap gap-2">
            {report.missingKeywords && report.missingKeywords.length > 0 ? (
              report.missingKeywords.map((kw, idx) => (
                <Badge key={idx} variant="warning" size="sm">
                  + {kw}
                </Badge>
              ))
            ) : (
              <p className="text-xs text-emerald-500 font-semibold">100% Keyword Coverage!</p>
            )}
          </div>
        </Card>

        {/* Duplicate / Overused Words */}
        <Card className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <FileText className="w-5 h-5 text-purple-500" />
            <span>Overused Keywords</span>
          </h3>
          <div className="flex flex-wrap gap-2">
            {duplicateKeywordsList && duplicateKeywordsList.length > 0 ? (
              duplicateKeywordsList.map((kw: string, idx: number) => (
                <Badge key={idx} variant="purple" size="sm">
                  {kw}
                </Badge>
              ))
            ) : (
              <p className="text-xs text-gray-500">No overused repetition detected</p>
            )}
          </div>
        </Card>
      </div>

      {/* Strengths & Weaknesses */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card className="space-y-4 border-l-4 border-l-emerald-500">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <CheckCircle2 className="w-5 h-5 text-emerald-500" />
            <span>Resume Strengths</span>
          </h3>
          <ul className="space-y-2 text-xs sm:text-sm text-gray-700 dark:text-gray-300">
            {strengthsList.map((str: string, idx: number) => (
              <li key={idx} className="flex items-start space-x-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-500 mt-0.5 flex-shrink-0" />
                <span>{str}</span>
              </li>
            ))}
          </ul>
        </Card>

        <Card className="space-y-4 border-l-4 border-l-amber-500">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <AlertTriangle className="w-5 h-5 text-amber-500" />
            <span>Areas to Improve</span>
          </h3>
          <ul className="space-y-2 text-xs sm:text-sm text-gray-700 dark:text-gray-300">
            {weaknessesList.map((wk: string, idx: number) => (
              <li key={idx} className="flex items-start space-x-2">
                <AlertCircle className="w-4 h-4 text-amber-500 mt-0.5 flex-shrink-0" />
                <span>{wk}</span>
              </li>
            ))}
          </ul>
        </Card>
      </div>

      {/* Prioritized Improvement Suggestions */}
      <Card className="space-y-4">
        <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
          <Sparkles className="w-5 h-5 text-primary-500" />
          <span>Priority Action Suggestions (Sorted via Comparator)</span>
        </h3>
        <div className="space-y-3">
          {suggestionsList.map((sug: string, idx: number) => (
            <div
              key={idx}
              className="p-3.5 rounded-xl bg-gray-50 dark:bg-dark-hover border border-gray-200 dark:border-dark-border flex items-start space-x-3 text-xs sm:text-sm text-gray-700 dark:text-gray-300"
            >
              <span className="w-5 h-5 rounded-full bg-primary-600 text-white font-extrabold text-[10px] flex items-center justify-center flex-shrink-0">
                {idx + 1}
              </span>
              <span>{sug}</span>
            </div>
          ))}
        </div>
      </Card>
    </motion.div>
  );
};
