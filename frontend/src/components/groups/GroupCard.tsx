import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { MapPin, Users, Calendar } from 'lucide-react';
import { Button } from '@/components/ui/button';
import type { Group } from '@/types';

const GroupCard = ({ group }: { group: Group }) => {
  const { t, i18n } = useTranslation();

  const categoryName = group.category
    ? (i18n.language === 'es' ? group.category.nameEs : group.category.nameEn)
    : '';

  return (
    <Link to={`/groups/${group.slug}`} className="group block">
      <div className="bg-card rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-all duration-300 group-hover:-translate-y-1">
        {/* Banner */}
        <div className="relative h-36 overflow-hidden">
          <img
            src={group.bannerUrl}
            alt={group.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
            loading="lazy"
          />
          {categoryName && (
            <span className="absolute top-3 left-3 bg-secondary/90 text-secondary-foreground text-xs font-body font-medium px-3 py-1 rounded-full">
              {categoryName}
            </span>
          )}
        </div>

        {/* Content */}
        <div className="p-4">
          <h3 className="font-heading text-lg font-semibold mb-2 group-hover:text-primary transition-colors">
            {group.name}
          </h3>
          <div className="flex items-center gap-3 text-xs font-body text-muted-foreground mb-3">
            <span className="flex items-center gap-1">
              <Users className="h-3 w-3" /> {group.memberCount} {t('members')}
            </span>
            {group.city && (
              <span className="flex items-center gap-1">
                <MapPin className="h-3 w-3" /> {group.city}
              </span>
            )}
            {group.upcomingEventCount !== undefined && (
              <span className="flex items-center gap-1">
                <Calendar className="h-3 w-3" /> {group.upcomingEventCount} {t('events')}
              </span>
            )}
          </div>
          <Button size="sm" className="rounded-full w-full font-body" variant="outline">
            {t('buttons.join')}
          </Button>
        </div>
      </div>
    </Link>
  );
};

export default GroupCard;
