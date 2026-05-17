package com.example.product.service;

import com.example.product.dto.ProductDtoGet;
import com.example.product.entity.Product;
import vn.saolasoft.base.service.VoidableDtoService;

public interface ProductService extends VoidableDtoService<ProductDtoGet, Product, String> {
    // VoidableDtoService đã khai báo đầy đủ các method:
    //
    // CRUD cơ bản:
    //   getById(id)                        → lấy 1 sản phẩm (bỏ qua đã void)
    //   getById(id, includeVoided)         → lấy 1 sản phẩm (tùy chọn có lấy đã void)
    //   getAll()                           → lấy tất cả chưa void
    //   getPageOfData(pagingInfo)          → lấy có phân trang
    //   createEntry(dto, callerId)         → tạo mới
    //   updateEntry(dto, callerId)         → cập nhật
    //
    // Voidable (soft-delete):
    //   deleteByID(id, purged, callerId)   → purged=false: xóa mềm | purged=true: xóa hẳn
    //   restoreByID(id, callerId)          → khôi phục bản ghi đã xóa mềm
    //
    // Search:
    //   search(filter, pagingInfo)         → tìm kiếm có lọc + phân trang
}
