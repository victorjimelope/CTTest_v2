package bl.impl;

import java.text.MessageFormat;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bl.StudentAnswerBL;
import dao.GenericHibernateDao;
import dao.StudentAnswerDao;
import exception.ValidationException;
import model.StudentAnswer;
import util.ResUtil;

@Service
public class StudentAnswerBLImpl extends GenericHibernateBLImpl<StudentAnswer, Exception>
	implements StudentAnswerBL {

	@Autowired
    private StudentAnswerDao dao;

	@Override
	protected GenericHibernateDao<StudentAnswer, Exception> getDao() {
		return dao;
	}
	
	
	@Override
	public void create(StudentAnswer entity, Integer loggedUserId) throws Exception {
		
		_checkNullability(entity);
		
		entity.setAddDate(LocalDateTime.now());
		
		dao.create(entity);
		
	}
	
	@Override
	public void update(StudentAnswer entity, Integer loggedUserId) throws Exception {
		
		_checkNullability(entity);
		
		entity.setModDate(LocalDateTime.now());
		
		dao.update(entity);
		
	}
	
	private void _checkNullability(StudentAnswer entity) throws Exception {
		
		if (entity == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_entity_xxx_must_not_be_null"),
					StudentAnswer.class.getSimpleName()));
		}
		
		if (entity.getStudentTestId() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_test")));
		}
		
		if (entity.getQuestionId() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_question")));
		}
		
		if (entity.getAnswerId() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_answer")));
		}
		
	}
	
}
