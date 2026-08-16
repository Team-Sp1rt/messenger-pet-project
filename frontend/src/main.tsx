import './shared/styles/variables.css';
import './shared/styles/global.css';

import { createRoot } from 'react-dom/client'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
    <App />
)
