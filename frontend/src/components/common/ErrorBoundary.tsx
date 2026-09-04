import React, { Component, ErrorInfo, ReactNode } from 'react';
import { AlertTriangle, RefreshCw } from 'lucide-react';
import { Button } from './Button';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Uncaught error in React Error Boundary:', error, errorInfo);
  }

  public handleReload = () => {
    this.setState({ hasError: false, error: null });
    window.location.reload();
  };

  public render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center p-6 bg-gray-50 dark:bg-dark-bg">
          <div className="max-w-md w-full p-8 bg-white dark:bg-dark-card rounded-3xl shadow-xl border border-gray-200 dark:border-dark-border text-center space-y-4">
            <div className="w-12 h-12 rounded-2xl bg-red-100 dark:bg-red-950/60 text-red-600 dark:text-red-400 flex items-center justify-center mx-auto">
              <AlertTriangle className="w-6 h-6" />
            </div>
            <h2 className="text-xl font-extrabold text-gray-900 dark:text-white">Something Went Wrong</h2>
            <p className="text-xs text-gray-500 dark:text-gray-400">
              An unexpected UI error occurred. Click reload to refresh the application state cleanly.
            </p>
            <Button variant="primary" size="sm" onClick={this.handleReload} leftIcon={<RefreshCw className="w-4 h-4" />}>
              Reload Platform
            </Button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
