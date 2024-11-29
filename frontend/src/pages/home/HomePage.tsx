const HomePage = () => {
  const openWindow = () => {
    const data = { eventId: '1' }; // 전달할 데이터
    const query = new URLSearchParams(data).toString(); // 쿼리스트링 생성

    // 창 옵션 설정
    const windowFeatures = 'width=900,height=600,top=100,left=100';

    window.open(
      `ticketing/select-session/${query}`, // 새 창에서 열 URL
      '_blank', // 새 창으로 열기
      windowFeatures // 창 옵션
    );
  };

  return (
    <>
      <h1>메인페이지 입니다.</h1>
      <button onClick={openWindow}>새 창 열기</button>
    </>
  );
};

export default HomePage;
