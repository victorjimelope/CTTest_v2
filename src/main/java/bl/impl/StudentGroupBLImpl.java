package bl.impl;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bl.StudentGroupBL;
import dao.GenericHibernateDao;
import dao.StudentGroupDao;
import exception.ValidationException;
import model.StudentGroup;
import model.enums.noper.GenericSortType;
import model.noper.StringPair;
import util.ResUtil;

@Service
public class StudentGroupBLImpl extends GenericHibernateBLImpl<StudentGroup, Exception>
	implements StudentGroupBL {

	@Autowired
    private StudentGroupDao dao;

	@Override
	protected GenericHibernateDao<StudentGroup, Exception> getDao() {
		return dao;
	}
	
	
	@Override
	public void create(StudentGroup entity, Integer loggedUserId) throws Exception {
		
		_checkNullability(entity);
		
		entity.setAddDate(LocalDateTime.now());
		entity.setAddUser(loggedUserId);
		
		dao.create(entity);
		
	}
	
	@Override
	public void update(StudentGroup entity, Integer loggedUserId) throws Exception {
		
		_checkNullability(entity);
		
		entity.setModDate(LocalDateTime.now());
		entity.setModUser(loggedUserId);
		
		dao.update(entity);
		
	}

	private void _checkNullability(StudentGroup entity) throws Exception {
		
		if (entity == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_entity_xxx_must_not_be_null"),
					StudentGroup.class.getSimpleName()));
		}
		
		if (entity.getCenter() == null || entity.getCenter().getId() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_center")));
		}
		
		if (StringUtils.isBlank(entity.getName())) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_name")));
		}
		
	}
	
	@Override
	public List<StudentGroup> finder(Integer centerId, String name,
			GenericSortType sortType) throws Exception {
		return dao.finder(centerId, name, sortType);
	}
	
	@Override
	public List<StringPair> getComboList(List<Integer> studentGroupIdList) throws Exception {
		return dao.getComboList(studentGroupIdList);
	}

	@Override
	public List<StringPair> getComboList(Integer centerId) throws Exception {
		return dao.getComboList(centerId);
	}
	
}
