import { useState } from 'react';

type PolicyAgreeProps = {
  onAgreeChange: (allAgreed: boolean) => void;
};

function PolicyAgree({ onAgreeChange }: PolicyAgreeProps) {
  const [isAgree1, setIsAgree1] = useState(false);
  const [isAgree2, setIsAgree2] = useState(false);

  const handleAgreementChange = (index: number, value: boolean) => {
    const updatedIsAgree1 = index === 1 ? value : isAgree1;
    const updatedIsAgree2 = index === 2 ? value : isAgree2;

    if (index === 1) setIsAgree1(value);
    if (index === 2) setIsAgree2(value);

    onAgreeChange(updatedIsAgree1 && updatedIsAgree2);
  };

  return (
    <div className="w-full max-w-[400px] h-[400px] flex flex-col items-center border border-white bg-[#FFFFFF] p-4">
      <h2 className="text-lg font-bold mb-2">원본정보 수집·이용 동의</h2>
      <table className="w-full border-collapse border border-gray-400 text-sm">
        <thead>
          <tr className="bg-gray-200">
            <th className="border border-gray-400 p-2">항목</th>
            <th className="border border-gray-400 p-2">목적</th>
            <th className="border border-gray-400 p-2">보유/이용기간</th>
            <th className="border border-gray-400 p-2">동의여부</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td className="border border-gray-400 p-2">얼굴정보 (원본정보)</td>
            <td className="border border-gray-400 p-2">
              [필수*] 이용자 식별 및 본인인증
            </td>
            <td className="border border-gray-400 p-2">
              예약 공연이 끝난 후 1일 뒤
            </td>
            <td className="border border-gray-400 p-2 text-center">
              <label>
                <input
                  type="checkbox"
                  className="mr-1"
                  checked={isAgree1}
                  onChange={(e) => handleAgreementChange(1, e.target.checked)}
                />
                동의함
              </label>
            </td>
          </tr>
        </tbody>
      </table>
      <h2 className="text-lg font-bold mt-4">민감정보(특정정보) 처리동의</h2>
      <table className="w-full border-collapse border border-gray-400 text-sm">
        <thead>
          <tr className="bg-gray-200">
            <th className="border border-gray-400 p-2">항목</th>
            <th className="border border-gray-400 p-2">목적</th>
            <th className="border border-gray-400 p-2">보유/이용기간</th>
            <th className="border border-gray-400 p-2">동의여부</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td className="border border-gray-400 p-2">얼굴정보 (원본정보)</td>
            <td className="border border-gray-400 p-2">
              [필수*] 이용자 식별 및 본인인증
            </td>
            <td className="border border-gray-400 p-2">
              예약 공연이 끝난 후 1일 뒤
            </td>
            <td className="border border-gray-400 p-2 text-center">
              <label>
                <input
                  type="checkbox"
                  className="mr-1"
                  checked={isAgree2}
                  onChange={(e) => handleAgreementChange(2, e.target.checked)}
                />
                동의함
              </label>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  );
}

export default PolicyAgree;
