package dao.impl;

import static constants.Constants._AND;
import static constants.Constants._EQUALS;
import static constants.Constants._FROM;
import static constants.Constants._LIKE;
import static constants.Constants._NOT_EQUALS;
import static constants.Constants._OR;
import static constants.Constants._ORDER_BY;
import static constants.Constants._SET;
import static constants.Constants._UPDATE;
import static constants.Constants._WHERE;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import dao.TestDao;
import model.Test;
import model.enums.StudentTestStatus;
import model.enums.TestStatus;
import model.enums.TestType;
import model.enums.noper.GenericSortType;
import util.ResUtil;

@Repository
public class TestDaoImpl extends GenericHibernateDaoImpl<Test, Exception>
	implements TestDao {
	
	private static final Logger logger = LogManager.getLogger(TestDaoImpl.class);
	
	
	public TestDaoImpl() {
		super();
		super.setEntityClass(Test.class);
	}
	
	
	@Override @SuppressWarnings("unchecked")
	public List<Test> finder(Integer centerId, String name,
			TestStatus status, GenericSortType sortType) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			StringBuilder mainQuery = new StringBuilder(
					_FROM + super.getEntityClass().getName());
			
			super.addFilter(mainQuery, "centerId", centerId, _EQUALS);
			
			if (!StringUtils.isBlank(name)) {
				super.addFilter(mainQuery, "name", "'%" + name + "%'", _LIKE);
			}
			
	        if (status != null) {
	        	super.addFilter(mainQuery, "status", status.getId(), _EQUALS);
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
	
	@Override @SuppressWarnings("unchecked")
	public List<Test> explore(Integer centerId, String name,
			GenericSortType sortType) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			StringBuilder mainQuery = new StringBuilder(
					_FROM + super.getEntityClass().getName());
			
			if (centerId != null) {
				mainQuery.append(mainQuery.toString().contains(_WHERE) ? _AND : _WHERE)
	        		.append("(").append("centerId").append(_NOT_EQUALS).append(centerId)
	        		.append(_OR).append("centerId IS NULL").append(")");
			}
			
			if (!StringUtils.isBlank(name)) {
				super.addFilter(mainQuery, "name", "'%" + name + "%'", _LIKE);
			}
			
			super.addFilter(mainQuery, "type", TestType.PUBLIC.getId(), _EQUALS);
	       
	        super.addFilter(mainQuery, "status", TestStatus.ACTIVE.getId(), _EQUALS);
	        
	        
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
	public void changeStatus(Integer testId, TestStatus newStatus,
			Integer loggedUserId) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			String mainQuery = _UPDATE + super.getEntityClass().getName()
					+ _SET + "status = :newStatus, "
					+ "modDate = :modDate, "
					+ "modUser = :modUser"
					+ _WHERE + "id = :id"; 
			
			getCurrentSession().createQuery(mainQuery)
					.setParameter("newStatus", newStatus)
					.setParameter("modDate", LocalDateTime.now())
					.setParameter("modUser", loggedUserId)
					.setParameter("id", testId)
					.executeUpdate();
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			e.printStackTrace();
			
			throw new Exception(MessageFormat.format(
					ResUtil.get("label_error_updating_xxx_with_params_yyy"),
					super.getEntityClass().getSimpleName(),
					"testId: " + (testId != null ? testId.toString() : "null")
					+ ", newStatus: " + (newStatus != null ? newStatus.getTitle() : "null")), e);
			
		}
		
	}
	
	@Override
	public void changeType(Integer testId, TestType newType,
			Integer loggedUserId) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			String mainQuery = _UPDATE + super.getEntityClass().getName()
					+ _SET + "type" + _EQUALS + newType + ", "
					+ "modDate" + _EQUALS + LocalDateTime.now() + ", "
					+ "modUser" + _EQUALS + loggedUserId
					+ _WHERE + "id" + _EQUALS + testId; 
			
			getCurrentSession().createQuery(mainQuery).executeUpdate();
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			e.printStackTrace();
			
			throw new Exception(MessageFormat.format(
					ResUtil.get("label_error_updating_xxx_with_params_yyy"),
					super.getEntityClass().getSimpleName(),
					"testId: " + (testId != null ? testId.toString() : "null")
					+ "newType: " + (newType != null ? newType.getTitle() : "null")), e);
			
		}
		
	}
	
}
