package dao.impl;

import org.springframework.stereotype.Repository;

import dao.StudentAnswerDao;
import model.StudentAnswer;

@Repository
public class StudentAnswerDaoImpl extends GenericHibernateDaoImpl<StudentAnswer, Exception>
	implements StudentAnswerDao {
	
	public StudentAnswerDaoImpl() {
		super();
		super.setEntityClass(StudentAnswer.class);
	}
	
}
