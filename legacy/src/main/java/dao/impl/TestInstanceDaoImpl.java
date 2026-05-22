package dao.impl;

import static constants.Constants.SELECT_COUNT;
import static constants.Constants._EQUALS;
import static constants.Constants._FROM;
import static constants.Constants._SELECT;
import static constants.Constants._WHERE;

import java.text.MessageFormat;
import java.util.List;

import javax.persistence.NoResultException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import dao.TestInstanceDao;
import model.TestInstance;
import util.ResUtil;

@Repository
public class TestInstanceDaoImpl extends GenericHibernateDaoImpl<TestInstance, Exception>
	implements TestInstanceDao {
	
	private static final Logger logger = LogManager.getLogger(TestInstanceDaoImpl.class);
	
	
	public TestInstanceDaoImpl() {
		super();
		super.setEntityClass(TestInstance.class);
	}
	
	
	@Override @SuppressWarnings("unchecked")
	public List<TestInstance> finder(Integer testId) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			StringBuilder mainQuery = new StringBuilder(
					_FROM + super.getEntityClass().getName());
			
			super.addFilter(mainQuery, "testId", testId, _EQUALS);
			
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
	public boolean isPinAvailable(Integer pin) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			String mainQuery = SELECT_COUNT + _FROM
					+ super.getEntityClass().getName()
					+ _WHERE + "pin" + _EQUALS + pin;
			
			Long count = (Long) getCurrentSession()
					.createQuery(mainQuery).getSingleResult(); 
			
			return count == 0;
			
		} catch (Exception e) {
			
			String mess = "Error";
			
			logger.error(mess);
			
			e.printStackTrace();
			
			throw new Exception(mess, e);
			
		}
		
	}

	@Override
	public Integer getIdByPin(Integer pin) throws Exception {

		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			String mainQuery = _SELECT + "id" + _FROM
					+ super.getEntityClass().getName()
					+ _WHERE + "pin" + _EQUALS + pin;
			
			return (Integer) getCurrentSession()
					.createQuery(mainQuery).getSingleResult(); 
			
		} catch (NoResultException e) {
			
			String mess = "Error";
			
			logger.error(mess);
			
			e.printStackTrace();
			
			throw new Exception(mess, e);
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			e.printStackTrace();
			
			throw new Exception(ResUtil.get("label_error_executing_query"), e);
			
		}
		
	}

	@Override
	public Integer getTestId(Integer id) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			String mainQuery = _SELECT + "testId" + _FROM
					+ super.getEntityClass().getName()
					+ _WHERE + "id" + _EQUALS + id;
			
			return (Integer) getCurrentSession()
					.createQuery(mainQuery).getSingleResult(); 
			
		} catch (NoResultException e) {
			
			String mess = "Error";
			
			logger.error(mess);
			
			e.printStackTrace();
			
			throw new Exception(mess, e);
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			e.printStackTrace();
			
			throw new Exception(ResUtil.get("label_error_executing_query"), e);
			
		}
		
	}
	
}
