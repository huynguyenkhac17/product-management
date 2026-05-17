package com.example.product.controller;

import com.example.product.dto.ProductDTOCreate;
import com.example.product.dto.ProductDTOGet;
import com.example.product.dto.ProductDTOUpdate;
import com.example.product.entity.Product;
import com.example.product.filter.ProductFilter;
import com.example.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AuditableDtoAPIMethod<ProductDTOGet, Product, String> api;
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

    // GET /api/products?firstRow=0&maxResults=20&orderBy=+name
    @GetMapping
    public ResponseEntity<APIListResponse<List<ProductDTOGet>>> getList(
            @RequestParam(defaultValue = "0")   int firstRow,
            @RequestParam(defaultValue = "20")  int maxResults,
            @RequestParam(defaultValue = "")    String orderBy) {

        PaginationInfo pageInfo = new PaginationInfo(firstRow, maxResults, orderBy);
        return api.getList(pageInfo);
    }

    // GET /api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<ProductDTOGet>> getById(@PathVariable String id) {
        return api.getById(id);
    }

    // POST /api/products
    @PostMapping
    public ResponseEntity<APIResponse<String>> create(@Valid @RequestBody ProductDTOCreate dto) {
        return api.create(dto, currentUserId());
    }

    // PUT /api/products
    @PutMapping
    public ResponseEntity<APIResponse<String>> update(@Valid @RequestBody ProductDTOUpdate dto) {
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
        try {
            String restoredId = productService.restoreByID(id, currentUserId());
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

    // POST /api/products/search
    @PostMapping("/search")
    public ResponseEntity<APIListResponse<List<ProductDTOGet>>> search(@RequestBody SearchRequest body) {
        ProductFilter filter = body.getFilter();
        PaginationInfo pageInfo = new PaginationInfo(body.getFirstRow(), body.getMaxResults(), body.getOrderBy());
        return api.search(filter != null ? filter : new ProductFilter(), pageInfo);
    }

    // GET /api/products/voided
    @GetMapping("/voided")
    public ResponseEntity<APIListResponse<List<ProductDTOGet>>> getVoided(
            @RequestParam(defaultValue = "0")   int firstRow,
            @RequestParam(defaultValue = "20")  int maxResults) {

        try {
            PaginationInfo pageInfo = new PaginationInfo(firstRow, maxResults);
            PageOfData<ProductDTOGet> results = productService.getPageOfData(true, pageInfo);
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
