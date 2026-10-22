/* ============================================
   HELP DESK PI IV — Theme Manager
   Gerenciamento de tema claro/escuro
   
   Armazenamento atual: localStorage
   
   TODO (Futuro): Substituir localStorage por chamada
   ao banco de dados via WebSocket Java.
   
   Exemplo de integração futura:
   ─────────────────────────────────
   async function saveTheme(theme) {
     // Substituir localStorage.setItem por:
     // await websocket.send(JSON.stringify({
     //   action: 'UPDATE_USER_PREFERENCE',
     //   data: { userId: currentUser.id, theme: theme }
     // }));
   }
   
   async function loadTheme() {
     // Substituir localStorage.getItem por:
     // const response = await websocket.request({
     //   action: 'GET_USER_PREFERENCE',
     //   data: { userId: currentUser.id, key: 'theme' }
     // });
     // return response.data.theme || 'light';
   }
   ─────────────────────────────────
   ============================================ */

const ThemeManager = (() => {
  const STORAGE_KEY = 'helpdesk_theme';
  const THEMES = { LIGHT: 'light', DARK: 'dark' };
  const DEFAULT_THEME = THEMES.LIGHT;

  let currentTheme = DEFAULT_THEME;

  /**
   * Inicializa o tema ao carregar a página.
   * Lê a preferência salva ou usa o padrão (claro).
   */
  function init() {
    // TODO (Futuro): Buscar tema do banco de dados via WebSocket
    const savedTheme = localStorage.getItem(STORAGE_KEY);
    currentTheme = savedTheme && Object.values(THEMES).includes(savedTheme)
      ? savedTheme
      : DEFAULT_THEME;
    
    applyTheme(currentTheme);
  }

  /**
   * Aplica o tema ao documento HTML.
   * @param {string} theme - 'light' ou 'dark'
   */
  function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    currentTheme = theme;
    
    // Atualiza meta theme-color para browsers mobile
    const metaThemeColor = document.querySelector('meta[name="theme-color"]');
    if (metaThemeColor) {
      metaThemeColor.setAttribute(
        'content',
        theme === THEMES.DARK ? '#0F0D15' : '#F8F7FC'
      );
    }
  }

  /**
   * Salva a preferência de tema.
   * @param {string} theme - 'light' ou 'dark'
   */
  function saveTheme(theme) {
    // TODO (Futuro): Salvar no banco de dados via WebSocket
    // websocket.send({ action: 'UPDATE_USER_PREFERENCE', data: { theme } });
    localStorage.setItem(STORAGE_KEY, theme);
  }

  /**
   * Alterna entre tema claro e escuro.
   */
  function toggle() {
    const newTheme = currentTheme === THEMES.LIGHT ? THEMES.DARK : THEMES.LIGHT;
    applyTheme(newTheme);
    saveTheme(newTheme);
    return newTheme;
  }

  /**
   * Define um tema específico.
   * @param {string} theme - 'light' ou 'dark'
   */
  function setTheme(theme) {
    if (Object.values(THEMES).includes(theme)) {
      applyTheme(theme);
      saveTheme(theme);
    }
  }

  /**
   * Retorna o tema atual.
   * @returns {string}
   */
  function getTheme() {
    return currentTheme;
  }

  /**
   * Verifica se o tema atual é escuro.
   * @returns {boolean}
   */
  function isDark() {
    return currentTheme === THEMES.DARK;
  }

  return {
    init,
    toggle,
    setTheme,
    getTheme,
    isDark,
    THEMES
  };
})();
