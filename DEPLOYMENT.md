# 배포 가이드

## 📋 사전 준비

### 1. Supabase PostgreSQL 설정

1. **Supabase 가입 및 프로젝트 생성**
   - https://supabase.com 접속
   - 새 프로젝트 생성 (리전: Seoul 또는 Singapore 권장)
   - 데이터베이스 비밀번호 설정 (안전한 비밀번호 사용)

2. **테이블 생성**
   - Supabase 대시보드 → SQL Editor
   - 아래 DDL 스크립트 실행:

```sql
-- 사용자 테이블
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- 축의금 테이블
CREATE TABLE gift_money (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_date DATE NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    giver_name VARCHAR(100) NOT NULL,
    giver_relation VARCHAR(50),
    amount DECIMAL(10, 0) NOT NULL,
    contact VARCHAR(50),
    memo TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_gift_money_user_id ON gift_money(user_id);
CREATE INDEX idx_gift_money_event_date ON gift_money(event_date);
CREATE INDEX idx_gift_money_giver_name ON gift_money(giver_name);
```

3. **연결 정보 확인**
   - Supabase 대시보드 → Settings → Database
   - Connection String 복사 (Direct Connection)
   - 형식: `postgresql://postgres:[YOUR-PASSWORD]@db.[PROJECT-REF].supabase.co:5432/postgres`

---

## 🚀 Render 배포

### 1. GitHub 레포지토리 생성

```bash
# Git 초기화 및 커밋
git init
git add .
git commit -m "Initial commit: Gift Money Management Service"

# GitHub 원격 레포지토리 생성 후
git remote add origin https://github.com/YOUR_USERNAME/gift-money-service.git
git branch -M main
git push -u origin main
```

### 2. Render 배포 설정

1. **Render 가입**
   - https://render.com 접속
   - GitHub 계정으로 로그인

2. **New Web Service 생성**
   - Dashboard → New → Web Service
   - GitHub 레포지토리 연결 (gift-money-service)

3. **설정 입력**
   ```
   Name: gift-money-service
   Environment: Java
   Build Command: ./mvnw clean package -DskipTests
   Start Command: java -jar target/gift-money-0.0.1-SNAPSHOT.jar
   Instance Type: Free
   ```

4. **환경변수 설정**
   - Environment 탭 → Add Environment Variable

   **필수 환경변수:**
   ```
   DB_PASSWORD=your_supabase_password
   JWT_SECRET=your-super-secret-key-min-256-bits-long-must-be-secure
   SPRING_PROFILES_ACTIVE=prod
   DATABASE_URL=jdbc:postgresql://db.[PROJECT-REF].supabase.co:5432/postgres
   DB_USERNAME=postgres
   ```

   **JWT_SECRET 생성 방법:**
   ```bash
   # 랜덤 256비트 키 생성
   openssl rand -base64 32
   ```

5. **Deploy 클릭**
   - 자동 빌드 및 배포 시작
   - 약 5-10분 소요
   - 배포 완료 후 URL 확인: `https://gift-money-service.onrender.com`

---

## ✅ 배포 확인

### 1. Health Check

```bash
curl https://gift-money-service.onrender.com/api/auth/login
```

Expected Response (401 Unauthorized):
```json
{
  "timestamp": "2025-10-12T...",
  "message": "..."
}
```

### 2. 회원가입 테스트

```bash
curl -X POST https://gift-money-service.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "name": "테스트사용자"
  }'
```

Expected Response (201 Created):
```json
{
  "id": 1,
  "email": "test@example.com",
  "name": "테스트사용자",
  "createdAt": "2025-10-12T..."
}
```

### 3. 로그인 테스트

```bash
curl -X POST https://gift-money-service.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

Expected Response (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "test@example.com",
    "name": "테스트사용자"
  }
}
```

---

## 🌐 도메인 연결 (선택사항)

### 무료 옵션

**1. Render 기본 도메인 사용**
- URL: `https://gift-money-service.onrender.com`
- 추가 설정 불필요

**2. 커스텀 도메인 (유료)**
1. 도메인 구매 (Namecheap, GoDaddy 등)
2. Render Dashboard → Settings → Custom Domain
3. 도메인 추가 및 DNS 설정:
   ```
   Type: CNAME
   Name: @ (or www)
   Value: gift-money-service.onrender.com
   ```
4. SSL 인증서 자동 발급 (Let's Encrypt)

---

## 🔄 자동 배포 설정

### GitHub Actions CI/CD (선택사항)

`.github/workflows/deploy.yml`:
```yaml
name: Deploy to Render

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build with Maven
        run: ./mvnw clean package -DskipTests

      - name: Deploy to Render
        env:
          RENDER_API_KEY: ${{ secrets.RENDER_API_KEY }}
        run: |
          curl -X POST \
            "https://api.render.com/deploy/srv-YOUR_SERVICE_ID?key=$RENDER_API_KEY"
```

---

## 📊 모니터링

### Render Dashboard
- Logs 탭: 실시간 로그 확인
- Metrics 탭: CPU, 메모리 사용량
- Events 탭: 배포 이력

### Supabase Dashboard
- Database → Logs: 쿼리 로그
- Database → Table Editor: 데이터 확인

---

## ⚠️ 주의사항

### 무료 플랜 제한사항

**Render Free Plan:**
- 15분 비활성 시 슬립 모드 (첫 요청 시 재시작 시간 소요)
- 750시간/월 무료 사용
- 512MB RAM

**Supabase Free Plan:**
- 500MB 데이터베이스 용량
- 무제한 API 요청
- 자동 백업 (7일)

### 해결 방법
- 슬립 방지: 외부 모니터링 서비스 (UptimeRobot, Pingdom) 사용
- 용량 초과 시: 유료 플랜 업그레이드 또는 데이터 정리

---

## 🐛 트러블슈팅

### 1. 빌드 실패
```bash
# 로컬에서 빌드 테스트
./mvnw clean package -DskipTests
```

### 2. 데이터베이스 연결 실패
- Supabase 비밀번호 확인
- DATABASE_URL 형식 확인
- Supabase 방화벽 설정 확인 (All IPs 허용)

### 3. JWT 토큰 오류
- JWT_SECRET 길이 확인 (최소 256비트)
- 환경변수 설정 확인

### 4. CORS 오류 (프론트엔드 연결 시)
- `SecurityConfig.java`에서 프론트엔드 도메인 추가
- `configuration.setAllowedOrigins()` 수정

---

## 📚 참고 자료

- Render 공식 문서: https://render.com/docs
- Supabase 공식 문서: https://supabase.com/docs
- Spring Boot 배포 가이드: https://spring.io/guides
