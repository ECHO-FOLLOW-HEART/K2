package aspectj;

import aizou.core.UserAPI;
import exception.AizouException;
import exception.ErrorCode;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static play.mvc.Controller.request;

/**
 * Created by zephyre on 2/14/15.
 */
public class UserCheckHandler {
    public void checkUser(JoinPoint pjp) throws AizouException {
        // 检查header中的UserId
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        checkIndividual(pjp, request().getHeader("UserId"), method.getAnnotation(CheckUser.class).nullable());

        // 检查参数中的UserId
        Object[] args = pjp.getArgs();
        Annotation[][] paramAnno = method.getParameterAnnotations();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Annotation[] annoList = paramAnno[i];
            for (Annotation annotation : annoList) {
                if (annotation != null && annotation instanceof CheckUser)
                    checkIndividual(pjp, arg, ((CheckUser) annotation).nullable());
            }
        }
    }

    private void checkIndividual(JoinPoint pjp, Object userIdVal, boolean nullable) throws AizouException {
        long userId;
        if (userIdVal == null) {
            if (nullable)
                return;
            else
                throw new AizouException(ErrorCode.INVALID_ARGUMENT);
        }

        try {
            userId = Long.parseLong(userIdVal.toString());
        } catch (NumberFormatException e) {
            throw new AizouException(ErrorCode.INVALID_ARGUMENT);
        }

        if (userId <= 0)
            throw new AizouException(ErrorCode.UNKOWN_ERROR);

        String oid = UserAPI.getUserOid(userId);
        if (oid == null)
            throw new AizouException(ErrorCode.UNKOWN_ERROR);
    }
}
