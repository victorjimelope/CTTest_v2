package util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Clase de utilidad para encriptar y validar contraseñas
 * 
 * @author victorjl 20250106
 */
public final class PasswordUtil {
	
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    public static String encryptPassword(String password) {
        return encoder.encode(password);
    }
    
    public static boolean matches(String password, String encryptedPassword) {
        return encoder.matches(password, encryptedPassword);
    }
    
}
