import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from './context/ThemeContext';
import { AuthProvider } from './context/AuthContext';
import { ResumeProvider } from './context/ResumeContext';
import { MainLayout } from './layouts/MainLayout';
import { DashboardLayout } from './layouts/DashboardLayout';
import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { DashboardPage } from './pages/DashboardPage';
import { ProfilePage } from './pages/ProfilePage';
import { ResumeUploadPage } from './pages/ResumeUploadPage';
import { ResumeDetailsPage } from './pages/ResumeDetailsPage';
import { AtsAnalysisPage } from './pages/AtsAnalysisPage';
import { GrammarAnalysisPage } from './pages/GrammarAnalysisPage';
import { AIRecommendationPage } from './pages/AIRecommendationPage';
import { ResumeHistoryPage } from './pages/ResumeHistoryPage';
import { VersionComparisonPage } from './pages/VersionComparisonPage';
import { JobDescriptionPage } from './pages/JobDescriptionPage';
import { MatchComparisonPage } from './pages/MatchComparisonPage';
import { ReportsPage } from './pages/ReportsPage';
import { AdminDashboardPage } from './pages/AdminDashboardPage';
import { RecruiterDashboardPage } from './pages/RecruiterDashboardPage';
import { JobDashboardPage } from './pages/JobDashboardPage';
import { SettingsPage } from './pages/SettingsPage';
import { NotFoundPage } from './pages/NotFoundPage';

export const App: React.FC = () => {
  return (
    <ThemeProvider>
      <AuthProvider>
        <ResumeProvider>
          <Router>
            <Routes>
              {/* Public Pages with Main Navbar and Footer */}
              <Route element={<MainLayout />}>
                <Route path="/" element={<LandingPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
              </Route>

              {/* Protected Dashboard Pages with Sidebar and TopNav */}
              <Route element={<DashboardLayout />}>
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/dashboard/upload" element={<ResumeUploadPage />} />
                <Route path="/dashboard/resumes/:id" element={<ResumeDetailsPage />} />
                <Route path="/dashboard/analysis/:resumeId" element={<AtsAnalysisPage />} />
                <Route path="/dashboard/grammar/:resumeId" element={<GrammarAnalysisPage />} />
                <Route path="/dashboard/ai/:resumeId" element={<AIRecommendationPage />} />
                <Route path="/dashboard/history" element={<ResumeHistoryPage />} />
                <Route path="/dashboard/history/compare/:oldId/:newId" element={<VersionComparisonPage />} />
                <Route path="/dashboard/job-description" element={<JobDescriptionPage />} />
                <Route path="/dashboard/match/:resumeId/:jdId" element={<MatchComparisonPage />} />
                <Route path="/profile" element={<ProfilePage />} />
                <Route path="/resume" element={<ResumeUploadPage />} />
                <Route path="/reports" element={<ReportsPage />} />
                <Route path="/admin" element={<AdminDashboardPage />} />
                <Route path="/recruiter" element={<RecruiterDashboardPage />} />
                <Route path="/dashboard/jobs" element={<JobDashboardPage />} />
                <Route path="/settings" element={<SettingsPage />} />
              </Route>

              {/* Fallback 404 Route */}
              <Route path="*" element={<NotFoundPage />} />
            </Routes>
          </Router>
        </ResumeProvider>
      </AuthProvider>
    </ThemeProvider>
  );
};

export default App;
