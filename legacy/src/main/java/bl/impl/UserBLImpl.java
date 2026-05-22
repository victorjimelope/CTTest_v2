package bl.impl;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bl.UserBL;
import dao.GenericHibernateDao;
import dao.UserDao;
import exception.ValidationException;
import model.LoggedUser;
import model.User;
import model.enums.noper.GenericSortType;
import util.PasswordUtil;
import util.ResUtil;

@Service
public class UserBLImpl extends GenericHibernateBLImpl<User, Exception>
	implements UserBL {

	@Autowired
    private UserDao dao;

	@Override
	protected GenericHibernateDao<User, Exception> getDao() {
		return dao;
	}
	
	
	@Override
	public void create(User entity, Integer loggedUserId) throws Exception {
		
		_checkNullability(entity);
		
		//TODO Generalizar
		if (entity.getCenter() != null
				&& entity.getCenter().getId() == null) {
			entity.setCenter(null);
		}
		
		if (!StringUtils.isBlank(entity.getNewPassword())) {
			entity.setPassword(PasswordUtil.encryptPassword(entity.getNewPassword()));
		}
		
		entity.setAddDate(LocalDateTime.now());
		entity.setAddUser(loggedUserId);
		
		dao.create(entity);
		
	}
	
	@Override
	public void update(User entity, Integer loggedUserId) throws Exception {
		
		_checkNullability(entity);
		
		if (!StringUtils.isBlank(entity.getNewPassword())) {
			entity.setPassword(PasswordUtil.encryptPassword(entity.getNewPassword()));
		}
		
		entity.setModDate(LocalDateTime.now());
		entity.setModUser(loggedUserId);
		
		dao.update(entity);
		
	}
	
	private void _checkNullability(User entity) throws Exception {
		
		if (entity == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_entity_xxx_must_not_be_null"),
					User.class.getSimpleName()));
		}
		
		if (StringUtils.isBlank(entity.getUsername())) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_username")));
		}
		
		if (StringUtils.isBlank(entity.getPassword())) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_password")));
		}
		
	}
	
	@Override
	public List<User> finder(Integer centerId, String name,
			GenericSortType sortType) throws Exception {
		return dao.finder(centerId, name, sortType);
	}
	
	@Override
	public User findByUserName(String username) throws Exception {
		return dao.findByUserName(username);
	}

	@Override
	public LoggedUser getLoggedUser(String username) throws Exception {
		return dao.getLoggedUser(username);
	}
	
}
