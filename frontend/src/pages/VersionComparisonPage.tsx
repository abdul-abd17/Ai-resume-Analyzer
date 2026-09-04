import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  GitCompare,
  ArrowLeft,
  CheckCircle2,
  TrendingUp,
  PlusCircle,
  MinusCircle,
  Award,
  BookOpen,
  Briefcase,
  AlertCircle,
  RefreshCw,
} from 'lucide-react';
import {
  ResponsiveContainer,
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
import { ResumeComparison } from '../types/version';
import { versionService } from '../services/versionService';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

export const VersionComparisonPage: React.FC = () => {
  const { oldId, newId } = useParams<{ oldId: string; newId: string }>();
  const navigate = useNavigate();

  const [comparison, setComparison] = useState<ResumeComparison | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchComparison = async () => {
    if (!oldId || !newId) return;
    setIsLoading(true);
    setError(null);
    try {
      const data = await versionService.compareVersions(Number(oldId), Number(newId));
      setComparison(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to compare resume versions.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchComparison();
  }, [oldId, newId]);

  if (isLoading) {
    return (
      <div className="py-20 flex justify-center">
        <LoadingSpinner size="lg" label="Running Java DSA Set Difference Engine on Selected Versions..." />
      </div>
    );
  }

  if (error || !comparison) {
    return (
      <div className="max-w-2xl mx-auto py-12 text-center space-y-4">
        <Card className="p-8 space-y-4">
          <AlertCircle className="w-12 h-12 text-red-500 mx-auto" />
          <h3 className="text-lg font-bold text-gray-900 dark:text-white">Comparison Error</h3>
          <p className="text-xs text-gray-500">{error || 'Could not load version comparison.'}</p>
          <Button variant="outline" size="sm" onClick={() => navigate('/dashboard/history')}>
            Back to Version History
          </Button>
        </Card>
      </div>
    );
  }

  const { oldVersion, newVersion } = comparison;

  const atsDelta = comparison.atsDifference;
  const grammarDelta = comparison.grammarDifference;
  const matchDelta = comparison.jobMatchDifference;

  const barData = [
    { metric: 'ATS Score', Old: oldVersion.atsScore ?? 0, New: newVersion.atsScore ?? 0 },
    { metric: 'Grammar Score', Old: oldVersion.grammarScore ?? 0, New: newVersion.grammarScore ?? 0 },
    { metric: 'Job Match %', Old: oldVersion.jobMatchScore ?? 0, New: newVersion.jobMatchScore ?? 0 },
  ];

  const radarData = [
    { subject: 'ATS Keywords', Old: oldVersion.atsScore ?? 0, New: newVersion.atsScore ?? 0 },
    { subject: 'Grammar', Old: oldVersion.grammarScore ?? 0, New: newVersion.grammarScore ?? 0 },
    { subject: 'Job Alignment', Old: oldVersion.jobMatchScore ?? 0, New: newVersion.jobMatchScore ?? 0 },
  ];

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-6xl mx-auto space-y-8 pb-16"
    >
      {/* Back Header */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate('/dashboard/history')}
          className="flex items-center space-x-2 text-xs font-semibold text-gray-600 dark:text-gray-400 hover:text-primary-600 dark:hover:text-primary-400 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Version History</span>
        </button>

        <Button variant="outline" size="sm" onClick={fetchComparison} leftIcon={<RefreshCw className="w-4 h-4" />}>
          Refresh Comparison
        </Button>
      </div>

      {/* Main Score Delta Banner */}
      <Card className="p-6 sm:p-8 bg-gradient-to-r from-slate-900 via-indigo-950 to-primary-950 text-white rounded-3xl relative overflow-hidden shadow-2xl space-y-6">
        <div className="relative z-10 flex flex-col md:flex-row items-center justify-between gap-6">
          <div>
            <div className="flex items-center space-x-3">
              <GitCompare className="w-7 h-7 text-primary-400" />
              <h1 className="text-xl sm:text-2xl font-bold">
                Side-by-Side Version Comparison
              </h1>
            </div>
            <p className="text-xs text-gray-300 mt-1">
              Comparing <span className="font-bold text-primary-300">v{oldVersion.versionNumber} ({oldVersion.versionName})</span> vs <span className="font-bold text-emerald-300">v{newVersion.versionNumber} ({newVersion.versionName})</span>
            </p>
          </div>

          {/* Metric Delta Badges */}
          <div className="grid grid-cols-3 gap-3 text-center">
            <div className="p-3 rounded-2xl bg-white/10 border border-white/10">
              <span className={`text-base font-extrabold ${atsDelta >= 0 ? 'text-emerald-400' : 'text-red-400'}`}>
                {atsDelta >= 0 ? `+${atsDelta}` : atsDelta}
              </span>
              <p className="text-[9px] text-gray-300 uppercase">ATS Delta</p>
            </div>

            <div className="p-3 rounded-2xl bg-white/10 border border-white/10">
              <span className={`text-base font-extrabold ${grammarDelta >= 0 ? 'text-purple-400' : 'text-red-400'}`}>
                {grammarDelta >= 0 ? `+${grammarDelta}` : grammarDelta}
              </span>
              <p className="text-[9px] text-gray-300 uppercase">Grammar Delta</p>
            </div>

            <div className="p-3 rounded-2xl bg-white/10 border border-white/10">
              <span className={`text-base font-extrabold ${matchDelta >= 0 ? 'text-blue-400' : 'text-red-400'}`}>
                {matchDelta >= 0 ? `+${matchDelta}%` : `${matchDelta}%`}
              </span>
              <p className="text-[9px] text-gray-300 uppercase">Match Delta</p>
            </div>
          </div>
        </div>

        <div className="p-4 rounded-2xl bg-white/10 border border-white/10 text-xs text-gray-200">
          <span className="font-bold text-primary-300">Summary: </span>
          {comparison.summary}
        </div>
      </Card>

      {/* Added & Removed Skills Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Added Skills */}
        <Card className="space-y-4 border-l-4 border-l-emerald-500">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <PlusCircle className="w-5 h-5 text-emerald-500" />
            <span>Skills Added in v{newVersion.versionNumber} ({comparison.addedSkills.length})</span>
          </h3>
          <div className="flex flex-wrap gap-2">
            {comparison.addedSkills.length > 0 ? (
              comparison.addedSkills.map((sk, idx) => (
                <span key={idx} className="px-3 py-1.5 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800 text-xs font-semibold">
                  + {sk}
                </span>
              ))
            ) : (
              <p className="text-xs text-gray-400">No new skills added in this version.</p>
            )}
          </div>
        </Card>

        {/* Removed Skills */}
        <Card className="space-y-4 border-l-4 border-l-red-500">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <MinusCircle className="w-5 h-5 text-red-500" />
            <span>Skills Removed from v{oldVersion.versionNumber} ({comparison.removedSkills.length})</span>
          </h3>
          <div className="flex flex-wrap gap-2">
            {comparison.removedSkills.length > 0 ? (
              comparison.removedSkills.map((sk, idx) => (
                <span key={idx} className="px-3 py-1.5 rounded-xl bg-red-50 dark:bg-red-950/40 text-red-700 dark:text-red-300 border border-red-200 dark:border-red-800 text-xs font-semibold line-through">
                  - {sk}
                </span>
              ))
            ) : (
              <p className="text-xs text-gray-400">No skills removed in this version.</p>
            )}
          </div>
        </Card>
      </div>

      {/* Visualizations Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Bar Chart Comparison */}
        <Card className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <TrendingUp className="w-5 h-5 text-primary-500" />
            <span>Metrics Comparison Bar Chart</span>
          </h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={barData}>
                <XAxis dataKey="metric" stroke="#888888" fontSize={11} />
                <YAxis domain={[0, 100]} stroke="#888888" fontSize={11} />
                <Tooltip />
                <Legend />
                <Bar dataKey="Old" fill="#94a3b8" radius={[8, 8, 0, 0]} />
                <Bar dataKey="New" fill="#0284c7" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>

        {/* Radar Comparison */}
        <Card className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <Award className="w-5 h-5 text-indigo-500" />
            <span>Quality Vector Radar Overlay</span>
          </h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <RadarChart data={radarData}>
                <PolarGrid />
                <PolarAngleAxis dataKey="subject" fontSize={11} />
                <PolarRadiusAxis angle={30} domain={[0, 100]} fontSize={10} />
                <Radar name="Old Version" dataKey="Old" stroke="#94a3b8" fill="#94a3b8" fillOpacity={0.3} />
                <Radar name="New Version" dataKey="New" stroke="#6366f1" fill="#6366f1" fillOpacity={0.5} />
                <Legend />
              </RadarChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </div>

      {/* Side-by-Side Version Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card className="space-y-3 bg-gray-50/50 dark:bg-dark-hover/50">
          <div className="flex items-center justify-between border-b pb-2">
            <span className="font-bold text-gray-900 dark:text-white">v{oldVersion.versionNumber} ({oldVersion.versionName})</span>
            <Badge variant="purple" size="sm">Old Draft</Badge>
          </div>
          <p className="text-xs text-gray-600 dark:text-gray-300 font-mono">
            {oldVersion.changeSummary}
          </p>
        </Card>

        <Card className="space-y-3 bg-emerald-50/20 dark:bg-emerald-950/20 border-emerald-200 dark:border-emerald-800">
          <div className="flex items-center justify-between border-b pb-2">
            <span className="font-bold text-gray-900 dark:text-white">v{newVersion.versionNumber} ({newVersion.versionName})</span>
            <Badge variant="success" size="sm">New Iteration</Badge>
          </div>
          <p className="text-xs text-gray-600 dark:text-gray-300 font-mono">
            {newVersion.changeSummary}
          </p>
        </Card>
      </div>
    </motion.div>
  );
};
