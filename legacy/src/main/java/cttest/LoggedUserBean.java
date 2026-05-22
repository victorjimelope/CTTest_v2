package cttest;

import static constants.NavigationConstants.FACES_REDIRECT_TRUE;
import static constants.NavigationConstants.LOGIN_URI;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@ManagedBean
@ViewScoped
public class LoggedUserBean extends BaseManagedBean implements Serializable {
	
    private static final long serialVersionUID = 1L;
    
    
    public String logout() {
    	
    	FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
    	
        return LOGIN_URI + FACES_REDIRECT_TRUE;
        
    }
    
}