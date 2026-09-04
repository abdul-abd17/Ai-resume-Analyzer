import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  Briefcase,
  Upload,
  FileText,
  Building,
  MapPin,
  Clock,
  Sparkles,
  ArrowRight,
  Search,
  Trash2,
  CheckCircle2,
  AlertCircle,
  FileCode,
  Zap,
} from 'lucide-react';
import { JobDescription } from '../types/job';
import { Resume } from '../types/resume';
import { jobService } from '../services/jobService';
import { resumeService } from '../services/resumeService';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { Badge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

export const JobDescriptionPage: React.FC = () => {
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState<'text' | 'file'>('text');
  const [jds, setJds] = useState<JobDescription[]>([]);
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [selectedResumeId, setSelectedResumeId] = useState<string>('');

  // Form State
  const [title, setTitle] = useState<string>('');
  const [companyName, setCompanyName] = useState<string>('');
  const [location, setLocation] = useState<string>('');
  const [employmentType, setEmploymentType] = useState<string>('Full-time');
  const [minimumExperience, setMinimumExperience] = useState<number>(2);
  const [educationRequirement, setEducationRequirement] = useState<string>("Bachelor's Degree");
  const [descriptionText, setDescriptionText] = useState<string>('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  const [searchQuery, setSearchQuery] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const fetchData = async () => {
    setIsLoading(true);
    try {
      const [jdList, resumePage] = await Promise.all([
        jobService.getJobDescriptions(),
        resumeService.getAllResumes(0, 50),
      ]);
      setJds(jdList);
      setResumes(resumePage.content);
      if (resumePage.content.length > 0) {
        setSelectedResumeId(String(resumePage.content[0].id));
      }
    } catch {
      // Ignore
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      const ext = file.name.split('.').pop()?.toLowerCase();
      if (ext !== 'txt' && ext !== 'pdf') {
        setError('Unsupported file format. Please select a TXT or PDF file.');
        setSelectedFile(null);
        return;
      }
      setError(null);
      setSelectedFile(file);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (activeTab === 'text' && !descriptionText.trim()) {
      setError('Please paste job description content.');
      return;
    }
    if (activeTab === 'file' && !selectedFile) {
      setError('Please select a TXT or PDF file to upload.');
      return;
    }

    setIsSubmitting(true);
    setError(null);
    setSuccessMsg(null);

    try {
      const formData = new FormData();
      formData.append('title', title || 'Target Job Role');
      formData.append('companyName', companyName || 'Target Company');
      formData.append('location', location || 'Remote');
      formData.append('employmentType', employmentType);
      formData.append('minimumExperience', String(minimumExperience));
      formData.append('educationRequirement', educationRequirement);

      if (activeTab === 'text') {
        formData.append('description', descriptionText);
      } else if (selectedFile) {
        formData.append('file', selectedFile);
      }

      const createdJd = await jobService.uploadJobDescription(formData);
      setSuccessMsg('Job description saved successfully!');

      // Reset form
      setTitle('');
      setCompanyName('');
      setDescriptionText('');
      setSelectedFile(null);

      fetchData();

      // If a resume is selected, navigate directly to comparison dashboard
      if (selectedResumeId) {
        navigate(`/dashboard/match/${selectedResumeId}/${createdJd.id}`);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save job description.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteJd = async (id: number) => {
    if (!window.confirm('Delete this job description?')) return;
    try {
      await jobService.deleteJobDescription(id);
      fetchData();
    } catch {
      alert('Failed to delete job description.');
    }
  };

  const filteredJds = jds.filter(
    (j) =>
      j.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (j.companyName && j.companyName.toLowerCase().includes(searchQuery.toLowerCase()))
  );

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="space-y-8 max-w-6xl mx-auto pb-16"
    >
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 dark:text-white flex items-center space-x-3">
            <Briefcase className="w-8 h-8 text-primary-500" />
            <span>Job Description Matching Engine</span>
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400 mt-1">
            Compare your uploaded resumes against target job descriptions using Java DSA set-intersection algorithms.
          </p>
        </div>

        <Badge variant="purple" size="md">
          Step 5 Matching Engine
        </Badge>
      </div>

      {/* Main Upload Card */}
      <Card className="p-6 sm:p-8 space-y-6">
        {/* Navigation Tabs */}
        <div className="flex border-b border-gray-200 dark:border-dark-border space-x-6">
          <button
            type="button"
            onClick={() => setActiveTab('text')}
            className={`pb-3 text-xs sm:text-sm font-semibold transition-colors flex items-center space-x-2 ${
              activeTab === 'text'
                ? 'border-b-2 border-primary-600 text-primary-600 dark:text-primary-400'
                : 'text-gray-500 hover:text-gray-700 dark:text-gray-400'
            }`}
          >
            <FileText className="w-4 h-4" />
            <span>Paste Job Text</span>
          </button>

          <button
            type="button"
            onClick={() => setActiveTab('file')}
            className={`pb-3 text-xs sm:text-sm font-semibold transition-colors flex items-center space-x-2 ${
              activeTab === 'file'
                ? 'border-b-2 border-primary-600 text-primary-600 dark:text-primary-400'
                : 'text-gray-500 hover:text-gray-700 dark:text-gray-400'
            }`}
          >
            <Upload className="w-4 h-4" />
            <span>Upload Document (TXT / PDF)</span>
          </button>
        </div>

        {error && (
          <div className="p-4 rounded-2xl bg-red-50 dark:bg-red-950/40 border border-red-500/30 text-red-900 dark:text-red-100 flex items-center space-x-3 text-xs sm:text-sm">
            <AlertCircle className="w-5 h-5 text-red-500 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {successMsg && (
          <div className="p-4 rounded-2xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-500/30 text-emerald-900 dark:text-emerald-100 flex items-center space-x-3 text-xs sm:text-sm">
            <CheckCircle2 className="w-5 h-5 text-emerald-500 flex-shrink-0" />
            <span>{successMsg}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            <Input
              label="Job Title"
              placeholder="e.g. Senior Java Spring Boot Developer"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
            />

            <Input
              label="Company Name"
              placeholder="e.g. Acme Corporation"
              value={companyName}
              onChange={(e) => setCompanyName(e.target.value)}
            />

            <Input
              label="Location"
              placeholder="e.g. San Francisco, CA / Remote"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 mb-1">
                Employment Type
              </label>
              <select
                value={employmentType}
                onChange={(e) => setEmploymentType(e.target.value)}
                className="w-full px-3 py-2 rounded-xl text-xs bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border focus:ring-2 focus:ring-primary-500 focus:outline-none"
              >
                <option value="Full-time">Full-time</option>
                <option value="Contract">Contract</option>
                <option value="Remote">Remote</option>
                <option value="Part-time">Part-time</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 mb-1">
                Minimum Experience (Years)
              </label>
              <input
                type="number"
                min="0"
                max="25"
                value={minimumExperience}
                onChange={(e) => setMinimumExperience(Number(e.target.value))}
                className="w-full px-3 py-2 rounded-xl text-xs bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border focus:ring-2 focus:ring-primary-500 focus:outline-none"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 mb-1">
                Compare Immediately With Resume
              </label>
              <select
                value={selectedResumeId}
                onChange={(e) => setSelectedResumeId(e.target.value)}
                className="w-full px-3 py-2 rounded-xl text-xs bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border focus:ring-2 focus:ring-primary-500 focus:outline-none font-semibold text-primary-600"
              >
                {resumes.length === 0 ? (
                  <option value="">No Resumes Uploaded Yet</option>
                ) : (
                  resumes.map((r) => (
                    <option key={r.id} value={r.id}>
                      {r.originalFileName}
                    </option>
                  ))
                )}
              </select>
            </div>
          </div>

          {activeTab === 'text' ? (
            <div>
              <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 mb-1">
                Job Description Text Content
              </label>
              <textarea
                rows={8}
                placeholder="Paste the target job description requirements, technical stack, responsibilities..."
                value={descriptionText}
                onChange={(e) => setDescriptionText(e.target.value)}
                className="w-full p-4 font-sans text-xs rounded-2xl bg-gray-50 dark:bg-dark-hover border border-gray-300 dark:border-dark-border focus:ring-2 focus:ring-primary-500 focus:outline-none leading-relaxed"
              />
            </div>
          ) : (
            <div className="p-8 border-2 border-dashed border-gray-300 dark:border-dark-border rounded-2xl text-center space-y-3">
              <Upload className="w-10 h-10 text-primary-500 mx-auto" />
              <div>
                <p className="text-xs font-semibold text-gray-900 dark:text-white">
                  {selectedFile ? selectedFile.name : 'Select a TXT or PDF Job Description Document'}
                </p>
                <p className="text-[11px] text-gray-400">Accepted formats: .txt, .pdf (Max 10 MB)</p>
              </div>
              <input
                type="file"
                accept=".txt,.pdf"
                onChange={handleFileChange}
                className="hidden"
                id="jd-file-picker"
              />
              <label htmlFor="jd-file-picker">
                <span className="inline-block px-4 py-2 text-xs font-semibold text-primary-600 bg-primary-50 dark:bg-primary-950/60 rounded-xl cursor-pointer hover:bg-primary-100 transition-colors">
                  Browse File
                </span>
              </label>
            </div>
          )}

          <div className="flex justify-end space-x-3 pt-2">
            <Button
              type="submit"
              variant="primary"
              size="md"
              isLoading={isSubmitting}
              leftIcon={<Zap className="w-4 h-4" />}
            >
              Save Job Description & Run Match
            </Button>
          </div>
        </form>
      </Card>

      {/* History Table of Job Descriptions */}
      <div className="space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <h2 className="text-lg font-bold text-gray-900 dark:text-white flex items-center space-x-2">
            <FileCode className="w-5 h-5 text-indigo-500" />
            <span>Saved Job Descriptions ({filteredJds.length})</span>
          </h2>

          <div className="relative w-full sm:w-64">
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="Search job titles..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-3 py-1.5 rounded-xl text-xs bg-white dark:bg-dark-card border border-gray-300 dark:border-dark-border focus:ring-2 focus:ring-primary-500 focus:outline-none"
            />
          </div>
        </div>

        {isLoading ? (
          <div className="py-12 flex justify-center">
            <LoadingSpinner size="md" label="Loading job descriptions..." />
          </div>
        ) : filteredJds.length === 0 ? (
          <Card className="p-8 text-center text-xs text-gray-500 border-dashed">
            No job descriptions saved yet. Use the form above to add one.
          </Card>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {filteredJds.map((jd) => (
              <Card
                key={jd.id}
                hoverEffect
                className="p-5 space-y-3 flex flex-col justify-between border-gray-200 dark:border-dark-border"
              >
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-gray-900 dark:text-white truncate">
                      {jd.title}
                    </span>
                    <Badge variant="primary" size="sm">
                      {jd.employmentType || 'Full-time'}
                    </Badge>
                  </div>

                  <p className="text-xs text-gray-500 dark:text-gray-400 flex items-center space-x-2">
                    <Building className="w-3.5 h-3.5" />
                    <span>{jd.companyName || 'Target Company'}</span>
                    <span>•</span>
                    <MapPin className="w-3.5 h-3.5" />
                    <span>{jd.location || 'Remote'}</span>
                  </p>

                  <p className="text-xs text-gray-600 dark:text-gray-300 line-clamp-2 pt-1 font-sans">
                    {jd.description}
                  </p>
                </div>

                <div className="pt-3 border-t border-gray-100 dark:border-dark-border flex items-center justify-between">
                  <span className="text-[11px] text-gray-400">
                    Added {new Date(jd.createdAt).toLocaleDateString()}
                  </span>

                  <div className="flex items-center space-x-2">
                    {resumes.length > 0 && (
                      <Button
                        variant="primary"
                        size="sm"
                        onClick={() =>
                          navigate(`/dashboard/match/${selectedResumeId || resumes[0].id}/${jd.id}`)
                        }
                        rightIcon={<ArrowRight className="w-3.5 h-3.5" />}
                      >
                        Compare
                      </Button>
                    )}

                    <button
                      onClick={() => handleDeleteJd(jd.id)}
                      className="p-1.5 text-gray-400 hover:text-red-500 transition-colors"
                      title="Delete Job Description"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </motion.div>
  );
};
