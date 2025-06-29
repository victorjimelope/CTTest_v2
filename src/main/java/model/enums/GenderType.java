package model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import model.noper.StringPair;
import util.ResUtil;

/**
 * Tipo de género siguiendo el estándar ISO/IEC 5218
 * 
 * Ver @link https://es.wikipedia.org/wiki/ISO/IEC_5218
 */
@Getter @AllArgsConstructor
public enum GenderType {
	
    UNKNOWN(0, ResUtil.get("label_unknown")),
    MALE(1, ResUtil.get("label_male")),
    FEMALE(2, ResUtil.get("label_female")),
    NOT_APPLICABLE(9, ResUtil.get("label_not_applicable"));
	
	private final Integer id;
    private final String title;
	
    
    public static List<StringPair> getComboList() {
		return Arrays.stream(GenderType.values())
                .map(x -> new StringPair(x.getId(), x.getTitle()))
                .collect(Collectors.toList());
	}
    
    public static GenderType getById(Integer id) {
    	
        if (id == null) {
            return null;
        }
        
        for (GenderType item : GenderType.values()) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        
        return null;
        
    }
    
}
