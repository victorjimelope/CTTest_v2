package model.enums.noper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import util.ResUtil;

@Getter @AllArgsConstructor
public enum UserRole {
	
    ADMIN(1, ResUtil.get("label_admin")),
    RESPONSIBLE(2, ResUtil.get("label_responsible")),
    TEACHER(3, ResUtil.get("label_teacher"));
	
	
	private final Integer id;
	private final String title;
	
	
    public static UserRole getById(Integer id) {
    	
        if (id == null) {
            return null;
        }
        
        for (UserRole item : UserRole.values()) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        
        return null;
        
    }
    
}
