import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { BarChart3, Activity, Calendar, Trash2, ArrowRight, ShieldCheck, CheckCircle2 } from 'lucide-react';
import { AnalysisReport } from '../types/analysis';
import { analysisService } from '../services/analysisService';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

export const ReportsPlaceholderPage: React.FC = () => {
  const navigate = useNavigate();
  const [history, setHistory] = useState<AnalysisReport[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const fetchHistory = async () => {
    setIsLoading(true);
    try {
      const data = await analysisService.getAnalysisHistory();
      setHistory(data);
    } catch {
      // Ignore
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  const handleDelete = async (id: number, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!window.confirm('Delete this ATS report?')) return;
    try {
      await analysisService.deleteAnalysis(id);
      fetchHistory();
    } catch {
      alert('Failed to delete report.');
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="space-y-6 max-w-5xl mx-auto pb-12"
    >
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 dark:text-white">
            ATS Evaluation Reports
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400 mt-1">
            Analysis history queue retrieved from backend GET /api/analysis/history
          </p>
        </div>

        <Badge variant="purple" size="md">
          Step 4 Analysis Engine
        </Badge>
      </div>

      {isLoading ? (
        <div className="py-16 flex justify-center">
          <LoadingSpinner size="lg" label="Loading ATS report history..." />
        </div>
      ) : history.length === 0 ? (
        <Card className="p-12 text-center space-y-4 border-dashed">
          <div className="w-16 h-16 rounded-3xl bg-primary-50 dark:bg-primary-950/60 text-primary-600 dark:text-primary-400 flex items-center justify-center mx-auto shadow-sm">
            <BarChart3 className="w-8 h-8" />
          </div>
          <div className="space-y-1">
            <h3 className="text-lg font-bold text-gray-900 dark:text-white">
              No ATS Reports Generated Yet
            </h3>
            <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400 max-w-md mx-auto">
              Upload a resume and click "Run ATS Analysis" to evaluate keyword coverage and scoring.
            </p>
          </div>
          <div className="pt-2">
            <Link to="/dashboard/upload">
              <Button variant="primary" size="sm" rightIcon={<ArrowRight className="w-4 h-4" />}>
                Go to Resume Upload Module
              </Button>
            </Link>
          </div>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {history.map((report) => (
            <Card
              key={report.id}
              hoverEffect
              onClick={() => navigate(`/dashboard/analysis/${report.resumeId}`)}
              className="p-6 space-y-4 flex flex-col justify-between border-gray-200 dark:border-dark-border hover:border-primary-500"
            >
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <Badge variant={report.overallScore >= 80 ? 'success' : report.overallScore >= 60 ? 'warning' : 'purple'} size="sm">
                    Score: {report.overallScore} / 100
                  </Badge>
                  <button
                    onClick={(e) => handleDelete(report.id, e)}
                    className="p-1 text-gray-400 hover:text-red-500 transition-colors"
                    title="Delete Report"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>

                <div>
                  <h3 className="text-base font-bold text-gray-900 dark:text-white truncate">
                    {report.resumeFileName || `Resume #${report.resumeId}`}
                  </h3>
                  <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5 flex items-center space-x-1.5">
                    <Calendar className="w-3.5 h-3.5" />
                    <span>{new Date(report.analysisDate).toLocaleString()}</span>
                  </p>
                </div>

                <div className="grid grid-cols-3 gap-2 pt-2 text-center text-xs">
                  <div className="p-2 rounded-xl bg-gray-50 dark:bg-dark-hover">
                    <span className="font-bold text-emerald-600 dark:text-emerald-400">
                      {report.keywordCoverage}%
                    </span>
                    <p className="text-[10px] text-gray-400">Coverage</p>
                  </div>
                  <div className="p-2 rounded-xl bg-gray-50 dark:bg-dark-hover">
                    <span className="font-bold text-amber-600 dark:text-amber-400">
                      {report.missingKeywordCount}
                    </span>
                    <p className="text-[10px] text-gray-400">Missing</p>
                  </div>
                  <div className="p-2 rounded-xl bg-gray-50 dark:bg-dark-hover">
                    <span className="font-bold text-indigo-600 dark:text-indigo-400">
                      {report.skillsScore}/25
                    </span>
                    <p className="text-[10px] text-gray-400">Skills Pts</p>
                  </div>
                </div>
              </div>

              <div className="pt-2 flex items-center justify-between text-xs font-semibold text-primary-600 dark:text-primary-400">
                <span>View Full ATS Report & Charts</span>
                <ArrowRight className="w-4 h-4" />
              </div>
            </Card>
          ))}
        </div>
      )}
    </motion.div>
  );
};
