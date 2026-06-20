import {
  createContext,
  startTransition,
  useContext,
  useEffect,
  useState
} from "react";
import { apiRequest } from "../lib/api";
import {
  clearStoredSession,
  readStoredSession,
  saveStoredSession
} from "./auth-storage";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [session, setSession] = useState(null);
  const [isBootstrapped, setIsBootstrapped] = useState(false);

  useEffect(() => {
    const storedSession = readStoredSession();
    startTransition(() => {
      setSession(storedSession);
      setIsBootstrapped(true);
    });
  }, []);

  async function login(credentials) {
    const response = await apiRequest("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(credentials)
    });

    saveStoredSession(response);
    startTransition(() => {
      setSession(response);
    });

    return response;
  }

  function logout() {
    clearStoredSession();
    startTransition(() => {
      setSession(null);
    });
  }

  const value = {
    session,
    user: session
      ? {
          id: session.userId,
          name: session.name,
          email: session.email,
          role: session.role
        }
      : null,
    token: session ? `${session.tokenType} ${session.token}` : null,
    isAuthenticated: Boolean(session?.token),
    isBootstrapped,
    login,
    logout
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
