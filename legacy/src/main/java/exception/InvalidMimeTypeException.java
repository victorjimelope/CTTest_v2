package exception;

public class InvalidMimeTypeException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	
    public InvalidMimeTypeException() {
        super();
    }
    
    public InvalidMimeTypeException(String message) {
        super(message);
    }
    
    public InvalidMimeTypeException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InvalidMimeTypeException(Throwable cause) {
        super(cause);
    }
    
}
