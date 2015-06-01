package controllers.aspectj;

import aspectj.UserCheckHandler;
import exception.AizouException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * Created by topy on 2015/5/23.
 */
@Aspect
public aspect ModifyCheckAspect {
    @Around("execution(public * controllers.app..*(..)) && @annotation(aspectj.CheckUser)")
    public void checkUser(JoinPoint pjp) throws AizouException {
        new UserCheckHandler().checkUser(pjp);
    }
}
