/* ============================================
   HELP DESK PI IV — Rendering Components
   All views and reusable components
   Variables in English, labels via i18n
   ============================================ */

// ── SVG Icons (Lucide-style) ──
const Icons = {
  headphones: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 14h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-7a9 9 0 0 1 18 0v7a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h3"/></svg>',
  layoutDashboard: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="7" height="9" x="3" y="3" rx="1"/><rect width="7" height="5" x="14" y="3" rx="1"/><rect width="7" height="9" x="14" y="12" rx="1"/><rect width="7" height="5" x="3" y="16" rx="1"/></svg>',
  ticket: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2Z"/><path d="M13 5v2"/><path d="M13 17v2"/><path d="M13 11v2"/></svg>',
  settings: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"/><circle cx="12" cy="12" r="3"/></svg>',
  search: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>',
  bell: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/></svg>',
  sun: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2"/><path d="M12 20v2"/><path d="m4.93 4.93 1.41 1.41"/><path d="m17.66 17.66 1.41 1.41"/><path d="M2 12h2"/><path d="M20 12h2"/><path d="m6.34 17.66-1.41 1.41"/><path d="m19.07 4.93-1.41 1.41"/></svg>',
  moon: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/></svg>',
  plus: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="M12 5v14"/></svg>',
  edit: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z"/><path d="m15 5 4 4"/></svg>',
  eye: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0"/><circle cx="12" cy="12" r="3"/></svg>',
  trash: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/><line x1="10" x2="10" y1="11" y2="17"/><line x1="14" x2="14" y1="11" y2="17"/></svg>',
  x: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>',
  chevronLeft: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m15 18-6-6 6-6"/></svg>',
  chevronRight: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg>',
  chevronsSort: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m7 15 5 5 5-5"/><path d="m7 9 5-5 5 5"/></svg>',
  arrowUp: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m5 12 7-7 7 7"/><path d="M12 19V5"/></svg>',
  arrowDown: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 5v14"/><path d="m19 12-7 7-7-7"/></svg>',
  trendingUp: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/><polyline points="16 7 22 7 22 13"/></svg>',
  trendingDown: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 17 13.5 8.5 8.5 13.5 2 7"/><polyline points="16 17 22 17 22 11"/></svg>',
  clipboardList: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="8" height="4" x="8" y="2" rx="1" ry="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="M12 11h4"/><path d="M12 16h4"/><path d="M8 11h.01"/><path d="M8 16h.01"/></svg>',
  alertCircle: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" x2="12" y1="8" y2="12"/><line x1="12" x2="12.01" y1="16" y2="16"/></svg>',
  loader: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2v4"/><path d="m16.2 7.8 2.9-2.9"/><path d="M18 12h4"/><path d="m16.2 16.2 2.9 2.9"/><path d="M12 18v4"/><path d="m4.9 19.1 2.9-2.9"/><path d="M2 12h4"/><path d="m4.9 4.9 2.9 2.9"/></svg>',
  checkCircle: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><path d="m9 11 3 3L22 4"/></svg>',
  xCircle: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="m15 9-6 6"/><path d="m9 9 6 6"/></svg>',
  info: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/></svg>',
  menu: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" x2="20" y1="12" y2="12"/><line x1="4" x2="20" y1="6" y2="6"/><line x1="4" x2="20" y1="18" y2="18"/></svg>',
  user: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>',
  calendar: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M8 2v4"/><path d="M16 2v4"/><rect width="18" height="18" x="3" y="4" rx="2"/><path d="M3 10h18"/></svg>',
  clock: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>',
  tag: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12.586 2.586A2 2 0 0 0 11.172 2H4a2 2 0 0 0-2 2v7.172a2 2 0 0 0 .586 1.414l8.704 8.704a2.426 2.426 0 0 0 3.42 0l6.58-6.58a2.426 2.426 0 0 0 0-3.42z"/><circle cx="7.5" cy="7.5" r=".5" fill="currentColor"/></svg>',
  messageSquare: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>',
  arrowLeft: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m12 19-7-7 7-7"/><path d="M19 12H5"/></svg>',
  inbox: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 16 12 14 15 10 15 8 12 2 12"/><path d="M5.45 5.11 2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z"/></svg>',
  shieldCheck: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/><path d="m9 12 2 2 4-4"/></svg>',
  logOut: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" x2="9" y1="12" y2="12"/></svg>'
};

// ── Component: Sidebar ──
function renderSidebar() {
  const stats = getDashboardStats();
  return `
    <aside class="sidebar" id="sidebar">
      <div class="sidebar__logo">
        <div class="sidebar__logo-icon">${Icons.headphones}</div>
        <div class="sidebar__logo-text">Help<span>Desk</span></div>
      </div>

      <nav class="sidebar__nav">
        <div class="sidebar__section-title">${i18n.t('main')}</div>
        <a class="sidebar__link active" data-route="dashboard" href="#dashboard">
          <span class="sidebar__link-icon">${Icons.layoutDashboard}</span>
          <span class="sidebar__link-text">${i18n.t('dashboard')}</span>
        </a>
        <a class="sidebar__link" data-route="chamados" href="#chamados">
          <span class="sidebar__link-icon">${Icons.ticket}</span>
          <span class="sidebar__link-text">${i18n.t('tickets')}</span>
          <span class="sidebar__link-badge" id="sidebarTicketCount">${stats.total}</span>
        </a>
        
        <div class="sidebar__section-title">${i18n.t('system')}</div>
        <a class="sidebar__link" data-route="configuracoes" href="#configuracoes">
          <span class="sidebar__link-icon">${Icons.settings}</span>
          <span class="sidebar__link-text">${i18n.t('settings')}</span>
        </a>
      </nav>

      <div class="sidebar__footer">
        <div class="sidebar__user" id="sidebarUser">
          <div class="sidebar__user-avatar">${CURRENT_USER?.initials || 'HD'}</div>
          <div class="sidebar__user-info">
            <div class="sidebar__user-name">${CURRENT_USER?.name || 'Usuário'}</div>
            <div class="sidebar__user-role">${CURRENT_USER?.jobTitle || 'Colaborador'}</div>
          </div>
        </div>
      </div>
    </aside>
  `;
}

// ── Component: Header ──
function renderHeader(breadcrumbs) {
  const themeIcon = ThemeManager.isDark() ? Icons.sun : Icons.moon;
  const unreadCount = getUnreadNotificationsCount();
  return `
    <header class="header">
      <div class="header__left">
        <button class="mobile-menu-btn" id="mobileMenuBtn" title="Menu">
          ${Icons.menu}
        </button>
        <div class="header__breadcrumb">
          ${breadcrumbs.map((b, i) => {
            if (i === breadcrumbs.length - 1) {
              return `<span class="header__breadcrumb-current">${b}</span>`;
            }
            return `<span>${b}</span><span class="header__breadcrumb-separator">/</span>`;
          }).join('')}
        </div>
      </div>
      <div class="header__right">
        <div class="header__search">
          <span class="header__search-icon">${Icons.search}</span>
          <input type="text" class="header__search-input" id="globalSearch" placeholder="${i18n.t('searchTickets')}" autocomplete="off">
        </div>
        <button class="header__icon-btn" id="themeToggleBtn" title="${i18n.t('toggleTheme')}">
          ${themeIcon}
        </button>
        <div class="header__dropdown-wrapper">
          <button class="header__icon-btn" id="notificationsBtn" title="${i18n.t('notifications')}">
            ${Icons.bell}
            ${unreadCount > 0 ? '<span class="notification-dot"></span>' : ''}
          </button>
          <div class="header__dropdown header__dropdown--notifications" id="notificationsDropdown">
            <div class="dropdown__header">
              <h3 class="dropdown__title">${i18n.t('notifications')}</h3>
              ${unreadCount > 0 ? `<button class="dropdown__action" id="markAllReadBtn">${unreadCount} ${unreadCount > 1 ? i18n.t('unreads') : i18n.t('unread')} — ${i18n.t('markAll')}</button>` : `<span class="dropdown__action-muted">${i18n.t('allRead')}</span>`}
            </div>
            <div class="dropdown__list" id="notificationsList">
              ${renderNotificationsList()}
            </div>
          </div>
        </div>
        <div class="header__dropdown-wrapper">
          <div class="header__avatar" id="profileBtn" title="${CURRENT_USER?.name || 'Usuário'}">${CURRENT_USER?.initials || 'HD'}</div>
          <div class="header__dropdown header__dropdown--profile" id="profileDropdown">
            <div class="dropdown__profile-header">
              <div class="dropdown__profile-avatar">${CURRENT_USER?.initials || 'HD'}</div>
              <div class="dropdown__profile-info">
                <div class="dropdown__profile-name">${CURRENT_USER?.name || 'Usuário'}</div>
                <div class="dropdown__profile-email">${CURRENT_USER?.email || ''}</div>
              </div>
            </div>
            <div class="dropdown__divider"></div>
            <div class="dropdown__menu">
              <button class="dropdown__menu-item" id="profileGoSettings">
                ${Icons.settings}
                <span>${i18n.t('settings')}</span>
              </button>
              <button class="dropdown__menu-item dropdown__menu-item--danger" id="logoutBtn">
                ${Icons.logOut}
                <span>${i18n.t('logout')}</span>
              </button>
            </div>
            <div class="dropdown__profile-footer">
              <span class="dropdown__footer-role">${Icons.shieldCheck} ${i18n.role(CURRENT_USER?.role || 'usuario')}</span>
              <span class="dropdown__footer-dept">${CURRENT_USER?.department || ''}</span>
            </div>
          </div>
        </div>
      </div>
    </header>
  `;
}

// ── Component: Notifications List ──
function renderNotificationsList() {
  if (notifications.length === 0) {
    return `<div class="dropdown__empty">
      ${Icons.bell}
      <p>${i18n.t('noNotifications')}</p>
    </div>`;
  }

  const notifIconMap = {
    chamado_novo: Icons.plus,
    status_alterado: Icons.tag,
    comentario: Icons.messageSquare,
    atribuicao: Icons.user,
    sla_alerta: Icons.alertCircle,
    resolvido: Icons.checkCircle
  };

  const notifColorMap = {
    chamado_novo: 'notif--info',
    status_alterado: 'notif--warning',
    comentario: 'notif--primary',
    atribuicao: 'notif--primary',
    sla_alerta: 'notif--danger',
    resolvido: 'notif--success'
  };

  return notifications
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    .map(n => `
      <div class="dropdown__notif-item ${n.isRead ? '' : 'dropdown__notif-item--unread'}" data-notif-id="${n.id}" data-chamado-id="${n.ticketId}">
        <div class="dropdown__notif-icon ${notifColorMap[n.type] || 'notif--primary'}">
          ${notifIconMap[n.type] || Icons.bell}
        </div>
        <div class="dropdown__notif-content">
          <div class="dropdown__notif-title">${n.title}</div>
          <p class="dropdown__notif-message">${n.message}</p>
          <span class="dropdown__notif-time">${formatTimeAgo(n.createdAt)}</span>
        </div>
        ${!n.isRead ? '<div class="dropdown__notif-unread-dot"></div>' : ''}
      </div>
    `).join('');
}


// ── View: Dashboard ──
function renderDashboard() {
  const stats = getDashboardStats();
  const recentTickets = [...tickets]
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    .slice(0, 5);

  const connectionBadge = stats.isConnected
    ? `<span class="badge badge-resolved" style="display:inline-flex; align-items:center; gap:var(--space-1); font-size:var(--text-xs);"><span class="badge-dot"></span>${i18n.t('backendConnected')}</span>`
    : `<span class="badge badge-closed" style="display:inline-flex; align-items:center; gap:var(--space-1); font-size:var(--text-xs);"><span class="badge-dot"></span>${i18n.t('backendOffline')}</span>`;

  return `
    <div class="page-transition-enter">
      <div class="page-header" style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:var(--space-4);">
        <div>
          <h1 class="page-header__title">${i18n.t('dashboard')}</h1>
          <p class="page-header__subtitle" style="margin-bottom:0">${i18n.t('dashboardSubtitle')}</p>
        </div>
        <div style="display:flex; align-items:center; gap:var(--space-3);">
          ${connectionBadge}
          <button class="btn btn-ghost btn-sm" onclick="App.refreshData()" title="${i18n.t('refreshData')}">
            ${Icons.loader} ${i18n.t('refreshData')}
          </button>
        </div>
      </div>

      <!-- Stat Cards (100% Neon DB Real Data) -->
      <div class="stats-grid">
        <div class="stat-card" style="--stat-accent: var(--purple-500)">
          <div class="stat-card__info">
            <span class="stat-card__label">${i18n.t('totalTickets')}</span>
            <span class="stat-card__value">${stats.total}</span>
            <span class="stat-card__trend">${stats.total} registros no Neon</span>
          </div>
          <div class="stat-card__icon" style="--stat-icon-bg: var(--color-primary-light); --stat-icon-color: var(--color-primary)">
            ${Icons.clipboardList}
          </div>
        </div>
        <div class="stat-card" style="--stat-accent: var(--status-open)">
          <div class="stat-card__info">
            <span class="stat-card__label">${i18n.t('openTickets')}</span>
            <span class="stat-card__value">${stats.newCount}</span>
            ${stats.total > 0 ? `<span class="stat-card__trend ${stats.newRate > 50 ? 'up' : ''}">${stats.newRate}% do total</span>` : '<span class="stat-card__trend">Aguardando</span>'}
          </div>
          <div class="stat-card__icon" style="--stat-icon-bg: var(--status-open-bg); --stat-icon-color: var(--status-open)">
            ${Icons.inbox}
          </div>
        </div>
        <div class="stat-card" style="--stat-accent: var(--status-progress)">
          <div class="stat-card__info">
            <span class="stat-card__label">${i18n.t('assignedTickets')}</span>
            <span class="stat-card__value">${stats.assigned}</span>
            ${stats.total > 0 ? `<span class="stat-card__trend">${stats.assignedRate}% do total</span>` : '<span class="stat-card__trend">Com técnico</span>'}
          </div>
          <div class="stat-card__icon" style="--stat-icon-bg: var(--status-progress-bg); --stat-icon-color: var(--status-progress)">
            ${Icons.user}
          </div>
        </div>
        <div class="stat-card" style="--stat-accent: var(--status-resolved)">
          <div class="stat-card__info">
            <span class="stat-card__label">${i18n.t('resolvedTickets')}</span>
            <span class="stat-card__value">${stats.closed}</span>
            ${stats.total > 0 ? `<span class="stat-card__trend up">${Icons.trendingUp} ${stats.resolutionRate}% taxa</span>` : '<span class="stat-card__trend">Finalizados</span>'}
          </div>
          <div class="stat-card__icon" style="--stat-icon-bg: var(--status-resolved-bg); --stat-icon-color: var(--status-resolved)">
            ${Icons.shieldCheck}
          </div>
        </div>
      </div>

      <!-- Analytics Breakdown (Status + Categories from Neon DB) -->
      <div style="display:grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: var(--space-6); margin-bottom: var(--space-6);">
        <!-- Distribution by Status (Real Neon DB Statuses) -->
        <div class="detail-card">
          <div class="detail-card__header">
            <h3 class="detail-card__title">${i18n.t('distributionByStatus')}</h3>
          </div>
          <div class="detail-card__body">
            <div class="chart-bar-group">
              ${renderChartBar(i18n.status('NEW'), stats.newCount, stats.total, 'var(--status-open)')}
              ${renderChartBar(i18n.status('ASSIGNED'), stats.assigned, stats.total, 'var(--status-progress)')}
              ${renderChartBar(i18n.status('CLOSED'), stats.closed, stats.total, 'var(--status-resolved)')}
              ${renderChartBar(i18n.status('UNRESOLVED'), stats.unresolved, stats.total, 'var(--status-closed)')}
            </div>
          </div>
        </div>

        <!-- Distribution by Category (Real Neon DB Categories) -->
        <div class="detail-card">
          <div class="detail-card__header">
            <h3 class="detail-card__title">${i18n.t('distributionByCategory')}</h3>
          </div>
          <div class="detail-card__body">
            ${stats.categories && stats.categories.length > 0 ? `
              <div class="chart-bar-group">
                ${stats.categories.slice(0, 5).map(cat => renderChartBar(cat.name, cat.count, stats.total, 'var(--purple-500)')).join('')}
              </div>
            ` : `
              <p style="color:var(--color-text-secondary); text-align:center; padding:var(--space-4);">Nenhuma categoria cadastrada.</p>
            `}
          </div>
        </div>
      </div>

      <!-- Recent Tickets Table (Neon DB) -->
      <div class="detail-card">
        <div class="detail-card__header">
          <h3 class="detail-card__title">${i18n.t('recentTickets')}</h3>
          <a href="#chamados" class="btn btn-ghost btn-sm">${i18n.t('viewAll')}</a>
        </div>
        <div class="detail-card__body" style="padding:0;">
          ${recentTickets.length > 0 ? `
            <table class="data-table">
              <thead>
                <tr>
                  <th>${i18n.t('thId')}</th>
                  <th>${i18n.t('thTitle')}</th>
                  <th>${i18n.t('thCategory')}</th>
                  <th>${i18n.t('thPriority')}</th>
                  <th>${i18n.t('thStatus')}</th>
                  <th>${i18n.t('thDate')}</th>
                </tr>
              </thead>
              <tbody>
                ${recentTickets.map(t => {
                  const catName = t.categoryName || (getCategoryById(t.categoryId)?.name) || '-';
                  return `
                    <tr>
                      <td class="table-cell-id">#${String(t.id).padStart(4, '0')}</td>
                      <td class="table-cell-title">
                        <a href="#chamado/${t.id}" class="truncate" style="display:block;max-width:280px;">${t.title}</a>
                      </td>
                      <td><span class="badge badge-normal">${catName}</span></td>
                      <td><span class="badge ${i18n.priorityClass(t.priority)}">${i18n.priority(t.priority)}</span></td>
                      <td><span class="badge ${i18n.statusClass(t.status)}"><span class="badge-dot"></span>${i18n.status(t.status)}</span></td>
                      <td class="table-cell-date">${formatTimeAgo(t.createdAt)}</td>
                    </tr>
                  `;
                }).join('')}
              </tbody>
            </table>
          ` : `
            <div class="table-empty" style="padding: var(--space-8);">
              <p class="table-empty__text">${i18n.t('noTicketsFound')}</p>
            </div>
          `}
        </div>
      </div>
    </div>
  `;
}

function renderChartBar(label, value, total, color) {
  const pct = total > 0 ? Math.round((value / total) * 100) : 0;
  return `
    <div class="chart-bar-item">
      <span class="chart-bar-item__label">${label}</span>
      <div class="chart-bar-item__track">
        <div class="chart-bar-item__fill" style="width:${pct}%; background:${color};">
          ${pct > 10 ? pct + '%' : ''}
        </div>
      </div>
      <span class="chart-bar-item__value">${value}</span>
    </div>
  `;
}

// ── View: Tickets List ──
function renderTicketsList(filteredData) {
  const data = filteredData || tickets;
  const statusList = i18n.getStatusList();
  const priorityList = i18n.getPriorityList();

  return `
    <div class="page-transition-enter">
      <div class="page-header">
        <div>
          <h1 class="page-header__title">${i18n.t('tickets')}</h1>
          <p class="page-header__subtitle" style="margin-bottom:0">${i18n.t('ticketsSubtitle')}</p>
        </div>
        <button class="btn btn-primary" id="btnNewTicket">
          ${Icons.plus} ${i18n.t('newTicket')}
        </button>
      </div>

      <div class="table-container">
        <div class="table-toolbar">
          <div class="table-toolbar__filters">
            <div class="table-toolbar__search">
              <span class="table-toolbar__search-icon">${Icons.search}</span>
              <input type="text" class="table-toolbar__search-input" id="searchTickets" placeholder="${i18n.t('searchPlaceholder')}">
            </div>
            <select class="filter-select" id="filterStatus">
              <option value="">${i18n.t('allStatuses')}</option>
              ${statusList.map(s => `<option value="${s.id}">${s.label}</option>`).join('')}
            </select>
            <select class="filter-select" id="filterPriority">
              <option value="">${i18n.t('allPriorities')}</option>
              ${priorityList.map(p => `<option value="${p.id}">${p.label}</option>`).join('')}
            </select>
            <select class="filter-select" id="filterCategory">
              <option value="">${i18n.t('allCategories')}</option>
              ${CATEGORIES.map(c => `<option value="${c.id}">${c.name}</option>`).join('')}
            </select>
          </div>
        </div>

        ${data.length > 0 ? `
          <div style="overflow-x:auto;">
            <table class="data-table" id="ticketsTable">
              <thead>
                <tr>
                  <th data-sort="id">${i18n.t('thId')} <span class="sort-icon">${Icons.chevronsSort}</span></th>
                  <th data-sort="title">${i18n.t('thTitle')} <span class="sort-icon">${Icons.chevronsSort}</span></th>
                  <th data-sort="status">${i18n.t('thStatus')} <span class="sort-icon">${Icons.chevronsSort}</span></th>
                  <th data-sort="priority">${i18n.t('thPriority')} <span class="sort-icon">${Icons.chevronsSort}</span></th>
                  <th>${i18n.t('thCategory')}</th>
                  <th>${i18n.t('thRequester')}</th>
                  <th>${i18n.t('thTechnician')}</th>
                  <th data-sort="createdAt">${i18n.t('thDate')} <span class="sort-icon">${Icons.chevronsSort}</span></th>
                  <th>${i18n.t('thActions')}</th>
                </tr>
              </thead>
              <tbody>
                ${data.map(t => renderTicketRow(t)).join('')}
              </tbody>
            </table>
          </div>
          ${renderPagination(data.length)}
        ` : `
          <div class="table-empty">
            <div class="table-empty__icon">${Icons.inbox}</div>
            <h3 class="table-empty__title">${i18n.t('noTicketsFound')}</h3>
            <p class="table-empty__text">${i18n.t('noTicketsHint')}</p>
            <button class="btn btn-primary" id="btnNewTicketEmpty">
              ${Icons.plus} ${i18n.t('newTicket')}
            </button>
          </div>
        `}
      </div>
    </div>
  `;
}

function renderTicketRow(t) {
  const clientName = t.clientName || (getUserById(t.clientId)?.name) || '-';
  const supportName = t.supportName || (t.supportId ? getUserById(t.supportId)?.name : null);
  const categoryName = t.categoryName || (getCategoryById(t.categoryId)?.name) || '-';
  const clientUser = getUserById(t.clientId);
  const supportUser = t.supportId ? getUserById(t.supportId) : null;

  return `
    <tr data-ticket-id="${t.id}">
      <td class="table-cell-id">#${String(t.id).padStart(4, '0')}</td>
      <td class="table-cell-title">
        <a href="#chamado/${t.id}">${t.title}</a>
      </td>
      <td>
        <span class="badge ${i18n.statusClass(t.status)}">
          <span class="badge-dot"></span>
          ${i18n.status(t.status)}
        </span>
      </td>
      <td>
        <span class="badge ${i18n.priorityClass(t.priority)}">${i18n.priority(t.priority)}</span>
      </td>
      <td style="font-size:var(--text-sm); color:var(--color-text-secondary)">${categoryName}</td>
      <td>
        ${clientUser ? `
          <div class="table-cell-user">
            <div class="table-cell-user__avatar">${clientUser.initials}</div>
            <span style="font-size:var(--text-sm)">${clientUser.name.split(' ')[0]}</span>
          </div>
        ` : `<span style="font-size:var(--text-sm)">${clientName}</span>`}
      </td>
      <td>
        ${supportUser ? `
          <div class="table-cell-user">
            <div class="table-cell-user__avatar">${supportUser.initials}</div>
            <span style="font-size:var(--text-sm)">${supportUser.name.split(' ')[0]}</span>
          </div>
        ` : (supportName ? `<span style="font-size:var(--text-sm)">${supportName}</span>` : `<span style="font-size:var(--text-sm);color:var(--color-text-tertiary)">${i18n.t('notAssigned')}</span>`)}
      </td>
      <td class="table-cell-date">${formatDate(t.createdAt)}</td>
      <td>
        <div class="table-actions">
          <button class="btn-icon" title="${i18n.t('view')}" onclick="App.viewTicket(${t.id})">
            ${Icons.eye}
          </button>
          <button class="btn-icon" title="${i18n.t('edit')}" onclick="App.editTicket(${t.id})">
            ${Icons.edit}
          </button>
          <button class="btn-icon danger" title="${i18n.t('delete')}" onclick="App.confirmDeleteTicket(${t.id})">
            ${Icons.trash}
          </button>
        </div>
      </td>
    </tr>
  `;
}

function renderPagination(total) {
  const perPage = 10;
  const totalPages = Math.ceil(total / perPage);
  if (totalPages <= 1) return '';

  return `
    <div class="pagination">
      <span class="pagination__info">${i18n.t('showing')} ${Math.min(total, perPage)} ${i18n.t('of')} ${total} ${i18n.t('ticketsCount')}</span>
      <div class="pagination__controls">
        <button class="pagination__btn" disabled title="${i18n.t('previous')}">${Icons.chevronLeft}</button>
        ${Array.from({length: totalPages}, (_, idx) => `
          <button class="pagination__btn ${idx === 0 ? 'active' : ''}">${idx + 1}</button>
        `).join('')}
        <button class="pagination__btn" ${totalPages <= 1 ? 'disabled' : ''} title="${i18n.t('next')}">${Icons.chevronRight}</button>
      </div>
    </div>
  `;
}

// ── View: Ticket Detail ──
function renderTicketDetail(ticketData) {
  if (!ticketData) {
    return `
      <div class="page-transition-enter" style="text-align:center; padding:var(--space-16);">
        <div style="font-size:48px; margin-bottom:var(--space-4); opacity:0.3;">${Icons.alertCircle}</div>
        <h2>${i18n.t('ticketNotFound')}</h2>
        <p style="color:var(--color-text-secondary)">${i18n.t('ticketNotFoundMsg')}</p>
        <a href="#chamados" class="btn btn-primary" style="margin-top:var(--space-4);">${Icons.arrowLeft} ${i18n.t('backToTickets')}</a>
      </div>
    `;
  }

  const clientName = ticketData.clientName || (getUserById(ticketData.clientId)?.name) || '-';
  const supportName = ticketData.supportName || (ticketData.supportId ? getUserById(ticketData.supportId)?.name : null) || i18n.t('notAssigned');
  const categoryName = ticketData.categoryName || (getCategoryById(ticketData.categoryId)?.name) || '-';
  const statusList = i18n.getStatusList();
  const technicians = USERS.filter(u => isTIUser(u));

  return `
    <div class="page-transition-enter">
      <div class="page-header">
        <div style="display:flex; align-items:center; gap:var(--space-3);">
          <a href="#chamados" class="btn btn-ghost btn-icon" title="${i18n.t('backToTickets')}">${Icons.arrowLeft}</a>
          <div>
            <h1 class="page-header__title">${i18n.t('tickets')} #${String(ticketData.id).padStart(4, '0')}</h1>
            <p class="page-header__subtitle" style="margin-bottom:0">${i18n.t('openedAt')} ${formatDateTime(ticketData.createdAt)}</p>
          </div>
        </div>
        <div style="display:flex; gap:var(--space-2);">
          <button class="btn btn-secondary" onclick="App.editTicket(${ticketData.id})">${Icons.edit} ${i18n.t('edit')}</button>
          <button class="btn btn-danger" onclick="App.confirmDeleteTicket(${ticketData.id})">${Icons.trash} ${i18n.t('delete')}</button>
        </div>
      </div>

      <div class="ticket-detail">
        <div class="ticket-detail__main">
          <!-- Ticket Header -->
          <div class="ticket-header">
            <div class="ticket-header__top">
              <div>
                <span class="ticket-header__id">#${String(ticketData.id).padStart(4, '0')}</span>
                <h2 class="ticket-header__title">${ticketData.title}</h2>
              </div>
              <div class="ticket-header__badges">
                <span class="badge ${i18n.statusClass(ticketData.status)}"><span class="badge-dot"></span>${i18n.status(ticketData.status)}</span>
                <span class="badge ${i18n.priorityClass(ticketData.priority)}">${i18n.priority(ticketData.priority)}</span>
              </div>
            </div>
          </div>

          <!-- Description -->
          <div class="detail-card">
            <div class="detail-card__header">
              <h3 class="detail-card__title">${i18n.t('description')}</h3>
            </div>
            <div class="detail-card__body">
              <p style="margin-bottom:0; line-height:1.7; color:var(--color-text-secondary)">${ticketData.description}</p>
            </div>
          </div>

          <!-- Timeline -->
          <div class="detail-card">
            <div class="detail-card__header">
              <h3 class="detail-card__title">${i18n.t('activity')}</h3>
            </div>
            <div class="detail-card__body">
              <div class="timeline">
                ${(ticketData.timeline || []).map(item => {
                  const authorName = item.authorName || (getUserById(item.authorId)?.name) || i18n.t('systemAuthor');
                  let icon = Icons.messageSquare;
                  if (item.eventType === 'CREATION') icon = Icons.plus;
                  if (item.eventType === 'STATUS_CHANGE') icon = Icons.checkCircle;
                  if (item.eventType === 'ASSIGNMENT') icon = Icons.user;
                  if (item.eventType === 'PRIORITY_CHANGE') icon = Icons.tag;

                  return `
                    <div class="timeline-item">
                      <div class="timeline-item__dot">${icon}</div>
                      <div class="timeline-item__content">
                        <div class="timeline-item__header">
                          <span class="timeline-item__author">${authorName}</span>
                          <span class="timeline-item__time">${formatDateTime(item.createdAt)}</span>
                        </div>
                        <div class="timeline-item__body">${item.message}</div>
                      </div>
                    </div>
                  `;
                }).join('')}
              </div>
            </div>
          </div>
        </div>

        <!-- Sidebar -->
        <div class="ticket-detail__sidebar">
          <div class="detail-card">
            <div class="detail-card__header">
              <h3 class="detail-card__title">${i18n.t('information')}</h3>
            </div>
            <div class="detail-card__body">
              <div class="info-list">
                <div class="info-item">
                  <span class="info-item__icon">${Icons.user}</span>
                  <div class="info-item__content">
                    <span class="info-item__label">${i18n.t('requester')}</span>
                    <span class="info-item__value">${clientName}</span>
                  </div>
                </div>
                <div class="info-item">
                  <span class="info-item__icon">${Icons.user}</span>
                  <div class="info-item__content">
                    <span class="info-item__label">${i18n.t('technicianResponsible')}</span>
                    <span class="info-item__value">${supportName}</span>
                  </div>
                </div>
                <div class="info-item">
                  <span class="info-item__icon">${Icons.tag}</span>
                  <div class="info-item__content">
                    <span class="info-item__label">${i18n.t('category')}</span>
                    <span class="info-item__value">${categoryName}</span>
                  </div>
                </div>
                <div class="info-item">
                  <span class="info-item__icon">${Icons.calendar}</span>
                  <div class="info-item__content">
                    <span class="info-item__label">${i18n.t('createdAt')}</span>
                    <span class="info-item__value">${formatDateTime(ticketData.createdAt)}</span>
                  </div>
                </div>
                <div class="info-item">
                  <span class="info-item__icon">${Icons.clock}</span>
                  <div class="info-item__content">
                    <span class="info-item__label">${i18n.t('lastUpdate')}</span>
                    <span class="info-item__value">${formatDateTime(ticketData.updatedAt)}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Quick Actions (only for TI) -->
          ${isTIUser(CURRENT_USER) ? `
          <div class="detail-card">
            <div class="detail-card__header">
              <h3 class="detail-card__title">${i18n.t('quickActions')}</h3>
            </div>
            <div class="detail-card__body" style="display:flex; flex-direction:column; gap:var(--space-2);">
              ${!ticketData.supportId ? `
                <button class="btn btn-primary" onclick="App.selfAssignTicket(${ticketData.id})" style="width:100%;">
                  ${Icons.user} ${i18n.t('selfAssign')}
                </button>
              ` : ''}
              <select class="form-select" id="quickStatusChange" onchange="App.quickStatusChange(${ticketData.id}, this.value)">
                <option value="" disabled selected>${i18n.t('changeStatus')}</option>
                ${statusList.map(s => `<option value="${s.id}" ${s.id === ticketData.status ? 'disabled' : ''}>${s.label}</option>`).join('')}
              </select>
              <select class="form-select" id="quickTechnicianChange" onchange="App.quickTechnicianChange(${ticketData.id}, this.value)">
                <option value="" disabled selected>${i18n.t('assignTechnician')}</option>
                ${technicians.map(u => `<option value="${u.id}" ${u.id === ticketData.supportId ? 'disabled' : ''}>${u.name}</option>`).join('')}
              </select>
            </div>
          </div>
          ` : ''}
        </div>
      </div>
    </div>
  `;
}

// ── View: Settings ──
function renderSettings() {
  const currentTheme = ThemeManager.getTheme();

  return `
    <div class="page-transition-enter">
      <div class="page-header">
        <div>
          <h1 class="page-header__title">${i18n.t('settings')}</h1>
          <p class="page-header__subtitle" style="margin-bottom:0">Personalize sua experiência no sistema</p>
        </div>
      </div>

      <div class="settings-grid">
        <!-- Appearance -->
        <div class="settings-card">
          <div class="settings-card__header">
            <h3 class="settings-card__title">${i18n.t('appearance')}</h3>
            <p class="settings-card__desc">Escolha o tema de sua preferência</p>
          </div>
          <div class="settings-card__body">
            <div class="theme-switcher">
              <label class="theme-option theme-option--light">
                <input type="radio" name="theme" value="light" ${currentTheme === 'light' ? 'checked' : ''} onchange="App.changeTheme('light')">
                <div class="theme-option__preview">
                  <div class="theme-option__preview-mock">
                    <div class="theme-option__preview-mock-sidebar"></div>
                    <div class="theme-option__preview-mock-content"></div>
                  </div>
                </div>
                <div class="theme-option__label">${Icons.sun} ${i18n.t('lightTheme')}</div>
              </label>
              <label class="theme-option theme-option--dark">
                <input type="radio" name="theme" value="dark" ${currentTheme === 'dark' ? 'checked' : ''} onchange="App.changeTheme('dark')">
                <div class="theme-option__preview">
                  <div class="theme-option__preview-mock">
                    <div class="theme-option__preview-mock-sidebar"></div>
                    <div class="theme-option__preview-mock-content"></div>
                  </div>
                </div>
                <div class="theme-option__label">${Icons.moon} ${i18n.t('darkTheme')}</div>
              </label>
            </div>
          </div>
        </div>

        <!-- Profile -->
        <div class="settings-card">
          <div class="settings-card__header">
            <h3 class="settings-card__title">${i18n.t('profile')}</h3>
            <p class="settings-card__desc">Informações da sua conta</p>
          </div>
          <div class="settings-card__body">
            <div class="profile-info">
              <div class="profile-avatar">${CURRENT_USER?.initials || 'HD'}</div>
              <div class="profile-details">
                <div class="profile-name">${CURRENT_USER?.name || 'Usuário'}</div>
                <div class="profile-role">${CURRENT_USER?.jobTitle || 'Colaborador'}</div>
                <div class="profile-email">${CURRENT_USER?.email || ''}</div>
              </div>
            </div>
            <div style="margin-top:var(--space-5); display:grid; grid-template-columns:1fr 1fr; gap:var(--space-4);">
              <div>
                <span class="info-item__label">Departamento</span>
                <span class="info-item__value">${CURRENT_USER?.department || '-'}</span>
              </div>
              <div>
                <span class="info-item__label">${i18n.t('profile')}</span>
                <span class="info-item__value" style="text-transform:capitalize">${i18n.role(CURRENT_USER?.role || 'usuario')}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;
}

// ── Modal: Create/Edit Ticket ──
function renderTicketModal(ticket = null) {
  const isEdit = ticket !== null;
  const title = isEdit ? i18n.t('editTicket') : i18n.t('createTicket');
  const technicians = USERS.filter(u => isTIUser(u));
  const isTI = isTIUser(CURRENT_USER);
  const statusList = i18n.getStatusList();
  const priorityList = i18n.getPriorityList();

  // Status + Technician section — only visible in edit mode for TI users
  const statusTechSection = isEdit && isTI ? `
            <div class="form-row">
              <div class="form-group">
                <label class="form-label" for="ticketStatus">${i18n.t('statusLabel')}</label>
                <select class="form-select" id="ticketStatus">
                  ${statusList.map(s => `<option value="${s.id}" ${ticket.status === s.id ? 'selected' : ''}>${s.label}</option>`).join('')}
                </select>
              </div>
              <div class="form-group">
                <label class="form-label" for="ticketTechnician">${i18n.t('technicianLabel')}</label>
                <select class="form-select" id="ticketTechnician">
                  <option value="">${i18n.t('notAssigned')}</option>
                  ${technicians.map(u => `<option value="${u.id}" ${ticket.supportId === u.id ? 'selected' : ''}>${u.name}</option>`).join('')}
                </select>
              </div>
            </div>
  ` : '';

  // Requester section — only visible in edit mode for TI users
  const requesterSection = isEdit && isTI ? `
            <div class="form-group">
              <label class="form-label" for="ticketRequester">${i18n.t('requesterLabel')}</label>
              <select class="form-select" id="ticketRequester">
                ${USERS.map(u => `<option value="${u.id}" ${ticket.clientId === u.id ? 'selected' : ''}>${u.name} (${u.department || 'Geral'})</option>`).join('')}
              </select>
            </div>
  ` : '';

  // In creation mode, show who is creating (read-only)
  const requesterInfo = !isEdit && CURRENT_USER ? `
            <div class="form-group">
              <label class="form-label">${i18n.t('requesterLabel')}</label>
              <div class="form-input form-input--readonly" style="background: var(--bg-tertiary); cursor: default; display: flex; align-items: center; gap: var(--space-2);">
                ${Icons.user}
                <span>${CURRENT_USER.name} — ${CURRENT_USER.department || 'Geral'}</span>
              </div>
            </div>
  ` : '';

  return `
    <div class="modal-overlay active" id="ticketModal">
      <div class="modal modal--lg">
        <div class="modal__header">
          <h2 class="modal__title">${title}</h2>
          <button class="modal__close" onclick="App.closeModal()">${Icons.x}</button>
        </div>
        <div class="modal__body">
          <form id="ticketForm" novalidate>
            <input type="hidden" id="ticketId" value="${isEdit ? ticket.id : ''}">
            
            <div class="form-group">
              <label class="form-label" for="ticketTitle">${i18n.t('titleLabel')} <span class="required">*</span></label>
              <input type="text" class="form-input" id="ticketTitle" placeholder="${i18n.t('titlePlaceholder')}" value="${isEdit ? ticket.title : ''}" required>
              <span class="form-error" id="errorTitle"></span>
            </div>

            <div class="form-group">
              <label class="form-label" for="ticketDescription">${i18n.t('descriptionLabel')} <span class="required">*</span></label>
              <textarea class="form-textarea" id="ticketDescription" placeholder="${i18n.t('descriptionPlaceholder')}" required>${isEdit ? ticket.description : ''}</textarea>
              <span class="form-error" id="errorDescription"></span>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label class="form-label" for="ticketCategory">${i18n.t('categoryLabel')} <span class="required">*</span></label>
                <select class="form-select" id="ticketCategory" required>
                  <option value="">${i18n.t('categorySelect')}</option>
                  ${CATEGORIES.map(c => `<option value="${c.id}" ${isEdit && ticket.categoryId === c.id ? 'selected' : ''}>${c.name}</option>`).join('')}
                </select>
                <span class="form-error" id="errorCategory"></span>
              </div>
              <div class="form-group">
                <label class="form-label" for="ticketPriority">${i18n.t('priorityLabel')}</label>
                <select class="form-select" id="ticketPriority">
                  ${priorityList.map(p => `<option value="${p.id}" ${isEdit && ticket.priority === p.id ? 'selected' : (!isEdit && p.id === 'MEDIUM' ? 'selected' : '')}>${p.label}</option>`).join('')}
                </select>
              </div>
            </div>

            ${statusTechSection}
            ${requesterSection}
            ${requesterInfo}
          </form>
        </div>
        <div class="modal__footer">
          <button class="btn btn-secondary" onclick="App.closeModal()">${i18n.t('cancel')}</button>
          <button class="btn btn-primary" onclick="App.saveTicket()">${isEdit ? i18n.t('saveChanges') : i18n.t('createTicketBtn')}</button>
        </div>
      </div>
    </div>
  `;
}

// ── Modal: Delete Confirmation ──
function renderDeleteModal(ticket) {
  return `
    <div class="modal-overlay active" id="deleteModal">
      <div class="modal modal--sm">
        <div class="modal__header">
          <h2 class="modal__title">${i18n.t('confirmDelete')}</h2>
          <button class="modal__close" onclick="App.closeModal()">${Icons.x}</button>
        </div>
        <div class="modal__body">
          <p class="confirm-dialog__text">
            ${i18n.t('confirmDeleteMsg')} 
            <span class="confirm-dialog__highlight">#${String(ticket.id).padStart(4, '0')} — ${ticket.title}</span>?
          </p>
          <p class="confirm-dialog__text" style="margin-top:var(--space-3); color:var(--color-danger);">
            ${i18n.t('cannotUndo')}
          </p>
        </div>
        <div class="modal__footer">
          <button class="btn btn-secondary" onclick="App.closeModal()">${i18n.t('cancel')}</button>
          <button class="btn btn-danger" onclick="App.deleteTicket(${ticket.id})">${i18n.t('delete')}</button>
        </div>
      </div>
    </div>
  `;
}

// ── Toast Notification ──
function showToast(type, title, message) {
  const container = document.getElementById('toastContainer');
  if (!container) return;

  const iconMap = {
    success: Icons.checkCircle,
    error: Icons.xCircle,
    warning: Icons.alertCircle,
    info: Icons.info
  };

  const toast = document.createElement('div');
  toast.className = `toast toast--${type}`;
  toast.innerHTML = `
    <span class="toast__icon">${iconMap[type] || iconMap.info}</span>
    <div class="toast__content">
      <div class="toast__title">${title}</div>
      <p class="toast__message">${message}</p>
    </div>
    <button class="toast__close" onclick="this.closest('.toast').remove()">${Icons.x}</button>
  `;

  container.appendChild(toast);

  // Trigger animation
  requestAnimationFrame(() => {
    toast.classList.add('show');
  });

  // Auto remove
  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}
