import Sidebar from "../../components/@common/Sidebar.tsx";
import CustomerSearchBar from "../../components/admin/customers/CustomerSearchBar.tsx";
import { useEffect, useState } from "react";
import { ApiResponse, SearchParams } from "../../types/eventList.ts";
import { fetchCustomerListByCond } from "../../service/admin/customer/customerService.ts";
import CustomerList from "../../components/admin/customers/CustomerList.tsx";
import { Helmet } from "react-helmet-async";
const UserManagePage = () => {
  const [data, setData] = useState<ApiResponse>({
    content: [],
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
  });

  const [searchParams, setSearchParams] = useState<SearchParams>({
    page: 0,
    size: 10,
    sort: "string",
  });

  useEffect(() => {
    const fetchCustomers = async () => {
      try {
        const response = await fetchCustomerListByCond(searchParams); // 서버에서 데이터 가져오기
        setData(response);
      } catch (error) {
        console.error("Error fetching customers:", error);
      }
    };

    fetchCustomers();
  }, [searchParams]); // searchParams 변경 시 데이터 갱신

  const handleSearch = (newParams: SearchParams) => {
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
        <title>관리자 - 고객 관리</title>
      </Helmet>
      <div className="w-64 h-full">
        <Sidebar currentStep={"customers"} />
      </div>
      <div className="flex-1 p-8 overflow-auto space-y-6">
        <CustomerSearchBar onSearch={handleSearch} />
        <CustomerList data={data} onPageChange={handlePageChange} />
      </div>
    </div>
  );
};

export default UserManagePage;
