package model.enums.noper;

import static constants.Constants._ASC;
import static constants.Constants._DESC;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import model.noper.StringPair;
import util.ResUtil;

@Getter @AllArgsConstructor
public enum StudentTestSortType {
	
	HIGHEST_SCORE(1, ResUtil.get("label_highest_score"), "score" + _DESC),
    LOWEST_SCORE(2, ResUtil.get("label_lowest_score"), "score" + _ASC),
    A_Z(3, ResUtil.get("label_a_z"), "student.name" + _ASC),
    Z_A(4, ResUtil.get("label_z_a"), "student.name" + _DESC);
	
	
	private final Integer id;
	private final String title;
	private final String sort;
	
    
	public static List<StringPair> getComboList() {
		return Arrays.stream(StudentTestSortType.values())
                .map(x -> new StringPair(x.getId(), x.getTitle()))
                .collect(Collectors.toList());
	}
	
    public static StudentTestSortType getById(Integer id) {
    	
        if (id == null) {
            return null;
        }
        
        for (StudentTestSortType item : StudentTestSortType.values()) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        
        return null;
        
    }
    
}
