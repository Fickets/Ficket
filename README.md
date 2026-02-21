# Ficket - 얼굴 인식 기반 티켓팅 플랫폼

## 1. 프로젝트 소개 🚀

Ficket은 얼굴 인식을 활용한 티켓 예매 및 검증 서비스입니다.
사용자는 티켓 예매 시 얼굴 정보를 등록하고, 입장 시 얼굴 인식만으로 빠르고 안전하게 검증을 완료할 수 있습니다.
별도의 실물 티켓이나 추가 인증 없이 간편하게 이용할 수 있으며, 실시간 검증 시스템을 통해 원활한 이벤트 운영을 지원합니다.

---

## 2. 기획 및 설계 💡

### 프로젝트 명세

https://www.notion.so/Ficket-125cb8b3a5cf80f5966bd3497a28b95d?pvs=4

### 와이어 프레임 & 화면 설계

https://www.figma.com/design/M9VDb3yhlblGtRa8O3DfQr/Ficket?node-id=9-1722&t=qpZDQ4gt0FRLL1xA-1

---

## 3. JIRA를 활용한 협업

### 스크럼

<img src="etc/img/jira/SCRUM.png" width="400"/>

### 번다운 차트
| ![WEEK6.png](etc%2Fimg%2Fjira%2FWEEK6.png) | ![WEEK7](etc%2Fimg%2Fjira%2FWEEK7.png) |
|--------------------------------------------|----------------------------------------|
| ![WEEK8](etc%2Fimg%2Fjira%2FWEEK8.png)     | ![WEEK9](etc%2Fimg%2Fjira%2FWEEK9.png) |

## 4. 제작기간 && 팀원소개 🏃‍🏃‍♀️💨

### 2024-11-21 ~ 2025-02-25🔥

| 이름                                 | 담당 기능                                                                   |
|------------------------------------|-------------------------------------------------------------------------|
| [최용수](https://github.com/TutiTuti) | Jenkins, CI/CD, OAuth2 인증/인가, 정산, 메인페이지, 티켓 검사, 회원 관리, 날짜 선택, 공연 조회 등   |
| [오형상](https://github.com/ohy1023)  | MSA 구성, 좌석 선점, 얼굴 인식, 랭킹, 대기열, Locust 부하 테스트 ,PortOne 결제, 마이티켓, 공연 관리 등 |

---

## 5. 🛠 기술 스택

<div align=middle>
  <h3>🎨 FrontEnd</h3>
  <div>
    <img src="https://img.shields.io/badge/typescript-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript">
    <img src="https://img.shields.io/badge/react-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React">
    <img src="https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white" alt="Vite">
    <img src="https://img.shields.io/badge/tailwind css-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white" alt="Tailwind CSS">
    <br>
    <img src="https://img.shields.io/badge/react zustand-E34F26?style=for-the-badge&logo=react&logoColor=blue" alt="React Zustand">
    <img src="https://img.shields.io/badge/node.js-339933?style=for-the-badge&logo=nodedotjs&logoColor=white" alt="Node.js">
    <img src="https://img.shields.io/badge/html5-E34F26?style=for-the-badge&logo=html5&logoColor=white" alt="HTML5">
    <br>
    <img src="https://img.shields.io/badge/css3-1572B6?style=for-the-badge&logo=css3&logoColor=white" alt="CSS3">
    <img src="https://img.shields.io/badge/stomp js-000000?style=for-the-badge&logo=webSocket&logoColor=white" alt="STOMP.js">
  </div>
  <h3>⚙ BackEnd</h3>
  <div>
    <img src="https://img.shields.io/badge/Java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java">
    <img src="https://img.shields.io/badge/spring boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot">
    <img src="https://img.shields.io/badge/spring webclient-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring WebClient">
    <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle">
    <br>
    <img src="https://img.shields.io/badge/Feign-0075A8?style=for-the-badge&logo=java&logoColor=white" alt="Feign">
    <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Security">
    <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT">
    <img src="https://img.shields.io/badge/OAuth-0081CB?style=for-the-badge&logo=auth0&logoColor=white" alt="OAuth">
    <img src="https://img.shields.io/badge/Spring%20Cloud-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Cloud">
    <img src="https://img.shields.io/badge/Resilience4j-FF9E0F?style=for-the-badge&logo=java&logoColor=white" alt="Resilience4j">
    <br>
    <img src="https://img.shields.io/badge/Spring%20Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Batch">
    <img src="https://img.shields.io/badge/Quartz-007396?style=for-the-badge&logo=java&logoColor=white" alt="Quartz Scheduler">
    <img src="https://img.shields.io/badge/python-3776AB?style=for-the-badge&logo=python&logoColor=white" alt="Python">
    <img src="https://img.shields.io/badge/InsightFace-EAEAEA?style=for-the-badge&logo=opencv&logoColor=black" alt="InsightFace">
    <img src="https://img.shields.io/badge/Flask-DDDDDD?style=for-the-badge&logo=flask&logoColor=black" alt="Flask">
    <img src="https://img.shields.io/badge/boto3-FFCB05?style=for-the-badge&logo=boto3&logoColor=white" alt="Boto3">
  </div>
  <h3>💾 Database & Caching</h3>
  <div>
    <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL">
    <img src="https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white" alt="MongoDB">
    <img src="https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white" alt="Elasticsearch">
    <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis">
  </div>
  <h3>🏠 Cloud & Infrastructure</h3>
  <div>
    <img src="https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white" alt="AWS EC2">
    <img src="https://img.shields.io/badge/AWS%20Lambda-FF9900?style=for-the-badge&logo=awslambda&logoColor=white" alt="AWS Lambda">
    <img src="https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white" alt="AWS S3">
    <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker">
    <img src="https://img.shields.io/badge/Eureka-7B42BC?style=for-the-badge&logo=spring&logoColor=white" alt="Eureka">
  </div>
  <h3>✉ Event Streaming</h3>
  <div>
    <img src="https://img.shields.io/badge/Apache%20Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white" alt="Apache Kafka">
    <img src="https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white" alt="RabbitMQ">
  </div>
  <h3>🛠️ Monitoring & Logging</h3>
  <div>
    <img src="https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white" alt="Prometheus">
    <img src="https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white" alt="Grafana">
    <img src="https://img.shields.io/badge/Zipkin-000000?style=for-the-badge&logo=zipkin&logoColor=white" alt="Zipkin">
  </div>
  <h3>🎮 Performance Testing & Load Testing</h3>
  <div>
    <img src="https://img.shields.io/badge/Locust-000000?style=for-the-badge&logo=locust&logoColor=white" alt="Locust">
  </div>
  <h3>💬 Cooperation</h3>
  <div>
    <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white" alt="GitHub">
    <img src="https://img.shields.io/badge/Jira-0052CC?style=for-the-badge&logo=jira&logoColor=white" alt="Jira">
    <img src="https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white" alt="Figma">
    <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger">
    <img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white" alt="Notion">
  </div>
</div>

---

## 6. 아키텍처 📃

![ficket_architecutre.png](etc%2Fimg%2Farchitecture%2Fficket_architecutre.png)

---

## 7. ERD 🗄️

![ERD.png](etc%2Fimg%2Ferd%2FERD.png)

---

## 8. API 명세서 📡

- Swagger: [https://api.ficket.shop](https://api.ficket.shop)
- Notion: [API 명세서 바로가기](https://www.notion.so/API-125cb8b3a5cf81dbbff3cec772823e6a?pvs=4)

---

## 9. 서비스 화면 (PC) 🖥️

1. 메인 페이지

   <img src="./etc/gif/mainBanner.gif" width="80%" /> <br>

2. 공연 상세 페이지

   <img src="./etc/gif/eventDetail.gif" width="80%" alt="공연_상세_페이지" /> <br>

3. 티켓팅
   
   <img src="./etc/gif/ticketReservation.gif" width="444" alt="티켓팅1" /> <br>

4. 마이 티켓

    <img src="./etc/gif/my_ticket_pc.png" width="80%" alt="티켓팅2" /> <br>

5. 오픈 티켓

    <img src="./etc/gif/open_event_pc.png" width="80%" alt="티켓팅3" /> <br>

6. 장르별 랭킹

    <img src="./etc/gif/장르별%20랭킹_pc.png" width="80%" alt="티켓팅4" /> <br>

7. 검색 (자동 완성)

    <img src="./etc/gif/뮤지컬%20자동완성_pc.png" width="80%" alt="티켓팅5" /> <br>

8. 검색 결과

    <img src="./etc/gif/뮤지컬_검색결과_pc.png" width="80%" alt="티켓팅6" /> <br>

9. 관리자 로그인

    <img src="./etc/gif/adminLogin.gif" width="80%" alt="관리자 로그인" /> <br>

10. 공연 등록 (관리자)

    <img src="./etc/gif/registerEvent1.gif" width="80%" /> <br>
    <img src="./etc/gif/registerEvent2.gif" width="80%" /> <br>

11. 임시 URL 발급 (관리자)

    <img src="./etc/gif/ticketCheckUrl.gif" width="80%" alt="관리자 로그인"/> <br>

12. 티켓 검사 (관리자 & 고객)

    <table>
      <td>
        <img src="./etc/gif/managerCheck.gif"  width="240" alt="관리자 로그인"/> 
      </td>
      <td>
        <img src="./etc/gif/ticketCheckCustomer.gif" width="240" alt="고객체크"/>
      </td>
    </table>


---

## 10. 서비스 화면 (MOBILE) 📱

<table style="border: 2px; text-align:center;">
  <tr style="text-align:center;">
    <td> 로그인 </td>
    <td> 메인 페이지 </td>
    <td> 공연 상세 </td>
  </tr>
  <tr>
    <td>
      <img src="etc/gif/login_mobile.gif" alt="로그인_모바일" width="200px" />
    </td>
    <td>
      <img src="./etc/gif/main_mobile.gif" alt="메인_화면_모바일" width="200px" />
    </td>
    <td>
      <img src="./etc/gif/eventDtail_mobile.gif" alt="메인_화면_모바일" width="200px" />
    </td>
  </tr>
</table>

<br>

<table style="border: 2px; text-align:center;">
  <tr style="text-align:center;">
    <td> 티켓팅 </td>
    <td> 장르별 랭킹 </td>
    <td> 오픈 티켓 </td>
  </tr>
  <tr>
    <td>
      <img src="./etc/gif/ticketing_mobile.gif" alt="장르별_랭킹_모바일1" width="200px" />
    </td>
    <td>
      <img src="./etc/gif/gerneRank_mobile.gif" alt="장르별_랭킹_모바일3" width="200px" />
    </td>
    <td>
      <img src="./etc/gif/오픈티켓_mobile.png" alt="장르별_랭킹_모바일2" width="200px" />
    </td>
  </tr>
</table>

---

## 11. 주요 기술 ✨

- 프론트
    - [zustand를 활용한 로그인 정보 유지](https://tutic982.tistory.com/3)
    - [반응형 처리하기](https://velog.io/@zvyg1023/React-%EB%B0%98%EC%9D%91%ED%98%95-%EC%B2%98%EB%A6%AC%ED%95%98%EA%B8%B0-w.-react-responsive)
- Docker
    - [도커 spring 서버 올리기](https://tutic982.tistory.com/2)
- Jenkins
    - [젠킨스 파이프라인 만들기](https://tutic982.tistory.com/5)
- MSA
    - [Eureka Server & Eureka Client & Gateway 설정](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-1-Eureka-Server-Eureka-Client-Gateway-%EC%84%A4%EC%A0%95)
    - [인증/인가 설정](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-2-%EC%9D%B8%EC%A6%9D%EC%9D%B8%EA%B0%80-%EC%84%A4%EC%A0%95)
    - [Config 서버 구축](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-3-Config-%EC%84%9C%EB%B2%84-%EA%B5%AC%EC%B6%95)
    - [Spring Cloud Bus와 RabbitMQ를 활용한 Config 변경 자동 반영 설정](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-4-Spring-Cloud-Bus%EC%99%80-RabbitMQ%EB%A5%BC-%ED%99%9C%EC%9A%A9%ED%95%9C-Config-%EB%B3%80%EA%B2%BD-%EC%9E%90%EB%8F%99-%EB%B0%98%EC%98%81-%EC%84%A4%EC%A0%95)
    - [설정 파일의 암호화 처리 : 비대칭키를 이용한 암/복호화](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-5-%EC%84%A4%EC%A0%95-%ED%8C%8C%EC%9D%BC%EC%9D%98-%EC%95%94%ED%98%B8%ED%99%94-%EC%B2%98%EB%A6%AC-%EB%B9%84%EB%8C%80%EC%B9%AD%ED%82%A4%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EC%95%94%EB%B3%B5%ED%98%B8%ED%99%94)
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
- 정산
    - [spring batch를 이용한 정산](https://tutic982.tistory.com/4)
- 티켓 검사
    - [face-api.js를 이용한 티켓검사](https://tutic982.tistory.com/6)
- 검색
    - [Elasticsearch Indexing 설계](https://velog.io/@zvyg1023/%EC%97%98%EB%9D%BC%EC%8A%A4%ED%8B%B1-%EC%84%9C%EC%B9%98-%EA%B2%80%EC%83%89-%EA%B5%AC%ED%98%84-1-%EC%83%89%EC%9D%B8-%EC%8B%9C%EC%8A%A4%ED%85%9C-%EC%84%A4%EA%B3%84)
    - [Elasticsearch 8.13.4, Kibana 설치 및 S3 연결 (docker-compose)](https://velog.io/@zvyg1023/%EC%97%98%EB%9D%BC%EC%8A%A4%ED%8B%B1-%EC%84%9C%EC%B9%98-%EA%B2%80%EC%83%89-%EA%B5%AC%ED%98%84-2-Elasticsearch-8.13.4-Kibana-%EC%84%A4%EC%B9%98-%EB%B0%8F-S3-%EC%97%B0%EA%B2%B0-docker-compose)
    - [전체 색인 구현 (Event)](https://velog.io/@zvyg1023/%EC%97%98%EB%9D%BC%EC%8A%A4%ED%8B%B1-%EC%84%9C%EC%B9%98-%EA%B2%80%EC%83%89-%EA%B5%AC%ED%98%84-3-%EC%A0%84%EC%B2%B4-%EC%83%89%EC%9D%B8-%EA%B5%AC%ED%98%84-Search-%EC%84%9C%EB%B2%84)
    - [전체 색인 구현 (Search)](https://velog.io/@zvyg1023/%EC%97%98%EB%9D%BC%EC%8A%A4%ED%8B%B1-%EC%84%9C%EC%B9%98-%EA%B2%80%EC%83%89-%EA%B5%AC%ED%98%84-4-%EC%A0%84%EC%B2%B4-%EC%83%89%EC%9D%B8-%EA%B5%AC%ED%98%84-Search-%EC%84%9C%EB%B2%84)
    - [부분 색인 구현](https://velog.io/@zvyg1023/%EC%97%98%EB%9D%BC%EC%8A%A4%ED%8B%B1-%EC%84%9C%EC%B9%98-%EA%B2%80%EC%83%89-%EA%B5%AC%ED%98%84-5-%EB%B6%80%EB%B6%84-%EC%83%89%EC%9D%B8-%EA%B5%AC%ED%98%84)
    - [검색 기능 구현](https://velog.io/@zvyg1023/%EC%97%98%EB%9D%BC%EC%8A%A4%ED%8B%B1-%EC%84%9C%EC%B9%98-%EA%B2%80%EC%83%89-%EA%B5%AC%ED%98%84-6-%EA%B2%80%EC%83%89-%EA%B8%B0%EB%8A%A5-%EA%B5%AC%ED%98%84)
    - [전체/부분 색인 구조 개선](https://velog.io/@zvyg1023/Ficket-Elasticsearch-%EC%83%89%EC%9D%B8-%EA%B5%AC%EC%A1%B0-%EA%B0%9C%EC%84%A0)
- 기타
    - [AWS Lambda를 이용해 이미지 리사이징 적용 - 이미지 로딩 속도 최적화](https://velog.io/@zvyg1023/AWS-Lambda%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%B4-%EC%9D%B4%EB%AF%B8%EC%A7%80-%EB%A6%AC%EC%82%AC%EC%9D%B4%EC%A7%95-%EC%A0%81%EC%9A%A9-%EC%9D%B4%EB%AF%B8%EC%A7%80-%EB%A1%9C%EB%94%A9-%EC%86%8D%EB%8F%84-%EC%B5%9C%EC%A0%81%ED%99%94)
    - [Redis(Redisson) 분산락을 활용한 좌석 선점](https://velog.io/@zvyg1023/Spring-RedisRedisson-%EB%B6%84%EC%82%B0%EB%9D%BD%EC%9D%84-%ED%99%9C%EC%9A%A9%ED%95%9C-%EC%A2%8C%EC%84%9D-%EC%84%A0%EC%A0%90-%EA%B0%9C%EB%B0%9C)
    - [Redis로 조회수 랭킹 시스템 구현](https://velog.io/@zvyg1023/Spring-Redis%EB%A1%9C-%EC%A1%B0%ED%9A%8C%EC%88%98-%EB%9E%AD%ED%82%B9-%EC%8B%9C%EC%8A%A4%ED%85%9C-%EA%B5%AC%ED%98%84%ED%95%98%EA%B8%B0)
    - [Redis 예매율 순위 시스템 구현](https://velog.io/@zvyg1023/Spring-Redis-%EC%98%88%EB%A7%A4%EC%9C%A8-%EC%88%9C%EC%9C%84-%EC%8B%9C%EC%8A%A4%ED%85%9C-%EA%B5%AC%ED%98%84)
    - [포트원 연동을 통한 결제 시스템](https://velog.io/@zvyg1023/%ED%8F%AC%ED%8A%B8%EC%9B%90%EC%9D%84-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EC%B9%B4%EC%B9%B4%EC%98%A4%ED%8E%98%EC%9D%B4-%EA%B2%B0%EC%A0%9C-%EC%97%B0%EB%8F%99-API-V2-Webhook-V2)

--- 

## 12. Trouble Shooting 🚧

- [Config 서버 설정 값 노출](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-6-%EC%95%94%ED%98%B8%ED%99%94%EB%90%9C-%EC%84%A4%EC%A0%95-%ED%8C%8C%EC%9D%BC-%EB%B3%B4%ED%98%B8%EC%99%80-decrypt-API-%EC%A0%91%EA%B7%BC-%EC%A0%9C%EC%96%B4)

- [Config 서버 복호화 실패](https://velog.io/@zvyg1023/MSA-%EA%B5%AC%EC%B6%95-7-Config-%EC%84%9C%EB%B2%84%EC%99%80-%ED%81%B4%EB%9D%BC%EC%9D%B4%EC%96%B8%ED%8A%B8%EC%97%90%EC%84%9C-%EA%B3%B5%EA%B0%9C%ED%82%A4%EC%99%80-%EA%B0%9C%EC%9D%B8%ED%82%A4%EB%A5%BC-%ED%99%9C%EC%9A%A9%ED%95%9C-%EC%95%94%ED%98%B8%ED%99%94%EB%B3%B5%ED%98%B8%ED%99%94-%EC%84%A4%EC%A0%95)

- [배포 시 Java가 ElasticSearch Self-signed SSL 인식 하지 못하는 문제](https://velog.io/@zvyg1023/Docker-%EB%B0%B0%ED%8F%AC-%EC%8B%9C-Elasticsearch-SSL-%EC%9D%B8%EC%A6%9D-%EB%AC%B8%EC%A0%9C-%ED%95%B4%EA%B2%B0)

- [XSS 방어: OWASP AntiSamy와 DOMPurify를 활용한 TinyMCE 보안 강화](https://velog.io/@zvyg1023/XSS-%EB%B0%A9%EC%96%B4-OWASP-AntiSamy%EC%99%80-DOMPurify%EB%A5%BC-%ED%99%9C%EC%9A%A9%ED%95%9C-TinyMCE-%EB%B3%B4%EC%95%88-%EA%B0%95%ED%99%94)

- [관리자 행사 목록 조회 API 성능 최적화 및 실행 계획 분석](https://velog.io/@zvyg1023/%EA%B4%80%EB%A6%AC%EC%9E%90-%ED%96%89%EC%82%AC-%EB%AA%A9%EB%A1%9D-%EC%A1%B0%ED%9A%8C-API-%EC%84%B1%EB%8A%A5-%EC%B5%9C%EC%A0%81%ED%99%94-%EB%B0%8F-%EC%8B%A4%ED%96%89-%EA%B3%84%ED%9A%8D-%EB%B6%84%EC%84%9D)

---

## 13. 외부 리소스 정보 📁

[포트원 개발자 센터](https://developers.portone.io/opi/ko/readme?v=v2)
<br>
[카카오 디벨로퍼](https://developers.kakao.com/)
