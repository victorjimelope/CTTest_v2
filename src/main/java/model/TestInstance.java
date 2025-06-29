package model;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import model.enums.TestInstanceStatus;

@Data @NoArgsConstructor @EqualsAndHashCode(callSuper = true)
public class TestInstance extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer testId;
	
	private StudentGroup studentGroup;
	
	private TestInstanceStatus status;
	
    private User assignedBy;
    
    private LocalDate assignedDate;
	
    private Integer pin;
    
    
    public boolean isClosed() {
    	return status != null && status.equals(TestInstanceStatus.CLOSED);
    }
    
}
