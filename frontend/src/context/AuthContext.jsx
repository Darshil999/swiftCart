import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import * as authService from '../services/authService';

const AuthContext = createContext(null);

const STORAGE_TOKEN = 'swiftcart_token';
const STORAGE_USER = 'swiftcart_user';

function loadStoredUser() {
  try {
    const raw = localStorage.getItem(STORAGE_USER);
    if (!raw) return null;
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(STORAGE_TOKEN));
  const [user, setUser] = useState(loadStoredUser);

  const login = useCallback(async (email, password) => {
    const data = await authService.login(email, password);
    const nextToken = data.token;
    const nextUser = { email: data.email, role: data.role };
    localStorage.setItem(STORAGE_TOKEN, nextToken);
    localStorage.setItem(STORAGE_USER, JSON.stringify(nextUser));
    setToken(nextToken);
    setUser(nextUser);
    return data;
  }, []);

  const register = useCallback(async (name, email, password) => {
    const data = await authService.register(name, email, password, 'BUYER');
    const nextToken = data.token;
    const nextUser = { email: data.email, role: data.role };
    localStorage.setItem(STORAGE_TOKEN, nextToken);
    localStorage.setItem(STORAGE_USER, JSON.stringify(nextUser));
    setToken(nextToken);
    setUser(nextUser);
    return data;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_TOKEN);
    localStorage.removeItem(STORAGE_USER);
    setToken(null);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token),
      isBuyer: user?.role === 'BUYER',
      login,
      register,
      logout,
    }),
    [token, user, login, register, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
}
