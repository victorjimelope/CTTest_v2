package cttest;

import static constants.Constants.PAGINATION_SIZE;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import bl.CenterBL;
import bl.StudentGroupBL;
import bl.UserBL;
import lombok.Getter;
import lombok.Setter;
import model.Center;
import model.StudentGroup;
import model.User;
import model.enums.noper.GenericSortType;
import model.noper.GenericPagination;
import model.noper.StringPair;
import util.ResUtil;
import util.SpringContextUtil;

@Getter @Setter
@ManagedBean
@ViewScoped
public class UserSearchBean extends BaseManagedBean implements Serializable {
	
	private static final Logger logger = LogManager.getLogger(UserSearchBean.class);
	
	private static final long serialVersionUID = 1L;
	
    protected UserBL serviceUserBL = 
			new SpringContextUtil<UserBL>()
			.initializeBean(UserBL.class);
    
    protected CenterBL serviceCenterBL = 
			new SpringContextUtil<CenterBL>()
			.initializeBean(CenterBL.class);
    
    protected StudentGroupBL serviceStudentGroupBL = 
			new SpringContextUtil<StudentGroupBL>()
			.initializeBean(StudentGroupBL.class);
    
    private User currentObject;
    
    private GenericPagination<User> pagination;
    
    private List<StringPair> centerComboList;
    private List<StudentGroup> studentGroupList;
    private List<StringPair> sortTypeComboList;
    
    private String filterName;
    private Integer filterCenterId;
    private GenericSortType sortType = GenericSortType.NEWEST;
    
    
	@PostConstruct
    public void init() {
		search();
    }
	
	public void search() {
		
		try {
			
			List<User> list = serviceUserBL.finder(
					filterCenterId, filterName, sortType);
			
    		pagination = new GenericPagination<User>(list, PAGINATION_SIZE);
    		
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
	
	public void load(User user) {
		
		if (user == null || user.getCenter() == null) {
			return;
		}
		
		currentObject = user;
		
		loadStudentGroupList(user.getCenter().getId());
		
		if (!CollectionUtils.isEmpty(currentObject.getStudentGroups())) {
			Set<Integer> studentGroupIdList = studentGroupList.stream()
					.map(StudentGroup::getId)
					.collect(Collectors.toSet());
			
			studentGroupList.forEach(x -> x.setSelected(
					studentGroupIdList.contains(x.getId())));
		}
		
	}
	
	public void initCurrentObject() {
		currentObject = new User();
		currentObject.setCenter(new Center());
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
			
			serviceUserBL.create(currentObject, super.getLoggedUserId());
			
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
			
			serviceUserBL.update(currentObject, super.getLoggedUserId());
			
			for (User center : pagination.getObjectList()) {
	            if (center.getId().equals(currentObject.getId())) {
	                center = currentObject;
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
	
	public void deleteCurrentObject() {
		
		super.setShowErrorMessage(false);
		
		try {
			
			serviceUserBL.delete(currentObject.getId());
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
	
	public void clearFilters() {
		clearFilterCenterId();
	}
	
	public void clearFilterCenterId() {
		filterCenterId = null;
	}
	
	public String getFilterCenterName() {
		
		if (filterCenterId != null
				&& !CollectionUtils.isEmpty(centerComboList)) {
			for (StringPair sp : centerComboList) {
				if (sp.getKey().equals(filterCenterId.toString())) {
					return sp.getValue();
				}
			}
		}
		
		return null;
		
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
	
	public List<StringPair> getCenterComboList() {
		if (centerComboList == null) {
			_loadCenterComboList();
		}
		return centerComboList;
	}
	
	public void loadStudentGroupList(Integer centerId) {
		
		if (centerId == null) {
			return;
		}
		
		try {
			
			studentGroupList = serviceStudentGroupBL
					.finder(centerId, null, GenericSortType.A_Z);
			
		} catch (Exception e) {
			
			studentGroupList = new LinkedList<>();
			
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
	
	public void handleStudentGroup(StudentGroup studentGroup) {
		
	    if (currentObject == null
	    		|| currentObject.getStudentGroups() == null
	    		|| studentGroup == null) {
	        return;
	    }
	    
	    // Delete
	    if (!studentGroup.isSelected()) {
	    	currentObject.getStudentGroups()
	    			.removeIf(x -> x.getId().equals(studentGroup.getId()));
 	        return;
	    }
	    
	    // Create
	    currentObject.getStudentGroups().add(studentGroup);
	    
	}

	public void selectAllStudentGroups() {
		
		if (currentObject == null
	    		|| currentObject.getStudentGroups() == null
	    		|| CollectionUtils.isEmpty(studentGroupList)) {
	        return;
	    }
		
		for (StudentGroup studentGroup : studentGroupList) {
			studentGroup.setSelected(true);
			currentObject.getStudentGroups().add(studentGroup);
		}
		
	}
	
}