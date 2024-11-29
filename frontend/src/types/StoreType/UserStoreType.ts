export interface UserStoreType {

    isLogin: boolean;

    accessToken: string;
    userId: string;
    userName: string;
    birth: string;
    gender: string;

    setIsLogin: (newBool: boolean) => void;

    setAccessToken: (newToken: string) => void;
    setUserId: (newId: string) => void;
    setUserName: (newName: string) => void;
    setBirth: (newBirth: string) => void;
    setGender: (newGender: string) => void;
}