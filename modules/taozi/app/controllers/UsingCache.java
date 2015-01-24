package controllers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Heaven on 2015/1/22.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface UsingCache {
    // default expire time is 10 seconds
    String key();
    int exprieTime() default 10;
}
