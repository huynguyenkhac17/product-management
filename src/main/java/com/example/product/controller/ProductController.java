package com.example.product.controller;

import com.example.product.dto.ProductDtoCreate;
import com.example.product.dto.ProductDtoGet;
import com.example.product.dto.ProductDtoUpdate;
import com.example.product.entity.Product;
import com.example.product.filter.ProductFilter;
import com.example.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.saolasoft.base.api.method.AuditableDtoAPIMethod;
import vn.saolasoft.base.api.response.*;
import vn.saolasoft.base.service.filter.PageOfData;
import vn.saolasoft.base.service.filter.PaginationInfo;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    // kiểm tra role từ context holder
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    // AuditableDtoAPIMethod là lớp helper của framework: gói sẵn try-catch, log, build response chuẩn.
    private final AuditableDtoAPIMethod<ProductDtoGet, Product, String> api;
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
        this.api = new AuditableDtoAPIMethod<>(productService);
    }

    // Lấy userId hiện tại từ JWT (đã được JwtAuthFilter set principal = Long).
    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long id) return id;
        throw new IllegalStateException("No authenticated user in context");
    }

    @GetMapping("/search")
    public ResponseEntity<APIListResponse<List<ProductDtoGet>>> search(
                @RequestParam(defaultValue = "") String query,
                @RequestParam(required = false) Boolean voided,
                @RequestParam(defaultValue = "0") int firstRow,
                @RequestParam(defaultValue = "20") int maxResults,
                @RequestParam(defaultValue = "") String orderBy) {
        ProductFilter filter = new ProductFilter();
        filter.setQuery(query);

        if (isAdmin()) {
            filter.setVoided(voided); // admin có thể lọc cả voided và unvoided
        } else {
            filter.setVoided(false); // user chỉ thấy unvoided
        }

        PaginationInfo pageInfo = new PaginationInfo(firstRow, maxResults, orderBy);
        return api.search(filter, pageInfo);
    }

    // GET /api/products?firstRow=0&maxResults=20&orderBy=+name
    @GetMapping
    public ResponseEntity<APIListResponse<List<ProductDtoGet>>> getList(
            @RequestParam(defaultValue = "0")   int firstRow,
            @RequestParam(defaultValue = "20")  int maxResults,
            @RequestParam(defaultValue = "")    String orderBy) {

        PaginationInfo pageInfo = new PaginationInfo(firstRow, maxResults, orderBy);
        return api.getList(pageInfo);
    }

    // GET /api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<ProductDtoGet>> getById(@PathVariable String id) {
        return api.getById(id);
    }

    // POST /api/products
    @PostMapping
    public ResponseEntity<APIResponse<String>> create(@Valid @RequestBody ProductDtoCreate dto) {
        return api.create(dto, currentUserId());
    }

    // PUT /api/products
    @PutMapping
    public ResponseEntity<APIResponse<String>> update(@Valid @RequestBody ProductDtoUpdate dto) {
        return api.update(dto, currentUserId());
    }

    // DELETE /api/products/{id}  — xóa mềm (voided = true)
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> delete(@PathVariable String id) {
        return api.delete(id, currentUserId());
    }

    // POST /api/products/{id}/restore  — khôi phục bản ghi đã xóa mềm
    @PostMapping("/{id}/restore")
    public ResponseEntity<APIResponse<String>> restore(@PathVariable String id) {
        String restoredId = productService.restoreByID(id, currentUserId());
        return ResponseEntity.ok(new APIResponse<>(
                new APIResponseHeader(APIResponseStatus.UNVOIDED, "Product restored"),
                restoredId));
    }

    // GET /api/products/voided
    @PreAuthorize("hasRole('ADMIN')") // chỉ admin mới được xem bản ghi đã xóa mềm
    @GetMapping("/voided")
    public ResponseEntity<APIListResponse<List<ProductDtoGet>>> getVoided(
            @RequestParam(defaultValue = "0")   int firstRow,
            @RequestParam(defaultValue = "20")  int maxResults) {

        PaginationInfo pageInfo = new PaginationInfo(firstRow, maxResults);
        PageOfData<ProductDtoGet> results = productService.getPageOfData(true, pageInfo);
        APIListResponseHeader header = new APIListResponseHeader(
                APIResponseStatus.FOUND,
                results.getElements().size() + " record(s) found",
                results.getOffset(), results.getLimit(), results.getTotalElements());
        return ResponseEntity.ok(new APIListResponse<>(header, results.getElements()));
    }

}
