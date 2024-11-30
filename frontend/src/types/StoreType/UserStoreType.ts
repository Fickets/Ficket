export interface UserStoreType {

    isLogin: boolean;

    accessToken: string;
    userId: number;
    userName: string;
    birth: number;
    gender: string;

    setIsLogin: (newBool: boolean) => void;

    setAccessToken: (newToken: string) => void;
    setUserId: (newId: number) => void;
    setUserName: (newName: string) => void;
    setBirth: (newBirth: number) => void;
    setGender: (newGender: string) => void;
}