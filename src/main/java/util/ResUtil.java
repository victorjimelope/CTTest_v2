package util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ResUtil {
	
	private static final Logger logger = LogManager.getLogger(ResUtil.class);
	
    private static final String BUNDLE_NAME = "messages";
    
    private static final ResourceBundle resourceBundle =
    		ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());
    
    
    public static String get(String key) {
    	
        try {
        	
            return resourceBundle.getString(key);
            
        } catch (MissingResourceException e) {
        	
        	StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
        } catch (Exception e) {
        	
        	StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
        }
        
        return key;
        
    }
    
}
