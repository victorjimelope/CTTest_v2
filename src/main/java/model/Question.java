package model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Transient;

import org.springframework.util.CollectionUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data @EqualsAndHashCode(callSuper = true) @ToString(exclude = "test")
public class Question extends QuestionBase implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Test test;
	
	private List<Answer> answers = new LinkedList<>();

	@Transient
	private boolean selected;
	
	private Integer countTotal;
	
	private Integer countCorrect;
	
	private Integer countWrong;
	
	
	public Question clone() {
		
		Question clone = new Question();
		clone.setTest(test);
		clone.setOrder(order);
		clone.setQuestion(question);
		clone.setImage(image != null ? image.clone() : null);
		clone.setAnswersLayout(answersLayout);
		
		clone.setAnswers(new LinkedList<>());
		
		for (Answer ans : answers) {
			Answer cloneAns = ans.clone();
			cloneAns.setQuestion(clone);
			clone.getAnswers().add(cloneAns);
		}
		
		return clone;
		
	}
	
	public Integer getCountTotal() {
		if (countTotal == null) {
			countTotal = 0;
		}
		return countTotal;
	}
	
	public Integer getCountCorrect() {
		if (countCorrect == null) {
			_loadCountCorrect();
		}
		return countCorrect;
	}
	
	private void _loadCountCorrect() {
		
		countCorrect = 0;
			
		if (!CollectionUtils.isEmpty(answers)) {
			for (Answer ans : answers) {
				if (ans.isCorrect()) {
					countCorrect = ans.getCountTotal();
					return;
				}
			}
		}
		
	}
	
	public Integer getCountWrong() {
		if (countWrong == null) {
			_loadCountWrong();
		}
		return countWrong;
	}
	
	private void _loadCountWrong() {
		countWrong = getCountTotal() - getCountCorrect();
	}
	
	// TODO Revisar
	public Integer getCorrectAnswerId() {
		
		if (!CollectionUtils.isEmpty(answers)) {
			for (Answer answer : answers) {
				if (answer.isCorrect()) {
					return answer.getId();
				}
			}
			
		}
		
		return null;
		
	}
	
}
