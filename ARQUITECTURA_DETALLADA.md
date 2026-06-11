# 🌍 KARMA Platform - Arquitectura Actual y Expansión Mundial

## 📋 Índice
1. [Componentes Actuales](#componentes-actuales)
2. [Arquitectura de Expansión](#arquitectura-de-expansión)
3. [Roadmap de Implementación](#roadmap-de-implementación)
4. [Consideraciones de Escalabilidad](#consideraciones-de-escalabilidad)

---

## ✅ Componentes Actuales

### 1. **Frontend (React + TypeScript + Vite)**
- **Ubicación:** `frontend/`
- **Características:**
  - UI responsivo con componentes reutilizables
  - Gestión de estado moderna
  - Integración con API REST backend
  - Proxy a `/api` en desarrollo

### 2. **Backend (Spring Boot)**
- **Ubicación:** `backend/`
- **Stack:** Java, Spring Framework, Gradle
- **Controladores Principales:**
  - `AuthController` - Autenticación y registro
  - `EventController` - Gestión de eventos
  - `UserController` - Perfiles de usuario
  - `OrganizerController` - Gestión de organizadores
  - `OrderController` - Procesamiento de órdenes
  - `StripeWebhookController` - Webhooks de pago
  - `BlogController` - Contenido de blog
  - `GroupController` & `GroupDiscussionController` - Comunidades
  - `UploadController` - Gestión de archivos

### 3. **Base de Datos (PostgreSQL 16 + PostGIS)**
- **Características:**
  - Datos geoespaciales con PostGIS
  - Migraciones con Flyway
  - Soporte para consultas complejas
  - Replicación disponible para HA

**Esquema Principal:**
```
Users → Events → Orders → Tickets
      ↓
    Groups → Discussions
      ↓
    Reviews & Ratings
      ↓
    Blog Posts
```

### 4. **Sistema de Autenticación (JWT)**
- JWT tokens con validación de audience
- Renovación de tokens
- Rol basado en control de acceso (RBAC)
- Seguridad en headers HTTP

### 5. **Almacenamiento (S3-Compatible)**
- Subida de imágenes y archivos
- Gestión de presigned URLs
- Limpieza automática de archivos
- Compatible con AWS S3, MinIO, DigitalOcean Spaces

### 6. **Sistema de Email (Trilingual)**
- **Idiomas:** Español, Inglés, Catalán
- **Características:**
  - MailHog para testing local
  - Cola de emails con retry
  - Templates multilingual
  - Digests semanales
  - Recordatorios de eventos
  
### 7. **Servicios de Negocio**

#### Event Management
- CRUD de eventos
- Búsqueda y filtrado
- Geocoding de ubicaciones
- Categorías de eventos

#### Orders & Ticketing
- Procesamiento de órdenes
- Sistema de tickets
- Tipos de tickets (entrada gratuita, pago, etc.)
- Gestión de inventario

#### Payments (Stripe)
- Integración con Stripe
- Webhooks para confirmación de pagos
- Manejo de refunds
- PCI compliance

#### Reviews & Ratings
- Sistema de calificaciones
- Reviews de eventos y organizadores
- Validación (solo usuarios que asistieron)

#### Blog & Content
- Gestión de posts
- Categorización de contenido
- SEO-friendly URLs

#### Groups & Discussions
- Creación de comunidades
- Foros de discusión
- Moderación

#### Notifications
- Email notifications
- Push notifications (futura)
- SMS (futura)

### 8. **Operacionales**
- **Health Checks:** `/actuator/health`
- **API Documentation:** Swagger/OpenAPI en `/swagger-ui.html`
- **Code Coverage:** JaCoCo reports
- **Security:** Pre-commit hooks para scan de secretos

### 9. **DevOps & CI/CD**
- **Containerización:** Docker + Docker Compose
- **Orquestación Test:** Dokploy
- **Orquestación Prod:** AWS EC2
- **CI/CD:** GitHub Actions
- **Deployment:** Automated testing + deployment pipelines

---

## 🚀 Arquitectura de Expansión (Para Proyecto Mundial)

### Fase 1: Internacionalización & Multi-Moneda ⭐ PRIORITARIO

#### 1.1 Internacionalización (i18n)
```
OBJETIVO: Soportar 15+ idiomas

COMPONENTES:
├── Frontend i18n Layer
│   ├── i18next / next-i18n-router
│   ├── Detección automática de locale
│   ├── Traducción de UI strings
│   └── Formatos locales (fechas, números, moneda)
│
├── Backend Localization
│   ├── Locale detection middleware
│   ├── Translation API
│   ├── Content localization service
│   └── Email templates in all languages
│
└── Content Management
    ├── i18n database schema
    ├── Translation management tool
    └── Crowdsourcing translations (opcional)

IDIOMAS PRIORITARIOS:
1. Español (ES)
2. Inglés (EN)
3. Catalán (CA)
4. Portugués (PT-BR)
5. Francés (FR)
6. Alemán (DE)
7. Italiano (IT)
8. Chino (ZH)
9. Japonés (JA)
10. Árabe (AR)
... + 5 más según demanda
```

#### 1.2 Multi-Moneda
```
OBJETIVO: Transacciones en 50+ monedas

COMPONENTES:
├── Currency Management Service
│   ├── Rates updater (API: OpenExchangeRates, Fixer.io)
│   ├── Cache de tipos de cambio
│   ├── Histórico de conversiones
│   └── Rounding rules por moneda
│
├── Pricing Layer
│   ├── Base price en USD
│   ├── Conversión automática en tiempo real
│   ├── Precios base por región (opcional)
│   └── Tax calculation por país
│
├── Payment Processing
│   ├── Multi-currency support en gateways
│   ├── Conversión automática
│   └── Settlement en moneda local
│
└── Frontend Integration
    ├── Currency selector
    ├── Formato de moneda local
    └── Histórico de conversiones
```

### Fase 2: Escalabilidad & Performance 🚀

#### 2.1 CDN Global
```
SERVICIOS: Cloudflare, CloudFront, Akamai

OPTIMIZACIONES:
├── Static Asset Distribution
│   ├── JS/CSS bundles
│   ├── Imágenes optimizadas (WebP, AVIF)
│   ├── Fonts y iconos
│   └── Cache headers optimizados
│
├── Edge Computing
│   ├── Cloudflare Workers para lógica edge
│   ├── Geo-routing inteligente
│   ├── Bot protection
│   └── DDoS mitigation
│
└── Performance Metrics
    ├── Core Web Vitals monitoring
    ├── Real user monitoring (RUM)
    └── Synthetic monitoring
```

#### 2.2 Distributed Cache (Redis)
```
OBJETIVO: Reducir latencia y carga DB

CASOS DE USO:
├── Session Management
│   └── Distributed sessions across regions
│
├── Query Caching
│   ├── Event listings
│   ├── User profiles
│   ├── Ratings/reviews
│   └── Blog posts
│
├── Rate Limiting
│   ├── API rate limits
│   ├── Login attempt limiting
│   └── Email sending limits
│
├── Job Queue
│   ├── Background tasks
│   ├── Email sending
│   ├── Image processing
│   └── Batch operations
│
└── Pub/Sub
    ├── Real-time notifications
    ├── Event streaming
    └── Cache invalidation
```

#### 2.3 Load Balancer & API Gateway
```
OPCIONES: Nginx, HAProxy, Kong, AWS ALB

FUNCIONALIDADES:
├── Load Distribution
│   ├── Round-robin
│   ├── Least connections
│   ├── Geo-routing
│   └── Health checks
│
├── API Gateway Features
│   ├── Request/response transformation
│   ├── Authentication delegation
│   ├── Rate limiting
│   ├── Request logging
│   └── Mock responses
│
├── SSL/TLS
│   ├── Certificate management
│   ├── mTLS for microservices
│   └── ACME auto-renewal
│
└── DDoS Protection
    ├── WAF rules
    ├── Bot detection
    └── Rate limiting
```

### Fase 3: Observabilidad & Monitoreo 📊

#### 3.1 Metrics & Monitoring
```
STACK: Prometheus + Grafana

MÉTRICAS:
├── Application Metrics
│   ├── Request latency
│   ├── Error rates
│   ├── Throughput
│   ├── Database query times
│   └── Cache hit rates
│
├── Infrastructure Metrics
│   ├── CPU, Memory, Disk usage
│   ├── Network I/O
│   ├── Database pool connections
│   └── Container resource usage
│
├── Business Metrics
│   ├── Events created/updated
│   ├── Orders processed
│   ├── Revenue by currency
│   ├── User growth
│   └── Organizer activity
│
└── Dashboards
    ├── Real-time application health
    ├── Performance tracking
    ├── Business KPIs
    └── Alerts & thresholds
```

#### 3.2 Logging Centralizado
```
STACK: ELK (Elasticsearch, Logstash, Kibana) o Loki

FUNCIONALIDADES:
├── Log Aggregation
│   ├── Frontend errors
│   ├── Backend logs
│   ├── Database logs
│   └── Infrastructure logs
│
├── Log Analysis
│   ├── Full-text search
│   ├── Structured logging (JSON)
│   ├── Correlation IDs
│   └── Log retention policies
│
└── Alerting
    ├── Error rate spikes
    ├── Performance degradation
    ├── Security events
    └── Custom business rules
```

#### 3.3 APM (Application Performance Monitoring)
```
OPCIONES: New Relic, DataDog, Elastic APM

RASTREO:
├── Distributed Tracing
│   ├── Request flow across services
│   ├── Latency breakdown
│   ├── Error tracking
│   └── Service dependencies
│
├── Error Tracking
│   ├── Stack traces
│   ├── Session replay
│   ├── User impact analysis
│   └── Error grouping
│
└── Performance Profiling
    ├── CPU profiling
    ├── Memory profiling
    ├── Database query analysis
    └── Hot spot identification
```

### Fase 4: Comunicación & Engagement 💬

#### 4.1 Real-time Communication (WebSockets)
```
TECNOLOGÍAS: Socket.IO, Centrifugo, SignalR

FUNCIONALIDADES:
├── Live Chat
│   ├── Direct messaging
│   ├── Group discussions
│   ├── Typing indicators
│   └── Message history
│
├── Real-time Notifications
│   ├── Event updates
│   ├── Order status
│   ├── New messages
│   └── Social interactions
│
├── Live Collaboration
│   ├── Real-time event updates
│   ├── Participant count
│   ├── Live comments
│   └── Q&A live

└── Presence & Activity
    ├── User online status
    ├── Activity feeds
    └── Typing indicators
```

#### 4.2 Push Notifications
```
SERVICIOS: Firebase Cloud Messaging, OneSignal, Pusher

CANALES:
├── Web Push
│   ├── Service Workers
│   ├── Desktop notifications
│   └── Browser alerts
│
├── Mobile Push
│   ├── iOS (APNs)
│   ├── Android (FCM)
│   └── Cross-platform SDKs
│
├── Smart Notifications
│   ├── Timezone-aware scheduling
│   ├── Personalization
│   ├── A/B testing
│   └── Unsubscribe management
│
└── Analytics
    ├── Delivery tracking
    ├── Open rates
    ├── Click-through rates
    └── Engagement metrics
```

#### 4.3 SMS & WhatsApp Integration
```
SERVICIOS: Twilio, Vonage, MessageBird

CASOS DE USO:
├── OTP (One-Time Passwords)
├── Event Reminders
├── Order Updates
├── Organizer Notifications
├── Support Alerts
└── Marketing Campaigns
```

### Fase 5: Pagos Globales 💳

#### 5.1 Multi-Gateway Payments
```
GATEWAYS PRIMARIOS:
├── Stripe (Global, 195+ países)
├── PayPal (Global, múltiples monedas)
├── Square (USA, Canada, Australia, Japan, UK)
├── Adyen (Global, 150+ monedas)
└── Local Payment Methods
    ├── iDEAL (Holanda)
    ├── Banco Santander (España, Latam)
    ├── Mercado Pago (Latam)
    ├── Alipay / WeChat Pay (Asia)
    ├── GCash (Philippines)
    └── M-Pesa (Africa)

ABSTRACCIÓN:
├── Payment Adapter Pattern
│   ├── Unified payment interface
│   ├── Gateway agnostic
│   └── Easy to add new gateways
│
├── Fallback Strategy
│   ├── Primary gateway fails → Secondary
│   ├── Retry logic with exponential backoff
│   └── Payment status reconciliation
│
└── PCI Compliance
    ├── Tokenization
    ├── 3D Secure / SCA
    ├── Encryption
    └── Audit logs
```

#### 5.2 Subscription & Recurring Billing
```
FUNCIONALIDADES:
├── Subscription Plans
├── Recurring Charges
├── Proration
├── Dunning Management
├── Churn Prevention
└── Revenue Recognition
```

### Fase 6: Análisis & BI 📈

#### 6.1 Analytics & Insights
```
STACK: Segment, Mixpanel, Amplitude

TRACKING:
├── User Events
│   ├── Page views
│   ├── Searches
│   ├── Event interactions
│   ├── Purchase journey
│   └── Sign-up funnel
│
├── Business Events
│   ├── Event creation
│   ├── Orders completed
│   ├── Revenue metrics
│   ├── Organizer activity
│   └── User engagement
│
├── Dashboards
│   ├── Daily active users
│   ├── Conversion funnels
│   ├── Revenue trends
│   ├── Geographic distribution
│   └── Churn analysis
│
└── Activation Campaigns
    ├── Cohort analysis
    ├── A/B testing
    ├── Personalized recommendations
    └── Retention campaigns
```

#### 6.2 Data Warehouse & BI
```
STACK: Snowflake, BigQuery, Redshift

DATA SOURCES:
├── Application events
├── User behavior
├── Transaction data
├── Operational metrics
└── 3rd party APIs

BI TOOLS:
├── Tableau
├── Looker
├── Metabase
└── Apache Superset

REPORTES:
├── Executive dashboards
├── Marketing analytics
├── Finance reports
├── Product metrics
└── Custom reports
```

### Fase 7: Machine Learning 🤖

#### 7.1 Recomendaciones de Eventos
```
MODELO: Collaborative Filtering + Content-based

FEATURES:
├── Event recommendations
├── Personalized search rankings
├── "You might like" suggestions
├── Trending events
└── Smart discovery

INFRAESTRUCTURA:
├── MLflow for model management
├── Feature store
├── Real-time inference
└── Batch processing
```

#### 7.2 Predictive Analytics
```
MODELOS:
├── Churn prediction
├── Revenue forecasting
├── Demand forecasting
├── Price optimization
└── Fraud detection
```

### Fase 8: Integraciones Sociales & Marketplace 🔗

#### 8.1 Social Login
```
PROVEEDORES:
├── Google OAuth
├── Facebook Login
├── Apple Sign In
├── LinkedIn
└── GitHub (para organizadores)

BENEFICIOS:
├── Faster sign-up
├── Unified profile data
├── Reduced password fatigue
└── Better user demographics
```

#### 8.2 Maps Integration
```
SERVICIOS:
├── Google Maps API (Geolocation, Directions)
├── Mapbox (Custom maps, styling)
├── OpenStreetMap (Privacy-first alternative)

FUNCIONALIDADES:
├── Event location on map
├── Distance calculation
├── Route planning
├── Nearby events discovery
└── Venue mapping
```

#### 8.3 CRM Integration
```
SERVICIOS: HubSpot, Salesforce, Pipedrive

SINCRONIZACIÓN:
├── User data sync
├── Event attendance tracking
├── Revenue tracking
├── Organizer management
└── Opportunity pipeline
```

#### 8.4 Video Streaming
```
SERVICIOS: Mux, Cloudflare Stream, AWS MediaLive

CASOS DE USO:
├── Hybrid events (virtual + in-person)
├── Webinars
├── Live streaming
├── On-demand recordings
├── Monetization options

CARACTERÍSTICAS:
├── HLS/DASH streaming
├── Adaptive bitrate
├── Analytics
├── DRM (optional)
└── Chat integration
```

#### 8.5 Blockchain & NFT (Future)
```
OPCIONALES:
├── Event tickets as NFTs
├── Attendance certificates
├── Loyalty programs
├── Creator economy
└── Smart contracts for revenue split
```

---

## 📅 Roadmap de Implementación

### Cuatrimestre 1 (Meses 1-4) - Foundation
- [ ] Multi-lenguaje core (i18n)
- [ ] Multi-moneda support
- [ ] Redis deployment
- [ ] Prometheus + Grafana setup
- [ ] ELK stack para logging

### Cuatrimestre 2 (Meses 5-8) - Scale
- [ ] CDN global implementation
- [ ] Load balancer setup
- [ ] API Gateway (Kong)
- [ ] WebSockets real-time chat
- [ ] APM implementation (New Relic)

### Cuatrimestre 3 (Meses 9-12) - Growth
- [ ] Push notifications
- [ ] SMS/WhatsApp integration
- [ ] Multi-payment gateway
- [ ] Analytics & BI setup
- [ ] Regional deployment (EU, APAC, LATAM)

### Año 2 - Advanced
- [ ] Machine Learning
- [ ] Advanced social integrations
- [ ] Video streaming
- [ ] Kubernetes multi-cloud
- [ ] Blockchain/NFT support

---

## 🏗️ Consideraciones de Escalabilidad

### Database Scaling
```
├── Read Replicas (Sharding by region)
├── Connection pooling (PgBouncer)
├── Partitioning (Time-based for events/orders)
├── Full-text search (Elasticsearch)
└── Graph database (Neo4j) para social features
```

### Service Architecture
```
├── Monolith → Microservices migration path
├── Service discovery (Consul, Eureka)
├── API versioning strategy
├── Backward compatibility
└── Contract testing
```

### Data Consistency
```
├── Event sourcing for critical data
├── CQRS pattern
├── Eventual consistency model
├── Saga pattern for distributed transactions
└── Conflict resolution strategies
```

### Infrastructure
```
├── Kubernetes (EKS, GKE, AKS)
├── Multi-region deployment
├── Terraform for IaC
├── Blue-green deployments
├── Canary releases
├── Feature flags
└── Disaster recovery (RTO/RPO targets)
```

### Security at Scale
```
├── API security scanning
├── Dependency scanning
├── SAST/DAST
├── Runtime application self-protection (RASP)
├── Secrets management (Vault)
├── Encryption at rest & in transit
└── Compliance (GDPR, CCPA, local regulations)
```

---

## 🎯 Success Metrics

### Performance
- P95 latency < 200ms globally
- 99.99% uptime SLA
- Core Web Vitals (LCP < 2.5s, FID < 100ms, CLS < 0.1)

### User Experience
- Sign-up completion rate > 85%
- Event discovery success rate > 75%
- Mobile app rating > 4.5 stars

### Business
- Revenue per user increased 3x
- Geographic diversification (50%+ revenue from outside USA)
- Organizer retention > 90%
- User growth 200% YoY

---

## 📚 Recursos & Referencias

- [Stripe Global Payments](https://stripe.com/docs)
- [i18n Best Practices](https://www.i18next.com/)
- [Redis Scalability](https://redis.io/docs/)
- [Kubernetes Production Best Practices](https://kubernetes.io/)
- [OWASP Security Checklist](https://cheatsheetseries.owasp.org/)
- [12 Factor App](https://12factor.net/)

---

**Última actualización:** 20 de Mayo, 2026  
**Documento:** ARQUITECTURA_DETALLADA.md  
**Proyecto:** Karma Platform - Global Events & Wellness
