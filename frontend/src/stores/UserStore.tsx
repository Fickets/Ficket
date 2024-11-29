import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { UserStoreType } from "../types/StoreType/UserStoreType";

export const userStore = create(
    persist<UserStoreType>((set) => ({
        isLogin: false,

        accessToken: "",

        userId: "",
        userName: "",
        birth: "",
        gender: "",

        setIsLogin: (newData: boolean) => set(() => ({ isLogin: newData })),

        setAccessToken: (newData: string) => set(() => ({ accessToken: newData })),
        setUserId: (newData: string) => set(() => ({ userId: newData })),
        setUserName: (newData: string) => (() => ({ gender: newData })),
        setBirth: (newData: string) => set(() => ({ birth: newData })),
        setGender: (newData: string) => set(() => ({ gender: newData })),

    }),
        { name: "USER_STORE" }
    ))