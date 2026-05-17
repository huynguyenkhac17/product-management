# API Documentation — Product Management

Base URL: `http://localhost:8080`

---

## Auth

### POST /api/auth/register

```json
POST /api/auth/register
Content-Type: application/json

{
  "username": "khoi",
  "password": "secret123",
  "fullName": "Nguyễn Văn Khôi"
}
```

**Response 201**
```json
{
  "header": { "code": 201, "message": "User registered" },
  "body": { "id": 1, "username": "khoi", "fullName": "Nguyễn Văn Khôi", "enabled": true }
}
```

**Response 400** — username trùng
```json
{ "header": { "code": 453, "message": "Username already exists: khoi" }, "body": null }
```

---

### POST /api/auth/login

```json
POST /api/auth/login
Content-Type: application/json

{
  "username": "khoi",
  "password": "secret123"
}
```

**Response 200**
```json
{
  "header": { "code": 200, "message": "Login success" },
  "body": {
    "token": "eyJhbGci...",
    "userId": 1,
    "username": "khoi",
    "expiresInMs": 86400000
  }
}
```

**Response 401** — sai password
```json
{ "header": { "code": 401, "message": "Invalid username or password" }, "body": null }
```

---

### GET /api/auth/me

```
GET /api/auth/me
Authorization: Bearer <token>
```

**Response 200**
```json
{
  "header": { "code": 209, "message": "User found" },
  "body": { "id": 1, "username": "khoi", "fullName": "Nguyễn Văn Khôi", "enabled": true }
}
```

---

## Products

> Tất cả endpoint `/api/products/**` đều yêu cầu header:
> `Authorization: Bearer <token>`
>
> Không có token → **401**

---

### GET /api/products

```
GET /api/products?firstRow=0&maxResults=20&orderBy=+name
Authorization: Bearer <token>
```

**Response 200**
```json
{
  "header": { "code": 209, "message": "N record(s) found", "offset": 0, "limit": 20, "totalRecords": N },
  "body": [ { "id": "...", "name": "...", "price": 150000, ... } ]
}
```

---

### GET /api/products/{id}

```
GET /api/products/abc-123
Authorization: Bearer <token>
```

**Response 200** — sản phẩm tìm thấy
**Response 400** — không tìm thấy (`code: 404`)

---

### POST /api/products

```json
POST /api/products
Content-Type: application/json
Authorization: Bearer <token>

{
  "name": "Áo thun nam",
  "description": "Áo thun cotton 100%",
  "price": 150000,
  "quantity": 50,
  "sku": "AT-NAM-001"
}
```

**Response 200** — trả về `id` (UUID) của sản phẩm vừa tạo
> Cột `creator_id` trong DB phải bằng `userId` từ JWT token.

---

### PUT /api/products

```json
PUT /api/products
Content-Type: application/json
Authorization: Bearer <token>

{
  "id": "<product-id>",
  "name": "Áo thun nam (updated)",
  "price": 180000
}
```

**Response 200** — trả về `id`

---

### DELETE /api/products/{id}

```
DELETE /api/products/abc-123
Authorization: Bearer <token>
```

Xóa mềm: `voided = true`, vẫn còn trong DB.

**Response 200**

---

### POST /api/products/{id}/restore

```
POST /api/products/abc-123/restore
Authorization: Bearer <token>
```

Khôi phục sản phẩm đã xóa mềm.

**Response 200**

---

### GET /api/products/voided

```
GET /api/products/voided?firstRow=0&maxResults=20
Authorization: Bearer <token>
```

Danh sách sản phẩm đã xóa mềm.

---

### POST /api/products/search

```json
POST /api/products/search
Content-Type: application/json
Authorization: Bearer <token>

{
  "filter": {
    "keyword": "áo"
  },
  "firstRow": 0,
  "maxResults": 10,
  "orderBy": "-price"
}
```

> `orderBy`: `+col` = ASC, `-col` = DESC. Các cột hợp lệ: `id`, `name`, `price`, `quantity`, `dateCreated`.
