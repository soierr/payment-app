package play.bank_1.exception;

public class HttpServiceException extends RuntimeException {
    public HttpServiceException(String message) {
        super(message);
    }
}
