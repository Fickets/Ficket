/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Noto Sans KR"],
      },
      fontWeight: {
        // 커스텀 폰트 굵기 추가 (예: 350, 450 등)
        light: 300,
        regular: 400,
        medium: 500,
        semibold: 700,
        bold: 800,
      },
    },
  },
  plugins: [
    function ({ addUtilities }) {
      addUtilities({
        ".scrollbar-hide": {
          "scrollbar-width": "none", // Firefox
          "-ms-overflow-style": "none", // IE and Edge
        },
        ".scrollbar-hide::-webkit-scrollbar": {
          display: "none", // Chrome, Safari, Edge
        },
      });
    },
    require("@tailwindcss/typography"),
  ],
};
