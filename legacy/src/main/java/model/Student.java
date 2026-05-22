package model;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import model.enums.GenderType;

@Data @NoArgsConstructor @EqualsAndHashCode(callSuper = true)
public class Student extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private StudentGroup studentGroup;
	
	private Integer identifier;
	
	private String name;
	
	private LocalDate birthDate;
	
	private GenderType gender;
	
	
	public Student(Integer id) {
		this.id = id;
	}
	
	public Integer getGenderId() {
		return gender != null ? gender.getId() : null;
	}
	
	public void setGenderId(Integer id) {
		gender = GenderType.getById(id);
	}
	
}
