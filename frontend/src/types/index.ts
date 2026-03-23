export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  avatarUrl?: string;
  bio?: string;
  phone?: string;
  role: 'USER' | 'ORGANIZER' | 'ADMIN';
  locale: 'es' | 'en';
  emailVerified: boolean;
}

export interface Category {
  id: string;
  nameEs: string;
  nameEn: string;
  slug: string;
  descriptionEs?: string;
  descriptionEn?: string;
  imageUrl?: string;
  eventCount?: number;
}

export interface Theme {
  id: string;
  categoryId: string;
  nameEs: string;
  nameEn: string;
  slug: string;
}

export interface OrganizerProfile {
  id: string;
  userId: string;
  name: string;
  slug: string;
  bio?: string;
  website?: string;
  logoUrl?: string;
  verified: boolean;
}

export interface Group {
  id: string;
  organizerId: string;
  organizer?: OrganizerProfile;
  name: string;
  slug: string;
  description?: string;
  categoryId?: string;
  category?: Category;
  bannerUrl?: string;
  city?: string;
  country?: string;
  isPrivate: boolean;
  status: 'ACTIVE' | 'ARCHIVED';
  memberCount: number;
  upcomingEventCount?: number;
  notificationPreference?: 'IMMEDIATE' | 'DIGEST' | 'NEVER';
}

export interface Event {
  id: string;
  organizerId: string;
  organizer?: OrganizerProfile;
  groupId?: string;
  group?: Group;
  title: string;
  slug: string;
  description?: string;
  coverImageUrl?: string;
  startDate: string;
  endDate?: string;
  venueName?: string;
  address?: string;
  city?: string;
  country?: string;
  isOnline: boolean;
  isHybrid: boolean;
  onlineUrl?: string;
  status: 'DRAFT' | 'PUBLISHED' | 'CANCELLED';
  featured: boolean;
  maxAttendees?: number;
  currentAttendees?: number;
  isFree: boolean;
  price?: number;
  currency?: string;
  language: string;
  themes?: Theme[];
  category?: Category;
}

export interface RSVP {
  id: string;
  eventId: string;
  userId: string;
  status: 'YES' | 'NO' | 'WAITLISTED' | 'MAYBE';
  waitlistPosition?: number;
  checkedIn: boolean;
}

export interface TicketType {
  id: string;
  eventId: string;
  name: string;
  description?: string;
  price: number;
  currency: string;
  quantity: number;
  soldCount: number;
  earlyBirdPrice?: number;
  earlyBirdQuantity?: number;
  earlyBirdDeadline?: string;
}

export interface Order {
  id: string;
  userId: string;
  eventId: string;
  event?: Event;
  status: 'PENDING' | 'PAID' | 'CANCELLED' | 'REFUNDED';
  totalAmount: number;
  currency: string;
  purchasedAt: string;
}

export interface GroupPost {
  id: string;
  groupId: string;
  author: User;
  content: string;
  imageUrl?: string;
  isPinned: boolean;
  createdAt: string;
  replyCount?: number;
}

export interface BlogPost {
  id: string;
  titleEs: string;
  titleEn: string;
  slug: string;
  excerptEs?: string;
  excerptEn?: string;
  coverImageUrl?: string;
  publishedAt?: string;
}
