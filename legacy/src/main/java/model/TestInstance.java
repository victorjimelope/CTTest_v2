package model;

import static constants.Constants.dd_MM_yyyy;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

	/**
	 * Devuelve la fecha de asignación con el formato común de la aplicación.
	 *
	 * @return fecha de asignación formateada, o {@code null} si no está definida
	 */
	public String getAssignedDateAsString() {
		return assignedDate != null ? assignedDate.format(
				DateTimeFormatter.ofPattern(dd_MM_yyyy)) : null;
	}

}
