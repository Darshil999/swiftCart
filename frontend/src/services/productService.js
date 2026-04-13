import { api } from './api';

export async function fetchProducts() {
  const { data } = await api.get('/products');
  return data;
}
