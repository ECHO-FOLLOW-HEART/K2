package exception;

/**
 * 异常类
 *
 * @author Zephyre
 */
public class TaoziException extends Exception {
    private int errCode = ErrorCode.NORMAL;

    public TaoziException(String msg) {
        super(msg);
    }

    public TaoziException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public TaoziException(Throwable cause) {
        super(cause);
    }

    public TaoziException(int code, String msg, Throwable cause) {
        super(msg, cause);
        errCode = code;
    }

    public TaoziException(int code, String msg) {
        super(msg);
        errCode = code;
    }

    public TaoziException() {
    }

    @Override
    public String toString() {
        if (getCause() != null)
            return String.format("Message: %s, cause: %s, code: %d.", getMessage(), getCause().getMessage(), errCode);
        else
            return String.format("Message: %s, code: %d.", getMessage(), errCode);
    }

    public int getErrCode() {
        return errCode;
    }
}
