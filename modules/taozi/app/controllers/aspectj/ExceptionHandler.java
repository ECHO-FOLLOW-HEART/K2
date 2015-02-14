package controllers.aspectj;

import exception.AizouException;
import exception.ErrorCode;
import formatter.taozi.ObjectIdSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import play.Configuration;
import play.mvc.Result;
import utils.Utils;
import utils.results.ResultBuilder;
import utils.results.TaoziResBuilder;

/**
 * Created by Heaven on 2014/12/30.
 */
@Aspect
public class ExceptionHandler {
    private Log logger = LogFactory.getLog(this.getClass());

    private boolean debug;

    public ExceptionHandler() {
        String runLevel = Configuration.root().getString("runlevel");
        debug = (runLevel != null && runLevel.toLowerCase().equals("debug"));

        logger.info(String.format("Exception handler init: %s", debug));
    }

    @Around("execution(public play.mvc.Result controllers.taozi..*(..)) || @annotation(controllers.aspectj.CatchException)")
    public Result catchException(ProceedingJoinPoint pjp) {
        try {
            return (Result) pjp.proceed();
        } catch (AizouException e) {
            ResultBuilder builder = new TaoziResBuilder().setCode(e.getErrCode());
            if (debug)
                builder.setDebugInfo(e.getMessage());
            return builder.build();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            logger.info("An unhandled Exception was caught by ExceptionHandler");
            logger.info("Action name:     " + pjp.getSignature());
            logger.info("CaughtException: " + throwable.toString());

            ResultBuilder builder = new TaoziResBuilder().setCode(ErrorCode.UNKOWN_ERROR);
            if (debug)
                builder.setDebugInfo(throwable.toString());
            return builder.build();
        }
    }
}
