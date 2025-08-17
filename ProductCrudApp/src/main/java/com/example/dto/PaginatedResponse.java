package com.example.dto;

import java.util.List;

public class PaginatedResponse<T> {

    private List<T> items;
    private long totalItems;
    private int totalPages;
    private int currentPage;
    private int pageSize;

    public PaginatedResponse() {}

    public PaginatedResponse(List<T> items, long totalItems, int currentPage, int pageSize) {
        this.items = items;
        this.totalItems = totalItems;
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }

    public List<T> getItems() { 
    	return items; 
    }
    
    public void setItems(List<T> items) { 
    	this.items = items; 
    }

    public long getTotalItems() {
    	return totalItems; 
    }
    
    public void setTotalItems(long totalItems) { 
    	this.totalItems = totalItems; 
    }

    public int getTotalPages() { 
    	return totalPages; 
    }
    
    public void setTotalPages(int totalPages) { 
    	this.totalPages = totalPages;
    }

    public int getCurrentPage() { 
    	return currentPage; 
    }
    public void setCurrentPage(int currentPage) { 
    	this.currentPage = currentPage; 
    }

    public int getPageSize() { 
    	return pageSize; 
    }
    
    public void setPageSize(int pageSize) { 
    	this.pageSize = pageSize; 
    }
}

