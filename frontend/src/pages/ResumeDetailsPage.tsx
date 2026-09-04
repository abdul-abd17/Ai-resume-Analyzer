import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  FileText,
  Download,
  Trash2,
  ArrowLeft,
  Calendar,
  HardDrive,
  FileType,
  User,
  ShieldCheck,
  Sparkles,
  Mail,
  Phone,
  MapPin,
  Linkedin,
  Github,
  Globe,
  Briefcase,
  GraduationCap,
  Award,
  Code,
  Trophy,
  ChevronDown,
  ChevronUp,
  FileCode,
  CheckCircle2,
  AlertCircle,
  Activity,
  SpellCheck,
  X,
  RefreshCw,
} from 'lucide-react';
import { Resume } from '../types/resume';
import { ParsedResume } from '../types/parser';
import { resumeService } from '../services/resumeService';
import { parserService } from '../services/parserService';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

export const ResumeDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [resume, setResume] = useState<Resume | null>(null);
  const [parsedData, setParsedData] = useState<ParsedResume | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isParsing, setIsParsing] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [parseError, setParseError] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState<boolean>(false);
  const [showRawText, setShowRawText] = useState<boolean>(false);

  useEffect(() => {
    const fetchDetailsAndParser = async () => {
      if (!id) return;
      setIsLoading(true);
      setError(null);
      try {
        const resumeId = Number(id);
        const details = await resumeService.getResumeDetails(resumeId);
        setResume(details);

        // Check if parsed data already exists
        try {
          const parsed = await parserService.getParsedResume(resumeId);
          setParsedData(parsed);
        } catch {
          // Resume has not been parsed yet; ignore error
        }
      } catch (err: any) {
        setError(err.response?.data?.message || 'Failed to load resume details.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchDetailsAndParser();
  }, [id]);

  const handleParse = async () => {
    if (!resume) return;
    setIsParsing(true);
    setParseError(null);

    try {
      const result = await parserService.parseResume(resume.id);
      setParsedData(result);
      setResume((prev) => (prev ? { ...prev, status: 'PARSED' } : null));
    } catch (err: any) {
      const errorMsg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Failed to parse resume document. The file might be corrupted or unreadable.';
      setParseError(errorMsg);
    } finally {
      setIsParsing(false);
    }
  };

  const handleDownload = async () => {
    if (!resume) return;
    try {
      await resumeService.downloadResume(resume.id, resume.originalFileName);
    } catch {
      alert('Failed to download resume.');
    }
  };

  const handleDelete = async () => {
    if (!resume) return;
    if (!window.confirm(`Are you sure you want to delete "${resume.originalFileName}"?`)) return;

    setIsDeleting(true);
    try {
      await resumeService.deleteResume(resume.id);
      navigate('/dashboard/upload');
    } catch {
      alert('Failed to delete resume.');
      setIsDeleting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="py-20 flex justify-center">
        <LoadingSpinner size="lg" label="Loading resume details & parser data..." />
      </div>
    );
  }

  if (error || !resume) {
    return (
      <div className="max-w-2xl mx-auto py-12 text-center space-y-4">
        <Card className="p-8 space-y-4">
          <p className="text-sm font-semibold text-red-500">{error || 'Resume not found'}</p>
          <Button variant="outline" size="sm" onClick={() => navigate('/dashboard/upload')}>
            Back to Upload Module
          </Button>
        </Card>
      </div>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-5xl mx-auto space-y-8 pb-16"
    >
      {/* Navigation Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <button
          onClick={() => navigate('/dashboard/upload')}
          className="flex items-center space-x-2 text-xs font-semibold text-gray-600 dark:text-gray-400 hover:text-primary-600 dark:hover:text-primary-400 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Upload Module</span>
        </button>

        <div className="flex items-center space-x-3">
          <Button
            variant="primary"
            size="sm"
            onClick={() => navigate(`/dashboard/ai/${resume.id}`)}
            leftIcon={<Sparkles className="w-4 h-4" />}
          >
            AI Suggestions
          </Button>

          <Button
            variant="outline"
            size="sm"
            onClick={() => navigate(`/dashboard/analysis/${resume.id}`)}
            leftIcon={<Activity className="w-4 h-4" />}
          >
            ATS Score
          </Button>

          <Button
            variant="outline"
            size="sm"
            onClick={() => navigate(`/dashboard/grammar/${resume.id}`)}
            leftIcon={<SpellCheck className="w-4 h-4 text-purple-500" />}
          >
            Grammar Check
          </Button>

          <Button
            variant="outline"
            size="sm"
            isLoading={isParsing}
            onClick={handleParse}
            leftIcon={<Sparkles className="w-4 h-4" />}
          >
            {parsedData ? 'Re-Parse' : 'Parse Text'}
          </Button>

          <Button
            variant="outline"
            size="sm"
            onClick={handleDownload}
            leftIcon={<Download className="w-4 h-4 text-indigo-500" />}
          >
            Download
          </Button>

          <Button
            variant="danger"
            size="sm"
            isLoading={isDeleting}
            onClick={handleDelete}
            leftIcon={<Trash2 className="w-4 h-4" />}
          >
            Delete
          </Button>
        </div>
      </div>

      {/* Parse Error Notification */}
      {parseError && (
        <div className="p-4 rounded-2xl bg-red-50 dark:bg-red-950/40 border border-red-500/30 text-red-900 dark:text-red-100 flex items-center justify-between shadow-md">
          <div className="flex items-center space-x-3">
            <AlertCircle className="w-5 h-5 text-red-500 flex-shrink-0" />
            <span className="text-xs sm:text-sm font-semibold">{parseError}</span>
          </div>
          <button onClick={() => setParseError(null)} className="text-red-500 hover:text-red-700">
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* File Header Overview Card */}
      <Card className="p-6 sm:p-8 space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-gray-100 dark:border-dark-border">
          <div className="flex items-center space-x-4">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-primary-600 to-indigo-600 text-white flex items-center justify-center shadow-md">
              <FileText className="w-7 h-7" />
            </div>
            <div>
              <h1 className="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white">
                {resume.originalFileName}
              </h1>
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                Storage Key: {resume.fileName}
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-2">
            <Badge variant={parsedData ? 'success' : 'warning'} size="md">
              {parsedData ? 'PARSED' : 'NOT PARSED'}
            </Badge>
          </div>
        </div>

        {/* File Metadata Metrics */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="p-4 rounded-2xl bg-gray-50 dark:bg-dark-hover space-y-1">
            <span className="text-[11px] font-semibold text-gray-400 uppercase tracking-wider">
              File Size
            </span>
            <p className="text-base font-bold text-gray-900 dark:text-white">
              {resume.formattedFileSize}
            </p>
          </div>

          <div className="p-4 rounded-2xl bg-gray-50 dark:bg-dark-hover space-y-1">
            <span className="text-[11px] font-semibold text-gray-400 uppercase tracking-wider">
              Format Type
            </span>
            <p className="text-base font-bold text-gray-900 dark:text-white">
              {resume.originalFileName.endsWith('.pdf') ? 'PDF Document' : 'DOCX Document'}
            </p>
          </div>

          <div className="p-4 rounded-2xl bg-gray-50 dark:bg-dark-hover space-y-1">
            <span className="text-[11px] font-semibold text-gray-400 uppercase tracking-wider">
              Upload Date
            </span>
            <p className="text-base font-bold text-gray-900 dark:text-white">
              {new Date(resume.uploadedAt).toLocaleDateString()}
            </p>
          </div>

          <div className="p-4 rounded-2xl bg-gray-50 dark:bg-dark-hover space-y-1">
            <span className="text-[11px] font-semibold text-gray-400 uppercase tracking-wider">
              Owner Email
            </span>
            <p className="text-base font-bold text-gray-900 dark:text-white truncate">
              {resume.ownerEmail}
            </p>
          </div>
        </div>
      </Card>

      {/* Structured Parsed Content */}
      {parsedData ? (
        <div className="space-y-8 animate-fadeIn">
          {/* Contact Information Banner */}
          <Card className="p-6 space-y-4 bg-gradient-to-r from-white via-primary-50/30 to-indigo-50/30 dark:from-dark-card dark:via-dark-hover dark:to-dark-card border-primary-200/60 dark:border-dark-border">
            <div className="flex items-center justify-between pb-3 border-b border-gray-200/60 dark:border-dark-border">
              <div className="flex items-center space-x-3">
                <div className="w-10 h-10 rounded-full bg-primary-600 text-white flex items-center justify-center font-extrabold text-lg shadow">
                  {parsedData.fullName ? parsedData.fullName.charAt(0).toUpperCase() : 'C'}
                </div>
                <div>
                  <h2 className="text-lg font-bold text-gray-900 dark:text-white">
                    {parsedData.fullName || 'Candidate Name'}
                  </h2>
                  <p className="text-xs text-gray-500 dark:text-gray-400">Extracted General Info</p>
                </div>
              </div>

              <Badge variant="purple" size="sm">
                Structured Profile
              </Badge>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 text-xs">
              {parsedData.email && (
                <div className="flex items-center space-x-2.5 text-gray-700 dark:text-gray-300">
                  <Mail className="w-4 h-4 text-primary-500 flex-shrink-0" />
                  <span className="truncate">{parsedData.email}</span>
                </div>
              )}

              {parsedData.phone && (
                <div className="flex items-center space-x-2.5 text-gray-700 dark:text-gray-300">
                  <Phone className="w-4 h-4 text-emerald-500 flex-shrink-0" />
                  <span>{parsedData.phone}</span>
                </div>
              )}

              {parsedData.location && (
                <div className="flex items-center space-x-2.5 text-gray-700 dark:text-gray-300">
                  <MapPin className="w-4 h-4 text-amber-500 flex-shrink-0" />
                  <span>{parsedData.location}</span>
                </div>
              )}

              {parsedData.linkedinUrl && (
                <a
                  href={parsedData.linkedinUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="flex items-center space-x-2.5 text-blue-600 dark:text-blue-400 hover:underline truncate"
                >
                  <Linkedin className="w-4 h-4 flex-shrink-0" />
                  <span className="truncate">{parsedData.linkedinUrl}</span>
                </a>
              )}

              {parsedData.githubUrl && (
                <a
                  href={parsedData.githubUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="flex items-center space-x-2.5 text-gray-800 dark:text-gray-200 hover:underline truncate"
                >
                  <Github className="w-4 h-4 flex-shrink-0" />
                  <span className="truncate">{parsedData.githubUrl}</span>
                </a>
              )}

              {parsedData.portfolioUrl && (
                <a
                  href={parsedData.portfolioUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="flex items-center space-x-2.5 text-purple-600 dark:text-purple-400 hover:underline truncate"
                >
                  <Globe className="w-4 h-4 flex-shrink-0" />
                  <span className="truncate">{parsedData.portfolioUrl}</span>
                </a>
              )}
            </div>
          </Card>

          {/* Professional Summary */}
          {parsedData.summary && (
            <Card className="space-y-3">
              <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
                <FileText className="w-5 h-5 text-primary-500" />
                <span>Professional Summary</span>
              </h3>
              <p className="text-xs sm:text-sm text-gray-700 dark:text-gray-300 leading-relaxed whitespace-pre-line">
                {parsedData.summary}
              </p>
            </Card>
          )}

          {/* Extracted Skills Badges */}
          {((parsedData.skillsList && parsedData.skillsList.length > 0) || parsedData.skills) && (
            <Card className="space-y-4">
              <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
                <Code className="w-5 h-5 text-indigo-500" />
                <span>Extracted Skills</span>
              </h3>

              {parsedData.skillsList && parsedData.skillsList.length > 0 ? (
                <div className="flex flex-wrap gap-2">
                  {parsedData.skillsList.map((skill, idx) => (
                    <Badge key={idx} variant="primary" size="md">
                      {skill}
                    </Badge>
                  ))}
                </div>
              ) : (
                <p className="text-xs sm:text-sm text-gray-700 dark:text-gray-300 whitespace-pre-line">
                  {parsedData.skills}
                </p>
              )}
            </Card>
          )}

          {/* Experience & Work History */}
          {parsedData.experience && (
            <Card className="space-y-4">
              <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
                <Briefcase className="w-5 h-5 text-emerald-500" />
                <span>Work Experience</span>
              </h3>
              <div className="p-4 rounded-xl bg-gray-50 dark:bg-dark-hover text-xs sm:text-sm text-gray-700 dark:text-gray-300 whitespace-pre-line leading-relaxed font-sans">
                {parsedData.experience}
              </div>
            </Card>
          )}

          {/* Education */}
          {parsedData.education && (
            <Card className="space-y-4">
              <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
                <GraduationCap className="w-5 h-5 text-amber-500" />
                <span>Education</span>
              </h3>
              <div className="p-4 rounded-xl bg-gray-50 dark:bg-dark-hover text-xs sm:text-sm text-gray-700 dark:text-gray-300 whitespace-pre-line leading-relaxed">
                {parsedData.education}
              </div>
            </Card>
          )}

          {/* Projects */}
          {parsedData.projects && (
            <Card className="space-y-4">
              <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
                <Code className="w-5 h-5 text-purple-500" />
                <span>Projects</span>
              </h3>
              <div className="p-4 rounded-xl bg-gray-50 dark:bg-dark-hover text-xs sm:text-sm text-gray-700 dark:text-gray-300 whitespace-pre-line leading-relaxed">
                {parsedData.projects}
              </div>
            </Card>
          )}

          {/* Certifications & Languages Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {parsedData.certifications && (
              <Card className="space-y-3">
                <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
                  <Award className="w-5 h-5 text-blue-500" />
                  <span>Certifications</span>
                </h3>
                <p className="text-xs sm:text-sm text-gray-700 dark:text-gray-300 whitespace-pre-line">
                  {parsedData.certifications}
                </p>
              </Card>
            )}

            {parsedData.languages && (
              <Card className="space-y-3">
                <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
                  <Globe className="w-5 h-5 text-emerald-500" />
                  <span>Languages</span>
                </h3>
                <p className="text-xs sm:text-sm text-gray-700 dark:text-gray-300 whitespace-pre-line">
                  {parsedData.languages}
                </p>
              </Card>
            )}
          </div>

          {/* Achievements */}
          {parsedData.achievements && (
            <Card className="space-y-3">
              <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center space-x-2">
                <Trophy className="w-5 h-5 text-amber-500" />
                <span>Achievements & Honors</span>
              </h3>
              <p className="text-xs sm:text-sm text-gray-700 dark:text-gray-300 whitespace-pre-line">
                {parsedData.achievements}
              </p>
            </Card>
          )}

          {/* Collapsible Raw Extracted Text Section */}
          <Card className="space-y-4 border-dashed">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <FileCode className="w-5 h-5 text-gray-500" />
                <h3 className="text-base font-bold text-gray-900 dark:text-white">
                  Raw Extracted Text
                </h3>
              </div>

              <Button
                variant="ghost"
                size="sm"
                onClick={() => setShowRawText(!showRawText)}
                rightIcon={showRawText ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
              >
                {showRawText ? 'Hide Raw Text' : 'View Raw Text'}
              </Button>
            </div>

            {showRawText && (
              <motion.div
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
                className="pt-2"
              >
                <textarea
                  readOnly
                  rows={14}
                  value={parsedData.rawText || 'No raw text available.'}
                  className="w-full font-mono text-xs p-4 rounded-xl bg-gray-900 text-gray-100 dark:bg-black border border-gray-700 focus:outline-none resize-none leading-relaxed"
                />
              </motion.div>
            )}
          </Card>
        </div>
      ) : (
        /* Not Parsed Callout Card */
        <Card className="p-10 text-center space-y-4 border-dashed">
          <div className="w-16 h-16 rounded-3xl bg-primary-50 dark:bg-primary-950/60 text-primary-600 dark:text-primary-400 flex items-center justify-center mx-auto shadow-sm">
            <Sparkles className="w-8 h-8 animate-pulse" />
          </div>

          <div className="space-y-2 max-w-md mx-auto">
            <h3 className="text-lg font-bold text-gray-900 dark:text-white">
              Resume Text Has Not Been Parsed Yet
            </h3>
            <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400">
              Click the <span className="font-semibold text-primary-600">Parse Resume</span> button above to extract contact information, skills, work history, and education using Apache PDFBox & POI.
            </p>
          </div>

          <div className="pt-2">
            <Button
              variant="primary"
              size="md"
              isLoading={isParsing}
              onClick={handleParse}
              leftIcon={<Sparkles className="w-4 h-4" />}
            >
              Parse Resume Now
            </Button>
          </div>
        </Card>
      )}
    </motion.div>
  );
};
