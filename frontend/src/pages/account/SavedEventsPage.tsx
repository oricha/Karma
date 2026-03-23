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
            {events.map(e => <EventCard key={e.id} event={e} />)}
          </div>
        </TabsContent>
        <TabsContent value="past">
          <EmptyState message={t('savedEvents.empty')} linkText={t('common:buttons.discover')} linkTo="/events" />
        </TabsContent>
      </Tabs>
    </AccountLayout>
  );
};

export default SavedEventsPage;
