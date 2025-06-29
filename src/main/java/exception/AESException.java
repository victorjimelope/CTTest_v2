package exception;

public class AESException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	
    public AESException() {
        super();
    }
    
    public AESException(String message) {
        super(message);
    }
    
    public AESException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AESException(Throwable cause) {
        super(cause);
    }
    
}
