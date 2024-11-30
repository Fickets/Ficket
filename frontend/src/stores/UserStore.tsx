import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { UserStoreType } from "../types/StoreType/UserStoreType";

export const userStore = create(
    persist<UserStoreType>((set) => ({
        isLogin: false,

        accessToken: "",

        userId: 0,
        userName: "",
        birth: 0,
        gender: "",

        setIsLogin: (newData: boolean) => set(() => ({ isLogin: newData })),

        setAccessToken: (newData: string) => set(() => ({ accessToken: newData })),
        setUserId: (newData: number) => set(() => ({ userId: newData })),
        setUserName: (newData: string) => set(() => ({ userName: newData })),
        setBirth: (newData: number) => set(() => ({ birth: newData })),
        setGender: (newData: string) => set(() => ({ gender: newData })),

    }),
        { name: "USER_STORE" }
    ))