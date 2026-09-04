import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { User as UserIcon, Mail, Shield, Calendar, RefreshCw } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { Card } from '../components/common/Card';
import { Badge } from '../components/common/Badge';
import { Button } from '../components/common/Button';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

export const ProfilePage: React.FC = () => {
  const { user, refreshUser } = useAuth();
  const [isRefreshing, setIsRefreshing] = useState(false);

  const handleRefresh = async () => {
    setIsRefreshing(true);
    try {
      await refreshUser();
    } finally {
      setIsRefreshing(false);
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      className="space-y-6 max-w-4xl mx-auto"
    >
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 dark:text-white">
            User Profile
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400">
            Account details retrieved from backend GET /api/users/profile
          </p>
        </div>

        <Button
          variant="outline"
          size="sm"
          onClick={handleRefresh}
          isLoading={isRefreshing}
          leftIcon={<RefreshCw className="w-4 h-4" />}
        >
          Refresh Profile
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Profile Card Header */}
        <Card className="md:col-span-1 text-center space-y-4">
          <div className="w-24 h-24 rounded-full bg-gradient-to-tr from-primary-600 to-indigo-600 text-white font-extrabold text-3xl flex items-center justify-center mx-auto shadow-lg">
            {user?.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
          </div>
          <div>
            <h2 className="text-lg font-bold text-gray-900 dark:text-white">
              {user?.fullName || 'User'}
            </h2>
            <p className="text-xs text-gray-500 dark:text-gray-400">{user?.email}</p>
          </div>
          <div>
            <Badge variant="purple" size="md">
              {user?.role || 'ROLE_USER'}
            </Badge>
          </div>
        </Card>

        {/* Profile Details List */}
        <Card className="md:col-span-2 space-y-6">
          <h3 className="text-base font-bold text-gray-900 dark:text-white pb-3 border-b border-gray-100 dark:border-dark-border">
            Account Information
          </h3>

          <div className="space-y-4">
            <div className="flex items-center justify-between p-3.5 rounded-xl bg-gray-50 dark:bg-dark-hover">
              <div className="flex items-center space-x-3">
                <UserIcon className="w-5 h-5 text-primary-600 dark:text-primary-400" />
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">Full Name</p>
                  <p className="text-sm font-semibold text-gray-900 dark:text-white">
                    {user?.fullName || 'N/A'}
                  </p>
                </div>
              </div>
            </div>

            <div className="flex items-center justify-between p-3.5 rounded-xl bg-gray-50 dark:bg-dark-hover">
              <div className="flex items-center space-x-3">
                <Mail className="w-5 h-5 text-indigo-600 dark:text-indigo-400" />
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">Email Address</p>
                  <p className="text-sm font-semibold text-gray-900 dark:text-white">
                    {user?.email || 'N/A'}
                  </p>
                </div>
              </div>
            </div>

            <div className="flex items-center justify-between p-3.5 rounded-xl bg-gray-50 dark:bg-dark-hover">
              <div className="flex items-center space-x-3">
                <Shield className="w-5 h-5 text-emerald-600 dark:text-emerald-400" />
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">Security Role</p>
                  <p className="text-sm font-semibold text-gray-900 dark:text-white">
                    {user?.role || 'ROLE_USER'}
                  </p>
                </div>
              </div>
            </div>

            <div className="flex items-center justify-between p-3.5 rounded-xl bg-gray-50 dark:bg-dark-hover">
              <div className="flex items-center space-x-3">
                <Calendar className="w-5 h-5 text-amber-600 dark:text-amber-400" />
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">Account Created</p>
                  <p className="text-sm font-semibold text-gray-900 dark:text-white">
                    {user?.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'Active'}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </Card>
      </div>
    </motion.div>
  );
};
