# Design Document: Karma Platform

## Overview

Karma is a wellness and spirituality events portal that operates on a group-centric model similar to Meetup.com. The platform enables users to discover and join wellness communities (groups), RSVP to free events, purchase tickets for paid events, and receive personalized event recommendations. Organizers can create groups, host events, and manage their communities.

The system is built with a Java 21 Spring Boot backend, React TypeScript frontend, and PostgreSQL database with PostGIS for geospatial queries. The platform supports bilingual operation (Spanish primary, English secondary) and includes automated email digests, event reminders, and payment processing through Stripe.

### Key Design Goals

- **Community-first architecture**: Groups are the primary organizing unit, with events belonging to groups
- **Scalability**: Stateless backend design supporting horizontal scaling
- **Geospatial efficiency**: PostGIS-powered location-based search for events and groups
- **Automated engagement**: Weekly digest system and event reminders to drive user retention
- **Bilingual support**: Complete i18n implementation for Spanish and English
- **Payment integration**: Seamless Stripe checkout for paid events with early bird pricing
- **Waitlist automation**: Automatic promotion from waitlist when spots become available

## Architecture

### High-Level Architecture

The system follows a three-tier architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend Layer                           │
│  React + TypeScript + Vite + Tailwind CSS + shadcn/ui      │
│  Hosted on Vercel                                           │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ HTTPS/REST
                            │
┌─────────────────────────────────────────────────────────────┐
│                     Backend Layer                            │
│  Java 21 + Spring Boot 3.x + Spring Security               │
│  JWT Authentication + Spring Data JPA                       │
│  Docker Container                                           │
└─────────────────────────────────────────────────────────────┘
                            │
                ┌───────────┴───────────┐
                │                       │
┌───────────────▼──────────┐  ┌────────▼──────────┐
│   PostgreSQL 16          │  │  External Services │
│   + PostGIS Extension    │  │  - Stripe          │
│   Docker Container       │  │  - SendGrid        │
└──────────────────────────┘  │  - S3 Storage      │
                              └────────────────────┘
```

### Backend Module Structure

The backend is organized into domain-driven modules:

- **auth**: Authentication and authorization (JWT tokens, password reset)
- **user**: User profile management and preferences
- **event**: Event creation, management, and discovery
- **rsvp**: RSVP system with waitlist management
- **group**: Group creation and membership management
- **discussion**: Group discussion posts and replies
- **category**: Categories and themes taxonomy
- **organizer**: Organizer profiles and dashboard
- **order**: Ticket purchasing and order management
- **preference**: User preferences (themes, location, newsletter frequency)
- **savedevent**: Saved events bookmarking
- **review**: Event reviews and ratings
- **blog**: Blog posts (bilingual content)
- **notification**: Email service, digest generation, and reminder scheduling
- **common**: Shared utilities, exception handling, i18n support

### Data Flow Patterns

**Event Discovery Flow:**
1. User sets location preferences and theme interests
2. Frontend requests events with filters (location radius, themes, date range)
3. Backend uses PostGIS ST_DWithin for geospatial query
4. Results filtered by theme preferences and sorted by relevance
5. Frontend displays EventCards with RSVP/ticket information

**RSVP with Waitlist Flow:**
1. User clicks "Asistir" on event at capacity
2. Backend creates RSVP with status WAITLISTED and assigns position
3. When another user cancels, WaitlistService promotes first waitlisted user
4. EmailService sends "You're in!" notification
5. Frontend updates RSVP status in real-time

**Weekly Digest Flow:**
1. Spring Scheduler triggers job every Monday 9AM UTC
2. Job queries users by newsletter_freq (WEEKLY, BIWEEKLY, MONTHLY)
3. For each user, query events within location_radius_km matching theme preferences
4. Include events from joined groups
5. EmailService sends personalized digest in user's preferred language
6. Track open/click rates via SendGrid webhooks

## Components and Interfaces

### Core Domain Entities

#### User Entity
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String passwordHash;
    
    private String firstName;
    private String lastName;
    private String avatarUrl;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    private String phone;
    
    @Enumerated(EnumType.STRING)
    private UserRole role; // USER, ORGANIZER, ADMIN
    
    @Column(length = 5)
    private String locale; // es, en
    
    private String timezone;
    private Boolean emailVerified;
    
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserPreference preference;
    
    @OneToMany(mappedBy = "user")
    private Set<GroupMembership> groupMemberships;
}
```

#### Event Entity
```java
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private OrganizerProfile organizer;
    
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;
    
    @Column(nullable = false, length = 300)
    private String title;
    
    @Column(unique = true, nullable = false)
    private String slug;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String coverImageUrl;
    
    @Column(nullable = false)
    private Instant startDate;
    
    private Instant endDate;
    
    private String venueName;
    private String address;
    private String city;
    private String country;
    
    @Column(columnDefinition = "geography(Point, 4326)")
    private Point locationPoint; // PostGIS type
    
    private Boolean isOnline;
    private Boolean isHybrid;
    private String onlineUrl;
    
    @Enumerated(EnumType.STRING)
    private EventStatus status; // DRAFT, PUBLISHED, CANCELLED
    
    @Enumerated(EnumType.STRING)
    private EventType eventType; // SINGLE, RECURRING
    
    private String recurrenceRule; // WEEKLY, BIWEEKLY, MONTHLY
    private LocalDate recurrenceEnd;
    
    @ManyToOne
    @JoinColumn(name = "parent_event_id")
    private Event parentEvent;
    
    private Boolean featured;
    private Integer maxAttendees;
    private Boolean isFree;
    
    @Column(length = 5)
    private String language;
    
    @ManyToMany
    @JoinTable(
        name = "event_themes",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "theme_id")
    )
    private Set<Theme> themes;
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private Set<Rsvp> rsvps;
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private Set<TicketType> ticketTypes;
}
```

#### Group Entity
```java
@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private OrganizerProfile organizer;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String slug;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    private String bannerUrl;
    private String city;
    private String country;
    
    @Column(columnDefinition = "geography(Point, 4326)")
    private Point locationPoint;
    
    private Boolean isPrivate;
    
    @Enumerated(EnumType.STRING)
    private GroupStatus status; // ACTIVE, ARCHIVED
    
    private Integer memberCount; // Denormalized for performance
    
    @OneToMany(mappedBy = "group")
    private Set<GroupMembership> memberships;
    
    @OneToMany(mappedBy = "group")
    private Set<Event> events;
}
```

#### RSVP Entity
```java
@Entity
@Table(name = "rsvps")
public class Rsvp {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    private RsvpStatus status; // YES, NO, WAITLISTED, MAYBE
    
    private Integer waitlistPosition;
    private Boolean checkedIn;
    private Boolean noShow;
    
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
}
```

### Service Layer Interfaces

#### EventService
```java
public interface EventService {
    EventDTO createEvent(CreateEventRequest request, UUID organizerId);
    EventDTO updateEvent(UUID eventId, UpdateEventRequest request, UUID organizerId);
    void deleteEvent(UUID eventId, UUID organizerId);
    EventDTO publishEvent(UUID eventId, UUID organizerId);
    EventDTO cancelEvent(UUID eventId, UUID organizerId);
    
    Page<EventDTO> findEvents(EventSearchCriteria criteria, Pageable pageable);
    Page<EventDTO> findNearbyEvents(Double lat, Double lng, Integer radiusKm, Pageable pageable);
    Page<EventDTO> findEventsByCategory(String categorySlug, Pageable pageable);
    Page<EventDTO> findEventsByTheme(String themeSlug, Pageable pageable);
    
    EventDTO getEventBySlug(String slug);
    List<EventDTO> getPopularEvents(String region, LocalDate startDate, LocalDate endDate);
    
    void generateRecurringInstances(UUID parentEventId);
}
```

#### RsvpService
```java
public interface RsvpService {
    RsvpDTO createOrUpdateRsvp(UUID eventId, UUID userId, RsvpStatus status);
    void cancelRsvp(UUID eventId, UUID userId);
    RsvpDTO getUserRsvp(UUID eventId, UUID userId);
    
    Page<RsvpDTO> getEventAttendees(UUID eventId, Pageable pageable);
    Page<RsvpDTO> getEventWaitlist(UUID eventId, Pageable pageable);
    
    void checkInAttendee(UUID eventId, UUID userId, UUID organizerId);
    void markNoShow(UUID eventId, UUID userId, UUID organizerId);
    
    int getConfirmedCount(UUID eventId);
    int getWaitlistCount(UUID eventId);
}
```

#### WaitlistService
```java
public interface WaitlistService {
    void addToWaitlist(UUID eventId, UUID userId);
    void promoteFromWaitlist(UUID eventId);
    void promoteSpecificUser(UUID eventId, UUID userId);
    
    Optional<Rsvp> getNextWaitlistedUser(UUID eventId);
    void reorderWaitlist(UUID eventId);
}
```

#### GroupService
```java
public interface GroupService {
    GroupDTO createGroup(CreateGroupRequest request, UUID organizerId);
    GroupDTO updateGroup(UUID groupId, UpdateGroupRequest request, UUID organizerId);
    void archiveGroup(UUID groupId, UUID organizerId);
    
    Page<GroupDTO> findGroups(GroupSearchCriteria criteria, Pageable pageable);
    Page<GroupDTO> findNearbyGroups(Double lat, Double lng, Integer radiusKm, Pageable pageable);
    GroupDTO getGroupBySlug(String slug);
    
    void joinGroup(UUID groupId, UUID userId);
    void leaveGroup(UUID groupId, UUID userId);
    void removeMember(UUID groupId, UUID userId, UUID organizerId);
    void updateMemberRole(UUID groupId, UUID userId, MemberRole role, UUID organizerId);
    
    Page<GroupMemberDTO> getGroupMembers(UUID groupId, Pageable pageable);
}
```

#### EmailService
```java
public interface EmailService {
    void sendWelcomeEmail(User user);
    void sendEmailVerification(User user, String token);
    void sendPasswordResetEmail(User user, String token);
    
    void sendRsvpConfirmation(Rsvp rsvp);
    void sendWaitlistPromotionEmail(Rsvp rsvp);
    void sendOrderConfirmation(Order order);
    
    void sendEventReminder(Rsvp rsvp, int hoursBeforeEvent);
    void sendEventCancellation(Event event, List<User> attendees);
    
    void sendReviewRequest(User user, Event event);
    void sendNewGroupEventNotification(Event event, List<User> members);
    
    void sendWeeklyDigest(User user, List<Event> recommendedEvents);
}
```

#### PaymentService
```java
public interface PaymentService {
    String createCheckoutSession(CreateOrderRequest request, UUID userId);
    Order confirmPayment(String stripePaymentId);
    void processRefund(UUID orderId, UUID organizerId);
    
    void handleStripeWebhook(String payload, String signature);
}
```

### REST API Endpoints

#### Authentication Endpoints
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and receive JWT tokens
- `POST /api/auth/refresh` - Refresh access token
- `POST /api/auth/forgot-password` - Request password reset
- `POST /api/auth/reset-password` - Reset password with token
- `GET /api/auth/verify-email` - Verify email address

#### Event Endpoints
- `GET /api/events` - List events with filters (category, theme, location, date, price)
- `GET /api/events/:slug` - Get event details
- `GET /api/events/popular` - Get popular events by region
- `GET /api/events/nearby` - Geospatial search (lat, lng, radius)
- `GET /api/events/category/:slug` - Events by category
- `GET /api/events/search` - Full-text search
- `POST /api/events` - Create event (organizer)
- `PUT /api/events/:id` - Update event (organizer)
- `DELETE /api/events/:id` - Delete event (organizer)
- `POST /api/events/:id/publish` - Publish draft event
- `POST /api/events/:id/cancel` - Cancel event

#### RSVP Endpoints
- `POST /api/events/:id/rsvp` - Create/update RSVP
- `DELETE /api/events/:id/rsvp` - Cancel RSVP
- `GET /api/events/:id/rsvp` - Get current user's RSVP
- `GET /api/events/:id/attendees` - Get attendee list
- `PUT /api/events/:id/attendees/:userId/check-in` - Check-in attendee (organizer)
- `POST /api/events/:id/waitlist/promote` - Promote from waitlist

#### Group Endpoints
- `GET /api/groups` - List groups with filters
- `GET /api/groups/:slug` - Get group details
- `GET /api/groups/nearby` - Geospatial search
- `POST /api/groups` - Create group (organizer)
- `PUT /api/groups/:id` - Update group (organizer)
- `POST /api/groups/:id/join` - Join group
- `DELETE /api/groups/:id/leave` - Leave group
- `GET /api/groups/:id/members` - Get members
- `PUT /api/groups/:id/members/:userId/role` - Update member role
- `DELETE /api/groups/:id/members/:userId` - Remove member
- `GET /api/groups/:id/events` - Get group's events

#### User Endpoints
- `GET /api/users/me` - Get current user profile
- `PUT /api/users/me` - Update profile
- `PUT /api/users/me/avatar` - Upload avatar
- `GET /api/users/me/preferences` - Get preferences
- `PUT /api/users/me/preferences` - Update preferences
- `GET /api/users/me/saved-events` - Get saved events
- `POST /api/users/me/saved-events/:eventId` - Save event
- `DELETE /api/users/me/saved-events/:eventId` - Unsave event
- `GET /api/users/me/orders` - Get orders
- `GET /api/users/me/groups` - Get user's groups
- `GET /api/users/me/events` - Get user's RSVP'd events

## Data Models

### Database Schema

The database uses PostgreSQL 16 with PostGIS extension for geospatial data. Key design decisions:

- **UUID primary keys**: For security and distributed system compatibility
- **PostGIS geography type**: For accurate distance calculations using ST_DWithin
- **Denormalized counters**: member_count, sold_count for performance
- **JSONB for flexible data**: notification payloads, future extensibility
- **Composite unique constraints**: Prevent duplicate RSVPs, group memberships
- **Cascading deletes**: Maintain referential integrity
- **Indexes on query patterns**: location_point (GIST), start_date, status, foreign keys

### Entity Relationships

```
User 1──────* GroupMembership *──────1 Group
User 1──────* Rsvp *──────1 Event
User 1──────* Order *──────1 Event
User 1──────* SavedEvent *──────1 Event
User 1──────* Review *──────1 Event
User 1──────1 UserPreference
User 1──────1 OrganizerProfile

OrganizerProfile 1──────* Group
OrganizerProfile 1──────* Event

Group 1──────* Event
Group 1──────* GroupPost
Group *──────1 Category

Event *──────* Theme
Event 1──────* TicketType
Event 1──────* OrderItem

Category 1──────* Theme

GroupPost 1──────* GroupPostReply
```

### Key Indexes

```sql
-- Geospatial indexes (GIST for geography types)
CREATE INDEX idx_events_location ON events USING GIST(location_point);
CREATE INDEX idx_groups_location ON groups USING GIST(location_point);
CREATE INDEX idx_user_preferences_location ON user_preferences USING GIST(location_point);

-- Query performance indexes
CREATE INDEX idx_events_start_date ON events(start_date);
CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_events_organizer ON events(organizer_id);
CREATE INDEX idx_events_group ON events(group_id);
CREATE INDEX idx_event_themes_theme ON event_themes(theme_id);

-- RSVP and membership lookups
CREATE INDEX idx_rsvps_event ON rsvps(event_id);
CREATE INDEX idx_rsvps_user ON rsvps(user_id);
CREATE INDEX idx_group_memberships_user ON group_memberships(user_id);
CREATE INDEX idx_group_memberships_group ON group_memberships(group_id);

-- Notification processing
CREATE INDEX idx_notifications_user_status ON notifications(user_id, status);
CREATE INDEX idx_notifications_scheduled ON notifications(scheduled_for) WHERE status = 'PENDING';
```

### Seed Data

Categories and themes are seeded at application startup with bilingual names (Spanish and English). The seed data includes:

**Categories**: Talleres/Workshops, Ceremonias/Ceremonies, Danza/Dance, Música/Music, Festivales y Retiros/Festivals & Retreats, Charlas y Espectáculos/Talks & Performances, Otros/Other

**Themes** (examples): Tantra, Yoga, Meditación/Meditation, Danza Extática/Ecstatic Dance, Kirtan, Cacao Ceremony, Temazcal/Sweat Lodge, etc.

Full seed data list is defined in the technical specification document.

## Key Algorithms

### Waitlist Auto-Promotion Algorithm

When a user cancels an RSVP with status YES:

```
1. Update RSVP status to NO
2. Query for next waitlisted user:
   SELECT * FROM rsvps 
   WHERE event_id = ? AND status = 'WAITLISTED'
   ORDER BY waitlist_position ASC
   LIMIT 1
3. If waitlisted user found:
   a. Update their status to YES
   b. Set waitlist_position to NULL
   c. Reorder remaining waitlist (decrement positions)
   d. Send "You're in!" email notification
4. Commit transaction
```

### Weekly Digest Personalization Algorithm

Executed every Monday at 9:00 AM UTC:

```
1. Query users WHERE newsletter_freq IN ('WEEKLY', 'BIWEEKLY', 'MONTHLY')
   - Filter by last_digest_sent date based on frequency
   
2. For each user:
   a. Query events from joined groups:
      SELECT e.* FROM events e
      JOIN groups g ON e.group_id = g.id
      JOIN group_memberships gm ON g.id = gm.group_id
      WHERE gm.user_id = ? 
      AND e.start_date BETWEEN NOW() AND NOW() + INTERVAL '14 days'
      AND e.status = 'PUBLISHED'
      LIMIT 5
      
   b. Query events matching theme preferences and location:
      SELECT e.* FROM events e
      JOIN event_themes et ON e.id = et.event_id
      JOIN user_theme_preferences utp ON et.theme_id = utp.theme_id
      JOIN user_preferences up ON up.user_id = ?
      WHERE utp.user_id = ?
      AND ST_DWithin(e.location_point, up.location_point, up.location_radius_km * 1000)
      AND e.start_date BETWEEN NOW() AND NOW() + INTERVAL '14 days'
      AND e.status = 'PUBLISHED'
      AND e.id NOT IN (group_event_ids)
      ORDER BY e.start_date ASC
      LIMIT 5
      
   c. Query popular events in user's region:
      SELECT e.*, COUNT(r.id) as rsvp_count
      FROM events e
      LEFT JOIN rsvps r ON e.id = r.event_id AND r.status = 'YES'
      JOIN user_preferences up ON up.user_id = ?
      WHERE ST_DWithin(e.location_point, up.location_point, up.location_radius_km * 1000)
      AND e.start_date BETWEEN NOW() AND NOW() + INTERVAL '7 days'
      AND e.status = 'PUBLISHED'
      GROUP BY e.id
      ORDER BY rsvp_count DESC
      LIMIT 3
      
   d. Render email template in user's preferred locale
   e. Send via SendGrid
   f. Log to email_digest_log table
   
3. Batch sends at max 500 emails/hour to avoid throttling
```

### Geospatial Search with PostGIS

For finding events within a radius:

```sql
SELECT e.*, 
       ST_Distance(e.location_point, ST_MakePoint(?, ?)::geography) / 1000 AS distance_km
FROM events e
WHERE ST_DWithin(
    e.location_point,
    ST_MakePoint(?, ?)::geography,  -- user's lat/lng
    ? * 1000  -- radius in meters
)
AND e.status = 'PUBLISHED'
AND e.start_date >= NOW()
ORDER BY distance_km ASC, e.start_date ASC
LIMIT ? OFFSET ?
```

Key points:
- `ST_MakePoint(lng, lat)` creates a point (note: longitude first!)
- `::geography` cast ensures accurate distance calculations on Earth's surface
- `ST_DWithin` uses spatial index for efficient radius queries
- Distance calculated in meters, converted to km for display

### Recurring Event Generation

When an organizer creates a recurring event:

```
1. Validate recurrence_end <= 6 months from start_date
2. Create parent event with event_type = RECURRING
3. Generate instances based on recurrence_rule:
   
   current_date = start_date
   while current_date <= recurrence_end:
       if recurrence_rule == 'WEEKLY':
           current_date += 7 days
       elif recurrence_rule == 'BIWEEKLY':
           current_date += 14 days
       elif recurrence_rule == 'MONTHLY':
           current_date = same day next month
       
       Create new Event:
           - Copy all fields from parent
           - Set start_date = current_date
           - Set parent_event_id = parent.id
           - Set event_type = SINGLE
           - Generate unique slug with date suffix
           
4. Return parent event ID
```

When updating a recurring event:
- Changes apply only to future instances (start_date > NOW)
- Past instances remain unchanged

When cancelling a recurring event:
- Cancel all future instances
- Send cancellation emails to all confirmed attendees

