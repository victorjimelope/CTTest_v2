package cttest;

import java.io.Serializable;
import java.text.MessageFormat;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bl.StudentAnswerBL;
import bl.StudentTestBL;
import bl.TestBL;
import bl.TestInstanceBL;
import lombok.Getter;
import lombok.Setter;
import model.Question;
import model.StudentAnswer;
import model.StudentTest;
import model.Test;
import model.enums.StudentTestStatus;
import util.AESUtil;
import util.ResUtil;
import util.SpringContextUtil;

@Getter @Setter
@ManagedBean
@ViewScoped
public class StudentTestFormBean implements Serializable {
	
	private static final Logger logger = LogManager.getLogger(StudentTestFormBean.class);
	
	private static final long serialVersionUID = 1L;
	
//	private static final String QUESTIONS_TAB_ID = "TeQtTbId";
//    private static final String TESTS_TAB_ID = "TeTeTbId";
	
	protected TestBL serviceTestBL = 
			new SpringContextUtil<TestBL>()
			.initializeBean(TestBL.class);
	
	protected StudentTestBL serviceStudentTestBL = 
			new SpringContextUtil<StudentTestBL>()
			.initializeBean(StudentTestBL.class);
	
	protected StudentAnswerBL serviceStudentAnswerBL = 
			new SpringContextUtil<StudentAnswerBL>()
			.initializeBean(StudentAnswerBL.class);
	
	protected TestInstanceBL serviceTestInstanceBL = 
			new SpringContextUtil<TestInstanceBL>()
			.initializeBean(TestInstanceBL.class);
	
	private Test currentTest;
	private StudentTest studentTest;
	
	private Question currentQuestion;
	private Integer currentIndex;
	
	private boolean isTestInstanceClosed;
	
	
	@PostConstruct
    public void init() {
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String idEncrypted = facesContext.getExternalContext().getRequestParameterMap().get("id");
		
		try {
			
			Integer id = (idEncrypted != null)
					? Integer.valueOf(AESUtil.decrypt(idEncrypted)) : null;
			
		    if (id != null) {
		    	
		    	 try {
		    		
		    		 _loadStudentTest(id);
		    		 _checkIsTestInstanceClosed(id);
		    		 _loadTest();
		    		 loadQuestion(0);
		    		 
	 	        } catch (Exception e) {
	 	          
	 	        }
		    	
		    }
			
		} catch (Exception e) {
			
			
			
		}
		
    }
	
	private void _loadStudentTest(Integer id) {
		
		try {
			
			studentTest = serviceStudentTestBL.read(id);
			
		} catch (Exception e) {
			
		}
		
	}
	
	private void _checkIsTestInstanceClosed(Integer id) throws Exception {
		
		try {
			
			isTestInstanceClosed = serviceStudentTestBL
					.isTestInstanceClosed(id);
			
		} catch (Exception e) {
			
			throw new Exception();
			
		}
		
	}
	
	private void _loadTest() {
		
		try {
			
			Integer testId = serviceTestInstanceBL
					.getTestId(studentTest.getTestInstanceId());
		
			currentTest = serviceTestBL.read(testId);
			
		} catch (Exception e) {
			
		}
		
		
	}
	
	public void loadQuestion(Integer index) {
		
		if (index == null || index < 0
				|| index >= currentTest.getQuestions().size()) {
			return;
		}
		
		currentIndex = index;
		currentQuestion = currentTest.getQuestions().get(currentIndex);
		
	}
	
	public void handleCurrentQuestion(Integer newAnswerId) {
		
	    if (currentQuestion == null
	    		|| currentIndex == null) {
	        return;
	    }
	    
	    StudentAnswer studentAnswer =
	    		studentTest.getStudentAnswers()
	    				.get(currentQuestion.getId());
	    
	    // Delete
	    if (studentAnswer != null && newAnswerId == null) {
	        _deleteStudentAnswer(studentAnswer.getId());
	        studentTest.getStudentAnswers().remove(currentQuestion.getId());
	        return;
	    }
	    
	    // Update
	    if (studentAnswer != null) {
	        if (!studentAnswer.getAnswerId().equals(newAnswerId)) {
	            studentAnswer.setAnswerId(newAnswerId);
	            _updateStudentAnswer(studentAnswer);
	            studentTest.getStudentAnswers().put(
	            		currentQuestion.getId(), studentAnswer);
	        }
	        return;
	    }
	    
	    // Create
	    if (newAnswerId != null) {
	        StudentAnswer newAnswer = new StudentAnswer();
	        newAnswer.setStudentTestId(studentTest.getId());
	        newAnswer.setQuestionId(currentQuestion.getId());
	        newAnswer.setAnswerId(newAnswerId);
	        _createStudentAnswer(newAnswer);
	        studentTest.getStudentAnswers().put(
            		currentQuestion.getId(), newAnswer);
	    }
	    
	}
	
	private void _updateStudentAnswer(StudentAnswer studentAnswer) {
		
		try {
			
			serviceStudentAnswerBL.update(studentAnswer, null);
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage());
			
			String mess = MessageFormat.format(
					ResUtil.get("label_error_updating_xxx"),
					StudentAnswer.class.getSimpleName());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(), mess));
			
		}
		
	}
	
	private void _createStudentAnswer(StudentAnswer studentAnswer) {
		
		try {
			
			serviceStudentAnswerBL.create(studentAnswer, null);
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage());
			
			String mess = MessageFormat.format(
					ResUtil.get("label_error_creating_xxx"),
					StudentAnswer.class.getSimpleName());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(), mess));
			
		}
		
	}
	
	private void _deleteStudentAnswer(Integer studentAnswerId) {
		
		try {
			
			serviceStudentAnswerBL.delete(studentAnswerId);
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage());
			
			String mess = MessageFormat.format(
					ResUtil.get("label_error_deleting_xxx"),
					StudentAnswer.class.getSimpleName());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(), mess));
			
		}
		
	}
	
	public void startTest() {
		
		if (studentTest.isInProgress() || studentTest.isClosed()) {
			return;
		}
		
		try {
			
			serviceStudentTestBL.startTest(studentTest.getId());
			
			studentTest.setStatus(StudentTestStatus.IN_PROGESS);
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}, Cause: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage(), 
					e.getCause() != null ? e.getCause() : "Unknown");
			
		}
		
	}
	
	public void finishTest() {
		
		if (studentTest.isAssigned() || studentTest.isClosed()) {
			return;
		}
		
		try {
			
			serviceStudentTestBL.finishTest(studentTest.getId());
			
			studentTest.setStatus(StudentTestStatus.FINISHED);
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}, Cause: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage(), 
					e.getCause() != null ? e.getCause() : "Unknown");
			
		}
		
	}
	
	public boolean isAnswerSelected(Integer questionId, Integer answerId) {
		
	    if (questionId == null || answerId == null) {
	        return false;
	    }
	    
	    StudentAnswer studentAnswer =
	    		studentTest.getStudentAnswers().get(questionId);
	    
	    return studentAnswer != null
	    		? answerId.equals(studentAnswer.getAnswerId()) : false;
	    
	}
	
}