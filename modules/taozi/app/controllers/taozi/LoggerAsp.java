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
    public Log logger = LogFactory.getLog("LoggerAspinTaozi");

    @Pointcut("execution(* *(..)) && !within(LoggerAsp)")
    void callFunc() {
    }

//    @Pointcut("cflow(execution(* index())) && !within(LoggerAsp)")
//    void index(){
//    }

    @Before("callFunc()")
    public void runCall(JoinPoint thisJoinPoint) {
        logger.info(thisJoinPoint.getSignature());
    }

//    @Before("cflow(callFunc()) && !within(LoggerAsp)")
//    public void testCall(JoinPoint joinPoint) {
//        logger.info("test called:       " + joinPoint.getSignature());
//    }
//    @Before("index()")
//    public void runIndex(JoinPoint thisJoinPoint) {
//        logger.info("index:     " + thisJoinPoint.getSignature());
//    }

//    @Around("callFunc()")
//    public Result instead(ProceedingJoinPoint pjp) {
//        Result ret = Utils.createResponse(ErrorCode.UNKOWN_ERROR);
//        try {
//            ret = (Result) pjp.proceed();
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        } finally {
//            return ret;
//        }
//    }
}
