# 🚀 Render + Supabase 배포 가이드

## 📋 전체 흐름

```
1. Supabase에서 PostgreSQL DB 생성
2. application.yml 설정 변경
3. 로컬에서 테스트
4. GitHub에 코드 업로드
5. Render에서 웹 서비스 생성
6. 환경 변수 설정
7. 배포 완료!
```

---

## 1️⃣ Supabase PostgreSQL DB 생성

### 1-1. Supabase 회원가입
1. https://supabase.com 접속
2. "Start your project" 클릭
3. GitHub 계정으로 로그인 (추천) 또는 이메일로 가입

### 1-2. 새 프로젝트 생성
1. "New Project" 클릭
2. 프로젝트 정보 입력:
   - **Name**: `gift-money-db` (원하는 이름)
   - **Database Password**: 강력한 비밀번호 설정 (나중에 필요함! 메모!)
   - **Region**: `Northeast Asia (Seoul)` 선택 (한국과 가장 가까움)
3. "Create new project" 클릭
4. 약 2분 기다림 (DB 생성 중...)

### 1-3. 연결 정보 확인
1. 프로젝트 대시보드에서 **Settings** (왼쪽 하단 톱니바퀴) 클릭
2. **Database** 클릭
3. **Connection string** 섹션에서 **URI** 탭 선택
4. 다음 형식의 URL이 보임:
   ```
   postgresql://postgres:[YOUR-PASSWORD]@db.xxxxxxxxxxxx.supabase.co:5432/postgres
   ```
5. 이 정보를 메모장에 복사 (나중에 사용)

### 1-4. 연결 정보 구성 요소
```
Host: db.xxxxxxxxxxxx.supabase.co
Port: 5432
Database: postgres
User: postgres
Password: (설정한 비밀번호)
```

---

## 2️⃣ application.yml 설정 변경

### 2-1. application.yml 파일 수정

**현재 H2 설정을 주석 처리하고, PostgreSQL 설정을 활성화합니다.**

```yaml
spring:
  application:
    name: gift-money-service

  # H2 Database (로컬 개발용) - 주석 처리
  # datasource:
  #   url: jdbc:h2:mem:testdb
  #   driver-class-name: org.h2.Driver
  #   username: sa
  #   password:

  # PostgreSQL (Supabase - 운영용)
  datasource:
    url: jdbc:postgresql://db.xxxxxxxxxxxx.supabase.co:5432/postgres
    username: postgres
    password: ${DB_PASSWORD:your-database-password-here}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

  # H2 Console 비활성화
  # h2:
  #   console:
  #     enabled: false

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

# JWT Configuration
jwt:
  secret: ${JWT_SECRET:your-super-secret-key-min-256-bits-long-for-development-only}
  expiration: 86400000  # 24시간 (밀리초)

# Logging
logging:
  level:
    com.example.giftmoney: INFO
    org.hibernate.SQL: DEBUG

# Server
server:
  port: ${PORT:8080}
```

**중요:**
- `url`의 `db.xxxxxxxxxxxx.supabase.co` 부분을 Supabase에서 받은 실제 호스트로 변경
- `password`는 로컬 테스트용으로 직접 입력하거나, 환경 변수 `DB_PASSWORD` 사용
- `${PORT:8080}`: Render는 자동으로 PORT 환경 변수를 제공함

---

## 3️⃣ 로컬에서 PostgreSQL 연결 테스트

### 3-1. application.yml에 비밀번호 임시 입력
```yaml
datasource:
  password: your-actual-password-here  # 임시로 직접 입력
```

### 3-2. 애플리케이션 실행
```bash
./mvnw spring-boot:run
```

### 3-3. 로그 확인
```
Hibernate: create table users ...
Hibernate: create table gift_money ...
```
이런 로그가 보이면 PostgreSQL 연결 성공! ✅

### 3-4. 브라우저 테스트
```
http://localhost:8080
→ 회원가입 → 로그인 → 축의금 추가 테스트
```

### 3-5. Supabase에서 데이터 확인
1. Supabase 대시보드 → **Table Editor** 클릭
2. `users`, `gift_money` 테이블이 생성되었는지 확인
3. 데이터가 잘 들어갔는지 확인

**✅ 로컬 테스트 성공하면 다음 단계로!**

---

## 4️⃣ GitHub 저장소 생성 및 코드 업로드

### 4-1. .gitignore 파일 확인
프로젝트에 이미 `.gitignore`가 있는지 확인하고, 없으면 생성:

```gitignore
# Maven
target/
!.mvn/wrapper/maven-wrapper.jar

# IDE
.idea/
*.iml
.vscode/
*.swp
*.swo

# OS
.DS_Store
Thumbs.db

# Logs
*.log

# 환경 변수 (중요! 절대 커밋하면 안 됨)
application-local.yml
```

### 4-2. application.yml에서 비밀번호 제거
**보안을 위해 비밀번호를 환경 변수로 변경:**

```yaml
datasource:
  password: ${DB_PASSWORD}  # 환경 변수 사용 (직접 입력한 비밀번호 삭제!)
```

### 4-3. GitHub 저장소 생성
1. https://github.com 접속
2. "+" → "New repository" 클릭
3. Repository 정보:
   - **Name**: `gift-money-service` (원하는 이름)
   - **Visibility**: Public (무료) 또는 Private (개인 프로젝트)
4. "Create repository" 클릭

### 4-4. 로컬에서 Git 초기화 및 푸시
```bash
# Git 저장소 초기화
git init

# 파일 추가
git add .

# 첫 커밋
git commit -m "Initial commit: Gift Money Management Service"

# GitHub 원격 저장소 연결 (본인의 저장소 URL로 변경)
git remote add origin https://github.com/your-username/gift-money-service.git

# 푸시
git branch -M main
git push -u origin main
```

**✅ GitHub에 코드가 업로드되었는지 확인!**

---

## 5️⃣ Render 웹 서비스 생성

### 5-1. Render 회원가입
1. https://render.com 접속
2. "Get Started" 클릭
3. GitHub 계정으로 로그인 (추천)

### 5-2. 새 웹 서비스 생성
1. Dashboard → "New +" → "Web Service" 클릭
2. "Build and deploy from a Git repository" 선택
3. GitHub 저장소 연결:
   - "Connect account" 클릭 (처음인 경우)
   - 본인의 `gift-money-service` 저장소 선택
   - "Connect" 클릭

### 5-3. 서비스 설정
다음 정보를 입력:

| 항목 | 값 |
|------|-----|
| **Name** | `gift-money-service` (원하는 이름) |
| **Region** | `Singapore` (한국과 가장 가까움) |
| **Branch** | `main` |
| **Runtime** | `Docker` |
| **Instance Type** | `Free` |

**Build Command**: (비워둠 - Dockerfile 사용)
**Start Command**: (비워둠 - Dockerfile 사용)

### 5-4. 고급 설정 (Advanced)
"Advanced" 버튼 클릭 후:

#### Auto-Deploy 설정
- ✅ "Yes" 선택 (GitHub에 푸시하면 자동 배포)

---

## 6️⃣ Render 환경 변수 설정

### 6-1. 환경 변수 추가
"Environment" 섹션에서 "Add Environment Variable" 클릭하고 다음 추가:

| Key | Value | 설명 |
|-----|-------|------|
| `DB_PASSWORD` | (Supabase DB 비밀번호) | PostgreSQL 비밀번호 |
| `JWT_SECRET` | (최소 32자 랜덤 문자열) | JWT 서명 키 |
| `SPRING_PROFILES_ACTIVE` | `prod` | 운영 환경 프로파일 |

**JWT_SECRET 생성 예시:**
```
your-super-secret-jwt-key-12345678901234567890123456789012
```
(온라인 랜덤 문자열 생성기 사용 추천)

### 6-2. 서비스 생성
"Create Web Service" 클릭

---

## 7️⃣ 배포 및 확인

### 7-1. 배포 진행
Render가 자동으로:
1. GitHub에서 코드 가져오기
2. Docker 이미지 빌드
3. 애플리케이션 실행

약 5~10분 소요 (처음에는 더 오래 걸릴 수 있음)

### 7-2. 배포 로그 확인
"Logs" 탭에서 실시간 로그 확인:
```
Starting application...
Hibernate: create table ...
Started GiftMoneyApplication in X seconds
```

### 7-3. URL 확인
배포 완료 후 Render가 제공하는 URL:
```
https://gift-money-service-xxxx.onrender.com
```

### 7-4. 브라우저에서 테스트
```
https://gift-money-service-xxxx.onrender.com
→ 회원가입 → 로그인 → 축의금 추가
```

**✅ 모든 기능이 작동하면 배포 성공!** 🎉

---

## 8️⃣ 추가 설정 (선택사항)

### 8-1. 커스텀 도메인 연결 (무료)
Render는 무료로 커스텀 도메인 지원:
1. Render Dashboard → 본인 서비스 → "Settings"
2. "Custom Domain" 섹션
3. 본인의 도메인 입력 (예: giftmoney.com)
4. DNS 설정 안내에 따라 설정

### 8-2. 슬립 모드 방지 (무료)
**UptimeRobot** 사용:
1. https://uptimerobot.com 가입
2. "Add New Monitor" 클릭
3. Monitor Type: HTTP(s)
4. URL: `https://gift-money-service-xxxx.onrender.com`
5. Monitoring Interval: 5분
6. 저장

→ 5분마다 자동 접속하여 슬립 모드 방지!

### 8-3. HTTPS 강제 (이미 기본 적용됨)
Render는 자동으로 무료 SSL 인증서 제공 ✅

---

## 🔧 문제 해결

### 문제 1: 배포 실패
**증상**: Build Failed 또는 Deploy Failed
**해결**:
1. Render Logs 확인
2. `./mvnw clean package` 로컬에서 빌드 테스트
3. Dockerfile 확인

### 문제 2: 데이터베이스 연결 오류
**증상**: "Connection refused" 또는 "Authentication failed"
**해결**:
1. Supabase 연결 정보 재확인
2. `DB_PASSWORD` 환경 변수 확인
3. Supabase 방화벽 설정 확인 (기본적으로 모든 IP 허용)

### 문제 3: 404 Not Found
**증상**: 메인 페이지 접속 안 됨
**해결**:
1. `server.port=${PORT:8080}` 확인
2. Render Logs에서 "Started" 확인
3. Static 리소스 경로 확인 (`/js`, `/css`)

### 문제 4: 슬립 모드 후 느림
**증상**: 15분 후 첫 접속이 매우 느림
**해결**:
- UptimeRobot 설정 (위 8-2 참고)
- 또는 유료 플랜 ($7/월)

---

## 📚 참고 자료

- Supabase 공식 문서: https://supabase.com/docs
- Render 공식 문서: https://render.com/docs
- Spring Boot + PostgreSQL: https://spring.io/guides/gs/accessing-data-jpa/

---

## 🎉 완료!

이제 전 세계 누구나 접속 가능한 축의금 관리 서비스가 완성되었습니다!

```
https://gift-money-service-xxxx.onrender.com
```

친구, 가족과 공유하세요! 🎊
