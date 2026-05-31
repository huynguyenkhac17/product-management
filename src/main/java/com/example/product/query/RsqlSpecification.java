package com.example.product.query;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import vn.saolasoft.base.exception.APIException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

// Parse query thành các node trên cây
// Một node so sánh trong RSQL = một điều kiện WHERE
public class RsqlSpecification<T> implements Specification<T> {

    private final ComparisonNode node;
    private final Set<String> whitelist;   // dùng whitelist giới hạn các fields được phép lọc

    public RsqlSpecification(ComparisonNode node, Set<String> whitelist) {
        this.node = node;
        this.whitelist = whitelist;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        String field = node.getSelector();

        // Chọn field ngoài whitelist -> 400 (code base đang catch trước khi vào service nên sẽ ném 400)
        if (!whitelist.contains(field)) {
            throw new APIException("Field not allowed for query: " + field);
        }

        Path<?> path = root.get(field);
        Class<?> type = path.getJavaType();        // lấy kiểu của cột từ JPA metamodel
        List<String> args = node.getArguments();   // lấy giá trị client gửi (luôn là String)
        String op = node.getOperator().getSymbol();

        switch (op) {
            case "==": {   // equal, nếu có '*' thì LIKE (không phân biệt hoa thường)
                String arg = args.get(0);
                if (arg.contains("*")) {
                    return cb.like(cb.lower(root.get(field).as(String.class)),
                                    arg.replace('*', '%').toLowerCase());
                }
                return cb.equal(path, cast(type, arg));
            }
            case "!=": { // not equal
                String arg = args.get(0);
                if (arg.contains("*")) {
                    return cb.notLike(cb.lower(root.get(field).as(String.class)),
                                    arg.replace('*', '%').toLowerCase());
                }
                return cb.notEqual(path, cast(type, arg));
            }
            // Các phép so sánh cần Comparable (số, ngày). Dùng raw type vì kiểu chỉ biết lúc runtime.
            case "=gt=": case ">":
                return cb.greaterThan((Expression) path, (Comparable) cast(type, args.get(0)));
            case "=ge=": case ">=":
                return cb.greaterThanOrEqualTo((Expression) path, (Comparable) cast(type, args.get(0)));
            case "=lt=": case "<":
                return cb.lessThan((Expression) path, (Comparable) cast(type, args.get(0)));
            case "=le=": case "<=":
                return cb.lessThanOrEqualTo((Expression) path, (Comparable) cast(type, args.get(0)));
            case "=in=": {
                List<Object> values = args.stream().map(a -> cast(type, a)).collect(Collectors.toList());
                return path.in(values);
            }
            case "=out=": {
                List<Object> values = args.stream().map(a -> cast(type, a)).collect(Collectors.toList());
                return cb.not(path.in(values));
            }
            default:
                throw new APIException("Unsupported operator: " + op);
        }
    }

    // Ép chuỗi client gửi sang đúng kiểu (wrapper) của cột. Sai định dạng → ném 400
    private Object cast(Class<?> type, String value) {
        try {
            if (type == String.class)                       return value;
            if (type == BigDecimal.class)                   return new BigDecimal(value);
            if (type == Integer.class || type == int.class) return Integer.valueOf(value);
            if (type == Long.class    || type == long.class) return Long.valueOf(value);
            if (type == Double.class  || type == double.class) return Double.valueOf(value);
            if (type == Boolean.class || type == boolean.class) return Boolean.valueOf(value);
            if (type == LocalDate.class)                    return LocalDate.parse(value);
            if (type == LocalDateTime.class)                return LocalDateTime.parse(value);
            if (type == Instant.class)                      return Instant.parse(value);
            if (type == UUID.class)                         return UUID.fromString(value);
            if (type.isEnum())                              return Enum.valueOf((Class<? extends Enum>) type, value);
            return value;
        } catch (Exception e) {
            throw new APIException("Invalid value '" + value + "' for field '" + node.getSelector() + "'");
        }
    }
}
