package controllers;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;

/**
 * Created by Heaven on 2015/1/28.
 */
@Aspect
@DeclarePrecedence(
        "controllers.AccessLogger," +
        "controllers.ExceptionHandler," +
        "controllers.UserCheckHandler," +
        "controllers.ModifyHandler," +
        "controllers.CacheHandler"
)
public class PrecedenceDeclaration {
    /**
     * 该AspectJ类用于指定其余Aspect的优先级
     */
}
