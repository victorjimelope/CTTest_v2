package util;

import java.util.Base64;

import org.apache.commons.lang3.StringUtils;

public final class Base64Util {
	
    public static String encode(byte[] param) {
    	return param != null && param.length > 0
    			? Base64.getEncoder().encodeToString(param) : null;
    }
    
    public static byte[] decode(String param) {
    	return !StringUtils.isBlank(param)
    			? Base64.getDecoder().decode(param) : null;
    }
    
}
