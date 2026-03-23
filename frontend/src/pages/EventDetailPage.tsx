import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Calendar, MapPin, Users, Heart, Share2, Clock } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { format } from 'date-fns';
import { es, enUS } from 'date-fns/locale';
import EventCard from '@/components/events/EventCard';
import { api } from '@/lib/api';
import { useSession } from '@/hooks/use-session';

const EventDetailPage = () => {
  const { slug } = useParams();
  const { t, i18n } = useTranslation('events');
  const locale = i18n.language === 'es' ? es : enUS;
  const queryClient = useQueryClient();
  const { isLoggedIn } = useSession();
  const { data } = useQuery({
    queryKey: ['event', slug],
    queryFn: () => api.getEvent(slug!),
    enabled: Boolean(slug),
  });
  const event = data?.event;
  const relatedEvents = data?.relatedEvents ?? [];
  const rsvpMutation = useMutation({
    mutationFn: () => api.rsvpEvent(event!.id),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['event', slug] }),
  });
  const saveMutation = useMutation({
    mutationFn: () => api.saveEvent(event!.id),
  });
  if (!event) return null;
  const capacityPercent = event.maxAttendees ? ((event.currentAttendees || 0) / event.maxAttendees) * 100 : 0;
  const isFull = capacityPercent >= 100;

  return (
    <>
      <Helmet><title>{event.title} — Karma</title></Helmet>

      {/* Hero image */}
      <div className="relative h-64 md:h-96 overflow-hidden">
        <img src={event.coverImageUrl} alt={event.title} className="w-full h-full object-cover" />
        <div className="absolute inset-0 bg-gradient-to-t from-foreground/60 to-transparent" />
      </div>

      <div className="container mx-auto px-4 -mt-16 relative z-10">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main content */}
          <div className="lg:col-span-2">
            <div className="bg-card rounded-2xl p-6 md:p-8 shadow-sm mb-6">
              {/* Category badge */}
              {event.category && (
                <span className="pill-tag pill-tag-active text-xs mb-4 inline-block">
                  {i18n.language === 'es' ? event.category.nameEs : event.category.nameEn}
                </span>
              )}
              <h1 className="font-heading text-3xl md:text-4xl font-bold mb-4">{event.title}</h1>

              <div className="flex flex-wrap gap-4 mb-6 text-sm font-body text-muted-foreground">
                <span className="flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-primary" />
                  {format(new Date(event.startDate), "EEEE, d MMMM yyyy", { locale })}
                </span>
                <span className="flex items-center gap-2">
                  <Clock className="h-4 w-4 text-primary" />
                  {format(new Date(event.startDate), "HH:mm", { locale })}
                  {event.endDate && ` - ${format(new Date(event.endDate), "HH:mm", { locale })}`}
                </span>
                <span className="flex items-center gap-2">
                  <MapPin className="h-4 w-4 text-primary" />
                  {event.isOnline ? 'Online' : `${event.venueName}, ${event.city}`}
                </span>
              </div>

              <div className="flex gap-3 mb-8">
                <Button variant="ghost" size="sm" className="rounded-full font-body gap-2">
                  <Heart className="h-4 w-4" onClick={() => saveMutation.mutate()} /> {t('common:buttons.save')}
                </Button>
                <Button variant="ghost" size="sm" className="rounded-full font-body gap-2">
                  <Share2 className="h-4 w-4" /> {t('common:buttons.share')}
                </Button>
              </div>

              <h2 className="font-heading text-xl font-semibold mb-3">{t('detail.about')}</h2>
              <p className="font-body text-muted-foreground leading-relaxed whitespace-pre-line">
                {event.description || 'Una experiencia transformadora que te invita a reconectar contigo y con la comunidad. Ven con ropa cómoda y mente abierta.'}
              </p>
            </div>

            {/* Related events */}
            {relatedEvents.length > 0 && (
              <div>
                <h2 className="font-heading text-2xl font-bold mb-6">{t('detail.relatedEvents')}</h2>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                  {relatedEvents.map(e => <EventCard key={e.id} event={e} />)}
                </div>
              </div>
            )}
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* RSVP / Ticket card */}
            <div className="bg-card rounded-2xl p-6 shadow-sm sticky top-24">
              <div className="text-center mb-4">
                <span className={`font-heading text-3xl font-bold ${event.isFree ? 'text-sage' : 'text-primary'}`}>
                  {event.isFree ? t('common:free') : `${event.price}€`}
                </span>
              </div>

              {event.maxAttendees && (
                <div className="mb-4">
                  <div className="flex items-center justify-between text-sm font-body text-muted-foreground mb-1">
                    <span className="flex items-center gap-1">
                      <Users className="h-4 w-4" />
                      {event.currentAttendees} {t('common:attendees')}
                    </span>
                    <span>{event.currentAttendees}/{event.maxAttendees}</span>
                  </div>
                  <div className="h-2 bg-muted rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full ${isFull ? 'bg-coral' : 'bg-primary'}`}
                      style={{ width: `${Math.min(capacityPercent, 100)}%` }}
                    />
                  </div>
                  {isFull && (
                    <p className="text-coral text-xs font-body mt-2">{t('detail.eventFull')}</p>
                  )}
                </div>
              )}

              <Button className="w-full rounded-full font-body h-12 text-base" size="lg" onClick={() => isLoggedIn && rsvpMutation.mutate()}>
                {event.isFree
                  ? (isFull ? t('rsvp.joinWaitlist') : t('rsvp.attending'))
                  : t('common:buttons.buyTickets')
                }
              </Button>
            </div>

            {/* Organizer card */}
            {event.organizer && (
              <div className="bg-card rounded-2xl p-6 shadow-sm">
                <h3 className="font-heading text-lg font-semibold mb-4">{t('detail.organizer')}</h3>
                <div className="flex items-center gap-3">
                  <img
                    src={event.organizer.logoUrl}
                    alt={event.organizer.name}
                    className="w-12 h-12 rounded-full object-cover border-2 border-primary-light"
                  />
                  <div>
                    <p className="font-body font-semibold text-sm">{event.organizer.name}</p>
                    {event.organizer.verified && (
                      <span className="text-xs font-body text-sage">✓ Verificado</span>
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
};

export default EventDetailPage;
