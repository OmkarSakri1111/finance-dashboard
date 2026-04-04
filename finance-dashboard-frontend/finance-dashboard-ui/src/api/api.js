import axios from 'axios';

const BASE_URL = 'http://localhost:8082';

const api = axios.create({ baseURL: BASE_URL });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('fd_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401 || err.response?.status === 403) {
      localStorage.removeItem('fd_token');
      localStorage.removeItem('fd_user');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export default api;
