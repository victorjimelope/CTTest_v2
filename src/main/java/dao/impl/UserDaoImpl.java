package dao.impl;

import static constants.Constants._EQUALS;
import static constants.Constants._FROM;
import static constants.Constants._LIKE;
import static constants.Constants._ORDER_BY;
import static constants.Constants._WHERE;

import java.text.MessageFormat;
import java.util.List;

import javax.persistence.NoResultException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import dao.UserDao;
import model.LoggedUser;
import model.User;
import model.enums.noper.GenericSortType;
import util.ResUtil;

@Repository
public class UserDaoImpl extends GenericHibernateDaoImpl<User, Exception>
	implements UserDao {
	
	private static final Logger logger = LogManager.getLogger(TestInstanceDaoImpl.class);
	
	public UserDaoImpl() {
		super();
		super.setEntityClass(User.class);
	}
	
	
	@Override @SuppressWarnings("unchecked")
	public List<User> finder(Integer centerId, String name,
			GenericSortType sortType) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			StringBuilder mainQuery = new StringBuilder(
					_FROM + super.getEntityClass().getName());
			
			super.addFilter(mainQuery, "center.id", centerId, _EQUALS);
			
			if (!StringUtils.isBlank(name)) {
				super.addFilter(mainQuery, "name", "'%" + name + "%'", _LIKE);
			}
			
	        if (sortType != null) {
	        	mainQuery.append(_ORDER_BY + sortType.getSort());
	        }
	        
			return getCurrentSession()
					.createQuery(mainQuery.toString())
					.getResultList();
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			e.printStackTrace();
			
			throw new Exception(MessageFormat.format(
					ResUtil.get("label_error_loading_list_of_xxx"),
					super.getEntityClass().getSimpleName()), e);
			
		}
		
	}
	
	@Override
	public User findByUserName(String username) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			String mainQuery = _FROM + super.getEntityClass().getName()
					+ _WHERE + "username" + _EQUALS + "'" + username + "'";
			
			return (User) getCurrentSession()
					.createQuery(mainQuery).getSingleResult(); 
			
		} catch (NoResultException e) {
			
			return null;
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			e.printStackTrace();
			
			throw new Exception(MessageFormat.format(
					ResUtil.get("label_error_loading_xxx"),
					super.getEntityClass().getSimpleName()));
			
		}
		
	}

	@Override
	public LoggedUser getLoggedUser(String username) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			String mainQuery = _FROM + LoggedUser.class.getName()
					+ _WHERE + "username" + _EQUALS + "'" + username + "'";
			
			return (LoggedUser) getCurrentSession()
					.createQuery(mainQuery).getSingleResult(); 
			
		} catch (NoResultException e) {
			
			return null;
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			e.printStackTrace();
			
			throw new Exception(MessageFormat.format(
					ResUtil.get("label_error_loading_xxx"),
					LoggedUser.class.getSimpleName()));
			
		}
		
	}
	
}
