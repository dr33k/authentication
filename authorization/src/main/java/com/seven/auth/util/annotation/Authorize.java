package com.seven.auth.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.TYPE})
public @interface Authorize {
    String[] roles() default {};

    String[] privileges() default {};

    String domain() default "";

    String permission() default "";
}
