import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import './index.css';
import { applyTheme, readStoredTheme } from './theme.js';

// Bind the user's saved palette before the first paint, so there is no flash
// of the default colours. Settings live in theme.js.
applyTheme(readStoredTheme());

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
