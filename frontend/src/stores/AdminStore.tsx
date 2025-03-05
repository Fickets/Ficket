import { create } from "zustand";
import { persist } from "zustand/middleware";
import { AdminStoreType } from "../types/StoreType/AdminStoreType";

export const adminStore = create(
  persist<AdminStoreType>(
    (set) => ({
      isLogin: false,
      accessToken: "",
      adminId: 0,
      adminName: "",

      setIsLogin: (newData: boolean) => set(() => ({ isLogin: newData })),
      setAccessToken: (newData: string) =>
        set(() => ({ accessToken: newData })),
      setAdminId: (newData: number) => set(() => ({ adminId: newData })),
      setAdminName: (newData: string) => set(() => ({ adminName: newData })),

      reset: () =>
        set(() => ({
          isLogin: false,
          accessToken: "",
          adminId: 0,
          adminName: "",
        })),
    }),
    { name: "ADMIN_STORE" }, // 로컬스토리지 키
  ),
);
