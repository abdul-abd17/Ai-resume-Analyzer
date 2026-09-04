import React, { createContext, useEffect, useState } from 'react';
import { User } from '../types/user';
import { LoginDTO, RegisterDTO } from '../types/auth';
import { authService } from '../services/authService';
import { storage } from '../utils/storage';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginDTO) => Promise<void>;
  register: (userData: RegisterDTO) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(storage.getUser());
  const [token, setToken] = useState<string | null>(storage.getToken());
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const initAuth = async () => {
      const storedToken = storage.getToken();
      if (storedToken) {
        try {
          const profile = await authService.getProfile();
          setUser(profile);
          storage.setUser(profile);
        } catch {
          // Token expired or invalid
          logout();
        }
      }
      setIsLoading(false);
    };
    initAuth();
  }, []);

  const login = async (credentials: LoginDTO) => {
    setIsLoading(true);
    try {
      const response = await authService.login(credentials);
      setToken(response.token);
      setUser(response.user);
      storage.setToken(response.token);
      storage.setUser(response.user);
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (userData: RegisterDTO) => {
    setIsLoading(true);
    try {
      const response = await authService.register(userData);
      setToken(response.token);
      setUser(response.user);
      storage.setToken(response.token);
      storage.setUser(response.user);
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    storage.clearAuth();
  };

  const refreshUser = async () => {
    if (storage.getToken()) {
      try {
        const profile = await authService.getProfile();
        setUser(profile);
        storage.setUser(profile);
      } catch {
        logout();
      }
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!user && !!token,
        isLoading,
        login,
        register,
        logout,
        refreshUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
