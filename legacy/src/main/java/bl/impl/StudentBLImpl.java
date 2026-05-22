package bl.impl;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bl.StudentBL;
import dao.GenericHibernateDao;
import dao.StudentDao;
import exception.ValidationException;
import model.Student;
import model.enums.noper.GenericSortType;
import util.ResUtil;

@Service
public class StudentBLImpl extends GenericHibernateBLImpl<Student, Exception>
	implements StudentBL {

	@Autowired
    private StudentDao dao;

	@Override
	protected GenericHibernateDao<Student, Exception> getDao() {
		return dao;
	}
	
	
	@Override
	public void create(Student entity, Integer loggedUserId) throws Exception {
		
		_checkNullability(entity);
		
		entity.setAddDate(LocalDateTime.now());
		entity.setAddUser(loggedUserId);
		
		dao.create(entity);
		
	}
	
	@Override
	public void update(Student entity, Integer loggedUserId) throws Exception {
		
		_checkNullability(entity);
		
		entity.setModDate(LocalDateTime.now());
		entity.setModUser(loggedUserId);
		
		dao.update(entity);
		
	}

	private void _checkNullability(Student entity) throws Exception {
		
		if (entity == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_entity_xxx_must_not_be_null"),
					Student.class.getSimpleName()));
		}
		
		if (entity.getStudentGroup() == null
				|| entity.getStudentGroup().getId() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_class")));
		}
		
		if (entity.getIdentifier() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_student_id")));
		}
		
		if (StringUtils.isBlank(entity.getName())) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_name")));
		}
	
		if (entity.getBirthDate() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_birth_date")));
		}
		
		if (entity.getGender() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_gender")));
		}
		
	}
	
	@Override
	public List<Student> finder(String name, Integer studentGroupId,
			GenericSortType sortType) throws Exception {
		return dao.finder(name, studentGroupId, sortType);
	}
	
}
