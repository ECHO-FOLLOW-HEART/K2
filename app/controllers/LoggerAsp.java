package controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Created by Heaven on 2014/12/30.
 */
@Aspect
public class LoggerAsp {
    Log logger = LogFactory.getLog("LoggerAsp");

    @Pointcut("call(public play.mvc.Result controllers.Application.index())")
    void callIndex() {
    }

    @Before("callIndex()")
    public void runCall(JoinPoint thisJionPoint) {
        logger.info("Action called:     " + thisJionPoint.getSignature());
    }

}
