package bl.impl;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bl.CenterBL;
import dao.CenterDao;
import dao.GenericHibernateDao;
import exception.ValidationException;
import model.Center;
import model.enums.noper.GenericSortType;
import model.noper.StringPair;
import util.ResUtil;

@Service
public class CenterBLImpl extends GenericHibernateBLImpl<Center, Exception>
	implements CenterBL {

	@Autowired
    private CenterDao dao;

	@Override
	protected GenericHibernateDao<Center, Exception> getDao() {
		return dao;
	}
	
	
	@Override
	public void create(Center entity, Integer loggedUserId) throws Exception {
		
		_checkNullability(entity);
		
		entity.setAddDate(LocalDateTime.now());
		entity.setAddUser(loggedUserId);
		
		dao.create(entity);
		
	}
	
	@Override
	public void update(Center entity, Integer loggedUserId) throws Exception {
		
		_checkNullability(entity);
		
		entity.setModDate(LocalDateTime.now());
		entity.setModUser(loggedUserId);
		
		dao.update(entity);
		
	}
	
	private void _checkNullability(Center entity) throws Exception {
		
		if (entity == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_entity_xxx_must_not_be_null"),
					Center.class.getSimpleName()));
		}
		
		if (StringUtils.isBlank(entity.getName())) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_name")));
		}
		
	}
	
	@Override
	public List<Center> finder(String name, GenericSortType sortType) throws Exception {
		return dao.finder(name, sortType);
	}
	
	@Override
	public List<StringPair> getComboList() throws Exception {
		return dao.getComboList();
	}
	
}
