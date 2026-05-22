package model;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
public class StudentAnswer implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	
	private Integer studentTestId;
	
	private Integer questionId;
	
	private Integer answerId;
	
	private LocalDateTime addDate;
	
	private LocalDateTime modDate;
	
}
