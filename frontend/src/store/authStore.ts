import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import Cookies from "js-cookie";
import { CurrentUserResponse, LoginResponse, UserInfo } from "@/types";

interface AuthState {
  user: UserInfo | null;
  currentUser: CurrentUserResponse | null;
  isAuthenticated: boolean;
  isLoading: boolean;

  // Actions
  setUser: (user: UserInfo | null) => void;
  setCurrentUser: (user: CurrentUserResponse | null) => void;
  setAuthenticated: (value: boolean) => void;
  setLoading: (value: boolean) => void;
  login: (response: LoginResponse) => void;
  logout: () => void;
  hasPermission: (permission: string) => boolean;
  hasRole: (role: string) => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      currentUser: null,
      isAuthenticated: false,
      isLoading: true,

      setUser: (user) => set({ user }),
      setCurrentUser: (currentUser) => set({ currentUser }),
      setAuthenticated: (isAuthenticated) => set({ isAuthenticated }),
      setLoading: (isLoading) => set({ isLoading }),

      login: (response) => {
        Cookies.set("accessToken", response.accessToken, { expires: 1 });
        Cookies.set("refreshToken", response.refreshToken, { expires: 7 });
        set({
          user: response.user,
          isAuthenticated: true,
          isLoading: false,
        });
      },

      logout: () => {
        Cookies.remove("accessToken");
        Cookies.remove("refreshToken");
        set({
          user: null,
          currentUser: null,
          isAuthenticated: false,
          isLoading: false,
        });
      },

      hasPermission: (permission: string) => {
        const { currentUser } = get();
        if (!currentUser) return false;
        return currentUser.permissions.includes(permission);
      },

      hasRole: (role: string) => {
        const { currentUser } = get();
        if (!currentUser) return false;
        return currentUser.roles.includes(role);
      },
    }),
    {
      name: "auth-storage",
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({ user: state.user, isAuthenticated: state.isAuthenticated }),
    }
  )
);
