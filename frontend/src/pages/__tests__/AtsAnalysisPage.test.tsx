import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { AtsAnalysisPage } from '../AtsAnalysisPage';
import { analysisService } from '../../services/analysisService';

vi.mock('../../services/analysisService', () => ({
  analysisService: {
    getAnalysis: vi.fn(),
    analyzeResume: vi.fn(),
    getAnalysisHistory: vi.fn(),
    deleteAnalysis: vi.fn(),
  },
}));

const mockReport = {
  id: 101,
  resumeId: 10,
  overallScore: 92,
  contactScore: 95,
  summaryScore: 88,
  skillsScore: 90,
  experienceScore: 92,
  educationScore: 90,
  projectsScore: 85,
  certificationScore: 80,
  keywordCoverage: 89,
  missingKeywordCount: 2,
  duplicateKeywordCount: 0,
  resumeStrength: ['Strong backend experience', 'Clear section formatting'],
  resumeWeakness: ['Missing Docker keywords'],
  suggestions: ['Add Docker certification details'],
  topSkills: [{ keyword: 'Java', frequency: 5, power: 5 }, { keyword: 'Spring Boot', frequency: 4, power: 4 }],
  missingKeywords: ['Docker'],
  duplicateKeywords: [],
  analysisDate: '2026-09-04T12:00:00',
};

describe('AtsAnalysisPage Lifecycle & State Machine Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (analysisService.getAnalysisHistory as any).mockResolvedValue([]);
  });

  const renderComponent = (resumeId = '10') => {
    return render(
      <MemoryRouter initialEntries={[`/dashboard/analysis/${resumeId}`]}>
        <Routes>
          <Route path="/dashboard/analysis/:resumeId" element={<AtsAnalysisPage />} />
        </Routes>
      </MemoryRouter>
    );
  };

  it('displays NO_ANALYSIS state and "No ATS analysis has been run yet." when opening an unanalysed resume', async () => {
    (analysisService.getAnalysis as any).mockRejectedValue({ response: { status: 404 } });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText(/No ATS Analysis Found/i)).toBeInTheDocument();
      expect(screen.getByText(/No ATS analysis has been run yet./i)).toBeInTheDocument();
    });

    expect(analysisService.getAnalysis).toHaveBeenCalledWith(10);
    expect(analysisService.analyzeResume).not.toHaveBeenCalled();
  });

  it('does NOT send a POST request on page load when no analysis exists', async () => {
    (analysisService.getAnalysis as any).mockRejectedValue({ response: { status: 404 } });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText(/No ATS Analysis Found/i)).toBeInTheDocument();
    });

    expect(analysisService.analyzeResume).toHaveBeenCalledTimes(0);
  });

  it('sends exactly one POST request when clicking "Run ATS Analysis"', async () => {
    (analysisService.getAnalysis as any).mockRejectedValue({ response: { status: 404 } });
    (analysisService.analyzeResume as any).mockResolvedValue(mockReport);

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText(/Run ATS Analysis/i)).toBeInTheDocument();
    });

    const runBtn = screen.getByText(/Run ATS Analysis/i);
    fireEvent.click(runBtn);

    await waitFor(() => {
      expect(analysisService.analyzeResume).toHaveBeenCalledTimes(1);
      expect(analysisService.analyzeResume).toHaveBeenCalledWith(10);
    });
  });

  it('renders real returned score after successful analysis', async () => {
    (analysisService.getAnalysis as any).mockResolvedValue(mockReport);

    renderComponent();

    await waitFor(() => {
      expect(screen.getAllByText('92').length).toBeGreaterThan(0);
    });

    expect(analysisService.analyzeResume).not.toHaveBeenCalled();
  });

  it('shows an error state and retry option if analysis fails', async () => {
    (analysisService.getAnalysis as any).mockRejectedValue({ response: { status: 404 } });
    (analysisService.analyzeResume as any).mockRejectedValue({
      response: { data: { message: 'Engine calculation failed' } },
    });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText(/Run ATS Analysis/i)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText(/Run ATS Analysis/i));

    await waitFor(() => {
      expect(screen.getByText(/Analysis Failed/i)).toBeInTheDocument();
      expect(screen.getByText(/Engine calculation failed/i)).toBeInTheDocument();
      expect(screen.getByText(/Retry ATS Analysis/i)).toBeInTheDocument();
    });
  });

  it('prevents repeated clicks from creating duplicate analysis requests', async () => {
    (analysisService.getAnalysis as any).mockRejectedValue({ response: { status: 404 } });
    (analysisService.analyzeResume as any).mockReturnValue(new Promise(() => {}));

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText(/Run ATS Analysis/i)).toBeInTheDocument();
    });

    const runBtn = screen.getByText(/Run ATS Analysis/i);
    fireEvent.click(runBtn);
    fireEvent.click(runBtn);
    fireEvent.click(runBtn);

    expect(analysisService.analyzeResume).toHaveBeenCalledTimes(1);
  });
});
