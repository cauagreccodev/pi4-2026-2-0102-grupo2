/* ============================================
   HELP DESK PI IV — API Communication Module
   Handles all HTTP requests to the backend
   ============================================ */

const Api = (() => {
  const BASE_URL = 'https://help-desk-pi-iv.onrender.com';

  /**
   * Get the auth token from localStorage
   * @returns {string|null}
   */
  function getToken() {
    return localStorage.getItem('token');
  }

  /**
   * Make an authenticated API request
   * @param {string} method - HTTP method
   * @param {string} path - API path (e.g. '/api/chamados')
   * @param {Object|null} body - Request body (for POST/PUT)
   * @returns {Promise<Object>} Response data
   */
  async function request(method, path, body = null) {
    const headers = {
      'Content-Type': 'application/json'
    };

    const token = getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const options = { method, headers };
    if (body && (method === 'POST' || method === 'PUT')) {
      options.body = JSON.stringify(body);
    }

    const response = await fetch(`${BASE_URL}${path}`, options);

    // Handle 401 — redirect to login
    if (response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = 'login.html';
      throw new Error('Unauthorized');
    }

    const text = await response.text();
    let data = null;
    try {
      data = text ? JSON.parse(text) : null;
    } catch (e) {
      data = text;
    }

    if (!response.ok) {
      const errorMsg = (data && data.error) ? data.error : `HTTP ${response.status}`;
      throw new Error(errorMsg);
    }

    return data;
  }

  // ── Ticket API ──

  /**
   * Fetch all tickets (with optional filters)
   * @param {Object} filters - {status, clientId, supportId}
   * @returns {Promise<Array>} List of tickets mapped to frontend format
   */
  async function fetchTickets(filters = {}) {
    const params = new URLSearchParams();
    if (filters.status) params.append('status', filters.status);
    if (filters.clientId) params.append('clientId', filters.clientId);
    if (filters.supportId) params.append('supportId', filters.supportId);

    const query = params.toString();
    const path = `/api/chamados${query ? '?' + query : ''}`;
    const data = await request('GET', path);

    return Array.isArray(data) ? data.map(mapTicketFromApi) : [];
  }

  /**
   * Fetch a single ticket with its timeline
   * @param {number} id - Ticket ID
   * @returns {Promise<Object>} Ticket with timeline
   */
  async function fetchTicketById(id) {
    const cached = tickets.find(ticket => String(ticket.id) === String(id));
    if (cached && !canViewTicket(cached)) throw new Error(i18n.t('ticketAccessDenied'));
    const data = await request('GET', `/api/chamados/${id}`);

    const ticket = mapTicketFromApi(data.ticket);
    if (!canViewTicket(ticket)) throw new Error(i18n.t('ticketAccessDenied'));
    ticket.timeline = Array.isArray(data.timeline)
      ? data.timeline.map(mapTimelineFromApi)
      : [];

    return ticket;
  }

  /**
   * Create a new ticket
   * @param {Object} ticketData - {title, description, priority, categoryId, supportId}
   * @returns {Promise<Object>} Created ticket
   */
  async function createTicket(ticketData) {
    const payload = {
      title: ticketData.title,
      description: ticketData.description,
      priority: ticketData.priority,
      categoryId: ticketData.categoryId,
      supportId: ticketData.supportId || null
    };
    const data = await request('POST', '/api/chamados', payload);
    return mapTicketFromApi(data);
  }

  /**
   * Update a ticket (status, supportId, message)
   * @param {number} id - Ticket ID
   * @param {Object} updateData - {status, supportId, message}
   * @returns {Promise<Object>} Response
   */
  async function updateTicket(id, updateData) {
    if (!isTIUser()) throw new Error(i18n.t('noPermissionMsg'));
    return await request('PUT', `/api/chamados/${id}`, updateData);
  }

  /**
   * Delete a ticket
   * @param {number} id - Ticket ID
   * @returns {Promise<Object>} Response
   */
  async function deleteTicket(id) {
    if (!isTIUser()) throw new Error(i18n.t('noPermissionMsg'));
    return await request('DELETE', `/api/chamados/${id}`);
  }

  // ── Users API ──

  /**
   * Fetch all users
   * @returns {Promise<Array>} List of users mapped to frontend format
   */
  async function fetchUsers() {
    const data = await request('GET', '/api/users');
    return Array.isArray(data) ? data.map(mapUserFromApi) : [];
  }

  // ── Categories API ──

  /**
   * Fetch all categories
   * @returns {Promise<Array>} List of categories
   */
  async function fetchCategories() {
    const data = await request('GET', '/api/categorias');
    return Array.isArray(data) ? data.map(mapCategoryFromApi) : [];
  }

  // ── Notifications API ──

  /**
   * Fetch notifications for current user from Neon DB
   * @returns {Promise<Array>} List of notifications
   */
  async function fetchNotifications() {
    const data = await request('GET', '/api/notificacoes');
    return Array.isArray(data) ? data.map(mapNotificationFromApi) : [];
  }

  /**
   * Mark a notification as read in Neon DB
   * @param {number} id - Notification ID
   * @returns {Promise<Object>}
   */
  async function markNotificationAsRead(id) {
    return await request('PUT', `/api/notificacoes/${id}/read`);
  }

  // ── Data Mappers (API → Frontend) ──

  /**
   * Map a ticket from API format to frontend format
   * All field names stay in English
   */
  function mapTicketFromApi(apiTicket) {
    return {
      id: apiTicket.id,
      title: apiTicket.title || '',
      description: apiTicket.description || '',
      status: apiTicket.status || 'NEW',
      priority: apiTicket.priority || 'MEDIUM',
      categoryId: apiTicket.categoryId || 0,
      clientId: apiTicket.clientId || 0,
      supportId: apiTicket.supportId || null,
      createdAt: apiTicket.createdAt || '',
      updatedAt: apiTicket.updatedAt || '',
      closedAt: apiTicket.closedAt || null,
      // Extra fields from JOINs
      clientName: apiTicket.clientName || null,
      supportName: apiTicket.supportName || null,
      categoryName: apiTicket.categoryName || null,
      // Timeline (populated separately on detail view)
      timeline: apiTicket.timeline || []
    };
  }

  /**
   * Map a timeline event from API format
   */
  function mapTimelineFromApi(apiEvent) {
    return {
      id: apiEvent.id,
      ticketId: apiEvent.ticketId,
      authorId: apiEvent.authorId,
      eventType: apiEvent.eventType || 'COMMENT',
      message: apiEvent.message || '',
      createdAt: apiEvent.createdAt || '',
      authorName: apiEvent.authorName || null
    };
  }

  /**
   * Map a notification from API format
   */
  function mapNotificationFromApi(apiNotif) {
    return {
      id: apiNotif.id,
      ticketId: apiNotif.ticketId || null,
      type: apiNotif.type || 'chamado_novo',
      title: apiNotif.title || 'Notificação',
      message: apiNotif.message || '',
      isRead: Boolean(apiNotif.read || apiNotif.isRead),
      createdAt: apiNotif.createdAt || ''
    };
  }

  /**
   * Map a user from API format
   */
  function mapUserFromApi(apiUser) {
    const name = apiUser.name || 'Usuário';
    const initials = name
      .split(' ')
      .filter(Boolean)
      .map(n => n[0])
      .slice(0, 2)
      .join('')
      .toUpperCase() || 'HD';

    const rawRole = String(apiUser.role || 'CLIENT').toUpperCase();
    let role = 'usuario';
    if (rawRole === 'ADMIN') role = 'admin';
    else if (rawRole === 'SUPPORT' || rawRole === 'TECNICO') role = 'tecnico';
    else if (rawRole === 'CLIENT' || rawRole === 'USUARIO') role = 'usuario';

    return {
      id: apiUser.id,
      name: name,
      email: apiUser.email || '',
      role: role,
      rawRole: rawRole,
      department: apiUser.department || '',
      jobTitle: apiUser.jobTitle || '',
      initials: initials
    };
  }

  /**
   * Map a category from API format
   */
  function mapCategoryFromApi(apiCat) {
    return {
      id: apiCat.id,
      name: apiCat.name || '',
      icon: apiCat.icon || '',
      description: apiCat.description || ''
    };
  }

  return {
    fetchTickets,
    fetchTicketById,
    createTicket,
    updateTicket,
    deleteTicket,
    fetchUsers,
    fetchCategories,
    fetchNotifications,
    markNotificationAsRead
  };
})();
