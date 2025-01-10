import { Helmet } from "react-helmet-async";

const NotFound = () => (
  <div className="flex flex-col items-center justify-center h-screen bg-gray-100 text-center p-4">
    <Helmet>
      <title>잘못된 주소</title>
    </Helmet>
    <h1 className="text-6xl font-extrabold text-red-600 mb-6 animate-pulse">
      404 ERROR
    </h1>
    <p className="text-xl font-medium text-gray-700 mb-4">
      죄송합니다. 페이지를 찾을 수 없습니다.
    </p>
    <p className="text-lg text-gray-600 mb-8 leading-relaxed">
      존재하지 않는 주소를 입력하셨거나,
      <br />
      요청하신 페이지의 주소가 변경 또는 삭제되어 찾을 수 없습니다.
    </p>
    <img
      src="https://ficket-event-content.s3.ap-northeast-2.amazonaws.com/404/%ED%99%94%EB%A9%B4+%EC%BA%A1%EC%B2%98+2024-12-01+102146.png"
      alt="404 Not Found"
      className="w-64 h-auto mb-8 rounded-lg shadow-lg"
    />
    <a
      href="/"
      className="px-6 py-3 text-lg font-semibold text-white bg-[#8E43E7] rounded-full shadow hover:bg-[#8029E8] transition-all duration-300"
    >
      홈으로 돌아가기
    </a>
  </div>
);

export default NotFound;
