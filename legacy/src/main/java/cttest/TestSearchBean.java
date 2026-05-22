package cttest;

import static constants.Constants.PAGINATION_SIZE;
import static constants.NavigationConstants.FACES_REDIRECT_TRUE;
import static constants.NavigationConstants.NAVIGATION_ID;
import static constants.NavigationConstants.TEST_VIEW_WITH_TEMPLATE_URI;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bl.CenterBL;
import bl.TestBL;
import lombok.Getter;
import lombok.Setter;
import model.Test;
import model.enums.TestStatus;
import model.enums.TestType;
import model.enums.noper.GenericSortType;
import model.noper.GenericPagination;
import model.noper.StringPair;
import util.ResUtil;
import util.SpringContextUtil;

@Getter @Setter
@ManagedBean
@ViewScoped
public class TestSearchBean extends BaseManagedBean implements Serializable {
	
	private static final Logger logger = LogManager.getLogger(TestSearchBean.class);
	
	private static final long serialVersionUID = 1L;
	
	private static final String MY_TESTS_TAB_ID = "MyTeTbId";
	private static final String EXPLORE_TAB_ID = "ExTeTbId";
	
    protected TestBL serviceTestBL = 
			new SpringContextUtil<TestBL>()
			.initializeBean(TestBL.class);
    
    protected CenterBL serviceCenterBL = 
			new SpringContextUtil<CenterBL>()
			.initializeBean(CenterBL.class);
    
    private Test currentObject;
    private String currentTab;
    
    private GenericPagination<Test> pagination;
    
    private List<StringPair> centerComboList;
    private List<StringPair> testStatusComboList;
    private List<StringPair> sortTypeComboList;
    
    private String filterName;
    private TestStatus filterTestStatus;
    private GenericSortType sortType = GenericSortType.NEWEST;
    
    
    public TestSearchBean() {
    	super();
    }
    
	@PostConstruct
    public void init() {
		currentTab = MY_TESTS_TAB_ID;
		search();
    }
	
	public void search() {
		
		if (currentTab.equals(MY_TESTS_TAB_ID)) {
			
			_loadMyTestsList();
			
		} else if (currentTab.equals(EXPLORE_TAB_ID)) {
			
			_loadExploreList();
			
		}
		
	}
	
	private void _loadMyTestsList() {
		
		try {
			
			List<Test> list = serviceTestBL.finder(
					super.getLoggedUserCenterId(),
					filterName, filterTestStatus, sortType);
    		
    		pagination = new GenericPagination<Test>(list, PAGINATION_SIZE);
    		
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
	
	private void _loadExploreList() {
		
		try {
			
			List<Test> list = serviceTestBL.explore(
					super.getLoggedUserCenterId(), filterName, sortType);
    		
    		pagination = new GenericPagination<Test>(list, PAGINATION_SIZE);
    		
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
	
	public void initCurrentObject() {
		currentObject = new Test();
		currentObject.setStatus(TestStatus.DRAFT);
		currentObject.setType(TestType.PRIVATE);
		currentObject.setCenterId(super.getLoggedUserCenterId());
	}
	
	public String save() {
		
		super.setShowErrorMessage(false);
		
		try {

			if (currentObject.getId() == null) {
				
				_createTest();
				
				return TEST_VIEW_WITH_TEMPLATE_URI + FACES_REDIRECT_TRUE
						+ NAVIGATION_ID + currentObject.getId();
				
			} else {
				
				_updateTest();
				currentObject = null;
				
			}
			
		} catch (Exception e) {
			
			super.setShowErrorMessage(true);
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(),
							e.getMessage()));
			
		}
		
		return null;
		
	}
	
	private void _createTest() throws Exception {
		
		try {
			
			serviceTestBL.create(currentObject, super.getLoggedUserId());
			
			FacesContext context = FacesContext.getCurrentInstance();
			context.getExternalContext().getFlash().setKeepMessages(true);

			context.addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, 
							FacesMessage.SEVERITY_INFO.toString(),
							ResUtil.get("label_record_created")));
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage());
			
			throw new Exception(e.getMessage(), e);
			
		}
		
	}
	
	private void _updateTest() throws Exception {
		
		try {
			
			serviceTestBL.update(currentObject, super.getLoggedUserId());
			
			for (Test test : pagination.getObjectList()) {
	            if (test.getId().equals(currentObject.getId())) {
	                test = currentObject;
	                break;
	            }
	        }
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, 
							FacesMessage.SEVERITY_INFO.toString(),
							ResUtil.get("label_record_updated")));
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage());
			
			throw new Exception(e.getMessage(), e);
			
		}
		
	}
	
	public void deleteCurrentTest() {
		
		super.setShowErrorMessage(false);
		
		try {
			
			serviceTestBL.delete(currentObject.getId());
			pagination.getObjectList().remove(currentObject);
	        currentObject = null;
			
	        FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, 
							FacesMessage.SEVERITY_INFO.toString(),
							ResUtil.get("label_record_deleted")));
	        
		} catch (Exception e) {
			
			super.setShowErrorMessage(true);
			
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
	
	public List<StringPair> getTestStatusComboList() {
		if (testStatusComboList == null) {
			testStatusComboList = TestStatus.getComboList();
		}
		return testStatusComboList;
	}
	
	public List<StringPair> getSortTypeComboList() {
		if (sortTypeComboList == null) {
			sortTypeComboList = GenericSortType.getComboList();
		}
		return sortTypeComboList;
	}
	
	public void clearFilters() {
		filterTestStatus = null;
	}
	
	public Integer getFilterTestStatusId() {
		return filterTestStatus != null ? filterTestStatus.getId() : null;
	}
	
	public void setFilterTestStatusId(Integer id) {
		filterTestStatus = TestStatus.getById(id);
	}
	
	public Integer getSortTypeId() {
		return sortType != null ? sortType.getId() : null;
	}
	
	public void setSortTypeId(Integer id) {
		sortType = GenericSortType.getById(id);
	}

	public String getMyTestsTabId() {
		return MY_TESTS_TAB_ID;
	}
	
	public String getExploreTabId() {
		return EXPLORE_TAB_ID;
	}
	
	public String cloneTest() {
		
		super.setShowErrorMessage(false);
		
		try {
			
			Integer cloneId = serviceTestBL.clone(currentObject.getId(),
					super.getLoggedUserCenterId(), currentObject.getName(),
					currentObject.getDescription(), super.getLoggedUserId());
			
			FacesContext context = FacesContext.getCurrentInstance();
			context.getExternalContext().getFlash().setKeepMessages(true);

			context.addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, 
							FacesMessage.SEVERITY_INFO.toString(),
							ResUtil.get("label_test_cloned")));
			
			return TEST_VIEW_WITH_TEMPLATE_URI + FACES_REDIRECT_TRUE
					+ NAVIGATION_ID + cloneId;
			
		} catch (Exception e) {
			
			super.setShowErrorMessage(true);
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(),
							e.getMessage()));
			
		}
		
		return null;
		
	}
	
	public List<StringPair> getCenterComboList() {
		if (centerComboList == null) {
			_loadCenterComboList();
		}
		return centerComboList;
	}
	
	private void _loadCenterComboList() {
		
		try {
			
			centerComboList = serviceCenterBL.getComboList();
			
		} catch (Exception e) {
			
			centerComboList = new LinkedList<>();
			
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
	
}