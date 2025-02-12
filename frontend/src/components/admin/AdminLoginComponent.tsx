import React, { useState } from "react";


import { useNavigate } from "react-router";
import { useStore } from "zustand";

import { adminLogin } from "../../service/admin/admin";
import { adminStore } from "../../stores/AdminStore";
import FicketLogo from "../../assets/logo.png";

const AdminLoginComponent: React.FC = () => {
  const navi = useNavigate();
  const admin = useStore(adminStore);

  const [adminId, setId] = useState("");
  const [adminPw, setPw] = useState("");

  const submitBtn = async (event: { preventDefault: () => void; }) => {
    console.log("TEST START")
    event.preventDefault();
    const requestData = {
      id: adminId,
      pw: adminPw,
    };
    await adminLogin(
      requestData,
      (response) => {

        // response.data는 ResponseData 타입이고, 그 안의 data는 string임
        // JSON.parse로 response.data.data를 파싱해야 함
        const resData = (response.data);  // response.data.data를 JSON.parse로 파싱

        console.log(resData);  // 이제 resData에서 adminId, adminName을 사용할 수 있음

        // adminId와 adminName 추출
        admin.setAccessToken(response.headers["authorization"]);
        admin.setAdminId(resData.adminId);
        admin.setAdminName(resData.adminName);
        admin.setIsLogin(true);

        navi("/admin/event-list");
      },
      (_error) => { console.log("TEST LOGIN FAILED") },
    );
  };

  return (
    <div className="flex flex-col md:flex-row min-h-screen">
      <div className="bg-gray-800 text-white md:w-1/3 flex items-center justify-center p-10">
        <div className="text-center">
          <img
            src={FicketLogo}
            alt="Ficket Logo"
            className="w-20 h-auto mx-auto mb-4"
          />
          <h1 className="text-3xl font-bold">Ficket &nbsp;&nbsp;&nbsp;</h1>
          <h1 className="text-3xl font-bold">Manager</h1>
        </div>
      </div>

      <div className="flex-1 flex items-center justify-center p-6 bg-white">
        <div className="w-full max-w-md">
          <form
            onSubmit={submitBtn}
            className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4"
          >
            <div className="mb-4">
              <label className="block text-gray-700 text-sm font-bold mb-2">
                아이디
              </label>
              <input
                value={adminId}
                onChange={(e) => setId(e.target.value)}
                className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                id="username"
                type="text"
                placeholder="아이디"
              />
            </div>
            <div className="mb-6">
              <label className="block text-gray-700 text-sm font-bold mb-2">
                비밀번호
              </label>
              <input
                value={adminPw}
                onChange={(e) => setPw(e.target.value)}
                className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 mb-3 leading-tight focus:outline-none focus:shadow-outline"
                id="password"
                type="password"
                placeholder="********"
              />
            </div>
            <div className="flex items-center justify-between">
              <button
                className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                type="submit"
              >
                로그인
              </button>
            </div>
          </form>
          <p className="text-center text-gray-600 text-sm">
            관리 계정 관련 내용은<br></br>업체에서 문의해주세요<br></br>업체
            담당자 연락처: 010-1234-5205
          </p>
        </div>
      </div>
    </div>
  );
};

export default AdminLoginComponent;
