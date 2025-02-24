import Sidebar from "../../components/@common/Sidebar.tsx";
import SettlementSearchBar from "../../components/admin/Settlement/SettlementSearchBar.tsx";
import { useEffect, useState } from "react";
import {
  ApiResponse,
  SettlementSearchParams,
} from "../../types/admins/Settlement/Settlement.ts";
import { fetchSettlementListByCond } from "../../service/admin/settlement/settlementService.ts";
import SettlementListComp from "../../components/admin/Settlement/SettlementList.tsx";
import { Helmet } from "react-helmet-async";
const SettlementManagePage = () => {
  const [data, setData] = useState<ApiResponse>({
    content: [],
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
  });
  useEffect(() => {
    window.scrollTo(0, 0); // 페이지 이동 후 스크롤을 맨 위로
  }, []);
  const [searchParams, setSearchParams] = useState<SettlementSearchParams>({
    page: 0,
    size: 10,
    sort: "string",
  });

  useEffect(() => {
    const fetchSettlements = async () => {
      try {
        const response = await fetchSettlementListByCond(searchParams); // 서버에서 데이터 가져오기
        setData(response);
      } catch (error) {
        console.error("Error fetching settlements:", error);
      }
    };

    fetchSettlements();
  }, [searchParams]); // searchParams 변경 시 데이터 갱신

  const handleSearch = (newParams: SettlementSearchParams) => {
    setSearchParams((prev) => ({
      ...prev,
      ...newParams, // 검색 조건 업데이트
      page: 0, // 검색 시 첫 페이지로 이동
    }));
  };

  const handlePageChange = (newPage: number) => {
    setSearchParams((prev) => ({
      ...prev,
      page: newPage, // 페이지 값 업데이트
    }));
  };

  return (
    <div className="flex h-screen bg-[#F0F2F5]">
      <Helmet>
        <title>관리자 - 정산 관리</title>
      </Helmet>
      <div className="w-64 h-full">
        <Sidebar currentStep={"settlements"} />
      </div>
      <div className="flex-1 p-8 overflow-auto space-y-6">
        <SettlementSearchBar onSearch={handleSearch} />
        <SettlementListComp data={data} onPageChange={handlePageChange} />
      </div>
    </div>
  );
};

export default SettlementManagePage;
