package cttest;

import static constants.Constants.PAGINATION_SIZE;
import static constants.NavigationConstants.FACES_REDIRECT_TRUE;
import static constants.NavigationConstants.TEST_SEARCH_URI;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bl.StudentGroupBL;
import bl.StudentTestBL;
import bl.TestBL;
import bl.TestInstanceBL;
import exception.InvalidMimeTypeException;
import lombok.Getter;
import lombok.Setter;
import model.Answer;
import model.Image;
import model.Question;
import model.StudentGroup;
import model.Test;
import model.TestInstance;
import model.User;
import model.enums.AnswersLayoutType;
import model.enums.MimeType;
import model.enums.TestInstanceStatus;
import model.noper.GenericPagination;
import model.noper.StringPair;
import util.ResUtil;
import util.SpringContextUtil;

@Getter @Setter
@ManagedBean
@ViewScoped
public class TestFormBean extends BaseManagedBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final Logger logger = LogManager.getLogger(TestFormBean.class);

    private static final String QUESTIONS_TAB_ID = "TeQtTbId";
    private static final String TESTS_TAB_ID = "TeTeTbId";
    
    protected TestBL serviceTestBL = 
			new SpringContextUtil<TestBL>()
			.initializeBean(TestBL.class);
    
    protected TestInstanceBL serviceTestInstanceBL = 
			new SpringContextUtil<TestInstanceBL>()
			.initializeBean(TestInstanceBL.class);
    
    protected StudentGroupBL serviceStudentGroupBL = 
			new SpringContextUtil<StudentGroupBL>()
			.initializeBean(StudentGroupBL.class);
    
    protected StudentTestBL serviceStudentTestBL = 
			new SpringContextUtil<StudentTestBL>()
			.initializeBean(StudentTestBL.class);
    
    private List<StringPair> tabList;
    
    private String currentTab;
    
    private Test currentObject;
    
    private GenericPagination<TestInstance> testInstancePagination;
    
    private List<StringPair> classComboList;
    
    
    public TestFormBean() {
    	super();
    }
    
    @PostConstruct
    public void init() {
    	
    	try {
    		
    		Integer id = super.getIdParam();

            _load(id);

//            super.checkLoggedUserAuthorization(currentObject.getCenterId());

            if (super.isShowErrorPage()) {
                return;
            }

            currentTab = QUESTIONS_TAB_ID;

        } catch (Exception e) {

        	super.setShowErrorPage(true);
        	
        	StackTraceElement element = e.getStackTrace()[0];
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}, Cause: {}",
	                    element.getClassName(), element.getMethodName(), 
	                    e.getClass().getName(), e.getMessage(), 
	                    e.getCause() != null ? e.getCause() : "Unknown");
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(),
							e.getMessage()));
			
        }
    	
    }
	
	public String save() {
		
		try {

			if (currentObject.getId() == null) {
				_create();
			} else {
				_update();
			}
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(),
							e.getMessage()));
			
		}
		
		return TEST_SEARCH_URI + FACES_REDIRECT_TRUE;
		
	}
	
	public void _create() {
		
		try {
			
			serviceTestBL.create(currentObject, super.getLoggedUserId());
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(),
							e.getMessage()));
			
		}
		
	}
	
	public void _update() {
		
		try {
			
			serviceTestBL.update(currentObject, super.getLoggedUserId());
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(),
							e.getMessage()));
			
		}
		
	}
	
	private void _load(Integer id) throws Exception {
		
		try {
			
			currentObject = serviceTestBL.read(id);
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
				logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}, Cause: {}",
		                    element.getClassName(), element.getMethodName(), 
		                    e.getClass().getName(), e.getMessage(), 
		                    e.getCause() != null ? e.getCause() : "Unknown");
			
			throw new Exception(e.getMessage(), e);
				
		}
		
	}
	
	public void addNewQuestion() {
		
		Question newQ = new Question();
        newQ.setTest(currentObject);
        newQ.setOrder(currentObject.getQuestions().size());
        newQ.setSelected(true);
    	newQ.setQuestion(ResUtil.get("label_question")
    			+ " " + (newQ.getOrder() + 1));
    	newQ.setAnswersLayout(AnswersLayoutType.COLUMNS);
    	
        currentObject.getQuestions().add(newQ);
		
	}

	public void duplicateQuestion(Integer index) {
		
		if (index == null || index < 0
				|| index >= currentObject.getQuestions().size()) {
			return;
		}
		
		Question q = currentObject.getQuestions().get(index);
		
		Integer newQIndex = index + 1;
	    Question newQ = q.clone();
	    newQ.setOrder(newQIndex);
        newQ.setSelected(true);
        
	    currentObject.getQuestions().add(newQIndex, newQ);
	    
	}
	
	public void deleteQuestion(Integer index) {
		
		if (index == null || index < 0
				|| index >= currentObject.getQuestions().size()) {
			return;
		}
		
		currentObject.getQuestions().remove((int) index);
		
		int i = 1;
	    for (Question q : currentObject.getQuestions()) {
	        q.setOrder(i++);
	    }
	    
	}
	
	public void moveQuestionUp(Integer index) {
		
		if (index == null || index <= 0
				|| index >= currentObject.getQuestions().size()) {
			return;
		}
		
		Question currentQuestion = currentObject.getQuestions().get(index);
	    Question previousQuestion = currentObject.getQuestions().get(index - 1);
	    
	    Integer tmpOrder = currentQuestion.getOrder();
	    currentQuestion.setOrder(previousQuestion.getOrder());
	    previousQuestion.setOrder(tmpOrder);
	    
	    currentObject.getQuestions().set(index, previousQuestion);
	    currentObject.getQuestions().set(index - 1, currentQuestion);
	    
	}
	
	public void moveQuestionDown(Integer index) {
		
		if (index == null || index < 0
				|| index >= currentObject.getQuestions().size()-1) {
			return;
		}
		
		Question currentQuestion = currentObject.getQuestions().get(index);
	    Question nextQuestion = currentObject.getQuestions().get(index + 1);
	    
	    Integer tmpOrder = currentQuestion.getOrder();
	    currentQuestion.setOrder(nextQuestion.getOrder());
	    nextQuestion.setOrder(tmpOrder);
	    
	    currentObject.getQuestions().set(index, nextQuestion);
	    currentObject.getQuestions().set(index + 1, currentQuestion);
	    
	}
	
	public void updateQuestions() {
		
	    try {
	    	
	        serviceTestBL.update(currentObject, super.getLoggedUserId());

	    } catch (Exception e) {
	    	
	    	StackTraceElement element = e.getStackTrace()[0];
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(),
							e.getMessage()));
	        
	    }
	    
	}
	
	public void deleteAnswer(Integer index, Integer indexAns) {
		
		if (index == null || index < 0
				|| index >= currentObject.getQuestions().size()) {
			return;
		}
		
		Question q = currentObject.getQuestions().get(index);
		
		if (indexAns == null || index < 0
				|| indexAns >= q.getAnswers().size()) {
			return;
		}
		
		q.getAnswers().remove((int) indexAns);
		
		int i = 1;
	    for (Answer ans : q.getAnswers()) {
	    	ans.setOrder(i++);
	    }
		
	}
	
	public void setAnswerAsCorrect(Integer index, Integer indexAns) {
		
		if (index == null || index < 0
				|| index >= currentObject.getQuestions().size()) {
			return;
		}
		
		Question q = currentObject.getQuestions().get(index);
		
		if (indexAns == null || index < 0
				|| indexAns >= q.getAnswers().size()) {
			return;
		}
		
		int i = 0;
	    for (Answer ans : q.getAnswers()) {
	    	ans.setCorrect(indexAns.equals(i++));
	    }
		
	}
	
    public void addNewAnswer(Integer index,
    		String imageDataAsBase64, String mimeTypeStr) {
    	
    	try {
    		
    		_checkNullabilityAddImage(index, imageDataAsBase64, mimeTypeStr);
    		
    		Question q = currentObject.getQuestions().get(index);
    		
        	Answer ans = new Answer();
            ans.setOrder(q.getAnswers().size() + 1);
            
            ans.setImage(new Image(imageDataAsBase64, mimeTypeStr));
            ans.setQuestion(q);
            
            q.getAnswers().add(ans);
    		
    	} catch (Exception e) {
    		
    		StackTraceElement element = e.getStackTrace()[0];
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(),
							e.getMessage()));
    		
    	}
    	
    }
    
    public void addQuestionImage(Integer index,
    		String imageDataAsBase64, String mimeTypeStr) {
    	
    	try {
    		
    		_checkNullabilityAddImage(index, imageDataAsBase64, mimeTypeStr);
    		
    		currentObject.getQuestions().get(index).setImage(
            		new Image(imageDataAsBase64, mimeTypeStr));
    		
    	} catch (Exception e) {
    		
    		StackTraceElement element = e.getStackTrace()[0];
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(),
							e.getMessage()));
    		
    	}
    	
    }
    
    private void _checkNullabilityAddImage(Integer index,
    		String imageDataAsBase64, String mimeTypeStr)
    				throws InvalidMimeTypeException {
    	
    	if (index < 0 || index >= currentObject.getQuestions().size()) {
    		throw new ArrayIndexOutOfBoundsException();
		}
    	
    	if (MimeType.findByType(mimeTypeStr) == null) {
    		throw new InvalidMimeTypeException("");
    	}
    	
		if (StringUtils.isBlank(imageDataAsBase64)) {
			throw new IllegalArgumentException();
		}
    	
    }
    
    public void deleteQuestionImage(Integer index) {
    
    	if (index == null || index < 0
				|| index >= currentObject.getQuestions().size()) {
			return;
		}
    	
    	currentObject.getQuestions().get(index).setImage(null);
    	
    }
    
    
    public void deleteImage(Integer index, Integer indexAns) {
    	
    	if (indexAns != null) {
    		deleteAnswer(index, indexAns);
    	} else {
    		deleteQuestionImage(index);
    	}
    	
    }
 
    public AnswersLayoutType[] getAnswersLayoutTypeList() {
    	return AnswersLayoutType.values();
    }
    
    public void createTestInstance(Integer studentGroupId) {
    	
    	try {
			
    		_createTestInstance(studentGroupId);
    		_loadTestInstancePagination();
        	
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}, Cause: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage(), 
					e.getCause() != null ? e.getCause() : "Unknown");
			
		}
    	
    }
    
    private void _createTestInstance(Integer studentGroupId) throws Exception {
    	
		TestInstance testInstance = new TestInstance();
		testInstance.setTestId(currentObject.getId());
		testInstance.setStudentGroup(new StudentGroup(studentGroupId));
		testInstance.setStatus(TestInstanceStatus.IN_PROGRESS);
		testInstance.setAssignedBy(new User(super.getLoggedUserId()));
		testInstance.setAssignedDate(LocalDate.now());
		
		serviceTestInstanceBL.createTestInstance(
				testInstance, super.getLoggedUserId());
    		
    }
    
    private void _loadTestInstancePagination() {
    	
    	try {
    		
			List<TestInstance> list = serviceTestInstanceBL
					.finder(currentObject.getId());
			
    		testInstancePagination =
    				new GenericPagination<TestInstance>(
    						list, PAGINATION_SIZE);
    		
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}, Cause: {}",
	                    element.getClassName(), element.getMethodName(), 
	                    e.getClass().getName(), e.getMessage(), 
	                    e.getCause() != null ? e.getCause() : "Unknown");
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(),
							e.getMessage()));
			
		}
    	
    }
    
    public GenericPagination<TestInstance> getTestInstancePagination() {
    	if (testInstancePagination == null) {
    		_loadTestInstancePagination();
    	}
    	return testInstancePagination;
    }
 
    private void _loadClassComboList() {
		
		try {
			
			classComboList = serviceStudentGroupBL
					.getComboList(currentObject.getCenterId());
					
		} catch (Exception e) {
			
			classComboList = new LinkedList<>();
			
			StackTraceElement element = e.getStackTrace()[0];
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}, Cause: {}",
	                    element.getClassName(), element.getMethodName(), 
	                    e.getClass().getName(), e.getMessage(), 
	                    e.getCause() != null ? e.getCause() : "Unknown");
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(),
							e.getMessage()));
			
		}
		
	}
	
	public List<StringPair> getClassComboList() {
		if (classComboList == null) {
			_loadClassComboList();
		}
		return classComboList;
	}
	
	public BigDecimal getPercentage(Integer count, Integer total) {
		
		if (count == null || total == null) {
			return null;
		}
		
		if (total.equals(0)) {
			return BigDecimal.ZERO;
		}
		
        return new BigDecimal(count).divide(
        		new BigDecimal(total), 2, RoundingMode.HALF_UP)
        		.multiply(new BigDecimal(100));
		
	}
	
	public String getQuestionsTabId() {
		return QUESTIONS_TAB_ID;
	}
	
	public String getTestsTabId() {
		return TESTS_TAB_ID;
	}
	
}