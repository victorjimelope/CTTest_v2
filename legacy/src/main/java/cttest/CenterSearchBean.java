package cttest;

import static constants.Constants.PAGINATION_SIZE;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bl.CenterBL;
import lombok.Getter;
import lombok.Setter;
import model.Center;
import model.enums.noper.GenericSortType;
import model.noper.GenericPagination;
import model.noper.StringPair;
import util.ResUtil;
import util.SpringContextUtil;

@Getter @Setter
@ManagedBean
@ViewScoped
public class CenterSearchBean extends BaseManagedBean implements Serializable {
	
	private static final Logger logger = LogManager.getLogger(CenterSearchBean.class);
	
	private static final long serialVersionUID = 1L;
	
    protected CenterBL serviceCenterBL = 
			new SpringContextUtil<CenterBL>()
			.initializeBean(CenterBL.class);
    
    private Center currentObject;
    
    private GenericPagination<Center> pagination;
    
    private List<StringPair> sortTypeComboList;
    
    private String filterName;
    private GenericSortType sortType = GenericSortType.NEWEST;
    
    
	@PostConstruct
    public void init() {
		search();
    }
	
	public void search() {
		
		try {
			
			List<Center> list = serviceCenterBL.finder(filterName, sortType);
			
    		pagination = new GenericPagination<Center>(list, PAGINATION_SIZE);
    		
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
		currentObject = new Center();
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
			
			serviceCenterBL.create(currentObject, super.getLoggedUserId());
			
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
			
			serviceCenterBL.update(currentObject, super.getLoggedUserId());
			
			for (Center center : pagination.getObjectList()) {
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
	
	public void deleteCurrentObject() throws Exception {
		
		try {
			
			serviceCenterBL.delete(currentObject.getId());
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
	
}