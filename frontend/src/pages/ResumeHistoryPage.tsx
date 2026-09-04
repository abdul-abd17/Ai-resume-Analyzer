import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  History,
  GitCommit,
  GitCompare,
  RotateCcw,
  Trash2,
  Edit3,
  Eye,
  Check,
  X,
  Search,
  Filter,
  Plus,
  ArrowRight,
  ShieldCheck,
  SpellCheck,
  Briefcase,
  Calendar,
  Layers,
  AlertCircle,
} from 'lucide-react';
import { ResumeVersion } from '../types/version';
import { versionService } from '../services/versionService';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

export const ResumeHistoryPage: React.FC = () => {
  const navigate = useNavigate();

  const [versions, setVersions] = useState<ResumeVersion[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [searchQuery, setSearchQuery] = useState<string>('');

  // Modals
  const [renameModalVer, setRenameModalVer] = useState<ResumeVersion | null>(null);
  const [newVersionName, setNewVersionName] = useState<string>('');

  const [compareModalOpen, setCompareModalOpen] = useState<boolean>(false);
  const [selectedOldId, setSelectedOldId] = useState<number | null>(null);
  const [selectedNewId, setSelectedNewId] = useState<number | null>(null);

  const fetchVersions = async () => {
    setIsLoading(true);
    try {
      const data = await versionService.getVersions();
      setVersions(data);
    } catch {
      // Fallback
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchVersions();
  }, []);

  const handleRenameSubmit = async () => {
    if (!renameModalVer || !newVersionName.trim()) return;
    try {
      await versionService.renameVersion(renameModalVer.id, newVersionName.trim());
      setRenameModalVer(null);
      fetchVersions();
    } catch {
      alert('Failed to rename version.');
    }
  };

  const handleRestore = async (ver: ResumeVersion) => {
    if (ver.isCurrent) return;
    if (!window.confirm(`Restore Version ${ver.versionNumber} ("${ver.versionName}") as the current active resume?`)) return;
    try {
      await versionService.restoreVersion(ver.id);
      fetchVersions();
    } catch {
      alert('Failed to restore version.');
    }
  };

  const handleDelete = async (ver: ResumeVersion) => {
    if (ver.isCurrent) {
      alert('Cannot delete the current active version.');
      return;
    }
    if (!window.confirm(`Delete Version ${ver.versionNumber}?`)) return;
    try {
      await versionService.deleteVersion(ver.id);
      fetchVersions();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to delete version.');
    }
  };

  const handleCompareSubmit = () => {
    if (!selectedOldId || !selectedNewId) {
      alert('Please select both an Old Version and a New Version.');
      return;
    }
    if (selectedOldId === selectedNewId) {
      alert('Cannot compare a version to itself.');
      return;
    }
    setCompareModalOpen(false);
    navigate(`/dashboard/history/compare/${selectedOldId}/${selectedNewId}`);
  };

  const filteredVersions = versions.filter((v) =>
    v.versionName.toLowerCase().includes(searchQuery.toLowerCase()) ||
    v.fileName.toLowerCase().includes(searchQuery.toLowerCase())
  );

  if (isLoading) {
    return (
      <div className="py-20 flex justify-center">
        <LoadingSpinner size="lg" label="Loading Resume History & Versions Timeline..." />
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
            <History className="w-7 h-7 text-primary-500" />
            <span>Resume History & Version Management</span>
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400 mt-1">
            Track improvements across iterations, compare side-by-side diffs, and restore historical drafts.
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <Button
            variant="primary"
            size="sm"
            onClick={() => setCompareModalOpen(true)}
            leftIcon={<GitCompare className="w-4 h-4" />}
          >
            Compare 2 Versions
          </Button>

          <Button
            variant="outline"
            size="sm"
            onClick={() => navigate('/dashboard/upload')}
            leftIcon={<Plus className="w-4 h-4" />}
          >
            Upload New Version
          </Button>
        </div>
      </div>

      {/* Toolbar Search */}
      <Card className="p-4 flex items-center justify-between gap-4">
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-gray-400 absolute left-3 top-3" />
          <input
            type="text"
            placeholder="Search version name or file..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-4 py-2 bg-gray-50 dark:bg-dark-hover border border-gray-200 dark:border-dark-border rounded-xl text-xs sm:text-sm text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-primary-500"
          />
        </div>

        <span className="text-xs font-semibold text-gray-500">
          {filteredVersions.length} {filteredVersions.length === 1 ? 'Version' : 'Versions'} Total
        </span>
      </Card>

      {/* Version Timeline Cards Grid */}
      <div className="space-y-6">
        {filteredVersions.length === 0 ? (
          <Card className="p-12 text-center space-y-4">
            <Layers className="w-12 h-12 text-gray-400 mx-auto" />
            <h3 className="text-base font-bold text-gray-900 dark:text-white">No Resume Versions Found</h3>
            <p className="text-xs text-gray-500">Upload a resume to automatically record Version 1.</p>
          </Card>
        ) : (
          filteredVersions.map((ver) => (
            <Card
              key={ver.id}
              className={`p-6 transition-all ${
                ver.isCurrent
                  ? 'border-2 border-primary-500 bg-primary-50/20 dark:bg-primary-950/20 shadow-md'
                  : 'border-gray-200/60 dark:border-dark-border'
              }`}
            >
              <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div className="flex items-start space-x-4">
                  <div className={`w-12 h-12 rounded-2xl flex items-center justify-center font-bold text-base shadow-sm ${
                    ver.isCurrent ? 'bg-primary-600 text-white' : 'bg-gray-200 dark:bg-dark-hover text-gray-700 dark:text-gray-300'
                  }`}>
                    v{ver.versionNumber}
                  </div>

                  <div className="space-y-1">
                    <div className="flex items-center space-x-3">
                      <h3 className="text-base font-bold text-gray-900 dark:text-white">
                        {ver.versionName}
                      </h3>
                      {ver.isCurrent && (
                        <Badge variant="primary" size="sm">
                          Current Active
                        </Badge>
                      )}
                    </div>

                    <p className="text-xs text-gray-500 dark:text-gray-400 flex items-center space-x-2">
                      <Calendar className="w-3.5 h-3.5" />
                      <span>{new Date(ver.createdAt).toLocaleString()}</span>
                      <span>•</span>
                      <span>{ver.fileName}</span>
                    </p>
                  </div>
                </div>

                {/* Score Badges */}
                <div className="flex items-center space-x-3">
                  <div className="px-3 py-1.5 rounded-xl bg-primary-50 dark:bg-primary-950/40 text-center">
                    <span className="text-xs font-bold text-primary-600 dark:text-primary-400">{ver.atsScore}</span>
                    <p className="text-[9px] text-gray-400 uppercase">ATS Score</p>
                  </div>

                  <div className="px-3 py-1.5 rounded-xl bg-purple-50 dark:bg-purple-950/40 text-center">
                    <span className="text-xs font-bold text-purple-600 dark:text-purple-400">{ver.grammarScore}</span>
                    <p className="text-[9px] text-gray-400 uppercase">Grammar</p>
                  </div>

                  <div className="px-3 py-1.5 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 text-center">
                    <span className="text-xs font-bold text-emerald-600 dark:text-emerald-400">{ver.jobMatchScore}%</span>
                    <p className="text-[9px] text-gray-400 uppercase">Match</p>
                  </div>
                </div>

                {/* Action Buttons */}
                <div className="flex items-center space-x-2">
                  <Link to={`/dashboard/resumes/${ver.resumeId}`}>
                    <Button variant="ghost" size="sm" leftIcon={<Eye className="w-3.5 h-3.5" />}>
                      View
                    </Button>
                  </Link>

                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => {
                      setRenameModalVer(ver);
                      setNewVersionName(ver.versionName);
                    }}
                    leftIcon={<Edit3 className="w-3.5 h-3.5" />}
                  >
                    Rename
                  </Button>

                  {!ver.isCurrent && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleRestore(ver)}
                      leftIcon={<RotateCcw className="w-3.5 h-3.5 text-primary-500" />}
                    >
                      Restore
                    </Button>
                  )}

                  {!ver.isCurrent && (
                    <Button
                      variant="danger"
                      size="sm"
                      onClick={() => handleDelete(ver)}
                      leftIcon={<Trash2 className="w-3.5 h-3.5" />}
                    >
                      Delete
                    </Button>
                  )}
                </div>
              </div>
            </Card>
          ))
        )}
      </div>

      {/* Rename Modal */}
      {renameModalVer && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-gray-900/50 backdrop-blur-sm">
          <Card className="w-full max-w-md p-6 space-y-4">
            <h3 className="text-lg font-bold text-gray-900 dark:text-white">Rename Version {renameModalVer.versionNumber}</h3>
            <input
              type="text"
              value={newVersionName}
              onChange={(e) => setNewVersionName(e.target.value)}
              className="w-full px-4 py-2 bg-gray-50 dark:bg-dark-hover border border-gray-200 dark:border-dark-border rounded-xl text-sm text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
            <div className="flex justify-end space-x-3">
              <Button variant="outline" size="sm" onClick={() => setRenameModalVer(null)}>
                Cancel
              </Button>
              <Button variant="primary" size="sm" onClick={handleRenameSubmit}>
                Save Rename
              </Button>
            </div>
          </Card>
        </div>
      )}

      {/* Compare Modal */}
      {compareModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-gray-900/50 backdrop-blur-sm">
          <Card className="w-full max-w-lg p-6 space-y-6">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-bold text-gray-900 dark:text-white flex items-center space-x-2">
                <GitCompare className="w-5 h-5 text-primary-500" />
                <span>Select 2 Versions to Compare</span>
              </h3>
              <button onClick={() => setCompareModalOpen(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-4">
              <div>
                <label className="text-xs font-semibold text-gray-500 block mb-1">Select Old Version</label>
                <select
                  value={selectedOldId || ''}
                  onChange={(e) => setSelectedOldId(Number(e.target.value))}
                  className="w-full px-4 py-2 bg-gray-50 dark:bg-dark-hover border border-gray-200 dark:border-dark-border rounded-xl text-sm text-gray-900 dark:text-white"
                >
                  <option value="">-- Choose Old Version --</option>
                  {versions.map((v) => (
                    <option key={v.id} value={v.id}>
                      v{v.versionNumber} - {v.versionName} ({new Date(v.createdAt).toLocaleDateString()})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="text-xs font-semibold text-gray-500 block mb-1">Select New Version</label>
                <select
                  value={selectedNewId || ''}
                  onChange={(e) => setSelectedNewId(Number(e.target.value))}
                  className="w-full px-4 py-2 bg-gray-50 dark:bg-dark-hover border border-gray-200 dark:border-dark-border rounded-xl text-sm text-gray-900 dark:text-white"
                >
                  <option value="">-- Choose New Version --</option>
                  {versions.map((v) => (
                    <option key={v.id} value={v.id}>
                      v{v.versionNumber} - {v.versionName} ({new Date(v.createdAt).toLocaleDateString()})
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="flex justify-end space-x-3 pt-2">
              <Button variant="outline" size="sm" onClick={() => setCompareModalOpen(false)}>
                Cancel
              </Button>
              <Button variant="primary" size="sm" onClick={handleCompareSubmit}>
                Run Side-by-Side Comparison
              </Button>
            </div>
          </Card>
        </div>
      )}
    </motion.div>
  );
};
