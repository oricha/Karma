import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useSession } from '@/hooks/use-session';

const ProtectedRoute = () => {
  const location = useLocation();
  const { isLoggedIn, isLoading } = useSession();

  if (isLoading) {
    return <div className="container mx-auto px-4 py-16 text-sm text-muted-foreground">Loading session...</div>;
  }

  if (!isLoggedIn) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
};

export default ProtectedRoute;
