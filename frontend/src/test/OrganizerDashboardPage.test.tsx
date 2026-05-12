// @vitest-environment jsdom

import '@testing-library/jest-dom/vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import OrganizerDashboardPage from '@/pages/organizer/OrganizerDashboardPage';

vi.mock('@/lib/api', () => ({
  api: {
    getOrganizerDashboard: vi.fn().mockResolvedValue({
      upcomingEvents: 2,
      totalRsvps: 18,
      totalTicketsSold: 9,
      totalRevenue: 240,
      averageRating: 4.7,
      totalReviews: 6,
      recentEvents: [
        {
          id: 'event-1',
          slug: 'danza-extatica-atardecer',
          title: 'Danza Extática al Atardecer',
          startDate: '2026-04-05T18:00:00',
          city: 'Madrid',
          isFree: true,
          isOnline: false,
          isHybrid: false,
          organizerId: 'org-1',
          status: 'PUBLISHED',
          featured: true,
          language: 'es',
          reviewCount: 3,
        },
      ],
      recentActivity: [
        {
          type: 'REVIEW',
          title: 'New review',
          description: '5/5 review received',
          occurredAt: '2026-04-06T10:00:00',
          eventId: 'event-1',
          eventSlug: 'danza-extatica-atardecer',
          eventTitle: 'Danza Extática al Atardecer',
        },
      ],
    }),
  },
}));

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string, options?: { count?: number }) => {
      if (key === 'reviewsCount') {
        return `${options?.count ?? 0} total reviews`;
      }
      if (key === 'title') {
        return 'Operational overview';
      }
      if (key === 'recentEventsTitle') {
        return 'Recent events';
      }
      if (key === 'recentActivityTitle') {
        return 'Recent activity';
      }
      if (key === 'activity.types.REVIEW') {
        return 'Review';
      }
      return key;
    },
    i18n: { language: 'en' },
  }),
}));

describe('OrganizerDashboardPage', () => {
  it('renders organizer metrics and recent activity from the API', async () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    });

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <OrganizerDashboardPage />
        </MemoryRouter>
      </QueryClientProvider>,
    );

    expect(await screen.findByText('Operational overview')).toBeInTheDocument();
    expect(screen.getByText('18')).toBeInTheDocument();
    expect(screen.getByText('240.00 €')).toBeInTheDocument();
    expect(screen.getByText('4.7')).toBeInTheDocument();
    expect(screen.getAllByText('Danza Extática al Atardecer')).toHaveLength(2);
    expect(screen.getByText('5/5 review received')).toBeInTheDocument();
  });
});
