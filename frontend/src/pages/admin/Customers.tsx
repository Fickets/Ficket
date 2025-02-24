import Sidebar from "../../components/@common/Sidebar.tsx";
import CustomerSearchBar from "../../components/admin/customers/CustomerSearchBar.tsx";
import { useEffect, useState } from "react";
import { ApiResponse2, SearchParams } from "../../types/eventList.ts";
import { fetchCustomerListByCond } from "../../service/admin/customer/customerService.ts";
import CustomerList from "../../components/admin/customers/CustomerList.tsx";
import { Helmet } from "react-helmet-async";

const UserManagePage = () => {
  const [data, setData] = useState<ApiResponse2>({
    content: [],
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
  });
  useEffect(() => {
    window.scrollTo(0, 0); // 페이지 이동 후 스크롤을 맨 위로
  }, []);
  const [searchParams, setSearchParams] = useState<SearchParams>({
    page: 0,
    size: 10,
    sort: "string",
  });

  useEffect(() => {
    const fetchCustomers = async () => {
      try {
        const response = await fetchCustomerListByCond(searchParams);
        setData(response);
      } catch (error) {
        console.error("Error fetching customers:", error);
      }
    };

    fetchCustomers();
  }, [searchParams]);

  const handleSearch = (newParams: SearchParams) => {
    setSearchParams((prev) => ({
      ...prev,
      ...newParams,
      page: 0,
    }));
  };

  const handlePageChange = (newPage: number) => {
    setSearchParams((prev) => ({
      ...prev,
      page: newPage,
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
