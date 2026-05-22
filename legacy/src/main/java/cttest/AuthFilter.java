package cttest;

import static constants.Constants.LOGGED_USER;
import static constants.NavigationConstants.HOME_URI;
import static constants.NavigationConstants.LOGIN_URI;
import static constants.NavigationConstants.TEST_DEMO_INIT_URI;
import static constants.NavigationConstants.WELCOME_URI;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter("*.xhtml")
public class AuthFilter implements Filter {
	
	private static final String JAVAX_FACES_RESOURCE = "javax.faces.resource";
	
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
	                     FilterChain chain) throws IOException, ServletException {

	    HttpServletRequest req = (HttpServletRequest) request;
	    HttpServletResponse res = (HttpServletResponse) response;

	    String requestURI = req.getRequestURI();
	    
	    if (requestURI.contains(JAVAX_FACES_RESOURCE)) {
	    	chain.doFilter(request, response);
	    	return;
	    }
	    
	    String contextPath = req.getContextPath();
	    
	    boolean isRootRequest = requestURI.equals(contextPath)
	    		|| requestURI.equals(contextPath + "/");
	    
        if (isRootRequest) {
        	chain.doFilter(request, response);
            return;
        }
        
	    boolean isLoginRequest = requestURI.endsWith(LOGIN_URI);
	    boolean isPublicPage = requestURI.endsWith(HOME_URI)
	    		|| requestURI.contains(TEST_DEMO_INIT_URI);

	    HttpSession session = req.getSession(false);
	    Object loggedUser = (session != null) ? session.getAttribute(LOGGED_USER) : null;

	    if (isLoginRequest) {
	    	
	        if (loggedUser != null) {
	            res.sendRedirect(req.getContextPath() + WELCOME_URI);
	            return;
	        }
	        
	    } else if (!isPublicPage && loggedUser == null) {
	    	
	        res.sendRedirect(req.getContextPath() + LOGIN_URI);
	        return;
	        
	    }

	    chain.doFilter(request, response);
	    
	}
	
}
