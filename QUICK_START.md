# 🚀 Quick Start Guide

## 1️⃣ 로컬 개발 환경 실행 (3분)

### 필수 요구사항
- Java 17 이상
- 인터넷 연결 (Maven 의존성 다운로드)

### 실행 방법

**Windows:**
```bash
# 프로젝트 디렉토리로 이동
cd D:\life_events

# 애플리케이션 실행
mvnw.cmd spring-boot:run
```

**Mac/Linux:**
```bash
cd /path/to/life_events
./mvnw spring-boot:run
```

애플리케이션이 실행되면:
- API 서버: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console

---

## 2️⃣ API 테스트 (5분)

### Step 1: 회원가입

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "hong@example.com",
    "password": "password123",
    "name": "홍길동"
  }'
```

**Response:**
```json
{
  "id": 1,
  "email": "hong@example.com",
  "name": "홍길동",
  "createdAt": "2025-10-12T..."
}
```

### Step 2: 로그인

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "hong@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": { ... }
}
```

**토큰 복사:** Response에서 `token` 값을 복사하세요!

### Step 3: 축의금 데이터 추가

```bash
# 토큰을 YOUR_JWT_TOKEN 부분에 붙여넣기
curl -X POST http://localhost:8080/api/gift-money \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "eventDate": "2025-10-15",
    "eventType": "결혼식",
    "giverName": "김철수",
    "giverRelation": "친구",
    "amount": 100000,
    "contact": "010-1234-5678",
    "memo": "대학 동기"
  }'
```

### Step 4: 목록 조회

```bash
curl -X GET "http://localhost:8080/api/gift-money?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "content": [
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
  ],
  "totalElements": 1,
  "totalPages": 1,
  "page": 0,
  "size": 20
}
```

### Step 5: Excel 파일 업로드

1. **템플릿 파일 사용:** `templates/gift_money_template.csv` 파일 참고
2. **Excel 파일 생성:** 템플릿 형식으로 `.xlsx` 파일 생성
3. **업로드:**

```bash
curl -X POST http://localhost:8080/api/gift-money/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@path/to/your/file.xlsx"
```

**Response:**
```json
{
  "successCount": 3,
  "failCount": 0,
  "errors": []
}
```

---

## 3️⃣ H2 Database Console 접속

1. 브라우저에서 http://localhost:8080/h2-console 접속
2. 설정 입력:
   - **JDBC URL:** `jdbc:h2:mem:testdb`
   - **User Name:** `sa`
   - **Password:** (비워두기)
3. Connect 클릭
4. 데이터 확인:
   ```sql
   SELECT * FROM users;
   SELECT * FROM gift_money;
   ```

---

## 4️⃣ 운영 배포 (10분)

자세한 배포 가이드는 `DEPLOYMENT.md` 참고

**요약:**
1. GitHub에 코드 푸시
2. Supabase에서 PostgreSQL 생성
3. Render에서 웹 서비스 생성
4. 환경변수 설정 (DB_PASSWORD, JWT_SECRET)
5. 배포 완료!

---

## 📚 추가 리소스

- **API 문서:** `README.md` 참고
- **배포 가이드:** `DEPLOYMENT.md` 참고
- **Excel 템플릿:** `templates/gift_money_template.csv`

---

## 🐛 문제 해결

### 포트 8080이 이미 사용 중인 경우

`application.yml` 파일에서 포트 변경:
```yaml
server:
  port: 8081  # 원하는 포트 번호
```

### Maven Wrapper가 없는 경우

Maven을 직접 설치하거나:
```bash
mvn spring-boot:run
```

### JWT 토큰이 만료된 경우

다시 로그인하여 새 토큰 발급

---

## ✅ 체크리스트

- [ ] Java 17 설치 확인
- [ ] 애플리케이션 실행 성공
- [ ] 회원가입 테스트 완료
- [ ] 로그인 및 JWT 토큰 획득
- [ ] 축의금 CRUD 테스트 완료
- [ ] Excel 업로드 테스트 완료
- [ ] H2 Console 접속 성공

---

**다음 단계:** 프론트엔드 개발 또는 운영 배포를 진행하세요!
