import React from 'react';
import { motion } from 'framer-motion';
import { Settings, Moon, Bell, Shield, User } from 'lucide-react';
import { Card } from '../components/common/Card';
import { DarkModeToggle } from '../components/ui/DarkModeToggle';
import { useAuth } from '../hooks/useAuth';

export const SettingsPage: React.FC = () => {
  const { user } = useAuth();

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="space-y-6 max-w-4xl mx-auto"
    >
      <div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 dark:text-white">
          Settings
        </h1>
        <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400">
          Manage your account preferences and theme settings
        </p>
      </div>

      <div className="space-y-4">
        {/* Appearance Settings */}
        <Card className="flex items-center justify-between p-6">
          <div className="flex items-center space-x-4">
            <div className="p-3 rounded-2xl bg-primary-50 dark:bg-primary-950/60 text-primary-600 dark:text-primary-400">
              <Moon className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-gray-900 dark:text-white">Theme Preference</h3>
              <p className="text-xs text-gray-500 dark:text-gray-400">
                Switch between Light and Dark interface modes
              </p>
            </div>
          </div>
          <DarkModeToggle />
        </Card>

        {/* Security Overview */}
        <Card className="flex items-center justify-between p-6">
          <div className="flex items-center space-x-4">
            <div className="p-3 rounded-2xl bg-indigo-50 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400">
              <Shield className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-gray-900 dark:text-white">Security & Tokens</h3>
              <p className="text-xs text-gray-500 dark:text-gray-400">
                JWT Authentication token active for user session: {user?.email}
              </p>
            </div>
          </div>
          <span className="text-xs font-semibold text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-950/60 px-3 py-1 rounded-full border border-emerald-200 dark:border-emerald-800">
            Encrypted
          </span>
        </Card>
      </div>
    </motion.div>
  );
};
