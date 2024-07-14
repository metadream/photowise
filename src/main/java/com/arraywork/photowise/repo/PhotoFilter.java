package com.arraywork.photowise.repo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.arraywork.photowise.entity.PhotoIndex;
import com.arraywork.photowise.enums.MediaType;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Photo Filter
 * @author AiChen
 * @created 2024/07/14
 */
public class PhotoFilter implements Specification<PhotoIndex> {

    private static final long serialVersionUID = -6339982001648186258L;
    private PhotoIndex condition;

    public PhotoFilter(PhotoIndex condition) {
        this.condition = condition;
    }

    @Override
    public Predicate toPredicate(Root<PhotoIndex> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get("isTrashed"), condition.isTrashed()));

        MediaType mediaType = condition.getMediaType();
        if (mediaType != null) {
            predicates.add(cb.equal(root.get("mediaType"), mediaType));
        }

        boolean isFavored = condition.isFavored();
        if (isFavored) {
            predicates.add(cb.equal(root.get("isFavored"), isFavored));
        }

        if (predicates.size() > 0) {
            Predicate[] p = new Predicate[predicates.size()];
            query.where(cb.and(predicates.toArray(p)));
            query.orderBy(cb.desc(root.get("photoTime")));
            return query.getRestriction();
        }
        return null;
    }

}