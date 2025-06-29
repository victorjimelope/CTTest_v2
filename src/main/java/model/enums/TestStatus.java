package model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import model.noper.StringPair;
import util.ResUtil;

@Getter @AllArgsConstructor
public enum TestStatus {
	
    DRAFT(1, ResUtil.get("label_draft"), "warning"),
    ACTIVE(2, ResUtil.get("label_active"), "success"),
    LOCKED(3, ResUtil.get("label_locked"), "danger");
	
	
	private final Integer id;
	private final String title;
	private final String color;
    
	
	public static List<StringPair> getComboList() {
		return Arrays.stream(TestStatus.values())
                .map(x -> new StringPair(x.getId(), x.getTitle()))
                .collect(Collectors.toList());
	}
	
    public static TestStatus getById(Integer id) {
    	
        if (id == null) {
            return null;
        }
        
        for (TestStatus item : TestStatus.values()) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        
        return null;
        
    }
    
}
