package aspectj;

import aizou.core.UserAPI;
import exception.AizouException;
import exception.ErrorCode;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

import static play.mvc.Controller.request;

/**
 * Created by zephyre on 2/14/15.
 */
public class UserCheckHandler {
    public void checkUser(JoinPoint pjp) throws AizouException {
        long userId;
        String userIdVal = request().getHeader("UserId");

        if (userIdVal == null) {
            // 检查是否nullable
            MethodSignature ms = (MethodSignature) pjp.getSignature();
            Method method = ms.getMethod();
            CheckUser annotation = method.getAnnotation(CheckUser.class);
            boolean nullable = annotation.nullable();

            if (nullable)
                return;
            else
                throw new AizouException(ErrorCode.INVALID_ARGUMENT);
        }

        try {
            userId = Long.parseLong(userIdVal);
        } catch (NumberFormatException e) {
            throw new AizouException(ErrorCode.INVALID_ARGUMENT);
        }

        if (userId <= 0)
            throw new AizouException(ErrorCode.USER_NOT_EXIST);

        String oid = UserAPI.getUserOid(userId);
        if (oid == null)
            throw new AizouException(ErrorCode.USER_NOT_EXIST);
    }
}