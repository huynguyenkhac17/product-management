package com.example.product.service;

import com.example.product.dto.LoginRequest;
import com.example.product.dto.LoginResponse;
import com.example.product.dto.RegisterRequest;
import com.example.product.dto.UserDtoGet;
import com.example.product.entity.User;
import vn.saolasoft.base.service.VoidableDtoService;

public interface UserService extends VoidableDtoService<UserDtoGet, User, Long> {
    // VoidableDtoService có sẵn CRUD cơ bản

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

    UserDtoGet register(RegisterRequest req);

    LoginResponse login(LoginRequest req);
}
