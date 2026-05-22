package model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import model.enums.TestStatus;
import model.enums.TestType;

@Data @EqualsAndHashCode(callSuper = true)
public class TestBase extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer centerId;
	
	private String name;
	
	private String description;
	
	private TestType type;
	
	private TestStatus status;
	
	
	public boolean isActive() {
		return status != null && status.equals(TestStatus.ACTIVE);
	}
	
}
