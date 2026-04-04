# Finance Dashboard — Frontend

A modern, role-aware React frontend for the Finance Dashboard system. Built with **React 18**, **React Router v6**, **Recharts**, and **Axios**, styled with a dark-first design system.

---

## Features

- **JWT-based authentication** — tokens stored in localStorage, automatically sent on every request
- **Role-aware UI** — navigation items, buttons, and entire pages are shown or hidden based on the logged-in user's role (VIEWER / ANALYST / ADMIN)
- **Finance Records page** — full CRUD table with filters by type, category, and date range
- **Overview dashboard** — area chart (income vs expenses trend), pie chart (category breakdown), bar chart (monthly net balance), and recent transactions table
- **User Management page** — admin-only; create, edit, activate/deactivate, and delete users with live search
- **Dark design system** — consistent CSS variables, smooth transitions, responsive layout

---

## Role Behaviour

| Page / Feature | VIEWER | ANALYST | ADMIN |
|---|:---:|:---:|:---:|
| Overview (charts & summary) | ❌ | ✅ | ✅ |
| Finance Records — view | ✅ | ✅ | ✅ |
| Finance Records — create/edit | ❌ | ✅ | ✅ |
| Finance Records — delete | ❌ | ❌ | ✅ |
| Users page | ❌ | ❌ | ✅ |

> Restricted pages show a friendly "Access Restricted" message instead of crashing or returning a blank screen.

---

## Tech Stack

| | |
|---|---|
| Framework | React 18 |
| Routing | React Router DOM v6 |
| HTTP | Axios (with request/response interceptors) |
| Charts | Recharts |
| Styling | Pure CSS (custom design system, no component library) |
| State | React Context (AuthContext) + local useState |
| Build tool | Create React App (react-scripts 5) |

---

## Prerequisites

- Node.js 16+
- npm 8+
- All 5 backend services running (see backend README)

---

## Setup & Running

```bash
# 1. Install dependencies
npm install

# 2. Start the development server
npm start
```

The app opens at **http://localhost:3000** and proxies API calls to the gateway at `http://localhost:8082` (configured in `package.json`).

---

## Project Structure

```
src/
├── api/
│   ├── api.js          ← Axios instance with JWT interceptor & 401 handler
│   ├── auth.js         ← login(), register()
│   ├── users.js        ← getUsers(), createUser(), updateUser(), deleteUser()
│   └── finance.js      ← getRecords(), createRecord(), getDashboardSummary(), …
│
├── context/
│   └── AuthContext.js  ← Global auth state (user, login, logout, hasRole)
│
├── pages/
│   ├── Login.js        ← Login form
│   ├── Register.js     ← Registration form with role selector
│   ├── Overview.js     ← Dashboard charts and recent transactions
│   ├── FinanceRecords.js ← Full CRUD table with filters and modal
│   └── Users.js        ← User management (Admin only)
│
├── components/
│   └── DashboardLayout.js ← Sidebar, topbar, navigation shell
│
├── styles/
│   ├── global.css      ← Design tokens, reset, utility classes
│   ├── auth.css        ← Login/Register page styles
│   └── dashboard.css   ← Sidebar, topbar, stat cards, charts, tables
│
├── App.js              ← Routes with PrivateRoute / PublicRoute guards
└── index.js            ← React root
```

---

## API Endpoints Consumed

All requests go through the API Gateway at `http://localhost:8082`.

| Method | URL | Used in |
|---|---|---|
| POST | `/api/auth/auth/login` | Login page |
| POST | `/api/auth/auth/register` | Register page |
| GET | `/api/users/users` | Users page |
| POST | `/api/users/users` | Users page (create) |
| PUT | `/api/users/users/:id` | Users page (edit) |
| DELETE | `/api/users/users/:id` | Users page (delete) |
| GET | `/api/finance/finance/records` | Finance Records page |
| POST | `/api/finance/finance/records` | Finance Records (create) |
| PUT | `/api/finance/finance/records/:id` | Finance Records (edit) |
| DELETE | `/api/finance/finance/records/:id` | Finance Records (delete) |
| GET | `/api/finance/finance/dashboard/summary` | Overview page |

---

## Design Decisions

**Why no component library (e.g. Material UI, Ant Design)?**
A custom CSS system gives full control over the dark theme, token-based colors, and component behavior without fighting overrides. It also demonstrates ability to design from scratch.

**Why React Context for auth instead of Redux?**
The auth state is simple (one user object + token). Context + a custom `useAuth()` hook is sufficient and avoids Redux boilerplate.

**Why Recharts?**
Declarative, React-native, and straightforward to theme with custom colors. Used for AreaChart (trends), PieChart (category breakdown), and BarChart (monthly net).

**Why `proxy` in package.json?**
Avoids CORS issues during development by routing all `/api/*` calls through the dev server to the API Gateway. In production, configure nginx or a deployment target to do the same.

---

## Assumptions

- The backend is running locally before `npm start` is executed.
- The API Gateway is on port `8082` (default). Change `proxy` in `package.json` and the `BASE_URL` in `src/api/api.js` if different.
- Tokens expire after 1 hour (set in the backend). The app redirects to `/login` on a 401/403 response automatically.
