/* ============================================
   HELP DESK PI IV — Main Application
   Ticket CRUD via API + Initialization
   All operations persist to Neon DB
   ============================================ */

const App = (() => {
  // ── State ──
  let currentFilters = {
    search: '',
    status: '',
    priority: '',
    category: ''
  };
  let sortField = 'createdAt';
  let sortDir = 'desc';

  // ── Initialization ──
  async function init() {
    // Init theme
    ThemeManager.init();

    // Show loading state
    const appEl = document.getElementById('app');

    // Load data from API FIRST (so sidebar/dashboard show real data)
    await loadAppData();

    // Build base layout (sidebar now has real ticket count)
    appEl.innerHTML = `
      ${renderSidebar()}
      <div class="sidebar-overlay" id="sidebarOverlay"></div>
      <div class="main-wrapper" id="mainWrapper">
        <div id="headerContainer"></div>
        <main class="page-content" id="pageContent"></main>
      </div>
      <div class="toast-container" id="toastContainer"></div>
      <div id="modalContainer"></div>
    `;

    // Register routes
    Router.register('dashboard', () => {
      renderPage(i18n.t('dashboard'), [i18n.t('home'), i18n.t('dashboard')], renderDashboard());
    });

    Router.register('chamados', () => {
      renderPage(i18n.t('tickets'), [i18n.t('home'), i18n.t('tickets')], renderTicketsList(getFilteredTickets()));
      bindTicketsEvents();
    });

    Router.register('chamado/:id', async (params) => {
      // Show loading state
      renderPage(i18n.t('tickets'), [i18n.t('home'), i18n.t('tickets'), '...'], `
        <div style="display:flex;align-items:center;justify-content:center;padding:var(--space-16);">
          <div class="animate-spin" style="color:var(--color-primary);">${Icons.loader}</div>
        </div>
      `);

      try {
        const ticketData = await Api.fetchTicketById(parseInt(params.id));
        const title = ticketData ? `${i18n.t('tickets')} #${String(ticketData.id).padStart(4, '0')}` : i18n.t('tickets');
        renderPage(title, [i18n.t('home'), i18n.t('tickets'), title], renderTicketDetail(ticketData));
      } catch (error) {
        console.error('Error loading ticket:', error);
        renderPage(i18n.t('tickets'), [i18n.t('home'), i18n.t('tickets')], renderTicketDetail(null));
      }
    });

    Router.register('configuracoes', () => {
      renderPage(i18n.t('settings'), [i18n.t('home'), i18n.t('settings')], renderSettings());
    });

    // Bind sidebar & header events
    bindGlobalEvents();

    // Start router
    Router.init();
  }

  // ── Page Rendering ──
  function renderPage(title, breadcrumbs, content) {
    document.title = `${title} — HelpDesk`;
    document.getElementById('headerContainer').innerHTML = renderHeader(breadcrumbs);
    document.getElementById('pageContent').innerHTML = content;

    // Re-bind header events
    bindHeaderEvents();
  }

  // ── Filters & Search ──
  function getFilteredTickets() {
    let data = [...tickets];

    // Search filter
    if (currentFilters.search) {
      const q = currentFilters.search.toLowerCase();
      data = data.filter(t =>
        t.title.toLowerCase().includes(q) ||
        String(t.id).includes(q) ||
        t.description.toLowerCase().includes(q)
      );
    }

    // Status filter
    if (currentFilters.status) {
      data = data.filter(t => t.status === currentFilters.status);
    }

    // Priority filter
    if (currentFilters.priority) {
      data = data.filter(t => t.priority === currentFilters.priority);
    }

    // Category filter
    if (currentFilters.category) {
      data = data.filter(t => t.categoryId === parseInt(currentFilters.category));
    }

    // Sorting (strictly Neon DB fields)
    const priorityOrder = { URGENT: 0, HIGH: 1, MEDIUM: 2, LOW: 3 };
    const statusOrder = { NEW: 0, ASSIGNED: 1, CLOSED: 2, UNRESOLVED: 3 };

    data.sort((a, b) => {
      let valA, valB;

      switch (sortField) {
        case 'id':
          valA = a.id;
          valB = b.id;
          break;
        case 'title':
          valA = a.title.toLowerCase();
          valB = b.title.toLowerCase();
          break;
        case 'status':
          valA = statusOrder[a.status] ?? 99;
          valB = statusOrder[b.status] ?? 99;
          break;
        case 'priority':
          valA = priorityOrder[a.priority] ?? 99;
          valB = priorityOrder[b.priority] ?? 99;
          break;
        case 'createdAt':
        default:
          valA = new Date(a.createdAt).getTime();
          valB = new Date(b.createdAt).getTime();
          break;
      }

      if (valA < valB) return sortDir === 'asc' ? -1 : 1;
      if (valA > valB) return sortDir === 'asc' ? 1 : -1;
      return 0;
    });

    return data;
  }

  function applyFilters() {
    const content = renderTicketsList(getFilteredTickets());
    document.getElementById('pageContent').innerHTML = content;
    bindTicketsEvents();
  }

  // ── CRUD: Create ──
  function openNewTicket() {
    const container = document.getElementById('modalContainer');
    container.innerHTML = renderTicketModal(null);
    document.body.style.overflow = 'hidden';
  }

  // ── CRUD: Read (View) ──
  function viewTicket(id) {
    Router.navigate(`#chamado/${id}`);
  }

  // ── CRUD: Update (Edit) ──
  async function editTicket(id) {
    try {
      const ticket = await Api.fetchTicketById(id);
      if (!ticket) return;
      const container = document.getElementById('modalContainer');
      container.innerHTML = renderTicketModal(ticket);
      document.body.style.overflow = 'hidden';
    } catch (error) {
      console.error('Error loading ticket for edit:', error);
      showToast('error', i18n.t('error'), i18n.t('loadError'));
    }
  }

  // ── CRUD: Save (Create or Update) ──
  async function saveTicket() {
    const id = document.getElementById('ticketId').value;
    const title = document.getElementById('ticketTitle').value.trim();
    const description = document.getElementById('ticketDescription').value.trim();
    const categoryId = document.getElementById('ticketCategory').value;
    const priority = document.getElementById('ticketPriority').value || 'MEDIUM';

    // Conditional fields (only exist for TI users in edit mode)
    const statusEl = document.getElementById('ticketStatus');
    const technicianEl = document.getElementById('ticketTechnician');

    // Validation
    let valid = true;

    if (!title) {
      showFieldError('ticketTitle', 'errorTitle', i18n.t('titleRequired'));
      valid = false;
    } else {
      clearFieldError('ticketTitle', 'errorTitle');
    }

    if (!description) {
      showFieldError('ticketDescription', 'errorDescription', i18n.t('descriptionRequired'));
      valid = false;
    } else {
      clearFieldError('ticketDescription', 'errorDescription');
    }

    if (!categoryId) {
      showFieldError('ticketCategory', 'errorCategory', i18n.t('categoryRequired'));
      valid = false;
    } else {
      clearFieldError('ticketCategory', 'errorCategory');
    }

    if (!valid) return;

    try {
      if (id) {
        // ── UPDATE ──
        const newStatus = statusEl ? statusEl.value : null;
        const newSupportId = technicianEl && technicianEl.value ? parseInt(technicianEl.value) : null;

        const changes = [];
        if (newStatus) changes.push(i18n.status(newStatus));

        const updatePayload = {
          status: newStatus || 'NEW',
          supportId: newSupportId,
          message: `Chamado atualizado por ${CURRENT_USER.name}. Alterações: ${changes.length > 0 ? changes.join(', ') : 'dados gerais'}.`
        };

        await Api.updateTicket(parseInt(id), updatePayload);
        await reloadTickets();

        closeModal();
        showToast('success', i18n.t('ticketUpdated'), `${i18n.t('tickets')} #${String(id).padStart(4, '0')} foi atualizado com sucesso.`);
      } else {
        // ── CREATE ──
        const newTicket = await Api.createTicket({
          title,
          description,
          priority,
          categoryId: parseInt(categoryId)
        });

        await reloadTickets();

        closeModal();
        showToast('success', i18n.t('ticketCreated'), `${i18n.t('tickets')} #${String(newTicket.id).padStart(4, '0')} foi criado com sucesso.`);
      }

      // Refresh view
      refreshCurrentView();
    } catch (error) {
      console.error('Error saving ticket:', error);
      showToast('error', i18n.t('error'), i18n.t('saveError'));
    }
  }

  // ── CRUD: Delete ──
  function confirmDeleteTicket(id) {
    const ticket = tickets.find(t => t.id === id);
    if (!ticket) return;
    const container = document.getElementById('modalContainer');
    container.innerHTML = renderDeleteModal(ticket);
    document.body.style.overflow = 'hidden';
  }

  async function deleteTicket(id) {
    try {
      await Api.deleteTicket(id);
      await reloadTickets();

      closeModal();
      showToast('success', i18n.t('ticketDeleted'), `${i18n.t('tickets')} #${String(id).padStart(4, '0')} foi excluído com sucesso.`);

      // If on detail page, go back to list
      if (Router.getCurrentRoute() && Router.getCurrentRoute().startsWith('chamado/')) {
        Router.navigate('#chamados');
      } else {
        refreshCurrentView();
      }
    } catch (error) {
      console.error('Error deleting ticket:', error);
      showToast('error', i18n.t('error'), i18n.t('deleteError'));
    }
  }

  // ── Quick Actions ──
  async function quickStatusChange(ticketId, newStatus) {
    if (!newStatus) return;

    try {
      await Api.updateTicket(ticketId, {
        status: newStatus,
        message: `Status alterado para "${i18n.status(newStatus)}" por ${CURRENT_USER.name}.`
      });

      await reloadTickets();
      showToast('success', i18n.t('statusChanged'), `${i18n.t('tickets')} #${String(ticketId).padStart(4, '0')} agora está "${i18n.status(newStatus)}".`);
      refreshCurrentView();
    } catch (error) {
      console.error('Error updating status:', error);
      showToast('error', i18n.t('error'), i18n.t('saveError'));
    }
  }

  async function quickTechnicianChange(ticketId, technicianId) {
    if (!technicianId) return;

    const technician = getUserById(parseInt(technicianId));
    const techName = technician ? technician.name : 'Técnico';

    try {
      // Get current ticket to check status
      const ticket = tickets.find(t => t.id === ticketId);
      const newStatus = (ticket && ticket.status === 'NEW') ? 'ASSIGNED' : (ticket ? ticket.status : 'ASSIGNED');

      await Api.updateTicket(ticketId, {
        status: newStatus,
        supportId: parseInt(technicianId),
        message: `Chamado atribuído ao técnico ${techName} por ${CURRENT_USER.name}.`
      });

      await reloadTickets();
      showToast('success', i18n.t('techAssigned'), `${techName} foi atribuído ao chamado #${String(ticketId).padStart(4, '0')}.`);
      refreshCurrentView();
    } catch (error) {
      console.error('Error assigning technician:', error);
      showToast('error', i18n.t('error'), i18n.t('saveError'));
    }
  }

  // ── Self-assign: Technician assigns themselves ──
  function selfAssignTicket(ticketId) {
    if (!CURRENT_USER) return;
    if (!isTIUser(CURRENT_USER)) {
      showToast('error', i18n.t('noPermission'), i18n.t('noPermissionMsg'));
      return;
    }
    quickTechnicianChange(ticketId, CURRENT_USER.id);
  }

  // ── Theme ──
  function changeTheme(theme) {
    ThemeManager.setTheme(theme);
    // Update header icon
    const btn = document.getElementById('themeToggleBtn');
    if (btn) {
      btn.innerHTML = ThemeManager.isDark() ? Icons.sun : Icons.moon;
    }
    showToast('info', i18n.t('themeChanged'), theme === 'dark' ? i18n.t('darkApplied') : i18n.t('lightApplied'));
  }

  // ── Helpers ──
  function closeModal() {
    const container = document.getElementById('modalContainer');
    const overlay = container.querySelector('.modal-overlay');
    if (overlay) {
      overlay.classList.remove('active');
      setTimeout(() => {
        container.innerHTML = '';
        document.body.style.overflow = '';
      }, 200);
    }
  }

  function updateSidebarBadges() {
    const badge = document.getElementById('sidebarTicketCount') || document.querySelector('.sidebar__link-badge');
    if (badge) {
      const stats = getDashboardStats();
      badge.textContent = stats.total;
    }
  }

  function updateNotificationBadges() {
    const unreadCount = getUnreadNotificationsCount();
    const btn = document.getElementById('notificationsBtn');
    if (btn) {
      const dot = btn.querySelector('.notification-dot');
      if (unreadCount > 0 && !dot) {
        btn.insertAdjacentHTML('beforeend', '<span class="notification-dot"></span>');
      } else if (unreadCount === 0 && dot) {
        dot.remove();
      }
    }
    const list = document.getElementById('notificationsList');
    if (list) {
      list.innerHTML = renderNotificationsList();
    }
    const markAllBtn = document.getElementById('markAllReadBtn');
    if (markAllBtn) {
      if (unreadCount > 0) {
        markAllBtn.textContent = `${unreadCount} ${unreadCount > 1 ? i18n.t('unreads') : i18n.t('unread')} — ${i18n.t('markAll')}`;
      } else {
        markAllBtn.outerHTML = `<span class="dropdown__action-muted">${i18n.t('allRead')}</span>`;
      }
    }
  }

  function refreshCurrentView() {
    updateSidebarBadges();
    updateNotificationBadges();
    Router.resolve();
  }

  async function refreshData() {
    showToast('info', 'Sincronizando...', 'Buscando dados atualizados do Neon DB.');
    const ok = await loadAppData();
    if (ok) {
      refreshCurrentView();
      showToast('success', 'Sincronizado', 'Dados sincronizados com sucesso do banco de dados.');
    } else {
      showToast('error', 'Erro de Conexão', 'Não foi possível conectar à API do Neon.');
    }
  }

  function showFieldError(inputId, errorId, message) {
    const input = document.getElementById(inputId);
    const error = document.getElementById(errorId);
    if (input) input.classList.add('error');
    if (error) error.textContent = message;
  }

  function clearFieldError(inputId, errorId) {
    const input = document.getElementById(inputId);
    const error = document.getElementById(errorId);
    if (input) input.classList.remove('error');
    if (error) error.textContent = '';
  }

  // ── Event Bindings ──
  function bindGlobalEvents() {
    // Mobile & overlay events
    document.addEventListener('click', (e) => {
      // Mobile menu
      const menuBtn = e.target.closest('#mobileMenuBtn');
      if (menuBtn) {
        document.getElementById('sidebar').classList.add('mobile-open');
        document.getElementById('sidebarOverlay').classList.add('active');
      }

      // Overlay click closes mobile sidebar
      if (e.target.id === 'sidebarOverlay') {
        document.getElementById('sidebar').classList.remove('mobile-open');
        document.getElementById('sidebarOverlay').classList.remove('active');
      }

      // Modal overlay click closes modal
      if (e.target.classList.contains('modal-overlay')) {
        closeModal();
      }
    });

    // ESC key closes modal
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        closeModal();
        // Close mobile sidebar too
        document.getElementById('sidebar')?.classList.remove('mobile-open');
        document.getElementById('sidebarOverlay')?.classList.remove('active');
      }
    });
  }

  function bindHeaderEvents() {
    // Theme toggle in header
    const themeBtn = document.getElementById('themeToggleBtn');
    if (themeBtn) {
      themeBtn.addEventListener('click', () => {
        const newTheme = ThemeManager.toggle();
        themeBtn.innerHTML = ThemeManager.isDark() ? Icons.sun : Icons.moon;

        // Sync settings page if visible
        const radioLight = document.querySelector('input[name="theme"][value="light"]');
        const radioDark = document.querySelector('input[name="theme"][value="dark"]');
        if (radioLight && radioDark) {
          radioLight.checked = newTheme === 'light';
          radioDark.checked = newTheme === 'dark';
        }
      });
    }

    // ── Dropdown Toggle Logic ──
    function closeAllDropdowns() {
      document.querySelectorAll('.header__dropdown.open').forEach(d => d.classList.remove('open'));
    }

    // Notifications dropdown
    const notifBtn = document.getElementById('notificationsBtn');
    const notifDropdown = document.getElementById('notificationsDropdown');
    if (notifBtn && notifDropdown) {
      notifBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        const isOpen = notifDropdown.classList.contains('open');
        closeAllDropdowns();
        if (!isOpen) notifDropdown.classList.add('open');
      });
    }

    // Profile dropdown
    const profileBtn = document.getElementById('profileBtn');
    const profileDropdown = document.getElementById('profileDropdown');
    if (profileBtn && profileDropdown) {
      profileBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        const isOpen = profileDropdown.classList.contains('open');
        closeAllDropdowns();
        if (!isOpen) profileDropdown.classList.add('open');
      });
    }

    // Close dropdowns when clicking outside
    document.addEventListener('click', (e) => {
      if (!e.target.closest('.header__dropdown-wrapper')) {
        closeAllDropdowns();
      }
    });

    // Mark all notifications as read
    const markAllBtn = document.getElementById('markAllReadBtn');
    if (markAllBtn) {
      markAllBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        markAllNotificationsAsRead();
        refreshCurrentView();
      });
    }

    // Notification items — navigate to ticket
    document.querySelectorAll('.dropdown__notif-item[data-chamado-id]').forEach(item => {
      item.addEventListener('click', () => {
        const ticketId = item.getAttribute('data-chamado-id');
        const notifId = item.getAttribute('data-notif-id');
        if (notifId) markNotificationAsRead(parseInt(notifId));
        closeAllDropdowns();
        if (ticketId) Router.navigate(`#chamado/${ticketId}`);
      });
    });

    // Profile menu items
    const profileGoSettings = document.getElementById('profileGoSettings');
    if (profileGoSettings) {
      profileGoSettings.addEventListener('click', () => {
        closeAllDropdowns();
        Router.navigate('#configuracoes');
      });
    }

    // Logout
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = 'login.html';
      });
    }

    // Global search
    const globalSearch = document.getElementById('globalSearch');
    if (globalSearch) {
      globalSearch.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
          const query = globalSearch.value.trim();
          if (query) {
            currentFilters.search = query;
            Router.navigate('#chamados');
          }
        }
      });
    }
  }

  function bindTicketsEvents() {
    // New ticket button
    const btnNew = document.getElementById('btnNewTicket');
    if (btnNew) btnNew.addEventListener('click', openNewTicket);

    const btnNewEmpty = document.getElementById('btnNewTicketEmpty');
    if (btnNewEmpty) btnNewEmpty.addEventListener('click', openNewTicket);

    // Search
    const searchInput = document.getElementById('searchTickets');
    if (searchInput) {
      searchInput.value = currentFilters.search;
      let debounce;
      searchInput.addEventListener('input', () => {
        clearTimeout(debounce);
        debounce = setTimeout(() => {
          currentFilters.search = searchInput.value.trim();
          applyFilters();
        }, 300);
      });
    }

    // Filters
    const filterStatus = document.getElementById('filterStatus');
    if (filterStatus) {
      filterStatus.value = currentFilters.status;
      filterStatus.addEventListener('change', () => {
        currentFilters.status = filterStatus.value;
        applyFilters();
      });
    }

    const filterPriority = document.getElementById('filterPriority');
    if (filterPriority) {
      filterPriority.value = currentFilters.priority;
      filterPriority.addEventListener('change', () => {
        currentFilters.priority = filterPriority.value;
        applyFilters();
      });
    }

    const filterCategory = document.getElementById('filterCategory');
    if (filterCategory) {
      filterCategory.value = currentFilters.category;
      filterCategory.addEventListener('change', () => {
        currentFilters.category = filterCategory.value;
        applyFilters();
      });
    }

    // Table sort
    const table = document.getElementById('ticketsTable');
    if (table) {
      table.querySelectorAll('th[data-sort]').forEach(th => {
        th.addEventListener('click', () => {
          const field = th.getAttribute('data-sort');
          if (sortField === field) {
            sortDir = sortDir === 'asc' ? 'desc' : 'asc';
          } else {
            sortField = field;
            sortDir = 'asc';
          }
          applyFilters();
        });
      });
    }
  }

  // ── Public API ──
  return {
    init,
    viewTicket,
    editTicket,
    saveTicket,
    confirmDeleteTicket,
    deleteTicket,
    closeModal,
    changeTheme,
    quickStatusChange,
    quickTechnicianChange,
    selfAssignTicket,
    refreshData,
    updateSidebarBadges
  };
})();

// ── Initialization & Route Guard ──
document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    // User is logged in, load user data from localStorage
    const userStr = localStorage.getItem('user');
    if (userStr) {
        try {
            const user = JSON.parse(userStr);
            const name = user.name || user.nome || 'Usuário';
            const initials = name
                .split(' ')
                .filter(Boolean)
                .map(n => n[0])
                .slice(0, 2)
                .join('')
                .toUpperCase() || 'HD';

            const rawRole = String(user.role || user.perfil || 'CLIENT').toUpperCase();
            let role = 'usuario';
            if (rawRole === 'ADMIN') role = 'admin';
            else if (rawRole === 'SUPPORT' || rawRole === 'TECNICO') role = 'tecnico';
            else if (rawRole === 'CLIENT' || rawRole === 'USUARIO') role = 'usuario';

            CURRENT_USER = {
                id: user.id || 1,
                name: name,
                email: user.email || '',
                jobTitle: user.jobTitle || user.cargo || (role === 'admin' ? 'Administrador' : role === 'tecnico' ? 'Técnico' : 'Colaborador'),
                department: user.department || user.departamento || 'Geral',
                role: role,
                rawRole: rawRole,
                initials: initials
            };
            window.CURRENT_USER = CURRENT_USER;
        } catch (e) {
            console.error("Error parsing user data", e);
        }
    }
    App.init();
});
