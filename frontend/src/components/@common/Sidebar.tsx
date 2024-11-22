import React from 'react';
import { FaTheaterMasks, FaUserFriends, FaRegFileAlt } from 'react-icons/fa';
import logo from '../../assets/logo.png'; // 경로 수정

function Sidebar() {
  return (
    <div className="h-full p-6 flex flex-col items-center bg-[#5E6770] text-white">
      {/* 로고 및 제목 */}
      <div className="mb-10 flex items-center justify-center w-full">
        <div className="flex items-center justify-center">
          <img src={logo} alt="Logo" className="w-8 h-8 mr-2" />{' '}
          {/* 로고 이미지 추가 및 크기 조정 */}
          <h1 className="text-xl font-bold whitespace-nowrap">
            Ficket Manager
          </h1>{' '}
          {/* 글씨 크기 조정 및 한 줄 유지 */}
        </div>
      </div>

      {/* 프로필 섹션 */}
      <div className="flex flex-col items-center mb-12">
        <div className="w-20 h-20 rounded-full bg-orange-400 flex items-center justify-center mb-4">
          <span className="text-4xl font-semibold text-white">홍</span>{' '}
          {/* 간단한 프로필 이미지 */}
        </div>
        <h2 className="text-lg font-semibold">홍길동</h2>
        <span className="text-sm text-gray-300">Product Designer</span>
      </div>

      {/* 네비게이션 메뉴 */}
      <nav className="flex flex-col space-y-6 w-full">
        <a
          href="#"
          className="flex items-center w-full px-4 py-2 text-base font-medium rounded-lg hover:bg-gray-600 transition-colors"
        >
          <FaTheaterMasks className="mr-3" />
          공연관리
        </a>
        <a
          href="#"
          className="flex items-center w-full px-4 py-2 text-base font-medium rounded-lg hover:bg-gray-600 transition-colors"
        >
          <FaUserFriends className="mr-3" />
          고객관리
        </a>
        <a
          href="#"
          className="flex items-center w-full px-4 py-2 text-base font-medium rounded-lg hover:bg-gray-600 transition-colors"
        >
          <FaRegFileAlt className="mr-3" />
          정산관리
        </a>
      </nav>
    </div>
  );
}

export default Sidebar;
