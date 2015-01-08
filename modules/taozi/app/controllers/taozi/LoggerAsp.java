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
    private Log logger = LogFactory.getLog("LoggerInTaozi");

    @Pointcut("execution(play.mvc.Result controllers.taozi..*(..)) && !within(LoggerAsp)")
    void callAction() {
    }

//    @Pointcut("cflow(execution(* index())) && !within(LoggerAsp)")
//    void index(){
//    }

    @Before("callAction()")
    public void runCall(JoinPoint thisJoinPoint) {
        logger.info("Action called:   " + thisJoinPoint.getSignature());
    }

}
