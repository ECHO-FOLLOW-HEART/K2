package controllers.aspectj;

import aspectj.OcsCacheHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import play.mvc.Result;

/**
 * Created by zephyre on 2/14/15.
 */
@Aspect
public class OcsCacheAspect {

    private OcsCacheHandler handler;

    public OcsCacheAspect() {
        handler = new OcsCacheHandler();
    }

    @Around(value = "execution(play.mvc.Result controllers.taozi..*(..))" +
            "&&@annotation(aspectj.RemoveOcsCache)")
    public Result removeCache(ProceedingJoinPoint pjp) throws Throwable {
        return handler.removeCache(pjp);
    }

    @Around(value = "execution(public * controllers.taozi..*(..)) && @annotation(aspectj.UsingOcsCache)")
    public Object tryUsingCache(ProceedingJoinPoint pjp) throws Throwable {
        return handler.tryUsingCache(pjp);
    }
}
