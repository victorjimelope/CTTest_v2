package model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true)
public class AnswerBase extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected Integer order;
	
	protected Image image;
	
	protected boolean correct;
	
}
