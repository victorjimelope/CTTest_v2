package model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Transient;

import org.springframework.util.CollectionUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
    private String username;
    
    private String password;

    private boolean admin;
    
    private Center center;
    
    private Set<StudentGroup> studentGroups = new HashSet<>();
    
    @Transient
    private String newPassword;
    
    
    public User(Integer id) {
		this.id = id;
	}
    
    public String getName() {
    	return username;
    }

    public String getStudentGroupsAsString() {
    	
    	if (CollectionUtils.isEmpty(studentGroups)) {
    		return null;
    	}
    	
    	return studentGroups.stream()
    			.map(StudentGroup::getName)
    			.collect(Collectors.joining(", "));
    	
    }
    
}
