import { useAuthStore } from '@/lib/auth';
import type { BlogPost, Category, Event, Group, Order, RSVP, User } from '@/types';

const API_URL = import.meta.env.VITE_API_URL ?? '';

export interface UserPreferences {
  newsletterFrequency: 'WEEKLY' | 'BIWEEKLY' | 'MONTHLY' | 'KARMA_ONLY' | 'NEVER';
  reviewReminders: boolean;
  preferredLocation: string;
  latitude: number;
  longitude: number;
  locationRadiusKm: number;
  themeIds: string[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
  emailVerificationToken?: string | null;
}

interface ActionResponse {
  message: string;
  token?: string | null;
}

async function request<T>(path: string, init: RequestInit = {}, allowRetry = true): Promise<T> {
  const token = localStorage.getItem('karma.accessToken');
  const headers = new Headers(init.headers);
  if (!(init.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_URL}${path}`, { ...init, headers });
  if (response.status === 401 && allowRetry && path !== '/api/auth/refresh') {
    const refreshToken = localStorage.getItem('karma.refreshToken');
    if (refreshToken) {
      try {
        const refreshed = await request<AuthResponse>(
          '/api/auth/refresh',
          { method: 'POST', body: JSON.stringify({ refreshToken }) },
          false,
        );
        useAuthStore.getState().setSession({
          accessToken: refreshed.accessToken,
          refreshToken: refreshed.refreshToken,
          user: refreshed.user,
        });
        return request<T>(path, init, false);
      } catch {
        useAuthStore.getState().clearSession();
      }
    }
  }
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Request failed' }));
    throw new Error(error.message ?? 'Request failed');
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}

export const api = {
  login: (payload: { email: string; password: string }) =>
    request<AuthResponse>('/api/auth/login', { method: 'POST', body: JSON.stringify(payload) }),
  register: (payload: { email: string; password: string; firstName: string; lastName: string }) =>
    request<AuthResponse>('/api/auth/register', { method: 'POST', body: JSON.stringify(payload) }),
  refresh: (refreshToken: string) =>
    request<AuthResponse>('/api/auth/refresh', { method: 'POST', body: JSON.stringify({ refreshToken }) }),
  forgotPassword: (payload: { email: string }) =>
    request<ActionResponse>('/api/auth/forgot-password', { method: 'POST', body: JSON.stringify(payload) }),
  resetPassword: (payload: { token: string; password: string }) =>
    request<ActionResponse>('/api/auth/reset-password', { method: 'POST', body: JSON.stringify(payload) }),
  verifyEmail: (token: string) =>
    request<{ message: string; user: User }>(`/api/auth/verify-email?token=${encodeURIComponent(token)}`),

  getCurrentUser: () => request<User>('/api/users/me'),
  updateCurrentUser: (payload: Partial<User> & { email: string; firstName: string; lastName: string }) =>
    request<User>('/api/users/me', { method: 'PUT', body: JSON.stringify(payload) }),

  getPreferences: () => request<UserPreferences>('/api/users/me/preferences'),
  updatePreferences: (payload: UserPreferences) =>
    request<UserPreferences>('/api/users/me/preferences', { method: 'PUT', body: JSON.stringify(payload) }),

  getSavedEvents: () => request<Event[]>('/api/users/me/saved-events'),
  saveEvent: (eventId: string) => request<void>(`/api/users/me/saved-events/${eventId}`, { method: 'POST' }),
  unsaveEvent: (eventId: string) => request<void>(`/api/users/me/saved-events/${eventId}`, { method: 'DELETE' }),
  getOrders: () => request<Order[]>('/api/users/me/orders'),
  getMyGroups: () => request<Group[]>('/api/users/me/groups'),
  getMyEvents: () => request<Event[]>('/api/users/me/events'),
  updateGroupNotification: (groupId: string, preference: 'IMMEDIATE' | 'DIGEST' | 'NEVER') =>
    request<void>(`/api/groups/${groupId}/notifications`, { method: 'PUT', body: JSON.stringify({ preference }) }),

  getCategories: () => request<Category[]>('/api/categories'),
  getCategoryDetails: (slug: string) =>
    request<{ category: Category; themes: Array<{ id: string; categoryId: string; nameEs: string; nameEn: string; slug: string }> }>(`/api/categories/${slug}`),
  getEvents: (params?: URLSearchParams) => request<Event[]>(`/api/events${params ? `?${params.toString()}` : ''}`),
  getPopularEvents: () => request<Event[]>('/api/events/popular'),
  getEvent: async (slug: string) => {
    const response = await request<{ event: Event; relatedEvents: Event[] }>(`/api/events/${slug}`);
    return response;
  },
  getEventRsvp: (id: string) => request<RSVP | null>(`/api/events/${id}/rsvp`),
  rsvpEvent: (id: string) => request<RSVP>(`/api/events/${id}/rsvp`, { method: 'POST' }),
  cancelRsvp: (id: string) => request<void>(`/api/events/${id}/rsvp`, { method: 'DELETE' }),
  checkout: (eventId: string) =>
    request<{ checkoutUrl: string; order: Order }>('/api/orders/checkout', { method: 'POST', body: JSON.stringify({ eventId }) }),

  getGroups: () => request<Group[]>('/api/groups'),
  getGroup: async (slug: string) => {
    const response = await request<{ group: Group; upcomingEvents: Event[]; members: User[] }>(`/api/groups/${slug}`);
    return response;
  },
  joinGroup: (id: string) => request<void>(`/api/groups/${id}/join`, { method: 'POST' }),
  leaveGroup: (id: string) => request<void>(`/api/groups/${id}/leave`, { method: 'DELETE' }),

  getBlogPosts: () => request<BlogPost[]>('/api/blog'),
  getFeaturedBlogPosts: () => request<BlogPost[]>('/api/blog/featured'),
  getOrganizerDashboard: () =>
    request<{ upcomingEvents: number; totalRsvps: number; totalTicketsSold: number; totalRevenue: number; recentEvents: Event[] }>('/api/organizers/me/dashboard'),
};
