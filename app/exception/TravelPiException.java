package exception;

/**
 * 异常类
 *
 * @author Zephyre
 */
public class TravelPiException extends Exception {
    private int errCode = ErrorCode.NORMAL;

    public TravelPiException(String msg) {
        super(msg);
    }

    public TravelPiException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public TravelPiException(Throwable cause) {
        super(cause);
    }

    public TravelPiException(int code, String msg, Throwable cause) {
        super(msg, cause);
        errCode = code;
    }

    public TravelPiException() {
    }

    @Override
    public String toString() {
        if (getCause() != null)
            return String.format("Message: %s, cause: %s, code: %d.", getMessage(), getCause().getMessage(), errCode);
        else
            return String.format("Message: %s, code: %d.", getMessage(), errCode);
    }
}
