/* ============================================
   HELP DESK PI IV — Internationalization (i18n)
   Global translation system: EN keys → PT-BR labels
   ============================================ */

const i18n = (() => {
  const translations = {
    // ── Status (strictly matches Neon DB CHECK constraint) ──
    status: {
      NEW:         'Novo',
      ASSIGNED:    'Atribuído',
      CLOSED:      'Resolvido',
      UNRESOLVED:  'Não Solucionado'
    },

    // ── Priority (strictly matches Neon DB CHECK constraint) ──
    priority: {
      LOW:    'Baixa',
      MEDIUM: 'Normal',
      HIGH:   'Alta',
      URGENT: 'Urgente'
    },

    // ── Roles (supports DB uppercase & frontend lowercase) ──
    role: {
      ADMIN:   'Administrador',
      SUPPORT: 'Técnico',
      CLIENT:  'Usuário',
      admin:   'Administrador',
      tecnico: 'Técnico',
      support: 'Técnico',
      usuario: 'Usuário',
      client:  'Usuário'
    },

    // ── Status CSS classes ──
    statusClass: {
      NEW:         'badge-new',
      ASSIGNED:    'badge-assigned',
      CLOSED:      'badge-resolved',
      UNRESOLVED:  'badge-closed'
    },

    // ── Priority CSS classes ──
    priorityClass: {
      LOW:    'badge-low',
      MEDIUM: 'badge-normal',
      HIGH:   'badge-high',
      URGENT: 'badge-urgent'
    },

    // ── UI Labels ──
    ui: {
      // Dashboard
      dashboard:              'Dashboard',
      dashboardSubtitle:      'Visão analítica dos chamados em tempo real (Neon DB)',
      totalTickets:           'Total de Chamados',
      openTickets:            'Aguardando Atendimento',
      assignedTickets:        'Em Atendimento',
      resolvedTickets:        'Resolvidos',
      unresolvedTickets:      'Não Solucionados',
      distributionByStatus:   'Distribuição por Status',
      distributionByCategory: 'Chamados por Categoria',
      distributionByPriority: 'Chamados por Prioridade',
      recentTickets:          'Chamados Recentes',
      viewAll:                'Ver todos',
      backendConnected:       'Conectado ao Neon',
      backendOffline:         'Sem conexão com a API',
      refreshData:            'Recarregar Dados',

      // Tickets list
      tickets:               'Chamados',
      ticketsSubtitle:       'Gerencie todos os chamados do sistema',
      newTicket:             'Novo Chamado',
      searchPlaceholder:     'Buscar por título ou ID...',
      allStatuses:           'Todos os Status',
      allPriorities:         'Todas as Prioridades',
      allCategories:         'Todas as Categorias',
      noTicketsFound:        'Nenhum chamado encontrado',
      noTicketsHint:         'Tente ajustar os filtros ou crie um novo chamado.',
      notAssigned:           'Não atribuído',

      // Table headers
      thId:                  'ID',
      thTitle:               'Título',
      thStatus:              'Status',
      thPriority:            'Prioridade',
      thCategory:            'Categoria',
      thRequester:           'Solicitante',
      thTechnician:          'Técnico',
      thDate:                'Data',
      thActions:             'Ações',

      // Ticket detail
      ticketNotFound:        'Chamado não encontrado',
      ticketNotFoundMsg:     'O chamado não existe ou foi removido.',
      backToTickets:         'Voltar aos chamados',
      description:           'Descrição',
      activity:              'Atividade',
      information:           'Informações',
      requester:             'Solicitante',
      technicianResponsible: 'Técnico Responsável',
      category:              'Categoria',
      createdAt:             'Criado em',
      lastUpdate:            'Última Atualização',
      sla:                   'SLA',
      quickActions:          'Ações Rápidas',
      selfAssign:            'Me Atribuir',
      changeStatus:          'Alterar Status...',
      assignTechnician:      'Atribuir Técnico...',
      edit:                  'Editar',
      delete:                'Excluir',
      view:                  'Visualizar',
      openedAt:              'Aberto em',

      // Modal: create/edit
      editTicket:            'Editar Chamado',
      createTicket:          'Novo Chamado',
      titleLabel:            'Título',
      titlePlaceholder:      'Descreva o problema brevemente...',
      titleRequired:         'O título é obrigatório.',
      descriptionLabel:      'Descrição',
      descriptionPlaceholder:'Descreva o problema com detalhes...',
      descriptionRequired:   'A descrição é obrigatória.',
      categoryLabel:         'Categoria',
      categorySelect:        'Selecione...',
      categoryRequired:      'Selecione uma categoria.',
      priorityLabel:         'Prioridade',
      statusLabel:           'Status',
      technicianLabel:       'Técnico Responsável',
      requesterLabel:        'Solicitante',
      cancel:                'Cancelar',
      saveChanges:           'Salvar Alterações',
      createTicketBtn:       'Criar Chamado',

      // Modal: delete confirmation
      confirmDelete:         'Confirmar Exclusão',
      confirmDeleteMsg:      'Tem certeza que deseja excluir o chamado',
      cannotUndo:            'Esta ação não pode ser desfeita.',

      // Toast messages
      ticketUpdated:         'Chamado atualizado',
      ticketCreated:         'Chamado criado',
      ticketDeleted:         'Chamado excluído',
      statusChanged:         'Status alterado',
      techAssigned:          'Técnico atribuído',
      noPermission:          'Sem permissão',
      noPermissionMsg:       'Apenas técnicos e administradores podem se atribuir a chamados.',
      error:                 'Erro',

      // Settings
      settings:              'Configurações',
      appearance:            'Aparência',
      theme:                 'Tema',
      lightTheme:            'Claro',
      darkTheme:             'Escuro',
      themeChanged:          'Tema alterado',
      lightApplied:          'Tema claro aplicado com sucesso.',
      darkApplied:           'Tema escuro aplicado com sucesso.',

      // Navigation
      home:                  'Início',
      main:                  'Principal',
      system:                'Sistema',

      // Header
      searchTickets:         'Buscar chamados...',
      toggleTheme:           'Alternar tema',
      notifications:         'Notificações',
      noNotifications:       'Nenhuma notificação',
      allRead:               'Tudo lido',
      unread:                'não lida',
      unreads:               'não lidas',
      markAll:               'Marcar todas',
      logout:                'Sair',

      // Misc
      loading:               'Carregando HelpDesk...',
      showing:               'Mostrando',
      of:                    'de',
      ticketsCount:          'chamados',
      previous:              'Anterior',
      next:                  'Próximo',

      // User info
      profile:               'Perfil',
      account:               'Conta',

      // Timeline event types
      creation:              'Chamado aberto',
      assignment:            'Atribuição',
      statusChange:          'Mudança de status',
      comment:               'Comentário',
      systemAuthor:          'Sistema',

      // API errors
      connectionError:       'Erro de conexão com o servidor. Tente novamente.',
      loadError:             'Erro ao carregar dados do servidor.',
      saveError:             'Erro ao salvar. Tente novamente.',
      deleteError:           'Erro ao excluir. Tente novamente.'
    }
  };

  /**
   * Get translated status label
   * @param {string} statusKey - e.g. 'NEW', 'ASSIGNED'
   * @returns {string} Translated label
   */
  function status(key) {
    return translations.status[key] || key;
  }

  /**
   * Get translated priority label
   * @param {string} priorityKey - e.g. 'LOW', 'HIGH'
   * @returns {string} Translated label
   */
  function priority(key) {
    return translations.priority[key] || key;
  }

  /**
   * Get translated role label
   * @param {string} roleKey - e.g. 'admin', 'tecnico'
   * @returns {string} Translated label
   */
  function role(key) {
    return translations.role[key] || key;
  }

  /**
   * Get CSS class for a status
   * @param {string} statusKey
   * @returns {string} CSS class
   */
  function statusClass(key) {
    return translations.statusClass[key] || 'badge-new';
  }

  /**
   * Get CSS class for a priority
   * @param {string} priorityKey
   * @returns {string} CSS class
   */
  function priorityClass(key) {
    return translations.priorityClass[key] || 'badge-normal';
  }

  /**
   * Get a UI label
   * @param {string} key
   * @returns {string}
   */
  function t(key) {
    return translations.ui[key] || key;
  }

  /**
   * Get all status entries as array of {id, label, cssClass}
   */
  function getStatusList() {
    return Object.keys(translations.status).map(key => ({
      id: key,
      label: translations.status[key],
      cssClass: translations.statusClass[key]
    }));
  }

  /**
   * Get all priority entries as array of {id, label, cssClass}
   */
  function getPriorityList() {
    return Object.keys(translations.priority).map(key => ({
      id: key,
      label: translations.priority[key],
      cssClass: translations.priorityClass[key]
    }));
  }

  return {
    status,
    priority,
    role,
    statusClass,
    priorityClass,
    t,
    getStatusList,
    getPriorityList
  };
})();
