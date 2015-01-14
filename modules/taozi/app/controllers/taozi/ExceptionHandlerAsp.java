package controllers.taozi;

import exception.ErrorCode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import play.mvc.Result;
import utils.Utils;

/**
 * Created by Heaven on 2014/12/30.
 */
@Aspect
public class ExceptionHandlerAsp {
    private Log logger = LogFactory.getLog("ExceptionHandler");

    @Pointcut("call(public play.mvc.Result controllers.taozi..*(..)) && !within(ExceptionHandlerAsp)")
    void allControllers() {
    }

    @Around("allControllers()")
    public Result catchException(ProceedingJoinPoint pjp, JoinPoint joinPoint) {
        try {
            return (Result) pjp.proceed();
        } catch (Throwable throwable) {
//            throwable.printStackTrace();
            logger.info("An unhandled Exception was caught by ExceptionHandler");
            logger.info("Action name:     " + joinPoint.getSignature());
            logger.info("CaughtException: " + throwable.toString());
            return Utils.createResponse(ErrorCode.UNKOWN_ERROR, "Unknown Error");
        }
    }
}
