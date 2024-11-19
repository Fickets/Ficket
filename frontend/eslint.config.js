import { defineConfig } from 'eslint-define-config';

export default defineConfig({
  // 환경 설정: 브라우저, Node.js, 최신 ES 기능
  env: {
    browser: true,
    es2021: true,
    node: true,
  },

  // 확장할 기본 규칙들
  extends: [
    'eslint:recommended', // 기본 ESLint 권장 규칙
    'plugin:react/recommended', // React 권장 규칙
    'plugin:@typescript-eslint/recommended', // TypeScript 권장 규칙
    'plugin:prettier/recommended', // Prettier와 연동
  ],

  // 플러그인 추가
  plugins: ['react', '@typescript-eslint', 'prettier'],

  // 파서 옵션
  parserOptions: {
    ecmaVersion: 2021, // 최신 ECMAScript 구문
    sourceType: 'module', // ESM 사용
    ecmaFeatures: {
      jsx: true, // JSX 지원
    },
  },

  // 규칙 설정
  rules: {
    'prettier/prettier': 'error', // Prettier 규칙 위반 시 오류 표시
    'react/react-in-jsx-scope': 'off', // React 17+에서 필요 없음
    'react/prop-types': 'off', // PropTypes 사용 안 함 (TypeScript 사용 시 불필요)
    '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_' }], // 사용하지 않는 변수 경고
  },

  // React 버전 자동 감지
  settings: {
    react: {
      version: 'detect',
    },
  },
});
import { defineConfig } from 'eslint-define-config';

export default defineConfig({
  // 환경 설정: 브라우저, Node.js, 최신 ES 기능
  env: {
    browser: true,
    es2021: true,
    node: true,
  },

  // 확장할 기본 규칙들
  extends: [
    'eslint:recommended', // 기본 ESLint 권장 규칙
    'plugin:react/recommended', // React 권장 규칙
    'plugin:@typescript-eslint/recommended', // TypeScript 권장 규칙
    'plugin:prettier/recommended', // Prettier와 연동
  ],

  // 플러그인 추가
  plugins: ['react', '@typescript-eslint', 'prettier'],

  // 파서 옵션
  parserOptions: {
    ecmaVersion: 2021, // 최신 ECMAScript 구문
    sourceType: 'module', // ESM 사용
    ecmaFeatures: {
      jsx: true, // JSX 지원
    },
  },

  // 규칙 설정
  rules: {
    'prettier/prettier': 'error', // Prettier 규칙 위반 시 오류 표시
    'react/react-in-jsx-scope': 'off', // React 17+에서 필요 없음
    'react/prop-types': 'off', // PropTypes 사용 안 함 (TypeScript 사용 시 불필요)
    '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_' }], // 사용하지 않는 변수 경고
  },

  // React 버전 자동 감지
  settings: {
    react: {
      version: 'detect',
    },
  },
});
