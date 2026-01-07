import { Component, type ReactNode } from 'react';

/**
 * Error Boundary Props definition
 */
interface ErrorBoundaryProps {
  children: ReactNode;
  fallback?: ReactNode; // UI to show when error occurs (optional)
}

/**
 * Error Boundary State definition
 */
interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
}

/**
 * React Error Boundary Component
 */
export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
    };
  }

  /**
   * Lifecycle method called when error occurs
   */
  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    // Store error in state to trigger fallback UI rendering
    return {
      hasError: true,
      error,
    };
  }

  /**
   * Lifecycle method for error logging
   */
  componentDidCatch(error: Error, errorInfo: React.ErrorInfo): void {
    // In production, send to error logging service
    console.error('Error Boundary caught an error:', {
      error,
      componentStack: errorInfo.componentStack,
    });
  }

  render() {
    // If error occurred, show fallback UI
    if (this.state.hasError) {
      // Use custom fallback if provided
      if (this.props.fallback) {
        return this.props.fallback;
      }

      // Default fallback UI
      return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
          <div className="max-w-md w-full bg-white dark:bg-gray-800 shadow-lg rounded-lg p-6">
            <div className="flex items-center justify-center w-12 h-12 mx-auto bg-red-100 dark:bg-red-900 rounded-full">
              <svg
                className="w-6 h-6 text-red-600 dark:text-red-400"
                fill="none"
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </div>

            <h3 className="mt-4 text-lg font-medium text-gray-900 dark:text-white text-center">
              A temporary error has occurred
            </h3>

            <p className="mt-2 text-sm text-gray-500 dark:text-gray-400 text-center">
              Refreshing the page may resolve this issue.
            </p>

            {/* Show error details only in development mode */}
            {import.meta.env.DEV && this.state.error && (
              <details className="mt-4 p-3 bg-gray-100 dark:bg-gray-700 rounded text-xs">
                <summary className="cursor-pointer font-medium text-gray-700 dark:text-gray-300">
                  Error Details (Dev Mode)
                </summary>
                <pre className="mt-2 text-red-600 dark:text-red-400 overflow-auto">
                  {this.state.error.toString()}
                </pre>
              </details>
            )}

            <button
              onClick={() => window.location.reload()}
              className="mt-6 w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded transition-colors"
            >
              Refresh Page
            </button>
          </div>
        </div>
      );
    }

    // If no error, render children normally
    return this.props.children;
  }
}
