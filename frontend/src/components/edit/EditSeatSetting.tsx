import { useState, useEffect, useRef } from 'react';
import { fetchStageSeats } from '../../service/register/api';
import { SeatInfo, SeatSettingProps, SeatGrade } from '../../types/edit';

const getRandomColor = () => {
  const letters = '0123456789ABCDEF';
  return `#${Array.from({ length: 6 })
    .map(() => letters[Math.floor(Math.random() * 16)])
    .join('')}`;
};

const EditSeatSetting = ({
  stageId,
  stageImg,
  initialData = [],
  onChange,
}: SeatSettingProps) => {
  const [seatCoordinates, setSeatCoordinates] = useState<SeatInfo[]>([]);
  const [selectedSeats, setSelectedSeats] = useState<SeatInfo[]>([]);
  const [grades, setGrades] = useState<
    { grade: SeatGrade; color: string; seats: SeatInfo[] }[]
  >([]);

  const [currentGrade, setCurrentGrade] = useState('');
  const [currentPrice, setCurrentPrice] = useState('');
  const [isDragging, setIsDragging] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const imageRef = useRef<HTMLImageElement | null>(null);
  const [scale, setScale] = useState({ x: 1, y: 1 });

  // 초기화 및 데이터 로드
  useEffect(() => {
    const loadSeats = async () => {
      if (!stageId) return;

      setLoading(true);
      setError('');

      try {
        const seats = await fetchStageSeats(stageId); // seatCoordinates 데이터 로드
        setSeatCoordinates(seats);

        // 초기 데이터로 등급 매핑
        const mappedGrades = initialData.map((grade) => {
          const matchingSeats = seats.filter(
            (seat) => grade.seats.includes(seat.seatId) // `seatId` 기준 매핑
          );
          return {
            grade: {
              grade: grade.grade,
              price: grade.price,
              seats: grade.seats,
            }, // 초기 등급 정보 유지
            color: getRandomColor(), // 등급별 고유 색상 지정
            seats: matchingSeats, // 매칭된 좌석
          };
        });
        setGrades(mappedGrades); // 상태 업데이트
      } catch (err) {
        setError('좌석 데이터를 불러오는 데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    loadSeats();
  }, [stageId, initialData]);

  useEffect(() => {
    const updateScale = () => {
      if (imageRef.current) {
        const { naturalWidth, naturalHeight, clientWidth, clientHeight } =
          imageRef.current;
        setScale({
          x: clientWidth / naturalWidth,
          y: clientHeight / naturalHeight,
        });
      }
    };

    updateScale();
    window.addEventListener('resize', updateScale);
    return () => window.removeEventListener('resize', updateScale);
  }, [stageImg]);

  const toggleSeatSelection = (seat: SeatInfo) => {
    if (getSeatGrade(seat)) return;

    setSelectedSeats((prev) =>
      prev.some((selected) => selected.seatId === seat.seatId)
        ? prev.filter((selected) => selected.seatId !== seat.seatId)
        : [...prev, seat]
    );
  };

  const handleMouseDown = () => setIsDragging(true);
  const handleMouseUp = () => setIsDragging(false);

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
    if (!currentGrade || !currentPrice || selectedSeats.length === 0) {
      alert('등급명, 가격을 입력하고 좌석을 선택하세요.');
      return;
    }

    const newGrade = {
      grade: {
        grade: currentGrade,
        price: parseFloat(currentPrice),
        seats: selectedSeats.map((seat) => seat.seatId),
      },
      color: getRandomColor(),
      seats: selectedSeats,
    };

    setGrades((prev) => [...prev, newGrade]);
    setSelectedSeats([]);
    setCurrentGrade('');
    setCurrentPrice('');
  };

  const handleRemoveGrade = (grade: string) => {
    setGrades((prev) => prev.filter((g) => g.grade.grade !== grade));
  };

  const handleSaveGrades = () => {
    const formattedGrades = grades.map(({ grade }) => grade);
    onChange({ seats: formattedGrades });

    alert('좌석 설정이 저장되었습니다.');
  };

  const getSeatGrade = (seat: SeatInfo) => {
    return grades.find(
      (grade) => grade.seats.some((s) => s.seatId === seat.seatId) // `seatId` 기준으로 좌석 찾기
    );
  };

  if (loading) return <p>좌석 데이터를 불러오는 중입니다...</p>;
  if (error) return <p className="text-red-500">{error}</p>;

  return (
    <div
      className="flex flex-col p-4 bg-white rounded-xl shadow-md"
      onMouseDown={handleMouseDown}
      onMouseUp={handleMouseUp}
    >
      <h2 className="text-lg font-semibold mb-4">좌석 설정</h2>
      <div className="flex gap-4">
        {/* 좌석 맵 */}
        <div className="flex-1 relative border border-gray-300 h-[600px]">
          <img
            ref={imageRef}
            src={stageImg}
            alt="Stage Map"
            className="absolute top-0 left-0 w-full h-full object-contain"
          />
          {seatCoordinates.map((seat) => {
            const seatGrade = getSeatGrade(seat); // 좌석 등급 확인
            const isSelected = selectedSeats.some(
              (selected) => selected.seatId === seat.seatId
            );

            const adjustedX = seat.x * scale.x;
            const adjustedY = seat.y * scale.y;

            return (
              <button
                key={seat.seatId}
                className={`absolute w-[9px] h-[9px] border ${
                  isSelected ? 'border-black' : 'border-gray-300'
                }`}
                style={{
                  backgroundColor: isSelected
                    ? 'red'
                    : seatGrade?.color || 'white', // 등급별 색상 적용
                  borderColor: seatGrade?.color || 'gray',
                  top: `${adjustedY}px`,
                  left: `${adjustedX}px`,
                  transform: 'translate(-50%, -50%)',
                }}
                onMouseEnter={() => handleSeatHover(seat)}
                onClick={() => toggleSeatSelection(seat)}
                disabled={!!seatGrade} // 이미 등급이 설정된 좌석 비활성화
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
                  {`${seat.seatRow}-${seat.seatCol}`}
                </li>
              ))}
            </ul>
          </div>

          {/* 등급 목록 */}
          <div>
            <h4 className="text-[15px] font-semibold mb-2">등급 목록</h4>
            {grades.map(({ grade, color }) => (
              <div
                key={grade.grade}
                className="flex items-center justify-between border border-gray-300 rounded p-1 mb-1"
              >
                <div className="flex items-center gap-1">
                  <span
                    className="w-2.5 h-2.5 rounded-full"
                    style={{ backgroundColor: color }}
                  ></span>
                  <span className="text-[10px]">{`${grade.grade} ${grade.seats.length}석`}</span>
                </div>
                <span className="text-[10px]">{`${grade.price.toLocaleString()}원`}</span>
                <button
                  onClick={() => handleRemoveGrade(grade.grade)}
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
    </div>
  );
};

export default EditSeatSetting;
