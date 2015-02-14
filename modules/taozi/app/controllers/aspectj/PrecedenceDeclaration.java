package controllers.aspectj;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;

/**
 * Created by Heaven on 2015/1/28.
 */
@Aspect
@DeclarePrecedence(
//        "controllers.aspectj.AccessLogger," +
        "controllers.aspectj.ExceptionHandler," +
//        "controllers.aspectj.UserCheckHandler," +
//        "controllers.aspectj.ModifyHandler," +
//        "controllers.aspectj.CacheHandler"
        "controllers.aspectj.UserCheckAspect," +
        "controllers.aspectj.OcsCacheAspect"
)
public class PrecedenceDeclaration {
    /**
     * 该AspectJ类用于指定其余Aspect的优先级
     */
}
