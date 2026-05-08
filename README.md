# Product Management API

Ứng dụng quản lí sản phẩm sử dụng framework base-saola --- Codebase của cty tôi đang thực tập (2026)

## Stack


- Java 17
- Spring Boot 3.2.0
- MySQL
- base-saola 6.5.1 (framework công ty)

## Setup

### 1. Yêu cầu

- JDK 17+
- Maven 3.9+
- MySQL (hoặc XAMPP)

### 2. Clone & Setup

```bash
git clone <repo-url>
cd backend
```

### 3. Install base-saola dependency

**Lưu ý:** base-saola phải được install vào local Maven repo trước.

```bash
# Nếu có base-saola source:
cd ../base-saola-dev-hibernate-6.5.1-khoi/base-saola-dev-hibernate-6.5.1-khoi
mvn install -DskipTests

# Hoặc download từ artifact repository công ty
# (liên hệ lead để lấy)
```

### 4. Cấu hình database

Sửa `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/product_db?createDatabaseIfNotExist=true
    username: root
    password: <your-mysql-password>
```

### 5. Chạy

```bash
mvn spring-boot:run
# hoặc chạy App.java từ IDE
```

Server chạy tại `http://localhost:8080`

## API Endpoints

| Method | URL | Ghi chú |
|---|---|---|
| GET | `/api/products` | Danh sách (phân trang) |
| GET | `/api/products/{id}` | Chi tiết |
| POST | `/api/products` | Tạo mới |
| PUT | `/api/products` | Cập nhật |
| DELETE | `/api/products/{id}` | Xóa mềm |
| POST | `/api/products/{id}/restore` | Khôi phục |
| GET | `/api/products/voided` | Xem đã xóa |
| POST | `/api/products/search` | Tìm kiếm |

## Ghi chú

- Framework base-saola là **internal framework** của công ty — không public
- Nếu không có access đến base-saola, không thể chạy project này
