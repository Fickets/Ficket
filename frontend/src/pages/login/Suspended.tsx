import React from "react";

const SuspendedUserPage: React.FC = () => {
    return (
        <div className="w-screen h-screen flex flex-col justify-center items-center bg-gray-100">
            <div className="bg-white shadow-lg rounded-lg p-8 text-center">
                <h1 className="text-red-500 text-5xl font-bold mb-4">계정이 정지되었습니다</h1>
                <p className="text-gray-700 text-lg mb-6">
                    회원님의 계정은 이용 약관 위반으로 인해 정지되었습니다.
                    <br />
                    이 내용이 잘못되었다고 생각되시면 고객센터에 문의해주세요.
                    <br />
                    PHONE: 574-1234
                </p>
                <a
                    href="/"
                    className="px-6 py-3 bg-red-500 text-white rounded-lg hover:bg-red-600 transition duration-200"
                >
                    홈으로 돌아가기
                </a>
            </div>
        </div>
    );
};

export default SuspendedUserPage;
