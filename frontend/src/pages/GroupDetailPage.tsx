import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { MapPin, Users, Calendar } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import EventCard from '@/components/events/EventCard';
import { api } from '@/lib/api';
import { useSession } from '@/hooks/use-session';

const GroupDetailPage = () => {
  const { slug } = useParams();
  const { t, i18n } = useTranslation('groups');
  const { isLoggedIn } = useSession();
  const queryClient = useQueryClient();
  const { data } = useQuery({
    queryKey: ['group', slug],
    queryFn: () => api.getGroup(slug!),
    enabled: Boolean(slug),
  });
  const joinMutation = useMutation({
    mutationFn: () => api.joinGroup(data!.group.id),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['group', slug] }),
  });
  const group = data?.group;
  const groupEvents = data?.upcomingEvents ?? [];
  if (!group) return null;

  return (
    <>
      <Helmet><title>{group.name} — Karma</title></Helmet>

      {/* Banner */}
      <div className="relative h-48 md:h-64 overflow-hidden">
        <img src={group.bannerUrl} alt={group.name} className="w-full h-full object-cover" />
        <div className="absolute inset-0 bg-gradient-to-t from-foreground/50 to-transparent" />
      </div>

      <div className="container mx-auto px-4 -mt-12 relative z-10">
        <div className="bg-card rounded-2xl p-6 md:p-8 shadow-sm mb-8">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
              {group.category && (
                <span className="pill-tag pill-tag-active text-xs mb-2 inline-block">
                  {i18n.language === 'es' ? group.category.nameEs : group.category.nameEn}
                </span>
              )}
              <h1 className="font-heading text-3xl font-bold mb-2">{group.name}</h1>
              <div className="flex flex-wrap gap-4 text-sm font-body text-muted-foreground">
                <span className="flex items-center gap-1"><Users className="h-4 w-4" /> {group.memberCount} {t('common:members')}</span>
                {group.city && <span className="flex items-center gap-1"><MapPin className="h-4 w-4" /> {group.city}</span>}
              </div>
            </div>
            <Button size="lg" className="rounded-full font-body px-8" onClick={() => isLoggedIn && joinMutation.mutate()}>{t('join')}</Button>
          </div>
        </div>

        {/* Tabs */}
        <Tabs defaultValue="events">
          <TabsList className="bg-muted rounded-full p-1 mb-8">
            <TabsTrigger value="events" className="rounded-full font-body">{t('tabs.events')}</TabsTrigger>
            <TabsTrigger value="past" className="rounded-full font-body">{t('tabs.pastEvents')}</TabsTrigger>
            <TabsTrigger value="discussion" className="rounded-full font-body">{t('tabs.discussion')}</TabsTrigger>
            <TabsTrigger value="members" className="rounded-full font-body">{t('tabs.members')}</TabsTrigger>
            <TabsTrigger value="about" className="rounded-full font-body">{t('tabs.about')}</TabsTrigger>
          </TabsList>

          <TabsContent value="events">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {groupEvents.map(e => <EventCard key={e.id} event={e} />)}
            </div>
          </TabsContent>

          <TabsContent value="past">
            <p className="font-body text-muted-foreground">{t('common:noResults')}</p>
          </TabsContent>

          <TabsContent value="discussion">
            <div className="bg-card rounded-2xl p-6">
              <p className="font-body text-muted-foreground">{t('discussion.writePost')}</p>
            </div>
          </TabsContent>

          <TabsContent value="members">
            <div className="space-y-3">
              {data?.members?.length ? (
                data.members.map((member) => (
                  <div key={member.id} className="flex items-center gap-3 p-3 bg-card rounded-lg">
                    <div className="w-10 h-10 rounded-full bg-muted flex items-center justify-center text-xs font-semibold">
                      {member.firstName[0]}{member.lastName[0]}
                    </div>
                    <div>
                      <p className="font-body font-semibold text-sm">{member.firstName} {member.lastName}</p>
                    </div>
                  </div>
                ))
              ) : group.organizer && (
                <div className="flex items-center gap-3 p-3 bg-card rounded-lg">
                  <img src={group.organizer.logoUrl} alt={group.organizer.name} className="w-10 h-10 rounded-full object-cover" />
                  <div>
                    <p className="font-body font-semibold text-sm">{group.organizer.name}</p>
                    <span className="text-xs font-body text-primary">Organizador</span>
                  </div>
                </div>
              )}
            </div>
          </TabsContent>

          <TabsContent value="about">
            <div className="bg-card rounded-2xl p-6">
              <p className="font-body text-muted-foreground leading-relaxed">{group.description}</p>
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </>
  );
};

export default GroupDetailPage;
