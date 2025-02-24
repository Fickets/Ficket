# [Ficket - 얼굴 인식 기반 티켓팅 플랫폼](http://ec2-43-201-23-107.ap-northeast-2.compute.amazonaws.com/)

## 1. 프로젝트 소개 🚀

### 배경

1. **비대면 환경의 확산**
    - COVID-19 팬데믹 이후, 비대면 서비스와 디지털화된 검증 시스템에 대한 수요가 증가하였습니다.
    - 기존의 QR코드, 바코드 등 물리적 티켓 검증 방식은 위조 및 유실 위험이 존재합니다.

2. **보안성과 편리성의 필요성**
    - 이벤트 및 공연 현장에서의 티켓 위조 방지와 신속한 입장 관리가 중요한 과제가 되었으며, 이를 해결하기 위해 보다 높은 보안성을 제공하는 기술이 요구되었습니다.

3. **AI 및 생체인식 기술의 발전**
    - 얼굴인식 기술의 발전으로 인해 실시간으로 높은 정확도를 가진 생체인식 검증 시스템을 구축할 수 있는 환경이 마련되었습니다.

### 목적

1. **티켓 검증의 간소화**
    - 사용자 얼굴 정보를 통해 빠르고 정확하게 티켓을 발급하고 입장을 확인합니다.
    - 추가적인 신분증, QR코드 또는 티켓의 소지가 필요하지 않아, 사용자의 편리성을 극대화합니다.

2. **보안 강화**
    - 얼굴인식을 통해 티켓의 위조 및 중복 사용을 방지하고, 개인화된 검증 시스템을 제공합니다.
    - AES 암호화와 S3 저장소를 활용하여 사용자의 민감한 데이터를 안전하게 보호합니다.

3. **운영 효율성 개선**
    - 이벤트 운영자에게 실시간 검증 데이터를 제공하여 입장 관리 및 고객 경험을 개선합니다.
    - 자동화된 프로세스를 통해 인력 및 운영 비용을 절감합니다.

4. **통합 경험 제공**
    - 사용자 등록, 티켓 발급, 입장 검증까지의 모든 과정을 하나의 시스템에서 제공하여 통합된 고객 경험을 제공합니다.

---

## 2. 기획 및 설계 💡

### 프로젝트 명세

https://www.notion.so/Ficket-125cb8b3a5cf80f5966bd3497a28b95d?pvs=4

### 와이어 프레임 & 화면 설계

https://www.figma.com/design/M9VDb3yhlblGtRa8O3DfQr/Ficket?node-id=9-1722&t=qpZDQ4gt0FRLL1xA-1

---

## 3. 제작기간 && 팀원소개 🏃‍🏃‍♀️💨

### 2024-11-21 ~ 2024-01-20🔥

| 이름                                 | 담당 기능                                                                             |
|------------------------------------|-----------------------------------------------------------------------------------|
| [최용수](https://github.com/TutiTuti) | Kubernetes, Jenkins, CI/CD, OAuth2 인증/인가, 정산, 메인페이지, 티켓 검사, 회원 관리, 날짜 선택, 공연 조회 등 |
| [오형상](https://github.com/ohy1023)  | MSA 구성, 좌석 선점, 얼굴 인식, 랭킹, 대기열, Locust 부하 테스트 ,PortOne 결제, 마이티켓, 공연 관리 등           |

---
## 4. 🛠 기술 스택
<div align=middle>
  <h3>🎨  FrontEnd</h3>
  <div>
    <img src="https://img.shields.io/badge/typescript-3178C6?style=for-the-badge&logo=typescript&logoColor=white">
    <img src="https://img.shields.io/badge/react-61DAFB?style=for-the-badge&logo=react&logoColor=black">
    <img src="https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white">
    <img src="https://img.shields.io/badge/tailwind css-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white">
    <br>
    <img src="https://img.shields.io/badge/react zustand-E34F26?style=for-the-badge&logo=react&logoColor=blue">
    <img src="https://img.shields.io/badge/node.js-339933?style=for-the-badge&logo=nodedotjs&logoColor=white">
    <img src="https://img.shields.io/badge/html5-E34F26?style=for-the-badge&logo=html5&logoColor=white">
    <br>
    <img src="https://img.shields.io/badge/css3-1572B6?style=for-the-badge&logo=css3&logoColor=white">
    <img src="https://img.shields.io/badge/stomp js-000000?style=for-the-badge&logo=webSocket&logoColor=white">
  </div>
  <h3>⚙  BackEnd</h3>
  <div>
      <img src="https://img.shields.io/badge/Java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white">
      <img src="https://img.shields.io/badge/spring boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
      <img src="https://img.shields.io/badge/spring webclient-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
      <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">
      <br>
      <img src="https://img.shields.io/badge/Feign-0075A8?style=for-the-badge&logo=apachekafka&logoColor=white">
      <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
      <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white">
      <img src="https://img.shields.io/badge/OAuth-0081CB?style=for-the-badge&logo=auth0&logoColor=white">
      <img src="https://img.shields.io/badge/Spring%20Cloud-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
      <img src="https://img.shields.io/badge/Resilience4j-FF9E0F?style=for-the-badge&logo=java&logoColor=white">
      <br>
      <img src="https://img.shields.io/badge/python-3776AB?style=for-the-badge&logo=python&logoColor=white">
      <img src="https://img.shields.io/badge/InsightFace-EAEAEA?style=for-the-badge&logo=opencv&logoColor=black">
      <img src="https://img.shields.io/badge/Flask-DDDDDD?style=for-the-badge&logo=flask&logoColor=black">
      <img src="https://img.shields.io/badge/Flask-000000?style=for-the-badge&logo=flask&logoColor=white">
      <img src="https://img.shields.io/badge/boto3-FFCB05?style=for-the-badge&logo=boto3&logoColor=white">
  </div>
  <h3>💾  Database & Caching</h3>
  <div>
    <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
    <img src="https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white">
    <img src="https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white">
    <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">
  </div>
  <h3>🏠  Cloud & Infrastructure</h3>
  <div>
    <img src="https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white">
    <img src="https://img.shields.io/badge/AWS%20Lambda-FF9900?style=for-the-badge&logo=awslambda&logoColor=white">
    <img src="https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white">
    <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
    <img src="https://img.shields.io/badge/Eureka-7B42BC?style=for-the-badge&logo=spring&logoColor=white">
  </div>
  <h3>✉  Monitoring & Logging</h3>
  <div>
    <img src="https://img.shields.io/badge/Apache%20Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white">
    <img src="https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white">
  </div>
  <h3>🎮  Performance Testing & Load Testing</h3>
  <div>
    <img src="https://img.shields.io/badge/Locust-000000?style=for-the-badge&logo=locust&logoColor=white">
  </div>
  <h3>>💬  Cooperation</h3>
  <div>
    <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white">
    <img src="https://img.shields.io/badge/Jira-0052CC?style=for-the-badge&logo=jira&logoColor=white">
    <img src="https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white">
    <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black">
    <img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white">
  </div>
    
---

## 5. 아키텍처 📃

추가 예정

---

## 6. API 명세서 📡

- Swagger: [http://ec2-52-78-23-203.ap-northeast-2.compute.amazonaws.com:9000/swagger-ui/index.html](http://ec2-52-78-23-203.ap-northeast-2.compute.amazonaws.com:9000/swagger-ui/index.html)
- Notion: [API 명세서 바로가기](https://www.notion.so/API-125cb8b3a5cf81dbbff3cec772823e6a?pvs=4)

---

## 7. ERD 🗄️

![ficket_erd.png](img%2Fficket_erd.png)

---

## 8. 주요 기술 ✨
- 쿠버네티스 
    - [??]()
- MSA
    - [Eureka Server & Eureka Client & Gateway 설정](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-1-Eureka-Server-Eureka-Client-Gateway-%EC%84%A4%EC%A0%95)
    - [인증/인가 설정](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-2-%EC%9D%B8%EC%A6%9D%EC%9D%B8%EA%B0%80-%EC%84%A4%EC%A0%95)
    - [Config 서버 구축](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-3-Config-%EC%84%9C%EB%B2%84-%EA%B5%AC%EC%B6%95)
    - [Spring Cloud Bus와 RabbitMQ를 활용한 Config 변경 자동 반영 설정](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-4-Spring-Cloud-Bus%EC%99%80-RabbitMQ%EB%A5%BC-%ED%99%9C%EC%9A%A9%ED%95%9C-Config-%EB%B3%80%EA%B2%BD-%EC%9E%90%EB%8F%99-%EB%B0%98%EC%98%81-%EC%84%A4%EC%A0%95)
    - [설정 파일의 암호화 처리 : 비대칭키를 이용한 암/복호화](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-5-%EC%84%A4%EC%A0%95-%ED%8C%8C%EC%9D%BC%EC%9D%98-%EC%95%94%ED%98%B8%ED%99%94-%EC%B2%98%EB%A6%AC-%EB%B9%84%EB%8C%80%EC%B9%AD%ED%82%A4%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EC%95%94%EB%B3%B5%ED%98%B8%ED%99%94)
    - [암호화된 설정 파일 보호와 /decrypt API 접근 제어](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-6-%EC%95%94%ED%98%B8%ED%99%94%EB%90%9C-%EC%84%A4%EC%A0%95-%ED%8C%8C%EC%9D%BC-%EB%B3%B4%ED%98%B8%EC%99%80-decrypt-API-%EC%A0%91%EA%B7%BC-%EC%A0%9C%EC%96%B4)
    - [암호화된 설정 파일 보호와 /decrypt API 접근 제어](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-7-Config-%EC%84%9C%EB%B2%84%EC%99%80-%ED%81%B4%EB%9D%BC%EC%9D%B4%EC%96%B8%ED%8A%B8%EC%97%90%EC%84%9C-%EA%B3%B5%EA%B0%9C%ED%82%A4%EC%99%80-%EA%B0%9C%EC%9D%B8%ED%82%A4%EB%A5%BC-%ED%99%9C%EC%9A%A9%ED%95%9C-%EC%95%94%ED%98%B8%ED%99%94%EB%B3%B5%ED%98%B8%ED%99%94-%EC%84%A4%EC%A0%95)
    - [Config 서버와 클라이언트에서 공개키와 개인키를 활용한 암호화/복호화 설정](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-7-Config-%EC%84%9C%EB%B2%84%EC%99%80-%ED%81%B4%EB%9D%BC%EC%9D%B4%EC%96%B8%ED%8A%B8%EC%97%90%EC%84%9C-%EA%B3%B5%EA%B0%9C%ED%82%A4%EC%99%80-%EA%B0%9C%EC%9D%B8%ED%82%A4%EB%A5%BC-%ED%99%9C%EC%9A%A9%ED%95%9C-%EC%95%94%ED%98%B8%ED%99%94%EB%B3%B5%ED%98%B8%ED%99%94-%EC%84%A4%EC%A0%95)
    - [Feign Client를 활용한 서버간 동기 통신](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-9-Feign-Client-%EC%84%9C%EB%B2%84%EA%B0%84-%ED%86%B5%EC%8B%A0-%ED%95%98%EA%B8%B0)
    - [서비스 장애 대응 Circuit Breaker 구현(feat. Resilience4J)](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-10-%EC%84%9C%EB%B9%84%EC%8A%A4-%EC%9E%A5%EC%95%A0-%EB%8C%80%EC%9D%91-Circuit-Breaker-%EA%B5%AC%ED%98%84feat.-Resilience4J)
- 대기열
    - [대기열 설계](https://velog.io/@zvyg1023/%ED%8B%B0%EC%BC%93%ED%8C%85-%EC%84%9C%EB%B9%84%EC%8A%A4-%EC%84%A4%EA%B3%84)
    - [Kafka, Redis, WebSocket, WebFlux를 활용한 대기열 구현](https://velog.io/@zvyg1023/%EB%8C%80%EA%B8%B0%EC%97%B4-%EA%B5%AC%ED%98%84-2-Kafka-Redis-WebSocket-WebFlux%EB%A5%BC-%ED%99%9C%EC%9A%A9%ED%95%9C-%EB%8C%80%EA%B8%B0%EC%97%B4-%EA%B4%80%EB%A6%AC)
    - [Locust를 활용한 부하 테스트](https://github.com/ohy1023/ficket-ticketing-locust)
- 얼굴 인식
    - [Flask, Eureka, Spring Config, RabbitMQ 통합](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-8-Flask-Eureka-Spring-Config-RabbitMQ-%ED%86%B5%ED%95%A9)
    - [이미지 암호화를 위한 S3 SSE-KMS 적용](https://velog.io/@zvyg1023/S3-SSE-KMS-%EC%A0%81%EC%9A%A9%ED%95%98%EA%B8%B0)
    - [Insightface를 활용한 얼굴 인식 개발](https://velog.io/@zvyg1023/Insightface%EB%A5%BC-%ED%99%9C%EC%9A%A9%ED%95%9C-%EC%96%BC%EA%B5%B4-%EC%9D%B8%EC%8B%9D)
- 검색
    - [Elasticsearch Indexing 설계](https://velog.io/@zvyg1023/%EC%97%98%EB%9D%BC%EC%8A%A4%ED%8B%B1-%EC%84%9C%EC%B9%98-%EA%B2%80%EC%83%89-%EA%B5%AC%ED%98%84-1-%EC%83%89%EC%9D%B8-%EC%8B%9C%EC%8A%A4%ED%85%9C-%EC%84%A4%EA%B3%84)
    - [Elasticsearch 8.13.4, Kibana 설치 및 S3 연결 (docker-compose)](https://velog.io/@zvyg1023/%EC%97%98%EB%9D%BC%EC%8A%A4%ED%8B%B1-%EC%84%9C%EC%B9%98-%EA%B2%80%EC%83%89-%EA%B5%AC%ED%98%84-2-Elasticsearch-8.13.4-Kibana-%EC%84%A4%EC%B9%98-%EB%B0%8F-S3-%EC%97%B0%EA%B2%B0-docker-compose)
    - [전체 색인 구현 (Event)](https://velog.io/@zvyg1023/%EC%97%98%EB%9D%BC%EC%8A%A4%ED%8B%B1-%EC%84%9C%EC%B9%98-%EA%B2%80%EC%83%89-%EA%B5%AC%ED%98%84-3-%EC%A0%84%EC%B2%B4-%EC%83%89%EC%9D%B8-%EA%B5%AC%ED%98%84-Search-%EC%84%9C%EB%B2%84)
    - [전체 색인 구현 (Search)](https://velog.io/@zvyg1023/%EC%97%98%EB%9D%BC%EC%8A%A4%ED%8B%B1-%EC%84%9C%EC%B9%98-%EA%B2%80%EC%83%89-%EA%B5%AC%ED%98%84-4-%EC%A0%84%EC%B2%B4-%EC%83%89%EC%9D%B8-%EA%B5%AC%ED%98%84-Search-%EC%84%9C%EB%B2%84)
    - [부분 색인 구현](https://velog.io/@zvyg1023/%EC%97%98%EB%9D%BC%EC%8A%A4%ED%8B%B1-%EC%84%9C%EC%B9%98-%EA%B2%80%EC%83%89-%EA%B5%AC%ED%98%84-5-%EB%B6%80%EB%B6%84-%EC%83%89%EC%9D%B8-%EA%B5%AC%ED%98%84)
    - [검색 기능 구현](https://velog.io/@zvyg1023/%EC%97%98%EB%9D%BC%EC%8A%A4%ED%8B%B1-%EC%84%9C%EC%B9%98-%EA%B2%80%EC%83%89-%EA%B5%AC%ED%98%84-6-%EA%B2%80%EC%83%89-%EA%B8%B0%EB%8A%A5-%EA%B5%AC%ED%98%84)
- 프론트
    - [zustand를 활용한 로그인 정보 유지]()
    - [반응형 처리하기](https://velog.io/@zvyg1023/React-%EB%B0%98%EC%9D%91%ED%98%95-%EC%B2%98%EB%A6%AC%ED%95%98%EA%B8%B0-w.-react-responsive)
- 기타
    - [OAuth + JWT + Redis를 이용한 카카오 로그인]()
    - [AWS Lambda를 이용해 이미지 리사이징 적용 - 이미지 로딩 속도 최적화](https://velog.io/@zvyg1023/AWS-Lambda%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%B4-%EC%9D%B4%EB%AF%B8%EC%A7%80-%EB%A6%AC%EC%82%AC%EC%9D%B4%EC%A7%95-%EC%A0%81%EC%9A%A9-%EC%9D%B4%EB%AF%B8%EC%A7%80-%EB%A1%9C%EB%94%A9-%EC%86%8D%EB%8F%84-%EC%B5%9C%EC%A0%81%ED%99%94)
    - [Redis(Redisson) 분산락을 활용한 좌석 선점](https://velog.io/@zvyg1023/Spring-RedisRedisson-%EB%B6%84%EC%82%B0%EB%9D%BD%EC%9D%84-%ED%99%9C%EC%9A%A9%ED%95%9C-%EC%A2%8C%EC%84%9D-%EC%84%A0%EC%A0%90-%EA%B0%9C%EB%B0%9C)
    - [Redis로 조회수 랭킹 시스템 구현](https://velog.io/@zvyg1023/Spring-Redis%EB%A1%9C-%EC%A1%B0%ED%9A%8C%EC%88%98-%EB%9E%AD%ED%82%B9-%EC%8B%9C%EC%8A%A4%ED%85%9C-%EA%B5%AC%ED%98%84%ED%95%98%EA%B8%B0)
    - [Redis 예매율 순위 시스템 구현](https://velog.io/@zvyg1023/Spring-Redis-%EC%98%88%EB%A7%A4%EC%9C%A8-%EC%88%9C%EC%9C%84-%EC%8B%9C%EC%8A%A4%ED%85%9C-%EA%B5%AC%ED%98%84)
    - [포트원 연동을 통한 결제 시스템](https://velog.io/@zvyg1023/%ED%8F%AC%ED%8A%B8%EC%9B%90%EC%9D%84-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EC%B9%B4%EC%B9%B4%EC%98%A4%ED%8E%98%EC%9D%B4-%EA%B2%B0%EC%A0%9C-%EC%97%B0%EB%8F%99-API-V2-Webhook-V2)

--- 

## 9. 외부 리소스 정보 📁

[포트원 개발자 센터](https://developers.portone.io/opi/ko/readme?v=v2)
