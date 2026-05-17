package com.example.product.dto;

import com.example.product.entity.Product;
import vn.saolasoft.base.service.dto.DtoGet;

import java.math.BigDecimal;
// import java.util.Date;

public class ProductDTOGet extends DtoGet<Product, String> {

    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String categoryId;
    private Integer quantity;
    private String imageUrl;
    private String sku;
    private String barcode;

    // Các field audit từ framework (tự có trong entity nhờ kế thừa)
    // private Boolean voided;
    // private Date dateCreated;
    // private Date dateUpdated;

    public ProductDTOGet() {}

    public ProductDTOGet(Product product) {
        super(product); // trigger parse()
    }

    // Framework gọi method này sau super(product).
    // để copy dữ liệu từ Entity sang DTO để trả về client. (hard separate luôn mà)
    @Override
    public void parse(Product product) {
        this.id          = product.getId();
        this.name        = product.getName();
        this.description = product.getDescription();
        this.price       = product.getPrice();
        this.categoryId  = product.getCategoryId();
        this.quantity    = product.getQuantity();
        this.imageUrl    = product.getImageUrl();
        this.sku         = product.getSku();
        this.barcode     = product.getBarcode();
        // this.voided      = product.getVoided();
        // this.dateCreated = product.getDateCreated();
        // this.dateUpdated = product.getDateUpdated();
    }

    // ===== Getters =====

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getCategoryId() { return categoryId; }
    public Integer getQuantity() { return quantity; }
    public String getImageUrl() { return imageUrl; }
    public String getSku() { return sku; }
    public String getBarcode() { return barcode; }
    // public Boolean getVoided() { return voided; }
    // public Date getDateCreated() { return dateCreated; }
    // public Date getDateUpdated() { return dateUpdated; }
}
