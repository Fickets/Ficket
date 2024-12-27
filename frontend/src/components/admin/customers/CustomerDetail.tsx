import React from 'react';
import { useMemo, useState, useEffect } from 'react';

import { refundMyTicket } from '../../../service/myTicket/api'
import { customerTicket } from '../../../types/admins/customer/Customers';
import { customerTicketList, customerDelete } from "../../../service/admin/customer/customerService"
import closeButtonimg from '../../../assets/customerDetail/closebutton.png';
import myinfoimg from '../../../assets/customerDetail/myid.png';
import ticketimg from '../../../assets/customerDetail/ticket.png';
interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    data: any; // 전달할 데이터 타입에 맞게 수정
}

const CustomerDetailModal: React.FC<ModalProps> = ({ isOpen, onClose, data }) => {
    if (!isOpen) return null;
    const [ticketInfo, setTicketInfo] = useState<customerTicket[]>([]);
    useEffect(() => {
        if (data.userId) {
            // data.userId가 있을 때만 API 호출
            getCustomerTicketList();
        }
    }, [data.userId]); // data.userId가 변경될 때마다 재호출

    const getCustomerTicketList = async () => {
        await customerTicketList(
            data.userId,
            (response) => {
                console.log(response.data)
                setTicketInfo(response.data);
            }, (error) => { },
        )
    }

    const orderCancel = async (orderId: string) => {
        try {
            const response = await refundMyTicket(Number(orderId));
            if (response === 204) {
                alert("환불 완료 되었습니다.");
            }
        } catch (error: any) {
            alert(error.message);
        }
    };

    const deleteCustomer = async (userId: string) => {
        await customerDelete(userId,
            (response) => {
                console.log(response.status, " SUCCESS ")
                alert("유저가 제거 되었습니다.");
                onClose();
                window.location.reload();
            }, (error) => {
                console.log(error);
                alert("모든 예약을 취소해 주세요.");
            })
    }

    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
            <div className="bg-white rounded shadow-lg  w-[1400px] h-[800px]">
                <div className='h-[40px] flex justify-between items-center bg-gray-100 px-[10px]'>
                    <h2 className='font-semibold'>유저 상세 조회</h2>
                    <button onClick={onClose} className=" text-black font-semibold">
                        <img src={closeButtonimg} alt="" />
                    </button>
                </div>
                <hr className='border border-1' />
                <div className='flex mx-[30px] my-[15px] justify-between'>
                    <div className='flex'>
                        <img src={myinfoimg} alt="" />
                        <h2 className="ml-[10px] text-lg font-bold">고객 정보</h2>
                    </div>
                    <button className='w-[45px] bg-red-500 text-white rounded'
                        onClick={() => deleteCustomer(data.userId)}
                    >탈퇴</button>
                </div>
                <div className='ml-[70px] mr-[300px] flex flex-col '>
                    <div className='flex text-center  bg-gray-100'>
                        <p className='w-1/5 border border-black'>고객 식별 번호</p>
                        <p className='w-1/5 border border-black'>고객 이름</p>
                        <p className='w-1/5 border border-black'>가입일</p>
                        <p className='w-1/5 border border-black'>출생연도</p>
                        <p className='w-1/5 border border-black'>성별</p>
                    </div>
                    {data && (
                        <div className='flex text-center'>
                            <p className='w-1/5 border border-black'>{data.userId}</p>
                            <p className='w-1/5 border border-black'>{data.userName}</p>
                            <p className='w-1/5 border border-black'>{data.createdAt || "no data"}</p>
                            <p className='w-1/5 border border-black'>{data.birth}</p>
                            <p className='w-1/5 border border-black'>{data.gender}</p>
                        </div>
                    )}
                </div>
                <div className='mt-[30px] '>
                    <div className='flex mx-[30px]'>
                        <img src={ticketimg} alt="" />
                        <h2 className="ml-[10px]  font-bold">티켓 리스트</h2>
                    </div>
                    <div className='flex mt-[15px] ml-[70px] mr-[200px] text-center  bg-gray-100'>
                        <p className='w-[50px] border border-black'>No</p>
                        <p className='w-1/6 border border-black'>티켓 식별 번호</p>
                        <p className='w-1/6 border border-black'>티켓 위치</p>
                        <p className='w-1/6 border border-black'>티켓가격</p>
                        <p className='w-1/6 border border-black'>공연 제목</p>
                        <p className='w-1/6 border border-black'>공연장</p>
                        <p className='w-1/6 border border-black'>구매 일시</p>
                        <p className='w-1/6 border border-black'>강제 취소</p>
                    </div>
                    <div className='h-[500px] flex overflow-y-auto  ml-[70px] mr-[200px] flex-col text-center'>
                        {ticketInfo.map((ticket, index) => (
                            <div key={ticket.orderId} className='flex'>
                                <p className='w-[50px] border border-black'>{index + 1}</p>
                                <p className='w-1/6 border border-black '>{ticket.orderId || "N/A"}</p>
                                <p className='w-1/6 border border-black '>{ticket.seatLoc || "N/A"}</p>
                                <p className='w-1/6 border border-black'>{ticket.ticketTotalPrice.toLocaleString() || 0}원</p>
                                <p className='w-1/6 border border-black '>{ticket.eventTitle || "N/A"}</p>
                                <p className='w-1/6 border border-black'>{ticket.stageName || "N/A"}</p>
                                <p className='w-1/6 border border-black'>{ticket.createdAt.split("T")[0] || "N/A"}</p>
                                <div className='w-1/6 border border-black'>
                                    <button className='m-1 bg-red-400 border border-black'
                                        onClick={() => orderCancel(ticket.orderId)}>예약강제취소</button>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CustomerDetailModal;
