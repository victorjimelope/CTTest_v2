package model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
public class LoggedUser implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	
    private String username;
    
    private boolean admin;
    
    //private UserRole role;
    
    private Integer centerId;
    
    private List<Integer> studentGroupIdList;
    
}
