/* ============================================
   HELP DESK PI IV — SPA Router
   Navegação hash-based simples
   ============================================ */

const Router = (() => {
  const routes = {};
  let currentRoute = null;
  let beforeNavigateCallback = null;

  /**
   * Registra uma rota.
   * @param {string} path - Caminho da rota (ex: 'dashboard', 'chamados', 'chamado/:id')
   * @param {Function} handler - Função que renderiza a view
   */
  function register(path, handler) {
    routes[path] = handler;
  }

  /**
   * Callback executado antes de cada navegação.
   * @param {Function} cb
   */
  function beforeNavigate(cb) {
    beforeNavigateCallback = cb;
  }

  /**
   * Navega para uma rota.
   * @param {string} path - Caminho com hash (ex: '#chamados', '#chamado/5')
   */
  function navigate(path) {
    window.location.hash = path;
  }

  /**
   * Resolve a rota atual baseada no hash da URL.
   */
  function resolve() {
    const requested = window.location.hash.slice(1);
    const home = getHomeRoute();
    if (!requested || (requested === 'dashboard' && !isTIUser()) || (requested === 'meu-departamento' && isTIUser())) {
      window.history.replaceState(null, '', `#${home}`);
    }
    const hash = window.location.hash.slice(1) || home;
    const parts = hash.split('/');
    
    // Tenta match exato primeiro
    if (routes[hash]) {
      if (beforeNavigateCallback) beforeNavigateCallback(hash);
      currentRoute = hash;
      routes[hash]();
      updateActiveLink(hash);
      return;
    }

    // Tenta match com parâmetro (ex: chamado/:id)
    for (const [pattern, handler] of Object.entries(routes)) {
      const patternParts = pattern.split('/');
      
      if (patternParts.length !== parts.length) continue;

      const params = {};
      let match = true;

      for (let i = 0; i < patternParts.length; i++) {
        if (patternParts[i].startsWith(':')) {
          params[patternParts[i].slice(1)] = parts[i];
        } else if (patternParts[i] !== parts[i]) {
          match = false;
          break;
        }
      }

      if (match) {
        if (beforeNavigateCallback) beforeNavigateCallback(hash);
        currentRoute = hash;
        handler(params);
        updateActiveLink(parts[0]);
        return;
      }
    }

    // Rota não encontrada — redireciona para dashboard
    navigate(`#${home}`);
  }

  /**
   * Atualiza o link ativo na sidebar.
   * @param {string} routeBase - Base da rota (ex: 'chamados')
   */
  function updateActiveLink(routeBase) {
    document.querySelectorAll('.sidebar__link').forEach(link => {
      const linkRoute = link.getAttribute('data-route');
      link.classList.toggle('active', linkRoute === routeBase);
    });
  }

  /**
   * Retorna a rota atual.
   * @returns {string|null}
   */
  function getCurrentRoute() {
    return currentRoute;
  }

  /**
   * Inicializa o router.
   */
  function init() {
    window.addEventListener('hashchange', resolve);
    resolve();
  }

  return {
    register,
    navigate,
    resolve,
    getCurrentRoute,
    beforeNavigate,
    init
  };
})();
