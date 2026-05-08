package com.example.product.repository;

import com.example.product.entity.Product;
import org.springframework.stereotype.Repository;
import vn.saolasoft.base.persistence.repository.VoidableRepository;

@Repository
public interface ProductRepository extends VoidableRepository<Product, String> {
    // VoidableRepository đã có sẵn tất cả method cần thiết:
    //
    // - findAll(Pageable)                        → lấy tất cả có phân trang
    // - findById(String)                         → tìm theo id (kể cả đã void)
    // - findByIdAndVoidedFalse(String)           → tìm theo id, chỉ lấy chưa void
    // - findAllByVoidedFalse()                   → lấy tất cả chưa void
    // - findAllByVoidedFalse(Pageable)           → lấy tất cả chưa void, có phân trang
    // - findAllByVoidedFalseAndIdIn(Collection)  → lấy nhiều id, chỉ lấy chưa void
    // - existsByIdAndVoidedFalse(String)         → kiểm tra tồn tại và chưa void
    // - save(Product)                            → tạo mới hoặc cập nhật
    // - delete(Product)                          → xóa vật lý
    //
    // Thêm method tùy chỉnh ở đây nếu cần, ví dụ:
    // Optional<Product> findBySkuAndVoidedFalse(String sku);
}
