package controllers.taozi;

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
    public Log logger = LogFactory.getLog("LoggerInTaozi");

    @Pointcut("execution(play.mvc.Result *(..)) && !within(LoggerAsp)")
    void callFunc() {
    }

//    @Pointcut("cflow(execution(* index())) && !within(LoggerAsp)")
//    void index(){
//    }

    @Before("callFunc()")
    public void runCall(JoinPoint thisJoinPoint) {
        logger.info("Action called:   " + thisJoinPoint.getSignature());
    }

}
