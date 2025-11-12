package com.seven.auth.util;

import lombok.Data;

@Data
public class Pagination {
    private int limit;
    private int offset;
    private String sortField;
    private String sortOrder;

    public Pagination(){
        this.limit = 10;
        this.offset = 0;
    }
}
