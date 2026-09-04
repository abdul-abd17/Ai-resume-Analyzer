import React, { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  FileText,
  Download,
  Printer,
  Eye,
  Trash2,
  Share2,
  Search,
  Filter,
  Plus,
  X,
  ZoomIn,
  ZoomOut,
  Sparkles,
  BarChart3,
  SpellCheck,
  Briefcase,
  Calendar,
  CheckCircle2,
  AlertCircle,
  FileCheck,
} from 'lucide-react';
import { Report, ReportPreview } from '../types/report';
import { reportService } from '../services/reportService';
import { resumeService } from '../services/resumeService';
import { Resume } from '../types/resume';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

export const ReportsPage: React.FC = () => {
  const [reports, setReports] = useState<Report[]>([]);
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isGenerating, setIsGenerating] = useState<boolean>(false);
  const [searchQuery, setSearchQuery] = useState<string>('');

  // Generate modal
  const [generateModalOpen, setGenerateModalOpen] = useState<boolean>(false);
  const [selectedResumeId, setSelectedResumeId] = useState<number | null>(null);

  // Preview modal
  const [previewData, setPreviewData] = useState<ReportPreview | null>(null);
  const [zoomLevel, setZoomLevel] = useState<number>(100);

  const fetchReportsData = async () => {
    setIsLoading(true);
    try {
      const [reportsData, resumesResponse] = await Promise.all([
        reportService.getReports(),
        resumeService.getAllResumes(0, 100),
      ]);
      setReports(reportsData);
      const resList = resumesResponse.content || [];
      setResumes(resList);
      if (resList.length > 0) {
        setSelectedResumeId(resList[0].id);
      }
    } catch {
      // Fallback
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchReportsData();
  }, []);

  const handleGenerateReport = async () => {
    if (!selectedResumeId) return;
    setIsGenerating(true);
    try {
      await reportService.generateReport(selectedResumeId);
      setGenerateModalOpen(false);
      fetchReportsData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to generate report.');
    } finally {
      setIsGenerating(false);
    }
  };

  const handlePreview = async (reportId: number) => {
    try {
      const preview = await reportService.getReportPreview(reportId);
      setPreviewData(preview);
    } catch {
      alert('Failed to load report preview.');
    }
  };

  const handleDownload = async (report: Report) => {
    try {
      await reportService.downloadReport(report.id, report.reportName);
      fetchReportsData();
    } catch {
      alert('Failed to download report PDF.');
    }
  };

  const handleDelete = async (reportId: number) => {
    if (!window.confirm('Delete this generated report?')) return;
    try {
      await reportService.deleteReport(reportId);
      fetchReportsData();
    } catch {
      alert('Failed to delete report.');
    }
  };

  const handlePrint = () => {
    window.print();
  };

  const filteredReports = reports.filter((r) =>
    r.reportName.toLowerCase().includes(searchQuery.toLowerCase()) ||
    (r.resumeFileName && r.resumeFileName.toLowerCase().includes(searchQuery.toLowerCase()))
  );

  if (isLoading) {
    return (
      <div className="py-20 flex justify-center">
        <LoadingSpinner size="lg" label="Loading Report History & PDF Generation Engine..." />
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
            <FileCheck className="w-7 h-7 text-primary-500" />
            <span>Professional Report Generation System</span>
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400 mt-1">
            Generate, preview, print, and export multi-page OpenPDF evaluation documents.
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          onClick={() => setGenerateModalOpen(true)}
          leftIcon={<Plus className="w-4 h-4" />}
        >
          Generate New Report
        </Button>
      </div>

      {/* Toolbar Search */}
      <Card className="p-4 flex items-center justify-between gap-4">
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-gray-400 absolute left-3 top-3" />
          <input
            type="text"
            placeholder="Search report name or resume..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-4 py-2 bg-gray-50 dark:bg-dark-hover border border-gray-200 dark:border-dark-border rounded-xl text-xs sm:text-sm text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-primary-500"
          />
        </div>

        <span className="text-xs font-semibold text-gray-500">
          {filteredReports.length} {filteredReports.length === 1 ? 'Report' : 'Reports'} Total
        </span>
      </Card>

      {/* Generated Reports Table */}
      <Card className="space-y-4">
        {filteredReports.length === 0 ? (
          <div className="py-12 text-center space-y-3">
            <FileText className="w-12 h-12 text-gray-400 mx-auto" />
            <h3 className="text-base font-bold text-gray-900 dark:text-white">No Reports Generated Yet</h3>
            <p className="text-xs text-gray-500">Click "Generate New Report" to create your first OpenPDF evaluation document.</p>
          </div>
        ) : (
          <div className="space-y-3">
            {filteredReports.map((report) => (
              <div
                key={report.id}
                className="p-5 rounded-2xl bg-gray-50 dark:bg-dark-hover border border-gray-200/60 dark:border-dark-border flex flex-col md:flex-row md:items-center justify-between gap-4"
              >
                <div className="flex items-start space-x-4">
                  <div className="w-12 h-12 rounded-2xl bg-primary-100 dark:bg-primary-950/60 text-primary-600 dark:text-primary-400 flex items-center justify-center font-bold">
                    PDF
                  </div>

                  <div className="space-y-1">
                    <h3 className="text-sm sm:text-base font-bold text-gray-900 dark:text-white">
                      {report.reportName}
                    </h3>
                    <p className="text-xs text-gray-500 dark:text-gray-400 flex items-center space-x-2">
                      <Calendar className="w-3.5 h-3.5" />
                      <span>{new Date(report.generatedDate).toLocaleString()}</span>
                      <span>•</span>
                      <span>{report.resumeFileName || 'Resume.pdf'}</span>
                      <span>•</span>
                      <span>Downloaded {report.downloadCount}x</span>
                    </p>
                  </div>
                </div>

                <div className="flex items-center space-x-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handlePreview(report.id)}
                    leftIcon={<Eye className="w-3.5 h-3.5 text-indigo-500" />}
                  >
                    Preview HTML
                  </Button>

                  <Button
                    variant="primary"
                    size="sm"
                    onClick={() => handleDownload(report)}
                    leftIcon={<Download className="w-3.5 h-3.5" />}
                  >
                    Download PDF
                  </Button>

                  <Button
                    variant="danger"
                    size="sm"
                    onClick={() => handleDelete(report.id)}
                    leftIcon={<Trash2 className="w-3.5 h-3.5" />}
                  >
                    Delete
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>

      {/* Generate Report Modal */}
      {generateModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-gray-900/50 backdrop-blur-sm">
          <Card className="w-full max-w-md p-6 space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-bold text-gray-900 dark:text-white">Generate OpenPDF Report</h3>
              <button onClick={() => setGenerateModalOpen(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div>
              <label className="text-xs font-semibold text-gray-500 block mb-1">Select Target Resume</label>
              <select
                value={selectedResumeId || ''}
                onChange={(e) => setSelectedResumeId(Number(e.target.value))}
                className="w-full px-4 py-2 bg-gray-50 dark:bg-dark-hover border border-gray-200 dark:border-dark-border rounded-xl text-sm text-gray-900 dark:text-white"
              >
                {resumes.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.originalFileName} ({new Date(r.uploadedAt).toLocaleDateString()})
                  </option>
                ))}
              </select>
            </div>

            <div className="flex justify-end space-x-3 pt-2">
              <Button variant="outline" size="sm" onClick={() => setGenerateModalOpen(false)}>
                Cancel
              </Button>
              <Button variant="primary" size="sm" isLoading={isGenerating} onClick={handleGenerateReport}>
                Generate PDF Document
              </Button>
            </div>
          </Card>
        </div>
      )}

      {/* HTML Preview Modal */}
      {previewData && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-gray-900/60 backdrop-blur-sm p-4 sm:p-6 flex justify-center items-start">
          <div className="w-full max-w-4xl bg-white dark:bg-dark-card rounded-3xl shadow-2xl border border-gray-200 dark:border-dark-border overflow-hidden my-8 space-y-6 p-6 sm:p-8">
            {/* Modal Header Bar */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b pb-4">
              <div>
                <h2 className="text-xl font-extrabold text-gray-900 dark:text-white">
                  HTML Report Preview & Print Suite
                </h2>
                <p className="text-xs text-gray-500">
                  {previewData.reportInfo.reportName} ({previewData.reportInfo.resumeFileName})
                </p>
              </div>

              <div className="flex items-center space-x-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setZoomLevel((z) => Math.max(75, z - 25))}
                  leftIcon={<ZoomOut className="w-3.5 h-3.5" />}
                >
                  Zoom Out
                </Button>
                <span className="text-xs font-bold text-gray-600 dark:text-gray-300">{zoomLevel}%</span>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setZoomLevel((z) => Math.min(175, z + 25))}
                  leftIcon={<ZoomIn className="w-3.5 h-3.5" />}
                >
                  Zoom In
                </Button>

                <Button variant="primary" size="sm" onClick={handlePrint} leftIcon={<Printer className="w-3.5 h-3.5" />}>
                  Print
                </Button>

                <button onClick={() => setPreviewData(null)} className="p-2 text-gray-400 hover:text-gray-600">
                  <X className="w-5 h-5" />
                </button>
              </div>
            </div>

            {/* Document Content View */}
            <div
              className="transition-all duration-300 font-sans"
              style={{ transform: `scale(${zoomLevel / 100})`, transformOrigin: 'top center' }}
            >
              {previewData.htmlContent ? (
                <div dangerouslySetInnerHTML={{ __html: previewData.htmlContent }} />
              ) : (
                <div className="space-y-6">
                  {/* Cover Header */}
                  <div className="p-6 rounded-2xl bg-gradient-to-r from-slate-900 to-indigo-950 text-white text-center space-y-2">
                    <h1 className="text-2xl font-black tracking-tight uppercase text-primary-400">
                      EXECUTIVE ATS RESUME EVALUATION REPORT
                    </h1>
                    <p className="text-xs text-gray-300">
                      Candidate: {previewData.candidateName} ({previewData.candidateEmail})
                    </p>
                  </div>

                  {/* Score Badges Row */}
                  <div className="grid grid-cols-3 gap-4 text-center">
                    <div className="p-4 rounded-2xl bg-primary-50 dark:bg-primary-950/40 border border-primary-200">
                      <span className="text-2xl font-black text-primary-600 dark:text-primary-400">
                        {previewData.atsScore !== null ? `${previewData.atsScore}/100` : 'Not analyzed'}
                      </span>
                      <p className="text-[10px] text-gray-400 uppercase font-bold">ATS Score</p>
                    </div>

                    <div className="p-4 rounded-2xl bg-purple-50 dark:bg-purple-950/40 border border-purple-200">
                      <span className="text-2xl font-black text-purple-600 dark:text-purple-400">
                        {previewData.grammarScore !== null ? `${previewData.grammarScore}/100` : 'Not analyzed'}
                      </span>
                      <p className="text-[10px] text-gray-400 uppercase font-bold">Grammar Index</p>
                    </div>

                    <div className="p-4 rounded-2xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200">
                      <span className="text-2xl font-black text-emerald-600 dark:text-emerald-400">
                        {previewData.jobMatchPercentage !== null ? `${previewData.jobMatchPercentage}%` : 'Not analyzed'}
                      </span>
                      <p className="text-[10px] text-gray-400 uppercase font-bold">Job Match</p>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </motion.div>
  );
};
