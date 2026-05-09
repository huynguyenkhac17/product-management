package com.example.product.controller;

import com.example.product.dto.ProductCreate;
import com.example.product.dto.ProductGet;
import com.example.product.dto.ProductUpdate;
import com.example.product.entity.Product;
import com.example.product.filter.ProductFilter;
import com.example.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.saolasoft.base.api.method.AuditableDtoAPIMethod;
import vn.saolasoft.base.api.response.*;
import vn.saolasoft.base.service.filter.PageOfData;
import vn.saolasoft.base.service.filter.PaginationInfo;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    // AuditableDtoAPIMethod là lớp helper của framework: gói sẵn try-catch, log, build response chuẩn.
    // Ko cần viết try-catch trong controller, gọi luôn.
    private final AuditableDtoAPIMethod<ProductGet, Product, String> api; // Tiện.
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
        this.api = new AuditableDtoAPIMethod<>(productService);
    }

    // ───────────────────────────────────────────────
    // GET /api/products?firstRow=0&maxResults=20&orderBy=+name
    // Lấy danh sách sản phẩm chưa void, có phân trang
    // ───────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<APIListResponse<List<ProductGet>>> getList(
            @RequestParam(defaultValue = "0")   int firstRow,
            @RequestParam(defaultValue = "20")  int maxResults,
            @RequestParam(defaultValue = "")    String orderBy) {

        PaginationInfo pageInfo = new PaginationInfo(firstRow, maxResults, orderBy);
        return api.getList(pageInfo);
    }

    // ───────────────────────────────────────────────
    // GET /api/products/{id}
    // Lấy chi tiết 1 sản phẩm theo id
    // ───────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<ProductGet>> getById(@PathVariable String id) {
        return api.getById(id);
    }

    // ───────────────────────────────────────────────
    // POST /api/products
    // Tạo sản phẩm mới
    // Header: X-User-Id: 1
    // Body: { "name": "...", "price": 100000, ... }
    // ───────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<APIResponse<String>> create(
            @Valid @RequestBody ProductCreate dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long callerId) {

        return api.create(dto, callerId);
    }

    // ───────────────────────────────────────────────
    // PUT /api/products
    // Cập nhật sản phẩm (phải có id trong body)
    // Header: X-User-Id: 1
    // Body: { "id": "...", "name": "...", "price": 200000, ... }
    // ───────────────────────────────────────────────
    @PutMapping
    public ResponseEntity<APIResponse<String>> update(
            @Valid @RequestBody ProductUpdate dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long callerId) {

        return api.update(dto, callerId);
    }

    // ───────────────────────────────────────────────
    // DELETE /api/products/{id}
    // Xóa mềm sản phẩm (voided = true, vẫn còn trong DB)
    // Header: X-User-Id: 1
    // ───────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> delete(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long callerId) {

        return api.delete(id, callerId);
    }

    // ───────────────────────────────────────────────
    // POST /api/products/{id}/restore
    // Khôi phục sản phẩm đã xóa mềm (voided → false)
    // Header: X-User-Id: 1
    // ───────────────────────────────────────────────
    @PostMapping("/{id}/restore")
    public ResponseEntity<APIResponse<String>> restore(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long callerId) {

        try {
            String restoredId = productService.restoreByID(id, callerId);
            return ResponseEntity.ok(new APIResponse<>(
                    new APIResponseHeader(APIResponseStatus.UNVOIDED, "Product restored"),
                    restoredId));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new APIResponse<>(
                            new APIResponseHeader(APIResponseStatus.INTERNAL_SERVER_ERROR, ex.getMessage()),
                            null));
        }
    }

    // ───────────────────────────────────────────────
    // POST /api/products/search
    // Tìm kiếm sản phẩm với filter + phân trang
    // Body: { "keyword": "áo", "minPrice": 50000, "maxPrice": 500000,
    //         "firstRow": 0, "maxResults": 20, "orderBy": "-price" }
    // ───────────────────────────────────────────────
    @PostMapping("/search")
    public ResponseEntity<APIListResponse<List<ProductGet>>> search(
            @RequestBody SearchRequest body) {

        ProductFilter filter = body.getFilter();
        PaginationInfo pageInfo = new PaginationInfo(body.getFirstRow(), body.getMaxResults(), body.getOrderBy());
        return api.search(filter != null ? filter : new ProductFilter(), pageInfo);
    }

    // ───────────────────────────────────────────────
    // GET /api/products/voided?firstRow=0&maxResults=20
    // Lấy danh sách sản phẩm ĐÃ xóa mềm (để xem / restore)
    // ───────────────────────────────────────────────
    @GetMapping("/voided")
    public ResponseEntity<APIListResponse<List<ProductGet>>> getVoided(
            @RequestParam(defaultValue = "0")   int firstRow,
            @RequestParam(defaultValue = "20")  int maxResults) {

        try {
            PaginationInfo pageInfo = new PaginationInfo(firstRow, maxResults);
            PageOfData<ProductGet> results = productService.getPageOfData(true, pageInfo);
            APIListResponseHeader header = new APIListResponseHeader(
                    APIResponseStatus.FOUND,
                    results.getElements().size() + " record(s) found",
                    results.getOffset(), results.getLimit(), results.getTotalElements());
            return ResponseEntity.ok(new APIListResponse<>(header, results.getElements()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new APIListResponse<>(new APIListResponseHeader(
                            APIResponseStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), firstRow, maxResults, 0), null));
        }
    }

    // Wrapper object cho request body của endpoint /search.
    // Gom filter + thông tin phân trang vào 1 body JSON cho gọn.
    public static class SearchRequest {
        private ProductFilter filter;
        private int firstRow   = 0;
        private int maxResults = 20;
        private String orderBy = "";

        public ProductFilter getFilter()    { return filter; }
        public void setFilter(ProductFilter filter) { this.filter = filter; }

        public int getFirstRow()            { return firstRow; }
        public void setFirstRow(int v)      { this.firstRow = v; }

        public int getMaxResults()          { return maxResults; }
        public void setMaxResults(int v)    { this.maxResults = v; }

        public String getOrderBy()          { return orderBy; }
        public void setOrderBy(String v)    { this.orderBy = v; }
    }
}
