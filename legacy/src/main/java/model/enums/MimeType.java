package model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public enum MimeType {
	
    JPG(1, "image/jpeg"),
    JPEG(2, "image/jpeg"),
    PNG(3, "image/png");
	
	
	private final Integer id;
    private final String type;
    
    
    public static MimeType getById(Integer id) {
    	
        if (id == null) {
            return null;
        }
        
        for (MimeType mimeType : MimeType.values()) {
            if (mimeType.id.equals(id)) {
                return mimeType;
            }
        }
        
        return null;
        
    }
    
    public static MimeType findByType(String type) {
    	
        if (type == null) {
            return null;
        }
        
        for (MimeType mimeType : MimeType.values()) {
            if (mimeType.type.equals(type)) {
                return mimeType;
            }
        }
        
        return null;
        
    }
    
}
