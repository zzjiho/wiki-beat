import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from './contexts/ThemeContext';
import { ErrorBoundary } from './components/common/ErrorBoundary';
import { Layout } from './components/layout/Layout';
import { Dashboard } from './pages/Dashboard';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30000, // Keep fresh state for 30 seconds
      retry: 2,
    },
  },
});

/**
 * App root component
 *
 * Provider nesting order:
 * 1. QueryClientProvider (Server state management - React Query)
 * 2. ErrorBoundary (Global error handling)
 * 3. ThemeProvider (Theme management)
 * 4. Layout & Dashboard (Actual UI)
 */
function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ErrorBoundary>
        <ThemeProvider>
          <Layout>
            <Dashboard />
          </Layout>
        </ThemeProvider>
      </ErrorBoundary>
    </QueryClientProvider>
  );
}

export default App;
