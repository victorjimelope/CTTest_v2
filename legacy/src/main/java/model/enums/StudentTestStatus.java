package model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import model.noper.StringPair;
import util.ResUtil;

@Getter @AllArgsConstructor
public enum StudentTestStatus {
	
    ASSIGNED(1, ResUtil.get("label_assigned"), "secondary"),
    IN_PROGESS(2, ResUtil.get("label_in_progress"), "warning"),
    FINISHED(3, ResUtil.get("label_finished"), "success"),
    NOT_DONE(4, ResUtil.get("label_not_done"), "danger");
    
	
	private final Integer id;
    private final String title;
	private final String color;
	
    
	public static List<StringPair> getComboList() {
		return Arrays.stream(StudentTestStatus.values())
                .map(x -> new StringPair(x.getId(), x.getTitle()))
                .collect(Collectors.toList());
	}
	
    public static StudentTestStatus getById(Integer id) {
    	
        if (id == null) {
            return null;
        }
        
        for (StudentTestStatus item : StudentTestStatus.values()) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        
        return null;
        
    }
    
}
