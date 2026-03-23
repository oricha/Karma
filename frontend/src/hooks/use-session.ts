import { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/auth';

export const useSession = () => {
  const user = useAuthStore((state) => state.user);
  const accessToken = useAuthStore((state) => state.accessToken);
  const setSession = useAuthStore((state) => state.setSession);
  const clearSession = useAuthStore((state) => state.clearSession);

  const query = useQuery({
    queryKey: ['current-user'],
    queryFn: api.getCurrentUser,
    enabled: Boolean(accessToken),
    retry: false,
  });

  useEffect(() => {
    if (query.data && (!user || user.id !== query.data.id)) {
      const refreshToken = useAuthStore.getState().refreshToken;
      if (refreshToken && accessToken) {
        setSession({ accessToken, refreshToken, user: query.data });
      }
    }
  }, [accessToken, query.data, setSession, user]);

  useEffect(() => {
    if (query.isError && accessToken) {
      clearSession();
    }
  }, [accessToken, clearSession, query.isError]);

  return {
    user: query.data ?? user,
    isLoggedIn: Boolean(accessToken),
    isLoading: query.isLoading,
  };
};
