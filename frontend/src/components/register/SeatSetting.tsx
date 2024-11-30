import { useState, useEffect } from 'react';
import { fetchStageSeats } from '../../service/register/api';
import { SeatInfo, SeatSettingProps } from '../../types/register';

const getRandomColor = () => {
  const letters = '0123456789ABCDEF';
  let color = '#';
  for (let i = 0; i < 6; i++) {
    color += letters[Math.floor(Math.random() * 16)];
  }
  return color;
};

const SeatSetting = ({
  stageId,
  stageImg,
  onChange,
}: SeatSettingProps & { stageImg: string }) => {
  const [seatCoordinates, setSeatCoordinates] = useState<SeatInfo[]>([]);
  const [selectedSeats, setSelectedSeats] = useState<SeatInfo[]>([]);
  const [grades, setGrades] = useState<
    {
      grade: string;
      price: number;
      seats: SeatInfo[];
      color: string;
    }[]
  >([]);
  const [currentGrade, setCurrentGrade] = useState('');
  const [currentPrice, setCurrentPrice] = useState('');
  const [isDragging, setIsDragging] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Reset state when stageId changes
  useEffect(() => {
    setSeatCoordinates([]); // 좌석 좌표 초기화
    setSelectedSeats([]); // 선택된 좌석 초기화
    setGrades([]); // 등급 초기화
    setCurrentGrade(''); // 입력 중인 등급 초기화
    setCurrentPrice(''); // 입력 중인 가격 초기화
  }, [stageId]);

  useEffect(() => {
    const loadSeatData = async () => {
      if (!stageId) return;

      setLoading(true);
      setError('');
      setSeatCoordinates([]);

      try {
        const seats = await fetchStageSeats(stageId);
        setSeatCoordinates(seats);
      } catch (err) {
        setError('좌석 정보를 불러오는 데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    loadSeatData();
  }, [stageId]);

  const toggleSeatSelection = (seat: SeatInfo) => {
    if (getSeatGrade(seat)) {
      return; // 등급이 지정된 좌석은 선택 불가
    }

    setSelectedSeats((prev) =>
      prev.some((selected) => selected.seatId === seat.seatId)
        ? prev.filter((selected) => selected.seatId !== seat.seatId)
        : [...prev, seat]
    );
  };

  const handleMouseDown = () => {
    setIsDragging(true);
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  const handleSeatHover = (seat: SeatInfo) => {
    if (isDragging && !getSeatGrade(seat)) {
      setSelectedSeats((prev) =>
        prev.some((selected) => selected.seatId === seat.seatId)
          ? prev
          : [...prev, seat]
      );
    }
  };

  const handleAddGrade = () => {
    if (!currentGrade || !currentPrice) {
      alert('등급명, 가격 입력 및 좌석 선택 해주세요.');
      return;
    } else if (selectedSeats.length === 0) {
      alert('좌석을 선택해 주세요.');
      return;
    }

    const color = getRandomColor(); // 등급별 색상 생성

    setGrades((prev) => [
      ...prev,
      {
        grade: currentGrade,
        price: parseInt(currentPrice, 10),
        seats: selectedSeats,
        color,
      },
    ]);

    setSelectedSeats([]); // 선택 초기화
    setCurrentGrade('');
    setCurrentPrice('');
  };

  // 원본 좌표 크기를 계산
  const calculateOriginalSize = (seats: SeatInfo[]) => {
    const maxX = Math.max(...seats.map((seat) => seat.x));
    const maxY = Math.max(...seats.map((seat) => seat.y));
    return { originalWidth: maxX, originalHeight: maxY };
  };

  const { originalWidth, originalHeight } =
    calculateOriginalSize(seatCoordinates);

  const containerWidth = 752; // 실제 컨테이너의 너비
  const containerHeight = 598; // 실제 컨테이너의 높이

  const calculatePosition = (x: number, y: number) => {
    const xScaleFactor = 0.9; // X 축을 90% 축소
    const yScaleFactor = 0.9; // Y 축을 90% 축소
    const scaledX = (x / originalWidth) * containerWidth * xScaleFactor;
    const scaledY = (y / originalHeight) * containerHeight * yScaleFactor;
    return { scaledX, scaledY };
  };

  const handleRemoveGrade = (grade: string) => {
    setGrades((prev) => prev.filter((g) => g.grade !== grade));
  };

  const handleSaveGrades = () => {
    if (grades.length === 0) {
      alert('저장할 등급이 없습니다.');
      return;
    }

    const formattedGrades = grades.map(({ grade, price, seats }) => ({
      grade,
      price,
      seats: seats.map((seat) => seat.seatId),
    }));

    onChange({ seats: formattedGrades });

    // 알림 표시
    alert('좌석 설정이 저장되었습니다.');
  };

  const getSeatGrade = (seat: SeatInfo) => {
    return grades.find((grade) =>
      grade.seats.some((s) => s.seatId === seat.seatId)
    );
  };

  if (loading) {
    return <p className="text-gray-500">좌석 정보를 불러오는 중입니다...</p>;
  }

  if (error) {
    return <p className="text-red-500">{error}</p>;
  }

  return (
    <div
      className="flex flex-col p-4 bg-white rounded-xl shadow-md"
      onMouseDown={handleMouseDown}
      onMouseUp={handleMouseUp}
    >
      <h2 className="text-lg font-semibold mb-4">좌석 설정</h2>
      {stageImg ? (
        <div className="flex gap-4">
          {/* 좌석 맵 */}
          <div className="flex-1 relative border border-gray-300 h-[600px]">
            <img
              src={stageImg}
              alt="Stage Map"
              className="top-0 left-0 w-full h-full"
              style={{ objectPosition: 'left top' }}
            />
            {seatCoordinates.map((seat) => {
              const seatGrade = getSeatGrade(seat);
              const isSelected = selectedSeats.some(
                (selected) => selected.seatId === seat.seatId
              );

              const { scaledX, scaledY } = calculatePosition(seat.x, seat.y);

              return (
                <button
                  key={seat.seatId}
                  className={`absolute w-[9px] h-[9px] border ${
                    isSelected ? 'bg-red-500' : seatGrade ? '' : 'bg-white'
                  }`}
                  style={{
                    backgroundColor: seatGrade?.color || '',
                    borderColor: seatGrade?.color || 'gray',
                    top: `${scaledY}px`, // Y 좌표
                    left: `${scaledX}px`, // X 좌표
                  }}
                  onMouseEnter={() => handleSeatHover(seat)}
                  onClick={() => toggleSeatSelection(seat)}
                  disabled={!!seatGrade}
                />
              );
            })}
          </div>

          {/* 우측 패널 */}
          <div className="w-[200px] p-4 border-l border-gray-300">
            <h3 className="text-md font-semibold mb-4">등급 설정</h3>
            <div className="mb-4">
              <input
                type="text"
                placeholder="등급명 입력"
                value={currentGrade}
                onChange={(e) => setCurrentGrade(e.target.value)}
                className="w-full mb-2 border border-gray-300 rounded px-2 py-1"
              />
              <input
                type="number"
                placeholder="가격 입력"
                value={currentPrice}
                onChange={(e) => setCurrentPrice(e.target.value)}
                className="w-full border border-gray-300 rounded px-2 py-1"
              />
              <button
                onClick={handleAddGrade}
                className="w-full mt-2 bg-blue-500 text-white px-4 py-2 rounded"
              >
                설정
              </button>
            </div>

            {/* 선택된 좌석 목록 */}
            <div className="mb-4 overflow-y-auto">
              <h4 className="text-sm font-semibold mb-2">
                선택(열-행) {selectedSeats.length}석
              </h4>
              <ul className="h-24 overflow-y-auto border border-gray-300 rounded p-1">
                {selectedSeats.map((seat) => (
                  <li key={seat.seatId} className="text-sm">
                    {` ${seat.seatRow}-${seat.seatCol}`}
                  </li>
                ))}
              </ul>
            </div>

            {/* 등급 목록 */}
            <div>
              <h4 className="text-[15px] font-semibold mb-2">등급 목록</h4>
              {grades.map(({ grade, price, seats, color }) => (
                <div
                  key={grade}
                  className="flex items-center justify-between border border-gray-300 rounded p-1 mb-1"
                >
                  <div className="flex items-center gap-1">
                    <span
                      className="w-2.5 h-2.5 rounded-full"
                      style={{ backgroundColor: color }}
                    ></span>
                    <span className="text-[10px]">{`${grade} ${seats.length}석`}</span>
                  </div>
                  <span className="text-[10px]">{`${price.toLocaleString()}원`}</span>
                  <button
                    onClick={() => handleRemoveGrade(grade)}
                    className="ml-2 text-red-500 text-[10px]"
                  >
                    삭제
                  </button>
                </div>
              ))}
            </div>

            <button
              onClick={handleSaveGrades}
              className="w-full mt-4 bg-green-500 text-white px-4 py-2 rounded"
            >
              저장
            </button>
          </div>
        </div>
      ) : (
        <p className="text-gray-500">공연장을 선택하면 좌석 맵이 표시됩니다.</p>
      )}
    </div>
  );
};

export default SeatSetting;
