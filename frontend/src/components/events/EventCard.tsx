import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Calendar, MapPin, Users } from 'lucide-react';
import { Heart } from 'lucide-react';
import type { Event } from '@/types';
import { format } from 'date-fns';
import { es, enUS } from 'date-fns/locale';

const EventCard = ({ event }: { event: Event }) => {
  const { t, i18n } = useTranslation();
  const locale = i18n.language === 'es' ? es : enUS;

  const dateStr = format(new Date(event.startDate), "EEE, d MMM · HH:mm", { locale });
  const capacityPercent = event.maxAttendees ? ((event.currentAttendees || 0) / event.maxAttendees) * 100 : 0;

  return (
    <Link to={`/events/${event.slug}`} className="group block">
      <div className="bg-card rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-all duration-300 group-hover:-translate-y-1">
        {/* Image */}
        <div className="relative aspect-[16/10] overflow-hidden">
          <img
            src={event.coverImageUrl}
            alt={event.title}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
            loading="lazy"
          />
          {/* Date badge */}
          <div className="absolute top-3 left-3 bg-background/90 backdrop-blur-sm rounded-lg px-3 py-1.5 text-xs font-body font-semibold">
            {format(new Date(event.startDate), 'd MMM', { locale }).toUpperCase()}
          </div>
          {/* Price badge */}
          <div className={`absolute top-3 right-3 rounded-full px-3 py-1 text-xs font-body font-semibold ${
            event.isFree ? 'bg-sage text-background' : 'bg-primary text-primary-foreground'
          }`}>
            {event.isFree ? t('free') : `${event.price}€`}
          </div>
          {/* Save button */}
          <button
            className="absolute bottom-3 right-3 p-2 rounded-full bg-background/80 backdrop-blur-sm hover:bg-background transition-colors"
            onClick={(e) => { e.preventDefault(); }}
          >
            <Heart className="h-4 w-4 text-coral" />
          </button>
        </div>

        {/* Content */}
        <div className="p-4">
          <p className="text-xs font-body text-muted-foreground flex items-center gap-1 mb-1">
            <Calendar className="h-3 w-3" />
            {dateStr}
          </p>
          <h3 className="font-heading text-lg font-semibold leading-tight mb-2 group-hover:text-primary transition-colors line-clamp-2">
            {event.title}
          </h3>
          <p className="text-sm font-body text-muted-foreground flex items-center gap-1 mb-3">
            <MapPin className="h-3 w-3" />
            {event.isOnline ? 'Online' : `${event.venueName || event.city}`}
          </p>

          {/* Capacity */}
          {event.maxAttendees && (
            <div className="mb-3">
              <div className="flex items-center justify-between text-xs font-body text-muted-foreground mb-1">
                <span className="flex items-center gap-1">
                  <Users className="h-3 w-3" />
                  {event.currentAttendees} {t('attendees')}
                </span>
                <span>{event.currentAttendees}/{event.maxAttendees}</span>
              </div>
              <div className="h-1.5 bg-muted rounded-full overflow-hidden">
                <div
                  className={`h-full rounded-full transition-all ${capacityPercent >= 100 ? 'bg-coral' : 'bg-primary'}`}
                  style={{ width: `${Math.min(capacityPercent, 100)}%` }}
                />
              </div>
            </div>
          )}

          {/* Organizer */}
          {event.organizer && (
            <div className="flex items-center gap-2 pt-2 border-t border-border">
              <img
                src={event.organizer.logoUrl}
                alt={event.organizer.name}
                className="w-6 h-6 rounded-full object-cover"
                loading="lazy"
              />
              <span className="text-xs font-body text-muted-foreground">{event.organizer.name}</span>
            </div>
          )}
        </div>
      </div>
    </Link>
  );
};

export default EventCard;
