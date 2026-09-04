import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  Briefcase,
  Search,
  Filter,
  Eye,
  Download,
  Award,
  Calendar,
  UserCheck,
  CheckCircle2,
  AlertCircle,
  RefreshCw,
} from 'lucide-react';
import { CandidateItem } from '../types/admin';
import { adminService } from '../services/adminService';
import { reportService } from '../services/reportService';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

export const RecruiterDashboardPage: React.FC = () => {
  const navigate = useNavigate();

  const [candidates, setCandidates] = useState<CandidateItem[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [skillFilter, setSkillFilter] = useState<string>('');
  const [minScoreFilter, setMinScoreFilter] = useState<number | undefined>(undefined);

  const fetchCandidates = async () => {
    setIsLoading(true);
    try {
      const data = await adminService.searchCandidates(skillFilter, minScoreFilter);
      setCandidates(data);
    } catch {
      // Fallback
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchCandidates();
  }, []);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchCandidates();
  };

  if (isLoading) {
    return (
      <div className="py-20 flex justify-center">
        <LoadingSpinner size="lg" label="Searching Candidate Talent Pool & ATS Evaluation Data..." />
      </div>
    );
  }

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
            <Briefcase className="w-8 h-8 text-primary-500" />
            <span>Recruiter Talent Search Portal</span>
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400 mt-1">
            Search candidates by skill keywords, minimum ATS score thresholds, and target role alignment.
          </p>
        </div>
      </div>

      {/* Candidate Search Toolbar */}
      <Card className="p-6">
        <form onSubmit={handleSearchSubmit} className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div>
            <label className="text-xs font-semibold text-gray-500 block mb-1">Search by Skill Keyword</label>
            <div className="relative">
              <Search className="w-4 h-4 text-gray-400 absolute left-3 top-3" />
              <input
                type="text"
                placeholder="e.g. Java, React, Docker..."
                value={skillFilter}
                onChange={(e) => setSkillFilter(e.target.value)}
                className="w-full pl-9 pr-4 py-2 bg-gray-50 dark:bg-dark-hover border border-gray-200 dark:border-dark-border rounded-xl text-xs sm:text-sm text-gray-900 dark:text-white"
              />
            </div>
          </div>

          <div>
            <label className="text-xs font-semibold text-gray-500 block mb-1">Minimum ATS Score</label>
            <input
              type="number"
              placeholder="e.g. 80"
              value={minScoreFilter || ''}
              onChange={(e) => setMinScoreFilter(e.target.value ? Number(e.target.value) : undefined)}
              className="w-full px-4 py-2 bg-gray-50 dark:bg-dark-hover border border-gray-200 dark:border-dark-border rounded-xl text-xs sm:text-sm text-gray-900 dark:text-white"
            />
          </div>

          <div className="flex items-end space-x-3">
            <Button variant="primary" size="sm" type="submit" leftIcon={<Search className="w-4 h-4" />}>
              Filter Candidates
            </Button>
            <Button
              variant="outline"
              size="sm"
              type="button"
              onClick={() => {
                setSkillFilter('');
                setMinScoreFilter(undefined);
                fetchCandidates();
              }}
            >
              Reset
            </Button>
          </div>
        </form>
      </Card>

      {/* Candidate Cards Grid */}
      <div className="space-y-4">
        <span className="text-xs font-semibold text-gray-500">
          Showing {candidates.length} Candidate Profiles
        </span>

        {candidates.map((cand) => (
          <Card key={cand.resumeId} className="p-6 space-y-4 border-gray-200/60 dark:border-dark-border">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div className="space-y-1">
                <div className="flex items-center space-x-3">
                  <h3 className="text-base font-bold text-gray-900 dark:text-white">
                    {cand.candidateName}
                  </h3>
                  <Badge variant="primary" size="sm">
                    {cand.email}
                  </Badge>
                </div>
                <p className="text-xs text-gray-500 dark:text-gray-400 flex items-center space-x-2">
                  <Calendar className="w-3.5 h-3.5" />
                  <span>Uploaded {cand.uploadedAt}</span>
                  <span>•</span>
                  <span>{cand.fileName}</span>
                </p>
              </div>

              <div className="flex items-center space-x-3">
                <div className="px-4 py-2 rounded-2xl bg-primary-50 dark:bg-primary-950/40 text-center">
                  <span className="text-base font-extrabold text-primary-600 dark:text-primary-400">{cand.atsScore}</span>
                  <p className="text-[9px] text-gray-400 uppercase font-bold">ATS Score</p>
                </div>

                <div className="px-4 py-2 rounded-2xl bg-emerald-50 dark:bg-emerald-950/40 text-center">
                  <span className="text-base font-extrabold text-emerald-600 dark:text-emerald-400">{cand.matchPercentage}%</span>
                  <p className="text-[9px] text-gray-400 uppercase font-bold">Target Match</p>
                </div>

                <Link to={`/dashboard/resumes/${cand.resumeId}`}>
                  <Button variant="primary" size="sm" leftIcon={<Eye className="w-3.5 h-3.5" />}>
                    View Candidate Details
                  </Button>
                </Link>
              </div>
            </div>

            <div className="p-3 rounded-2xl bg-gray-50 dark:bg-dark-hover flex flex-wrap gap-2 text-xs">
              <span className="font-bold text-gray-700 dark:text-gray-300 mr-2">Top Candidate Skills:</span>
              {cand.topSkills.map((sk, idx) => (
                <Badge key={idx} variant="success" size="sm">
                  ✓ {sk}
                </Badge>
              ))}
            </div>
          </Card>
        ))}
      </div>
    </motion.div>
  );
};
