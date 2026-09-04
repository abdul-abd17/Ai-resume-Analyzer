import React from 'react';
import { Link } from 'react-router-dom';
import { Home, ArrowLeft } from 'lucide-react';
import { Button } from '../components/common/Button';

export const NotFoundPage: React.FC = () => {
  return (
    <div className="min-h-[70vh] flex flex-col items-center justify-center text-center px-4">
      <h1 className="text-8xl font-black gradient-text">404</h1>
      <h2 className="text-2xl font-bold text-gray-900 dark:text-white mt-4">Page Not Found</h2>
      <p className="text-sm text-gray-500 dark:text-gray-400 mt-2 max-w-md">
        The page you are looking for does not exist or has been moved to another location.
      </p>
      <div className="mt-6 flex space-x-4">
        <Link to="/">
          <Button variant="primary" leftIcon={<Home className="w-4 h-4" />}>
            Back to Home
          </Button>
        </Link>
      </div>
    </div>
  );
};
