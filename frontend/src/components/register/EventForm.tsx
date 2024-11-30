import { useState, useEffect } from 'react';
import Select from 'react-select';
import { EventData, GenreOption, EventFormProps } from '../../types/register';
import { fetchCompanies, fetchStages } from '../../service/register/api';

const genres: GenreOption[] = [
  { label: '뮤지컬', value: '뮤지컬' },
  { label: '콘서트', value: '콘서트' },
  { label: '스포츠', value: '스포츠' },
  { label: '전시/행사', value: '전시_행사' },
  { label: '클래식/무용', value: '클래식_무용' },
  { label: '아동/가족', value: '아동_가족' },
];

const EventForm = ({ onChange }: EventFormProps) => {
  const [companies, setCompanies] = useState<
    { value: number; label: string }[]
  >([]);
  const [stages, setStages] = useState<
    { value: number; label: string; img: string }[]
  >([]);
  const [formState, setFormState] = useState<EventData>({
    adminId: 0,
    companyId: 0,
    stageId: 0,
    genre: [],
    age: '전체관람가',
    content: '',
    title: '',
    subTitle: '',
    runningTime: 0,
    ticketingTime: '',
    reservationLimit: 0,
    eventDate: [],
    seats: [],
  });

  const [selectedStageImg, setSelectedStageImg] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const companyData = await fetchCompanies();
        const stageData = await fetchStages();

        setCompanies(
          companyData.map((c: { companyId: number; companyName: string }) => ({
            value: c.companyId,
            label: c.companyName,
          }))
        );

        setStages(
          stageData.map(
            (s: {
              stageId: number;
              stageName: string;
              eventStageImg: string;
            }) => ({
              value: s.stageId,
              label: s.stageName,
              img: s.eventStageImg,
            })
          )
        );
      } catch (error) {
        console.error('Error fetching companies or stages:', error);
      }
    };

    fetchData();
  }, []);

  const handleInputChange = (field: keyof EventData, value: any) => {
    const updatedState = { ...formState, [field]: value };
    setFormState(updatedState);
    onChange(updatedState);
  };

  const handleStageChange = (
    selected: { value: number; label: string; img: string } | null
  ) => {
    if (selected) {
      handleInputChange('stageId', selected.value);
      setSelectedStageImg(selected.img); // Update stage image
    } else {
      handleInputChange('stageId', 0);
      setSelectedStageImg(null); // Clear stage image
    }
  };

  return (
    <div className="w-full bg-white rounded-lg shadow-md p-6 border border-gray-200 font-sans">
      <h3 className="text-xl font-semibold text-gray-800 mb-6">
        공연 정보 입력
      </h3>
      <form>
        {/* 제목 */}
        <div className="mb-4">
          <label className="block mb-2 text-sm font-medium text-gray-700">
            제목
          </label>
          <input
            type="text"
            value={formState.title}
            onChange={(e) => handleInputChange('title', e.target.value)}
            className="border border-gray-300 rounded-lg w-full p-3 text-sm focus:outline-none focus:ring focus:ring-blue-200"
            placeholder="공연 제목을 입력해 주세요"
          />
        </div>

        {/* 부제목 */}
        <div className="mb-4">
          <label className="block mb-2 text-sm font-medium text-gray-700">
            부제목
          </label>
          <input
            type="text"
            value={formState.subTitle}
            onChange={(e) => handleInputChange('subTitle', e.target.value)}
            className="border border-gray-300 rounded-lg w-full p-3 text-sm focus:outline-none focus:ring focus:ring-blue-200"
            placeholder="부제목을 입력해 주세요"
          />
        </div>

        {/* 공연회사 */}
        <div className="mb-4">
          <label className="block mb-2 text-sm font-medium text-gray-700">
            공연회사
          </label>
          <Select
            options={companies}
            onChange={(selected) =>
              handleInputChange('companyId', selected?.value || 0)
            }
            placeholder="공연회사를 검색해 주세요"
            isClearable
          />
        </div>

        {/* 공연장 */}
        <div className="mb-4">
          <label className="block mb-2 text-sm font-medium text-gray-700">
            공연장
          </label>
          <Select
            options={stages}
            onChange={handleStageChange}
            placeholder="공연장을 검색해 주세요"
            isClearable
          />
        </div>

        {/* 티켓팅 날짜/시간 */}
        <div className="mb-4">
          <label className="block mb-2 text-sm font-medium text-gray-700">
            티켓팅 시작 날짜/시간
          </label>
          <input
            type="datetime-local"
            value={formState.ticketingTime}
            onChange={(e) => handleInputChange('ticketingTime', e.target.value)}
            className="border border-gray-300 rounded-lg w-full p-3 text-sm focus:outline-none focus:ring focus:ring-blue-200"
          />
        </div>

        {/* 1인당 예매 제한 */}
        <div className="mb-4">
          <label className="block mb-2 text-sm font-medium text-gray-700">
            1인당 예매 제한
          </label>
          <input
            type="number"
            min="1"
            value={formState.reservationLimit}
            onChange={(e) =>
              handleInputChange('reservationLimit', Number(e.target.value))
            }
            className="border border-gray-300 rounded-lg w-full p-3 text-sm focus:outline-none focus:ring focus:ring-blue-200"
            placeholder="예: 4"
          />
        </div>

        {/* 상영시간 */}
        <div className="mb-4">
          <label className="block mb-2 text-sm font-medium text-gray-700">
            상영시간 (분)
          </label>
          <input
            type="number"
            value={formState.runningTime}
            onChange={(e) =>
              handleInputChange('runningTime', Number(e.target.value))
            }
            className="border border-gray-300 rounded-lg w-full p-3 text-sm focus:outline-none focus:ring focus:ring-blue-200"
          />
        </div>

        {/* 관람 연령 */}
        <div className="mb-4">
          <label className="block mb-2 text-sm font-medium text-gray-700">
            관람 연령
          </label>
          <select
            value={formState.age}
            onChange={(e) => handleInputChange('age', e.target.value)}
            className="border border-gray-300 rounded-lg w-full p-3 text-sm focus:outline-none focus:ring focus:ring-blue-200"
          >
            <option value="전체관람가">전체 관람가</option>
            <option value="만_7세_이상">만 7세 이상</option>
            <option value="만_12세_이상">만 12세 이상</option>
            <option value="만_15세_이상">만 15세 이상</option>
            <option value="청소년_관람불가">청소년 관람 불가</option>
          </select>
        </div>

        {/* 장르 */}
        <div className="mb-4">
          <label className="block mb-2 text-sm font-medium text-gray-700">
            장르
          </label>
          <div className="flex flex-wrap gap-4">
            {genres.map((genre) => (
              <label key={genre.value} className="flex items-center space-x-2">
                <input
                  type="checkbox"
                  checked={formState.genre.includes(genre.value)}
                  onChange={() => {
                    const newGenres = formState.genre.includes(genre.value)
                      ? formState.genre.filter((g) => g !== genre.value)
                      : [...formState.genre, genre.value];
                    handleInputChange('genre', newGenres);
                  }}
                  className="focus:ring focus:ring-blue-200"
                />
                <span className="text-sm text-gray-700">{genre.label}</span>
              </label>
            ))}
          </div>
        </div>
      </form>
    </div>
  );
};

export default EventForm;
