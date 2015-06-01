package controllers.aspectj;

import aspectj.UserCheckHandler;
import exception.AizouException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * Created by zephyre on 2/14/15.
 */
@Aspect
public class UserCheckAspect {
    @Before("execution(public * controllers.app..*(..)) && @annotation(aspectj.CheckUser)")
    public void checkUser(JoinPoint pjp) throws AizouException {
        new UserCheckHandler().checkUser(pjp);
    }
}
