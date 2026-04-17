import React from 'react';

interface Props {
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

export class ErrorBoundary extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('ErrorBoundary caught error:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center p-8 bg-gradient-to-br from-background to-muted/50">
          <div className="max-w-md w-full space-y-4 text-center">
            <div className="w-24 h-24 mx-auto bg-destructive/10 rounded-full flex items-center justify-center">
              <span className="text-destructive text-3xl">!</span>
            </div>
            <h2 className="text-2xl font-bold text-foreground">Something went wrong</h2>
            <p className="text-muted-foreground">This page encountered an error. Try refreshing.</p>
            {this.props.fallback}
            <button
              onClick={() => window.location.reload()}
              className="cyber-button primary px-6 py-2"
            >
              Reload Page
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

