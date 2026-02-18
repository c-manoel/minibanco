const API_BASE = "";
const TOKEN_KEY = "minibanco_jwt";

const loginSection = document.getElementById("login-section");
const dashboardSection = document.getElementById("dashboard-section");
const feedbackSection = document.getElementById("feedback");
const saldoValue = document.getElementById("saldo-value");

const loginForm = document.getElementById("login-form");
const depositoForm = document.getElementById("deposito-form");
const pixForm = document.getElementById("pix-form");
const logoutBtn = document.getElementById("logout-btn");
const refreshSaldoBtn = document.getElementById("refresh-saldo-btn");

function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

function showFeedback(type, message) {
  feedbackSection.classList.remove("hidden", "success", "error");
  feedbackSection.classList.add(type);
  feedbackSection.textContent = message;
}

function hideFeedback() {
  feedbackSection.classList.add("hidden");
  feedbackSection.textContent = "";
}

function formatCurrency(value) {
  const numberValue = Number(value || 0);
  return numberValue.toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL"
  });
}

function setAuthenticatedUI(isAuthenticated) {
  loginSection.classList.toggle("hidden", isAuthenticated);
  dashboardSection.classList.toggle("hidden", !isAuthenticated);
}

async function apiRequest(path, options = {}) {
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
  try {
    const data = await apiRequest("/contas/saldo", { method: "GET" });
    saldoValue.textContent = formatCurrency(data.saldo);
  } catch (error) {
    if (error.message.toLowerCase().includes("credenciais") || error.message.toLowerCase().includes("unauthorized")) {
      clearToken();
      setAuthenticatedUI(false);
    }
    showFeedback("error", error.message);
  }
}

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  hideFeedback();

  const email = document.getElementById("email").value.trim();
  const senha = document.getElementById("senha").value;

  try {
    const data = await apiRequest("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, senha })
    });

    setToken(data.token);
    setAuthenticatedUI(true);
    showFeedback("success", "Login realizado com sucesso");
    await carregarSaldo();
    loginForm.reset();
  } catch (error) {
    showFeedback("error", error.message);
  }
});

depositoForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  hideFeedback();

  const valor = Number(document.getElementById("deposito-valor").value);

  try {
    const data = await apiRequest("/contas/deposito", {
      method: "POST",
      body: JSON.stringify({ valor })
    });
    showFeedback("success", data.mensagem || "Deposito realizado");
    depositoForm.reset();
    await carregarSaldo();
  } catch (error) {
    showFeedback("error", error.message);
  }
});

pixForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  hideFeedback();

  const emailDestino = document.getElementById("pix-email-destino").value.trim();
  const valor = Number(document.getElementById("pix-valor").value);

  try {
    const data = await apiRequest("/contas/pix", {
      method: "POST",
      body: JSON.stringify({ emailDestino, valor })
    });
    showFeedback("success", data.mensagem || "PIX realizado");
    pixForm.reset();
    await carregarSaldo();
  } catch (error) {
    showFeedback("error", error.message);
  }
});

logoutBtn.addEventListener("click", () => {
  clearToken();
  setAuthenticatedUI(false);
  saldoValue.textContent = formatCurrency(0);
  showFeedback("success", "Logout realizado");
});

refreshSaldoBtn.addEventListener("click", carregarSaldo);

async function init() {
  const token = getToken();
  if (!token) {
    setAuthenticatedUI(false);
    return;
  }

  setAuthenticatedUI(true);
  await carregarSaldo();
}

init();
