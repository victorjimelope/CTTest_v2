package cttest;

import static constants.Constants.LOGGED_USER;

import java.io.Serializable;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import lombok.Getter;
import lombok.Setter;
import model.LoggedUser;

@Getter @Setter
public class BaseManagedBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private LoggedUser loggedUser;
	
	private boolean showErrorMessage;
	
	private boolean showErrorPage;
	
	private boolean readOnly;
	
	
	protected BaseManagedBean() {
		_loadLoggedUser();
	}
	
	private void _loadLoggedUser() {
		
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
				.getExternalContext().getSession(false);
		
		if (session != null) {
			this.loggedUser = (LoggedUser) session.getAttribute(LOGGED_USER);
		}
		
	}
	
	protected Integer getLoggedUserId() {
		return loggedUser != null ? loggedUser.getId() : null;
	}
	
	public boolean isLoggedUserAdmin() {
		return loggedUser != null ? loggedUser.isAdmin() : false;
	}
	
	protected Integer getLoggedUserCenterId() {
		return loggedUser != null ? loggedUser.getCenterId() : null;
	}
	
	protected List<Integer> getLoggedUserStudentGroupIdList() {
		return loggedUser != null ? loggedUser.getStudentGroupIdList() : null;
	}

	protected Integer getIdParam() throws Exception {
		
	    String idParam = FacesContext.getCurrentInstance()
	    		.getExternalContext().getRequestParameterMap().get("id");
	    
	    try {
	    	
	        return Integer.valueOf(idParam);
	        
	    } catch (Exception e) {
	    	
	        throw new IllegalArgumentException();
	        
	    }
	    
	}
	
	protected void checkLoggedUserAuthorization(Integer currentCenterId) {
		showErrorPage = currentCenterId == null
				|| loggedUser == null
				|| loggedUser.getCenterId() == null
				|| !loggedUser.getCenterId().equals(currentCenterId);	
	}
	
}