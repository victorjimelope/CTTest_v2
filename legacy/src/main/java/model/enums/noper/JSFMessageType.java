package model.enums.noper;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public enum JSFMessageType {
	
    INFO(0, FacesMessage.SEVERITY_INFO, "bi-check-circle-fill", "success"),
    WARN(1, FacesMessage.SEVERITY_WARN, "bi-exclamation-circle-fill", "warning"),
    ERROR(2, FacesMessage.SEVERITY_ERROR, "bi bi-x-circle-fill", "danger");
	
	
	private final Integer ordinal;
	private final Severity severity;
    private final String icon;
    private final String color;
	
	
    public static JSFMessageType getByOrdinal(Integer ordinal) {
    	
        if (ordinal == null) {
            return null;
        }
        
        for (JSFMessageType item : JSFMessageType.values()) {
            if (item.ordinal.equals(ordinal)) {
                return item;
            }
        }
        
        return null;
        
    }
    
}
