package com.example.product.filter;

import com.example.product.entity.Product;
import com.example.product.query.RsqlVoidableFilter;

import java.util.Set;

public class ProductFilter extends RsqlVoidableFilter<Product, String> {

    // Chỉ được lọc động (RSQL) trên những field này.
    private static final Set<String> QUERYABLE = Set.of(
            "name", "sku", "price", "quantity", "categoryId", "barcode", "description"
    );

    public ProductFilter() {
        setWhitelist(QUERYABLE);
    }
}
