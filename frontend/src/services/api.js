import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('swiftcart_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('swiftcart_token');
      localStorage.removeItem('swiftcart_user');
      const path = window.location.pathname;
      if (path !== '/login' && path !== '/register') {
        window.location.assign('/login');
      }
    }
    return Promise.reject(error);
  }
);
