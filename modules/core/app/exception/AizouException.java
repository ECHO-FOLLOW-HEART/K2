package exception;

/**
 * 异常类
 *
 * @author Zephyre
 */
public class AizouException extends Exception {
    protected final ErrorCode errCode;

    public AizouException(ErrorCode code, String msg, Throwable cause) {
        super(msg, cause);
        errCode = code;
    }

    public AizouException(ErrorCode code, String msg) {
        super(msg);
        errCode = code;
    }

    public AizouException(ErrorCode code) {
        errCode = code;
    }

    public ErrorCode getErrCode() {
        return errCode;
    }

    @Override
    public String toString() {
        if (getCause() != null)
            return String.format("Message: %s, cause: %s, code: %d.", getMessage(), getCause().getMessage(), errCode);
        else
            return String.format("Message: %s, code: %d.", getMessage(), errCode);
    }
}
