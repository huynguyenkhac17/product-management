package com.example.product.filter;

import com.example.product.entity.Product;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import vn.saolasoft.base.service.filter.VoidableFilter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductFilter extends VoidableFilter<Product, String> {

    // Tìm theo tên hoặc SKU (chứa chuỗi, không phân biệt hoa thường)
    private String keyword;

    // Lọc theo danh mục
    private String categoryId;

    // Lọc theo khoảng giá
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // Framework (SpecBuilder) gọi method này để build câu WHERE động.
    // Mỗi tiêu chí được thêm vào list rồi AND tất cả lại thành 1 điều kiện.
    @Override
    public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();

        // Lấy các predicate từ VoidableFilter cha (lọc ids, lọc voided)
        Predicate parentPredicate = super.toPredicate(root, query, builder);
        if (parentPredicate != null) predicates.add(parentPredicate);

        // WHERE (name LIKE '%keyword%' OR sku LIKE '%keyword%')
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            Predicate byName = builder.like(builder.lower(root.get("name")), pattern);
            Predicate bySku  = builder.like(builder.lower(root.get("sku")),  pattern);
            predicates.add(builder.or(byName, bySku));
        }

        // WHERE category_id = ?
        if (categoryId != null && !categoryId.isBlank()) {
            predicates.add(builder.equal(root.get("categoryId"), categoryId));
        }

        // WHERE price >= minPrice
        if (minPrice != null) {
            predicates.add(builder.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        // WHERE price <= maxPrice
        if (maxPrice != null) {
            predicates.add(builder.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        return predicates.isEmpty() ? null : builder.and(predicates.toArray(new Predicate[0]));
    }

    // ===== Getters & Setters =====

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
}
