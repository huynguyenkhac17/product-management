package com.example.product.dto;

import com.example.product.entity.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import vn.saolasoft.base.service.dto.GeneratedIDDtoCreate;

import java.math.BigDecimal;

public class ProductDTOCreate extends GeneratedIDDtoCreate<Product> {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    private String categoryId;

    @PositiveOrZero(message = "Số lượng không được âm")
    private Integer quantity;

    private String imageUrl;
    private String sku;
    private String barcode;

    // Framework gọi method này để tạo Entity từ dữ liệu DTO.
    // Nhiệm vụ: dựng object Product và set các field từ DTO vào.
    @Override
    public Product toEntry() {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategoryId(categoryId);
        product.setQuantity(quantity != null ? quantity : 0);
        product.setImageUrl(imageUrl);
        product.setSku(sku);
        product.setBarcode(barcode);
        return product;
        // Lưu ý: id sẽ do framework tự sinh UUID, KHÔNG set ở đây
        // Audit fields (creator, dateCreated...) cũng do framework tự điền
    }

    // ===== Getters & Setters (cần setter để Jackson deserialize JSON từ request) =====

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
}
