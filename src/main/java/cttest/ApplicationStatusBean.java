package cttest;

import static constants.NavigationConstants.CENTER_FORM_URI;
import static constants.NavigationConstants.CENTER_SEARCH_URI;
import static constants.NavigationConstants.CENTER_VIEW_WITH_TEMPLATE_URI;
import static constants.NavigationConstants.HOME_URI;
import static constants.NavigationConstants.LOGIN_URI;
import static constants.NavigationConstants.STUDENT_FORM_URI;
import static constants.NavigationConstants.STUDENT_GROUP_FORM_URI;
import static constants.NavigationConstants.STUDENT_GROUP_SEARCH_URI;
import static constants.NavigationConstants.STUDENT_SEARCH_URI;
import static constants.NavigationConstants.TEST_DEMO_INIT_URI;
import static constants.NavigationConstants.TEST_DEMO_URI;
import static constants.NavigationConstants.TEST_FORM_URI;
import static constants.NavigationConstants.TEST_INSTANCE_VIEW_WITH_TEMPLATE_URI;
import static constants.NavigationConstants.TEST_SEARCH_URI;
import static constants.NavigationConstants.TEST_VIEW_WITH_TEMPLATE_URI;
import static constants.NavigationConstants.USER_SEARCH_URI;
import static constants.NavigationConstants.WELCOME_URI;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@ManagedBean
@SessionScoped
public class ApplicationStatusBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    
    public String getLoginURI() {
    	return LOGIN_URI;
    }
    
    public String getWelcomeURI() {
    	 return WELCOME_URI;
    }
    
    public String getCenterQueryURI() {
        return CENTER_SEARCH_URI;
    }
    
    public String getCenterFormURI() {
    	return CENTER_FORM_URI;
    }
    
    public String getCenterViewWithTemplateURI() {
        return CENTER_VIEW_WITH_TEMPLATE_URI;
    }
    
    public String getTestQueryURI() {
        return TEST_SEARCH_URI;
    }
    
    public String getTestFormURI() {
        return TEST_FORM_URI;
    }
    
    public String getTestViewWithTemplateURI() {
        return TEST_VIEW_WITH_TEMPLATE_URI;
    }
 
    public String getStudentGroupSearchURI() {
        return STUDENT_GROUP_SEARCH_URI;
    }

    public String getStudentGroupFormURI() {
        return STUDENT_GROUP_FORM_URI;
    }
    
    public String getStudentSearchURI() {
        return STUDENT_SEARCH_URI;
    }

    public String getStudentFormURI() {
        return STUDENT_FORM_URI;
    }
 
    public String getTestDemoURI() {
    	return TEST_DEMO_URI;
    }
    
    public String getTestDemoInitURI() {
    	return TEST_DEMO_INIT_URI;
    }
 
    public String getTestInstanceViewWithTemplateURI() {
    	return TEST_INSTANCE_VIEW_WITH_TEMPLATE_URI;
    }
    
    public String getHomeURI() {
    	return HOME_URI;
    }
    
    public String getUserSearchURI() {
    	return USER_SEARCH_URI;
    }
    
}