# Implementation Plan: Karma Platform

## Overview

This implementation plan covers building the complete Karma platform from scratch, including:
- Java 21 + Spring Boot 3.x backend with all REST APIs
- PostgreSQL database with PostGIS extension
- JWT authentication and Spring Security configuration
- Email service integration (SendGrid)
- Payment processing (Stripe)
- Scheduled jobs (weekly digest, event reminders)
- Integration between existing React frontend and new backend
- Docker setup for backend and database

The implementation follows a bottom-up approach: infrastructure → core domain models → services → REST APIs → integration → scheduled jobs.

## Tasks

- [ ] 1. Project setup and infrastructure
  - [x] 1.1 Initialize Spring Boot project with Java 21
    - Create Gradle project with Spring Boot 3.x
    - Add dependencies: Spring Web, Spring Data JPA, Spring Security, PostgreSQL driver, PostGIS, Lombok, MapStruct
    - Configure application.yml with database connection, JWT settings, CORS, server port
    - Set up project structure: controller, service, repository, entity, dto, config, exception packages
    - _Requirements: 27, 28_

  - [ ] 1.2 Set up PostgreSQL database with PostGIS
    - Create Docker Compose file for PostgreSQL 16 with PostGIS extension
    - Create database initialization script with CREATE EXTENSION postgis
    - Configure connection pooling with HikariCP
    - Set up Flyway or Liquibase for database migrations
    - _Requirements: 17, 27_

  - [x] 1.3 Configure Spring Security and JWT authentication
    - Create JwtTokenProvider utility class for token generation and validation
    - Implement JwtAuthenticationFilter for request authentication
    - Configure SecurityFilterChain with public/protected endpoints
    - Set up password encoding with BCrypt
    - Configure CORS to allow frontend origin
    - _Requirements: 1, 25, 28_


  - [ ] 1.4 Set up internationalization (i18n) support
    - Configure MessageSource bean for localized messages
    - Create messages.properties (Spanish default) and messages_en.properties
    - Implement LocaleResolver to extract locale from Accept-Language header or user preference
    - Create utility class for retrieving localized messages
    - _Requirements: 2, 26_

  - [x] 1.5 Create common exception handling and validation
    - Create custom exception classes: ResourceNotFoundException, UnauthorizedException, ValidationException
    - Implement @ControllerAdvice global exception handler
    - Configure validation with Hibernate Validator
    - Return localized error messages in API responses
    - _Requirements: 26, 28_

  - [ ] 1.6 Set up S3-compatible storage for images
    - Add AWS SDK or MinIO client dependency
    - Configure S3 bucket credentials in application.yml
    - Create FileStorageService for uploading/deleting images
    - Implement image validation (file type, size limits)
    - _Requirements: 3, 4, 22, 23_

  - [ ] 1.7 Set up geocoding provider abstraction
    - Define GeocodingService interface for address-to-coordinate resolution
    - Configure provider credentials and timeout/retry policy in application.yml
    - Implement provider adapter with graceful fallback when geocoding fails
    - Normalize address inputs before persistence to reduce duplicate locations
    - _Requirements: 11, 17, 27, 28_

- [ ] 2. Core domain entities and database schema
  - [ ] 2.1 Create User entity and repository
    - Define User entity with JPA annotations (id, email, passwordHash, firstName, lastName, avatarUrl, bio, phone, role, locale, timezone, emailVerified, timestamps)
    - Create UserRepository extending JpaRepository
    - Add custom query methods: findByEmail, existsByEmail
    - _Requirements: 1, 22_

  - [ ] 2.2 Create UserPreference entity and repository
    - Define UserPreference entity (id, userId, newsletterFreq, reviewReminders, preferredLocation, locationPoint as Point type, locationRadiusKm, timestamps)
    - Create UserPreferenceRepository
    - Configure PostGIS Point type mapping with Hibernate Spatial
    - _Requirements: 11, 9_


  - [ ] 2.3 Create Category and Theme entities
    - Define Category entity (id, nameEs, nameEn, slug, descriptionEs, descriptionEn, imageUrl, sortOrder, createdAt)
    - Define Theme entity (id, categoryId, nameEs, nameEn, slug, sortOrder, createdAt)
    - Create CategoryRepository and ThemeRepository
    - Create database seed script with all categories and themes (bilingual)
    - _Requirements: 16_

  - [ ] 2.4 Create OrganizerProfile entity and repository
    - Define OrganizerProfile entity (id, userId, name, slug, bio, website, logoUrl, verified, subscriptionTier, timestamps)
    - Create OrganizerProfileRepository
    - Add query method: findBySlug, findByUserId
    - _Requirements: 23_

  - [ ] 2.5 Create Group entity and repository
    - Define Group entity (id, organizerId, name, slug, description, categoryId, bannerUrl, city, country, locationPoint, isPrivate, status, memberCount, timestamps)
    - Create GroupRepository
    - Add custom query methods: findBySlug, findByOrganizerIdAndStatus
    - Add PostGIS spatial query method: findNearbyGroups using ST_DWithin
    - _Requirements: 3, 17_

  - [ ] 2.6 Create GroupMembership entity and repository
    - Define GroupMembership entity (id, groupId, userId, role, status, notificationPreference, joinedAt, approvedAt)
    - Use notificationPreference enum values IMMEDIATE, DIGEST, NEVER instead of a boolean flag
    - Create GroupMembershipRepository with composite unique constraint
    - Add query methods: findByGroupIdAndUserId, findByUserIdAndStatus, countByGroupIdAndStatus, findByGroupIdAndStatus
    - _Requirements: 3, 11_

  - [ ] 2.7 Create Event entity and repository
    - Define Event entity (id, organizerId, groupId, title, slug, description, coverImageUrl, startDate, endDate, venueName, address, city, country, locationPoint, isOnline, isHybrid, onlineUrl, status, eventType, recurrenceRule, recurrenceEnd, parentEventId, featured, maxAttendees, isFree, language, remindersEnabled, timestamps)
    - Create EventRepository
    - Add query methods: findBySlug, findByOrganizerIdAndStatus, findByGroupId
    - Add PostGIS spatial query: findNearbyEvents using ST_DWithin
    - _Requirements: 4, 7, 10, 17, 18_


  - [ ] 2.8 Create EventTheme join table entity
    - Define EventTheme entity for many-to-many relationship
    - Configure @ManyToMany relationship in Event entity
    - Create repository if needed for custom queries
    - _Requirements: 4, 16_

  - [ ] 2.9 Create Rsvp entity and repository
    - Define Rsvp entity (id, eventId, userId, status, waitlistPosition, checkedIn, noShow, timestamps)
    - Create RsvpRepository with composite unique constraint
    - Add query methods: findByEventIdAndUserId, findByEventIdAndStatus, countByEventIdAndStatus
    - Add waitlist query: findFirstByEventIdAndStatusOrderByWaitlistPositionAsc
    - _Requirements: 5, 14_

  - [ ] 2.10 Create TicketType entity and repository
    - Define TicketType entity (id, eventId, name, description, price, currency, quantity, soldCount, earlyBirdPrice, earlyBirdQuantity, earlyBirdDeadline, saleStart, saleEnd, timestamps)
    - Create TicketTypeRepository
    - Add query methods: findByEventId, findByEventIdAndId
    - _Requirements: 6_

  - [ ] 2.11 Create Order and OrderItem entities
    - Define Order entity (id, userId, eventId, totalAmount, currency, status, stripePaymentId, timestamps)
    - Define OrderItem entity (id, orderId, ticketTypeId, quantity, priceAtPurchase)
    - Create OrderRepository and OrderItemRepository
    - Add query methods: findByUserId, findByStripePaymentId
    - _Requirements: 6, 20_

  - [ ] 2.12 Create SavedEvent entity and repository
    - Define SavedEvent entity (id, userId, eventId, savedAt)
    - Create SavedEventRepository with composite unique constraint
    - Add query methods: findByUserId, existsByUserIdAndEventId
    - _Requirements: 12_


  - [ ] 2.13 Create Review entity and repository
    - Define Review entity (id, eventId, userId, rating, comment, timestamps)
    - Create ReviewRepository with composite unique constraint
    - Add query methods: findByEventId, findByUserIdAndEventId, calculateAverageRating
    - _Requirements: 15_

  - [ ] 2.14 Create GroupPost and GroupPostReply entities
    - Define GroupPost entity (id, groupId, authorId, content, imageUrl, isPinned, timestamps)
    - Define GroupPostReply entity (id, postId, authorId, content, createdAt)
    - Create GroupPostRepository and GroupPostReplyRepository
    - Add query methods: findByGroupIdOrderByIsPinnedDescCreatedAtDesc, countByGroupIdAndIsPinned
    - _Requirements: 8_

  - [ ] 2.15 Create BlogPost entity and repository
    - Define BlogPost entity (id, titleEs, titleEn, excerptEs, excerptEn, contentEs, contentEn, slug, coverImageUrl, published, publishedAt, timestamps)
    - Create BlogPostRepository
    - Add query methods: findBySlug, findByPublishedTrueOrderByPublishedAtDesc
    - _Requirements: 24_

  - [ ] 2.16 Create UserThemePreference join table
    - Define UserThemePreference entity for many-to-many relationship
    - Create UserThemePreferenceRepository
    - Add query method: findThemeIdsByUserId
    - _Requirements: 11_

  - [ ] 2.17 Create authentication token entities
    - Define PasswordResetToken entity (token, userId, expiryDate, usedAt, createdAt)
    - Define EmailVerificationToken entity (token, userId, expiryDate, usedAt, createdAt)
    - Create repositories with lookup and cleanup query methods
    - Add indexes for token and expiry date for efficient validation and cleanup
    - _Requirements: 1, 27_

  - [ ] 2.18 Create email and scheduler tracking entities
    - Define EmailDigestLog entity to track newsletter deliveries and last digest sent date
    - Define EventReminderLog entity to deduplicate 7-day, 1-day, and 2-hour reminders
    - Store reminder type, recipient, event, locale, status, and sent timestamp
    - Create repositories and indexes for scheduled job lookups
    - _Requirements: 9, 10, 19, 27_

- [ ] 3. Authentication and user management services
  - [x] 3.1 Implement AuthService for registration and login
    - Create AuthService with methods: register, login, refreshToken
    - Implement password hashing with BCrypt
    - Generate JWT access token (15 min expiry) and refresh token (7 days expiry)
    - Validate email format and uniqueness
    - Create UserDTO and AuthResponseDTO
    - _Requirements: 1_


  - [x] 3.2 Implement password reset functionality
    - Implement forgotPassword method to generate token and send email
    - Implement resetPassword method to validate token and update password
    - Mark tokens as used after successful reset and reject reused tokens
    - Set token expiry to 1 hour
    - _Requirements: 1_

  - [x] 3.3 Implement email verification
    - Send verification email on registration
    - Implement verifyEmail endpoint to mark email as verified
    - Mark verification tokens as used after successful confirmation
    - _Requirements: 1_

  - [ ] 3.4 Implement UserService for profile management
    - Create UserService with methods: getUserProfile, updateProfile, uploadAvatar, changePassword, changeEmail
    - Validate current password before allowing password change
    - Send verification email when email is changed
    - Use FileStorageService for avatar upload
    - _Requirements: 22_

  - [ ] 3.5 Implement UserPreferenceService
    - Create UserPreferenceService with methods: getPreferences, updatePreferences, updateThemePreferences, updateLocationPreference
    - Validate location_radius_km between 10 and 100
    - Convert address to PostGIS Point using geocoding service
    - _Requirements: 11_

- [ ] 4. Authentication REST controllers
  - [x] 4.1 Create AuthController
    - POST /api/auth/register - register new user
    - POST /api/auth/login - login with email/password
    - POST /api/auth/refresh - refresh access token
    - POST /api/auth/forgot-password - request password reset
    - POST /api/auth/reset-password - reset password with token
    - GET /api/auth/verify-email - verify email with token
    - Add request/response DTOs with validation annotations
    - _Requirements: 1_


  - [ ] 4.2 Create UserController
    - GET /api/users/me - get current user profile
    - PUT /api/users/me - update profile
    - POST /api/users/me/avatar - upload avatar image
    - PUT /api/users/me/password - change password
    - GET /api/users/me/preferences - get preferences
    - PUT /api/users/me/preferences - update preferences
    - PUT /api/users/me/preferences/themes - update theme preferences
    - PUT /api/users/me/groups/:groupId/notifications - update per-group notification preference
    - Secure all endpoints with @PreAuthorize
    - _Requirements: 22, 11, 2_

- [ ] 5. Category and theme services and controllers
  - [x] 5.1 Implement CategoryService
    - Create CategoryService with methods: getAllCategories, getCategoryBySlug, getCategoryWithThemes
    - Return localized names based on Accept-Language header or user preference
    - Cache category data with @Cacheable
    - _Requirements: 16_

  - [x] 5.2 Create CategoryController
    - GET /api/categories - list all categories with localized names
    - GET /api/categories/:slug - get category details
    - GET /api/categories/:slug/themes - get themes for category
    - Public endpoints (no authentication required)
    - _Requirements: 16_

- [ ] 6. Group management services
  - [ ] 6.1 Implement GroupService for CRUD operations
    - Create GroupService with methods: createGroup, updateGroup, archiveGroup, getGroupBySlug, findGroups
    - Generate unique slug from group name
    - Validate organizer permissions
    - Geocode address to PostGIS Point
    - Increment/decrement memberCount on membership changes
    - _Requirements: 3_

  - [ ] 6.2 Implement GroupMembershipService
    - Create GroupMembershipService with methods: joinGroup, approveMembership, leaveGroup, removeMember, updateMemberRole, updateNotificationPreference, getGroupMembers
    - Handle private group approval workflow (status PENDING → ACTIVE)
    - Validate role permissions (only organizers can remove members)
    - Allow members to configure per-group notifications as IMMEDIATE, DIGEST, or NEVER
    - Update group memberCount on membership changes
    - _Requirements: 3, 11_


  - [ ] 6.3 Implement geospatial group search
    - Add findNearbyGroups method using PostGIS ST_DWithin
    - Calculate distance using ST_Distance and return in results
    - Support filtering by category, status, privacy
    - Sort by distance or member count
    - _Requirements: 17_

  - [ ] 6.4 Implement GroupDiscussionService
    - Create GroupDiscussionService with methods: createPost, replyToPost, deletePost, pinPost, unpinPost, getGroupPosts
    - Validate user is group member before allowing post creation
    - Limit pinned posts to 3 per group
    - Allow organizers to delete any post, members only their own
    - _Requirements: 8_

- [ ] 7. Group REST controllers
  - [ ] 7.1 Create GroupController
    - GET /api/groups - list groups with filters (category, location, status)
    - GET /api/groups/nearby - geospatial search (lat, lng, radius)
    - GET /api/groups/:slug - get group details
    - POST /api/groups - create group (organizer only)
    - PUT /api/groups/:id - update group (organizer only)
    - DELETE /api/groups/:id - archive group (organizer only)
    - POST /api/groups/:id/join - join group
    - DELETE /api/groups/:id/leave - leave group
    - GET /api/groups/:id/members - get members list
    - POST /api/groups/:id/members/:userId/approve - approve pending membership (organizer only)
    - PUT /api/groups/:id/members/:userId/role - update member role (organizer only)
    - DELETE /api/groups/:id/members/:userId - remove member (organizer only)
    - PUT /api/groups/:id/notifications - update current user's group notification preference
    - _Requirements: 3, 17_

  - [ ] 7.2 Create GroupDiscussionController
    - GET /api/groups/:id/posts - get discussion posts (members only)
    - POST /api/groups/:id/posts - create post (members only)
    - POST /api/groups/:id/posts/:postId/replies - reply to post
    - DELETE /api/groups/:id/posts/:postId - delete post
    - PUT /api/groups/:id/posts/:postId/pin - pin/unpin post (organizer only)
    - _Requirements: 8_


- [ ] 8. Event management services
  - [ ] 8.1 Implement EventService for CRUD operations
    - Create EventService with methods: createEvent, updateEvent, deleteEvent, publishEvent, cancelEvent, getEventBySlug
    - Generate unique slug from event title
    - Validate organizer permissions
    - Geocode address to PostGIS Point for in-person/hybrid events
    - Validate online_url for online/hybrid events
    - Support remindersEnabled flag with organizer-managed default behavior
    - Set default status to DRAFT
    - _Requirements: 4, 10_

  - [ ] 8.2 Implement event search and filtering
    - Add findEvents method with filters: category, theme, date range, location, price, format (online/in-person/hybrid)
    - Implement full-text search on title and description
    - Support sorting by date, popularity (RSVP count), relevance
    - Return paginated results
    - _Requirements: 7_

  - [ ] 8.3 Implement geospatial event search
    - Add findNearbyEvents method using PostGIS ST_DWithin
    - Calculate distance and include in results
    - Filter by date range (upcoming events only)
    - Sort by distance or start date
    - _Requirements: 7, 17_

  - [ ] 8.4 Implement recurring event generation
    - Add generateRecurringInstances method
    - Validate recurrence_end <= 6 months from start_date
    - Generate instances based on recurrence_rule (WEEKLY, BIWEEKLY, MONTHLY)
    - Create child events with parent_event_id reference
    - Generate unique slugs with date suffix
    - _Requirements: 18_

  - [ ] 8.5 Implement event cancellation with notifications
    - Add cancelEvent method to update status to CANCELLED
    - Query all confirmed attendees (RSVP status YES or paid orders)
    - Trigger email notifications to all attendees
    - For paid events, initiate refund process
    - _Requirements: 4, 19_


- [ ] 9. RSVP system with waitlist
  - [x] 9.1 Implement RsvpService for basic RSVP operations
    - Create RsvpService with methods: createOrUpdateRsvp, cancelRsvp, getUserRsvp, getEventAttendees, getConfirmedCount
    - Validate user is not already RSVP'd before creating
    - Check event capacity before confirming RSVP
    - If at capacity, add to waitlist with position
    - Send RSVP confirmation email
    - _Requirements: 5_

  - [ ] 9.2 Implement WaitlistService for auto-promotion
    - Create WaitlistService with methods: addToWaitlist, promoteFromWaitlist, getNextWaitlistedUser, reorderWaitlist
    - When RSVP cancelled, automatically promote first waitlisted user
    - Update waitlist positions after promotion
    - Send "You're in!" email to promoted user
    - _Requirements: 5_

  - [ ] 9.3 Implement check-in and no-show tracking
    - Add checkInAttendee method to mark RSVP as checked in
    - Add markNoShow method to distinguish no-shows from cancellations
    - Only allow organizers to check in attendees
    - Add exportAttendeesCsv method with RSVP, check-in, and no-show columns
    - Calculate attendance rate for analytics
    - _Requirements: 14_

- [ ] 10. RSVP REST controllers
  - [ ] 10.1 Create RsvpController
    - POST /api/events/:id/rsvp - create or update RSVP
    - DELETE /api/events/:id/rsvp - cancel RSVP
    - GET /api/events/:id/rsvp - get current user's RSVP
    - GET /api/events/:id/attendees - get attendee list
    - GET /api/events/:id/waitlist - get waitlist
    - PUT /api/events/:id/attendees/:userId/check-in - check in attendee (organizer only)
    - GET /api/events/:id/attendees/export - export attendee list to CSV (organizer only)
    - POST /api/events/:id/waitlist/promote - manually promote from waitlist (organizer only)
    - _Requirements: 5, 14_


- [ ] 11. Ticketing and payment integration
  - [ ] 11.1 Implement TicketTypeService
    - Create TicketTypeService with methods: createTicketType, updateTicketType, deleteTicketType, getTicketTypesForEvent
    - Validate at least one ticket type for paid events
    - Check early bird pricing logic (deadline, quantity)
    - Validate quantity and price constraints
    - _Requirements: 6_

  - [ ] 11.2 Implement Stripe payment service
    - Add Stripe Java SDK dependency
    - Configure Stripe API keys in application.yml
    - Create PaymentService with methods: createCheckoutSession, confirmPayment, processRefund
    - Create Stripe checkout session with line items for each ticket type
    - Handle success/cancel redirect URLs
    - _Requirements: 20_

  - [ ] 11.3 Implement OrderService
    - Create OrderService with methods: createOrder, getOrderById, getUserOrders, refundOrder
    - Create order after successful payment confirmation
    - Decrement soldCount for each ticket type
    - Mark ticket types as sold out when quantity reached
    - Calculate total amount with early bird pricing if applicable
    - _Requirements: 6, 20_

  - [ ] 11.4 Implement Stripe webhook handler
    - Create endpoint POST /api/webhooks/stripe
    - Verify webhook signature
    - Handle payment_intent.succeeded event
    - Handle payment_intent.payment_failed event
    - Update order status based on webhook events
    - _Requirements: 20_

- [ ] 12. Payment REST controllers
  - [ ] 12.1 Create OrderController
    - POST /api/orders/checkout - create Stripe checkout session
    - GET /api/orders/:id - get order details
    - GET /api/users/me/orders - get user's orders
    - POST /api/orders/:id/refund - process refund (organizer only)
    - _Requirements: 6, 20_


- [ ] 13. Event REST controllers
  - [x] 13.1 Create EventController for public endpoints
    - GET /api/events - list events with filters (category, theme, location, date, price, format)
    - GET /api/events/search - full-text search
    - GET /api/events/nearby - geospatial search (lat, lng, radius)
    - GET /api/events/:slug - get event details
    - GET /api/events/popular - get popular events by region
    - GET /api/events/category/:slug - events by category
    - Include average rating and review count in event detail responses
    - Public endpoints (no authentication required)
    - _Requirements: 7, 15, 17_

  - [ ] 13.2 Create EventController for organizer endpoints
    - POST /api/events - create event (organizer only)
    - PUT /api/events/:id - update event (organizer only)
    - DELETE /api/events/:id - delete event (organizer only)
    - POST /api/events/:id/publish - publish draft event (organizer only)
    - POST /api/events/:id/cancel - cancel event (organizer only)
    - GET /api/events/:id/analytics - get event analytics (organizer only)
    - Validate organizer owns the event
    - _Requirements: 4, 13_

- [ ] 14. Saved events and reviews
  - [ ] 14.1 Implement SavedEventService
    - Create SavedEventService with methods: saveEvent, unsaveEvent, getUserSavedEvents, isEventSaved
    - Prevent duplicate saves
    - Support filtering by upcoming/past status
    - _Requirements: 12_

  - [ ] 14.2 Implement ReviewService
    - Create ReviewService with methods: createReview, getEventReviews, calculateAverageRating, canUserReview
    - Validate user attended the event (RSVP YES or paid order)
    - Prevent duplicate reviews
    - Calculate and cache average rating
    - _Requirements: 15_

  - [ ] 14.3 Create SavedEventController
    - GET /api/users/me/saved-events - get saved events
    - POST /api/users/me/saved-events/:eventId - save event
    - DELETE /api/users/me/saved-events/:eventId - unsave event
    - _Requirements: 12_


  - [ ] 14.4 Create ReviewController
    - GET /api/events/:id/reviews - get event reviews
    - POST /api/events/:id/reviews - create review
    - PUT /api/reviews/:id - update review
    - DELETE /api/reviews/:id - delete review
    - _Requirements: 15_

- [ ] 15. Organizer profile and dashboard
  - [ ] 15.1 Implement OrganizerProfileService
    - Create OrganizerProfileService with methods: createProfile, updateProfile, getProfileBySlug, uploadLogo
    - Generate unique slug from organizer name
    - Validate user doesn't already have organizer profile
    - Update user role to ORGANIZER on profile creation
    - _Requirements: 23_

  - [ ] 15.2 Implement OrganizerDashboardService
    - Create OrganizerDashboardService with methods: getDashboardStats, getRsvpTrends, getRevenueStats, getRecentActivity
    - Calculate upcoming events count, total RSVPs, total tickets sold, total revenue
    - Aggregate RSVP trends over time for charts
    - Calculate attendance rate (checked in / confirmed)
    - Get recent activity: new members, RSVPs, reviews
    - _Requirements: 13_

  - [ ] 15.3 Create OrganizerController
    - GET /api/organizers/:slug - get public organizer profile
    - POST /api/organizers - create organizer profile
    - PUT /api/organizers/me - update organizer profile
    - POST /api/organizers/me/logo - upload logo
    - GET /api/organizers/me/dashboard - get dashboard stats
    - GET /api/organizers/me/events - get organizer's events
    - GET /api/organizers/me/groups - get organizer's groups
    - _Requirements: 23, 13_

- [ ] 16. Email service integration
  - [ ] 16.1 Configure SendGrid integration
    - Add SendGrid Java SDK dependency
    - Configure SendGrid API key in application.yml
    - Create EmailService interface and SendGridEmailService implementation
    - Implement rate limiting (max 500 emails/hour)
    - _Requirements: 19_


  - [ ] 16.2 Create bilingual email templates
    - Create HTML email templates for Spanish and English
    - Templates: welcome, email verification, password reset, RSVP confirmation, waitlist promotion, order confirmation, event reminder, event cancellation, review request, new group event, weekly digest
    - Use Thymeleaf or FreeMarker for template rendering
    - Include unsubscribe links in all marketing emails
    - _Requirements: 2, 19_

  - [ ] 16.3 Implement transactional email methods
    - Implement sendWelcomeEmail
    - Implement sendEmailVerification
    - Implement sendPasswordResetEmail
    - Implement sendRsvpConfirmation
    - Implement sendWaitlistPromotionEmail
    - Implement sendOrderConfirmation (with e-ticket PDF)
    - Implement sendEventCancellation
    - Implement sendReviewRequest
    - Implement sendNewGroupEventNotification
    - Select template based on user's preferred locale
    - _Requirements: 19_

- [ ] 17. Weekly digest system
  - [ ] 17.1 Implement digest generation logic
    - Create DigestService with method: generateDigestForUser
    - Query events from user's joined groups (upcoming, published)
    - Query events matching user's theme preferences within location radius
    - Query popular events in user's region (by RSVP count)
    - Limit to 5 group events, 5 recommended events, 3 popular events
    - Render digest email template in user's preferred language
    - _Requirements: 9_

  - [ ] 17.2 Implement digest scheduling job
    - Create @Scheduled job to run every Monday at 9:00 AM UTC
    - Query users by newsletter_freq (WEEKLY, BIWEEKLY, MONTHLY)
    - For BIWEEKLY, check last_digest_sent date (every other Monday)
    - For MONTHLY, send on first Monday of month
    - Skip users with newsletter_freq NEVER
    - Route users with newsletter_freq KARMA_ONLY to a platform-news-only digest flow
    - Batch email sends at max 500/hour
    - Log digest sends to email_digest_log table
    - _Requirements: 9_

  - [ ] 17.3 Implement KARMA_ONLY platform news digest flow
    - Create digest branch that excludes personalized event recommendations
    - Source content from platform announcements, featured blog posts, and curated editorial picks
    - Reuse bilingual email templates and unsubscribe behavior
    - Track sends alongside the standard digest log for frequency enforcement
    - _Requirements: 9, 19, 24_


- [ ] 18. Event reminder system
  - [ ] 18.1 Implement reminder scheduling jobs
    - Create @Scheduled job for 7-day reminders (runs daily, checks events starting in 7 days)
    - Create @Scheduled job for 1-day reminders (runs daily, checks events starting in 1 day)
    - Create @Scheduled job for 2-hour reminders (runs hourly, checks events starting in 2 hours)
    - Query confirmed attendees (RSVP status YES or paid orders)
    - Skip if organizer disabled reminders for event
    - Send reminder email in user's preferred language
    - Track sent reminders to avoid duplicates
    - _Requirements: 10_

- [ ] 19. Blog and content management
  - [ ] 19.1 Implement BlogService
    - Create BlogService with methods: createPost, updatePost, publishPost, getPostBySlug, getPublishedPosts
    - Return content in user's preferred language
    - Support draft/published status
    - _Requirements: 24_

  - [ ] 19.2 Create BlogController
    - GET /api/blog - list published blog posts
    - GET /api/blog/:slug - get blog post details
    - GET /api/blog/featured - list posts eligible for KARMA_ONLY newsletter blocks
    - POST /api/blog - create blog post (admin only)
    - PUT /api/blog/:id - update blog post (admin only)
    - POST /api/blog/:id/publish - publish blog post (admin only)
    - _Requirements: 24_

- [ ] 20. Checkpoint - Core backend complete
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 21. Frontend integration with backend
  - [x] 21.1 Create API client service in frontend
    - Create axios instance with base URL and interceptors
    - Add request interceptor to attach JWT token from localStorage
    - Add response interceptor to handle 401 errors and refresh token
    - Create API methods for all backend endpoints
    - Handle CORS and error responses
    - _Requirements: 1, 25_


  - [x] 21.2 Replace mock data with real API calls in event pages
    - Update EventListPage to call GET /api/events with filters
    - Update EventDetailPage to call GET /api/events/:slug
    - Update RSVP functionality to call POST /api/events/:id/rsvp
    - Update saved events to call POST /api/users/me/saved-events/:eventId
    - Handle loading states and error messages
    - _Requirements: 4, 5, 7, 12_

  - [x] 21.3 Replace mock data with real API calls in group pages
    - Update GroupListPage to call GET /api/groups with filters
    - Update GroupDetailPage to call GET /api/groups/:slug
    - Update join/leave functionality to call POST /api/groups/:id/join
    - Update discussion posts to call GET /api/groups/:id/posts
    - _Requirements: 3, 8_

  - [x] 21.4 Implement authentication flow in frontend
    - Create login page calling POST /api/auth/login
    - Create registration page calling POST /api/auth/register
    - Store JWT tokens in localStorage
    - Implement token refresh logic
    - Create protected route wrapper component
    - Implement email verification and password reset screens
    - Add logout functionality
    - _Requirements: 1_

  - [x] 21.5 Implement user account pages
    - Update account details page to call GET /api/users/me and PUT /api/users/me
    - Update preferences page to call GET /api/users/me/preferences and PUT /api/users/me/preferences
    - Add UI for theme preferences, language preference, and newsletter frequency
    - Add UI for per-group notification mode (IMMEDIATE, DIGEST, NEVER)
    - Update saved events page to call GET /api/users/me/saved-events
    - Update orders page to call GET /api/users/me/orders
    - _Requirements: 22, 11, 12, 2_

  - [ ] 21.6 Implement organizer dashboard
    - Create organizer registration page calling POST /api/organizers
    - Update dashboard page to call GET /api/organizers/me/dashboard
    - Create event creation form calling POST /api/events
    - Create event management page calling GET /api/organizers/me/events
    - Implement attendee management calling GET /api/events/:id/attendees
    - _Requirements: 23, 13, 4_


  - [ ] 21.7 Implement Stripe checkout flow in frontend
    - Add Stripe.js library to frontend
    - Create checkout page that calls POST /api/orders/checkout
    - Redirect to Stripe checkout session
    - Handle success/cancel redirects
    - Display order confirmation page
    - _Requirements: 20_

  - [ ] 21.8 Implement geospatial search UI
    - Add location input with autocomplete
    - Add radius slider (10-100 km)
    - Call GET /api/events/nearby with lat, lng, radius
    - Display results on map (optional: integrate Mapbox or Google Maps)
    - Show distance from user's location
    - _Requirements: 7, 17_

  - [ ] 21.9 Implement frontend internationalization and localized routing
    - Configure i18n resources with Spanish default and English secondary locale
    - Add language switcher and persist anonymous preference in cookies
    - Add locale-prefixed routes (for example /es/eventos and /en/events)
    - Render hreflang metadata for localized pages and SEO
    - Sync logged-in language changes with backend profile preferences
    - _Requirements: 2_

  - [ ] 21.10 Implement organizer attendee operations in frontend
    - Add attendee check-in UI for organizers
    - Add no-show visibility in attendee lists
    - Add CSV export action for attendee management
    - Add reminder toggle control in event settings
    - _Requirements: 10, 14_

- [ ] 22. Docker and deployment setup
  - [ ] 22.1 Create Dockerfile for Spring Boot backend
    - Use multi-stage build with Gradle
    - Base image: eclipse-temurin:21-jre-alpine
    - Expose port 8080
    - Set environment variables for database, Stripe, SendGrid
    - _Requirements: 27_

  - [ ] 22.2 Update Docker Compose for full stack
    - Add backend service with build context
    - Add PostgreSQL service with PostGIS
    - Add volume mounts for database persistence
    - Configure network for service communication
    - Add environment variables file (.env)
    - _Requirements: 27_

  - [ ] 22.3 Create database migration scripts
    - Create Flyway/Liquibase migration files for all tables
    - Include PostGIS extension creation
    - Include indexes creation
    - Include seed data for categories and themes
    - Version migrations properly (V1__initial_schema.sql, V2__add_indexes.sql, etc.)
    - _Requirements: 16, 17_


- [ ] 23. Security hardening and performance optimization
  - [ ] 23.1 Implement rate limiting
    - Add rate limiting filter using Bucket4j or similar
    - Limit to 100 requests per minute per IP
    - Return 429 Too Many Requests when exceeded
    - _Requirements: 28_

  - [ ] 23.2 Implement input sanitization
    - Add HTML sanitization for user-generated content (descriptions, posts, comments)
    - Use OWASP Java HTML Sanitizer
    - Prevent XSS attacks
    - _Requirements: 28_

  - [ ] 23.3 Add database query optimization
    - Review N+1 query problems and add @EntityGraph where needed
    - Add database indexes for frequently queried fields
    - Implement pagination for all list endpoints
    - Add query result caching for categories, themes
    - _Requirements: 27_

  - [ ] 23.4 Configure HTTPS and CORS
    - Configure SSL/TLS certificates for production
    - Set up CORS to allow only frontend domain
    - Configure secure headers (HSTS, X-Frame-Options, CSP)
    - _Requirements: 28_

- [ ] 24. Testing and validation
  - [ ]* 24.1 Write unit tests for services
    - Test AuthService: registration, login, token generation
    - Test RsvpService: RSVP creation, waitlist logic
    - Test WaitlistService: auto-promotion algorithm
    - Test EventService: event creation, recurring generation
    - Test PaymentService: checkout session creation
    - Use JUnit 5 and Mockito
    - _Requirements: 1, 5, 4, 20_

  - [ ]* 24.2 Write integration tests for REST APIs
    - Test authentication endpoints with valid/invalid credentials
    - Test event CRUD operations with authorization
    - Test RSVP flow with capacity limits
    - Test payment flow with Stripe test mode
    - Use Spring Boot Test and TestRestTemplate
    - _Requirements: 1, 4, 5, 20_

  - [x] 24.2.1 Write authentication integration tests
    - Test authentication endpoints with register, login, email verification, forgot password, and reset password flows
    - Validate token-bearing responses and successful credential updates with MockMvc
    - _Requirements: 1_


  - [ ]* 24.3 Write tests for scheduled jobs
    - Test weekly digest generation logic
    - Test event reminder scheduling
    - Test email batching and rate limiting
    - Mock email service to avoid sending real emails
    - _Requirements: 9, 10_

  - [ ]* 24.4 Test geospatial queries
    - Test ST_DWithin queries with various radii
    - Test distance calculations
    - Verify results are sorted by distance
    - Test edge cases (events at exact radius boundary)
    - _Requirements: 17_

  - [ ]* 24.5 Test notification preferences and reminder suppression
    - Test per-group notification preference persistence and filtering
    - Test KARMA_ONLY digest branching logic
    - Test event reminder suppression when reminders are disabled
    - Test CSV attendee export authorization and output columns
    - _Requirements: 9, 10, 11, 14_

- [ ] 25. Final integration and deployment
  - [ ] 25.1 End-to-end testing
    - Test complete user registration → event discovery → RSVP flow
    - Test organizer registration → group creation → event creation flow
    - Test ticket purchase → payment → order confirmation flow
    - Test weekly digest generation and email delivery
    - Test event reminders at different intervals
    - _Requirements: 1, 3, 4, 5, 6, 9, 10_

  - [ ] 25.2 Performance testing
    - Load test event listing endpoint with 1000+ events
    - Load test geospatial search with various radii
    - Test weekly digest generation with 10,000+ users
    - Verify response times meet requirements (<500ms for event listing)
    - _Requirements: 27_

  - [ ] 25.3 Documentation
    - Create API documentation with Swagger/OpenAPI
    - Document environment variables and configuration
    - Create deployment guide for Docker
    - Document database schema and migrations
    - Create developer setup guide
    - _Requirements: All_

  - [ ] 25.4 Deploy to production
    - Set up production database with backups
    - Configure production environment variables
    - Deploy backend container
    - Configure domain and SSL certificates
    - Set up monitoring and logging
    - Test all critical flows in production
    - _Requirements: All_


- [ ] 26. Final checkpoint - Complete platform ready
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional testing tasks and can be skipped for faster MVP delivery
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at major milestones
- The implementation follows a bottom-up approach: infrastructure → domain models → services → controllers → integration
- PostGIS geospatial queries are critical for location-based event discovery
- JWT authentication with refresh tokens provides secure stateless authentication
- Weekly digest and reminder systems drive user engagement and retention
- Stripe integration handles all payment processing securely
- Bilingual support (Spanish/English) is implemented throughout backend and frontend
- Docker setup enables consistent development and production environments
