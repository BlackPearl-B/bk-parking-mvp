import React, { createContext, useContext, useState, useEffect } from 'react';
import { UserInfo } from '../../../types/api';
import { authApi } from '../api/authApi';

interface AuthContextType {
  user: UserInfo | null;
  token: string | null;
  login: (username: string, password?: string) => Promise<UserInfo>;
  logout: () => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserInfo | null>(() => {
    const saved = localStorage.getItem('bk_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState<string | null>(
    () => localStorage.getItem('accessToken') || localStorage.getItem('bk_token')
  );
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (user && token) {
      localStorage.setItem('bk_user', JSON.stringify(user));
      localStorage.setItem('accessToken', token);
      localStorage.setItem('bk_token', token);
    } else {
      localStorage.removeItem('bk_user');
      localStorage.removeItem('accessToken');
      localStorage.removeItem('bk_token');
    }
  }, [user, token]);

  const login = async (username: string, password?: string): Promise<UserInfo> => {
    setIsLoading(true);
    try {
      const data = await authApi.login(username, password);
      setUser(data.user);
      setToken(data.accessToken);
      return data.user;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
};
