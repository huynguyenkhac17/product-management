package com.example.product.dto;

import com.example.product.entity.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import vn.saolasoft.base.service.dto.DtoUpdate;

import java.math.BigDecimal;
import java.util.Objects;

public class ProductDtoUpdate extends DtoUpdate<Product, String> {

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

    // Framework gọi method này để áp thay đổi lên entity đang có trong DB.
    // Trả về true  → có thay đổi → framework sẽ gọi repository.save()
    // Trả về false → không đổi gì → framework bỏ qua, không save (tránh UPDATE thừa)
    @Override
    public boolean apply(Product product) {
        boolean changed = false;

        if (!Objects.equals(product.getName(), name)) {
            product.setName(name);
            changed = true;
        }
        if (!Objects.equals(product.getDescription(), description)) {
            product.setDescription(description);
            changed = true;
        }
        if (!Objects.equals(product.getPrice(), price)) {
            product.setPrice(price);
            changed = true;
        }
        if (!Objects.equals(product.getCategoryId(), categoryId)) {
            product.setCategoryId(categoryId);
            changed = true;
        }
        if (!Objects.equals(product.getQuantity(), quantity)) {
            product.setQuantity(quantity);
            changed = true;
        }
        if (!Objects.equals(product.getImageUrl(), imageUrl)) {
            product.setImageUrl(imageUrl);
            changed = true;
        }
        if (!Objects.equals(product.getSku(), sku)) {
            product.setSku(sku);
            changed = true;
        }
        if (!Objects.equals(product.getBarcode(), barcode)) {
            product.setBarcode(barcode);
            changed = true;
        }

        return changed;
    }

    // ===== Getters & Setters =====
    // getId() / setId() đã có sẵn trong DtoUpdate cha, không cần khai báo lại

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
