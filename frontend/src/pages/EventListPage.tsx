import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';
import { useQuery } from '@tanstack/react-query';
import { Search, SlidersHorizontal } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import EventCard from '@/components/events/EventCard';
import ThemePill from '@/components/categories/ThemePill';
import { api } from '@/lib/api';

const EventListPage = () => {
  const { t, i18n } = useTranslation('events');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const { data: categories = [] } = useQuery({ queryKey: ['categories'], queryFn: api.getCategories });
  const { data: events = [] } = useQuery({
    queryKey: ['events', selectedCategory],
    queryFn: () => api.getEvents(selectedCategory === 'all' || selectedCategory === 'free'
      ? undefined
      : new URLSearchParams({ category: selectedCategory })),
  });
  const filteredEvents = selectedCategory === 'free' ? events.filter((event) => event.isFree) : events;

  return (
    <>
      <Helmet><title>{t('title')} — Karma</title></Helmet>
      <div className="container mx-auto px-4 py-8">
        <h1 className="font-heading text-4xl font-bold mb-6">{t('title')}</h1>

        {/* Filters */}
        <div className="flex flex-wrap gap-3 mb-6">
          <div className="relative flex-1 min-w-[200px] max-w-md">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input placeholder={t('common:buttons.search')} className="pl-10 rounded-full font-body" />
          </div>
          <Select>
            <SelectTrigger className="w-[180px] rounded-full font-body">
              <SelectValue placeholder={t('filters.allCategories')} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">{t('filters.allCategories')}</SelectItem>
              {categories.map(c => (
                <SelectItem key={c.id} value={c.slug}>
                  {i18n.language === 'es' ? c.nameEs : c.nameEn}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select>
            <SelectTrigger className="w-[160px] rounded-full font-body">
              <SelectValue placeholder={t('filters.sortBy')} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="date">{t('filters.date_asc')}</SelectItem>
              <SelectItem value="popularity">{t('filters.popularity')}</SelectItem>
              <SelectItem value="price">{t('filters.price_asc')}</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Category pills */}
        <div className="flex flex-wrap gap-2 mb-8">
          <ThemePill label={t('filters.allCategories')} active={selectedCategory === 'all'} onClick={() => setSelectedCategory('all')} />
          <ThemePill label={t('filters.free')} active={selectedCategory === 'free'} onClick={() => setSelectedCategory('free')} />
          {categories.map(c => (
            <ThemePill
              key={c.id}
              label={i18n.language === 'es' ? c.nameEs : c.nameEn}
              active={selectedCategory === c.slug}
              onClick={() => setSelectedCategory(c.slug)}
            />
          ))}
        </div>

        {/* Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {filteredEvents.map(event => (
            <EventCard key={event.id} event={event} />
          ))}
        </div>
      </div>
    </>
  );
};

export default EventListPage;
