import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  Briefcase,
  CheckCircle2,
  AlertTriangle,
  ArrowLeft,
  RefreshCw,
  Sparkles,
  PieChart as PieIcon,
  BarChart3,
  ShieldCheck,
  TrendingUp,
  Award,
  ChevronRight,
  FileText,
  AlertCircle,
  Code,
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
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar,
  LineChart,
  Line,
} from 'recharts';
import { MatchAnalysis } from '../types/job';
import { jobService } from '../services/jobService';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

const COLORS = ['#10b981', '#f59e0b', '#6366f1', '#ec4899'];

export const MatchComparisonPage: React.FC = () => {
  const { resumeId, jdId } = useParams<{ resumeId: string; jdId: string }>();
  const navigate = useNavigate();

  const [matchData, setMatchData] = useState<MatchAnalysis | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isComparing, setIsComparing] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const fetchMatchAnalysis = async () => {
    if (!resumeId || !jdId) return;
    setIsLoading(true);
    setError(null);
    try {
      const rId = Number(resumeId);
      const jId = Number(jdId);
      const data = await jobService.compareResume(rId, jId);
      setMatchData(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to compare resume vs job description.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchMatchAnalysis();
  }, [resumeId, jdId]);

  if (isLoading) {
    return (
      <div className="py-20 flex justify-center">
        <LoadingSpinner size="lg" label="Running Java DSA Match Comparison Engine..." />
      </div>
    );
  }

  if (error || !matchData) {
    return (
      <div className="max-w-2xl mx-auto py-12 text-center space-y-4">
        <Card className="p-8 space-y-4">
          <AlertCircle className="w-12 h-12 text-red-500 mx-auto" />
          <h3 className="text-lg font-bold text-gray-900 dark:text-white">Comparison Failed</h3>
          <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400">{error}</p>
          <Button variant="outline" size="sm" onClick={() => navigate('/dashboard/job-description')}>
            Back to Job Descriptions
          </Button>
        </Card>
      </div>
    );
  }

  const pieData = [
    { name: 'Matched Skills', value: matchData.matchedSkills.length },
    { name: 'Missing Skills', value: matchData.missingSkills.length },
    { name: 'Extra Skills', value: matchData.extraSkills.length },
  ];

  const barData = [
    { section: 'Skills (40%)', match: matchData.skillMatchPercentage },
    { section: 'Experience (20%)', match: matchData.experienceMatchPercentage },
    { section: 'Keywords (20%)', match: matchData.keywordCoveragePercentage },
    { section: 'Education (10%)', match: matchData.educationMatchPercentage },
    { section: 'Projects (10%)', match: matchData.projectMatchPercentage },
  ];

  const radarData = [
    { subject: 'Skills', score: matchData.skillMatchPercentage },
    { subject: 'Experience', score: matchData.experienceMatchPercentage },
    { subject: 'Keywords', score: matchData.keywordCoveragePercentage },
    { subject: 'Education', score: matchData.educationMatchPercentage },
    { subject: 'Projects', score: matchData.projectMatchPercentage },
  ];

  const getMatchColor = (pct: number) => {
    if (pct >= 80) return 'text-emerald-500 border-emerald-500';
    if (pct >= 60) return 'text-amber-500 border-amber-500';
    return 'text-red-500 border-red-500';
  };

  const getMatchBadge = (pct: number) => {
    if (pct >= 80) return { label: 'High Alignment', variant: 'success' as const };
    if (pct >= 60) return { label: 'Moderate Match', variant: 'warning' as const };
    return { label: 'Low Match', variant: 'purple' as const };
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-6xl mx-auto space-y-8 pb-16"
    >
      {/* Navigation Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <button
          onClick={() => navigate('/dashboard/job-description')}
          className="flex items-center space-x-2 text-xs font-semibold text-gray-600 dark:text-gray-400 hover:text-primary-600 dark:hover:text-primary-400 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Job Descriptions</span>
        </button>

        <div className="flex items-center space-x-3">
          <Button
            variant="outline"
            size="sm"
            onClick={fetchMatchAnalysis}
            leftIcon={<RefreshCw className="w-4 h-4" />}
          >
            Re-run Match Engine
          </Button>
        </div>
      </div>

      {/* Main Overall Match Hero Card */}
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
                  className={getMatchColor(matchData.overallMatchPercentage)}
                  strokeDasharray={264}
                  strokeDashoffset={264 - (264 * matchData.overallMatchPercentage) / 100}
                  strokeLinecap="round"
                  fill="transparent"
                  style={{ transition: 'stroke-dashoffset 1s ease-in-out' }}
                />
              </svg>
              <div className="absolute inset-0 flex flex-col items-center justify-center">
                <span className="text-3xl font-black tracking-tight">
                  {matchData.overallMatchPercentage}%
                </span>
                <span className="text-[10px] text-gray-400 font-semibold uppercase">
                  Match Score
                </span>
              </div>
            </div>
            <Badge variant={getMatchBadge(matchData.overallMatchPercentage).variant} size="md">
              {getMatchBadge(matchData.overallMatchPercentage).label}
            </Badge>
          </div>

          {/* Hiring Verdict Info */}
          <div className="md:col-span-2 space-y-4">
            <div>
              <span className="text-[11px] font-semibold text-primary-300 uppercase tracking-wider">
                Resume VS Job Description Match
              </span>
              <h1 className="text-xl sm:text-2xl font-bold mt-0.5">
                {matchData.jobTitle || 'Target Role'} <span className="text-gray-400">at</span> {matchData.companyName || 'Target Company'}
              </h1>
              <p className="text-xs text-gray-300 mt-1 font-mono">
                Resume File: {matchData.resumeFileName}
              </p>
            </div>

            <div className="p-4 rounded-2xl bg-white/10 border border-white/10 text-xs sm:text-sm font-semibold flex items-center space-x-3 text-emerald-300">
              <ShieldCheck className="w-5 h-5 flex-shrink-0 text-emerald-400" />
              <span>{matchData.hiringRecommendation}</span>
            </div>
          </div>
        </div>
      </Card>

      {/* 5-Part Weighted Breakdown Progress Grid */}
      <div className="space-y-4">
        <h2 className="text-lg font-bold text-gray-900 dark:text-white flex items-center space-x-2">
          <BarChart3 className="w-5 h-5 text-primary-500" />
          <span>Weighted Section Match Progress (100% Total)</span>
        </h2>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
          {[
            { label: 'Skills Match (40%)', pct: matchData.skillMatchPercentage, color: 'bg-emerald-500' },
            { label: 'Experience Match (20%)', pct: matchData.experienceMatchPercentage, color: 'bg-indigo-500' },
            { label: 'Keywords Coverage (20%)', pct: matchData.keywordCoveragePercentage, color: 'bg-amber-500' },
            { label: 'Education Match (10%)', pct: matchData.educationMatchPercentage, color: 'bg-purple-500' },
            { label: 'Projects Match (10%)', pct: matchData.projectMatchPercentage, color: 'bg-blue-500' },
          ].map((sec, idx) => (
            <Card key={idx} className="p-4 space-y-2">
              <span className="text-[11px] font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider block truncate">
                {sec.label}
              </span>
              <div className="flex items-center justify-between">
                <span className="text-base font-extrabold text-gray-900 dark:text-white">
                  {sec.pct}%
                </span>
              </div>
              <div className="w-full bg-gray-200 dark:bg-dark-hover h-2 rounded-full overflow-hidden">
                <div
                  className={`h-full ${sec.color} transition-all duration-700`}
                  style={{ width: `${sec.pct}%` }}
                />
              </div>
            </Card>
          ))}
        </div>
      </div>

      {/* Data Visualizations Grid (Recharts) */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Pie Chart: Matched vs Missing Skills */}
        <Card className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <PieIcon className="w-5 h-5 text-primary-500" />
            <span>Skills Set Proportion (HashSet Intersection/Difference)</span>
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

        {/* Bar Chart: Section Match Breakdown */}
        <Card className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <BarChart3 className="w-5 h-5 text-indigo-500" />
            <span>5-Part Section Match Comparison</span>
          </h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={barData}>
                <XAxis dataKey="section" stroke="#888888" fontSize={10} />
                <YAxis domain={[0, 100]} stroke="#888888" fontSize={11} />
                <Tooltip />
                <Bar dataKey="match" fill="#6366f1" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>

        {/* Radar Chart: Resume Strength vs JD */}
        <Card className="space-y-4 lg:col-span-2">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <ShieldCheck className="w-5 h-5 text-emerald-500" />
            <span>Multi-Axis Resume vs JD Requirements Radar</span>
          </h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <RadarChart data={radarData}>
                <PolarGrid />
                <PolarAngleAxis dataKey="subject" fontSize={11} />
                <PolarRadiusAxis angle={30} domain={[0, 100]} fontSize={10} />
                <Radar name="Match Score" dataKey="score" stroke="#10b981" fill="#10b981" fillOpacity={0.4} />
              </RadarChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </div>

      {/* Skill Classification Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Matched Skills */}
        <Card className="space-y-4 border-l-4 border-l-emerald-500">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <CheckCircle2 className="w-5 h-5 text-emerald-500" />
            <span>Matched Skills ({matchData.matchedSkills.length})</span>
          </h3>
          <div className="flex flex-wrap gap-2">
            {matchData.matchedSkills.length > 0 ? (
              matchData.matchedSkills.map((sk, idx) => (
                <Badge key={idx} variant="success" size="sm">
                  ✓ {sk}
                </Badge>
              ))
            ) : (
              <p className="text-xs text-gray-500">No overlapping skills detected</p>
            )}
          </div>
        </Card>

        {/* Missing Skills (PriorityQueue Ranked) */}
        <Card className="space-y-4 border-l-4 border-l-amber-500">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <AlertTriangle className="w-5 h-5 text-amber-500" />
            <span>Missing Skills ({matchData.missingSkills.length})</span>
          </h3>
          <div className="flex flex-wrap gap-2">
            {matchData.missingSkills.length > 0 ? (
              matchData.missingSkills.map((sk, idx) => (
                <Badge key={idx} variant="warning" size="sm">
                  + {sk}
                </Badge>
              ))
            ) : (
              <p className="text-xs text-emerald-500 font-semibold">100% Required Skills Covered!</p>
            )}
          </div>
        </Card>

        {/* Extra Skills */}
        <Card className="space-y-4 border-l-4 border-l-blue-500">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <Code className="w-5 h-5 text-blue-500" />
            <span>Additional Skills ({matchData.extraSkills.length})</span>
          </h3>
          <div className="flex flex-wrap gap-2">
            {matchData.extraSkills.length > 0 ? (
              matchData.extraSkills.map((sk, idx) => (
                <Badge key={idx} variant="primary" size="sm">
                  {sk}
                </Badge>
              ))
            ) : (
              <p className="text-xs text-gray-500">No extra skills found</p>
            )}
          </div>
        </Card>
      </div>

      {/* Priority Recommendations Card */}
      <Card className="space-y-4">
        <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
          <Sparkles className="w-5 h-5 text-primary-500" />
          <span>Priority Action Recommendations (Sorted via Comparator)</span>
        </h3>
        <div className="space-y-3">
          {matchData.priorityRecommendations.map((rec, idx) => (
            <div
              key={idx}
              className="p-4 rounded-2xl bg-gray-50 dark:bg-dark-hover border border-gray-200/60 dark:border-dark-border text-xs sm:text-sm text-gray-800 dark:text-gray-200 flex items-start space-x-3"
            >
              <ChevronRight className="w-4 h-4 text-primary-500 mt-0.5 flex-shrink-0" />
              <span>{rec}</span>
            </div>
          ))}
        </div>
      </Card>
    </motion.div>
  );
};
