package wiki.exception;

public class WikiStreamException extends RuntimeException {
    
    public WikiStreamException(String message) {
        super(message);
    }
    
    public WikiStreamException(String message, Throwable cause) {
        super(message, cause);
    }
}