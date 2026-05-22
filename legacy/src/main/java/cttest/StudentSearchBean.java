package cttest;

import static constants.Constants.PAGINATION_SIZE;

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
import org.springframework.util.CollectionUtils;

import bl.StudentBL;
import bl.StudentGroupBL;
import lombok.Getter;
import lombok.Setter;
import model.Student;
import model.StudentGroup;
import model.enums.GenderType;
import model.enums.noper.GenericSortType;
import model.noper.GenericPagination;
import model.noper.StringPair;
import util.ResUtil;
import util.SpringContextUtil;

@Getter @Setter
@ManagedBean
@ViewScoped
public class StudentSearchBean extends BaseManagedBean implements Serializable {
	
	private static final Logger logger = LogManager.getLogger(StudentSearchBean.class);
	
	private static final long serialVersionUID = 1L;
	
    
    protected StudentBL serviceStudentBL = 
			new SpringContextUtil<StudentBL>()
			.initializeBean(StudentBL.class);
    
    protected StudentGroupBL serviceStudentGroupBL = 
			new SpringContextUtil<StudentGroupBL>()
			.initializeBean(StudentGroupBL.class);
    
    private Student currentObject;
    
    private GenericPagination<Student> pagination;
    
    private List<StringPair> studentGroupComboList;
    private List<StringPair> genderTypeComboList;
    private List<StringPair> sortTypeComboList;
    
    private String filterName;
    private Integer filterStudentGroupId;
    private GenericSortType sortType = GenericSortType.NEWEST;
    
    
	@PostConstruct
    public void init() {
		search();
    }
	
	public void search() {
		
		try {
			
			List<Student> list = serviceStudentBL.finder(
					filterName, filterStudentGroupId, sortType);
			
    		pagination = new GenericPagination<Student>(list, PAGINATION_SIZE);
    		
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
		currentObject = new Student();
		currentObject.setStudentGroup(new StudentGroup());
	}
	
	public String save() {
		
		super.setShowErrorMessage(false);
		
		try {

			if (currentObject.getId() == null) {
				
				_create();
				
			} else {
				
				_update();
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
	
	private void _create() throws Exception {
		
		try {
			
			serviceStudentBL.create(currentObject, super.getLoggedUserId());
			
			FacesContext.getCurrentInstance().addMessage(null, 
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
	
	private void _update() throws Exception {
		
		try {
			
			serviceStudentBL.update(currentObject, super.getLoggedUserId());
			
			for (Student student : pagination.getObjectList()) {
	            if (student.getId().equals(currentObject.getId())) {
	                student = currentObject;
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
	
	public void deleteCurrentObject() throws Exception {
		
		try {
			
			serviceStudentBL.delete(currentObject.getId());
			pagination.getObjectList().remove(currentObject);
	        currentObject = null;
			
	        FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, 
							FacesMessage.SEVERITY_INFO.toString(),
							ResUtil.get("label_record_deleted")));
	        
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage());
			
			throw new Exception(e.getMessage(), e);
			
		}
		
	}
	
	public void clearFilters() {
		//filterTestStatus = null;
	}
	
	public List<StringPair> getGenderTypeComboList() {
		if (genderTypeComboList == null) {
			genderTypeComboList = GenderType.getComboList();
		}
		return genderTypeComboList;
	}
	
	public List<StringPair> getSortTypeComboList() {
		if (sortTypeComboList == null) {
			sortTypeComboList = GenericSortType.getComboList();
		}
		return sortTypeComboList;
	}
	
	public Integer getSortTypeId() {
		return sortType != null ? sortType.getId() : null;
	}
	
	public void setSortTypeId(Integer id) {
		sortType = GenericSortType.getById(id);
	}
	
	public List<StringPair> getStudentGroupComboList() {
		if (studentGroupComboList == null) {
			_loadStudentGroupComboList();
		}
		return studentGroupComboList;
	}
	
	private void _loadStudentGroupComboList() {
		
		try {
			
			studentGroupComboList = serviceStudentGroupBL
					.getComboList(new LinkedList<Integer>());
			
		} catch (Exception e) {
			
			studentGroupComboList = new LinkedList<>();
			
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
	
	public String getFilterStudentGroupName() {
		
		if (filterStudentGroupId != null
				&& !CollectionUtils.isEmpty(studentGroupComboList)) {
			for (StringPair sp : studentGroupComboList) {
				if (sp.getKey().equals(filterStudentGroupId.toString())) {
					return sp.getValue();
				}
			}
		}
		
		return null;
		
	}
	
}