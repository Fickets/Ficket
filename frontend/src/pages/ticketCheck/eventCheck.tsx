import React, { useEffect, useRef, useState } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import Logo from '../../assets/logo.png'
import { checkUrl } from "../../service/ticketCheck/ticketCheck";
import { set } from "date-fns";

const EventCheckPage: React.FC = () => {
    const { eventId } = useParams<{ eventId: string }>();
    const location = useLocation();

    const queryParams = new URLSearchParams(location.search);
    const uuid = queryParams.get("uuid");

    const [ready, setReady] = useState<boolean>(false);
    const [inputValue, setInputValue] = useState<string>("");


    useEffect(() => {
        checkUUID()
    }, []);

    const navi = useNavigate();
    const goManager = () => {
        if (ready) {
            navi("/")
        } else {
            alert("잘못된 URL 입니다.")
        }
    }

    const goCustomer = () => {
        if (ready) {
            // 세션 연결? 여기서 아니면 이동해서 .어디에서
            navi("/")
        } else {
            alert("잘못된 URL 입니다.")
        }
    }

    const checkUUID = async () => {
        try {
            const guestUrl = await checkUrl(eventId, uuid);
            console.log(guestUrl);
            setReady(true);
            // 토큰 저장 로직 추가
        } catch (error) {
            console.error("Error fetching guest URL:", error);
        }
    };

    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setInputValue(event.target.value); // 입력값을 상태로 저장
    };

    return (
        <div className="flex flex-col items-center justify-center">
            <img src={Logo} alt="" className="w-[140px] mt-[120px]" />
            <div>
                <p className="text-[40px] font-medium">Ficket 예매관리</p>
            </div>

            <input className="mt-[150px] w-[300px] border border-black rounded "
                type="text"
                id="userInput"
                placeholder="&nbsp;&nbsp;Enter the Link Value"
                value={inputValue} // 상태를 입력값으로 설정
                onChange={handleChange} // 입력값 변경 시 상태 업데이트 />
            />

            <div className="flex mt-[30px] items-center justify-center  w-[400px] ">
                <button onClick={goManager} className="rounded-lg m-[20px] font-regular text-[20px] border-4 border-[#666666] w-[130px] h-[130px]">관리자 확인</button>
                <button onClick={goCustomer} className="rounded-lg m-[20px] font-regular text-[20px] border-4 border-[#666666] w-[130px] h-[130px]">고객 확인</button>
            </div>
        </div>
    )
}

export default EventCheckPage;