package exception;

/**
 * 异常类
 *
 * @author Zephyre
 */
public class AizouException extends Exception {
    protected final int errCode;

    public AizouException(int code, String msg, Throwable cause) {
        super(msg, cause);
        errCode = code;
    }

    public AizouException(int code, String msg) {
        super(msg);
        errCode = code;
    }

    public AizouException(int code) {
        errCode = code;
    }

    public int getErrCode() {
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
