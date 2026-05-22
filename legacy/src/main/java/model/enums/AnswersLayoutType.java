package model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public enum AnswersLayoutType {
	
	COLUMNS(1, "bi bi-layout-three-columns", "col-3"),
    ROWS(2, "bi bi-layout-three-columns rotate-90", "col-lg-12"),
    GRID(3, "bi bi-grid", "col-6");
	
	
	private final Integer id;
    private final String icon;
    private final String colClass;
	
    
    public static AnswersLayoutType getById(Integer id) {
    	
        if (id == null) {
            return null;
        }
        
        for (AnswersLayoutType item : AnswersLayoutType.values()) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        
        return null;
        
    }
    
}
