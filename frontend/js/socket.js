/* CT19/CT22 — native WebSocket; the browser never publishes internal events. */
const TicketSocket = (() => {
  let socket = null;
  let reconnectTimer = null;
  let authTimer = null;
  let stopped = true;
  let attempts = 0;
  let status = 'Tempo real não configurado';

  function setStatus(value) {
    status = value;
    const label = document.getElementById('ticketSocketStatus');
    if (label) label.textContent = value;
  }

  function disconnect() {
    stopped = true;
    clearTimeout(reconnectTimer);
    clearTimeout(authTimer);
    const previous = socket;
    socket = null;
    if (previous) previous.close();
  }

  function retry() {
    if (stopped) return;
    setStatus('Tempo real desconectado — reconectando…');
    clearTimeout(reconnectTimer);
    reconnectTimer = setTimeout(open, Math.min(1000 * 2 ** attempts++, 30000));
  }

  function open() {
    const token = localStorage.getItem('token');
    if (!token) { disconnect(); return; }
    const url = document.querySelector('meta[name="helpdesk-socket-url"]')?.content?.trim() ||
      localStorage.getItem('helpdesk-socket-url')?.trim();
    if (!url) { setStatus('Tempo real não configurado'); return; }
    try {
      const parsed = new URL(url);
      if (!['ws:', 'wss:'].includes(parsed.protocol) ||
          (window.location.protocol === 'https:' && parsed.protocol !== 'wss:')) throw new Error('URL inválida');
    } catch {
      setStatus('Endereço de tempo real inválido');
      return;
    }
    setStatus('Conectando ao tempo real…');
    let current;
    let authenticated = false;
    try { current = new WebSocket(url); } catch { retry(); return; }
    socket = current;
    // A stalled handshake/authentication must not leave the UI connecting forever.
    authTimer = setTimeout(() => { if (socket === current) current.close(); }, 12000);
    current.onopen = () => {
      if (socket !== current) return;
      current.send(JSON.stringify({ type: 'autenticarConexao', data: { token } }));
    };
    current.onmessage = async event => {
      if (socket !== current) return;
      let message;
      try { message = JSON.parse(event.data); } catch { return; }
      if (!message || typeof message !== 'object') return;
      if (message.type === 'erro') {
        disconnect();
        setStatus('Tempo real indisponível: autenticação recusada');
        return;
      }
      if (message.type === 'conexaoAutenticada' && !authenticated) {
        if (String(message.data?.userId) !== String(CURRENT_USER?.id)) {
          disconnect();
          setStatus('Tempo real indisponível: sessão inválida');
          return;
        }
        authenticated = true;
        attempts = 0;
        clearTimeout(authTimer);
        setStatus('Tempo real conectado');
        // Includes reconnects: recover events missed while offline.
        const synced = await reloadTickets();
        if (socket !== current) return;
        if (!synced) setStatus('Tempo real conectado — atualize o painel para sincronizar');
        App.renderLiveViews();
      } else if (authenticated && message.type === 'chamadoAtualizado') {
        App.handleTicketUpdated(message.data);
      }
    };
    current.onerror = () => { if (socket === current) current.close(); };
    current.onclose = () => {
      if (socket !== current) return;
      clearTimeout(authTimer);
      socket = null;
      retry();
    };
  }

  function connect() {
    disconnect();
    stopped = false;
    attempts = 0;
    open();
  }

  window.addEventListener('pagehide', disconnect);
  window.addEventListener('pageshow', event => { if (event.persisted) connect(); });
  return { connect, disconnect, getStatus: () => status };
})();
