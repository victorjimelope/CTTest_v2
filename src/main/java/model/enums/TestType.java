package model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import util.ResUtil;

@Getter @AllArgsConstructor
public enum TestType {
	
    PUBLIC(1, ResUtil.get("label_public"), "success"),
    PRIVATE(2, ResUtil.get("label_private"), "primary");
	
	
	private final Integer id;
	private final String title;
	private final String color;
    
    
    public static TestType getById(Integer id) {
    	
        if (id == null) {
            return null;
        }
        
        for (TestType item : TestType.values()) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        
        return null;
        
    }
    
}
