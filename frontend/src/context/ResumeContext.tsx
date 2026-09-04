import React, { createContext, useState, useEffect, useCallback } from 'react';
import { Resume, PagedResumeResponse } from '../types/resume';
import { resumeService } from '../services/resumeService';

interface PaginationState {
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}

interface ResumeContextType {
  resumes: Resume[];
  pagination: PaginationState;
  selectedFile: File | null;
  uploadProgress: number;
  isUploading: boolean;
  isLoading: boolean;
  error: string | null;
  successMessage: string | null;
  searchQuery: string;
  sortBy: string;
  sortDir: 'asc' | 'desc';
  setSelectedFile: (file: File | null) => void;
  clearSelectedFile: () => void;
  uploadSelectedFile: () => Promise<void>;
  fetchResumes: (page?: number, search?: string, sortBy?: string, sortDir?: 'asc' | 'desc') => Promise<void>;
  deleteResumeById: (id: number) => Promise<void>;
  downloadResumeById: (id: number, originalFileName: string) => Promise<void>;
  setSearchQuery: (query: string) => void;
  setSorting: (field: string) => void;
  setPage: (page: number) => void;
  clearMessages: () => void;
}

export const ResumeContext = createContext<ResumeContextType | undefined>(undefined);

export const ResumeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [pagination, setPagination] = useState<PaginationState>({
    pageNumber: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 0,
    last: true,
    first: true,
  });
  const [selectedFile, setSelectedFileState] = useState<File | null>(null);
  const [uploadProgress, setUploadProgress] = useState<number>(0);
  const [isUploading, setIsUploading] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [searchQuery, setSearchQueryState] = useState<string>('');
  const [sortBy, setSortBy] = useState<string>('uploadedAt');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('desc');

  const clearMessages = () => {
    setError(null);
    setSuccessMessage(null);
  };

  const fetchResumes = useCallback(
    async (page = pagination.pageNumber, search = searchQuery, sortField = sortBy, dir = sortDir) => {
      setIsLoading(true);
      try {
        const response: PagedResumeResponse = await resumeService.getAllResumes(
          page,
          pagination.pageSize,
          search,
          sortField,
          dir
        );
        setResumes(response.content);
        setPagination({
          pageNumber: response.pageNumber,
          pageSize: response.pageSize,
          totalElements: response.totalElements,
          totalPages: response.totalPages,
          last: response.last,
          first: response.first,
        });
      } catch (err: any) {
        setError(err.response?.data?.message || 'Failed to fetch uploaded resumes.');
      } finally {
        setIsLoading(false);
      }
    },
    [pagination.pageSize, pagination.pageNumber, searchQuery, sortBy, sortDir]
  );

  useEffect(() => {
    fetchResumes(0, searchQuery, sortBy, sortDir);
  }, [searchQuery, sortBy, sortDir]);

  const setSelectedFile = (file: File | null) => {
    clearMessages();
    setUploadProgress(0);
    setSelectedFileState(file);
  };

  const clearSelectedFile = () => {
    setSelectedFileState(null);
    setUploadProgress(0);
    clearMessages();
  };

  const uploadSelectedFile = async () => {
    if (!selectedFile) {
      setError('Please select a file to upload.');
      return;
    }

    clearMessages();
    setIsUploading(true);
    setUploadProgress(10);

    try {
      await resumeService.uploadResume(selectedFile, (progressEvent) => {
        if (progressEvent.total) {
          const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          setUploadProgress(percent);
        }
      });

      setUploadProgress(100);
      setSuccessMessage(`Resume "${selectedFile.name}" uploaded successfully!`);
      setSelectedFileState(null);
      await fetchResumes(0, searchQuery, sortBy, sortDir);
    } catch (err: any) {
      const errorMsg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Failed to upload resume. Please check file type and size.';
      setError(errorMsg);
      setUploadProgress(0);
    } finally {
      setIsUploading(false);
    }
  };

  const deleteResumeById = async (id: number) => {
    clearMessages();
    try {
      await resumeService.deleteResume(id);
      setSuccessMessage('Resume deleted successfully.');
      await fetchResumes(pagination.pageNumber, searchQuery, sortBy, sortDir);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete resume.');
    }
  };

  const downloadResumeById = async (id: number, originalFileName: string) => {
    clearMessages();
    try {
      await resumeService.downloadResume(id, originalFileName);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to download resume.');
    }
  };

  const setSearchQuery = (query: string) => {
    setSearchQueryState(query);
  };

  const setSorting = (field: string) => {
    if (sortBy === field) {
      setSortDir((prev) => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(field);
      setSortDir('desc');
    }
  };

  const setPage = (page: number) => {
    fetchResumes(page, searchQuery, sortBy, sortDir);
  };

  return (
    <ResumeContext.Provider
      value={{
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
        fetchResumes,
        deleteResumeById,
        downloadResumeById,
        setSearchQuery,
        setSorting,
        setPage,
        clearMessages,
      }}
    >
      {children}
    </ResumeContext.Provider>
  );
};
