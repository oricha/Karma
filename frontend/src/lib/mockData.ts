import type { Event, Group, Category, OrganizerProfile, BlogPost } from '@/types';

export const mockOrganizer: OrganizerProfile = {
  id: '1',
  userId: '1',
  name: 'María Luna',
  slug: 'maria-luna',
  bio: 'Facilitadora de danza consciente y ceremonias de cacao con más de 10 años de experiencia.',
  logoUrl: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop&crop=face',
  verified: true,
};

export const mockOrganizer2: OrganizerProfile = {
  id: '2',
  userId: '2',
  name: 'Carlos Sánchez',
  slug: 'carlos-sanchez',
  bio: 'Profesor de yoga y meditación.',
  logoUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=face',
  verified: false,
};

export const mockCategories: Category[] = [
  { id: '1', nameEs: 'Talleres', nameEn: 'Workshops', slug: 'talleres', imageUrl: 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=400&h=300&fit=crop', eventCount: 128 },
  { id: '2', nameEs: 'Ceremonias', nameEn: 'Ceremonies', slug: 'ceremonias', imageUrl: 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400&h=300&fit=crop', eventCount: 56 },
  { id: '3', nameEs: 'Danza', nameEn: 'Dance', slug: 'danza', imageUrl: 'https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=400&h=300&fit=crop', eventCount: 89 },
  { id: '4', nameEs: 'Música', nameEn: 'Music', slug: 'musica', imageUrl: 'https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=400&h=300&fit=crop', eventCount: 34 },
  { id: '5', nameEs: 'Festivales y Retiros', nameEn: 'Festivals & Retreats', slug: 'festivales-retiros', imageUrl: 'https://images.unsplash.com/photo-1528495612343-9ca9f755e7bc?w=400&h=300&fit=crop', eventCount: 22 },
  { id: '6', nameEs: 'Charlas y Espectáculos', nameEn: 'Talks & Performances', slug: 'charlas-espectaculos', imageUrl: 'https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=400&h=300&fit=crop', eventCount: 41 },
];

export const mockEvents: Event[] = [
  {
    id: '1', organizerId: '1', organizer: mockOrganizer, title: 'Danza Extática al Atardecer', slug: 'danza-extatica-atardecer',
    description: 'Una experiencia de movimiento libre y consciente acompañada de música envolvente. Conecta con tu cuerpo y libera tensiones a través de la danza.',
    coverImageUrl: 'https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=600&h=400&fit=crop',
    startDate: '2026-04-05T18:00:00', endDate: '2026-04-05T21:00:00', venueName: 'Espacio Gaia',
    address: 'Calle del Sol 12', city: 'Madrid', country: 'España', isOnline: false, isHybrid: false,
    status: 'PUBLISHED', featured: true, maxAttendees: 50, currentAttendees: 38, isFree: true, language: 'es',
    category: mockCategories[2],
  },
  {
    id: '2', organizerId: '1', organizer: mockOrganizer, title: 'Ceremonia de Cacao Sagrado', slug: 'ceremonia-cacao-sagrado',
    coverImageUrl: 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600&h=400&fit=crop',
    startDate: '2026-04-08T19:00:00', endDate: '2026-04-08T22:00:00', venueName: 'Casa del Alma',
    city: 'Barcelona', country: 'España', isOnline: false, isHybrid: false,
    status: 'PUBLISHED', featured: false, maxAttendees: 25, currentAttendees: 25, isFree: false, price: 35, currency: 'EUR', language: 'es',
    category: mockCategories[1],
  },
  {
    id: '3', organizerId: '2', organizer: mockOrganizer2, title: 'Retiro de Yoga y Meditación', slug: 'retiro-yoga-meditacion',
    coverImageUrl: 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=600&h=400&fit=crop',
    startDate: '2026-04-12T09:00:00', endDate: '2026-04-13T18:00:00', venueName: 'Finca La Paz',
    city: 'Granada', country: 'España', isOnline: false, isHybrid: false,
    status: 'PUBLISHED', featured: true, maxAttendees: 20, currentAttendees: 12, isFree: false, price: 120, currency: 'EUR', language: 'es',
    category: mockCategories[4],
  },
  {
    id: '4', organizerId: '2', organizer: mockOrganizer2, title: 'Kirtan: Cantos de Mantras', slug: 'kirtan-cantos-mantras',
    coverImageUrl: 'https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=600&h=400&fit=crop',
    startDate: '2026-04-06T20:00:00', endDate: '2026-04-06T22:30:00', venueName: 'Centro Ananda',
    city: 'Valencia', country: 'España', isOnline: false, isHybrid: false,
    status: 'PUBLISHED', featured: false, maxAttendees: 40, currentAttendees: 15, isFree: true, language: 'es',
    category: mockCategories[3],
  },
  {
    id: '5', organizerId: '1', organizer: mockOrganizer, title: 'Taller de Trabajo de Respiración', slug: 'taller-breathwork',
    coverImageUrl: 'https://images.unsplash.com/photo-1528495612343-9ca9f755e7bc?w=600&h=400&fit=crop',
    startDate: '2026-04-10T10:00:00', endDate: '2026-04-10T13:00:00', venueName: 'Espacio Gaia',
    city: 'Madrid', country: 'España', isOnline: false, isHybrid: false,
    status: 'PUBLISHED', featured: false, maxAttendees: 30, currentAttendees: 22, isFree: false, price: 25, currency: 'EUR', language: 'es',
    category: mockCategories[0],
  },
  {
    id: '6', organizerId: '2', organizer: mockOrganizer2, title: 'Meditación Guiada Online', slug: 'meditacion-guiada-online',
    coverImageUrl: 'https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=600&h=400&fit=crop',
    startDate: '2026-04-07T08:00:00', endDate: '2026-04-07T09:00:00',
    city: 'Online', country: '', isOnline: true, isHybrid: false,
    status: 'PUBLISHED', featured: false, isFree: true, language: 'es',
    category: mockCategories[0], currentAttendees: 64,
  },
];

export const mockGroups: Group[] = [
  {
    id: '1', organizerId: '1', organizer: mockOrganizer, name: 'Ecstatic Dance Madrid', slug: 'ecstatic-dance-madrid',
    description: 'Comunidad de danza libre y consciente en Madrid. Nos reunimos cada semana para bailar, sentir y conectar.',
    bannerUrl: 'https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=800&h=300&fit=crop',
    city: 'Madrid', country: 'España', isPrivate: false, status: 'ACTIVE', memberCount: 342,
    category: mockCategories[2], upcomingEventCount: 4,
  },
  {
    id: '2', organizerId: '1', organizer: mockOrganizer, name: 'Yoga en el Parque BCN', slug: 'yoga-parque-bcn',
    description: 'Sesiones de yoga gratuitas al aire libre en los parques de Barcelona.',
    bannerUrl: 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800&h=300&fit=crop',
    city: 'Barcelona', country: 'España', isPrivate: false, status: 'ACTIVE', memberCount: 189,
    category: mockCategories[0], upcomingEventCount: 2,
  },
  {
    id: '3', organizerId: '2', organizer: mockOrganizer2, name: 'Círculo de Cacao Valencia', slug: 'circulo-cacao-valencia',
    description: 'Ceremonias de cacao y círculos de compartir en Valencia.',
    bannerUrl: 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=800&h=300&fit=crop',
    city: 'Valencia', country: 'España', isPrivate: false, status: 'ACTIVE', memberCount: 98,
    category: mockCategories[1], upcomingEventCount: 1,
  },
  {
    id: '4', organizerId: '2', organizer: mockOrganizer2, name: 'Sound Healing España', slug: 'sound-healing-espana',
    description: 'Red de facilitadores y practicantes de sonoterapia y baños de sonido.',
    bannerUrl: 'https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=800&h=300&fit=crop',
    city: 'Madrid', country: 'España', isPrivate: false, status: 'ACTIVE', memberCount: 156,
    category: mockCategories[3], upcomingEventCount: 3,
  },
];

export const mockBlogPosts: BlogPost[] = [
  {
    id: '1', titleEs: '5 Beneficios de la Danza Extática', titleEn: '5 Benefits of Ecstatic Dance',
    slug: '5-beneficios-danza-extatica', excerptEs: 'Descubre cómo la danza libre puede transformar tu bienestar físico y emocional.',
    excerptEn: 'Discover how free dance can transform your physical and emotional well-being.',
    coverImageUrl: 'https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=400&h=250&fit=crop', publishedAt: '2026-03-15',
  },
  {
    id: '2', titleEs: 'Guía para tu Primera Ceremonia de Cacao', titleEn: 'Guide to Your First Cacao Ceremony',
    slug: 'guia-primera-ceremonia-cacao', excerptEs: 'Todo lo que necesitas saber antes de asistir a una ceremonia de cacao sagrado.',
    excerptEn: 'Everything you need to know before attending a sacred cacao ceremony.',
    coverImageUrl: 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400&h=250&fit=crop', publishedAt: '2026-03-10',
  },
  {
    id: '3', titleEs: 'Meditación para Principiantes', titleEn: 'Meditation for Beginners',
    slug: 'meditacion-principiantes', excerptEs: 'Empieza tu práctica de meditación con estos consejos simples y efectivos.',
    excerptEn: 'Start your meditation practice with these simple and effective tips.',
    coverImageUrl: 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=400&h=250&fit=crop', publishedAt: '2026-03-05',
  },
];
