package com.example.be_exercise.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDto<T> {
    private List<T> items;
    private Integer page;
    private Integer size;
    private Integer totalPages;
    private Long totalItems;

    // Convert items
    public <R> PageDto<R> map(Function<T, R> mapper) {
        final List<R> items = this.items.stream().map(mapper).toList();
        return new PageDto<>(items, page, size, totalPages, totalItems);
    }

    // Convert Page to PageDto
    public static <R> PageDto<R> of(Page<R> page) {
        return new PageDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements()
        );
    }
}
