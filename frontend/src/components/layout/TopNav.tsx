import React, { useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Search,
  Bell,
  User as UserIcon,
  Settings,
  LogOut,
  ChevronDown,
  Menu,
  CheckCircle,
} from 'lucide-react';
import { DarkModeToggle } from '../ui/DarkModeToggle';
import { useAuth } from '../../hooks/useAuth';

interface TopNavProps {
  onOpenMobileSidebar?: () => void;
}

export const TopNav: React.FC<TopNavProps> = ({ onOpenMobileSidebar }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [profileDropdownOpen, setProfileDropdownOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const notificationsRef = useRef<HTMLDivElement>(null);

  // Close dropdowns when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setProfileDropdownOpen(false);
      }
      if (notificationsRef.current && !notificationsRef.current.contains(event.target as Node)) {
        setNotificationsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-30 h-16 bg-white dark:bg-dark-card border-b border-gray-200 dark:border-dark-border px-4 sm:px-6 flex items-center justify-between transition-colors duration-200">
      {/* Left side: Hamburger button for mobile & Search Box */}
      <div className="flex items-center space-x-3 flex-1 max-w-lg">
        {onOpenMobileSidebar && (
          <button
            onClick={onOpenMobileSidebar}
            className="lg:hidden p-2 rounded-xl text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-dark-hover"
            aria-label="Open Sidebar"
          >
            <Menu className="w-5 h-5" />
          </button>
        )}

        {/* Search Box */}
        <div className="relative w-full">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 dark:text-gray-500" />
          <input
            type="text"
            placeholder="Search reports, resumes, keywords..."
            className="w-full bg-gray-100 dark:bg-dark-bg border-none rounded-xl pl-10 pr-4 py-2 text-xs sm:text-sm text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-primary-500 transition-all"
          />
        </div>
      </div>

      {/* Right side: Dark Mode, Notifications, Profile Dropdown */}
      <div className="flex items-center space-x-2 sm:space-x-4 ml-4">
        <DarkModeToggle />

        {/* Notifications Icon Dropdown */}
        <div className="relative" ref={notificationsRef}>
          <button
            type="button"
            onClick={() => setNotificationsOpen(!notificationsOpen)}
            className="relative p-2 rounded-xl text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-dark-hover transition-colors"
            aria-label="Notifications"
          >
            <Bell className="w-5 h-5" />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-primary-600 ring-2 ring-white dark:ring-dark-card" />
          </button>

          {notificationsOpen && (
            <div className="absolute right-0 mt-2 w-80 bg-white dark:bg-dark-card border border-gray-200 dark:border-dark-border rounded-2xl shadow-xl py-3 z-50 animate-fadeIn">
              <div className="px-4 pb-2 border-b border-gray-100 dark:border-dark-border flex items-center justify-between">
                <h4 className="text-xs font-bold text-gray-900 dark:text-white uppercase tracking-wider">
                  Notifications
                </h4>
                <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-primary-50 text-primary-600 dark:bg-primary-950 dark:text-primary-400">
                  System Ready
                </span>
              </div>
              <div className="p-3 space-y-2">
                <div className="flex items-start space-x-3 p-2 rounded-xl bg-gray-50 dark:bg-dark-hover">
                  <CheckCircle className="w-4 h-4 text-emerald-500 mt-0.5 flex-shrink-0" />
                  <div>
                    <p className="text-xs font-semibold text-gray-800 dark:text-gray-200">
                      Phase 1 Active
                    </p>
                    <p className="text-[11px] text-gray-500 dark:text-gray-400 mt-0.5">
                      Dashboard shell and authentication service are fully functional.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Profile Dropdown */}
        <div className="relative" ref={dropdownRef}>
          <button
            type="button"
            onClick={() => setProfileDropdownOpen(!profileDropdownOpen)}
            className="flex items-center space-x-2.5 p-1.5 rounded-xl hover:bg-gray-100 dark:hover:bg-dark-hover transition-colors"
          >
            <div className="w-8 h-8 rounded-full bg-gradient-to-r from-primary-600 to-indigo-600 text-white font-bold text-xs flex items-center justify-center shadow">
              {user?.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
            </div>
            <span className="hidden sm:block text-xs font-semibold text-gray-700 dark:text-gray-200 max-w-[100px] truncate">
              {user?.fullName || 'Account'}
            </span>
            <ChevronDown className="w-4 h-4 text-gray-500" />
          </button>

          {profileDropdownOpen && (
            <div className="absolute right-0 mt-2 w-56 bg-white dark:bg-dark-card border border-gray-200 dark:border-dark-border rounded-2xl shadow-xl py-2 z-50 animate-fadeIn">
              <div className="px-4 py-2 border-b border-gray-100 dark:border-dark-border">
                <p className="text-xs font-bold text-gray-900 dark:text-white truncate">
                  {user?.fullName || 'User'}
                </p>
                <p className="text-[11px] text-gray-500 dark:text-gray-400 truncate">
                  {user?.email || 'user@example.com'}
                </p>
              </div>

              <div className="py-1">
                <Link
                  to="/profile"
                  onClick={() => setProfileDropdownOpen(false)}
                  className="flex items-center space-x-2.5 px-4 py-2 text-xs font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-dark-hover"
                >
                  <UserIcon className="w-4 h-4 text-gray-500" />
                  <span>Profile</span>
                </Link>

                <Link
                  to="/settings"
                  onClick={() => setProfileDropdownOpen(false)}
                  className="flex items-center space-x-2.5 px-4 py-2 text-xs font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-dark-hover"
                >
                  <Settings className="w-4 h-4 text-gray-500" />
                  <span>Settings</span>
                </Link>
              </div>

              <div className="pt-1 border-t border-gray-100 dark:border-dark-border">
                <button
                  onClick={handleLogout}
                  className="w-full flex items-center space-x-2.5 px-4 py-2 text-xs font-medium text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-950/30"
                >
                  <LogOut className="w-4 h-4" />
                  <span>Logout</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
