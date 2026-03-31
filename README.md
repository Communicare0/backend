# Communicare Backend

이 프로젝트는 Communicare 서비스의 백엔드 서버입니다. 안정적이고 확장 가능한 API 제공을 목표로 하며, 컨테이너 기반 환경에서 배포 및 운영됩니다.

## Tech Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Build Tool:** Gradle
- **Database:** PostgreSQL
- **Infrastructure:** Docker
- **CI/CD:** GitHub Actions

## Project Structure

```text
.
├── .github/workflows     # CI/CD 파이프라인 및 자동화 설정
├── src                   # 애플리케이션 비즈니스 로직 및 소스 코드
├── Dockerfile            # 도커 이미지 빌드 파일
├── docker-compose.yml    # 로컬 개발 환경 및 다중 컨테이너 실행 설정
├── build.gradle          # 빌드 및 의존성 관리
└── settings.gradle       # 프로젝트 빌드 설정
```

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Gradle

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone [https://github.com/Communicare0/backend.git](https://github.com/Communicare0/backend.git)
   cd backend
   ```

2. **Run with Docker Compose**
   로컬 개발에 필요한 데이터베이스(PostgreSQL, Redis 등)와 백엔드 애플리케이션을 한 번에 실행합니다.
   ```bash
   docker-compose up -d
   ```

3. **Build & Run (without Docker)**
   로컬 환경에서 직접 빌드하고 실행할 경우 다음 명령어를 사용합니다.
   ```bash
   ./gradlew clean build
   ./gradlew bootRun
   ```

## CI/CD Pipeline

GitHub Actions를 통해 코드가 브랜치에 푸시될 때마다 자동으로 빌드 및 테스트가 수행되며, 도커 이미지로 패키징되어 배포 환경에 적용됩니다. 자세한 워크플로우 설정은 `.github` 디렉토리 내부에서 확인할 수 있습니다.
