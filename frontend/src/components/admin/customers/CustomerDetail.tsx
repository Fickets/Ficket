import React from 'react';
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
                    // onClick={}
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
                    <div className='flex mt-[15px] ml-[70px] mr-[250px] text-center  bg-gray-100'>
                        <p className='w-[50px] border border-black'>No</p>
                        <p className='w-1/5 border border-black'>티켓 식별 번호</p>
                        <p className='w-1/5 border border-black'>고객 이름</p>
                        <p className='w-1/5 border border-black'>가입일</p>
                        <p className='w-1/5 border border-black'>출생연도</p>
                        <p className='w-1/5 border border-black'>성별</p>
                    </div>
                    <div className='flex text-center'>

                    </div>
                </div>
            </div>
        </div>
    );
};

export default CustomerDetailModal;
