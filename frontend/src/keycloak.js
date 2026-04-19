// Replaced Keycloak SSO client with a minimal local JWT auth helper.
// Keeps the same exported shape (`initKeycloak`, `login`, `logout`, `getToken`, `isLoggedIn`)
// so existing components don't need large changes.

const decodeJwt = (token) => {
  try {
    const payload = token.split('.')[1];
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const json = JSON.parse(decodeURIComponent(Array.prototype.map.call(atob(base64), (c) => '%'+('00'+c.charCodeAt(0).toString(16)).slice(-2)).join('')));
    return json;
  } catch (e) {
    return null;
  }
};

const setTokenData = (token) => {
  try {
    if (token) localStorage.setItem('token', token);
    const parsed = decodeJwt(token);
    const role = parsed?.role;
    if (role) localStorage.setItem('role', `ROLE_${String(role).toUpperCase()}`);
    else localStorage.removeItem('role');
    if (parsed?.sub) localStorage.setItem('userId', parsed.sub);
    else localStorage.removeItem('userId');
  } catch (e) {
    // ignore
  }
};

const initKeycloak = (onAuthenticated) =>
  new Promise((resolve) => {
    const token = localStorage.getItem('token');
    if (token) {
      setTokenData(token);
      if (onAuthenticated) onAuthenticated();
      resolve(true);
    } else {
      resolve(false);
    }
  });

const login = async (creds) => {
  let username, password;
  if (creds && creds.username && creds.password) {
    username = creds.username;
    password = creds.password;
  } else {
    // fallback prompt to avoid breaking existing call sites that call login() without args
    username = window.prompt('Email:');
    password = window.prompt('Password:');
    if (!username || !password) throw new Error('Login cancelled');
  }

  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  if (!res.ok) {
    const body = await res.json().catch(() => null);
    throw new Error(body?.message || 'Login failed');
  }
  const data = await res.json();
  const token = data.token;
  setTokenData(token);
  return true;
};

const logout = async () => {
  try {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('userId');
  } catch (e) {}
  return Promise.resolve();
};

const getToken = () => localStorage.getItem('token');
const isLoggedIn = () => !!getToken();

export default {
  keycloak: null,
  initKeycloak,
  login,
  logout,
  getToken,
  isLoggedIn,
};
