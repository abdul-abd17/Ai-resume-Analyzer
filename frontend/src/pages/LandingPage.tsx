import React from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  FileCheck,
  Search,
  SpellCheck,
  Sparkles,
  Cpu,
  ArrowRight,
  CheckCircle2,
  TrendingUp,
  Shield,
  Zap,
} from 'lucide-react';
import { Button } from '../components/common/Button';
import { Card } from '../components/common/Card';
import { Badge } from '../components/common/Badge';
import { FEATURES_DATA, FeatureItem } from '../constants/navigation';
import { fadeIn, staggerContainer, cardHover } from '../animations/motionVariants';

export const LandingPage: React.FC = () => {
  const getFeatureIcon = (iconName: FeatureItem['iconName']) => {
    switch (iconName) {
      case 'fileCheck':
        return <FileCheck className="w-6 h-6 text-primary-600 dark:text-primary-400" />;
      case 'search':
        return <Search className="w-6 h-6 text-indigo-600 dark:text-indigo-400" />;
      case 'spellCheck':
        return <SpellCheck className="w-6 h-6 text-emerald-600 dark:text-emerald-400" />;
      case 'sparkles':
        return <Sparkles className="w-6 h-6 text-amber-600 dark:text-amber-400" />;
      case 'cpu':
        return <Cpu className="w-6 h-6 text-purple-600 dark:text-purple-400" />;
      default:
        return <Sparkles className="w-6 h-6 text-primary-600" />;
    }
  };

  return (
    <div className="space-y-20 pb-20">
      {/* Hero Section */}
      <section className="relative pt-12 lg:pt-20 overflow-hidden">
        {/* Glow Effects */}
        <div className="absolute top-1/4 left-1/2 -translate-x-1/2 w-96 h-96 bg-primary-500/10 dark:bg-primary-500/20 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute top-1/3 right-10 w-72 h-72 bg-purple-500/10 dark:bg-purple-500/20 rounded-full blur-3xl pointer-events-none" />

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <motion.div
            initial="hidden"
            animate="visible"
            variants={staggerContainer}
            className="text-center max-w-4xl mx-auto space-y-8"
          >
            <motion.div variants={fadeIn} className="flex justify-center">
              <Badge variant="purple" size="md" className="space-x-2 shadow-sm">
                <Sparkles className="w-4 h-4 text-purple-600 dark:text-purple-400" />
                <span>Smart ATS Resume Evaluation System</span>
              </Badge>
            </motion.div>

            <motion.h1
              variants={fadeIn}
              className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight leading-tight text-gray-900 dark:text-white"
            >
              Optimize Your Resume for{' '}
              <span className="gradient-text">ATS Algorithms</span> & Land More Interviews
            </motion.h1>

            <motion.p
              variants={fadeIn}
              className="text-lg sm:text-xl text-gray-600 dark:text-gray-300 max-w-2xl mx-auto leading-relaxed"
            >
              Eliminate rejection from automated hiring filters. Analyze resumes, calculate exact ATS compatibility scores, detect missing keywords, and receive AI-driven structural enhancements.
            </motion.p>

            <motion.div variants={fadeIn} className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4">
              <Link to="/register">
                <Button size="lg" variant="primary" rightIcon={<ArrowRight className="w-5 h-5" />}>
                  Get Started Free
                </Button>
              </Link>
              <a href="#features">
                <Button size="lg" variant="outline">
                  Learn More
                </Button>
              </a>
            </motion.div>

            {/* Feature Highlights metrics bar */}
            <motion.div
              variants={fadeIn}
              className="pt-12 grid grid-cols-2 md:grid-cols-4 gap-4 max-w-4xl mx-auto"
            >
              {[
                { label: 'Keyword Detection', value: '100% Precise' },
                { label: 'ATS Format Rating', value: 'Instant Check' },
                { label: 'Security & Privacy', value: 'Enterprise Grade' },
                { label: 'AI Enhancement', value: 'Smart Scoring' },
              ].map((stat, idx) => (
                <div
                  key={idx}
                  className="p-4 rounded-2xl bg-white/60 dark:bg-dark-card/60 border border-gray-200/60 dark:border-dark-border backdrop-blur shadow-sm"
                >
                  <p className="text-lg font-bold text-gray-900 dark:text-white">{stat.value}</p>
                  <p className="text-xs text-gray-500 dark:text-gray-400 font-medium">{stat.label}</p>
                </div>
              ))}
            </motion.div>
          </motion.div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-12">
        <div className="text-center max-w-3xl mx-auto space-y-4 mb-16">
          <Badge variant="primary" size="md">
            Features
          </Badge>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-gray-900 dark:text-white">
            Everything You Need to Beat the ATS Filter
          </h2>
          <p className="text-gray-600 dark:text-gray-300 text-base">
            Comprehensive evaluation suite designed to refine formatting, highlight key competencies, and make your application stand out.
          </p>
        </div>

        <motion.div
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, margin: '-50px' }}
          variants={staggerContainer}
          className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8"
        >
          {FEATURES_DATA.map((feature) => (
            <motion.div key={feature.id} variants={cardHover} className="h-full">
              <Card className="h-full flex flex-col justify-between hover:border-primary-500/50 transition-colors">
                <div className="space-y-4">
                  <div className="w-12 h-12 rounded-2xl bg-primary-50 dark:bg-dark-hover flex items-center justify-center">
                    {getFeatureIcon(feature.iconName)}
                  </div>
                  <div className="flex items-center justify-between">
                    <h3 className="text-xl font-bold text-gray-900 dark:text-white">
                      {feature.title}
                    </h3>
                    {feature.badge && (
                      <Badge variant="info" size="sm">
                        {feature.badge}
                      </Badge>
                    )}
                  </div>
                  <p className="text-sm text-gray-600 dark:text-gray-400 leading-relaxed">
                    {feature.description}
                  </p>
                </div>
                <div className="pt-6 border-t border-gray-100 dark:border-dark-border/60 mt-6 flex items-center text-xs font-semibold text-primary-600 dark:text-primary-400">
                  <span>Phase 1 Verified UI</span>
                  <CheckCircle2 className="w-4 h-4 ml-1.5" />
                </div>
              </Card>
            </motion.div>
          ))}
        </motion.div>
      </section>

      {/* About Section */}
      <section id="about" className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-12">
        <div className="bg-gradient-to-r from-primary-900 via-indigo-900 to-slate-900 rounded-3xl p-8 sm:p-12 lg:p-16 text-white relative overflow-hidden shadow-2xl">
          <div className="absolute right-0 top-0 w-96 h-96 bg-primary-500/10 rounded-full blur-3xl pointer-events-none" />

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center relative z-10">
            <div className="space-y-6">
              <Badge variant="purple" size="md" className="bg-white/10 text-purple-200 border-white/20">
                About the Platform
              </Badge>
              <h2 className="text-3xl sm:text-4xl font-extrabold leading-tight">
                Designed for Job Seekers, Built with Enterprise Security
              </h2>
              <p className="text-gray-300 text-sm sm:text-base leading-relaxed">
                Applicant Tracking Systems screen out up to 75% of resumes before a recruiter ever reads them. ResuMatch AI bridges the gap between your real skills and automated scanner metrics.
              </p>
              <ul className="space-y-3 pt-2">
                {[
                  'Role-based JWT Authentication with Spring Security 6',
                  'Modular architecture for future PDF/DOCX parsing',
                  'Clean Zod client & Jakarta Bean server validations',
                  'Responsive dark/light adaptive layout design',
                ].map((item, i) => (
                  <li key={i} className="flex items-center space-x-3 text-sm text-gray-200">
                    <CheckCircle2 className="w-5 h-5 text-emerald-400 flex-shrink-0" />
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
            </div>

            <div className="bg-white/10 dark:bg-dark-card/40 backdrop-blur border border-white/10 rounded-2xl p-6 space-y-4">
              <h3 className="text-lg font-bold text-white flex items-center space-x-2">
                <Shield className="w-5 h-5 text-primary-400" />
                <span>Phase 1 Architecture Compliance</span>
              </h3>
              <div className="space-y-3 text-xs text-gray-300">
                <div className="p-3 rounded-xl bg-black/20 border border-white/5">
                  <p className="font-semibold text-white">Frontend Framework</p>
                  <p className="text-gray-400">React + TypeScript + Vite + Tailwind CSS + Framer Motion</p>
                </div>
                <div className="p-3 rounded-xl bg-black/20 border border-white/5">
                  <p className="font-semibold text-white">Backend Microservice</p>
                  <p className="text-gray-400">Java 21 Spring Boot + Spring Security + JWT + PostgreSQL</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Contact / CTA Section */}
      <section id="contact" className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center pt-8">
        <Card className="max-w-4xl mx-auto p-10 bg-gradient-to-b from-white to-primary-50/50 dark:from-dark-card dark:to-dark-hover/50 border-primary-100 dark:border-dark-border">
          <div className="space-y-6">
            <Zap className="w-10 h-10 text-primary-600 dark:text-primary-400 mx-auto" />
            <h2 className="text-3xl font-extrabold text-gray-900 dark:text-white">
              Ready to Upgrade Your Job Search Strategy?
            </h2>
            <p className="text-gray-600 dark:text-gray-400 text-sm max-w-xl mx-auto">
              Register your account today to access your dashboard shell and prepare for upcoming ATS resume uploads.
            </p>
            <div className="pt-2 flex flex-col sm:flex-row justify-center gap-4">
              <Link to="/register">
                <Button size="lg" variant="primary">
                  Create Account Now
                </Button>
              </Link>
              <Link to="/login">
                <Button size="lg" variant="outline">
                  Log In
                </Button>
              </Link>
            </div>
          </div>
        </Card>
      </section>
    </div>
  );
};
