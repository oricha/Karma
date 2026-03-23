package com.karma.platform.seed;

import com.karma.platform.model.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlatformDataStore {

    private record TimedToken(String userId, LocalDateTime expiresAt) {
    }

    private final PasswordEncoder passwordEncoder;

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, UserPreference> preferences = new ConcurrentHashMap<>();
    private final Map<String, Category> categories = new LinkedHashMap<>();
    private final Map<String, Theme> themes = new LinkedHashMap<>();
    private final Map<String, OrganizerProfile> organizers = new LinkedHashMap<>();
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, GroupMembership> memberships = new ConcurrentHashMap<>();
    private final Map<String, Event> events = new LinkedHashMap<>();
    private final Map<String, Rsvp> rsvps = new ConcurrentHashMap<>();
    private final Map<String, SavedEvent> savedEvents = new ConcurrentHashMap<>();
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final Map<String, BlogPost> blogPosts = new LinkedHashMap<>();
    private final Map<String, String> refreshTokens = new ConcurrentHashMap<>();
    private final Map<String, TimedToken> passwordResetTokens = new ConcurrentHashMap<>();
    private final Map<String, TimedToken> emailVerificationTokens = new ConcurrentHashMap<>();

    public PlatformDataStore(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        seed();
    }

    public synchronized User register(String email, String password, String firstName, String lastName) {
        User user = new User(id(), email, passwordEncoder.encode(password), firstName, lastName, null, null, null, UserRole.USER, "es", false);
        users.put(user.id(), user);
        preferences.put(user.id(), new UserPreference(user.id(), NewsletterFrequency.WEEKLY, true, "Madrid", 40.4168, -3.7038, 50, List.of("theme-yoga", "theme-ecstatic")));
        return user;
    }

    public Optional<User> findUserByEmail(String email) {
        return users.values().stream().filter(user -> user.email().equalsIgnoreCase(email)).findFirst();
    }

    public Optional<User> findUserById(String userId) {
        return Optional.ofNullable(users.get(userId));
    }

    public synchronized void saveUser(User user) {
        users.put(user.id(), user);
    }

    public UserPreference getPreference(String userId) {
        return preferences.get(userId);
    }

    public synchronized void savePreference(UserPreference preference) {
        preferences.put(preference.userId(), preference);
    }

    public List<Category> categories() {
        return new ArrayList<>(categories.values());
    }

    public Optional<Category> categoryBySlug(String slug) {
        return categories.values().stream().filter(category -> category.slug().equals(slug)).findFirst();
    }

    public List<Theme> themes() {
        return new ArrayList<>(themes.values());
    }

    public List<Theme> themesByCategory(String categoryId) {
        return themes.values().stream().filter(theme -> theme.categoryId().equals(categoryId)).toList();
    }

    public List<Group> groups() {
        return new ArrayList<>(groups.values());
    }

    public Optional<Group> groupBySlug(String slug) {
        return groups.values().stream().filter(group -> group.slug().equals(slug)).findFirst();
    }

    public Optional<Group> groupById(String id) {
        return Optional.ofNullable(groups.get(id));
    }

    public OrganizerProfile organizerById(String id) {
        return organizers.get(id);
    }

    public Optional<OrganizerProfile> organizerByUserId(String userId) {
        return organizers.values().stream().filter(organizer -> organizer.userId().equals(userId)).findFirst();
    }

    public List<Event> events() {
        return new ArrayList<>(events.values());
    }

    public Optional<Event> eventBySlug(String slug) {
        return events.values().stream().filter(event -> event.slug().equals(slug)).findFirst();
    }

    public Optional<Event> eventById(String id) {
        return Optional.ofNullable(events.get(id));
    }

    public List<Event> eventsByGroup(String groupId) {
        return events.values().stream()
                .filter(event -> Objects.equals(event.groupId(), groupId))
                .sorted(Comparator.comparing(Event::startDate))
                .toList();
    }

    public List<Rsvp> rsvpsForEvent(String eventId) {
        return rsvps.values().stream().filter(rsvp -> rsvp.eventId().equals(eventId)).toList();
    }

    public Optional<Rsvp> userRsvp(String eventId, String userId) {
        return rsvps.values().stream()
                .filter(rsvp -> rsvp.eventId().equals(eventId) && rsvp.userId().equals(userId))
                .findFirst();
    }

    public synchronized Rsvp saveRsvp(Rsvp rsvp) {
        rsvps.put(rsvp.id(), rsvp);
        return rsvp;
    }

    public synchronized void deleteRsvp(String id) {
        rsvps.remove(id);
    }

    public List<GroupMembership> membershipsByUser(String userId) {
        return memberships.values().stream().filter(membership -> membership.userId().equals(userId)).toList();
    }

    public List<GroupMembership> membershipsByGroup(String groupId) {
        return memberships.values().stream().filter(membership -> membership.groupId().equals(groupId)).toList();
    }

    public Optional<GroupMembership> membership(String groupId, String userId) {
        return memberships.values().stream()
                .filter(membership -> membership.groupId().equals(groupId) && membership.userId().equals(userId))
                .findFirst();
    }

    public synchronized GroupMembership saveMembership(GroupMembership membership) {
        memberships.put(membership.id(), membership);
        return membership;
    }

    public synchronized void deleteMembership(String id) {
        memberships.remove(id);
    }

    public List<SavedEvent> savedEventsByUser(String userId) {
        return savedEvents.values().stream()
                .filter(savedEvent -> savedEvent.userId().equals(userId))
                .sorted(Comparator.comparing(SavedEvent::savedAt).reversed())
                .toList();
    }

    public Optional<SavedEvent> savedEvent(String userId, String eventId) {
        return savedEvents.values().stream()
                .filter(savedEvent -> savedEvent.userId().equals(userId) && savedEvent.eventId().equals(eventId))
                .findFirst();
    }

    public synchronized SavedEvent saveSavedEvent(SavedEvent savedEvent) {
        savedEvents.put(savedEvent.id(), savedEvent);
        return savedEvent;
    }

    public synchronized void deleteSavedEvent(String id) {
        savedEvents.remove(id);
    }

    public List<Order> ordersByUser(String userId) {
        return orders.values().stream()
                .filter(order -> order.userId().equals(userId))
                .sorted(Comparator.comparing(Order::purchasedAt).reversed())
                .toList();
    }

    public Optional<Order> orderById(String id) {
        return Optional.ofNullable(orders.get(id));
    }

    public synchronized Order saveOrder(Order order) {
        orders.put(order.id(), order);
        return order;
    }

    public List<BlogPost> blogPosts() {
        return new ArrayList<>(blogPosts.values());
    }

    public Optional<BlogPost> blogPostBySlug(String slug) {
        return blogPosts.values().stream().filter(blogPost -> blogPost.slug().equals(slug)).findFirst();
    }

    public void storeRefreshToken(String token, String userId) {
        refreshTokens.put(token, userId);
    }

    public Optional<String> consumeRefreshTokenOwner(String token) {
        return Optional.ofNullable(refreshTokens.get(token));
    }

    public String createPasswordResetToken(String userId) {
        String token = UUID.randomUUID().toString();
        passwordResetTokens.put(token, new TimedToken(userId, LocalDateTime.now().plusHours(1)));
        return token;
    }

    public Optional<String> consumePasswordResetToken(String token) {
        TimedToken timedToken = passwordResetTokens.remove(token);
        if (timedToken == null || timedToken.expiresAt().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }
        return Optional.of(timedToken.userId());
    }

    public String createEmailVerificationToken(String userId) {
        String token = UUID.randomUUID().toString();
        emailVerificationTokens.put(token, new TimedToken(userId, LocalDateTime.now().plusDays(2)));
        return token;
    }

    public Optional<String> consumeEmailVerificationToken(String token) {
        TimedToken timedToken = emailVerificationTokens.remove(token);
        if (timedToken == null || timedToken.expiresAt().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }
        return Optional.of(timedToken.userId());
    }

    public int attendeeCount(String eventId) {
        return (int) rsvps.values().stream()
                .filter(rsvp -> rsvp.eventId().equals(eventId) && rsvp.status() == RsvpStatus.YES)
                .count();
    }

    public String id() {
        return UUID.randomUUID().toString();
    }

    private void seed() {
        Category workshops = new Category("cat-workshops", "Talleres", "Workshops", "talleres", "Talleres de bienestar", "Wellness workshops", "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=400&h=300&fit=crop", 128);
        Category ceremonies = new Category("cat-ceremonies", "Ceremonias", "Ceremonies", "ceremonias", "Ceremonias conscientes", "Conscious ceremonies", "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400&h=300&fit=crop", 56);
        Category dance = new Category("cat-dance", "Danza", "Dance", "danza", "Danza y movimiento", "Dance and movement", "https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=400&h=300&fit=crop", 89);
        Category music = new Category("cat-music", "Música", "Music", "musica", "Música y vibración", "Music and vibration", "https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=400&h=300&fit=crop", 34);
        Category retreats = new Category("cat-retreats", "Festivales y Retiros", "Festivals & Retreats", "festivales-retiros", "Experiencias inmersivas", "Immersive experiences", "https://images.unsplash.com/photo-1528495612343-9ca9f755e7bc?w=400&h=300&fit=crop", 22);
        List.of(workshops, ceremonies, dance, music, retreats).forEach(category -> categories.put(category.id(), category));

        addTheme(new Theme("theme-yoga", workshops.id(), "Yoga", "Yoga", "yoga"));
        addTheme(new Theme("theme-meditation", workshops.id(), "Meditación", "Meditation", "meditacion"));
        addTheme(new Theme("theme-ecstatic", dance.id(), "Danza Extática", "Ecstatic Dance", "danza-extatica"));
        addTheme(new Theme("theme-tantra", workshops.id(), "Tantra", "Tantra", "tantra"));
        addTheme(new Theme("theme-cacao", ceremonies.id(), "Cacao", "Cacao", "cacao"));
        addTheme(new Theme("theme-kirtan", music.id(), "Kirtan", "Kirtan", "kirtan"));
        addTheme(new Theme("theme-sound", music.id(), "Sound Healing", "Sound Healing", "sound-healing"));
        addTheme(new Theme("theme-breathwork", workshops.id(), "Breathwork", "Breathwork", "breathwork"));

        User organizerUser1 = new User("user-1", "maria@karma.app", passwordEncoder.encode("password123"), "María", "Luna", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop&crop=face", "Facilitadora de danza consciente y ceremonias de cacao.", "+34111111111", UserRole.ORGANIZER, "es", true);
        User organizerUser2 = new User("user-2", "carlos@karma.app", passwordEncoder.encode("password123"), "Carlos", "Sánchez", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=face", "Profesor de yoga y meditación.", "+34222222222", UserRole.ORGANIZER, "es", true);
        User demoUser = new User("user-3", "demo@karma.app", passwordEncoder.encode("demo123"), "Demo", "User", null, "Explorando eventos conscientes.", "+34999999999", UserRole.USER, "es", true);
        List.of(organizerUser1, organizerUser2, demoUser).forEach(user -> users.put(user.id(), user));

        preferences.put(demoUser.id(), new UserPreference(demoUser.id(), NewsletterFrequency.WEEKLY, true, "Madrid", 40.4168, -3.7038, 50, List.of("theme-yoga", "theme-ecstatic")));
        preferences.put(organizerUser1.id(), new UserPreference(organizerUser1.id(), NewsletterFrequency.MONTHLY, true, "Madrid", 40.4168, -3.7038, 30, List.of("theme-cacao", "theme-ecstatic")));
        preferences.put(organizerUser2.id(), new UserPreference(organizerUser2.id(), NewsletterFrequency.KARMA_ONLY, false, "Barcelona", 41.3874, 2.1686, 40, List.of("theme-yoga", "theme-meditation")));

        OrganizerProfile maria = new OrganizerProfile("org-1", organizerUser1.id(), "María Luna", "maria-luna", "Facilitadora de danza consciente y ceremonias de cacao con más de 10 años de experiencia.", null, organizerUser1.avatarUrl(), true);
        OrganizerProfile carlos = new OrganizerProfile("org-2", organizerUser2.id(), "Carlos Sánchez", "carlos-sanchez", "Profesor de yoga y meditación.", null, organizerUser2.avatarUrl(), false);
        organizers.put(maria.id(), maria);
        organizers.put(carlos.id(), carlos);

        Group group1 = new Group("group-1", maria.id(), "Ecstatic Dance Madrid", "ecstatic-dance-madrid", "Comunidad de danza libre y consciente en Madrid.", dance.id(), "https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=800&h=300&fit=crop", "Madrid", "España", 40.4168, -3.7038, false, GroupStatus.ACTIVE, 342);
        Group group2 = new Group("group-2", maria.id(), "Yoga en el Parque BCN", "yoga-parque-bcn", "Sesiones de yoga gratuitas al aire libre en Barcelona.", workshops.id(), "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800&h=300&fit=crop", "Barcelona", "España", 41.3874, 2.1686, false, GroupStatus.ACTIVE, 189);
        Group group3 = new Group("group-3", carlos.id(), "Círculo de Cacao Valencia", "circulo-cacao-valencia", "Ceremonias de cacao y círculos de compartir en Valencia.", ceremonies.id(), "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=800&h=300&fit=crop", "Valencia", "España", 39.4699, -0.3763, false, GroupStatus.ACTIVE, 98);
        List.of(group1, group2, group3).forEach(group -> groups.put(group.id(), group));

        memberships.put("membership-1", new GroupMembership("membership-1", group1.id(), demoUser.id(), "MEMBER", "ACTIVE", GroupNotificationPreference.IMMEDIATE));
        memberships.put("membership-2", new GroupMembership("membership-2", group2.id(), demoUser.id(), "MEMBER", "ACTIVE", GroupNotificationPreference.DIGEST));

        Event event1 = new Event("event-1", maria.id(), group1.id(), "Danza Extática al Atardecer", "danza-extatica-atardecer", "Una experiencia de movimiento libre y consciente acompañada de música envolvente.", "https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=600&h=400&fit=crop", LocalDateTime.of(2026, 4, 5, 18, 0), LocalDateTime.of(2026, 4, 5, 21, 0), "Espacio Gaia", "Calle del Sol 12", "Madrid", "España", 40.4168, -3.7038, false, false, null, EventStatus.PUBLISHED, true, 50, true, null, null, "es", List.of("theme-ecstatic"), dance.id(), true);
        Event event2 = new Event("event-2", maria.id(), group3.id(), "Ceremonia de Cacao Sagrado", "ceremonia-cacao-sagrado", "Círculo de cacao y música medicina.", "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600&h=400&fit=crop", LocalDateTime.of(2026, 4, 8, 19, 0), LocalDateTime.of(2026, 4, 8, 22, 0), "Casa del Alma", null, "Barcelona", "España", 41.3874, 2.1686, false, false, null, EventStatus.PUBLISHED, false, 25, false, 35.0, "EUR", "es", List.of("theme-cacao"), ceremonies.id(), true);
        Event event3 = new Event("event-3", carlos.id(), group2.id(), "Retiro de Yoga y Meditación", "retiro-yoga-meditacion", "Dos días de práctica, descanso y comunidad.", "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=600&h=400&fit=crop", LocalDateTime.of(2026, 4, 12, 9, 0), LocalDateTime.of(2026, 4, 13, 18, 0), "Finca La Paz", null, "Granada", "España", 37.1773, -3.5986, false, false, null, EventStatus.PUBLISHED, true, 20, false, 120.0, "EUR", "es", List.of("theme-yoga", "theme-meditation"), retreats.id(), true);
        Event event4 = new Event("event-4", carlos.id(), null, "Kirtan: Cantos de Mantras", "kirtan-cantos-mantras", "Una noche para cantar y conectar.", "https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=600&h=400&fit=crop", LocalDateTime.of(2026, 4, 6, 20, 0), LocalDateTime.of(2026, 4, 6, 22, 30), "Centro Ananda", null, "Valencia", "España", 39.4699, -0.3763, false, false, null, EventStatus.PUBLISHED, false, 40, true, null, null, "es", List.of("theme-kirtan"), music.id(), true);
        Event event5 = new Event("event-5", maria.id(), group2.id(), "Taller de Trabajo de Respiración", "taller-breathwork", "Práctica guiada de respiración consciente.", "https://images.unsplash.com/photo-1528495612343-9ca9f755e7bc?w=600&h=400&fit=crop", LocalDateTime.of(2026, 4, 10, 10, 0), LocalDateTime.of(2026, 4, 10, 13, 0), "Espacio Gaia", null, "Madrid", "España", 40.4168, -3.7038, false, false, null, EventStatus.PUBLISHED, false, 30, false, 25.0, "EUR", "es", List.of("theme-breathwork"), workshops.id(), true);
        Event event6 = new Event("event-6", carlos.id(), null, "Meditación Guiada Online", "meditacion-guiada-online", "Sesión online para empezar el día centrado.", "https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=600&h=400&fit=crop", LocalDateTime.of(2026, 4, 7, 8, 0), LocalDateTime.of(2026, 4, 7, 9, 0), null, null, "Online", "", 0, 0, true, false, "https://karma.app/live/meditacion", EventStatus.PUBLISHED, false, null, true, null, null, "es", List.of("theme-meditation"), workshops.id(), true);
        List.of(event1, event2, event3, event4, event5, event6).forEach(event -> events.put(event.id(), event));

        rsvps.put("rsvp-1", new Rsvp("rsvp-1", event1.id(), demoUser.id(), RsvpStatus.YES, null, false, false));
        rsvps.put("rsvp-2", new Rsvp("rsvp-2", event6.id(), demoUser.id(), RsvpStatus.YES, null, false, false));
        savedEvents.put("saved-1", new SavedEvent("saved-1", demoUser.id(), event2.id(), LocalDateTime.now().minusDays(2)));
        savedEvents.put("saved-2", new SavedEvent("saved-2", demoUser.id(), event3.id(), LocalDateTime.now().minusDays(1)));
        orders.put("order-1", new Order("order-1", demoUser.id(), event3.id(), OrderStatus.PAID, 120.0, "EUR", LocalDateTime.now().minusDays(14)));

        blogPosts.put("blog-1", new BlogPost("blog-1", "5 Beneficios de la Danza Extática", "5 Benefits of Ecstatic Dance", "5-beneficios-danza-extatica", "Descubre cómo la danza libre puede transformar tu bienestar físico y emocional.", "Discover how free dance can transform your physical and emotional well-being.", "https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=400&h=250&fit=crop", LocalDate.of(2026, 3, 15)));
        blogPosts.put("blog-2", new BlogPost("blog-2", "Guía para tu Primera Ceremonia de Cacao", "Guide to Your First Cacao Ceremony", "guia-primera-ceremonia-cacao", "Todo lo que necesitas saber antes de asistir a una ceremonia de cacao sagrado.", "Everything you need to know before attending a sacred cacao ceremony.", "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400&h=250&fit=crop", LocalDate.of(2026, 3, 10)));
    }

    private void addTheme(Theme theme) {
        themes.put(theme.id(), theme);
    }
}
