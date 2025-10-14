# 🧪 테스트 완벽 가이드

## 📌 용어 정리 (중요!)

### Supabase vs Render
| 항목 | Supabase | Render |
|------|----------|--------|
| **역할** | 데이터베이스 호스팅 | 애플리케이션 배포 |
| **비유** | 내 데이터를 저장하는 클라우드 하드디스크 | 내 프로그램을 실행하는 클라우드 컴퓨터 |
| **무료인가?** | ✅ 오픈소스 + 무료 플랜 (500MB) | ✅ 상용 + 무료 플랜 (750시간/월) |
| **대안** | PlanetScale, MongoDB Atlas | Railway, Fly.io, AWS EC2 |

**💡 간단히:**
- **Supabase** = PostgreSQL 데이터베이스를 인터넷에서 쓸 수 있게 해줌
- **Render** = Spring Boot 앱을 인터넷에서 접속 가능하게 만들어줌

---

## 🏠 로컬 테스트 (배포 필요 없음!)

지금 당장 내 컴퓨터에서만 테스트할 수 있습니다!

### ✅ 방법 1: 직접 Java로 실행 (가장 간단!)

```bash
# 1. 프로젝트 폴더로 이동
cd D:\life_events

# 2. 컴파일
javac -encoding UTF-8 -d target/classes -cp "target/classes;%USERPROFILE%\.m2\repository\*" src/main/java/com/example/giftmoney/**/*.java

# 3. Spring Boot JAR 빌드 (Maven이 설치되어 있다면)
mvn clean package -DskipTests

# 4. 실행
java -jar target/gift-money-0.0.1-SNAPSHOT.jar
```

**문제:** Maven이 없으면 안 됨!

---

### ✅ 방법 2: Spring Initializr에서 다시 생성 (권장!)

**문제 원인:** 제가 Maven Wrapper 파일을 누락했습니다.

**해결 방법:**
1. https://start.spring.io 접속
2. 다음 설정으로 프로젝트 생성:
   ```
   Project: Maven
   Language: Java
   Spring Boot: 3.2.0
   Group: com.example
   Artifact: gift-money
   Dependencies: Spring Web, Spring Data JPA, Spring Security, H2 Database, Validation, Lombok
   ```
3. **Generate** 클릭 → ZIP 다운로드
4. 압축 해제 후 제가 만든 소스코드를 복사:
   ```
   src/main/java/com/example/giftmoney/**/*.java → 새 프로젝트에 복사
   src/main/resources/application.yml → 새 프로젝트에 복사
   pom.xml → JWT, POI 의존성 추가
   ```

---

### ✅ 방법 3: IntelliJ IDEA 사용 (가장 편함!)

1. **IntelliJ IDEA 설치** (Community 무료 버전)
2. **Open Project** → `D:\life_events` 선택
3. IntelliJ가 자동으로 Maven 다운로드 및 설정
4. **Run** 버튼 클릭!

---

## 🧪 API 테스트 방법

### 1️⃣ 브라우저로 테스트 (간단!)

애플리케이션 실행 후:
```
http://localhost:8080/h2-console
```

H2 Console 설정:
- JDBC URL: `jdbc:h2:mem:testdb`
- User Name: `sa`
- Password: (비워두기)

---

### 2️⃣ Postman 사용 (추천!)

**Postman 설치:**
https://www.postman.com/downloads/

**테스트 순서:**

#### Step 1: 회원가입
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

Body (raw JSON):
{
  "email": "test@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

#### Step 2: 로그인
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

Body:
{
  "email": "test@example.com",
  "password": "password123"
}
```

**Response에서 token 복사!**

#### Step 3: 축의금 생성
```
POST http://localhost:8080/api/gift-money
Content-Type: application/json
Authorization: Bearer [복사한_토큰_붙여넣기]

Body:
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

---

### 3️⃣ curl 사용 (명령줄)

**Windows PowerShell:**
```powershell
# 회원가입
Invoke-WebRequest -Uri http://localhost:8080/api/auth/register `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"test@example.com","password":"password123","name":"홍길동"}'

# 로그인
$response = Invoke-WebRequest -Uri http://localhost:8080/api/auth/login `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"test@example.com","password":"password123"}'

$token = ($response.Content | ConvertFrom-Json).token
Write-Host "Token: $token"

# 축의금 생성
Invoke-WebRequest -Uri http://localhost:8080/api/gift-money `
  -Method POST `
  -ContentType "application/json" `
  -Headers @{"Authorization"="Bearer $token"} `
  -Body '{"eventDate":"2025-10-15","eventType":"결혼식","giverName":"김철수","giverRelation":"친구","amount":100000,"contact":"010-1234-5678","memo":"대학 동기"}'
```

---

## 🌐 배포 테스트 (나중에!)

배포는 나중에 해도 됩니다! 로컬 테스트가 먼저입니다.

### 배포 순서 (간단 요약)
1. **GitHub에 코드 올리기**
2. **Supabase 가입** → PostgreSQL 생성 (5분)
3. **Render 가입** → GitHub 연동 → 배포 (10분)

---

## ❓ 자주 묻는 질문

### Q1: Maven Wrapper가 왜 없나요?
**A:** 제가 실수로 생성 안 했습니다. Spring Initializr에서 다시 생성하면 자동으로 포함됩니다.

### Q2: 로컬에서만 테스트하려면?
**A:** Supabase, Render 필요 없습니다! IntelliJ나 Eclipse로 실행하면 됩니다.

### Q3: 배포는 언제 해야 하나요?
**A:** 로컬 테스트 완료 후, 친구/가족에게 공유하고 싶을 때!

### Q4: 무료인가요?
**A:**
- 로컬 개발: 완전 무료
- Supabase: 500MB까지 무료
- Render: 750시간/월 무료 (충분함)

### Q5: DB는 어떻게 되나요?
**A:**
- 로컬: H2 데이터베이스 (메모리, 재시작 시 삭제)
- 배포: PostgreSQL (Supabase, 영구 저장)

---

## 🎯 추천 테스트 순서

1. **IntelliJ IDEA 설치** (무료)
2. **프로젝트 열기** (`D:\life_events`)
3. **Run 버튼 클릭!**
4. **Postman으로 API 테스트**
5. **H2 Console로 데이터 확인**
6. **(나중에) Supabase + Render 배포**

---

## 🛠️ 다음 할 일

- [ ] IntelliJ IDEA 설치
- [ ] 프로젝트 실행 확인
- [ ] Postman으로 API 테스트
- [ ] H2 Console 접속 확인
- [ ] 데이터 CRUD 테스트 완료

**성공하면** → 프론트엔드 개발 또는 배포 진행!
