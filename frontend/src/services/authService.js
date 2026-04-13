import { api } from './api';

export async function login(email, password) {
  const { data } = await api.post('/auth/login', { email, password });
  return data;
}

export async function register(name, email, password, role = 'BUYER') {
  const { data } = await api.post('/auth/register', {
    name,
    email,
    password,
    role,
  });
  return data;
}
