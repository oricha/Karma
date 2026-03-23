export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
export const DEFAULT_LOCALE = import.meta.env.VITE_DEFAULT_LOCALE || 'es';
export const SUPPORTED_LOCALES = (import.meta.env.VITE_SUPPORTED_LOCALES || 'es,en').split(',');
