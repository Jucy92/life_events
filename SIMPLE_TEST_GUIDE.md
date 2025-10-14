# 🚀 초간단 테스트 가이드 (5분 완성!)

## 📌 가장 쉬운 방법: IntelliJ IDEA

### 1단계: IntelliJ IDEA 설치 (3분)

1. **다운로드**
   - https://www.jetbrains.com/idea/download/
   - **Community Edition (무료)** 선택
   - Windows용 다운로드

2. **설치**
   - 다운로드한 파일 실행
   - 기본 설정으로 Next 계속 클릭
   - 완료!

---

### 2단계: 프로젝트 열기 (1분)

1. **IntelliJ IDEA 실행**

2. **Open 클릭**
   - `D:\life_events` 폴더 선택
   - Open

3. **Trust Project** 클릭

4. **Maven 자동 다운로드 대기** (5~10분)
   - 화면 오른쪽 아래 "Importing..." 진행 상황 확인
   - 인터넷 연결 필요!
   - 처음만 오래 걸림

---

### 3단계: 실행! (10초)

1. **GiftMoneyApplication.java 파일 열기**
   - 왼쪽 프로젝트 탭에서:
   ```
   src
   └── main
       └── java
           └── com.example.giftmoney
               └── GiftMoneyApplication.java
   ```

2. **Run 버튼 클릭!**
   - 화면 오른쪽 위 녹색 삼각형 ▶️ 버튼
   - 또는 파일에서 우클릭 → Run 'GiftMoneyApplication'

3. **성공!**
   - 콘솔에 다음 메시지 확인:
   ```
   Started GiftMoneyApplication in X.XXX seconds
   ```

---

### 4단계: 브라우저로 확인 (1분)

1. **브라우저 열기**
   ```
   http://localhost:8080/h2-console
   ```

2. **H2 Console 로그인**
   - JDBC URL: `jdbc:h2:mem:testdb`
   - User Name: `sa`
   - Password: (비워두기)
   - **Connect** 클릭

3. **테이블 확인**
   - 왼쪽에 `USERS`, `GIFT_MONEY` 테이블이 보이면 성공!

---

## 🧪 API 테스트 (Postman)

### 5단계: Postman 설치 (2분)

1. **다운로드**
   - https://www.postman.com/downloads/
   - Windows 64-bit 다운로드

2. **설치**
   - 기본 설정으로 설치
   - 계정 만들기 (선택사항)

---

### 6단계: API 테스트 (5분)

#### 📝 테스트 1: 회원가입

1. **New Request** 클릭
2. **설정:**
   ```
   Method: POST
   URL: http://localhost:8080/api/auth/register
   ```

3. **Headers 탭:**
   ```
   Key: Content-Type
   Value: application/json
   ```

4. **Body 탭:**
   - **raw** 선택
   - **JSON** 선택
   ```json
   {
     "email": "hong@example.com",
     "password": "password123",
     "name": "홍길동"
   }
   ```

5. **Send 클릭!**

6. **성공 Response (201 Created):**
   ```json
   {
     "id": 1,
     "email": "hong@example.com",
     "name": "홍길동",
     "createdAt": "2025-10-12T..."
   }
   ```

---

#### 🔐 테스트 2: 로그인

1. **New Request**
2. **설정:**
   ```
   Method: POST
   URL: http://localhost:8080/api/auth/login
   ```

3. **Body:**
   ```json
   {
     "email": "hong@example.com",
     "password": "password123"
   }
   ```

4. **Send**

5. **Response에서 token 복사!**
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJob25nQGV4YW1wbGUuY29tIiwidXNlcklkIjoxLCJpYXQiOjE3MDc3Mjg3MjAsImV4cCI6MTcwNzgxNTEyMH0.XYZ...",
     "user": {...}
   }
   ```

   **token 값 전체를 마우스로 드래그 → 복사 (Ctrl+C)**

---

#### 💰 테스트 3: 축의금 생성

1. **New Request**
2. **설정:**
   ```
   Method: POST
   URL: http://localhost:8080/api/gift-money
   ```

3. **Headers:**
   ```
   Key: Content-Type
   Value: application/json

   Key: Authorization
   Value: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```
   ☝️ **Bearer 다음에 공백 한 칸 + 복사한 토큰 붙여넣기!**

4. **Body:**
   ```json
   {
     "eventDate": "2025-10-15",
     "eventType": "결혼식",
     "giverName": "김철수",
     "giverRelation": "친구",
     "amount": 100000,
     "contact": "010-1234-5678",
     "memo": "대학 동기"
   }
   ```

5. **Send**

6. **성공 Response (201 Created):**
   ```json
   {
     "id": 1,
     "eventDate": "2025-10-15",
     "eventType": "결혼식",
     "giverName": "김철수",
     "giverRelation": "친구",
     "amount": 100000,
     "contact": "010-1234-5678",
     "memo": "대학 동기",
     "createdAt": "2025-10-12T...",
     "updatedAt": "2025-10-12T..."
   }
   ```

---

#### 📋 테스트 4: 목록 조회

1. **New Request**
2. **설정:**
   ```
   Method: GET
   URL: http://localhost:8080/api/gift-money
   ```

3. **Headers:**
   ```
   Key: Authorization
   Value: Bearer [복사한_토큰]
   ```

4. **Send**

5. **Response:**
   ```json
   {
     "content": [
       {
         "id": 1,
         "eventDate": "2025-10-15",
         "giverName": "김철수",
         "amount": 100000,
         ...
       }
     ],
     "totalElements": 1,
     "totalPages": 1,
     "page": 0,
     "size": 20
   }
   ```

---

## ✅ 테스트 완료 체크리스트

- [ ] IntelliJ IDEA 설치 완료
- [ ] 프로젝트 열기 성공
- [ ] 애플리케이션 실행 성공 (`Started GiftMoneyApplication...`)
- [ ] H2 Console 접속 성공
- [ ] Postman 설치 완료
- [ ] 회원가입 API 테스트 성공 (201 Created)
- [ ] 로그인 API 테스트 성공 (토큰 받음)
- [ ] 축의금 생성 API 테스트 성공 (201 Created)
- [ ] 목록 조회 API 테스트 성공

---

## ❌ 문제 해결

### 문제 1: "Port 8080 is already in use"
**해결:**
```
1. 다른 프로그램이 8080 포트 사용 중
2. IntelliJ에서 Stop 버튼 (빨간 네모) 클릭
3. 다시 Run
```

### 문제 2: "Cannot resolve symbol 'springframework'"
**해결:**
```
1. 오른쪽 위 "Load Maven Changes" 클릭
2. 또는 File → Invalidate Caches → Restart
```

### 문제 3: "401 Unauthorized" (토큰 에러)
**해결:**
```
1. 로그인 다시 해서 새 토큰 받기
2. Authorization 헤더에 "Bearer " + 토큰 확인
3. Bearer 다음 공백 확인!
```

### 문제 4: "이메일 또는 비밀번호가 잘못되었습니다"
**해결:**
```
1. 회원가입 먼저 해야 함!
2. 이메일, 비밀번호 오타 확인
```

---

## 🎉 축하합니다!

모든 테스트를 통과하셨다면 **축의금 관리 시스템이 정상 작동**하는 겁니다!

### 다음 단계 (선택)
- [ ] 프론트엔드 개발 (React)
- [ ] 클라우드 배포 (Supabase + Render)
- [ ] 추가 기능 구현 (통계, 필터링 등)

---

## 📞 도움이 필요하신가요?

에러 메시지나 궁금한 점이 있으시면 말씀해주세요!
