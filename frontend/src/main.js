const API_BASE = "http://localhost:8080";
const TOKEN_KEY = "minibanco_access_token";
const LEGACY_TOKEN_KEY = "minibanco_jwt";
const REFRESH_TOKEN_KEY = "minibanco_refresh_token";
const ACCESS_EXPIRES_AT_KEY = "minibanco_access_expires_at";
const REFRESH_EXPIRES_AT_KEY = "minibanco_refresh_expires_at";
const LAST_ACCESS_KEY = "minibanco_last_access";
const READ_NOTIFY_IDS_KEY = "minibanco_notify_read_ids";

const authShell = document.getElementById("auth-shell");
const dashboardShell = document.getElementById("dashboard-shell");
const feedbackSection = document.getElementById("feedback");

const loginForm = document.getElementById("login-form");
const cadastroForm = document.getElementById("cadastro-form");
const authTabButtons = document.querySelectorAll("[data-auth-tab]");

const menuButtons = document.querySelectorAll("[data-section]");
const sectionSaldo = document.getElementById("section-saldo");
const sectionDeposito = document.getElementById("section-deposito");
const sectionPix = document.getElementById("section-pix");
const sectionExtrato = document.getElementById("section-extrato");

const saldoValue = document.getElementById("saldo-value");
const extratoList = document.getElementById("extrato-list");
const profileName = document.getElementById("profile-name");
const profileCpf = document.getElementById("profile-cpf");
const welcomeName = document.getElementById("welcome-name");
const lastAccess = document.getElementById("last-access");
const sessionExpiry = document.getElementById("session-expiry");
const summarySaldo = document.getElementById("summary-saldo");
const summaryLastMove = document.getElementById("summary-last-move");
const summaryMonthOut = document.getElementById("summary-month-out");
const accountMask = document.getElementById("account-mask");
const recentList = document.getElementById("recent-list");
const saldoChartArea = document.getElementById("saldo-chart-area");
const saldoChartLine = document.getElementById("saldo-chart-line");
const notifyBtn = document.getElementById("notify-btn");
const notifyBadge = document.getElementById("notify-badge");
const notifyPanel = document.getElementById("notify-panel");
const notifyList = document.getElementById("notify-list");
const notifyReadBtn = document.getElementById("notify-read-btn");
const notifyFilterButtons = document.querySelectorAll("[data-notify-type]");
const notifyUnreadOnlyInput = document.getElementById("notify-unread-only");
const extratoPeriodoSelect = document.getElementById("extrato-periodo");
const extratoTipoSelect = document.getElementById("extrato-tipo");
const extratoExportBtn = document.getElementById("extrato-export-btn");
const extratoPrevBtn = document.getElementById("extrato-prev-btn");
const extratoNextBtn = document.getElementById("extrato-next-btn");
const extratoPageInfo = document.getElementById("extrato-page-info");
const profileBtn = document.getElementById("profile-btn");
const profileModal = document.getElementById("profile-modal");
const profileCloseBtn = document.getElementById("profile-close-btn");
const profilePasswordForm = document.getElementById("profile-password-form");
const perfilModalNome = document.getElementById("perfil-modal-nome");
const perfilModalEmail = document.getElementById("perfil-modal-email");
const perfilModalCpf = document.getElementById("perfil-modal-cpf");
const depositoForm = document.getElementById("deposito-form");
const pixPreviewForm = document.getElementById("pix-preview-form");
const pixConfirmForm = document.getElementById("pix-confirm-form");
const pixPreviewCard = document.getElementById("pix-preview-card");
const pixChaveForm = document.getElementById("pix-chave-form");
const pixChaveTipo = document.getElementById("pix-chave-tipo");
const pixChaveValor = document.getElementById("pix-chave-valor");
const pixChaveList = document.getElementById("pix-chave-list");
const pixPreviewOperacao = document.getElementById("pix-preview-operacao");
const pixPreviewDestino = document.getElementById("pix-preview-destino");
const pixPreviewCpf = document.getElementById("pix-preview-cpf");
const pixPreviewChave = document.getElementById("pix-preview-chave");
const pixPreviewValor = document.getElementById("pix-preview-valor");
const pixPreviewExpira = document.getElementById("pix-preview-expira");
const refreshSaldoBtn = document.getElementById("refresh-saldo-btn");
const logoutBtn = document.getElementById("logout-btn");
const endSessionsBtn = document.getElementById("end-sessions-btn");
let feedbackTimeoutId;
let cachedSaldo = 0;
let cachedExtrato = [];
let notifications = [];
let knownNotificationIds = new Set();
let notificationsReady = false;
let activeNotifyType = "TODOS";
let unreadOnly = false;
let tokenRefreshPromise = null;
let sessionTickerId = null;
let pixPreview = null;
let pixChaves = [];
let extratoPeriodo = "TODOS";
let extratoTipo = "TODOS";
let extratoCurrentPage = 1;
const EXTRATO_PAGE_SIZE = 8;
let cachedProfile = {
  nome: "Titular",
  email: "-",
  cpfMascarado: "***.***.***-**"
};

function getToken() {
  return localStorage.getItem(TOKEN_KEY) || localStorage.getItem(LEGACY_TOKEN_KEY);
}

function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

function getAccessTokenExpiresAt() {
  return Number(localStorage.getItem(ACCESS_EXPIRES_AT_KEY) || 0);
}

function setAuthTokens(payload) {
  const accessToken = payload.accessToken || payload.token;
  const refreshToken = payload.refreshToken;
  const accessTokenExpiresAt = Number(payload.accessTokenExpiresAt || 0);
  const refreshTokenExpiresAt = Number(payload.refreshTokenExpiresAt || 0);

  if (!accessToken) {
    return;
  }

  localStorage.setItem(TOKEN_KEY, accessToken);
  localStorage.removeItem(LEGACY_TOKEN_KEY);
  if (refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }
  if (accessTokenExpiresAt) {
    localStorage.setItem(ACCESS_EXPIRES_AT_KEY, String(accessTokenExpiresAt));
  }
  if (refreshTokenExpiresAt) {
    localStorage.setItem(REFRESH_EXPIRES_AT_KEY, String(refreshTokenExpiresAt));
  }
  startSessionTicker();
}

function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(LEGACY_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(ACCESS_EXPIRES_AT_KEY);
  localStorage.removeItem(REFRESH_EXPIRES_AT_KEY);
  stopSessionTicker();
}

function showFeedback(type, message) {
  if (feedbackTimeoutId) {
    clearTimeout(feedbackTimeoutId);
  }
  feedbackSection.classList.remove("hidden", "success", "error");
  feedbackSection.classList.add(type);
  feedbackSection.textContent = message;
  feedbackTimeoutId = setTimeout(() => {
    hideFeedback();
  }, 3500);
}

function hideFeedback() {
  feedbackSection.classList.add("hidden");
  feedbackSection.textContent = "";
}

function formatDateTime(dateValue) {
  const date = new Date(dateValue);
  if (Number.isNaN(date.getTime())) {
    return "--";
  }
  return date.toLocaleString("pt-BR");
}

function formatRemainingTime(ms) {
  const totalSeconds = Math.max(0, Math.floor(ms / 1000));
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${minutes}m ${String(seconds).padStart(2, "0")}s`;
}

function updateSessionExpiryView() {
  const expiresAt = getAccessTokenExpiresAt();
  if (!expiresAt) {
    sessionExpiry.textContent = "Sessao expira em: --";
    return;
  }
  const remaining = expiresAt - Date.now();
  if (remaining <= 0) {
    sessionExpiry.textContent = "Sessao expirada";
    return;
  }
  sessionExpiry.textContent = `Sessao expira em: ${formatRemainingTime(remaining)}`;
}

function stopSessionTicker() {
  if (sessionTickerId) {
    clearInterval(sessionTickerId);
    sessionTickerId = null;
  }
  updateSessionExpiryView();
}

function startSessionTicker() {
  stopSessionTicker();
  updateSessionExpiryView();
  sessionTickerId = setInterval(() => {
    updateSessionExpiryView();
  }, 1000);
}

function setButtonLoading(button, loadingText) {
  if (!button) {
    return () => {};
  }

  const originalText = button.textContent;
  button.disabled = true;
  button.textContent = loadingText;

  return () => {
    button.disabled = false;
    button.textContent = originalText;
  };
}

function getTipoMeta(tipo) {
  if (tipo === "DEPOSITO") {
    return { label: "Deposito", className: "valor-deposito", prefix: "+", icon: "📥" };
  }
  if (tipo === "PIX_ENVIADO") {
    return { label: "PIX enviado", className: "valor-enviado", prefix: "-", icon: "📤" };
  }
  return { label: "PIX recebido", className: "valor-recebido", prefix: "+", icon: "📥" };
}

function isSameDay(dateA, dateB) {
  return (
    dateA.getFullYear() === dateB.getFullYear() &&
    dateA.getMonth() === dateB.getMonth() &&
    dateA.getDate() === dateB.getDate()
  );
}

function filterExtratoItems(items) {
  const now = new Date();
  const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const sevenDaysAgo = new Date(todayStart);
  sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 6);

  return items.filter((item) => {
    const itemDate = new Date(item.dataHora);

    const byTipo = extratoTipo === "TODOS" || item.tipo === extratoTipo;
    if (!byTipo) {
      return false;
    }

    if (extratoPeriodo === "HOJE") {
      return isSameDay(itemDate, now);
    }
    if (extratoPeriodo === "7DIAS") {
      return itemDate >= sevenDaysAgo;
    }
    if (extratoPeriodo === "MES") {
      return itemDate.getFullYear() === now.getFullYear() && itemDate.getMonth() === now.getMonth();
    }
    return true;
  });
}

function paginateExtratoItems(items) {
  const totalPages = Math.max(1, Math.ceil(items.length / EXTRATO_PAGE_SIZE));
  if (extratoCurrentPage > totalPages) {
    extratoCurrentPage = totalPages;
  }
  const startIndex = (extratoCurrentPage - 1) * EXTRATO_PAGE_SIZE;
  const pageItems = items.slice(startIndex, startIndex + EXTRATO_PAGE_SIZE);

  return { pageItems, totalPages };
}

function updateExtratoPaginationUI(totalPages, totalItems) {
  extratoPageInfo.textContent = `Pagina ${extratoCurrentPage}/${totalPages} · ${totalItems} itens`;
  extratoPrevBtn.disabled = extratoCurrentPage <= 1;
  extratoNextBtn.disabled = extratoCurrentPage >= totalPages;
}

function buildExtratoCsv(items) {
  const header = ["tipo", "valor", "dataHora", "emailOrigem", "emailDestino"];
  const lines = items.map((item) =>
    [
      item.tipo,
      Number(item.valor || 0).toFixed(2),
      item.dataHora,
      item.emailOrigem || "",
      item.emailDestino || ""
    ].join(";")
  );
  return [header.join(";"), ...lines].join("\n");
}

function updateLastAccess(dateValue = new Date()) {
  localStorage.setItem(LAST_ACCESS_KEY, new Date(dateValue).toISOString());
  lastAccess.textContent = `Ultimo acesso: ${formatDateTime(dateValue)}`;
}

function getReadNotificationIds() {
  try {
    const raw = localStorage.getItem(READ_NOTIFY_IDS_KEY);
    const parsed = raw ? JSON.parse(raw) : [];
    return new Set(Array.isArray(parsed) ? parsed : []);
  } catch {
    return new Set();
  }
}

function saveReadNotificationIds(readIds) {
  localStorage.setItem(READ_NOTIFY_IDS_KEY, JSON.stringify([...readIds]));
}

function createNotificationId(item) {
  return [
    item.tipo,
    item.dataHora,
    item.valor,
    item.emailOrigem ?? "",
    item.emailDestino ?? ""
  ].join("|");
}

function triggerNotificationAlert(notification) {
  notifyBtn.classList.add("notify-alert");
  setTimeout(() => {
    notifyBtn.classList.remove("notify-alert");
  }, 1800);

  try {
    const audioContext = new (window.AudioContext || window.webkitAudioContext)();
    const oscillator = audioContext.createOscillator();
    const gain = audioContext.createGain();
    oscillator.type = "sine";
    oscillator.frequency.value = 920;
    gain.gain.value = 0.03;
    oscillator.connect(gain);
    gain.connect(audioContext.destination);
    oscillator.start();
    oscillator.stop(audioContext.currentTime + 0.11);
  } catch {
    // Some browsers require prior user interaction for audio playback.
  }

  showFeedback("success", `Nova notificacao: ${notification.message}`);
}

function getFilteredNotifications(readIds = getReadNotificationIds()) {
  return notifications.filter((item) => {
    const byType = activeNotifyType === "TODOS" || item.tipo === activeNotifyType;
    const byUnread = !unreadOnly || !readIds.has(item.id);
    return byType && byUnread;
  });
}

function buildNotifications() {
  const nextNotifications = cachedExtrato
    .filter((item) => ["DEPOSITO", "PIX_ENVIADO", "PIX_RECEBIDO"].includes(item.tipo))
    .map((item) => {
      const meta = getTipoMeta(item.tipo);
      return {
        id: createNotificationId(item),
        tipo: item.tipo,
        message: `${meta.icon} ${meta.label}: ${meta.prefix} ${formatCurrency(item.valor)}`,
        date: item.dataHora
      };
    });

  if (notificationsReady) {
    const newOnes = nextNotifications.filter((item) => !knownNotificationIds.has(item.id));
    if (newOnes.length > 0) {
      triggerNotificationAlert(newOnes[0]);
    }
  }

  notifications = nextNotifications;
  knownNotificationIds = new Set(nextNotifications.map((item) => item.id));
  notificationsReady = true;
}

function renderNotifications() {
  const readIds = getReadNotificationIds();
  const unreadTotal = notifications.filter((item) => !readIds.has(item.id)).length;
  const filtered = getFilteredNotifications(readIds);

  notifyFilterButtons.forEach((button) => {
    button.classList.toggle("active", button.dataset.notifyType === activeNotifyType);
  });
  notifyUnreadOnlyInput.checked = unreadOnly;

  notifyList.innerHTML = "";
  if (!filtered.length) {
    const li = document.createElement("li");
    li.className = "notify-item";
    li.innerHTML = "<p>Nenhuma notificacao para o filtro atual.</p>";
    notifyList.appendChild(li);
  } else {
    filtered.slice(0, 30).forEach((item) => {
      const li = document.createElement("li");
      const isUnread = !readIds.has(item.id);
      li.className = `notify-item${isUnread ? " unread" : ""}`;
      li.innerHTML = `
        <p>${item.message}</p>
        <small>${formatDateTime(item.date)}</small>
      `;
      notifyList.appendChild(li);
    });
  }

  if (unreadTotal > 0) {
    notifyBadge.textContent = String(unreadTotal);
    notifyBadge.classList.remove("hidden");
  } else {
    notifyBadge.classList.add("hidden");
  }
}

function toggleNotifications(forceOpen) {
  const willOpen = typeof forceOpen === "boolean" ? forceOpen : notifyPanel.classList.contains("hidden");
  notifyPanel.classList.toggle("hidden", !willOpen);
  if (willOpen) {
    renderNotifications();
  }
}

function updateSummaryCards() {
  summarySaldo.textContent = formatCurrency(cachedSaldo);

  if (!cachedExtrato.length) {
    summaryLastMove.textContent = "Sem dados";
    summaryMonthOut.textContent = formatCurrency(0);
    return;
  }

  const lastItem = cachedExtrato[0];
  const lastLabel = getTipoMeta(lastItem.tipo).label;
  summaryLastMove.textContent = `${lastLabel} · ${formatDateTime(lastItem.dataHora)}`;

  const now = new Date();
  const monthOut = cachedExtrato
    .filter((item) => {
      if (item.tipo !== "PIX_ENVIADO") {
        return false;
      }
      const itemDate = new Date(item.dataHora);
      return (
        itemDate.getFullYear() === now.getFullYear() &&
        itemDate.getMonth() === now.getMonth()
      );
    })
    .reduce((total, item) => total + Number(item.valor || 0), 0);

  summaryMonthOut.textContent = formatCurrency(monthOut);
}

function buildDailySeries() {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const days = Array.from({ length: 7 }, (_, index) => {
    const date = new Date(today);
    date.setDate(today.getDate() - (6 - index));
    return date;
  });

  const netByDay = days.map(() => 0);
  cachedExtrato.forEach((item) => {
    const date = new Date(item.dataHora);
    date.setHours(0, 0, 0, 0);

    const dayIndex = days.findIndex((day) => day.getTime() === date.getTime());
    if (dayIndex === -1) {
      return;
    }

    const value = Number(item.valor || 0);
    const delta = item.tipo === "PIX_ENVIADO" ? -value : value;
    netByDay[dayIndex] += delta;
  });

  const totalNet = netByDay.reduce((sum, value) => sum + value, 0);
  let runningBalance = cachedSaldo - totalNet;
  return netByDay.map((net) => {
    runningBalance += net;
    return runningBalance;
  });
}

function renderChart() {
  const series = buildDailySeries();
  const min = Math.min(...series);
  const max = Math.max(...series);
  const range = max - min || 1;

  const points = series
    .map((value, index) => {
      const x = (index / (series.length - 1)) * 300;
      const y = 80 - ((value - min) / range) * 60;
      return { x, y };
    });

  const linePath = points
    .map((point, index, arr) => {
      if (index === 0) {
        return `M ${point.x.toFixed(2)} ${point.y.toFixed(2)}`;
      }

      const prev = arr[index - 1];
      const controlX = ((prev.x + point.x) / 2).toFixed(2);
      return `Q ${controlX} ${prev.y.toFixed(2)} ${point.x.toFixed(2)} ${point.y.toFixed(2)}`;
    })
    .join(" ");

  const areaPoints = [
    `0,86`,
    ...points.map((point) => `${point.x.toFixed(2)},${point.y.toFixed(2)}`),
    `300,86`
  ].join(" ");

  saldoChartArea.setAttribute("points", areaPoints);
  saldoChartLine.setAttribute("d", linePath);
}

function renderRecentTransactions() {
  recentList.innerHTML = "";

  if (!cachedExtrato.length) {
    const li = document.createElement("li");
    li.className = "recent-item";
    li.innerHTML = `
      <div class="recent-meta">
        <strong>Sem movimentacoes</strong>
        <small>As transacoes vao aparecer aqui.</small>
      </div>
      <span class="recent-valor">--</span>
    `;
    recentList.appendChild(li);
    return;
  }

  cachedExtrato.slice(0, 3).forEach((item) => {
    const meta = getTipoMeta(item.tipo);
    const li = document.createElement("li");
    li.className = "recent-item";
    li.innerHTML = `
      <div class="recent-meta">
        <strong>${meta.icon} ${meta.label}</strong>
        <small>${formatDateTime(item.dataHora)}</small>
      </div>
      <span class="recent-valor ${meta.className}">${meta.prefix} ${formatCurrency(item.valor)}</span>
    `;
    recentList.appendChild(li);
  });
}

function updateAccountMask(cpfMascarado = "") {
  const digits = String(cpfMascarado).replace(/\D/g, "");
  const suffix = digits.slice(-4) || "0000";
  accountMask.textContent = `**** ${suffix}`;
}

function resetPixPreview() {
  pixPreview = null;
  pixPreviewCard.classList.add("hidden");
  pixPreviewOperacao.textContent = "-";
  pixPreviewDestino.textContent = "-";
  pixPreviewCpf.textContent = "-";
  pixPreviewChave.textContent = "-";
  pixPreviewValor.textContent = "-";
  pixPreviewExpira.textContent = "-";
  pixConfirmForm.reset();
}

function renderPixChaves() {
  pixChaveList.innerHTML = "";

  if (!pixChaves.length) {
    const li = document.createElement("li");
    li.className = "pix-chave-item";
    li.innerHTML = "<small>Nenhuma chave PIX cadastrada ainda.</small>";
    pixChaveList.appendChild(li);
    return;
  }

  pixChaves.forEach((chave) => {
    const li = document.createElement("li");
    li.className = "pix-chave-item";
    li.innerHTML = `
      <strong>${chave.tipo}</strong>
      <small>${chave.chaveMascarada}</small>
    `;
    pixChaveList.appendChild(li);
  });
}

function resetDashboardSnapshot() {
  profileName.textContent = "Titular";
  profileCpf.textContent = "CPF: ***.***.***-**";
  welcomeName.textContent = "Ola";
  lastAccess.textContent = "Ultimo acesso: --";
  sessionExpiry.textContent = "Sessao expira em: --";
  saldoValue.textContent = formatCurrency(0);
  accountMask.textContent = "**** 0000";
  extratoList.innerHTML = "";
  cachedSaldo = 0;
  cachedExtrato = [];
  pixChaves = [];
  notifications = [];
  knownNotificationIds = new Set();
  notificationsReady = false;
  activeNotifyType = "TODOS";
  unreadOnly = false;
  tokenRefreshPromise = null;
  extratoPeriodo = "TODOS";
  extratoTipo = "TODOS";
  extratoCurrentPage = 1;
  cachedProfile = {
    nome: "Titular",
    email: "-",
    cpfMascarado: "***.***.***-**"
  };
  updateSummaryCards();
  renderChart();
  renderRecentTransactions();
  renderExtratoList();
  renderNotifications();
  notifyPanel.classList.add("hidden");
  profileModal.classList.add("hidden");
  perfilModalNome.textContent = "-";
  perfilModalEmail.textContent = "-";
  perfilModalCpf.textContent = "-";
  profilePasswordForm.reset();
  resetPixPreview();
  renderPixChaves();
  extratoPeriodoSelect.value = "TODOS";
  extratoTipoSelect.value = "TODOS";
}

function doLogout(successMessage) {
  clearToken();
  localStorage.removeItem(LAST_ACCESS_KEY);
  localStorage.removeItem(READ_NOTIFY_IDS_KEY);
  setAuthenticatedUI(false);
  resetDashboardSnapshot();
  showFeedback("success", successMessage);
  toggleAuthTab("login");
}

function formatCurrency(value) {
  const numberValue = Number(value || 0);
  return numberValue.toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL"
  });
}

function setAuthenticatedUI(isAuthenticated) {
  authShell.classList.toggle("hidden", isAuthenticated);
  dashboardShell.classList.toggle("hidden", !isAuthenticated);
}

function toggleAuthTab(tab) {
  const showLogin = tab === "login";
  loginForm.classList.toggle("hidden", !showLogin);
  cadastroForm.classList.toggle("hidden", showLogin);
  authTabButtons.forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.authTab === tab);
  });
}

function openDashboardSection(section) {
  const map = {
    saldo: sectionSaldo,
    deposito: sectionDeposito,
    pix: sectionPix,
    extrato: sectionExtrato
  };

  Object.entries(map).forEach(([key, element]) => {
    element.classList.toggle("hidden", key !== section);
  });
  menuButtons.forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.section === section);
  });
}

async function requestNewAccessToken() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    throw new Error("Sessao expirada. Faca login novamente.");
  }

  const response = await fetch(`${API_BASE}/auth/refresh`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ refreshToken })
  });

  let data = null;
  const text = await response.text();
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }

  if (!response.ok) {
    const message =
      (data && typeof data === "object" && data.message) ||
      (typeof data === "string" ? data : "Falha ao renovar sessao");
    throw new Error(message);
  }

  setAuthTokens(data || {});
}

async function ensureSessionIsValid() {
  const token = getToken();
  if (!token) {
    return;
  }

  const expiresAt = getAccessTokenExpiresAt();
  const needsRefresh = expiresAt && Date.now() >= (expiresAt - 15000);
  if (!needsRefresh) {
    return;
  }

  if (!tokenRefreshPromise) {
    tokenRefreshPromise = requestNewAccessToken().finally(() => {
      tokenRefreshPromise = null;
    });
  }
  await tokenRefreshPromise;
}

async function apiRequest(path, options = {}, hasRetriedAfterRefresh = false) {
  if (path !== "/auth/login" && path !== "/auth/refresh") {
    try {
      await ensureSessionIsValid();
    } catch (error) {
      doLogout("Sessao expirada. Faca login novamente.");
      throw error;
    }
  }

  const token = getToken();
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {})
  };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers
  });

  if (response.status === 401 && path !== "/auth/login" && path !== "/auth/refresh" && !hasRetriedAfterRefresh) {
    try {
      await requestNewAccessToken();
      return apiRequest(path, options, true);
    } catch (error) {
      doLogout("Sessao expirada. Faca login novamente.");
      throw error;
    }
  }

  let data = null;
  const text = await response.text();
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }

  if (!response.ok) {
    const message =
      (data && typeof data === "object" && data.message) ||
      (typeof data === "string" ? data : "Erro na requisicao");
    throw new Error(message);
  }

  return data;
}

async function carregarSaldo() {
  const data = await apiRequest("/contas/saldo", { method: "GET" });
  cachedSaldo = Number(data.saldo || 0);
  saldoValue.textContent = formatCurrency(cachedSaldo);
  updateSummaryCards();
  renderChart();
}

async function carregarPerfil() {
  const perfil = await apiRequest("/contas/perfil", { method: "GET" });
  cachedProfile = {
    nome: perfil.nome || "Titular",
    email: perfil.email || "-",
    cpfMascarado: perfil.cpfMascarado || "***.***.***-**"
  };
  profileName.textContent = cachedProfile.nome;
  profileCpf.textContent = `CPF: ${cachedProfile.cpfMascarado}`;
  welcomeName.textContent = `Ola, ${cachedProfile.nome}`;
  updateAccountMask(cachedProfile.cpfMascarado);
}

async function carregarChavesPix() {
  const data = await apiRequest("/contas/pix/chaves", { method: "GET" });
  pixChaves = Array.isArray(data) ? data : [];
  renderPixChaves();
}

function openProfileModal() {
  perfilModalNome.textContent = cachedProfile.nome;
  perfilModalEmail.textContent = cachedProfile.email;
  perfilModalCpf.textContent = cachedProfile.cpfMascarado;
  profilePasswordForm.reset();
  profileModal.classList.remove("hidden");
}

function closeProfileModal() {
  profileModal.classList.add("hidden");
  profilePasswordForm.reset();
}

function renderExtratoList() {
  extratoList.innerHTML = "";
  const filtered = filterExtratoItems(cachedExtrato);
  const { pageItems, totalPages } = paginateExtratoItems(filtered);
  updateExtratoPaginationUI(totalPages, filtered.length);

  if (!pageItems.length) {
    const li = document.createElement("li");
    li.className = "extrato-item empty";
    li.textContent = "Sem transacoes para o filtro selecionado.";
    extratoList.appendChild(li);
    return;
  }

  pageItems.forEach((item) => {
    const li = document.createElement("li");
    li.className = "extrato-item";

    const meta = getTipoMeta(item.tipo);
    const data = formatDateTime(item.dataHora);
    const valor = `${meta.prefix} ${formatCurrency(item.valor)}`;
    const origem = item.emailOrigem || "-";
    const destino = item.emailDestino || "-";

    li.innerHTML = `
      <div class="extrato-topo">
        <span class="extrato-tipo">${meta.label}</span>
        <span class="${meta.className}">${valor}</span>
      </div>
      <div class="extrato-data">${data}</div>
      <div class="extrato-email">${origem} -> ${destino}</div>
    `;
    extratoList.appendChild(li);
  });
}

async function carregarExtrato() {
  const itens = await apiRequest("/contas/extrato", { method: "GET" });
  cachedExtrato = Array.isArray(itens) ? itens : [];
  renderExtratoList();
  updateSummaryCards();
  renderChart();
  renderRecentTransactions();
  buildNotifications();
  renderNotifications();
}

authTabButtons.forEach((btn) => {
  btn.addEventListener("click", () => {
    hideFeedback();
    toggleAuthTab(btn.dataset.authTab);
  });
});

cadastroForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  hideFeedback();

  const nome = document.getElementById("cadastro-nome").value.trim();
  const cpf = document.getElementById("cadastro-cpf").value.trim();
  const email = document.getElementById("cadastro-email").value.trim();
  const senha = document.getElementById("cadastro-senha").value;

  try {
    await apiRequest("/contas", {
      method: "POST",
      body: JSON.stringify({ nome, cpf, email, senha })
    });
    showFeedback("success", "Conta criada com sucesso. Agora faca login.");
    cadastroForm.reset();
    toggleAuthTab("login");
    document.getElementById("email").value = email;
  } catch (error) {
    showFeedback("error", error.message);
  }
});

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  hideFeedback();

  const email = document.getElementById("email").value.trim();
  const senha = document.getElementById("senha").value;
  const unlockButton = setButtonLoading(event.submitter, "Entrando...");

  try {
    const data = await apiRequest("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, senha })
    });
    setAuthTokens(data || {});
    setAuthenticatedUI(true);
    openDashboardSection("saldo");
    await carregarPerfil();
    await carregarChavesPix();
    await carregarSaldo();
    await carregarExtrato();
    updateLastAccess(new Date());
    showFeedback("success", "Login realizado com sucesso.");
    loginForm.reset();
  } catch (error) {
    showFeedback("error", error.message);
  } finally {
    unlockButton();
  }
});

depositoForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  hideFeedback();
  const valor = Number(document.getElementById("deposito-valor").value);
  const unlockButton = setButtonLoading(event.submitter, "Processando...");

  try {
    const data = await apiRequest("/contas/deposito", {
      method: "POST",
      body: JSON.stringify({ valor })
    });
    await carregarSaldo();
    await carregarExtrato();
    showFeedback("success", data.mensagem || "Deposito realizado.");
    depositoForm.reset();
    openDashboardSection("saldo");
  } catch (error) {
    showFeedback("error", error.message);
  } finally {
    unlockButton();
  }
});

pixPreviewForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  hideFeedback();
  const chaveDestino = document.getElementById("pix-chave-destino").value.trim();
  const valor = Number(document.getElementById("pix-valor").value);
  const confirmacaoExtra = document.getElementById("pix-confirmacao-extra").checked;
  const unlockButton = setButtonLoading(event.submitter, "Validando...");

  try {
    const data = await apiRequest("/contas/pix/preview", {
      method: "POST",
      body: JSON.stringify({ chaveDestino, valor, confirmacaoExtra })
    });
    pixPreview = data;
    pixPreviewOperacao.textContent = data.numeroOperacao || "-";
    pixPreviewDestino.textContent = data.destinoNome || "-";
    pixPreviewCpf.textContent = data.destinoCpfMascarado || "-";
    pixPreviewChave.textContent = data.destinoChaveMascarada || "-";
    pixPreviewValor.textContent = formatCurrency(data.valor);
    pixPreviewExpira.textContent = formatDateTime(data.expiraEm);
    pixPreviewCard.classList.remove("hidden");
    showFeedback("success", "PIX validado. Confirme com sua senha.");
  } catch (error) {
    resetPixPreview();
    showFeedback("error", error.message);
  } finally {
    unlockButton();
  }
});

pixChaveForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  hideFeedback();

  const tipo = pixChaveTipo.value;
  const chave = pixChaveValor.value.trim();
  const unlockButton = setButtonLoading(event.submitter, "Cadastrando...");

  try {
    await apiRequest("/contas/pix/chaves", {
      method: "POST",
      body: JSON.stringify({ tipo, chave })
    });
    await carregarChavesPix();
    pixChaveForm.reset();
    pixChaveTipo.value = "CPF";
    showFeedback("success", "Chave PIX cadastrada com sucesso.");
  } catch (error) {
    showFeedback("error", error.message);
  } finally {
    unlockButton();
  }
});

pixConfirmForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  hideFeedback();

  if (!pixPreview?.numeroOperacao) {
    showFeedback("error", "Gere uma validacao PIX antes de confirmar.");
    return;
  }

  const senha = document.getElementById("pix-confirm-senha").value;
  const unlockButton = setButtonLoading(event.submitter, "Confirmando...");

  try {
    const data = await apiRequest("/contas/pix/confirmar", {
      method: "POST",
      body: JSON.stringify({
        numeroOperacao: pixPreview.numeroOperacao,
        senha
      })
    });
    await carregarSaldo();
    await carregarExtrato();
    showFeedback("success", data.mensagem || "PIX confirmado com sucesso.");
    pixPreviewForm.reset();
    resetPixPreview();
    openDashboardSection("extrato");
  } catch (error) {
    showFeedback("error", error.message);
  } finally {
    unlockButton();
  }
});

menuButtons.forEach((btn) => {
  btn.addEventListener("click", () => {
    hideFeedback();
    openDashboardSection(btn.dataset.section);
  });
});

extratoPeriodoSelect.addEventListener("change", () => {
  extratoPeriodo = extratoPeriodoSelect.value;
  extratoCurrentPage = 1;
  renderExtratoList();
});

extratoTipoSelect.addEventListener("change", () => {
  extratoTipo = extratoTipoSelect.value;
  extratoCurrentPage = 1;
  renderExtratoList();
});

extratoPrevBtn.addEventListener("click", () => {
  if (extratoCurrentPage <= 1) {
    return;
  }
  extratoCurrentPage -= 1;
  renderExtratoList();
});

extratoNextBtn.addEventListener("click", () => {
  const filtered = filterExtratoItems(cachedExtrato);
  const totalPages = Math.max(1, Math.ceil(filtered.length / EXTRATO_PAGE_SIZE));
  if (extratoCurrentPage >= totalPages) {
    return;
  }
  extratoCurrentPage += 1;
  renderExtratoList();
});

extratoExportBtn.addEventListener("click", () => {
  const filtered = filterExtratoItems(cachedExtrato);
  if (!filtered.length) {
    showFeedback("error", "Nao ha dados para exportar no filtro atual.");
    return;
  }

  const csv = buildExtratoCsv(filtered);
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  const date = new Date().toISOString().slice(0, 10);
  anchor.href = url;
  anchor.download = `extrato_${date}.csv`;
  document.body.appendChild(anchor);
  anchor.click();
  document.body.removeChild(anchor);
  URL.revokeObjectURL(url);
  showFeedback("success", "CSV exportado com sucesso.");
});

notifyBtn.addEventListener("click", () => {
  toggleNotifications();
});

notifyReadBtn.addEventListener("click", () => {
  const readIds = getReadNotificationIds();
  const filtered = getFilteredNotifications(readIds);
  filtered.forEach((item) => {
    readIds.add(item.id);
  });
  saveReadNotificationIds(readIds);
  renderNotifications();
});

notifyFilterButtons.forEach((button) => {
  button.addEventListener("click", () => {
    activeNotifyType = button.dataset.notifyType || "TODOS";
    renderNotifications();
  });
});

notifyUnreadOnlyInput.addEventListener("change", () => {
  unreadOnly = notifyUnreadOnlyInput.checked;
  renderNotifications();
});

profileBtn.addEventListener("click", () => {
  openProfileModal();
});

profileCloseBtn.addEventListener("click", () => {
  closeProfileModal();
});

profileModal.addEventListener("click", (event) => {
  if (event.target === profileModal) {
    closeProfileModal();
  }
});

profilePasswordForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  hideFeedback();

  const senhaAtual = document.getElementById("senha-atual").value;
  const novaSenha = document.getElementById("nova-senha").value;
  const confirmarNovaSenha = document.getElementById("confirmar-nova-senha").value;
  const unlockButton = setButtonLoading(event.submitter, "Alterando...");

  if (novaSenha !== confirmarNovaSenha) {
    showFeedback("error", "Confirmacao de nova senha nao confere.");
    unlockButton();
    return;
  }

  try {
    const data = await apiRequest("/contas/perfil/senha", {
      method: "POST",
      body: JSON.stringify({ senhaAtual, novaSenha })
    });
    showFeedback("success", data.mensagem || "Senha alterada com sucesso.");
    closeProfileModal();
  } catch (error) {
    showFeedback("error", error.message);
  } finally {
    unlockButton();
  }
});

document.addEventListener("click", (event) => {
  if (notifyPanel.classList.contains("hidden")) {
    return;
  }
  if (notifyPanel.contains(event.target) || notifyBtn.contains(event.target)) {
    return;
  }
  toggleNotifications(false);
});

refreshSaldoBtn.addEventListener("click", async () => {
  hideFeedback();
  const unlockButton = setButtonLoading(refreshSaldoBtn, "Atualizando...");
  try {
    await carregarSaldo();
    await carregarExtrato();
  } catch (error) {
    showFeedback("error", error.message);
  } finally {
    unlockButton();
  }
});

logoutBtn.addEventListener("click", () => {
  const confirmou = window.confirm("Deseja realmente sair da conta?");
  if (!confirmou) {
    return;
  }
  doLogout("Logout realizado.");
});

endSessionsBtn.addEventListener("click", () => {
  const confirmou = window.confirm("Encerrar todas as sessoes ativas?");
  if (!confirmou) {
    return;
  }
  doLogout("Todas as sessoes foram encerradas.");
});

async function init() {
  toggleAuthTab("login");
  updateSummaryCards();
  renderChart();
  renderRecentTransactions();
  updateSessionExpiryView();
  const token = getToken();
  if (!token) {
    setAuthenticatedUI(false);
    stopSessionTicker();
    return;
  }

  setAuthenticatedUI(true);
  startSessionTicker();
  openDashboardSection("saldo");
  try {
    await carregarPerfil();
    await carregarChavesPix();
    await carregarSaldo();
    await carregarExtrato();
    const storedAccess = localStorage.getItem(LAST_ACCESS_KEY);
    if (storedAccess) {
      lastAccess.textContent = `Ultimo acesso: ${formatDateTime(storedAccess)}`;
    } else {
      updateLastAccess(new Date());
    }
  } catch (error) {
    clearToken();
    setAuthenticatedUI(false);
    showFeedback("error", error.message);
  }
}

init();
