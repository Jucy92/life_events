# 경조금 관리 서비스 (Gift Money Management Service)

온라인에서 경조사 경조금을 관리할 수 있는 웹 애플리케이션입니다.

## 🚀 주요 기능

### 기본 기능
- ✅ 회원가입 / 로그인 (JWT 인증)
- ✅ 경조금 CRUD (생성, 조회, 수정, 삭제)
- ✅ 페이징 및 검색
- ✅ Excel/CSV 대량 업로드
- ✅ 사용자별 데이터 격리 (멀티테넌트)

### 추가 기능 (예정)
- 📊 통계 대시보드
- 📅 날짜 범위 필터링
- 📥 Excel/CSV 내보내기
- 📝 감사 로그 (수정 이력 추적)

## 🛠️ 기술 스택

### Backend
- Java 17
- Spring Boot 3.2.0
- Spring Security + JWT
- Spring Data JPA
- H2 Database (개발용)
- PostgreSQL (운영용)
- Apache POI (Excel 처리)

### Frontend (예정)
- React
- Axios
- Bootstrap / Tailwind CSS

### 배포
- Render (백엔드)
- Supabase (PostgreSQL DB)
- Vercel / Netlify (프론트엔드)

## 📦 프로젝트 구조

```
src/main/java/com/example/giftmoney/
├── GiftMoneyApplication.java          # Main Application
├── domain/entity/
│   ├── User.java                      # 사용자 엔티티
│   └── GiftMoney.java                 # 축의금 엔티티
├── repository/
│   ├── UserRepository.java
│   └── GiftMoneyRepository.java
├── service/
│   ├── AuthService.java               # 인증 서비스
│   └── GiftMoneyService.java          # 경조금 서비스
├── controller/
│   ├── AuthController.java            # 인증 API
│   └── GiftMoneyController.java       # 경조금 API
├── dto/
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── UserResponse.java
│   ├── GiftMoneyRequest.java
│   └── GiftMoneyResponse.java
├── security/
│   ├── JwtTokenProvider.java          # JWT 토큰 생성/검증
│   ├── JwtAuthenticationFilter.java   # JWT 필터
│   └── SecurityConfig.java            # Security 설정
└── exception/
    ├── ErrorResponse.java
    └── GlobalExceptionHandler.java    # 예외 처리
```

## 🏃 로컬 실행 방법

### 1. 프로젝트 클론
```bash
git clone <repository-url>
cd life_events
```

### 2. 환경변수 설정
`.env.example` 파일을 `.env`로 복사하고 실제 값으로 수정:

```bash
cp .env.example .env
```

`.env` 파일 내용:
```bash
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/gift_money_db
DB_USERNAME=postgres
DB_PASSWORD=your_password_here

# JWT Configuration (최소 256비트 이상 랜덤 문자열 생성 필수)
# 생성: openssl rand -base64 64
JWT_SECRET=your-strong-random-secret-key-here
JWT_EXPIRATION=86400000

# CORS (쉼표로 구분)
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080

# Server Port
PORT=8080
```

**🔒 보안 주의사항:**
- `.env` 파일은 절대 Git에 커밋하지 마세요 (이미 .gitignore에 포함됨)
- JWT_SECRET은 반드시 강력한 랜덤 문자열로 변경하세요
- 프로덕션에서는 더 강력한 비밀번호 사용 필수

### 3. 애플리케이션 실행

**개발 환경 (기본):**
```bash
./mvnw spring-boot:run
```

Windows의 경우:
```bash
mvnw.cmd spring-boot:run
```

**프로덕션 환경:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

또는:
```bash
java -jar target/gift-money-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 4. H2 Console 접속 (개발 환경만)
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (비어있음)

### 5. API 테스트

**회원가입:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "name": "홍길동"
  }'
```

**로그인:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**경조금 생성 (JWT 토큰 필요):**
```bash
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

## 🌐 배포 가이드

### Supabase PostgreSQL 설정
1. https://supabase.com 가입
2. 새 프로젝트 생성
3. SQL Editor에서 테이블 생성 (DDL 실행)
4. 연결 정보 복사

### Render 배포
1. https://render.com 가입
2. New Web Service → GitHub 연동
3. 환경변수 설정:
   ```
   DB_PASSWORD=your_supabase_password
   JWT_SECRET=your_super_secret_key_min_256_bits
   ```
4. Deploy 클릭

### application.yml 수정 (운영용)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://db.PROJECT_ID.supabase.co:5432/postgres
    username: postgres
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate  # 운영에서는 validate 사용
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

## 📚 API 문서

### 인증 API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | 회원가입 |
| POST | `/api/auth/login` | 로그인 |
| GET | `/api/auth/me` | 현재 사용자 정보 |

### 경조금 API (JWT 필수)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/gift-money` | 목록 조회 (페이징, 검색) |
| GET | `/api/gift-money/{id}` | 단일 항목 조회 |
| POST | `/api/gift-money` | 새 항목 생성 |
| PUT | `/api/gift-money/{id}` | 항목 수정 |
| DELETE | `/api/gift-money/{id}` | 항목 삭제 |
| POST | `/api/gift-money/upload` | Excel/CSV 대량 업로드 (예정) |
| GET | `/api/gift-money/statistics` | 통계 조회 (예정) |

## 🔒 보안

### 구현된 보안 기능
- ✅ **비밀번호 암호화**: BCrypt (Spring Security)
- ✅ **인증**: JWT 기반 Stateless 인증
- ✅ **CORS**: 환경변수로 허용 도메인 관리
- ✅ **데이터 격리**: 사용자별 데이터 완전 분리 (멀티테넌트)
- ✅ **환경변수**: 민감한 정보는 모두 환경변수로 관리
- ✅ **SQL 인젝션 방지**: JPA PreparedStatement 사용
- ✅ **프로덕션 로깅**: 민감한 정보 로깅 비활성화

### 보안 권장사항
1. **JWT Secret 생성**: `openssl rand -base64 64`로 강력한 랜덤 키 생성
2. **데이터베이스 비밀번호**: 최소 16자 이상의 복잡한 비밀번호 사용
3. **CORS**: 프로덕션에서는 실제 도메인만 허용
4. **HTTPS**: 프로덕션 배포 시 반드시 HTTPS 사용
5. **환경변수**: .env 파일을 절대 Git에 커밋하지 말 것

### 성능 최적화
- ✅ **N+1 쿼리 해결**: JOIN FETCH 사용
- ✅ **통계 쿼리 최적화**: 6개 쿼리 → 1개 쿼리로 통합
- ✅ **인덱스**: 자주 조회되는 컬럼에 인덱스 적용
- ✅ **커넥션 풀**: 프로덕션 환경용 최적화 설정

## 📝 라이선스

MIT License

## 👥 기여자

- Your Name

## 📞 문의

이슈나 질문이 있으시면 GitHub Issues를 통해 문의해주세요.
