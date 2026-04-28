insert into app_users (id, email, password_hash, first_name, last_name, avatar_url, bio, phone, role, locale, email_verified, created_at, updated_at) values
('user-1', 'maria@karma.app', '{noop}password123', 'Maria', 'Luna', 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop&crop=face', 'Facilitadora de danza consciente y ceremonias de cacao.', '+34111111111', 'ORGANIZER', 'es', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('user-2', 'carlos@karma.app', '{noop}password123', 'Carlos', 'Sanchez', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=face', 'Profesor de yoga y meditacion.', '+34222222222', 'ORGANIZER', 'es', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('user-3', 'demo@karma.app', '{noop}demo123', 'Demo', 'User', null, 'Explorando eventos conscientes.', '+34999999999', 'USER', 'es', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00')
on conflict (id) do nothing;

insert into user_preferences (user_id, newsletter_frequency, review_reminders, preferred_location, latitude, longitude, location_radius_km, created_at, updated_at) values
('user-1', 'MONTHLY', true, 'Madrid', 40.4168, -3.7038, 30, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('user-2', 'KARMA_ONLY', false, 'Barcelona', 41.3874, 2.1686, 40, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('user-3', 'WEEKLY', true, 'Madrid', 40.4168, -3.7038, 50, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00')
on conflict (user_id) do nothing;

insert into categories (id, slug, name_es, name_en, description_es, description_en, image_url, event_count, sort_order, created_at, updated_at) values
('cat-workshops', 'talleres', 'Talleres', 'Workshops', 'Talleres de bienestar', 'Wellness workshops', 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=400&h=300&fit=crop', 128, 1, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('cat-ceremonies', 'ceremonias', 'Ceremonias', 'Ceremonies', 'Ceremonias conscientes', 'Conscious ceremonies', 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400&h=300&fit=crop', 56, 2, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('cat-dance', 'danza', 'Danza', 'Dance', 'Danza y movimiento', 'Dance and movement', 'https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=400&h=300&fit=crop', 89, 3, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('cat-music', 'musica', 'Musica', 'Music', 'Musica y vibracion', 'Music and vibration', 'https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=400&h=300&fit=crop', 34, 4, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('cat-retreats', 'festivales-retiros', 'Festivales y Retiros', 'Festivals & Retreats', 'Experiencias inmersivas', 'Immersive experiences', 'https://images.unsplash.com/photo-1528495612343-9ca9f755e7bc?w=400&h=300&fit=crop', 22, 5, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00')
on conflict (id) do nothing;

insert into themes (id, category_id, name_es, name_en, slug, sort_order, created_at, updated_at) values
('theme-yoga', 'cat-workshops', 'Yoga', 'Yoga', 'yoga', 1, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('theme-meditation', 'cat-workshops', 'Meditacion', 'Meditation', 'meditacion', 2, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('theme-ecstatic', 'cat-dance', 'Danza Extatica', 'Ecstatic Dance', 'danza-extatica', 3, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('theme-tantra', 'cat-workshops', 'Tantra', 'Tantra', 'tantra', 4, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('theme-cacao', 'cat-ceremonies', 'Cacao', 'Cacao', 'cacao', 5, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('theme-kirtan', 'cat-music', 'Kirtan', 'Kirtan', 'kirtan', 6, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('theme-sound', 'cat-music', 'Sound Healing', 'Sound Healing', 'sound-healing', 7, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('theme-breathwork', 'cat-workshops', 'Breathwork', 'Breathwork', 'breathwork', 8, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00')
on conflict (id) do nothing;

insert into user_theme_preferences (user_id, theme_id) values
('user-1', 'theme-cacao'),
('user-1', 'theme-ecstatic'),
('user-2', 'theme-yoga'),
('user-2', 'theme-meditation'),
('user-3', 'theme-yoga'),
('user-3', 'theme-ecstatic')
on conflict do nothing;

insert into organizer_profiles (id, user_id, name, slug, bio, website, logo_url, verified, created_at, updated_at) values
('org-1', 'user-1', 'Maria Luna', 'maria-luna', 'Facilitadora de danza consciente y ceremonias de cacao con mas de 10 anos de experiencia.', null, 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop&crop=face', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('org-2', 'user-2', 'Carlos Sanchez', 'carlos-sanchez', 'Profesor de yoga y meditacion.', null, 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=face', false, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00')
on conflict (id) do nothing;

insert into community_groups (id, organizer_id, name, slug, description, category_id, banner_url, city, country, latitude, longitude, is_private, status, member_count, created_at, updated_at) values
('group-1', 'org-1', 'Ecstatic Dance Madrid', 'ecstatic-dance-madrid', 'Comunidad de danza libre y consciente en Madrid.', 'cat-dance', 'https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=800&h=300&fit=crop', 'Madrid', 'Espana', 40.4168, -3.7038, false, 'ACTIVE', 342, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('group-2', 'org-1', 'Yoga en el Parque BCN', 'yoga-parque-bcn', 'Sesiones de yoga gratuitas al aire libre en Barcelona.', 'cat-workshops', 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800&h=300&fit=crop', 'Barcelona', 'Espana', 41.3874, 2.1686, false, 'ACTIVE', 189, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('group-3', 'org-2', 'Circulo de Cacao Valencia', 'circulo-cacao-valencia', 'Ceremonias de cacao y circulos de compartir en Valencia.', 'cat-ceremonies', 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=800&h=300&fit=crop', 'Valencia', 'Espana', 39.4699, -0.3763, false, 'ACTIVE', 98, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00')
on conflict (id) do nothing;

insert into group_memberships (id, group_id, user_id, role, status, notification_preference, joined_at, approved_at) values
('membership-1', 'group-1', 'user-3', 'MEMBER', 'ACTIVE', 'IMMEDIATE', timestamp '2026-03-20 10:00:00', timestamp '2026-03-20 10:00:00'),
('membership-2', 'group-2', 'user-3', 'MEMBER', 'ACTIVE', 'DIGEST', timestamp '2026-03-21 10:00:00', timestamp '2026-03-21 10:00:00')
on conflict (id) do nothing;

insert into events (id, organizer_id, group_id, title, slug, description, cover_image_url, start_date, end_date, venue_name, address, city, country, latitude, longitude, is_online, is_hybrid, online_url, status, featured, max_attendees, is_free, price, currency, language, category_id, reminders_enabled, created_at, updated_at) values
('event-1', 'org-1', 'group-1', 'Danza Extatica al Atardecer', 'danza-extatica-atardecer', 'Una experiencia de movimiento libre y consciente acompanada de musica envolvente.', 'https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=600&h=400&fit=crop', timestamp '2026-04-05 18:00:00', timestamp '2026-04-05 21:00:00', 'Espacio Gaia', 'Calle del Sol 12', 'Madrid', 'Espana', 40.4168, -3.7038, false, false, null, 'PUBLISHED', true, 50, true, null, null, 'es', 'cat-dance', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('event-2', 'org-1', 'group-3', 'Ceremonia de Cacao Sagrado', 'ceremonia-cacao-sagrado', 'Circulo de cacao y musica medicina.', 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600&h=400&fit=crop', timestamp '2026-04-08 19:00:00', timestamp '2026-04-08 22:00:00', 'Casa del Alma', null, 'Barcelona', 'Espana', 41.3874, 2.1686, false, false, null, 'PUBLISHED', false, 25, false, 35.0, 'EUR', 'es', 'cat-ceremonies', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('event-3', 'org-2', 'group-2', 'Retiro de Yoga y Meditacion', 'retiro-yoga-meditacion', 'Dos dias de practica, descanso y comunidad.', 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=600&h=400&fit=crop', timestamp '2026-04-12 09:00:00', timestamp '2026-04-13 18:00:00', 'Finca La Paz', null, 'Granada', 'Espana', 37.1773, -3.5986, false, false, null, 'PUBLISHED', true, 20, false, 120.0, 'EUR', 'es', 'cat-retreats', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('event-4', 'org-2', null, 'Kirtan Cantos de Mantras', 'kirtan-cantos-mantras', 'Una noche para cantar y conectar.', 'https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=600&h=400&fit=crop', timestamp '2026-04-06 20:00:00', timestamp '2026-04-06 22:30:00', 'Centro Ananda', null, 'Valencia', 'Espana', 39.4699, -0.3763, false, false, null, 'PUBLISHED', false, 40, true, null, null, 'es', 'cat-music', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('event-5', 'org-1', 'group-2', 'Taller de Trabajo de Respiracion', 'taller-breathwork', 'Practica guiada de respiracion consciente.', 'https://images.unsplash.com/photo-1528495612343-9ca9f755e7bc?w=600&h=400&fit=crop', timestamp '2026-04-10 10:00:00', timestamp '2026-04-10 13:00:00', 'Espacio Gaia', null, 'Madrid', 'Espana', 40.4168, -3.7038, false, false, null, 'PUBLISHED', false, 30, false, 25.0, 'EUR', 'es', 'cat-workshops', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00'),
('event-6', 'org-2', null, 'Meditacion Guiada Online', 'meditacion-guiada-online', 'Sesion online para empezar el dia centrado.', 'https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=600&h=400&fit=crop', timestamp '2026-04-07 08:00:00', timestamp '2026-04-07 09:00:00', null, null, 'Online', '', 0, 0, true, false, 'https://karma.app/live/meditacion', 'PUBLISHED', false, null, true, null, null, 'es', 'cat-workshops', true, timestamp '2026-03-01 10:00:00', timestamp '2026-03-01 10:00:00')
on conflict (id) do nothing;

insert into event_themes (event_id, theme_id) values
('event-1', 'theme-ecstatic'),
('event-2', 'theme-cacao'),
('event-3', 'theme-yoga'),
('event-3', 'theme-meditation'),
('event-4', 'theme-kirtan'),
('event-5', 'theme-breathwork'),
('event-6', 'theme-meditation')
on conflict do nothing;

insert into rsvps (id, event_id, user_id, status, waitlist_position, checked_in, no_show, created_at, updated_at) values
('rsvp-1', 'event-1', 'user-3', 'YES', null, false, false, timestamp '2026-03-25 10:00:00', timestamp '2026-03-25 10:00:00'),
('rsvp-2', 'event-6', 'user-3', 'YES', null, false, false, timestamp '2026-03-26 10:00:00', timestamp '2026-03-26 10:00:00')
on conflict (id) do nothing;

insert into saved_events (id, user_id, event_id, saved_at) values
('saved-1', 'user-3', 'event-2', timestamp '2026-03-29 10:00:00'),
('saved-2', 'user-3', 'event-3', timestamp '2026-03-30 10:00:00')
on conflict (id) do nothing;

insert into event_orders (id, user_id, event_id, status, total_amount, currency, purchased_at) values
('order-1', 'user-3', 'event-3', 'PAID', 120.0, 'EUR', timestamp '2026-03-15 10:00:00')
on conflict (id) do nothing;

insert into blog_posts (id, title_es, title_en, slug, excerpt_es, excerpt_en, cover_image_url, published_at, created_at, updated_at) values
('blog-1', '5 Beneficios de la Danza Extatica', '5 Benefits of Ecstatic Dance', '5-beneficios-danza-extatica', 'Descubre como la danza libre puede transformar tu bienestar fisico y emocional.', 'Discover how free dance can transform your physical and emotional well-being.', 'https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=400&h=250&fit=crop', date '2026-03-15', timestamp '2026-03-15 10:00:00', timestamp '2026-03-15 10:00:00'),
('blog-2', 'Guia para tu Primera Ceremonia de Cacao', 'Guide to Your First Cacao Ceremony', 'guia-primera-ceremonia-cacao', 'Todo lo que necesitas saber antes de asistir a una ceremonia de cacao sagrado.', 'Everything you need to know before attending a sacred cacao ceremony.', 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400&h=250&fit=crop', date '2026-03-10', timestamp '2026-03-10 10:00:00', timestamp '2026-03-10 10:00:00')
on conflict (id) do nothing;
