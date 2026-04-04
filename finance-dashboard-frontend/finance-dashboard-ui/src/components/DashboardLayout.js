import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/dashboard.css';

const NAV = [
  {
    label: 'Main',
    items: [
      { path: '/dashboard', icon: '📊', title: 'Overview',         roles: ['VIEWER','ANALYST','ADMIN'] },
      { path: '/records',   icon: '💳', title: 'Finance Records',  roles: ['VIEWER','ANALYST','ADMIN'] },
    ],
  },
  {
    label: 'Management',
    items: [
      { path: '/users', icon: '👥', title: 'Users', roles: ['ADMIN'] },
    ],
  },
];

export default function DashboardLayout({ children }) {
  const { user, logout, hasRole } = useAuth();
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = () => { logout(); navigate('/login'); };

  const initials = user?.name
    ? user.name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()
    : '?';

  const pageTitle = NAV.flatMap(s => s.items).find(i => i.path === pathname)?.title ?? 'Dashboard';

  return (
    <div className="app-shell">
      {/* ── Sidebar ─────────────────────────────────────────── */}
      <aside className="sidebar" style={sidebarOpen ? { transform: 'translateX(0)' } : {}}>
        <div className="sidebar-logo">
          <div className="logo-icon">💹</div>
          <span>FinanceDash</span>
        </div>

        <nav className="sidebar-nav">
          {NAV.map(section => (
            <React.Fragment key={section.label}>
              <div className="nav-section-label">{section.label}</div>
              {section.items
                .filter(item => item.roles.some(r => hasRole(r)))
                .map(item => (
                  <button
                    key={item.path}
                    className={`nav-item${pathname === item.path ? ' active' : ''}`}
                    onClick={() => { navigate(item.path); setSidebarOpen(false); }}
                  >
                    <span className="nav-icon">{item.icon}</span>
                    {item.title}
                  </button>
                ))}
            </React.Fragment>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="user-chip">
            <div className="user-avatar">{initials}</div>
            <div className="user-info">
              <div className="user-name">{user?.name}</div>
              <div className="user-role">{user?.role}</div>
            </div>
          </div>
          <button className="btn btn-ghost" style={{ width: '100%', justifyContent: 'center' }} onClick={handleLogout}>
            🚪 Logout
          </button>
        </div>
      </aside>

      {/* ── Main ────────────────────────────────────────────── */}
      <div className="main-content">
        <header className="topbar">
          <span className="topbar-title">{pageTitle}</span>
          <div className="topbar-right">
            <span className={`badge badge-${user?.role?.toLowerCase()}`}>{user?.role}</span>
          </div>
        </header>
        <main className="page-body">{children}</main>
      </div>
    </div>
  );
}
