package cttest;

import static constants.Constants.LOGGED_USER;
import static constants.NavigationConstants.FACES_REDIRECT_TRUE;
import static constants.NavigationConstants.WELCOME_URI;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bl.LoginBL;
import bl.UserBL;
import lombok.Getter;
import lombok.Setter;
import model.LoggedUser;
import util.SpringContextUtil;

@Getter @Setter
@ManagedBean
@ViewScoped
public class LoginBean implements Serializable {
	
	private static final Logger logger = LogManager.getLogger(TestSearchBean.class);
	
    private static final long serialVersionUID = 1L;
    
    protected LoginBL serviceLoginBL = 
			new SpringContextUtil<LoginBL>()
			.initializeBean(LoginBL.class);
    
    protected UserBL serviceUserBL = 
			new SpringContextUtil<UserBL>()
			.initializeBean(UserBL.class);
    
    private String username;
    
    private String password;
    
    
    public String login() {
    	
    	if (!_validateUser()) {
    		FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(),
							"Credenciales erróneas"));
    		return null;
    	}
    	
    	FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
		
		LoggedUser loggedUser = _buildLoggedUser();
		
		session.setAttribute(LOGGED_USER, loggedUser);

		return WELCOME_URI + FACES_REDIRECT_TRUE;
		
    }
    
    private boolean _validateUser() {
    	
    	try {
    		
    		return serviceLoginBL.validateUser(username, password);
    		
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
    	
    	return false;
    	
    }
 
    private LoggedUser _buildLoggedUser() {
    	
    	try {
    		
    		return serviceUserBL.getLoggedUser(username);
    		
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
    	
    	return null;
    	
    }
    
}