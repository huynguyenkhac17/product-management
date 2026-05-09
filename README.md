# Product Management Backend

## Tổng quan
Dự án Quản lý Sản phẩm là một ứng dụng Spring Boot CRUD được xây dựng trên nền tảng **base-saola** framework. Ứng dụng cung cấp RESTful API hoàn chỉnh để quản lý danh sách sản phẩm với các tính năng: CRUD, phân trang, lọc/tìm kiếm, kiểm toán (audit) và xóa logic (soft delete).

## Ngăn xếp công nghệ

| Thành phần              | Phiên bản   |
|------------------------|-----------|
| Java                   | 17        |
| Spring Boot            | 3.2.0     |
| Hibernate              | 6.5.1     |
| Jackson                | 2.15.0    |
| Jakarta Validation     | 3.1.0     |
| base-saola framework   | 6.5.1     |

---

## Cấu trúc dự án

Dự án được tổ chức theo kiến trúc phân lớp (Layered Architecture) với sự hỗ trợ đầy đủ từ base-saola:

```
src/main/java/com/example/product/
├── entity/              # Entity models
│   └── Product
├── repository/          # Spring Data JPA repositories
│   └── ProductRepository
├── service/             # Business logic
│   ├── ProductService (interface)
│   └── ProductServiceImpl
├── dto/                 # DTO classes
│   ├── ProductGet
│   ├── ProductCreate
│   └── ProductUpdate
├── filter/              # Filter & pagination support
│   └── ProductFilter
├── controller/          # REST controllers
│   └── ProductController
└── resources/           # Configuration files
    └── application.yml
```

### Mô hình Entity

Ứng dụng sử dụng **3 cấp entity** theo base-saola:

- **`BaseEntity`**: chỉ có `id`
- **`AuditableEntity`**: thêm `creator`, `dateCreated`, `updater`, `dateUpdated`
- **`VoidableEntity`**: thêm `voided`, `voidedBy`, `dateVoided`, `voidReason` (xóa logic)

**Product** kế thừa từ `VoidableGeneratedIDEntry<String>` (UUID tự sinh, hỗ trợ soft delete).

### Repository

```java
public interface ProductRepository extends VoidableRepository<Product, String> { }
```

Cung cấp sẵn:
- CRUD cơ bản từ `JpaRepository`
- Specification-based filtering từ `JpaSpecificationExecutor`
- Xóa logic và khôi phục bản ghi (`voided = false` mặc định)

### DTO

3 loại DTO tương ứng với 3 thao tác chính:

| DTO              | Mục đích           | Phương thức chính           |
|------------------|------------------|---------------------------|
| `ProductGet`     | Trả về cho client | `parse(Product entity)`   |
| `ProductCreate`  | Tạo mới           | `toEntry()` → Entity      |
| `ProductUpdate`  | Cập nhật          | `apply(Product) → boolean`|

### Service

`ProductService` kế thừa từ `VoidableDtoService`, được implement bởi:

```java
@Service
public class ProductServiceImpl 
    extends VoidableDtoJpaServiceImpl<ProductGet, Product, String>
    implements ProductService { }
```

Cung cấp sẵn:
- Quản lý CRUD + xóa logic / khôi phục
- Phân trang & lọc qua `BaseFilter` + `SpecBuilder`
- Kiểm toán tự động (audit info gắn kèm từ `callerId`)
- Xử lý exception & validation

### Controller

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final AuditableDtoAPIMethod<ProductGet, Product, String> api;
    // REST endpoints: getList, getById, create, update, delete, search
}
```

---

## API Response & Exception Handling

### Cấu trúc Response chuẩn

Mọi endpoint trả về `APIResponse<T>` hoặc `APIListResponse<T>`:

```json
{
  "header": {
    "datetime": "2026-05-09 10:30:45 +07:00",
    "code": 209,
    "message": "Object found"
  },
  "body": { "id": "uuid-xxx", "name": "Product Name" }
}
```

**Status codes**:
- `209` (FOUND): lấy dữ liệu thành công
- `211` (DELETED): xóa thành công
- `212` (UPDATED): cập nhật thành công
- `452` (INVALID_PARAMETER): dữ liệu không hợp lệ
- `453` (DUPLICATED): id trùng
- `454` (PARAMETER_REQUIRED): thiếu field bắt buộc

### Exception Mapping

Mọi lỗi nghiệp vụ được tự động xử lý bởi `APIMethod`:

| Exception                       | HTTP Status | Mã Response |
|---------------------------------|-----------|-----------|
| `ObjectNotFoundException`        | 400       | 404       |
| `DuplicateIdentifierException`  | 400       | 453       |
| `IllegalPropertyException`      | 400       | 452       |
| `CannotDeleteObjectInUseException` | 400    | 424       |

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
    // Thêm tiêu chí lọc tùy chỉnh
}
```

Service sẽ convert `ProductFilter` → `Specification<Product>` tự động.

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
- **Database**: Cấu hình trong `application.yml`

### Các bước

```bash
# Clone repository
git clone https://github.com/huynguyenkhac17/product-management.git

# Cài đặt dependencies
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

API sẽ chạy trên `http://localhost:8080` (hoặc port trong `application.yml`).

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

## Tham chiếu

- **base-saola Framework**: `vn.saolasoft:base-saola:6.5.1`
- **Repository**: https://github.com/huynguyenkhac17/product-management
- **Tài liệu Saola**: Xem `DOCUMENTATION.md` của base-saola
