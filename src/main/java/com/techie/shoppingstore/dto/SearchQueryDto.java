package com.techie.shoppingstore.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SearchQueryDto {
    private String textQuery;
    private List<Filter> filters;

    @Data
    public static class Filter implements Serializable {
        private String key;
        private String value;
        private String from;
        private String to;
    }
}
