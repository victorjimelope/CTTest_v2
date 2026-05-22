package model.noper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class StringPair {
	
    private String key;
    
    private String value;
    
    
    public StringPair(Integer key, String value) {
    	this(key.toString(), value);
    }
    
    public StringPair(Integer key, Integer value) {
    	this(key.toString(), value.toString());
    }
    
}
