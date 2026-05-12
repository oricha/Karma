import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import AccountLayout from '@/components/layout/AccountLayout';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import EventCard from '@/components/events/EventCard';
import EmptyState from '@/components/common/EmptyState';
import { api } from '@/lib/api';

const SavedEventsPage = () => {
  const { t } = useTranslation('account');
  const { data: events = [] } = useQuery({ queryKey: ['saved-events'], queryFn: api.getSavedEvents });
  const now = new Date();
  const upcomingEvents = events.filter((event) => new Date(event.startDate) >= now);
  const pastEvents = events.filter((event) => new Date(event.startDate) < now);

  return (
    <AccountLayout>
      <h1 className="font-heading text-2xl font-bold mb-6">{t('savedEvents.title')}</h1>
      <Tabs defaultValue="upcoming">
        <TabsList className="bg-muted rounded-full p-1 mb-6">
          <TabsTrigger value="upcoming" className="rounded-full font-body">{t('common:upcoming')}</TabsTrigger>
          <TabsTrigger value="past" className="rounded-full font-body">{t('common:past')}</TabsTrigger>
        </TabsList>
        <TabsContent value="upcoming">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {upcomingEvents.length
              ? upcomingEvents.map((event) => <EventCard key={event.id} event={event} />)
              : <EmptyState message={t('savedEvents.empty')} linkText={t('common:buttons.discover')} linkTo="/events" />}
          </div>
        </TabsContent>
        <TabsContent value="past">
          {pastEvents.length
            ? <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">{pastEvents.map((event) => <EventCard key={event.id} event={event} />)}</div>
            : <EmptyState message={t('savedEvents.empty')} linkText={t('common:buttons.discover')} linkTo="/events" />}
        </TabsContent>
      </Tabs>
    </AccountLayout>
  );
};

export default SavedEventsPage;
