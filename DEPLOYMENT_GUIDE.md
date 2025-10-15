# 경조금 관리 시스템 배포 가이드

Spring Boot 애플리케이션을 Render 클라우드와 Supabase PostgreSQL로 배포하는 완전한 가이드입니다.

## 목차
1. [사전 준비사항](#사전-준비사항)
2. [Supabase 데이터베이스 설정](#supabase-데이터베이스-설정)
3. [Render 클라우드 배포](#render-클라우드-배포)
4. [배포 후 확인](#배포-후-확인)
5. [문제 해결](#문제-해결)

---

## 사전 준비사항

### 필수 계정
- GitHub 계정
- Supabase 계정 (https://supabase.com)
- Render 계정 (https://render.com)

### 로컬 개발 환경
- Java 17 이상
- Git
- 코드가 GitHub 저장소에 push되어 있어야 함

---

## Supabase 데이터베이스 설정

### 1. Supabase 프로젝트 생성

1. https://supabase.com 접속 및 로그인
2. "New Project" 클릭
3. 프로젝트 정보 입력:
   - **Name**: `life-events` (원하는 이름)
   - **Database Password**: 강력한 비밀번호 생성 (저장 필수!)
   - **Region**: `Northeast Asia (Seoul)` 선택 (가장 가까운 지역)
   - **Pricing Plan**: Free 선택

4. "Create new project" 클릭 (생성 완료까지 1-2분 소요)

### 2. 데이터베이스 연결 정보 확인

프로젝트 생성 완료 후:

1. 좌측 메뉴에서 **"Project Settings"** (톱니바퀴 아이콘) 클릭
2. **"Database"** 탭 클릭
3. **"Connection string"** 섹션에서 필요한 정보 확인:

#### Connection Pooling 정보 (권장)
```
Host: aws-0-ap-northeast-2.pooler.supabase.com
Port: 6543
Database: postgres
User: postgres.xxxxxxxxxxxxxxxxxxxxx
Password: [프로젝트 생성 시 입력한 비밀번호]
```

**중요**: Connection Pooling을 사용하는 이유
- 무료 플랜에서 동시 연결 수 제한 문제 해결
- 연결 효율성 향상
- **포트 6543 사용** (일반 연결은 5432)

#### URI 형식으로 저장
다음 형식으로 정리해두세요:
```
jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:6543/postgres
```

### 3. IPv6 연결 문제 해결 (필요시)

**증상**: `No route to host` 또는 연결 실패 에러

**원인**: Supabase는 IPv6를 우선 사용하지만, 일부 환경(Render 등)에서는 IPv6를 지원하지 않음

**해결방법**: 연결 문자열에 IPv4 강제 옵션 추가

```
jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:6543/postgres?ipv6=false
```

또는

```
jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:6543/postgres?preferQueryMode=simple&sslmode=require
```

### 4. 데이터베이스 테이블 자동 생성 확인

Spring Boot의 JPA 설정으로 테이블이 자동 생성됩니다:
- `application-prod.properties`에서 `spring.jpa.hibernate.ddl-auto=update` 설정 확인
- 첫 실행 시 `users`, `gift_money` 테이블 자동 생성

---

## Render 클라우드 배포

### 1. Render 계정 생성 및 GitHub 연동

1. https://render.com 접속
2. "Get Started" 또는 "Sign Up" 클릭
3. **"GitHub"로 가입 선택** (권장)
4. GitHub 인증 및 Render 접근 권한 승인

### 2. 새 Web Service 생성

1. Render 대시보드에서 **"New +"** 버튼 클릭
2. **"Web Service"** 선택
3. GitHub 저장소 연결:
   - "Connect a repository" 섹션에서 저장소 검색
   - `life_events` 저장소 선택
   - "Connect" 클릭

### 3. Web Service 설정

다음 정보를 입력:

#### 기본 설정
- **Name**: `life-events` (원하는 서비스 이름)
- **Region**: `Singapore (Southeast Asia)` (가장 가까운 지역)
- **Branch**: `main` (배포할 브랜치)
- **Root Directory**: 비워두기 (프로젝트 루트 사용)

#### 빌드 설정
- **Runtime**: **Docker** 선택
  - ⚠️ **중요**: Java 옵션이 보이지 않으면 Docker 선택
  - Dockerfile이 자동으로 감지됨

#### 인스턴스 타입
- **Instance Type**: **Free** 선택

### 4. 환경 변수 설정

"Environment Variables" 섹션에서 **"Add Environment Variable"** 클릭하여 다음 변수들을 추가:

#### 필수 환경 변수

| Key | Value | 설명 |
|-----|-------|------|
| `DB_URL` | `jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:6543/postgres` | Supabase 데이터베이스 URL |
| `DB_USERNAME` | `postgres.xxxxxxxxxxxxxxxxxxxxx` | Supabase 데이터베이스 사용자명 |
| `DB_PASSWORD` | `your_database_password` | Supabase 프로젝트 생성 시 설정한 비밀번호 |
| `JWT_SECRET` | `your-secret-key-min-32-characters-long` | JWT 토큰 암호화 키 (32자 이상) |
| `JWT_EXPIRATION` | `86400000` | JWT 토큰 만료 시간 (밀리초, 24시간) |
| `SPRING_PROFILES_ACTIVE` | `prod` | Spring 프로파일 (운영 환경) |
| `CORS_ALLOWED_ORIGINS` | `https://your-frontend-url.com` | CORS 허용 도메인 |

#### 환경 변수 값 생성 팁

**JWT_SECRET 생성 방법**:
```bash
# 안전한 랜덤 키 생성 (최소 32자)
openssl rand -base64 32
```

또는 간단히:
```
my-super-secret-jwt-key-for-production-2025
```

**CORS_ALLOWED_ORIGINS**:
- 프론트엔드가 있다면 프론트엔드 URL 입력
- 없다면 Render 서비스 URL 입력: `https://life-events-xxxx.onrender.com`

### 5. Dockerfile 확인

프로젝트 루트에 `Dockerfile`이 다음과 같이 존재하는지 확인:

```dockerfile
# 빌드 스테이지
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 실행 스테이지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/gift-money-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**중요 사항**:
- JAR 파일명이 `pom.xml`의 `artifactId`와 일치해야 함
- `gift-money-0.0.1-SNAPSHOT.jar` 부분을 실제 빌드되는 JAR 파일명으로 수정

### 6. 배포 시작

1. 모든 설정 확인 후 **"Create Web Service"** 클릭
2. 자동으로 배포 시작:
   - GitHub에서 코드 다운로드
   - Docker 이미지 빌드 (Maven 빌드 포함)
   - 이미지 레지스트리에 푸시
   - 컨테이너 시작
   - 애플리케이션 실행

### 7. 배포 진행 상황 모니터링

배포 페이지에서 실시간 로그 확인:

#### 성공적인 배포 로그 예시
```
==> Building...
#14 33.39 [INFO] BUILD SUCCESS
#15 DONE 0.2s
==> Deploying...
Tomcat started on port 10000 (http)
Started GiftMoneyApplication in 125 seconds
```

배포 완료 시 상태가 **"Live"**로 변경됩니다.

---

## 배포 후 확인

### 1. 서비스 URL 확인

배포 완료 후 Render 대시보드에서 서비스 URL 확인:
```
https://life-events-xxxx.onrender.com
```

### 2. API 엔드포인트 테스트

#### Health Check
```bash
curl https://life-events-xxxx.onrender.com/actuator/health
```

예상 응답:
```json
{"status":"UP"}
```

#### 회원가입 테스트
```bash
curl -X POST https://life-events-xxxx.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test1234!",
    "email": "test@example.com"
  }'
```

#### 로그인 테스트
```bash
curl -X POST https://life-events-xxxx.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test1234!"
  }'
```

### 3. Supabase 테이블 확인

1. Supabase 대시보드 접속
2. 좌측 메뉴에서 **"Table Editor"** 클릭
3. 생성된 테이블 확인:
   - `users` 테이블
   - `gift_money` 테이블

### 4. Render 로그 모니터링

Render 대시보드에서:
1. 서비스 선택
2. **"Logs"** 탭 클릭
3. 실시간 로그 확인

---

## 문제 해결

### 1. 빌드 실패: JAR 파일을 찾을 수 없음

**에러 메시지**:
```
failed to calculate checksum: "/app/target/gift-money-service-0.0.1-SNAPSHOT.jar": not found
```

**원인**: Dockerfile의 JAR 파일명이 실제 빌드되는 파일명과 다름

**해결방법**:
1. `pom.xml`에서 `<artifactId>` 확인:
   ```xml
   <artifactId>gift-money</artifactId>
   ```

2. Dockerfile의 JAR 파일명 수정:
   ```dockerfile
   COPY --from=build /app/target/gift-money-0.0.1-SNAPSHOT.jar app.jar
   ```

### 2. 데이터베이스 연결 실패: No route to host

**에러 메시지**:
```
java.net.NoRouteToHostException: No route to host
```

**원인**: IPv6 연결 시도 실패

**해결방법 1**: DB_URL에 IPv4 강제 옵션 추가
```
jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:6543/postgres?ipv6=false
```

**해결방법 2**: Connection Pooler 사용 (포트 6543)
```
jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:6543/postgres
```

### 3. Maven Wrapper 파일 없음

**에러 메시지**:
```
./mvnw: not found
```

**원인**: Maven Wrapper 파일이 Git에 커밋되지 않음

**해결방법**: Dockerfile에서 시스템 Maven 사용
```dockerfile
FROM maven:3.9-eclipse-temurin-17-alpine AS build
...
RUN mvn clean package -DskipTests
```

### 4. 환경 변수 설정 누락

**증상**: 애플리케이션이 시작되지 않거나 데이터베이스 연결 실패

**해결방법**:
1. Render 대시보드에서 서비스 선택
2. **"Environment"** 탭 클릭
3. 모든 필수 환경 변수가 설정되었는지 확인
4. 변경 후 자동으로 재배포됨

### 5. Free Tier 콜드 스타트 지연

**증상**: 첫 요청 시 50초 이상 응답 없음

**원인**: Render Free 인스턴스는 15분 비활성 후 자동 스핀다운

**해결방법**:
- 정상 동작입니다 (Free 플랜 제한사항)
- 유료 플랜으로 업그레이드하면 상시 가동
- 또는 주기적인 핑(ping) 서비스 사용 (예: UptimeRobot)

### 6. CORS 에러

**증상**: 프론트엔드에서 API 호출 시 CORS 에러 발생

**해결방법**:
1. Render 대시보드에서 `CORS_ALLOWED_ORIGINS` 환경 변수 확인
2. 프론트엔드 URL이 정확히 설정되었는지 확인:
   ```
   https://your-frontend-url.com
   ```
3. 여러 도메인 허용 시 쉼표로 구분:
   ```
   https://domain1.com,https://domain2.com
   ```

### 7. 배포 로그에서 "in progress" 무한 대기

**증상**: 배포 상태가 계속 "in progress"에서 멈춤

**해결방법**:
1. 로그에서 마지막 메시지 확인
2. 애플리케이션이 포트 10000에서 시작되었는지 확인:
   ```
   Tomcat started on port 10000 (http)
   ```
3. Spring Boot가 `server.port=${PORT:8080}` 설정으로 Render의 PORT 환경 변수를 읽는지 확인

---

## 추가 설정 (선택사항)

### Auto Deploy 설정

GitHub에 push할 때마다 자동 배포:

1. Render 서비스 설정에서 **"Settings"** 탭
2. **"Build & Deploy"** 섹션
3. **"Auto-Deploy"**: **Yes** 선택 (기본값)

특정 브랜치만 배포하려면:
- **"Branch"** 설정에서 원하는 브랜치 선택 (예: `main`, `production`)

### Custom Domain 설정

자신의 도메인 연결:

1. Render 서비스 설정에서 **"Settings"** 탭
2. **"Custom Domains"** 섹션
3. **"Add Custom Domain"** 클릭
4. 도메인 입력 (예: `api.yourdomain.com`)
5. DNS 레코드 추가 (Render가 안내하는 대로)

### Health Check 설정

애플리케이션 상태 모니터링:

1. `application.properties`에 Actuator 설정:
   ```properties
   management.endpoints.web.exposure.include=health,info
   management.endpoint.health.show-details=always
   ```

2. Render에서 Health Check 경로 설정:
   - **"Settings"** 탭
   - **"Health Check Path"**: `/actuator/health`

---

## 배포 체크리스트

배포 전 최종 확인:

- [ ] GitHub에 최신 코드 push 완료
- [ ] Supabase 프로젝트 생성 및 연결 정보 확보
- [ ] Dockerfile 작성 및 JAR 파일명 확인
- [ ] `.gitignore`에 `.env` 파일 추가 (환경 변수 보안)
- [ ] Render 계정 생성 및 GitHub 연동
- [ ] 7개 환경 변수 모두 설정
  - [ ] DB_URL
  - [ ] DB_USERNAME
  - [ ] DB_PASSWORD
  - [ ] JWT_SECRET
  - [ ] JWT_EXPIRATION
  - [ ] SPRING_PROFILES_ACTIVE
  - [ ] CORS_ALLOWED_ORIGINS
- [ ] 배포 시작 및 로그 모니터링
- [ ] 배포 완료 후 API 테스트
- [ ] Supabase 테이블 생성 확인

---

## 참고 자료

- [Render 공식 문서](https://render.com/docs)
- [Supabase 공식 문서](https://supabase.com/docs)
- [Spring Boot 배포 가이드](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html)
- [Docker 멀티 스테이지 빌드](https://docs.docker.com/build/building/multi-stage/)

---

## 지원

문제가 발생하면:
1. Render 로그 확인
2. Supabase 프로젝트 설정 확인
3. 환경 변수 재확인
4. 이 가이드의 "문제 해결" 섹션 참조

---

**축하합니다!** 성공적으로 배포를 완료했습니다! 🎉
