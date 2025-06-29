package bl.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bl.StudentTestBL;
import bl.TestBL;
import bl.TestInstanceBL;
import cttest.TestSearchBean;
import dao.GenericHibernateDao;
import dao.StudentTestDao;
import exception.ValidationException;
import model.Question;
import model.StudentTest;
import model.Test;
import model.enums.StudentTestStatus;
import model.enums.noper.StudentTestSortType;
import util.ResUtil;

@Service
public class StudentTestBLImpl extends GenericHibernateBLImpl<StudentTest, Exception>
	implements StudentTestBL {

	private static final Logger logger = LogManager.getLogger(TestSearchBean.class);
	
	
	@Autowired
    private StudentTestDao dao;
	
	@Autowired
    private TestInstanceBL serviceTestInstanceBL;
	
	@Autowired
    private TestBL serviceTestBL;
	
	@Override
	protected GenericHibernateDao<StudentTest, Exception> getDao() {
		return dao;
	}
	
	
	@Override
	public void create(StudentTest entity, Integer loggedUserId) throws Exception {
		
		_checkNullability(entity);
		
		entity.setAddDate(LocalDateTime.now());
		entity.setAddUser(loggedUserId);
		
		dao.create(entity);
		
	}
	
	@Override
	public void update(StudentTest entity, Integer loggedUserId) throws Exception {
		
		_checkNullability(entity);
		
		entity.setModDate(LocalDateTime.now());
		entity.setModUser(loggedUserId);
		
		dao.update(entity);
		
	}
	
	private void _checkNullability(StudentTest entity) throws Exception {
		
		if (entity == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_entity_xxx_must_not_be_null"),
					StudentTest.class.getSimpleName()));
		}
		
		if (entity.getTestInstanceId() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_prueba")));
		}
		
		if (entity.getStudent() == null || entity.getStudent().getId() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_student")));
		}
		
		if (entity.getStatus() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_status")));
		}
		
	}
	
	@Override
	public List<StudentTest> finder(Integer testInstanceId, String name,
			StudentTestStatus status, StudentTestSortType sortType) throws Exception {
		return dao.finder(testInstanceId, name, status, sortType);
	}
	
	@Override
	public StudentTest find(Integer testInstanceId, Integer studentIdentifier) throws Exception {
		return dao.find(testInstanceId, studentIdentifier);
	}
	
	@Override
	public boolean isTestInstanceClosed(Integer id) throws Exception {
		return dao.isTestInstanceClosed(id);
	}
	
//	@Override
//	public void updateStatus(Integer id, StudentTestStatus newStatus) throws Exception {
//		dao.updateStatus(id, newStatus);
//	}
	
//	@Override
//	public StudentTestFull getStudentTestFull(Integer id) throws Exception {
//		
//		StudentTest studentTest = read(id);
//		
//		List<QuestionWithStudentAnswer> questions =
//				serviceQuestionBL.getQuestionsWithStudentAnswer(id);
//		
//		return new StudentTestFull(studentTest, questions);
//		
//	}
	
//	@Override
//	public StudentTestFull getStudentTestFullV2(Integer studentTestId) {
//		return dao.getStudentTestFullV2(studentTestId);
//	}
	
//	@Override
//	public List<StudentTestFull> finderStudentTestFull(Integer testInstanceId) throws Exception {
//		
//		List<Integer> studentTestIdList = dao.finderIdList(testInstanceId);
//		
//		if (CollectionUtils.isEmpty(studentTestIdList)) {
//			return new LinkedList<>();
//		}
//		
//		List<StudentTestFull> studentTestFullList = new LinkedList<>();
//		
//		for (Integer id : studentTestIdList) {
//			studentTestFullList.add(getStudentTestFull(id));
//		}
//		
//		return studentTestFullList;
//		
//	}
	
	@Override
	public void startTest(Integer id) throws Exception {
		
		StudentTest studentTest = read(id);
		
		if (studentTest.isInProgress() || studentTest.isClosed()) {
			return;
		}
		
		dao.startTest(id);
		
	}
	
	@Override
	public void finishTest(Integer id) throws Exception {
		
		StudentTest studentTest = read(id);
		
		if (studentTest.isClosed()) {
			return;
		}
		
		StudentTestStatus newStatus = StudentTestStatus.NOT_DONE;
		
		BigDecimal score = null;
		
		if (studentTest.isInProgress()) {
			
			newStatus = StudentTestStatus.FINISHED;
			
			Integer testId = _getTestIdByTestInstanceId(
					studentTest.getTestInstanceId());
			
			Test test = _getTest(testId);
			
			score = _calculateScore(test, studentTest);
			
		}
		
		dao.finishTest(id, newStatus, score);
		
	}
	
	private Integer _getTestIdByTestInstanceId(Integer testInstanceId) throws Exception {
		
		try {
			
			return serviceTestInstanceBL.getTestId(testInstanceId);
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}, Cause: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage(), 
					e.getCause() != null ? e.getCause() : "Unknown");
			
			throw new Exception(e);
			
		}
		
	}
	
	private BigDecimal _calculateScore(Test test,
			StudentTest studentTest) throws Exception {
		
		Integer countCorrect = 0;
		
		for (Question question : test.getQuestions()) {
		
			Integer answerId = studentTest.getAnswerId(question.getId());
			
			if (answerId != null && answerId.equals(question.getCorrectAnswerId())) {
				countCorrect++;
			}
			
		}
		
		BigDecimal score = new BigDecimal(countCorrect)
				.divide(new BigDecimal(test.getQuestions().size()),
						2, RoundingMode.HALF_UP)
				.multiply(new BigDecimal(10));
		
		return score;
		
	}
	
	private Test _getTest(Integer id) throws Exception {
		
		try {
			
			return serviceTestBL.read(id);
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}, Cause: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage(), 
					e.getCause() != null ? e.getCause() : "Unknown");
			
			throw new Exception(e);
			
		}
		
	}
	
//	@Override
//	public void calculateScore() {
//		
//		try {
//		
//			List<StudentTest> studentTestList = findAll();
//			
//			for (StudentTest studentTest : studentTestList) {
//				
//				Integer testId = _getTestIdByTestInstanceId(
//						studentTest.getTestInstanceId());
//				
//				Test test = _getTest(testId);
//				
//				BigDecimal score = _calculateScore(test, studentTest);
//				
//				studentTest.setScore(score);
//				
//				update(studentTest);
//				
//			}
//			
//		} catch (Exception e) {
//			
//			StackTraceElement element = e.getStackTrace()[0];
//			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}, Cause: {}",
//					element.getClassName(), element.getMethodName(), 
//					e.getClass().getName(), e.getMessage(), 
//					e.getCause() != null ? e.getCause() : "Unknown");
//			
//		}
//		
//	}
	
}
