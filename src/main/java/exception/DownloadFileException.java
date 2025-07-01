package exception;

public class DownloadFileException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	
    public DownloadFileException() {
        super();
    }
    
    public DownloadFileException(String message) {
        super(message);
    }
    
    public DownloadFileException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DownloadFileException(Throwable cause) {
        super(cause);
    }
    
}
