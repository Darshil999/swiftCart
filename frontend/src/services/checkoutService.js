import { api } from './api';

export async function createCheckoutSession() {
  const { data } = await api.post('/checkout/session');
  return data;
}
