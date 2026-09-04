import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Bell, X, CheckCircle2, Info, AlertTriangle, Trash2 } from 'lucide-react';
import { NotificationItem } from '../../types/dashboard';

interface NotificationDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  notifications: NotificationItem[];
  onClearAll: () => void;
}

export const NotificationDrawer: React.FC<NotificationDrawerProps> = ({
  isOpen,
  onClose,
  notifications,
  onClearAll,
}) => {
  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-50 overflow-hidden">
        {/* Backdrop */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          onClick={onClose}
          className="absolute inset-0 bg-gray-900/50 backdrop-blur-sm transition-opacity"
        />

        <div className="fixed inset-y-0 right-0 max-w-full flex pl-10">
          <motion.div
            initial={{ x: '100%' }}
            animate={{ x: 0 }}
            exit={{ x: '100%' }}
            transition={{ type: 'spring', damping: 25, stiffness: 200 }}
            className="w-screen max-w-md bg-white dark:bg-dark-card shadow-2xl border-l border-gray-200 dark:border-dark-border flex flex-col justify-between"
          >
            {/* Header */}
            <div className="p-6 border-b border-gray-200 dark:border-dark-border flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <Bell className="w-5 h-5 text-primary-500" />
                <h2 className="text-lg font-bold text-gray-900 dark:text-white">
                  Notifications Feed
                </h2>
              </div>
              <button
                onClick={onClose}
                className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* List */}
            <div className="flex-1 overflow-y-auto p-6 space-y-4">
              {notifications.length === 0 ? (
                <div className="py-12 text-center text-xs text-gray-500">
                  No notifications to display.
                </div>
              ) : (
                notifications.map((notif) => (
                  <div
                    key={notif.id}
                    className={`p-4 rounded-2xl border text-xs space-y-1 transition-all ${
                      notif.read
                        ? 'bg-gray-50/60 dark:bg-dark-hover border-gray-200/60 dark:border-dark-border opacity-75'
                        : 'bg-primary-50/40 dark:bg-primary-950/30 border-primary-200 dark:border-primary-800'
                    }`}
                  >
                    <div className="flex items-center justify-between font-bold text-gray-900 dark:text-white">
                      <span className="flex items-center space-x-2">
                        {notif.type === 'SUCCESS' && <CheckCircle2 className="w-4 h-4 text-emerald-500" />}
                        {notif.type === 'INFO' && <Info className="w-4 h-4 text-blue-500" />}
                        {notif.type === 'WARNING' && <AlertTriangle className="w-4 h-4 text-amber-500" />}
                        <span>{notif.title}</span>
                      </span>
                      <span className="text-[10px] text-gray-400 font-normal">{notif.timestamp}</span>
                    </div>
                    <p className="text-gray-600 dark:text-gray-300 leading-relaxed font-sans">
                      {notif.message}
                    </p>
                  </div>
                ))
              )}
            </div>

            {/* Footer Actions */}
            <div className="p-4 border-t border-gray-200 dark:border-dark-border flex justify-between items-center text-xs">
              <button
                onClick={onClearAll}
                className="flex items-center space-x-1.5 text-gray-500 hover:text-red-500 transition-colors"
              >
                <Trash2 className="w-4 h-4" />
                <span>Clear All Feed</span>
              </button>

              <button
                onClick={onClose}
                className="px-4 py-2 bg-primary-600 text-white font-semibold rounded-xl hover:bg-primary-700 transition-colors"
              >
                Done
              </button>
            </div>
          </motion.div>
        </div>
      </div>
    </AnimatePresence>
  );
};
