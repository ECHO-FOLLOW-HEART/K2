package controllers;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static play.mvc.Controller.response;
import static utils.WrappedStatus.MiscWrappedStatus;

/**
 * Created by Heaven on 2015/1/29.
 */
@Aspect
public class ModifyHandler {
    private static final java.lang.String SEPARATOR = "\\|";
    public static final String hCACHE_CONTROL = "Cache-Control";
    public static final String hMAX_AGE = "max-age";
    public static final String hLAST_MODIFIED = "Last-Modified";
    public static final String hIF_MODIFIED_SINCE = "If-Modified-Since";
    private static final String CACHE_POLICY = "private";
    private static final Long MAX_AGE = (long) (3600 * 12);

    @Around(value = "execution(play.mvc.Result controllers.taozi..*(..))" +
            "&&@annotation(controllers.CheckLastModify)" +
            "&&args(ctrl)")
    public Result checkLastModify(ProceedingJoinPoint pjp, play.mvc.Controller ctrl) throws Throwable {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method method = ms.getMethod();
        CheckLastModify annotation = method.getAnnotation(CheckLastModify.class);

        //获取数据的Last-Modified时间
        String callback = annotation.callback();
        String[] args = annotation.args().split(SEPARATOR);
        Method getLastModifyTimeMethod = getMethod(ctrl.getClass(), callback);
        List<Object> argsList = new ArrayList<>();
        for (String argTag : args) {
            Object arg = getArgWithTag(argTag, method.getParameterAnnotations(), pjp.getArgs());
            argsList.add(arg);
        }
        Object[] objects = argsList.toArray();
        long lastModified = (long) getLastModifyTimeMethod.invoke(ctrl.getClass(), objects);

        //从Header中获取If-Modified-Since字段
        Http.Context context = Http.Context.current();
        Http.Request request = context.request();
        String ifModifySinceStr = request.getHeader(hIF_MODIFIED_SINCE);
        long ifModifySince = 0;
        if (ifModifySinceStr != null && !ifModifySinceStr.isEmpty()) {
            ifModifySince = Long.parseLong(ifModifySinceStr);
        }

        //判断时间戳
        if (lastModified <= ifModifySince) {
            return MiscWrappedStatus(304);
        }

        Long last = lastModified;
        response().setHeader(hCACHE_CONTROL, CACHE_POLICY + ", " + hMAX_AGE + "=" + MAX_AGE);
        response().setHeader(hLAST_MODIFIED, last.toString());
        return (Result) pjp.proceed();
    }

    private Object getArgWithTag(String argTag, Annotation[][] parameterAnnotations, Object[] pjpArgs) {
        for (Annotation[] p : parameterAnnotations) {
            for (Annotation an : p) {
                if (an instanceof CacheKey && ((CacheKey) an).tag().equals(argTag)) {
                    return pjpArgs;
                }
            }
        }
        return null;
    }

    private Method getMethod(Class<? extends Controller> ctrlClass, String callback) {
        Method[] declaredMethods = ctrlClass.getDeclaredMethods();
        for (Method m : declaredMethods) {
            if (m.getName().equals(callback)) {
                return m;
            }
        }
        throw new NullPointerException("No Such Method : " + callback);
    }

}