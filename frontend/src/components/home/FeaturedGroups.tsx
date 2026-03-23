import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { ChevronRight } from 'lucide-react';
import GroupCard from '@/components/groups/GroupCard';
import { api } from '@/lib/api';

const FeaturedGroups = () => {
  const { t } = useTranslation('home');
  const { data: groups = [] } = useQuery({ queryKey: ['featured-groups'], queryFn: api.getGroups });

  return (
    <section className="section-padding gradient-section-cool">
      <div className="container mx-auto px-4">
        <div className="text-center mb-10">
          <h2 className="font-heading text-3xl font-bold mb-3">{t('featuredGroups.title')}</h2>
          <p className="font-body text-muted-foreground">{t('featuredGroups.subtitle')}</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {groups.slice(0, 4).map(group => (
            <GroupCard key={group.id} group={group} />
          ))}
        </div>

        <div className="text-center mt-8">
          <Link to="/groups" className="text-primary hover:text-primary-hover font-body text-sm font-medium inline-flex items-center gap-1">
            {t('common:buttons.seeAll')} <ChevronRight className="h-4 w-4" />
          </Link>
        </div>
      </div>
    </section>
  );
};

export default FeaturedGroups;
