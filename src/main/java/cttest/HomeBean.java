package cttest;

import static constants.NavigationConstants.FACES_REDIRECT_TRUE;
import static constants.NavigationConstants.NAVIGATION_ID;
import static constants.NavigationConstants.TEST_DEMO_INIT_URI;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bl.StudentTestBL;
import bl.TestInstanceBL;
import lombok.Getter;
import lombok.Setter;
import model.StudentTest;
import util.AESUtil;
import util.SpringContextUtil;

@Getter @Setter
@ManagedBean
@ViewScoped
public class HomeBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final Logger logger = LogManager.getLogger(HomeBean.class);
    
    protected TestInstanceBL serviceTestInstanceBL = 
			new SpringContextUtil<TestInstanceBL>()
			.initializeBean(TestInstanceBL.class);
    
    protected StudentTestBL serviceStudentTestBL = 
    		new SpringContextUtil<StudentTestBL>()
			.initializeBean(StudentTestBL.class);
    
    private String pinStr;
    
    private String identifier;
    
    
    public String navigateToTest() {
    	
		try {
			
			Integer pin = Integer.valueOf(pinStr);
			Integer studentIdentifier = Integer.valueOf(identifier);
			
			Integer testInstanceId = serviceTestInstanceBL.getIdByPin(pin);
			
			// No se ha reconocido el PIN
			
			// TODO: Crear método concreto
			StudentTest studentTest = serviceStudentTestBL
					.find(testInstanceId, studentIdentifier);
			
			if (studentTest == null) {
				throw new Exception("Ocurrió un error al acceder al test");
			}
			return TEST_DEMO_INIT_URI + FACES_REDIRECT_TRUE + NAVIGATION_ID
					+ URLEncoder.encode(AESUtil.encrypt(
							String.valueOf(studentTest.getId())),
							StandardCharsets.UTF_8.toString());
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
		}
    	
		return null;
		
    }
    
}