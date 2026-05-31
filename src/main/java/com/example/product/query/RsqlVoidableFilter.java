package com.example.product.query;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import vn.saolasoft.base.persistence.model.interfaces.VoidableEntity;
import vn.saolasoft.base.service.filter.VoidableFilter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


// Generic <T, Id> hỗ trợ để tái sử dụng
public class RsqlVoidableFilter<T extends VoidableEntity<Id>, Id extends Serializable>
        extends VoidableFilter<T, Id> {
    
    private String query;
    private Set<String> whitelist;

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        Predicate parent = super.toPredicate(root, query, cb); // kế thừa từ VoidableFilter (lọc id + voided)
        if (parent != null) predicates.add(parent);

        Specification<T> spec = Queryable.toSpecification(this.query, whitelist);
        if (spec != null) {
            Predicate rsql = spec.toPredicate(root, query, cb);
            if (rsql != null) predicates.add(rsql);
        }

        return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
    }

    public String getQuery() {return query;}
    public void setQuery(String query) {this.query = query;}

    public Set<String> getWhitelist() {return this.whitelist;}
    public void setWhitelist(Set<String> whitelist) {this.whitelist = whitelist;}
}
