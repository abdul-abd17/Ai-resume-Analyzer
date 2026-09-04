import React from 'react';
import { motion } from 'framer-motion';
import { FileText, Lock, Sparkles } from 'lucide-react';
import { Card } from '../components/common/Card';
import { Badge } from '../components/common/Badge';

export const ResumePlaceholderPage: React.FC = () => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="space-y-6 max-w-4xl mx-auto text-center py-12"
    >
      <Card className="p-12 space-y-6 border-dashed">
        <div className="w-16 h-16 rounded-3xl bg-primary-50 dark:bg-primary-950/60 text-primary-600 dark:text-primary-400 flex items-center justify-center mx-auto shadow-sm">
          <FileText className="w-8 h-8" />
        </div>
        <div className="space-y-2">
          <Badge variant="purple" size="md">
            Phase 2 Planned Feature
          </Badge>
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
            Resume Management & Upload
          </h2>
          <p className="text-sm text-gray-500 dark:text-gray-400 max-w-md mx-auto">
            Resume uploading, PDF/DOCX text parsing, and ATS score evaluation will be activated in future steps as specified in Phase 1 guidelines.
          </p>
        </div>
      </Card>
    </motion.div>
  );
};
