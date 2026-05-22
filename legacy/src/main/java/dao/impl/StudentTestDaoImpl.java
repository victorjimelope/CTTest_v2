package dao.impl;

import static constants.Constants._AND;
import static constants.Constants._EQUALS;
import static constants.Constants._FROM;
import static constants.Constants._LEFT_JOIN;
import static constants.Constants._LIKE;
import static constants.Constants._ORDER_BY;
import static constants.Constants._SELECT;
import static constants.Constants._SET;
import static constants.Constants._UPDATE;
import static constants.Constants._WHERE;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.NoResultException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import dao.StudentTestDao;
import model.StudentTest;
import model.TestInstance;
import model.enums.StudentTestStatus;
import model.enums.TestInstanceStatus;
import model.enums.noper.StudentTestSortType;
import util.ResUtil;

@Repository
public class StudentTestDaoImpl extends GenericHibernateDaoImpl<StudentTest, Exception>
	implements StudentTestDao {
	
	private static final Logger logger = LogManager.getLogger(StudentTestDaoImpl.class);
	
	
	public StudentTestDaoImpl() {
		super();
		super.setEntityClass(StudentTest.class);
	}
	
	@Override @SuppressWarnings("unchecked")
	public List<StudentTest> finder(Integer testInstanceId, String name,
			StudentTestStatus status, StudentTestSortType sortType) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			StringBuilder mainQuery = new StringBuilder(
					_FROM + super.getEntityClass().getName());
			
			super.addFilter(mainQuery, "testInstanceId", testInstanceId, _EQUALS);
			
			if (!StringUtils.isBlank(name)) {
				super.addFilter(mainQuery, "student.name", "'%" + name + "%'", _LIKE);
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
	
	@Override
	public StudentTest find(Integer testInstanceId, Integer studentIdentifier) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			String mainQuery = _FROM + super.getEntityClass().getName()
					+ _WHERE + "testInstanceId" + _EQUALS + testInstanceId
					+ _AND + "student.identifier" + _EQUALS + studentIdentifier;
   		 
			return (StudentTest) getCurrentSession()
					.createQuery(mainQuery)
					.getSingleResult();
			
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
			
			throw new Exception(MessageFormat.format(
					ResUtil.get("label_error_loading_xxx"),
					super.getEntityClass().getSimpleName()));
			
		}
		
	}
	
	@Override
	public boolean isTestInstanceClosed(Integer id) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		TestInstanceStatus returnValue = null;
		
		try {
			
			String mainQuery = _SELECT + "ti.status" + _FROM
					+ super.getEntityClass().getName() + " st "
					+ _LEFT_JOIN + TestInstance.class.getName()
					+ " ti ON ti.id" + _EQUALS + "st.testInstanceId"
					+ _WHERE + "st.id" + _EQUALS + id;
			
			returnValue = (TestInstanceStatus) getCurrentSession()
					.createQuery(mainQuery)
					.getSingleResult();
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			e.printStackTrace();
			
			throw new Exception(ResUtil.get("label_error_executing_query"), e);
			
		}
		
		return returnValue.equals(TestInstanceStatus.CLOSED);
		
	}
	
//	@Override
//	public StudentTestFull getStudentTestFullV2(Integer studentTestId) {
//		
//		try {
//			
//			List<Object[]> result = getCurrentSession().createQuery(
//			        "SELECT DISTINCT q, sa, st FROM StudentTest st " +
//			        "JOIN Question q WITH q.test.id = st.testInstanceId " +  // Use WITH instead of ON
//			        "LEFT JOIN StudentAnswer sa WITH sa.question.id = q.id AND sa.studentTest.id = st.id " +
//			        "WHERE st.id = :studentTestId", Object[].class)
//			        .setParameter("studentTestId", studentTestId)
//			        .list();
//
//
//			
//			StudentTest studentTest = null;
//			List<QuestionWithStudentAnswer> questionList = new ArrayList<>();
//
//			for (Object[] row : result) {
//			    Question question = (Question) row[0];
//			    StudentAnswer studentAnswer = (StudentAnswer) row[1]; // May be null
//			    studentTest = (StudentTest) row[2]; // Only assigned once
//
//			    questionList.add(new QuestionWithStudentAnswer(question, studentAnswer));
//			}
//
//			// Ensure we return a valid object even if no data is found
//			return (studentTest != null) ? new StudentTestFull(studentTest, questionList) : null;
//			
//		} catch (Exception e) {
//			
//			String mess = "Error";
//			
//			logger.error(mess);
//			
//			e.printStackTrace();
//			
//			return null;
//			
//		}
//		
//	}

	@Override
	public void startTest(Integer id) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			String mainQuery = _UPDATE + super.getEntityClass().getName()
					+ _SET + "status = :newStatus, "
					+ "startDate = :startDate"
					+ _WHERE + "id = :id"; 
			
			getCurrentSession().createQuery(mainQuery)
					.setParameter("newStatus", StudentTestStatus.IN_PROGESS)
					.setParameter("startDate", LocalDateTime.now())
					.setParameter("id", id)
					.executeUpdate();
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			e.printStackTrace();
			
			throw new Exception(MessageFormat.format(
					ResUtil.get("label_error_updating_xxx"),
					super.getEntityClass().getSimpleName()), e);
			
		}
		
	}
	
	@Override
	public void finishTest(Integer id, StudentTestStatus newStatus,
			BigDecimal score) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			String mainQuery = _UPDATE + super.getEntityClass().getName()
					+ _SET + "status = :newStatus, "
					+ "finishDate = :finishDate, "
					+ "score = :score"
					+ _WHERE + "id = :id";
			
			getCurrentSession().createQuery(mainQuery)
					.setParameter("newStatus", newStatus)
					.setParameter("finishDate", LocalDateTime.now())
					.setParameter("score", score)
					.setParameter("id", id)
					.executeUpdate();
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			e.printStackTrace();
			
			throw new Exception(MessageFormat.format(
					ResUtil.get("label_error_updating_xxx"),
					super.getEntityClass().getSimpleName()), e);
			
		}
		
	}
	
}
