import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import * as productService from '../services/productService';
import * as cartService from '../services/cartService';
import { getErrorMessage } from '../utils/errorMessage';

function formatMoney(value) {
  if (value == null) return '—';
  const n = Number(value);
  return Number.isFinite(n) ? n.toFixed(2) : String(value);
}

export default function Products() {
  const { isAuthenticated, isBuyer } = useAuth();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cartHint, setCartHint] = useState('');

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      setError('');
      try {
        const data = await productService.fetchProducts();
        if (!cancelled) setProducts(Array.isArray(data) ? data : []);
      } catch (e) {
        if (!cancelled) setError(getErrorMessage(e));
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  async function handleAdd(productId) {
    setCartHint('');
    if (!isAuthenticated) {
      setCartHint('Please log in to add items to your cart.');
      return;
    }
    if (!isBuyer) {
      setCartHint('Only buyer accounts can use the cart. Log in with a buyer account.');
      return;
    }
    try {
      await cartService.addToCart(productId, 1);
      setCartHint('Added to cart. Open Cart to review or checkout.');
    } catch (e) {
      setCartHint(getErrorMessage(e));
    }
  }

  return (
    <div>
      <h2>Products</h2>
      {!isAuthenticated && (
        <p className="muted">
          <Link to="/login">Log in</Link> as a buyer to add items to your cart.
        </p>
      )}
      {error && <div className="error-banner">{error}</div>}
      {cartHint && <div className="card" style={{ background: '#e8f0fe', borderColor: '#c4d7f5' }}>{cartHint}</div>}
      {loading && <p>Loading…</p>}
      {!loading && products.length === 0 && !error && <p>No products yet.</p>}
      <ul style={{ listStyle: 'none', padding: 0 }}>
        {products.map((p) => (
          <li key={p.id} className="card">
            <strong>{p.name}</strong>
            <div className="muted">
              {p.category?.name ? `${p.category.name} · ` : ''}
              ${formatMoney(p.price)}
            </div>
            {p.description && (
              <p style={{ margin: '0.5rem 0 0', fontSize: '0.95rem' }}>{p.description}</p>
            )}
            <p style={{ marginTop: '0.75rem' }}>
              <button type="button" className="btn btn-primary" onClick={() => handleAdd(p.id)}>
                Add to cart
              </button>
            </p>
          </li>
        ))}
      </ul>
    </div>
  );
}
