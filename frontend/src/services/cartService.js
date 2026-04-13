import { api } from './api';

export async function fetchCart() {
  const { data } = await api.get('/cart');
  return data;
}

export async function addToCart(productId, quantity = 1) {
  const { data } = await api.post('/cart/add', { productId, quantity });
  return data;
}

export async function removeCartItem(productId) {
  const { data } = await api.delete(`/cart/remove/${productId}`);
  return data;
}

export async function clearCart() {
  await api.delete('/cart/clear');
}
