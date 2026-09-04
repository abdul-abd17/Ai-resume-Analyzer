import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import {
  Cpu,
  Clock,
  CheckCircle2,
  AlertCircle,
  XCircle,
  RotateCcw,
  Ban,
  RefreshCw,
  FileText,
  Calendar,
  Zap,
} from 'lucide-react';
import { JobItem } from '../types/saas';
import { jobService } from '../services/jobService';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

export const JobDashboardPage: React.FC = () => {
  const [jobs, setJobs] = useState<JobItem[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const fetchJobs = async () => {
    setIsLoading(true);
    try {
      const data = await jobService.getJobs();
      setJobs(data);
    } catch {
      // Fallback
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchJobs();
  }, []);

  const handleRetry = async (jobId: number) => {
    try {
      await jobService.retryJob(jobId);
      fetchJobs();
    } catch {
      alert('Failed to retry job.');
    }
  };

  const handleCancel = async (jobId: number) => {
    try {
      await jobService.cancelJob(jobId);
      fetchJobs();
    } catch {
      alert('Failed to cancel job.');
    }
  };

  if (isLoading) {
    return (
      <div className="py-20 flex justify-center">
        <LoadingSpinner size="lg" label="Loading Asynchronous Job Queue & ThreadPool Operations..." />
      </div>
    );
  }

  const queuedCount = jobs.filter((j) => j.jobStatus === 'QUEUED').length;
  const runningCount = jobs.filter((j) => j.jobStatus === 'RUNNING').length;
  const completedCount = jobs.filter((j) => j.jobStatus === 'COMPLETED').length;
  const failedCount = jobs.filter((j) => j.jobStatus === 'FAILED').length;

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-6xl mx-auto space-y-8 pb-16"
    >
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 dark:text-white flex items-center space-x-3">
            <Cpu className="w-8 h-8 text-primary-500" />
            <span>Background Job Queue Dashboard</span>
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400 mt-1">
            Asynchronous threadpool task execution for heavy document parsing, ATS calculation, and PDF rendering.
          </p>
        </div>

        <Button variant="outline" size="sm" onClick={fetchJobs} leftIcon={<RefreshCw className="w-4 h-4" />}>
          Refresh Queue
        </Button>
      </div>

      {/* Overview Cards Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <Card className="p-4 space-y-1">
          <span className="text-[10px] font-bold uppercase text-amber-500 flex items-center space-x-1">
            <Clock className="w-3.5 h-3.5" />
            <span>Queued</span>
          </span>
          <span className="text-2xl font-extrabold text-gray-900 dark:text-white block">{queuedCount}</span>
        </Card>

        <Card className="p-4 space-y-1">
          <span className="text-[10px] font-bold uppercase text-primary-500 flex items-center space-x-1">
            <Zap className="w-3.5 h-3.5" />
            <span>Running</span>
          </span>
          <span className="text-2xl font-extrabold text-primary-600 dark:text-primary-400 block">{runningCount}</span>
        </Card>

        <Card className="p-4 space-y-1">
          <span className="text-[10px] font-bold uppercase text-emerald-500 flex items-center space-x-1">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Completed</span>
          </span>
          <span className="text-2xl font-extrabold text-emerald-600 dark:text-emerald-400 block">{completedCount}</span>
        </Card>

        <Card className="p-4 space-y-1">
          <span className="text-[10px] font-bold uppercase text-red-500 flex items-center space-x-1">
            <AlertCircle className="w-3.5 h-3.5" />
            <span>Failed</span>
          </span>
          <span className="text-2xl font-extrabold text-red-600 dark:text-red-400 block">{failedCount}</span>
        </Card>
      </div>

      {/* Job Queue Table */}
      <Card className="p-6 space-y-4">
        <h3 className="text-base font-bold text-gray-900 dark:text-white">Active & Historical Tasks</h3>

        {jobs.length === 0 ? (
          <div className="py-12 text-center space-y-3">
            <Cpu className="w-12 h-12 text-gray-400 mx-auto" />
            <h4 className="text-base font-bold text-gray-900 dark:text-white">No Jobs Queued</h4>
            <p className="text-xs text-gray-500">Asynchronous tasks will automatically register here when triggered.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs sm:text-sm">
              <thead>
                <tr className="border-b border-gray-200 dark:border-dark-border text-gray-400 font-bold uppercase">
                  <th className="py-3 px-4">Job ID & Type</th>
                  <th className="py-3 px-4">Resume Target</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Started At</th>
                  <th className="py-3 px-4">Actions</th>
                </tr>
              </thead>
              <tbody>
                {jobs.map((j) => (
                  <tr key={j.id} className="border-b border-gray-100 dark:border-dark-border/50">
                    <td className="py-3 px-4">
                      <span className="font-bold text-gray-900 dark:text-white block">#{j.id} — {j.jobType}</span>
                      <span className="text-gray-400 text-xs">{j.createdBy}</span>
                    </td>
                    <td className="py-3 px-4 text-gray-600 dark:text-gray-300 font-medium">
                      {j.resumeFileName || 'resume.pdf'}
                    </td>
                    <td className="py-3 px-4">
                      <Badge
                        variant={
                          j.jobStatus === 'COMPLETED'
                            ? 'success'
                            : j.jobStatus === 'RUNNING'
                            ? 'primary'
                            : j.jobStatus === 'QUEUED'
                            ? 'warning'
                            : 'purple'
                        }
                        size="sm"
                      >
                        {j.jobStatus}
                      </Badge>
                    </td>
                    <td className="py-3 px-4 text-gray-500 text-xs">
                      {new Date(j.startedAt).toLocaleString()}
                    </td>
                    <td className="py-3 px-4 flex items-center space-x-2">
                      {j.jobStatus === 'FAILED' && (
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleRetry(j.id)}
                          leftIcon={<RotateCcw className="w-3.5 h-3.5" />}
                        >
                          Retry
                        </Button>
                      )}

                      {(j.jobStatus === 'QUEUED' || j.jobStatus === 'RUNNING') && (
                        <Button
                          variant="danger"
                          size="sm"
                          onClick={() => handleCancel(j.id)}
                          leftIcon={<Ban className="w-3.5 h-3.5" />}
                        >
                          Cancel
                        </Button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </motion.div>
  );
};
