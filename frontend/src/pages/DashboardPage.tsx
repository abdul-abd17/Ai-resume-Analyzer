import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  LayoutDashboard,
  Sparkles,
  BarChart3,
  SpellCheck,
  Briefcase,
  FileText,
  TrendingUp,
  Award,
  Search,
  Bell,
  Download,
  Share2,
  Filter,
  Check,
  ArrowRight,
  ShieldCheck,
  Clock,
  Eye,
  Activity,
  Layers,
  Zap,
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
  AreaChart,
  Area,
} from 'recharts';
import { DashboardData, NotificationItem } from '../types/dashboard';
import { dashboardService } from '../services/dashboardService';
import { useAuth } from '../hooks/useAuth';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { NotificationDrawer } from '../components/dashboard/NotificationDrawer';

const COLORS = ['#0284c7', '#6366f1', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899'];

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [data, setData] = useState<DashboardData | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [activeTab, setActiveTab] = useState<'skills' | 'grammar' | 'match' | 'ai'>('skills');

  // Search & Filter state
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [scoreFilter, setScoreFilter] = useState<string>('all');

  // Notifications drawer state
  const [isNotifOpen, setIsNotifOpen] = useState<boolean>(false);
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [isShared, setIsShared] = useState<boolean>(false);

  const fetchDashboard = async () => {
    setIsLoading(true);
    try {
      const res = await dashboardService.getDashboardData();
      setData(res);
      setNotifications(res.notifications || []);
    } catch {
      // Fallback UI
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  const handleClearNotifications = () => {
    setNotifications([]);
  };

  const handleShareReport = () => {
    if (!data) return;
    const summaryText = `ResuMatch Analytics Summary for ${user?.fullName || 'User'}:\n` +
      `• ATS Score: ${data.quickStats.averageAtsScore}/100\n` +
      `• Grammar Score: ${data.quickStats.averageGrammarScore}/100\n` +
      `• Job Match: ${data.quickStats.latestJobMatchPercentage}%\n` +
      `• Resume Strength: ${data.quickStats.overallResumeStrengthScore}/100\n` +
      `Generated on ${new Date().toLocaleDateString()}`;

    navigator.clipboard.writeText(summaryText);
    setIsShared(true);
    setTimeout(() => setIsShared(false), 2000);
  };

  const handleDownloadSummary = () => {
    if (!data) return;
    const reportText = `RESUMATCH UNIFIED ANALYTICS DASHBOARD SUMMARY\n` +
      `User: ${user?.fullName} (${user?.email})\n` +
      `Date: ${new Date().toLocaleString()}\n\n` +
      `=======================================\nQUICK STATS METRICS\n=======================================\n` +
      `Average ATS Score: ${data.quickStats.averageAtsScore}/100\n` +
      `Average Grammar Score: ${data.quickStats.averageGrammarScore}/100\n` +
      `Latest Job Match Alignment: ${data.quickStats.latestJobMatchPercentage}%\n` +
      `Overall Resume Strength: ${data.quickStats.overallResumeStrengthScore}/100\n` +
      `Total Analyses Executed: ${data.quickStats.totalAnalysesCount}\n` +
      `Total Uploaded Resumes: ${data.quickStats.totalUploadedResumes}\n` +
      `Latest Resume: ${data.quickStats.latestResumeFileName}\n\n` +
      `=======================================\nSKILL ANALYTICS SUMMARY\n=======================================\n` +
      `Keyword Coverage: ${data.skillAnalytics.keywordCoverage}%\n` +
      `Top Extracted Skills: ${Array.isArray(data.skillAnalytics.topSkills) ? data.skillAnalytics.topSkills.join(', ') : ''}\n` +
      `Missing Keywords: ${Array.isArray(data.skillAnalytics.missingSkills) ? data.skillAnalytics.missingSkills.join(', ') : ''}\n\n` +
      `=======================================\nAI INSIGHTS & SUMMARY\n=======================================\n` +
      `Professional Summary: ${data.aiAnalytics.professionalSummary}\n` +
      `Experience Bullet: ${data.aiAnalytics.improvedExperience}\n`;

    const blob = new Blob([reportText], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `ResuMatch_Dashboard_Summary_${new Date().toISOString().slice(0, 10)}.txt`;
    a.click();
    URL.revokeObjectURL(url);
  };

  if (isLoading) {
    return (
      <div className="py-20 flex justify-center">
        <LoadingSpinner size="lg" label="Aggregating ATS, Grammar, Job Match & AI Analytics..." />
      </div>
    );
  }

  if (!data) {
    return (
      <div className="py-12 text-center text-xs text-gray-500">
        Failed to load dashboard metrics. Please refresh.
      </div>
    );
  }

  const unreadNotifCount = notifications.filter((n) => !n.read).length;

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="space-y-8 max-w-7xl mx-auto pb-16"
    >
      {/* Header Bar */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 dark:text-white flex items-center space-x-3">
            <span>Welcome back, {user?.fullName?.split(' ')[0] || 'Architect'}!</span>
            <span className="text-xl">👋</span>
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400 mt-1">
            {new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
          </p>
        </div>

        <div className="flex items-center space-x-3">
          {/* Notification Button */}
          <button
            onClick={() => setIsNotifOpen(true)}
            className="relative p-2.5 rounded-xl bg-white dark:bg-dark-card border border-gray-200 dark:border-dark-border text-gray-600 dark:text-gray-300 hover:text-primary-600 dark:hover:text-primary-400 transition-colors shadow-sm"
            title="Notification Center"
          >
            <Bell className="w-5 h-5" />
            {unreadNotifCount > 0 && (
              <span className="absolute -top-1 -right-1 w-5 h-5 rounded-full bg-red-500 text-white text-[10px] font-extrabold flex items-center justify-center animate-pulse">
                {unreadNotifCount}
              </span>
            )}
          </button>

          {/* Action Export Buttons */}
          <Button
            variant="outline"
            size="sm"
            onClick={handleShareReport}
            leftIcon={isShared ? <Check className="w-4 h-4 text-emerald-500" /> : <Share2 className="w-4 h-4" />}
          >
            {isShared ? 'Copied' : 'Share'}
          </Button>

          <Button
            variant="primary"
            size="sm"
            onClick={handleDownloadSummary}
            leftIcon={<Download className="w-4 h-4" />}
          >
            Download Report
          </Button>
        </div>
      </div>

      {/* Notification Slide-Over Drawer */}
      <NotificationDrawer
        isOpen={isNotifOpen}
        onClose={() => setIsNotifOpen(false)}
        notifications={notifications}
        onClearAll={handleClearNotifications}
      />

      {/* 7 Quick Stats Glassmorphism Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* ATS Score Card */}
        <Card className="p-5 space-y-3 bg-gradient-to-br from-primary-500/10 via-white to-white dark:from-primary-950/40 dark:via-dark-card dark:to-dark-card border-primary-200/80 dark:border-dark-border shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400">
              Average ATS Score
            </span>
            <div className="w-8 h-8 rounded-xl bg-primary-600 text-white flex items-center justify-center">
              <BarChart3 className="w-4 h-4" />
            </div>
          </div>

          <div className="flex items-baseline space-x-2">
            <span className="text-3xl font-extrabold text-gray-900 dark:text-white">
              {data.quickStats.averageAtsScore}
            </span>
            <span className="text-xs text-gray-400 font-semibold">/ 100</span>
          </div>

          <div className="w-full bg-gray-200 dark:bg-dark-hover h-2 rounded-full overflow-hidden">
            <div
              className="h-full bg-primary-600 transition-all duration-700"
              style={{ width: `${data.quickStats.averageAtsScore}%` }}
            />
          </div>
        </Card>

        {/* Grammar Score Card */}
        <Card className="p-5 space-y-3 bg-gradient-to-br from-purple-500/10 via-white to-white dark:from-purple-950/40 dark:via-dark-card dark:to-dark-card border-purple-200/80 dark:border-dark-border shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400">
              Grammar Score
            </span>
            <div className="w-8 h-8 rounded-xl bg-purple-600 text-white flex items-center justify-center">
              <SpellCheck className="w-4 h-4" />
            </div>
          </div>

          <div className="flex items-baseline space-x-2">
            <span className="text-3xl font-extrabold text-gray-900 dark:text-white">
              {data.quickStats.averageGrammarScore}
            </span>
            <span className="text-xs text-gray-400 font-semibold">/ 100</span>
          </div>

          <div className="w-full bg-gray-200 dark:bg-dark-hover h-2 rounded-full overflow-hidden">
            <div
              className="h-full bg-purple-600 transition-all duration-700"
              style={{ width: `${data.quickStats.averageGrammarScore}%` }}
            />
          </div>
        </Card>

        {/* Job Match Card */}
        <Card className="p-5 space-y-3 bg-gradient-to-br from-emerald-500/10 via-white to-white dark:from-emerald-950/40 dark:via-dark-card dark:to-dark-card border-emerald-200/80 dark:border-dark-border shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400">
              Target Job Match
            </span>
            <div className="w-8 h-8 rounded-xl bg-emerald-600 text-white flex items-center justify-center">
              <Briefcase className="w-4 h-4" />
            </div>
          </div>

          <div className="flex items-baseline space-x-2">
            <span className="text-3xl font-extrabold text-gray-900 dark:text-white">
              {data.quickStats.latestJobMatchPercentage}%
            </span>
            <span className="text-xs text-emerald-600 dark:text-emerald-400 font-semibold">Matched</span>
          </div>

          <div className="w-full bg-gray-200 dark:bg-dark-hover h-2 rounded-full overflow-hidden">
            <div
              className="h-full bg-emerald-600 transition-all duration-700"
              style={{ width: `${data.quickStats.latestJobMatchPercentage}%` }}
            />
          </div>
        </Card>

        {/* Resume Strength Card */}
        <Card className="p-5 space-y-3 bg-gradient-to-br from-amber-500/10 via-white to-white dark:from-amber-950/40 dark:via-dark-card dark:to-dark-card border-amber-200/80 dark:border-dark-border shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400">
              Resume Strength
            </span>
            <div className="w-8 h-8 rounded-xl bg-amber-600 text-white flex items-center justify-center">
              <ShieldCheck className="w-4 h-4" />
            </div>
          </div>

          <div className="flex items-baseline space-x-2">
            <span className="text-3xl font-extrabold text-gray-900 dark:text-white">
              {data.quickStats.overallResumeStrengthScore}
            </span>
            <span className="text-xs text-gray-400 font-semibold">/ 100</span>
          </div>

          <div className="w-full bg-gray-200 dark:bg-dark-hover h-2 rounded-full overflow-hidden">
            <div
              className="h-full bg-amber-600 transition-all duration-700"
              style={{ width: `${data.quickStats.overallResumeStrengthScore}%` }}
            />
          </div>
        </Card>
      </div>

      {/* 3 Secondary Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card className="p-4 flex items-center justify-between">
          <div>
            <span className="text-[11px] font-semibold text-gray-400 uppercase tracking-wider block">
              Total Analyses Ran
            </span>
            <span className="text-xl font-extrabold text-gray-900 dark:text-white mt-0.5 block">
              {data.quickStats.totalAnalysesCount}
            </span>
          </div>
          <Activity className="w-7 h-7 text-primary-500 opacity-80" />
        </Card>

        <Card className="p-4 flex items-center justify-between">
          <div>
            <span className="text-[11px] font-semibold text-gray-400 uppercase tracking-wider block">
              Uploaded Resumes
            </span>
            <span className="text-xl font-extrabold text-gray-900 dark:text-white mt-0.5 block">
              {data.quickStats.totalUploadedResumes}
            </span>
          </div>
          <FileText className="w-7 h-7 text-indigo-500 opacity-80" />
        </Card>

        <Card className="p-4 flex items-center justify-between">
          <div className="truncate">
            <span className="text-[11px] font-semibold text-gray-400 uppercase tracking-wider block">
              Latest Resume File
            </span>
            <span className="text-sm font-bold text-gray-900 dark:text-white truncate mt-0.5 block">
              {data.quickStats.latestResumeFileName}
            </span>
          </div>
          {data.quickStats.latestResumeId && (
            <Link to={`/dashboard/resumes/${data.quickStats.latestResumeId}`}>
              <Button variant="outline" size="sm" rightIcon={<Eye className="w-3.5 h-3.5" />}>
                View
              </Button>
            </Link>
          )}
        </Card>
      </div>

      {/* Recharts Data Visualizations Grid (6 Interactive Charts) */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Pie Chart: Skill Category Distribution */}
        <Card className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <BarChart3 className="w-5 h-5 text-primary-500" />
            <span>Category-Wise Skill Distribution</span>
          </h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={data.chartsData.skillDistributionPie || []}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={80}
                  paddingAngle={5}
                  dataKey="count"
                  nameKey="category"
                >
                  {(data.chartsData.skillDistributionPie || []).map((_: any, index: number) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </Card>

        {/* Bar Chart: Grammar Errors */}
        <Card className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <SpellCheck className="w-5 h-5 text-purple-500" />
            <span>Grammar & Writing Error Breakdown</span>
          </h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data.chartsData.grammarErrorsBar || []}>
                <XAxis dataKey="type" stroke="#888888" fontSize={11} />
                <YAxis stroke="#888888" fontSize={11} />
                <Tooltip />
                <Bar dataKey="count" fill="#8b5cf6" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>

        {/* Radar Chart: Resume Quality */}
        <Card className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <ShieldCheck className="w-5 h-5 text-emerald-500" />
            <span>Multi-Vector Quality Radar</span>
          </h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <RadarChart data={data.chartsData.qualityRadar || []}>
                <PolarGrid />
                <PolarAngleAxis dataKey="subject" fontSize={11} />
                <PolarRadiusAxis angle={30} domain={[0, 100]} fontSize={10} />
                <Radar name="Score" dataKey="score" stroke="#10b981" fill="#10b981" fillOpacity={0.4} />
              </RadarChart>
            </ResponsiveContainer>
          </div>
        </Card>

        {/* Line Chart: ATS Score History */}
        <Card className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <TrendingUp className="w-5 h-5 text-amber-500" />
            <span>ATS Score Timeline Progress</span>
          </h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={data.chartsData.atsImprovementLine || []}>
                <XAxis dataKey="run" stroke="#888888" fontSize={11} />
                <YAxis domain={[0, 100]} stroke="#888888" fontSize={11} />
                <Tooltip />
                <Line type="monotone" dataKey="score" stroke="#f59e0b" strokeWidth={3} dot={{ r: 5 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </div>

      {/* Tabbed Analytics Component */}
      <Card className="p-6 space-y-6">
        <div className="flex border-b border-gray-200 dark:border-dark-border space-x-6">
          {[
            { id: 'skills', label: 'Skill Analytics', icon: Award },
            { id: 'grammar', label: 'Grammar Analytics', icon: SpellCheck },
            { id: 'match', label: 'Job Match Analytics', icon: Briefcase },
            { id: 'ai', label: 'AI Insights', icon: Sparkles },
          ].map((tab) => {
            const Icon = tab.icon;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`pb-3 text-xs sm:text-sm font-semibold transition-colors flex items-center space-x-2 ${
                  activeTab === tab.id
                    ? 'border-b-2 border-primary-600 text-primary-600 dark:text-primary-400'
                    : 'text-gray-500 hover:text-gray-700 dark:text-gray-400'
                }`}
              >
                <Icon className="w-4 h-4" />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </div>

        {/* Tab Content */}
        {activeTab === 'skills' && (
          <div className="space-y-4 text-xs sm:text-sm">
            <div className="flex items-center justify-between">
              <span className="font-bold text-gray-900 dark:text-white">
                Keyword Coverage Ratio: {data.skillAnalytics.keywordCoverage}%
              </span>
              <Badge variant="primary" size="sm">
                Java DSA Max-Heap
              </Badge>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="p-4 rounded-2xl bg-gray-50 dark:bg-dark-hover space-y-2">
                <span className="font-bold text-gray-900 dark:text-white block">Top Extracted Skills</span>
                <div className="flex flex-wrap gap-2">
                  {(data.skillAnalytics.topSkills || []).map((sk: string, idx: number) => (
                    <Badge key={idx} variant="success" size="sm">
                      ✓ {sk}
                    </Badge>
                  ))}
                </div>
              </div>

              <div className="p-4 rounded-2xl bg-gray-50 dark:bg-dark-hover space-y-2">
                <span className="font-bold text-gray-900 dark:text-white block">Missing Keywords</span>
                <div className="flex flex-wrap gap-2">
                  {(data.skillAnalytics.missingSkills || []).map((sk: string, idx: number) => (
                    <Badge key={idx} variant="warning" size="sm">
                      + {sk}
                    </Badge>
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'grammar' && (
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-center">
            <div className="p-4 rounded-2xl bg-gray-50 dark:bg-dark-hover">
              <span className="text-xl font-bold text-purple-600 dark:text-purple-400">
                {data.grammarAnalytics.grammarScore}/100
              </span>
              <p className="text-[10px] text-gray-400">Grammar Score</p>
            </div>
            <div className="p-4 rounded-2xl bg-gray-50 dark:bg-dark-hover">
              <span className="text-xl font-bold text-indigo-600 dark:text-indigo-400">
                {data.grammarAnalytics.readabilityScore}/100
              </span>
              <p className="text-[10px] text-gray-400">Readability Index</p>
            </div>
            <div className="p-4 rounded-2xl bg-gray-50 dark:bg-dark-hover">
              <span className="text-xl font-bold text-amber-600 dark:text-amber-400">
                {data.grammarAnalytics.passiveVoiceCount}
              </span>
              <p className="text-[10px] text-gray-400">Passive Voice</p>
            </div>
            <div className="p-4 rounded-2xl bg-gray-50 dark:bg-dark-hover">
              <span className="text-xl font-bold text-red-600 dark:text-red-400">
                {data.grammarAnalytics.spellingErrorCount}
              </span>
              <p className="text-[10px] text-gray-400">Spelling Typos</p>
            </div>
          </div>
        )}

        {activeTab === 'match' && (
          <div className="space-y-3 text-xs sm:text-sm">
            <div className="flex items-center justify-between p-4 rounded-2xl bg-emerald-50 dark:bg-emerald-950/30 text-emerald-900 dark:text-emerald-200">
              <span className="font-bold">
                Target Role Alignment: {data.jobMatchAnalytics.matchPercentage}%
              </span>
              <span className="text-xs">{data.jobMatchAnalytics.hiringRecommendation}</span>
            </div>
          </div>
        )}

        {activeTab === 'ai' && (
          <div className="space-y-4 text-xs sm:text-sm">
            <div className="p-4 rounded-2xl bg-primary-50/50 dark:bg-primary-950/30 border border-primary-200/60 dark:border-dark-border text-gray-800 dark:text-gray-200">
              <span className="font-bold block text-primary-600 dark:text-primary-400 mb-1">
                AI Professional Summary Recommendation:
              </span>
              <p className="leading-relaxed font-sans">{data.aiAnalytics.professionalSummary}</p>
            </div>
          </div>
        )}
      </Card>

      {/* Recent Activity Table */}
      <Card className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <Clock className="w-5 h-5 text-gray-500" />
            <span>Recent Activity Timeline</span>
          </h3>
        </div>

        <div className="space-y-2">
          {data.recentActivity && data.recentActivity.length > 0 ? (
            data.recentActivity.map((act) => (
              <div
                key={act.id}
                className="p-4 rounded-2xl bg-gray-50 dark:bg-dark-hover border border-gray-200/60 dark:border-dark-border flex items-center justify-between text-xs"
              >
                <div className="flex items-center space-x-3">
                  <Activity className="w-4 h-4 text-primary-500" />
                  <div>
                    <span className="font-bold text-gray-900 dark:text-white">{act.title}</span>
                    <p className="text-[10px] text-gray-400">{act.timestamp}</p>
                  </div>
                </div>

                <Badge variant="primary" size="sm">
                  {act.statusBadge}
                </Badge>
              </div>
            ))
          ) : (
            <p className="text-xs text-gray-500 text-center py-4">No recent activity items</p>
          )}
        </div>
      </Card>
    </motion.div>
  );
};
