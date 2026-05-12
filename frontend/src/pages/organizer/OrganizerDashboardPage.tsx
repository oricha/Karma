import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { Calendar, Euro, MessageSquareText, Ticket, Users } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';
import { enUS, es } from 'date-fns/locale';
import { api } from '@/lib/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

const OrganizerDashboardPage = () => {
  const { t, i18n } = useTranslation('organizer');
  const locale = i18n.language === 'es' ? es : enUS;
  const { data } = useQuery({
    queryKey: ['organizer-dashboard'],
    queryFn: api.getOrganizerDashboard,
  });

  const metrics = [
    { key: 'upcomingEvents', icon: Calendar, value: data?.upcomingEvents ?? 0 },
    { key: 'totalRsvps', icon: Users, value: data?.totalRsvps ?? 0 },
    { key: 'totalTicketsSold', icon: Ticket, value: data?.totalTicketsSold ?? 0 },
    { key: 'totalRevenue', icon: Euro, value: `${(data?.totalRevenue ?? 0).toFixed(2)} €` },
    { key: 'averageRating', icon: MessageSquareText, value: data?.averageRating?.toFixed(1) ?? '0.0' },
  ];

  return (
    <div className="container mx-auto px-4 py-10">
      <div className="mb-8 flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="text-sm font-body uppercase tracking-[0.24em] text-muted-foreground">{t('eyebrow')}</p>
          <h1 className="font-heading text-4xl font-bold text-foreground">{t('title')}</h1>
          <p className="mt-2 max-w-2xl font-body text-muted-foreground">{t('subtitle')}</p>
        </div>
        <Badge variant="secondary" className="w-fit rounded-full px-4 py-2 text-sm font-body">
          {t('reviewsCount', { count: data?.totalReviews ?? 0 })}
        </Badge>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-5">
        {metrics.map(({ key, icon: Icon, value }) => (
          <Card key={key} className="border-border/60 shadow-sm">
            <CardHeader className="pb-2">
              <CardTitle className="flex items-center justify-between text-sm font-body text-muted-foreground">
                {t(`metrics.${key}`)}
                <Icon className="h-4 w-4 text-primary" />
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="font-heading text-3xl font-semibold">{value}</p>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="mt-8 grid grid-cols-1 gap-6 lg:grid-cols-[1.2fr_0.8fr]">
        <Card className="border-border/60 shadow-sm">
          <CardHeader>
            <CardTitle className="font-heading text-2xl">{t('recentEventsTitle')}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {data?.recentEvents.length ? data.recentEvents.map((event) => (
              <Link
                key={event.id}
                to={`/events/${event.slug}`}
                className="flex items-start justify-between rounded-2xl border border-border/60 px-4 py-4 transition-colors hover:bg-muted/40"
              >
                <div>
                  <h2 className="font-body text-base font-semibold">{event.title}</h2>
                  <p className="mt-1 text-sm text-muted-foreground">
                    {event.city} · {new Date(event.startDate).toLocaleDateString(i18n.language)}
                  </p>
                </div>
                <Badge variant="outline" className="rounded-full">
                  {event.reviewCount ?? 0}★
                </Badge>
              </Link>
            )) : (
              <p className="font-body text-sm text-muted-foreground">{t('emptyEvents')}</p>
            )}
          </CardContent>
        </Card>

        <Card className="border-border/60 shadow-sm">
          <CardHeader>
            <CardTitle className="font-heading text-2xl">{t('recentActivityTitle')}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {data?.recentActivity.length ? data.recentActivity.map((activity, index) => (
              <div key={`${activity.type}-${activity.occurredAt}-${index}`} className="rounded-2xl border border-border/60 px-4 py-4">
                <div className="mb-2 flex items-center justify-between gap-3">
                  <Badge variant={activity.type === 'REVIEW' ? 'secondary' : 'default'} className="rounded-full">
                    {t(`activity.types.${activity.type}`)}
                  </Badge>
                  <span className="text-xs text-muted-foreground">
                    {formatDistanceToNow(new Date(activity.occurredAt), { addSuffix: true, locale })}
                  </span>
                </div>
                <p className="font-body text-sm font-semibold">{activity.title}</p>
                <p className="mt-1 text-sm text-muted-foreground">{activity.description}</p>
                {activity.eventSlug && activity.eventTitle && (
                  <Link to={`/events/${activity.eventSlug}`} className="mt-3 inline-block text-sm font-medium text-primary">
                    {activity.eventTitle}
                  </Link>
                )}
              </div>
            )) : (
              <p className="font-body text-sm text-muted-foreground">{t('emptyActivity')}</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default OrganizerDashboardPage;
