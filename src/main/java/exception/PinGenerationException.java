package exception;

public class PinGenerationException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	
    public PinGenerationException() {
        super();
    }
    
    public PinGenerationException(String message) {
        super(message);
    }
    
    public PinGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PinGenerationException(Throwable cause) {
        super(cause);
    }
    
}
