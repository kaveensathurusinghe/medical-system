import Keycloak from 'keycloak-js';

const APP_ROLES = ['ADMIN', 'DOCTOR', 'PATIENT'];

const resolveAppRole = (roles) => {
  if (!Array.isArray(roles)) return null;
  for (const preferred of APP_ROLES) {
    const found = roles.find((r) => String(r).toUpperCase() === preferred);
    if (found) return `ROLE_${preferred}`;
  }
  return null;
};

const keycloakConfig = {
  url: (import.meta.env.VITE_KEYCLOAK_URL) || (window.location.protocol + '//' + window.location.hostname + ':8082'),
  realm: (import.meta.env.VITE_KEYCLOAK_REALM) || 'medicalsystem',
  clientId: (import.meta.env.VITE_KEYCLOAK_CLIENT) || 'medical-system-client',
};

const keycloak = new Keycloak(keycloakConfig);

const setTokenData = () => {
  try {
    if (keycloak.token) {
      localStorage.setItem('token', keycloak.token);
    }
    const roles = keycloak.tokenParsed?.realm_access?.roles;
    const appRole = resolveAppRole(roles);
    if (appRole) {
      localStorage.setItem('role', appRole);
    } else {
      localStorage.removeItem('role');
    }
    if (keycloak.tokenParsed?.sub) {
      localStorage.setItem('userId', keycloak.tokenParsed.sub);
    }
  } catch (e) {
    // ignore
  }
};

const initKeycloak = (onAuthenticated) =>
  keycloak
    .init({ onLoad: 'check-sso', pkceMethod: 'S256', checkLoginIframe: false })
    .then((authenticated) => {
      if (authenticated) {
        setTokenData();
        if (onAuthenticated) onAuthenticated();
      } else {
        // Prevent stale client-side auth state when SSO session is not active.
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        localStorage.removeItem('userId');
      }

      // token refresh loop
      setInterval(() => {
        keycloak
          .updateToken(70)
          .then((refreshed) => {
            if (refreshed) setTokenData();
          })
          .catch(() => {
            // failed to refresh token
          });
      }, 60000);

      return authenticated;
    });

const login = (options) => keycloak.login(options);
const logout = (options) => {
  try {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('userId');
  } catch (e) {}
  return keycloak.logout(options);
};

const getToken = () => keycloak.token;
const isLoggedIn = () => !!keycloak.token;

export default {
  keycloak,
  initKeycloak,
  login,
  logout,
  getToken,
  isLoggedIn,
};
