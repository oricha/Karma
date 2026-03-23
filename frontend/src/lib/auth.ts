import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User } from '@/types';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: User | null;
  setSession: (payload: { accessToken: string; refreshToken: string; user: User }) => void;
  clearSession: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      setSession: ({ accessToken, refreshToken, user }) => {
        localStorage.setItem('karma.accessToken', accessToken);
        localStorage.setItem('karma.refreshToken', refreshToken);
        set({ accessToken, refreshToken, user });
      },
      clearSession: () => {
        localStorage.removeItem('karma.accessToken');
        localStorage.removeItem('karma.refreshToken');
        set({ accessToken: null, refreshToken: null, user: null });
      },
    }),
    { name: 'karma-auth' },
  ),
);
