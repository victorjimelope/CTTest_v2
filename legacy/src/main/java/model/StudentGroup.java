package model;

import java.io.Serializable;

import javax.persistence.Transient;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @EqualsAndHashCode(callSuper = true)
public class StudentGroup extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Center center;
	
	private String name;
	
	@Transient
	private boolean selected;
	
	
	public StudentGroup(Integer id) {
		this.id = id;
	}
	
}
