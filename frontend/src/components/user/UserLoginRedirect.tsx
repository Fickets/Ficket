import React, { useEffect, useState } from 'react';

import { useCookies } from 'react-cookie';
import { useNavigate } from 'react-router';
import { useStore } from 'zustand';
import { userStore } from '../../stores/UserStore';
import { userTokenRefresh, userAdditionalInfo } from '../../service/user/userApi';

import FicketLogo from '../../assets/logo.png';
import BangMark from '../../assets/bang.png';

const UserLoginRedirect: React.FC = () => {
    const [cookies] = useCookies(['isLogin']);
    const navi = useNavigate();
    const user = useStore(userStore);


    const [birth, setBirth] = useState("");

    const [gender, setGender] = useState("");

    const currentYear = new Date().getFullYear();
    const startYear = currentYear - 100; // 100년 전부터 시작
    const years: number[] = [];
    for (let year = currentYear; year >= startYear; year--) {
        years.push(year);
    }


    useEffect(() => {
        getAccess();
        setGender("MALE")
        setBirth(years[0].toString());
    }, [])

    const getAccess = async () => {
        if (Boolean(cookies.isLogin)) {
            await userTokenRefresh(
                (response) => {

                    user.setAccessToken(response.headers['authorization']);
                    user.setIsLogin(true);


                },
                () => {
                    navi("/users/login")
                }
            )
        }
    }
    const submitBtn = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const requestData = {
            birth: parseInt(birth),
            gender: gender,
        }

        await userAdditionalInfo(
            requestData,
            (response) => {
                const parsedData = response.data;
                user.setGender(parsedData.gender)
                user.setBirth(parsedData.birth);
                user.setUserName(parsedData.userName);
                user.setUserId(parsedData.userId);

                navi("/");
            }, (error) => {
                console.log(error.status);
            }
        )

    }

    return (
        <div className='mt-[10vh] w-screen h-[calc(60vh-60px)] flex flex-col justify-center items-center bg-white'>

            <div className='flex'>
                <img src={FicketLogo} className='h-auto w-20 sm:w-30 md:w-40 xl:w-50' />
                <p className='ml-4 self-end font-black text-3xl md:text-4xl xl:text-6xl '>Ficket</p>
            </div>
            <div className="max-w-sm mx-auto mt-10 p-6 bg-white rounded-lg shadow-lg border border-blue-500">
                <h1 className="text-2xl md:text-3xl xl:text-4xl font-bold text-center mb-4">추가 정보 입력</h1>
                <div className='flex'>
                    <img src={BangMark} className='mt-10 w-4' />
                    <p className='ml-2 self-end text-xs'>원활한 서비스 이용을 위해 추가정보 입력은 필수입니다.</p>
                </div>

                <form className='mt-5' onSubmit={submitBtn}>


                    <label id="birth" className="block text-sm font-medium text-gray-700 mb-2">출생년도</label>
                    <select id="birth" value={birth} onChange={(e) => setBirth(e.target.value)} className="border border-gray-300 rounded-lg p-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-500">
                        {years.map((year) => (
                            <option key={year} value={year}>
                                {year}
                            </option>
                        ))}
                    </select>



                    <span className="mt-5 block text-sm font-medium text-gray-700 mb-2">성별</span>
                    <div className="flex items-center">
                        <label className="mr-4 flex items-center">
                            <input type="radio" name="gender" value="MALE"
                                onChange={(e) => setGender(e.target.value)}
                                className="mr-2" defaultChecked /> 남자
                        </label>
                        <label className="flex items-center">
                            <input type="radio" name="gender" value="FEMALE"
                                onChange={(e) => setGender(e.target.value)}
                                className="mr-2" /> 여자
                        </label>
                    </div>


                    <button type="submit" className="mt-5 w-full py-2 bg-purple-600 text-white font-semibold rounded-lg hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-purple-500">저장하기</button>

                </form>
            </div>


        </div>
    );
}

export default UserLoginRedirect;




