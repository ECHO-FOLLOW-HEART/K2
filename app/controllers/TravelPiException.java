package controllers;

/**
 * 异常类
 *
 * @author Zephyre
 */
public class TravelPiException extends Exception {
    public TravelPiException(String msg) {
        super(msg);
    }

    public TravelPiException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public TravelPiException(Throwable cause) {
        super(cause);
    }

    public TravelPiException(){
    }
}
