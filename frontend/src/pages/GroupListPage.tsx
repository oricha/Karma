import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';
import { useQuery } from '@tanstack/react-query';
import { Search } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import GroupCard from '@/components/groups/GroupCard';
import ThemePill from '@/components/categories/ThemePill';
import { api } from '@/lib/api';

const GroupListPage = () => {
  const { t, i18n } = useTranslation('groups');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const { data: categories = [] } = useQuery({ queryKey: ['categories'], queryFn: api.getCategories });
  const { data: groups = [] } = useQuery({ queryKey: ['groups'], queryFn: api.getGroups });
  const filteredGroups = selectedCategory === 'all' ? groups : groups.filter((group) => group.category?.slug === selectedCategory);

  return (
    <>
      <Helmet><title>{t('title')} — Karma</title></Helmet>
      <div className="container mx-auto px-4 py-8">
        <h1 className="font-heading text-4xl font-bold mb-6">{t('title')}</h1>

        <div className="flex flex-wrap gap-3 mb-6">
          <div className="relative flex-1 min-w-[200px] max-w-md">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input placeholder={t('common:buttons.search')} className="pl-10 rounded-full font-body" />
          </div>
          <Select>
            <SelectTrigger className="w-[180px] rounded-full font-body">
              <SelectValue placeholder={t('filters.sort')} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="active">{t('filters.mostActive')}</SelectItem>
              <SelectItem value="newest">{t('filters.newest')}</SelectItem>
              <SelectItem value="members">{t('filters.mostMembers')}</SelectItem>
              <SelectItem value="nearest">{t('filters.nearest')}</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="flex flex-wrap gap-2 mb-8">
          <ThemePill label={i18n.language === 'es' ? 'Todas' : 'All'} active={selectedCategory === 'all'} onClick={() => setSelectedCategory('all')} />
          {categories.map(c => (
            <ThemePill
              key={c.id}
              label={i18n.language === 'es' ? c.nameEs : c.nameEn}
              active={selectedCategory === c.slug}
              onClick={() => setSelectedCategory(c.slug)}
            />
          ))}
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {filteredGroups.map(group => (
            <GroupCard key={group.id} group={group} />
          ))}
        </div>
      </div>
    </>
  );
};

export default GroupListPage;
