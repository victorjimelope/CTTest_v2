package util;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import exception.AESException;

public final class AESUtil {

	private static final Logger logger = LogManager.getLogger(AESUtil.class);
	
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final byte[] KEY = "ABCDEFGHIJKLMNOP".getBytes(StandardCharsets.UTF_8);
    private static final SecretKeySpec SECRET_KEY = new SecretKeySpec(KEY, ALGORITHM);
    
    
    public static String encrypt(String param) throws AESException {
    	
        try {
        	
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY);
            
            return Base64Util.encode(cipher.doFinal(
            		param.getBytes(StandardCharsets.UTF_8)));
            
        } catch (Exception e) {
        	
        	StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage());
			
			throw new AESException(e);
			
        }
        
    }

    public static String decrypt(String param) throws AESException {
    	
        try {
        	
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY);
            
            return new String(cipher.doFinal(
            		Base64Util.decode(param)),
            		StandardCharsets.UTF_8);
            
        } catch (Exception e) {
        	
        	StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(), 
					e.getClass().getName(), e.getMessage());
			
			throw new AESException(e);
			
        }
        
    }
    
}
