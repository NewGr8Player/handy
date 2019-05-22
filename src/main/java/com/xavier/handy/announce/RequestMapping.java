package com.xavier.handy.announce;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequestMapping {

    /**
     * 映射 url
     */
    String value() default "";

}
