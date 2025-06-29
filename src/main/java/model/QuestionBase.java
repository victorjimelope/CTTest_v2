package model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import model.enums.AnswersLayoutType;

@Data @EqualsAndHashCode(callSuper = true)
public class QuestionBase extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected Integer order;
	
	protected String question;
	
	protected Image image;

	protected AnswersLayoutType answersLayout;
	
}
