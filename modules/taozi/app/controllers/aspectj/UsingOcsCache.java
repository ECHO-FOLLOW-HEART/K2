package controllers.aspectj;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Heaven on 2015/1/22.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UsingOcsCache {

    /**
     * 缓存的key（模板）
     */
    String key();

    /**
     * 序列化器
     */
    String serializer() default "";

    /**
     * 缓存的过期时间。单位是秒。
     */
    int expireTime() default 600;
}
