# Product Management Backend

## Tổng quan
Dự án Quản lý Sản phẩm là một ứng dụng Spring Boot CRUD được xây dựng trên nền tảng **base-saola** framework. Ứng dụng cung cấp RESTful API hoàn chỉnh để:

- Quản lý người dùng (đăng ký, đăng nhập, JWT authentication)
- Quản lý sản phẩm: CRUD, phân trang, lọc/tìm kiếm, kiểm toán (audit), xóa logic (soft delete) & khôi phục

## Ngăn xếp công nghệ

| Thành phần             | Phiên bản   |
|------------------------|-----------|
| Java                   | 17        |
| Spring Boot            | 3.2.0     |
| Spring Security        | 6.x       |
| Hibernate              | 6.5.1     |
| Jackson                | 2.15.0    |
| Jakarta Validation     | 3.1.0     |
| jjwt (JWT)             | 0.12.x    |
| base-saola framework   | 6.5.1     |

---

## Cấu trúc dự án

Dự án được tổ chức theo kiến trúc phân lớp (Layered Architecture) với sự hỗ trợ đầy đủ từ base-saola:

```
src/main/java/com/example/product/
├── App.java                # Spring Boot entry point + @EnableJpaAuditing
├── entity/                 # Entity models
│   ├── Product
│   └── User
├── repository/             # Spring Data JPA repositories
│   ├── ProductRepository
│   └── UserRepository
├── service/                # Business logic
│   ├── ProductService           (interface)
│   ├── UserService              (interface)
│   └── impl/
│       ├── ProductServiceImpl
│       └── UserServiceImpl
├── dto/                    # DTO classes (PascalCase: Dto, không phải DTO)
│   ├── ProductDtoGet
│   ├── ProductDtoCreate
│   ├── ProductDtoUpdate
│   ├── UserDtoGet
│   ├── RegisterRequest
│   ├── LoginRequest
│   └── LoginResponse
├── filter/                 # Filter & pagination support
│   └── ProductFilter
├── security/               # JWT + Spring Security
│   ├── JwtUtil
│   ├── JwtAuthFilter
│   └── SecurityConfig
├── controller/             # REST controllers
│   ├── AuthController
│   └── ProductController
└── resources/
    └── application.yml
```

### Mô hình Entity

Ứng dụng sử dụng **3 cấp entity** theo base-saola:

- **`BaseEntity`**: chỉ có `id`
- **`AuditableEntity`**: thêm `creator`, `dateCreated`, `updater`, `dateUpdated`
- **`VoidableEntity`**: thêm `voided`, `voidedBy`, `dateVoided`, `voidReason` (xóa logic)

| Entity    | Kế thừa                              | ID          |
|-----------|--------------------------------------|-------------|
| `Product` | `VoidableGeneratedIDEntry<String>`   | UUID (String) |
| `User`    | `VoidableSerialIDEntry<Long>`        | Serial (Long) |

> **Convention dự án:** mọi entity trong dự án đều kế thừa `Voidable*` — mặc định soft-delete.

### Repository

```java
public interface ProductRepository extends VoidableRepository<Product, String> { }
public interface UserRepository    extends VoidableRepository<User, Long> {
    Optional<User> findByUsernameAndVoidedFalse(String username);
    boolean existsByUsernameAndVoidedFalse(String username);
}
```

Có sẵn:
- CRUD cơ bản từ `JpaRepository`
- Specification-based filtering từ `JpaSpecificationExecutor`
- Xóa logic và khôi phục bản ghi (`voided = false` mặc định)

### DTO

| DTO                | Mục đích                | Phương thức chính           |
|--------------------|-------------------------|-----------------------------|
| `ProductDtoGet`    | Trả về cho client        | `parse(Product entity)`     |
| `ProductDtoCreate` | Tạo mới                  | `toEntry()` → Entity        |
| `ProductDtoUpdate` | Cập nhật                 | `apply(Product) → boolean`  |
| `UserDtoGet`       | Trả về user (không lộ hash) | `parse(User entity)`     |
| `RegisterRequest`  | Đăng ký user             | username/password/fullName  |
| `LoginRequest`     | Đăng nhập                | username/password           |
| `LoginResponse`    | Token + thông tin user   | token/userId/username/expiresInMs |

### Service

`ProductService` kế thừa từ `VoidableDtoService`, được implement bởi:

```java
@Service
public class ProductServiceImpl
    extends VoidableDtoJpaServiceImpl<ProductDtoGet, Product, String>
    implements ProductService { }
```

`UserService` thêm 2 method nghiệp vụ riêng:

```java
public interface UserService extends VoidableDtoService<UserDtoGet, User, Long> {
    UserDtoGet register(RegisterRequest req);
    User authenticate(String username, String rawPassword);
}
```

Service cung cấp sẵn:
- Quản lý CRUD + xóa logic / khôi phục
- Phân trang & lọc qua `BaseFilter` + `SpecBuilder`
- Kiểm toán tự động (audit info gắn kèm từ `callerId`)
- Xử lý exception & validation

### Controller

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final AuditableDtoAPIMethod<ProductDtoGet, Product, String> api;
    // REST endpoints: getList, getById, create, update, delete, restore, search, getVoided
}

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    // register, login, me
}
```

`callerId` lấy từ `SecurityContextHolder` (do `JwtAuthFilter` set principal = `userId: Long`).

---

## Authentication & Authorization

### Flow

1. `POST /api/auth/register` → tạo user mới (password hash bằng BCrypt).
2. `POST /api/auth/login` → trả về JWT (HS256), TTL cấu hình trong `app.jwt.expiration-ms`.
3. Các request sau gửi header `Authorization: Bearer <token>`.
4. `JwtAuthFilter` parse token, set `principal = userId` vào `SecurityContext`.
5. Controller lấy `userId` qua `SecurityContextHolder` để truyền vào `callerId` của `APIMethod`.

### Phân quyền hiện tại

- `/api/auth/**` → public
- `/api/products/**` → yêu cầu JWT hợp lệ
- Chưa có role-based access (mọi user đã đăng nhập đều có quyền như nhau).

---

## API Response & Exception Handling

### Cấu trúc Response chuẩn

Mọi endpoint trả về `APIResponse<T>` hoặc `APIListResponse<T>`:

```json
{
  "header": {
    "datetime": "2026-05-18 10:30:45 +07:00",
    "code": 209,
    "message": "Object found"
  },
  "body": { "id": "uuid-xxx", "name": "Product Name" }
}
```

**Status codes** (`APIResponseStatus`):
- `200` (OK), `201` (CREATED), `209` (FOUND)
- `211` (DELETED), `212` (UPDATED), `213` (VOIDED), `214` (UNVOIDED)
- `401` (UNAUTHORIZED), `404` (NOT_FOUND)
- `452` (INVALID_PARAMETER), `453` (DUPLICATED), `454` (PARAMETER_REQUIRED)
- `500` (INTERNAL_SERVER_ERROR)

### Exception Mapping

Mọi lỗi nghiệp vụ được tự động xử lý bởi `APIMethod`:

| Exception                          | HTTP Status            | Mã Response              |
|------------------------------------|------------------------|--------------------------|
| `ObjectNotFoundException`          | 400 BAD_REQUEST        | `NOT_FOUND` (404)        |
| `DuplicateIdentifierException`     | 400 BAD_REQUEST        | `DUPLICATED` (453)       |
| `IllegalPropertyException`         | 400 BAD_REQUEST        | `INVALID_PARAMETER` (452)|
| `MissingRequiredPropertyException` | 400 BAD_REQUEST        | `INVALID_PARAMETER` (452)|
| `CannotDeleteObjectInUseException` | 400 BAD_REQUEST        | `FAILED_DEPENDENCY` (424)|
| `APIAuthenticationException`       | 403 FORBIDDEN          | `UNAUTHORIZED` (401)     |
| bất kỳ `Exception` khác            | 500 INTERNAL_SERVER_ERROR | `INTERNAL_SERVER_ERROR` (500) |

---

## Filter & Pagination

### PaginationInfo

Hỗ trợ phân trang kiểu REST (offset/limit):

```json
{
  "firstRow": 0,
  "maxResults": 20,
  "orders": [
    { "orderColumn": "dateCreated", "orderAsc": false }
  ]
}
```

Sắp xếp động qua chuỗi `+col1,-col2` (`+` = ASC, `-` = DESC).

### BaseFilter

```java
public class ProductFilter extends BaseFilter<Product, String> {
    private String keyword;       // search name/description
    // ... getters/setters
}
```

Service convert `ProductFilter` → `Specification<Product>` tự động.

---

## Utilities

base-saola cung cấp sẵn các tiện ích:

- **`AuditUtil`**: Ghi nhận creator/updater, xóa logic
- **`DateUtil`**: Format datetime chuẩn `"yyyy-MM-dd HH:mm:ss Z"`
- **`TextUtil`**: Xử lý chuỗi tiếng Việt (chuẩn hóa dấu)
- **`CollectionUtil`**: Chuyển đổi `String ↔ Set<String>`

---

## Cách chạy

### Yêu cầu hệ thống
- **Java**: 17 trở lên
- **Maven**: 3.6+
- **Database**: Cấu hình trong `application.yml` (mặc định MySQL/PostgreSQL/H2 tuỳ profile)
- **base-saola 6.5.1**: phải có trong local Maven repo (xem mục Ghi chú quan trọng bên dưới)

### Các bước

```bash
# Clone repository
git clone https://github.com/huynguyenkhac17/product-management.git
cd product-management

# Build
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

API sẽ chạy trên `http://localhost:8080` (hoặc port trong `application.yml`).

### Test nhanh

```bash
# Đăng ký
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"khoi","password":"secret123","fullName":"Nguyễn Văn Khôi"}'

# Đăng nhập (lấy token)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"khoi","password":"secret123"}'

# Tạo sản phẩm
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"name":"Áo thun nam","price":150000,"quantity":50,"sku":"AT-NAM-001"}'
```

Danh sách API đầy đủ + payload mẫu: xem [api-test.md](api-test.md).

---

## Tài liệu liên quan

- [DOCUMENTATION.md](DOCUMENTATION.md) — Tài liệu chi tiết về framework base-saola (entity layers, repository, service, API method, exception hierarchy).
- [api-test.md](api-test.md) — Danh sách endpoint + request/response mẫu.

---

## Ghi chú quan trọng

### Framework base-saola là internal
**base-saola** là internal framework của công ty — không public. Nếu không có access đến base-saola, không thể chạy project này.

### Cài đặt base-saola dependency

**Lưu ý:** base-saola phải được install vào local Maven repo trước.

**Nếu có base-saola source:**

```bash
cd ../base-saola-dev-hibernate-6.5.1-khoi/base-saola-dev-hibernate-6.5.1-khoi
mvn install -DskipTests
```

**Hoặc download từ artifact repository công ty:**

Liên hệ lead để lấy hướng dẫn truy cập artifact repository.

---
