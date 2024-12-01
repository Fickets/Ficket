export interface AdminStoreType {
    isLogin: boolean;

    accessToken: string,
    adminId: number;
    adminName: string;

    setIsLogin: (newBool: boolean) => void;
    setAccessToken: (newToken: string) => void;
    setAdminId: (newId: number) => void;
    setAdminName: (newName: string) => void;
}