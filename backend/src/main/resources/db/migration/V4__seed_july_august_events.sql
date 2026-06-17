-- Sample events for July/August 2026 (extends base seed data)

INSERT INTO events (
    id, organizer_id, group_id, title, slug, description, cover_image_url,
    start_date, end_date, venue_name, address, city, country,
    latitude, longitude, is_online, is_hybrid, online_url,
    status, featured, max_attendees, is_free, price, currency, language,
    category_id, reminders_enabled, created_at, updated_at
) VALUES
(
    'event-july-1', 'org-1', 'group-1',
    'Sunset Ecstatic Dance', 'sunset-ecstatic-dance-july',
    'Sesion de danza consciente al atardecer con DJ set organico y cierre en silencio.',
    'https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=600&h=400&fit=crop',
    TIMESTAMP '2026-07-11 19:00:00', TIMESTAMP '2026-07-11 22:00:00',
    'Espacio Matadero', NULL, 'Madrid', 'Espana', 40.4168, -3.7038,
    FALSE, FALSE, NULL, 'PUBLISHED', TRUE, 50, FALSE, 24.0, 'EUR', 'es',
    'cat-dance', TRUE, TIMESTAMP '2026-03-01 10:00:00', TIMESTAMP '2026-03-01 10:00:00'
),
(
    'event-july-2', 'org-2', 'group-2',
    'Urban Breathwork Lab', 'urban-breathwork-july',
    'Laboratorio guiado de respiracion, regulacion del sistema nervioso y journaling.',
    'https://images.unsplash.com/photo-1528495612343-9ca9f755e7bc?w=600&h=400&fit=crop',
    TIMESTAMP '2026-07-18 10:00:00', TIMESTAMP '2026-07-18 13:30:00',
    'Casa Virupa', NULL, 'Barcelona', 'Espana', 41.3874, 2.1686,
    FALSE, FALSE, NULL, 'PUBLISHED', FALSE, 30, FALSE, 32.0, 'EUR', 'es',
    'cat-workshops', TRUE, TIMESTAMP '2026-03-01 10:00:00', TIMESTAMP '2026-03-01 10:00:00'
),
(
    'event-july-3', 'org-2', NULL,
    'Mantra Night Online', 'mantra-night-july',
    'Circulo digital de canto y meditacion para comunidad hispanohablante.',
    'https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=600&h=400&fit=crop',
    TIMESTAMP '2026-07-24 20:00:00', TIMESTAMP '2026-07-24 21:30:00',
    NULL, NULL, 'Online', '', 0, 0,
    TRUE, FALSE, 'https://karma.app/live/mantra-night', 'PUBLISHED', FALSE, NULL, TRUE, NULL, NULL, 'es',
    'cat-music', TRUE, TIMESTAMP '2026-03-01 10:00:00', TIMESTAMP '2026-03-01 10:00:00'
),
(
    'event-august-1', 'org-1', 'group-1',
    'Summer Embodiment Retreat', 'summer-embodiment-retreat-august',
    'Fin de semana inmersivo con yoga suave, danza, cacao y banos de sonido.',
    'https://images.unsplash.com/photo-1528495612343-9ca9f755e7bc?w=600&h=400&fit=crop',
    TIMESTAMP '2026-08-07 17:00:00', TIMESTAMP '2026-08-09 16:00:00',
    'Finca El Bosque', NULL, 'Sierra de Madrid', 'Espana', 40.4168, -3.7038,
    FALSE, FALSE, NULL, 'PUBLISHED', TRUE, 20, FALSE, 185.0, 'EUR', 'es',
    'cat-retreats', TRUE, TIMESTAMP '2026-03-01 10:00:00', TIMESTAMP '2026-03-01 10:00:00'
),
(
    'event-august-2', 'org-2', 'group-2',
    'Park Yoga Sunrise', 'park-yoga-sunrise-august',
    'Clase abierta al amanecer con meditacion breve y picnic comunitario.',
    'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=600&h=400&fit=crop',
    TIMESTAMP '2026-08-16 07:30:00', TIMESTAMP '2026-08-16 09:00:00',
    'Parc de la Ciutadella', NULL, 'Barcelona', 'Espana', 41.3874, 2.1686,
    FALSE, FALSE, NULL, 'PUBLISHED', FALSE, 60, TRUE, NULL, NULL, 'es',
    'cat-workshops', TRUE, TIMESTAMP '2026-03-01 10:00:00', TIMESTAMP '2026-03-01 10:00:00'
),
(
    'event-august-3', 'org-1', NULL,
    'Sound Healing Portal', 'sound-healing-portal-august',
    'Viaje inmersivo con cuencos, voz y ambient meditation.',
    'https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=600&h=400&fit=crop',
    TIMESTAMP '2026-08-28 19:30:00', TIMESTAMP '2026-08-28 21:30:00',
    'Lumen Studio', NULL, 'Valencia', 'Espana', 39.4699, -0.3763,
    FALSE, FALSE, NULL, 'PUBLISHED', FALSE, 40, FALSE, 28.0, 'EUR', 'es',
    'cat-music', TRUE, TIMESTAMP '2026-03-01 10:00:00', TIMESTAMP '2026-03-01 10:00:00'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO event_themes (event_id, theme_id) VALUES
    ('event-july-1', 'theme-ecstatic'),
    ('event-july-2', 'theme-breathwork'),
    ('event-july-3', 'theme-kirtan'),
    ('event-august-1', 'theme-yoga'),
    ('event-august-1', 'theme-cacao'),
    ('event-august-2', 'theme-yoga'),
    ('event-august-2', 'theme-meditation'),
    ('event-august-3', 'theme-sound')
ON CONFLICT DO NOTHING;
