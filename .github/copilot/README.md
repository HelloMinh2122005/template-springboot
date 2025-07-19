# Hướng dẫn GitHub Copilot cho dự án

## Ngôn ngữ
- Luôn trả lời bằng **tiếng Việt**.

## Cấu trúc thư mục
- **Thư mục gốc dự án**: `../../src/main/java/org/minh/template`
- **Cấu hình Docker**: `../../docker`
- **Cấu hình ứng dụng**: `../../src/main/java/org/minh/template/config`
- **Controller**: `../../src/main/java/org/minh/template/controller`
- **DTO**:
    - Request: `../../src/main/java/org/minh/template/dto/request`
    - Response: `../../src/main/java/org/minh/template/dto/response`
- **Entity**: `../../src/main/java/org/minh/template/entity`
- **Exception**: `../../src/main/java/org/minh/template/exception`
- **Repository**: `../../src/main/java/org/minh/template/repository`
- **Security**: `../../src/main/java/org/minh/template/security`
- **Service**: `../../src/main/java/org/minh/template/service`

## File cấu hình quan trọng
- **Cấu hình ứng dụng (YAML)**: `../../src/main/resources/application.yaml`
- **Cấu hình Maven**: `../../pom.xml`

## Yêu cầu khi viết code

### Ngôn ngữ & Framework
- Sử dụng **Java (Spring Boot)**.
- Ưu tiên **clean code**, tuân thủ nguyên tắc **SOLID**.
- Luôn đặt **Javadoc** cho các method `public`.

### API
- Đặt tên endpoint theo chuẩn **RESTful**.
- Sử dụng **DTO** để xử lý request/response.
- **Validate** dữ liệu đầu vào (dùng `jakarta.validation`).

### Security
- Áp dụng **JWT** cho xác thực.
- Mã hóa nhạy cảm (ví dụ: password) bằng `BCryptPasswordEncoder`.

### Database
- Sử dụng **Hibernate/JPA**.
- Không dùng query **raw SQL** nếu không cần thiết.
- Ưu tiên `@Query` hoặc **JpaRepository method naming convention**.

### Ghi chú
- Khi đề xuất code, Copilot cần **giải thích ngắn gọn (tiếng Việt)** về logic.
- Gợi ý phải **phù hợp với cấu trúc dự án** ở trên.


### Giải thích:
1. **Tổ chức rõ ràng**:
    - Phân loại theo mục (ngôn ngữ, cấu trúc thư mục, env, yêu cầu code).
    - Đường dẫn được liệt kê đầy đủ để Copilot hiểu ngữ cảnh.

2. **Định hướng Copilot**:
    - Luôn nhắc nhở sử dụng tiếng Việt.
    - Ghi rõ quy tắc code (RESTful, Javadoc, security...).

3. **Tính thực tế**:
    - Copy nguyên block `env` để Copilot biết các biến hiện có.
    - Nhấn mạnh vào các thư mục quan trọng (DTO, Controller, Security).

## Biến môi trường (env)
```env
# App Configuration
SERVER_PORT=8080
SERVER_ADDRESS=0.0.0.0
SERVER_SERVLET_CONTEXT_PATH=/
APP_SECRET=c04nKbDFfJ2v0XSRtNKHQQq5Km3zMSZxla7paMSUzBOI/ORhgnTC7eJrqqEZQGKm
APP_JWT_TOKEN_EXPIRES_IN=3600000
APP_JWT_REFRESH_TOKEN_EXPIRES_IN=86400000
APP_JWT_REMEMBER_ME_EXPIRES_IN=604800000
APP_REGISTRATION_EMAIL_TOKEN_EXPIRES_IN=3600
APP_REGISTRATION_PASSWORD_TOKEN_EXPIRES_IN=3600000
APP_DEFAULT_LOCALE=vi
APP_DEFAULT_TIMEZONE=Asia/Ho_Chi_Minh
APP_URL=http://localhost:8080
APP_NAME=Template
APP_DESCRIPTION=Template_Description
PROJECT_NAME=template-project

# Postgresql
DB_DDL_AUTO=update
POSTGRESQL_HOST=localhost
POSTGRESQL_PORT=5432
POSTGRESQL_DB=postgres
POSTGRESQL_USER=postgres
POSTGRESQL_PASSWORD=secret

# Redis Configuration
REDIS_DATABASE=0
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=secret
REDIS_TIMEOUT=60000

# Pagination
PAGEABLE_DEFAULT_PAGE_SIZE=20

# Swagger / OpenAPI
API_DOCS_ENABLED=true
SWAGGER_ENABLED=true
SWAGGER_PATH=/swagger-ui

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_HIBERNATE=INFO
LOGGING_LEVEL_SPRING=INFO
MESSAGES_CACHE_DURATION=-1