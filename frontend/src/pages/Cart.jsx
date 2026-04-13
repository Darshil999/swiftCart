import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import * as cartService from '../services/cartService';
import * as checkoutService from '../services/checkoutService';
import { getErrorMessage } from '../utils/errorMessage';

function formatMoney(value) {
  if (value == null) return '—';
  const n = Number(value);
  return Number.isFinite(n) ? n.toFixed(2) : String(value);
}

export default function Cart() {
  const { isBuyer } = useAuth();
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');

  const load = useCallback(async () => {
    setError('');
    try {
      const data = await cartService.fetchCart();
      setCart(data);
    } catch (e) {
      setError(getErrorMessage(e));
      setCart(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  async function handleRemove(productId) {
    setActionError('');
    try {
      const data = await cartService.removeCartItem(productId);
      setCart(data);
    } catch (e) {
      setActionError(getErrorMessage(e));
    }
  }

  async function handleClear() {
    setActionError('');
    try {
      await cartService.clearCart();
      setCart({ items: [], totalPrice: 0 });
    } catch (e) {
      setActionError(getErrorMessage(e));
    }
  }

  async function handleCheckout() {
    setActionError('');
    const items = cart?.items ?? [];
    if (items.length === 0) {
      setActionError('Your cart is empty. Add products first.');
      return;
    }
    try {
      const session = await checkoutService.createCheckoutSession();
      if (session.checkoutUrl) {
        window.location.href = session.checkoutUrl;
      } else {
        setActionError('Checkout did not return a payment URL.');
      }
    } catch (e) {
      setActionError(getErrorMessage(e));
    }
  }

  if (!isBuyer) {
    return (
      <div>
        <h2>Cart</h2>
        <div className="error-banner">
          Your account is not a buyer. Register a new buyer account to use the cart and checkout.
        </div>
        <p>
          <Link to="/register">Register</Link> or <Link to="/login">log in</Link> with a buyer account.
        </p>
      </div>
    );
  }

  const items = cart?.items ?? [];
  const empty = items.length === 0;

  return (
    <div>
      <h2>Cart</h2>
      {loading && <p>Loading…</p>}
      {error && <div className="error-banner">{error}</div>}
      {actionError && <div className="error-banner">{actionError}</div>}

      {!loading && !error && empty && (
        <p className="muted">Your cart is empty. <Link to="/">Browse products</Link>.</p>
      )}

      {!loading && !empty && (
        <>
          <table className="card">
            <thead>
              <tr>
                <th>Product</th>
                <th>Price</th>
                <th>Qty</th>
                <th>Line</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {items.map((line) => (
                <tr key={line.productId}>
                  <td>{line.productName}</td>
                  <td>${formatMoney(line.price)}</td>
                  <td>{line.quantity}</td>
                  <td>${formatMoney(Number(line.price) * line.quantity)}</td>
                  <td>
                    <button type="button" className="btn" onClick={() => handleRemove(line.productId)}>
                      Remove
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <p>
            <strong>Total:</strong> ${formatMoney(cart?.totalPrice)}
          </p>
          <p style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
            <button type="button" className="btn btn-primary" onClick={handleCheckout}>
              Checkout (Stripe)
            </button>
            <button type="button" className="btn btn-danger" onClick={handleClear}>
              Clear cart
            </button>
          </p>
        </>
      )}
    </div>
  );
}
