import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import AccountLayout from '@/components/layout/AccountLayout';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import EventCard from '@/components/events/EventCard';
import { api } from '@/lib/api';

const MyEventsPage = () => {
  const { t } = useTranslation('account');
  const { data: events = [] } = useQuery({ queryKey: ['my-events'], queryFn: api.getMyEvents });

  return (
    <AccountLayout>
      <h1 className="font-heading text-2xl font-bold mb-6">{t('myEvents.title')}</h1>
      <Tabs defaultValue="upcoming">
        <TabsList className="bg-muted rounded-full p-1 mb-6">
          <TabsTrigger value="upcoming" className="rounded-full font-body">{t('myEvents.upcoming')}</TabsTrigger>
          <TabsTrigger value="past" className="rounded-full font-body">{t('myEvents.past')}</TabsTrigger>
        </TabsList>
        <TabsContent value="upcoming">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {events.map(e => <EventCard key={e.id} event={e} />)}
          </div>
        </TabsContent>
        <TabsContent value="past">
          <p className="font-body text-muted-foreground">{t('common:noResults')}</p>
        </TabsContent>
      </Tabs>
    </AccountLayout>
  );
};

export default MyEventsPage;
