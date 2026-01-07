package wiki.exception;

public class EditDataException extends RuntimeException {
    
    public EditDataException(String message) {
        super(message);
    }
    
    public EditDataException(String message, Throwable cause) {
        super(message, cause);
    }
}