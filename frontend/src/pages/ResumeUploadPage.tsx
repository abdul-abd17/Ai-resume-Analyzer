import React, { useState, useRef, ChangeEvent, DragEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  UploadCloud,
  FileText,
  FileCode2,
  CheckCircle2,
  AlertCircle,
  X,
  Trash2,
  Download,
  Eye,
  Search,
  ArrowUpDown,
  ChevronLeft,
  ChevronRight,
  FileCheck,
  RefreshCw,
} from 'lucide-react';
import { useResume } from '../hooks/useResume';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { NotificationToast, ToastMessage } from '../components/ui/NotificationToast';

export const ResumeUploadPage: React.FC = () => {
  const {
    resumes,
    pagination,
    selectedFile,
    uploadProgress,
    isUploading,
    isLoading,
    error,
    successMessage,
    searchQuery,
    sortBy,
    sortDir,
    setSelectedFile,
    clearSelectedFile,
    uploadSelectedFile,
    deleteResumeById,
    downloadResumeById,
    setSearchQuery,
    setSorting,
    setPage,
    clearMessages,
  } = useResume();

  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isDragOver, setIsDragOver] = useState(false);
  const [deleteConfirmId, setDeleteConfirmId] = useState<number | null>(null);

  // Drag & Drop Handlers
  const handleDragOver = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(true);
  };

  const handleDragLeave = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
  };

  const validateAndSetFile = (file: File) => {
    clearMessages();
    const maxBytes = 10 * 1024 * 1024;
    const extension = file.name.split('.').pop()?.toLowerCase();

    if (!extension || !['pdf', 'docx'].includes(extension)) {
      alert('Invalid file format. Please upload only PDF or DOCX documents.');
      return;
    }

    if (file.size > maxBytes) {
      alert('File size exceeds the 10 MB limit. Please select a smaller file.');
      return;
    }

    setSelectedFile(file);
  };

  const handleDrop = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);

    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      const file = e.dataTransfer.files[0];
      validateAndSetFile(file);
    }
  };

  const handleFileInputChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      validateAndSetFile(e.target.files[0]);
    }
  };

  const triggerFileSelect = () => {
    fileInputRef.current?.click();
  };

  const formatBytes = (bytes: number): string => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  };

  const getFileIcon = (fileTypeOrName: string) => {
    const lower = fileTypeOrName.toLowerCase();
    if (lower.includes('pdf')) {
      return <FileText className="w-8 h-8 text-red-500 flex-shrink-0" />;
    }
    return <FileCode2 className="w-8 h-8 text-blue-500 flex-shrink-0" />;
  };

  return (
    <div className="space-y-8 max-w-6xl mx-auto pb-12">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 dark:text-white">
            Resume Upload Module
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400 mt-1">
            Upload PDF or DOCX resumes to store in your ATS evaluation account.
          </p>
        </div>

        <Badge variant="purple" size="md" className="self-start md:self-auto shadow-sm">
          Step 2 Storage Active
        </Badge>
      </div>

      {/* Success / Error Banners */}
      <AnimatePresence>
        {successMessage && (
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            className="p-4 rounded-2xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-500/30 text-emerald-900 dark:text-emerald-100 flex items-center justify-between shadow-md"
          >
            <div className="flex items-center space-x-3">
              <CheckCircle2 className="w-5 h-5 text-emerald-500 flex-shrink-0" />
              <span className="text-xs sm:text-sm font-semibold">{successMessage}</span>
            </div>
            <button onClick={clearMessages} className="text-emerald-500 hover:text-emerald-700">
              <X className="w-4 h-4" />
            </button>
          </motion.div>
        )}

        {error && (
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            className="p-4 rounded-2xl bg-red-50 dark:bg-red-950/40 border border-red-500/30 text-red-900 dark:text-red-100 flex items-center justify-between shadow-md"
          >
            <div className="flex items-center space-x-3">
              <AlertCircle className="w-5 h-5 text-red-500 flex-shrink-0" />
              <span className="text-xs sm:text-sm font-semibold">{error}</span>
            </div>
            <button onClick={clearMessages} className="text-red-500 hover:text-red-700">
              <X className="w-4 h-4" />
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Upload Drag & Drop Area */}
      <Card className="p-6 sm:p-10 space-y-6">
        <input
          ref={fileInputRef}
          type="file"
          accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
          className="hidden"
          onChange={handleFileInputChange}
        />

        {!selectedFile ? (
          <motion.div
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            whileHover={{ scale: 1.005 }}
            className={`border-2 border-dashed rounded-3xl p-8 sm:p-12 text-center transition-all cursor-pointer ${
              isDragOver
                ? 'border-primary-500 bg-primary-50/50 dark:bg-primary-950/30 ring-4 ring-primary-500/20'
                : 'border-gray-300 dark:border-dark-border bg-gray-50/50 dark:bg-dark-hover/30 hover:border-primary-400 hover:bg-gray-100/50 dark:hover:bg-dark-hover/60'
            }`}
            onClick={triggerFileSelect}
          >
            <div className="w-16 h-16 rounded-3xl bg-primary-100 dark:bg-primary-950 text-primary-600 dark:text-primary-400 flex items-center justify-center mx-auto mb-4 shadow-sm">
              <UploadCloud className="w-8 h-8 animate-bounce" />
            </div>

            <h3 className="text-lg font-bold text-gray-900 dark:text-white">
              Drag & Drop your resume here
            </h3>
            <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400 mt-1 max-w-sm mx-auto">
              Supports <span className="font-semibold text-gray-700 dark:text-gray-200">PDF</span> and{' '}
              <span className="font-semibold text-gray-700 dark:text-gray-200">DOCX</span> files up to{' '}
              <span className="font-semibold text-gray-700 dark:text-gray-200">10 MB</span>
            </p>

            <div className="mt-6">
              <Button type="button" variant="outline" size="sm">
                Browse Files
              </Button>
            </div>
          </motion.div>
        ) : (
          /* Selected File Preview Card & Progress */
          <div className="space-y-6">
            <div className="flex items-center justify-between p-4 sm:p-5 rounded-2xl bg-gray-50 dark:bg-dark-hover border border-gray-200/80 dark:border-dark-border">
              <div className="flex items-center space-x-4 min-w-0">
                {getFileIcon(selectedFile.name)}
                <div className="min-w-0">
                  <p className="text-sm font-bold text-gray-900 dark:text-white truncate">
                    {selectedFile.name}
                  </p>
                  <p className="text-xs text-gray-500 dark:text-gray-400">
                    {formatBytes(selectedFile.size)} • {selectedFile.type || 'Document'}
                  </p>
                </div>
              </div>

              {!isUploading && (
                <div className="flex items-center space-x-2 flex-shrink-0">
                  <Button type="button" variant="ghost" size="sm" onClick={triggerFileSelect}>
                    Replace
                  </Button>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={clearSelectedFile}
                    className="text-red-500 hover:text-red-600"
                  >
                    <X className="w-4 h-4" />
                  </Button>
                </div>
              )}
            </div>

            {/* Upload Progress Bar */}
            {isUploading && (
              <div className="space-y-2">
                <div className="flex justify-between text-xs font-semibold">
                  <span className="text-primary-600 dark:text-primary-400">Uploading File...</span>
                  <span className="text-gray-600 dark:text-gray-300">{uploadProgress}%</span>
                </div>
                <div className="w-full bg-gray-200 dark:bg-dark-border rounded-full h-3 overflow-hidden">
                  <div
                    className="bg-gradient-to-r from-primary-600 to-indigo-600 h-3 rounded-full transition-all duration-300"
                    style={{ width: `${uploadProgress}%` }}
                  />
                </div>
              </div>
            )}

            {/* Action Buttons */}
            <div className="flex items-center justify-end space-x-3 pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={clearSelectedFile}
                disabled={isUploading}
              >
                Cancel
              </Button>

              <Button
                type="button"
                variant="primary"
                isLoading={isUploading}
                onClick={uploadSelectedFile}
                leftIcon={<UploadCloud className="w-4 h-4" />}
              >
                Upload Resume
              </Button>
            </div>
          </div>
        )}
      </Card>

      {/* Upload History Table */}
      <Card className="space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-gray-100 dark:border-dark-border">
          <div>
            <h3 className="text-lg font-bold text-gray-900 dark:text-white">
              Uploaded Resumes ({pagination.totalElements})
            </h3>
            <p className="text-xs text-gray-500 dark:text-gray-400">
              Manage your saved resume files for future ATS scoring
            </p>
          </div>

          {/* Search Box & Sort Controls */}
          <div className="flex items-center space-x-3">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search by filename..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9 pr-4 py-2 text-xs rounded-xl border border-gray-300 dark:border-dark-border bg-white dark:bg-dark-bg text-gray-900 dark:text-gray-100 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-primary-500"
              />
            </div>

            <Button
              variant="ghost"
              size="sm"
              onClick={() => setSorting('uploadedAt')}
              leftIcon={<ArrowUpDown className="w-3.5 h-3.5" />}
              title="Toggle Upload Date Sort"
            >
              <span className="hidden sm:inline">Sort</span> ({sortDir.toUpperCase()})
            </Button>
          </div>
        </div>

        {/* Table Container */}
        {isLoading ? (
          <div className="py-12 flex justify-center">
            <LoadingSpinner size="md" label="Loading uploaded resumes..." />
          </div>
        ) : resumes.length === 0 ? (
          <div className="text-center py-12 space-y-3">
            <FileText className="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto" />
            <h4 className="text-sm font-semibold text-gray-700 dark:text-gray-300">
              No resumes found
            </h4>
            <p className="text-xs text-gray-500 dark:text-gray-400 max-w-sm mx-auto">
              {searchQuery
                ? `No resumes matched "${searchQuery}". Try a different search term.`
                : 'Upload your first resume using the drag & drop uploader above.'}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-gray-200 dark:border-dark-border text-[11px] font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  <th className="py-3 px-4">File Name</th>
                  <th className="py-3 px-4">Type</th>
                  <th className="py-3 px-4">Size</th>
                  <th className="py-3 px-4">Upload Date</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-dark-border text-xs sm:text-sm">
                {resumes.map((resume) => (
                  <tr
                    key={resume.id}
                    className="hover:bg-gray-50/80 dark:hover:bg-dark-hover/50 transition-colors"
                  >
                    {/* File Name */}
                    <td className="py-4 px-4 font-semibold text-gray-900 dark:text-white max-w-[220px] truncate">
                      <div className="flex items-center space-x-2.5 truncate">
                        {getFileIcon(resume.originalFileName)}
                        <span className="truncate" title={resume.originalFileName}>
                          {resume.originalFileName}
                        </span>
                      </div>
                    </td>

                    {/* Type */}
                    <td className="py-4 px-4">
                      <Badge
                        variant={resume.originalFileName.endsWith('.pdf') ? 'primary' : 'info'}
                        size="sm"
                      >
                        {resume.originalFileName.endsWith('.pdf') ? 'PDF' : 'DOCX'}
                      </Badge>
                    </td>

                    {/* Size */}
                    <td className="py-4 px-4 text-gray-600 dark:text-gray-400 font-medium">
                      {resume.formattedFileSize}
                    </td>

                    {/* Upload Date */}
                    <td className="py-4 px-4 text-gray-600 dark:text-gray-400">
                      {new Date(resume.uploadedAt).toLocaleDateString()} •{' '}
                      {new Date(resume.uploadedAt).toLocaleTimeString([], {
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </td>

                    {/* Status */}
                    <td className="py-4 px-4">
                      <Badge variant="success" size="sm">
                        {resume.status}
                      </Badge>
                    </td>

                    {/* Actions */}
                    <td className="py-4 px-4 text-right">
                      <div className="flex items-center justify-end space-x-2">
                        <button
                          onClick={() => navigate(`/dashboard/resumes/${resume.id}`)}
                          className="p-1.5 rounded-lg text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-dark-hover hover:text-primary-600 transition-colors"
                          title="View Details"
                        >
                          <Eye className="w-4 h-4" />
                        </button>

                        <button
                          onClick={() => downloadResumeById(resume.id, resume.originalFileName)}
                          className="p-1.5 rounded-lg text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-dark-hover hover:text-indigo-600 transition-colors"
                          title="Download Resume"
                        >
                          <Download className="w-4 h-4" />
                        </button>

                        <button
                          onClick={() => setDeleteConfirmId(resume.id)}
                          className="p-1.5 rounded-lg text-gray-600 dark:text-gray-400 hover:bg-red-50 dark:hover:bg-red-950/40 hover:text-red-600 transition-colors"
                          title="Delete Resume"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination Bar */}
        {pagination.totalPages > 1 && (
          <div className="flex items-center justify-between pt-4 border-t border-gray-100 dark:border-dark-border text-xs text-gray-600 dark:text-gray-400">
            <div>
              Showing page <span className="font-bold text-gray-900 dark:text-white">{pagination.pageNumber + 1}</span> of{' '}
              <span className="font-bold text-gray-900 dark:text-white">{pagination.totalPages}</span>
            </div>

            <div className="flex items-center space-x-2">
              <Button
                variant="outline"
                size="sm"
                disabled={pagination.first}
                onClick={() => setPage(pagination.pageNumber - 1)}
                leftIcon={<ChevronLeft className="w-4 h-4" />}
              >
                Previous
              </Button>

              <Button
                variant="outline"
                size="sm"
                disabled={pagination.last}
                onClick={() => setPage(pagination.pageNumber + 1)}
                rightIcon={<ChevronRight className="w-4 h-4" />}
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </Card>

      {/* Delete Confirmation Modal */}
      {deleteConfirmId && (
        <div className="fixed inset-0 z-50 bg-gray-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <motion.div
            initial={{ scale: 0.95, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            className="bg-white dark:bg-dark-card border border-gray-200 dark:border-dark-border rounded-3xl p-6 max-w-md w-full shadow-2xl space-y-4"
          >
            <div className="flex items-center space-x-3 text-red-600">
              <AlertCircle className="w-6 h-6" />
              <h3 className="text-lg font-bold text-gray-900 dark:text-white">Delete Resume</h3>
            </div>
            <p className="text-xs sm:text-sm text-gray-600 dark:text-gray-400 leading-relaxed">
              Are you sure you want to delete this resume? The file will be permanently removed from disk storage.
            </p>
            <div className="flex justify-end space-x-3 pt-2">
              <Button variant="outline" size="sm" onClick={() => setDeleteConfirmId(null)}>
                Cancel
              </Button>
              <Button
                variant="danger"
                size="sm"
                onClick={async () => {
                  const id = deleteConfirmId;
                  setDeleteConfirmId(null);
                  await deleteResumeById(id);
                }}
              >
                Confirm Delete
              </Button>
            </div>
          </motion.div>
        </div>
      )}
    </div>
  );
};
