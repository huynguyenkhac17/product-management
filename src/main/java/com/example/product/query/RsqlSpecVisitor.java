package com.example.product.query;

// tất cả các truy vấn đều đưa đệ quy về 3 loại node:
import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.OrNode;

import cz.jirutka.rsql.parser.ast.RSQLVisitor;
import org.springframework.data.jpa.domain.Specification;
import java.util.Set;

// visit theo mỗi loại node, sau đó ghép lại thành các Spec lồng nhau
public class RsqlSpecVisitor<T> implements RSQLVisitor<Specification<T>, Void> {

    private final Set<String> whitelist;

    public  RsqlSpecVisitor(Set<String> whitelist) {
        this.whitelist = whitelist;
    }

    @Override
    public Specification<T> visit(AndNode node, Void param) { // AND logic ';'
        return node.getChildren().stream() // dùng stream + map để đệ quy (Java 8+)
                .map(child -> child.accept(this))
                .reduce(Specification::and) // build cây logic
                .orElse(null);
    }

    @Override
    public Specification<T> visit(OrNode node, Void param) { // OR logic ','
        return node.getChildren().stream()
                   .map(child -> child.accept(this))
                   .reduce(Specification::or)
                   .orElse(null);
    }

    @Override
    public Specification<T> visit(ComparisonNode node, Void param) { // Điều kiện đơn (lá của cây)
        return new RsqlSpecification<>(node, whitelist);
    }
}
