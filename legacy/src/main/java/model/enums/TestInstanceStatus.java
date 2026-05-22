package model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import model.noper.StringPair;
import util.ResUtil;

@Getter @AllArgsConstructor
public enum TestInstanceStatus {
	
    IN_PROGRESS(1, ResUtil.get("label_in_progress"), "warning"),
    CLOSED(2, ResUtil.get("label_closed"), "danger");
	
	
	private final Integer id;
	private final String title;
	private final String color;
    
	
	public static List<StringPair> getComboList() {
		return Arrays.stream(TestInstanceStatus.values())
                .map(x -> new StringPair(x.getId(), x.getTitle()))
                .collect(Collectors.toList());
	}
	
    public static TestInstanceStatus getById(Integer id) {
    	
        if (id == null) {
            return null;
        }
        
        for (TestInstanceStatus item : TestInstanceStatus.values()) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        
        return null;
        
    }
    
}
