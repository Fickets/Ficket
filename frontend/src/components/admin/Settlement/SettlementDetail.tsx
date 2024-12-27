import React from 'react';
import { useMemo, useState, useEffect } from 'react';


import closeButtonimg from '../../../assets/customerDetail/closebutton.png';
import { Settlement } from '../../../types/admins/Settlement/Settlement';
import { settlementsList, clearSettlement } from '../../../service/admin/settlement/SettlementService';


interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    data: any; // 전달할 데이터 타입에 맞게 수정
}

const SettlementDetailModal: React.FC<ModalProps> = ({ isOpen, onClose, data }) => {
    if (!isOpen) return null;
    const [settlementInfo, setSettlementInfo] = useState<Settlement[]>([]);
    useEffect(() => {
        console.log(data)
        if (data.eventId) {
            getSettlementList();
        }
    }, [data.userId]); // data.userId가 변경될 때마다 재호출

    const getSettlementList = async () => {
        await settlementsList(
            data.eventId,
            (response) => {
                console.log(response.data)
                setSettlementInfo(response.data);
            }, (error) => { },
        )
    }

    const clearSettlements = async () => {
        console.log("TEST START")
        try {
            const eventId = data.eventId;
            await clearSettlement(eventId
                , (response) => {
                    onClose();
                }, (error) => {
                    alert("잠시후 다시 시도해 주세요.");
                }
            )

        } catch (error: any) {
            alert(error.message);
        }
    };


    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
            <div className="bg-white rounded shadow-lg  w-[1400px] h-[800px]">
                <div className='h-[40px] flex justify-between items-center bg-gray-100 px-[10px]'>
                    <h2 className='font-semibold'>정산 상세 조회</h2>
                    <button onClick={onClose} className=" text-black font-semibold">
                        <img src={closeButtonimg} alt="" />
                    </button>
                </div>
                <hr className='border border-1' />
                <div className='flex mx-[30px] my-[15px]'>

                    <h2 className="ml-[10px] text-lg font-bold">정산 정보</h2>

                </div>
                <div className='ml-[70px] mr-[300px] flex flex-col '>
                    <div className='flex text-center  '>
                        <p className='w-1/5 border   bg-gray-100'>거래처 PIN</p>
                        <p className='w-1/5 border   bg-gray-100'>거래처명</p>
                        <p className='w-1/5 border   bg-gray-100'>공연 PIN</p>
                        <p className='w-1/5 border   bg-gray-100'>공연 제목</p>
                    </div>
                    {data && (
                        <div className='flex text-center'>
                            <p className='w-1/5 border  '>{data.companyId}</p>
                            <p className='w-1/5 border  '>{data.companyName}</p>
                            <p className='w-1/5 border  '>{data.eventId}</p>
                            <p className='w-1/5 border  '>{data.title}</p>
                        </div>
                    )}
                </div>
                <div className='mt-[15px] ml-[70px] mr-[300px] flex flex-col '>
                    <div className='flex text-center  bg-gray-100'>
                        <p className='w-1/5 border  '>총 공급가액</p>
                        <p className='w-1/5 border  '>총 서비스료</p>
                        <p className='w-1/5 border  '>총 거래가액</p>
                        <p className='w-1/5 border  '>총 정산금액</p>
                        <p className='w-1/5 border  '>정산 여부</p>
                    </div>
                    {data && (
                        <div className='flex text-center'>
                            <p className='w-1/5 border  '>{data.totalNetSupplyAmount}</p>
                            <p className='w-1/5 border  '>{data.totalServiceFee}</p>
                            <p className='w-1/5 border  '>{data.totalSupplyAmount}</p>
                            <p className='w-1/5 border  '>{data.totalSettlementValue}</p>
                            <p className='w-1/5 border  '>{data.settlementStatus}</p>
                        </div>
                    )}
                </div>
                <div className='mt-[30px] '>
                    <div className='flex mx-[30px] justify-between'>
                        <h2 className="ml-[10px]  font-bold">정산 리스트</h2>
                        <button className='w-[100px] text-[18px] mr-[180px] bg-[#5967FF] text-white rounded'
                            onClick={() => clearSettlements()}
                        >일괄정산</button>
                    </div>
                    <div className='flex mt-[15px] ml-[70px] mr-[200px] text-center  bg-gray-100'>
                        <p className='w-[50px] border  '>No</p>
                        <p className='w-1/6 border  '>정산 PIN</p>
                        <p className='w-1/6 border  '>주문 PIN</p>
                        <p className='w-1/6 border  '>공급가액</p>
                        <p className='w-1/6 border  '>TAX</p>
                        <p className='w-1/6 border  '>거래가액</p>
                        <p className='w-1/6 border  '>이용료</p>
                        <p className='w-1/6 border  '>환불금액</p>
                        <p className='w-1/6 border  '>정산금액</p>
                        <p className='w-1/6 border  '>정산여부</p>
                    </div>
                    <div className='h-[450px] flex overflow-y-auto  ml-[70px] mr-[200px] flex-col text-center'>
                        {settlementInfo.map((settlement, index) => (
                            <div key={settlement.orderId} className='flex'>
                                <p className='w-[50px] border  '>{index + 1}</p>
                                <p className='w-1/6 border  '>{settlement.settlementId || "N/A"}</p>
                                <p className='w-1/6 border  '>{settlement.orderId || "N/A"}</p>
                                <p className='w-1/6 border   '>{settlement.supplyValue}</p>
                                <p className='w-1/6 border  '>{settlement.vat}</p>
                                <p className='w-1/6 border   '>{settlement.netSupplyAmount}</p>
                                <p className='w-1/6 border  '>{settlement.serviceFee}</p>
                                <p className='w-1/6 border  '>{settlement.refundValue}</p>
                                <p className='w-1/6 border  '>{settlement.settlementValue}</p>
                                <p className='w-1/6 border  '>{settlement.settlementStatus || "N/A"}</p>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SettlementDetailModal;
