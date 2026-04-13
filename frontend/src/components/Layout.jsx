import { Link, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Layout() {
  const { isAuthenticated, user, logout } = useAuth();

  return (
    <>
      <header className="app-header">
        <h1>
          <Link to="/" style={{ textDecoration: 'none', color: 'inherit' }}>
            SwiftCart
          </Link>
        </h1>
        <nav>
          <Link to="/">Products</Link>
          {isAuthenticated && <Link to="/cart">Cart</Link>}
          {!isAuthenticated && (
            <>
              <Link to="/login">Login</Link>
              <Link to="/register">Register</Link>
            </>
          )}
          {isAuthenticated && (
            <>
              <span className="muted">
                {user?.email} ({user?.role})
              </span>
              <button type="button" className="btn" onClick={logout}>
                Logout
              </button>
            </>
          )}
        </nav>
      </header>
      <Outlet />
    </>
  );
}
