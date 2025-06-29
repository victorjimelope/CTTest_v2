package model.noper;

import java.util.List;

import lombok.Getter;

@Getter
public class GenericPagination<T> {
	
    private List<T> objectList;
    private int currentPage;
    private int pageSize;
    private int totalPages;
    
    
    public GenericPagination(List<T> objectList, int pageSize) {
        this.objectList = objectList;
        this.pageSize = pageSize;
        this.currentPage = 1;
        this.totalPages = (int) Math.ceil((double) objectList.size() / pageSize);
    }

    public List<T> getCurrentPageList() {
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, objectList.size());
        return objectList.subList(fromIndex, toIndex);
    }
    
    public boolean hasNextPage() {
        return currentPage < totalPages;
    }

    public boolean hasPreviousPage() {
        return currentPage > 1;
    }

    public void nextPage() {
        if (hasNextPage()) {
            currentPage++;
        }
    }

    public void previousPage() {
        if (hasPreviousPage()) {
            currentPage--;
        }
    }
    
}
