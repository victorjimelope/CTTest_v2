package bl.impl;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bl.TestBL;
import dao.GenericHibernateDao;
import dao.TestDao;
import exception.ValidationException;
import model.Question;
import model.Test;
import model.enums.TestStatus;
import model.enums.TestType;
import model.enums.noper.GenericSortType;
import util.ResUtil;

@Service
public class TestBLImpl extends GenericHibernateBLImpl<Test, Exception>
	implements TestBL {

	@Autowired
    private TestDao dao;
	
	@Override
	protected GenericHibernateDao<Test, Exception> getDao() {
		return dao;
	}
	
	
	@Override
	public void create(Test entity, Integer loggedUserId) throws Exception {
		
		_checkNullability(entity);
		
		entity.setAddDate(LocalDateTime.now());
		entity.setAddUser(loggedUserId);
		
		dao.create(entity);
		
	}
	
	@Override
	public void update(Test entity, Integer loggedUserId) throws Exception {
		
		_checkNullability(entity);
		
		entity.setModDate(LocalDateTime.now());
		entity.setModUser(loggedUserId);
		
		dao.update(entity);
		
	}

	private void _checkNullability(Test entity) throws Exception {
		
		if (entity == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_entity_xxx_must_not_be_null"),
					Test.class.getSimpleName()));
		}
		
		if (entity.getCenterId() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_center")));
		}
		
		if (StringUtils.isBlank(entity.getName())) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_name")));
		}
		
		if (entity.getType() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_type")));
		}
		
		if (entity.getStatus() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_status")));
		}
		
	}
	
	@Override
	public List<Test> finder(Integer centerId, String name,
			TestStatus status, GenericSortType sortType) throws Exception {
		return dao.finder(centerId, name, status, sortType);
	}

	@Override
	public List<Test> explore(Integer centerId, String name,
			GenericSortType sortType) throws Exception {
		return dao.explore(centerId, name, sortType);
	}
	
	@Override
	public Integer clone(Integer testId, Integer centerId, String name,
			String description, Integer loggedUserId) throws Exception {
		
		Test test = read(testId);
		
		Test clone = _buildTest(centerId, name, description);
		
		create(clone, loggedUserId);
		
		clone.setQuestions(new LinkedList<>());
		
		for (Question q : test.getQuestions()) {
			Question cloneQtn = q.clone();
			cloneQtn.setTest(clone);
			clone.getQuestions().add(cloneQtn);
		}
		
		update(clone, loggedUserId);
		
		return clone.getId();
		
	}
	
	private Test _buildTest(Integer centerId, String name, String description) {
		
		Test test = new Test();
		test.setCenterId(centerId);
		test.setName(name);
		test.setDescription(description);
		test.setType(TestType.PRIVATE);
		test.setStatus(TestStatus.DRAFT);
		
		return test;
		
	}

	@Override
	public void changeStatus(Integer testId, TestStatus newStatus,
			Integer loggedUserId) throws Exception {
		dao.changeStatus(testId, newStatus, loggedUserId);
	}
	
	@Override
	public void changeType(Integer testId, TestType newType,
			Integer loggedUserId) throws Exception {
		
	}
	
}
