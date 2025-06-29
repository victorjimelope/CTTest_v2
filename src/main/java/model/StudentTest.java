package model;

import static constants.Constants.dd_MM_yyyy_HH_mm;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.persistence.Transient;

import org.springframework.util.CollectionUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import model.enums.StudentTestStatus;

@Data @NoArgsConstructor @EqualsAndHashCode(callSuper = true) @ToString(exclude = "studentAnswers")
public class StudentTest extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer testInstanceId;
	
	private Student student;
	
	private StudentTestStatus status;
	
	private LocalDateTime startDate;
	
	private LocalDateTime finishDate;
	
	private BigDecimal score;
	
	private String comment;

	@Transient
	private Map<Integer, StudentAnswer> studentAnswers;
	
	
	public Integer getAnswerId(Integer questionId) {
		
		if (!CollectionUtils.isEmpty(studentAnswers)
				&& questionId != null
				&& studentAnswers.containsKey(questionId)) {
			return studentAnswers.get(questionId).getAnswerId();
		}
		
		return null;
		
	}
	
	public boolean isAssigned() {
		return status != null && status.equals(StudentTestStatus.ASSIGNED);
	}
	
	public boolean isInProgress() {
		return status != null && status.equals(StudentTestStatus.IN_PROGESS);
	}
	
	public boolean isClosed() {
		return status != null && (status.equals(StudentTestStatus.FINISHED)
				|| status.equals(StudentTestStatus.NOT_DONE));
	}

	public String getStartDateAsString() {
		return startDate != null ? startDate.format(
				DateTimeFormatter.ofPattern(dd_MM_yyyy_HH_mm)) : null;
	}
	
	public String getFinishDateAsString() {
		return finishDate != null ? finishDate.format(
				DateTimeFormatter.ofPattern(dd_MM_yyyy_HH_mm)) : null;
	}
	
}
