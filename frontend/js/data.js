/* ============================================
   HELP DESK PI IV — Data Layer
   Global state, Neon DB synchronization & analytics
   All metrics strictly derived from real backend data
   ============================================ */

// ── Global State (populated directly by Neon API) ──
let tickets = [];
let USERS = [];
let CATEGORIES = [];
let CURRENT_USER = null;
let notifications = [];
let ticketAlerts = [];
let seenTicketIds = new Set();
let ticketAlertsUser = null;

function saveTicketAlerts() {
  try {
    localStorage.setItem(`helpdesk-ticket-alerts:${CURRENT_USER.id}`, JSON.stringify({
      seen: [...seenTicketIds], items: ticketAlerts
    }));
  } catch { /* Notifications still work when browser storage is unavailable. */ }
}

function syncTicketAlerts(announce = true) {
  if (!isTIUser()) {
    ticketAlerts = [];
    ticketAlertsUser = null;
    return;
  }
  if (ticketAlertsUser !== String(CURRENT_USER.id)) {
    ticketAlertsUser = String(CURRENT_USER.id);
    let saved;
    try { saved = JSON.parse(localStorage.getItem(`helpdesk-ticket-alerts:${CURRENT_USER.id}`)); } catch {}
    ticketAlerts = Array.isArray(saved?.items) ? saved.items : [];
    seenTicketIds = new Set(Array.isArray(saved?.seen) ? saved.seen.map(String) : tickets.map(t => String(t.id)));
  }
  const added = [];
  for (const ticket of tickets) {
    const id = String(ticket.id);
    if (seenTicketIds.has(id)) continue;
    seenTicketIds.add(id);
    const department = getRequesterDepartment(ticket);
    if (department === 'TI') continue;
    const alert = {
      id: `ticket-opened:${id}`, ticketId: ticket.id, type: 'chamado_novo',
      title: `Chamado aberto (${department})`, message: `#${id} — ${String(ticket.title || '').slice(0, 160)}`,
      createdAt: ticket.createdAt || new Date().toISOString(), isRead: false, local: true
    };
    ticketAlerts.unshift(alert);
    added.push(alert);
  }
  ticketAlerts = ticketAlerts.slice(0, 100);
  saveTicketAlerts();
  if (announce && added.length && typeof App !== 'undefined') App.notifyNewTickets(added);
}

// ── Connection & Synchronization State ──
let isBackendConnected = false;
let lastSyncTimestamp = null;

// ── Notification Helpers (synced with Neon DB) ──
function getUnreadNotificationsCount() {
  return getVisibleNotifications().filter(n => !n.isRead).length;
}

async function markNotificationAsRead(id) {
  const alert = ticketAlerts.find(n => String(n.id) === String(id));
  if (alert) {
    alert.isRead = true;
    saveTicketAlerts();
    return;
  }
  const notif = notifications.find(n => String(n.id) === String(id));
  if (notif) notif.isRead = true;
  try {
    await Api.markNotificationAsRead(id);
  } catch (err) {
    console.warn('Could not sync notification read status to backend:', err);
  }
}

async function markAllNotificationsAsRead() {
  const unread = getVisibleNotifications().filter(n => !n.isRead);
  unread.forEach(n => n.isRead = true);
  saveTicketAlerts();
  try {
    await Promise.all(unread.filter(n => !n.local).map(n => Api.markNotificationAsRead(n.id)));
  } catch (err) {
    console.warn('Could not sync all notifications read status to backend:', err);
  }
}

// ── Helpers: Users & Categories ──
function getUserById(id) {
  return USERS.find(u => String(u.id) === String(id)) || null;
}

function getRequesterDepartment(ticket) {
  const requester = getUserById(ticket.clientId) ||
    (CURRENT_USER && String(CURRENT_USER.id) === String(ticket.clientId) ? CURRENT_USER : null);
  return normalizeDepartment(requester?.department || requester?.departamento || ticket.clientDepartment) || 'Não informado';
}

function getUserByName(name) {
  return USERS.find(u => u.name === name) || null;
}

function getCategoryById(id) {
  return CATEGORIES.find(c => c.id === id) || null;
}

function isTIUser(user = CURRENT_USER) {
  return Boolean(user) && normalizeDepartment(user.department || user.departamento) === 'TI';
}

function getCurrentDepartment() {
  return normalizeDepartment(CURRENT_USER?.department || CURRENT_USER?.departamento);
}

function getHomeRoute() {
  return isTIUser() ? 'dashboard' : 'meu-departamento';
}

function getDashboardTitle() {
  return isTIUser() ? i18n.t('dashboard') : `${i18n.t('departmentDashboard')} — ${getCurrentDepartment() || i18n.t('noDepartment')}`;
}

// A sector can follow requests opened by its members or assigned to its members.
// Unknown departments never grant access to unrelated tickets.
function canViewTicket(ticket, user = CURRENT_USER) {
  if (!ticket || !user) return false;
  if (isTIUser(user)) return true;
  const department = normalizeDepartment(user.department || user.departamento);
  if (!DEPARTMENTS.includes(department)) return false;
  return [ticket.clientId, ticket.supportId].some(id => {
    if (!id) return false;
    const member = String(id) === String(user.id) ? user : getUserById(id);
    return member && normalizeDepartment(member.department || member.departamento) === department;
  });
}

function getVisibleTickets(source = tickets) {
  return source.filter(ticket => canViewTicket(ticket));
}

function getVisibleNotifications() {
  if (isTIUser()) {
    const localIds = new Set(ticketAlerts.map(n => String(n.ticketId)));
    return [...ticketAlerts, ...notifications.filter(n => {
      if (n.type !== 'chamado_novo') return true;
      const ticket = tickets.find(t => String(t.id) === String(n.ticketId));
      return !localIds.has(String(n.ticketId)) && ticket && getRequesterDepartment(ticket) !== 'TI';
    })];
  }
  const visibleIds = new Set(getVisibleTickets().map(ticket => String(ticket.id)));
  return notifications.filter(notification => notification.ticketId && visibleIds.has(String(notification.ticketId)));
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, character => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[character]));
}

function getAssignmentDepartments() {
  return [...DEPARTMENTS];
}

function getAssignmentUsers(department = '') {
  return USERS.filter(user => !department || normalizeDepartment(user.department) === department)
    .slice().sort((a, b) => a.name.localeCompare(b.name, 'pt-BR'));
}

// ── Dashboard Statistics (100% computed from real Neon DB data) ──
function getDashboardStats() {
  const tickets = getVisibleTickets();
  const total = tickets.length;

  // Real Neon DB Status counts (CHECK: status IN ('NEW', 'ASSIGNED', 'CLOSED', 'UNRESOLVED'))
  const newCount   = tickets.filter(t => t.status === 'NEW').length;
  const assigned   = tickets.filter(t => t.status === 'ASSIGNED').length;
  const closed     = tickets.filter(t => t.status === 'CLOSED').length;
  const unresolved = tickets.filter(t => t.status === 'UNRESOLVED').length;
  const open       = newCount + assigned;

  // Real percentage metrics derived from Neon records
  const resolutionRate = total > 0 ? Math.round((closed / total) * 100) : 0;
  const newRate        = total > 0 ? Math.round((newCount / total) * 100) : 0;
  const assignedRate   = total > 0 ? Math.round((assigned / total) * 100) : 0;
  const unresolvedRate = total > 0 ? Math.round((unresolved / total) * 100) : 0;

  // Real Neon DB Priority breakdown
  const priorities = {
    LOW:    tickets.filter(t => t.priority === 'LOW').length,
    MEDIUM: tickets.filter(t => t.priority === 'MEDIUM').length,
    HIGH:   tickets.filter(t => t.priority === 'HIGH').length,
    URGENT: tickets.filter(t => t.priority === 'URGENT').length
  };

  // Real breakdown by category from Neon DB
  const categoriesBreakdown = CATEGORIES.map(cat => {
    const count = tickets.filter(t => t.categoryId === cat.id).length;
    const percentage = total > 0 ? Math.round((count / total) * 100) : 0;
    return {
      id: cat.id,
      name: cat.name,
      icon: cat.icon,
      count,
      percentage
    };
  }).sort((a, b) => b.count - a.count);

  return {
    total,
    newCount,
    assigned,
    closed,
    unresolved,
    open,
    resolutionRate,
    newRate,
    assignedRate,
    unresolvedRate,
    priorities,
    categories: categoriesBreakdown,
    isConnected: isBackendConnected,
    lastSync: lastSyncTimestamp
  };
}

// ── Date Formatting Helpers ──
function formatDate(dateStr) {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return '-';
  return date.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
}

function formatDateTime(dateStr) {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return '-';
  return date.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

function formatTimeAgo(dateStr) {
  if (!dateStr) return '-';
  const now = new Date();
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return '-';
  const diffMs = now - date;
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return 'Agora mesmo';
  if (diffMins < 60) return `${diffMins}min atrás`;
  if (diffHours < 24) return `${diffHours}h atrás`;
  if (diffDays < 7) return `${diffDays}d atrás`;
  return formatDate(dateStr);
}

/**
 * Load all application data from the Neon backend API.
 * Synchronizes tickets, users, categories and notifications.
 * @returns {Promise<boolean>} True if loaded successfully, false on error
 */
async function loadAppData() {
  try {
    const [ticketsData, usersData, categoriesData, notificationsData] = await Promise.all([
      Api.fetchTickets(),
      Api.fetchUsers(),
      Api.fetchCategories(),
      Api.fetchNotifications().catch(err => {
        console.warn('Could not load notifications from backend:', err);
        return [];
      })
    ]);

    tickets = Array.isArray(ticketsData) ? ticketsData : [];
    USERS = Array.isArray(usersData) ? usersData : [];
    CATEGORIES = Array.isArray(categoriesData) ? categoriesData : [];
    notifications = Array.isArray(notificationsData) ? notificationsData : [];

    // Use the current server profile so department changes take effect on reload.
    const serverUser = USERS.find(user => user.id === CURRENT_USER?.id);
    if (serverUser) {
      CURRENT_USER = { ...CURRENT_USER, ...serverUser };
      window.CURRENT_USER = CURRENT_USER;
    }
    syncTicketAlerts(false);

    isBackendConnected = true;
    lastSyncTimestamp = new Date();
    return true;
  } catch (error) {
    console.error('Error loading app data from backend:', error);
    isBackendConnected = false;
    return false;
  }
}

/**
 * Reload only tickets and notifications from the API (after CRUD operations)
 * @returns {Promise<boolean>}
 */
async function reloadTickets() {
  try {
    const [ticketsData, notificationsData] = await Promise.all([
      Api.fetchTickets(),
      Api.fetchNotifications().catch(() => notifications)
    ]);

    tickets = Array.isArray(ticketsData) ? ticketsData : [];
    notifications = Array.isArray(notificationsData) ? notificationsData : [];

    syncTicketAlerts();

    isBackendConnected = true;
    lastSyncTimestamp = new Date();
    return true;
  } catch (error) {
    console.error('Error reloading tickets from backend:', error);
    return false;
  }
}
