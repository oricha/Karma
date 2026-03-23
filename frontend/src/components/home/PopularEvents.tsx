import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { ChevronRight } from 'lucide-react';
import EventCard from '@/components/events/EventCard';
import { api } from '@/lib/api';

const tabs = ['thisWeek', 'today', 'tomorrow', 'weekend', 'nextWeek', 'all'] as const;

const PopularEvents = () => {
  const { t } = useTranslation('home');
  const [activeTab, setActiveTab] = useState<string>('thisWeek');
  const { data: events = [] } = useQuery({ queryKey: ['popular-events'], queryFn: api.getPopularEvents });

  return (
    <section className="section-padding">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between mb-6">
          <h2 className="font-heading text-3xl font-bold">{t('popularEvents.title')}</h2>
          <Link to="/events" className="text-primary hover:text-primary-hover font-body text-sm font-medium flex items-center gap-1">
            {t('common:buttons.seeAll')} <ChevronRight className="h-4 w-4" />
          </Link>
        </div>

        {/* Tabs */}
        <div className="flex gap-2 mb-8 overflow-x-auto pb-2 scrollbar-none">
          {tabs.map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`pill-tag whitespace-nowrap ${activeTab === tab ? 'pill-tag-active' : 'pill-tag-inactive'}`}
            >
              {t(`popularEvents.tabs.${tab}`)}
            </button>
          ))}
        </div>

        {/* Events grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {events.map(event => (
            <EventCard key={event.id} event={event} />
          ))}
        </div>
      </div>
    </section>
  );
};

export default PopularEvents;
