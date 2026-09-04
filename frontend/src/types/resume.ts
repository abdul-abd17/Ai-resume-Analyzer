export interface Resume {
  id: number;
  fileName: string;
  originalFileName: string;
  fileType: string;
  fileSize: number;
  formattedFileSize: string;
  uploadedAt: string;
  status: string;
  userId: number;
  ownerName: string;
  ownerEmail: string;
}

export interface PagedResumeResponse {
  content: Resume[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}

export interface UploadProgress {
  percentage: number;
  status: 'idle' | 'uploading' | 'success' | 'error';
  errorMessage?: string;
}
