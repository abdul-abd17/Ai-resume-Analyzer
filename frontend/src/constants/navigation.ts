export interface NavLinkItem {
  name: string;
  href: string;
}

export const NAV_LINKS: NavLinkItem[] = [
  { name: 'Home', href: '/' },
  { name: 'Features', href: '/#features' },
  { name: 'About', href: '/#about' },
  { name: 'Contact', href: '/#contact' },
];

export interface FeatureItem {
  id: string;
  title: string;
  description: string;
  iconName: 'fileCheck' | 'search' | 'spellCheck' | 'sparkles' | 'cpu';
  badge?: string;
}

export const FEATURES_DATA: FeatureItem[] = [
  {
    id: 'ats-analysis',
    title: 'ATS Analysis',
    description:
      'Evaluate your resume layout and formatting compatibility against modern Applicant Tracking System algorithms.',
    iconName: 'fileCheck',
    badge: 'Core Feature',
  },
  {
    id: 'keyword-detection',
    title: 'Keyword Detection',
    description:
      'Identify crucial missing job keywords and skill gaps required for high-scoring application matching.',
    iconName: 'search',
  },
  {
    id: 'grammar-analysis',
    title: 'Grammar Analysis',
    description:
      'Detect typos, stylistic inconsistencies, passive phrasing, and grammatical errors instantly.',
    iconName: 'spellCheck',
  },
  {
    id: 'resume-suggestions',
    title: 'Resume Suggestions',
    description:
      'Receive actionable recommendations to improve bullet points, action verbs, and quantify achievements.',
    iconName: 'sparkles',
  },
  {
    id: 'ai-assistance',
    title: 'AI Assistance',
    description:
      'Smart tailroing guidance designed to optimize your resume for target job descriptions seamlessly.',
    iconName: 'cpu',
    badge: 'Smart Engine',
  },
];

export const SIDEBAR_ITEMS = [
  { name: 'Dashboard', path: '/dashboard', icon: 'LayoutDashboard' },
  { name: 'Profile', path: '/profile', icon: 'User' },
  { name: 'Resume Upload', path: '/resume', icon: 'FileText' },
  { name: 'Job Matching', path: '/dashboard/job-description', icon: 'Briefcase' },
  { name: 'ATS Reports', path: '/reports', icon: 'BarChart3' },
  { name: 'Settings', path: '/settings', icon: 'Settings' },
];

export const FOOTER_LINKS = {
  product: [
    { name: 'Features', href: '/#features' },
    { name: 'Pricing', href: '/#pricing' },
    { name: 'ATS Checker', href: '/#features' },
    { name: 'FAQ', href: '/#faq' },
  ],
  company: [
    { name: 'About Us', href: '/#about' },
    { name: 'Careers', href: '#' },
    { name: 'Blog', href: '#' },
    { name: 'Contact', href: '/#contact' },
  ],
  legal: [
    { name: 'Privacy Policy', href: '#' },
    { name: 'Terms of Service', href: '#' },
    { name: 'Security', href: '#' },
  ],
};
