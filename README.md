# Jack & Sam Product Filter

Permission-based product access control platform — a Spring Boot REST API with JWT authentication, RBAC, multi-field filtering, pagination, audit logging, and Docker support.

## Quick Start

### Option 1: Run locally (zero setup, no Docker)

```bash
git clone https://github.com/lukedo/jack-sam-product-filter.git
cd jack-sam-product-filter
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Uses H2 in-memory database. Auto-creates tables and loads seed data on startup.

> Requires Java 18+ and Maven. Check with `java -version` and `mvn --version`.
> H2 console accessible at http://localhost:8080/h2-console

### Option 2: Docker (PostgreSQL)

```bash
git clone https://github.com/lukedo/jack-sam-product-filter.git
cd jack-sam-product-filter
docker-compose up
```

> Requires Docker Desktop. Download from https://docs.docker.com/desktop/install/mac-install

### Run with Web Dashboard

The app serves the React dashboard at `http://localhost:8080` automatically. Visit it in your browser and log in with the test accounts.

**Frontend dev mode (hot reload):**
```bash
cd frontend
npm install
npm run dev
# Starts on http://localhost:5173, proxies API to backend on :8080
```

**Production build** (frontend is bundled into the JAR):
```bash
cd frontend && npm ci && npm run build
cd .. && mvn package -DskipTests
java -jar target/product-filter-1.0.0.jar --spring.profiles.active=dev
```

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Jack & Sam Product Filter                     │
│                                                                  │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────────┐  │
│  │ Auth     │   │ Product  │   │ Admin    │   │ H2 Console   │  │
│  │ Controller│  │ Controller│  │ Controller│  │ (dev only)   │  │
│  └────┬─────┘   └────┬─────┘   └────┬─────┘  └──────────────┘  │
│       │               │              │                           │
│  ┌────▼───────────────▼──────────────▼──────────────────────┐   │
│  │                    Service Layer                          │   │
│  │  ProductService (cache/filter/access check/audit)        │   │
│  │  UserService / AuditService                              │   │
│  └────────────────────┬─────────────────────────────────────┘   │
│       │               │              │                           │
│  ┌────▼───────────────▼──────────────▼──────────────────────┐   │
│  │  JPA Repositories (Spring Data)                         │   │
│  └────────────────────┬─────────────────────────────────────┘   │
│       │               │              │                           │
│  ┌────▼───────────────▼──────────────▼──────────────────────┐   │
│  │  Database (H2 dev / PostgreSQL prod)                     │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 18+ |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security + JWT (jjwt 0.12) |
| Database | PostgreSQL 16 (prod) / H2 (dev) |
| ORM | Spring Data JPA / Hibernate |
| Cache | Spring Cache (in-memory) |
| Build | Maven |
| Deployment | Docker / Docker Compose |

## Web Dashboard

A full-featured React dashboard with real-time filtering, pagination, and admin controls.

### Pages

| Page | Path | Access | Description |
|---|---|---|---|
| Login | `/login` | Public | Sign in with JWT |
| Dashboard | `/` | All | Product stats overview |
| Products | `/products` | All | List, search, filter products |
| New Product | `/products/new` | All | Create a product |
| Users | `/users` | ADMIN | Create and manage users |
| Access | `/access` | ADMIN | Grant/revoke product access |
| Audit Logs | `/audit-logs` | ADMIN | View all audit events |

### Tech Stack (Frontend)

| Layer | Technology |
|---|---|
| Framework | React 18 + TypeScript |
| Build | Vite 4 |
| Styling | TailwindCSS 3 |
| Routing | React Router 6 |
| API | Axios (auto JWT, redirect on 401) |
| Notifications | react-hot-toast |

## Pre-loaded Test Accounts

| Username | Password | Role | Permissions |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | All permissions |
| `manager` | `manager123` | MANAGER | Product CRUD, inventory view, grant access |
| `viewer` | `viewer123` | VIEWER | Read-only product view |

Seed data also includes 8 sample products across 4 categories (Electronics, Laptops, Phones, Clothing).

## API Reference

### Authentication

```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": { "id": 1, "username": "admin", "roles": ["ADMIN"] }
}
```

Use the token in subsequent requests:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Products

#### List products (with filtering + pagination)

```
GET /api/products?search=MacBook&minPrice=500&maxPrice=3000
  &categoryId=1&inStock=true&sortBy=price&order=desc
  &page=0&size=20
```

| Parameter | Type | Default | Description |
|---|---|---|---|
| `search` | string | — | Fuzzy search on name + description |
| `minPrice` | decimal | — | Minimum price |
| `maxPrice` | decimal | — | Maximum price |
| `categoryId` | long | — | Category ID |
| `includeSubCategories` | bool | false | Include sub-categories |
| `inStock` | bool | — | Filter by stock > 0 |
| `active` | bool | — | Filter by active status |
| `sortBy` | string | `name` | Sort field |
| `order` | asc/desc | `asc` | Sort direction |
| `page` | int | 0 | Page number (0-based) |
| `size` | int | 20 | Page size (max 100) |

#### Get single product

```
GET /api/products/1
```

Records a view in audit log + access metrics.

#### Create product

```
POST /api/products
Content-Type: application/json

{
  "name": "New Product",
  "description": "Description here",
  "price": 99.99,
  "quantity": 50,
  "categoryId": 1
}
```

### Admin Endpoints (ADMIN role only)

#### Bulk grant access

```
POST /api/admin/user-access/bulk-grant
Content-Type: application/json

{
  "userIds": [1, 2, 3],
  "productIds": [10, 20, 30],
  "accessLevel": "READ"
}
```

`accessLevel` values: `READ`, `WRITE`, `ADMIN`

#### Revoke access

```
DELETE /api/admin/user-access/{userId}/{productId}
```

#### View audit logs

```
GET /api/admin/audit-logs?page=0&size=50
```

#### Create user

```
POST /api/admin/users
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123",
  "email": "user@example.com",
  "displayName": "New User",
  "roleName": "VIEWER"
}
```

## Database Schema

```
users (id, username, password, email, display_name, enabled, department_id, created_at)
roles (id, name, description)
user_roles (user_id, role_id)
role_permissions (role_id, permission)

products (id, name, description, price, quantity, active, image_url, category_id,
          owner_id, department_id, created_at, updated_at, version)
categories (id, name, description, parent_id)
user_access (id, user_id, product_id, access_level, granted_by, granted_at)

audit_logs (id, user_id, action, resource_type, resource_id, details, ip_address, timestamp)
product_access_metrics (id, product_id, total_views, total_edits, unique_users, date)
```

## Project Structure

### Backend (`src/`)

```
src/main/java/com/jacksam/productfilter/
├── ProductFilterApplication.java     # Entry point (@EnableCaching)
├── config/
│   ├── DataSeeder.java               # Seeds users, roles, categories, products
│   ├── GlobalExceptionHandler.java    # REST error handling
│   └── SecurityConfig.java           # JWT filter, CORS, endpoint security
├── controller/
│   ├── AuthController.java           # POST /api/auth/login
│   ├── ProductController.java        # GET/POST /api/products
│   └── AdminController.java          # Bulk grant, audit logs, user management
├── dto/                              # Request/response records
├── entity/                           # JPA entities (8 tables)
├── enums/                            # Permission, AccessLevel, AuditAction
├── repository/                       # Spring Data JPA repositories (7)
├── security/
│   ├── JwtTokenProvider.java         # JWT generation + validation
│   └── JwtAuthFilter.java            # Bearer token extraction filter
└── service/
    ├── ProductService.java           # Core: caching, filtering, access control, metrics
    ├── UserService.java              # User CRUD
    └── AuditService.java             # Audit logging
```

### Frontend (`frontend/`)

```
frontend/
├── index.html                       # Vite entry
├── package.json                     # Dependencies
├── vite.config.ts                   # Vite + dev proxy to :8080
├── tailwind.config.js
├── postcss.config.js
├── tsconfig.json
└── src/
    ├── main.tsx                     # React root (BrowserRouter + AuthProvider)
    ├── App.tsx                      # Route definitions
    ├── index.css                    # Tailwind directives
    ├── api/
    │   └── client.ts                # Axios instance (JWT interceptor, all API calls)
    ├── contexts/
    │   └── AuthContext.tsx           # Auth state (user, token, login, logout)
    ├── components/
    │   ├── Layout.tsx               # Sidebar + main content area
    │   ├── Sidebar.tsx              # Navigation (role-aware)
    │   └── ProtectedRoute.tsx       # Auth + admin guards
    ├── pages/
    │   ├── Login.tsx                # Sign-in form with test account hints
    │   ├── Dashboard.tsx            # Product stats cards
    │   ├── Products.tsx             # Filterable product table + pagination
    │   ├── ProductForm.tsx          # Create/edit product form
    │   ├── Users.tsx                # User list + create user form
    │   ├── UserAccess.tsx           # Grant product access to users
    │   └── AuditLogs.tsx            # Audit event viewer
    └── types/
        └── index.ts                 # TypeScript interfaces
```

## Features Implemented

### Backend
- [x] JWT authentication (3 pre-seeded users)
- [x] RBAC with fine-grained permissions (12 permission types)
- [x] Multi-field product filtering (search, price range, category, stock)
- [x] Pagination + sorting
- [x] Product access control (owner-based + explicit grants)
- [x] Bulk access grant / revoke
- [x] Audit logging (view, create, grant, revoke events)
- [x] Product access analytics (view counts, unique users per day)
- [x] Caching (user accessible products with TTL)
- [x] Category hierarchy (parent/child)
- [x] Global exception handling
- [x] Docker Compose (app + PostgreSQL)
- [x] Dev profile (H2 in-memory, no Docker needed)

### Frontend
- [x] Login page with JWT authentication
- [x] Dashboard with product statistics cards
- [x] Product list with search, filter, sort, and pagination
- [x] Product create/edit form
- [x] User management (list + create)
- [x] Access grant interface
- [x] Audit log viewer with action labels
- [x] Role-aware sidebar navigation (admin pages hidden for non-admins)
- [x] SPA routing (React Router)
- [x] Auto-redirect to login on 401
- [x] TailwindCSS responsive design
- [x] Hot-reload dev server (Vite) with API proxy
- [x] Production build bundled into backend JAR
