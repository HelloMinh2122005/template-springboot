package org.minh.template.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class CreatePageHelper {
    public static Pageable createPageable(int page, int limit, Integer offset, String sortType, String sortBy) {
        Sort.Direction direction = (sortType != null && sortType.equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
        Sort sort = Sort.by(direction, sortField);

        int pageNumber = page - 1;
        if (offset != null) {
            pageNumber = offset / limit;
        }
        return PageRequest.of(pageNumber, limit, sort);
    }

}
