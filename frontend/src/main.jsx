import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import keycloakService from './keycloak'

const root = createRoot(document.getElementById('root'));

keycloakService
  .initKeycloak()
  .catch(() => {
    // ignore init errors; app will still render and login can be triggered manually
  })
  .finally(() => {
    root.render(
      <StrictMode>
        <App />
      </StrictMode>,
    );
  });
