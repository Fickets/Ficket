import React from "react";
import AdminLoginComponent from "../../components/admin/AdminLoginComponent";
import { Helmet } from "react-helmet-async";

const AdminLogin: React.FC = () => {
  return (
    <div className="w-screen h-[calc(100vh-60px)]">
      <Helmet>
        <title>관리자 로그인</title>
      </Helmet>
      <AdminLoginComponent />
    </div>
  );
};

export default AdminLogin;
