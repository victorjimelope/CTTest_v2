package model;

import java.io.Serializable;

import javax.persistence.Transient;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data @EqualsAndHashCode(callSuper = true) @ToString(exclude = "question")
public class Answer extends AnswerBase implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Question question;
	
	private Integer countTotal;

	@Transient
	private boolean selected;
	
	
	public Answer clone() {
		
		Answer clone = new Answer();
		
		clone.setQuestion(question);
		clone.setOrder(order);
		clone.setImage(image != null ? image.clone() : null);
		clone.setCorrect(correct);
		
		return clone;
		
	}
	
	public Integer getCountTotal() {
		if (countTotal == null) {
			countTotal = 0;
		}
		return countTotal;
	}
	
}
