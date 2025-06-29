package bl.impl;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import bl.StudentBL;
import bl.StudentTestBL;
import bl.TestInstanceBL;
import dao.GenericHibernateDao;
import dao.TestInstanceDao;
import exception.PinGenerationException;
import exception.ValidationException;
import model.Student;
import model.StudentTest;
import model.TestInstance;
import model.enums.StudentTestStatus;
import model.enums.TestInstanceStatus;
import util.ResUtil;

@Service
public class TestInstanceBLImpl extends GenericHibernateBLImpl<TestInstance, Exception>
	implements TestInstanceBL {

	private static final Integer PIN_MIN_VALUE = 100000;
    private static final Integer PIN_MAX_VALUE = 999999;
    private static final Integer PIN_GEN_MAX_ATTEMPTS = 100;
    
	
	@Autowired
    private TestInstanceDao dao;
	
	@Autowired
    private StudentBL serviceStudentBL;
	
	@Autowired
    private StudentTestBL serviceStudentTestBL;
	
	@Override
	protected GenericHibernateDao<TestInstance, Exception> getDao() {
		return dao;
	}
	
	
	@Override
	public void create(TestInstance entity, Integer loggedUserId) throws Exception {
		
		_checkNullability(entity);
		
		entity.setAddDate(LocalDateTime.now());
		entity.setAddUser(loggedUserId);
		
		dao.create(entity);
		
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void createTestInstance(TestInstance entity, Integer loggedUserId) throws Exception {
		
		entity.setPin(_generatePin());
		
		create(entity, loggedUserId);
		
		List<Student> studentList = serviceStudentBL
				.finder(null, entity.getStudentGroup().getId(), null);
		
		if (CollectionUtils.isEmpty(studentList)) {
			throw new Exception("La clase no tiene alumnos");
		}
		
		Integer testInstanceId = entity.getId();
		
		for (Student st : studentList) {
			
			StudentTest studentTest = new StudentTest();
			studentTest.setTestInstanceId(testInstanceId);
			studentTest.setStudent(st);
			studentTest.setStatus(StudentTestStatus.ASSIGNED);
			
			serviceStudentTestBL.create(studentTest, loggedUserId);
			
		}
		
	}
	
	public Integer _generatePin() throws Exception {
		
		Integer pin;
		Random random = new Random();
		
        Integer nAttempts = 0;
        
	    while (nAttempts < PIN_GEN_MAX_ATTEMPTS) {
	    	
	    	pin = random.nextInt(PIN_MAX_VALUE - PIN_MIN_VALUE + 1) + PIN_MIN_VALUE;
	    	
	        if (dao.isPinAvailable(pin)) {
	            return pin;
	        }
	        
	        nAttempts++;
	        
	    }
	    
	    throw new PinGenerationException(ResUtil.get("label_error_generating_pin"));
	    
    }
	
	@Override
	public void update(TestInstance entity, Integer loggedUserId) throws Exception {
		
		_checkNullability(entity);
		
		entity.setModDate(LocalDateTime.now());
		entity.setModUser(loggedUserId);
		
		dao.update(entity);
		
	}

	private void _checkNullability(TestInstance entity) throws Exception {
		
		if (entity == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_entity_xxx_must_not_be_null"),
					TestInstance.class.getSimpleName()));
		}
		
		if (entity.getTestId() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_test")));
		}
		
		if (entity.getStudentGroup() == null || entity.getStudentGroup().getId() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_student_group")));
		}
		
		if (entity.getStatus() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_status")));
		}
		
		if (entity.getAssignedBy() == null || entity.getAssignedBy().getId() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_user")));
		}
		
		if (entity.getAssignedDate() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_date")));
		}
		
		if (entity.getPin() == null) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_pin")));
		}
		
	}
	
	@Override
	public List<TestInstance> finder(Integer testId) throws Exception {
		return dao.finder(testId);
	}

	@Override
	public Integer getIdByPin(Integer pin) throws Exception {
		return dao.getIdByPin(pin);
	}
	
	@Override
	public Integer getTestId(Integer id) throws Exception {
		return dao.getTestId(id);
	}

	@Override
	public void closeTestInstance(Integer id, Integer loggedUserId) throws Exception {
		
		TestInstance testInstance = dao.read(id);
		
		List<StudentTest> studentTestList = 
				serviceStudentTestBL.finder(testInstance.getId(),
						null, null, null);
		
		for (StudentTest studentTest : studentTestList) {
			serviceStudentTestBL.finishTest(studentTest.getId());
		}
		
		testInstance.setStatus(TestInstanceStatus.CLOSED);
		
		update(testInstance, loggedUserId);
		
	}
	
}
