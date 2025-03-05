import React from "react";
import UserLoginRedirect from "../../components/user/UserLoginRedirect";
import { Helmet } from "react-helmet-async";

const UserAdditionalInfo: React.FC = () => {
  return (
    <div className="w-screen h-[calc(100vh-60px)]">
      <Helmet>
        <title>추가 정보 입력</title>
      </Helmet>
      <UserLoginRedirect />
    </div>
  );
};

export default UserAdditionalInfo;
