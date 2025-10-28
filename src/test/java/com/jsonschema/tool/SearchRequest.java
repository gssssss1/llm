package com.jsonschema.tool;

import com.jsonschema.annotations.SchemaDescription;
import com.jsonschema.annotations.SchemaProperty;

@SchemaDescription("Search query parameters")
public class SearchRequest {
    
    @SchemaProperty(required = true, minLength = 1)
    @SchemaDescription("The search query string")
    private String query;
    
    @SchemaProperty(minimum = 1, maximum = 100)
    @SchemaDescription("Maximum number of results to return")
    private Integer limit = 10;
    
    @SchemaProperty(minimum = 0)
    @SchemaDescription("Number of results to skip (for pagination)")
    private Integer offset = 0;
    
    public SearchRequest() {}
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public Integer getLimit() {
        return limit;
    }
    
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    
    public Integer getOffset() {
        return offset;
    }
    
    public void setOffset(Integer offset) {
        this.offset = offset;
    }
}
