package com.xavier.handy.announce;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller {

    /**
     * 起别名
     *
     * @return
     */
    String value() default "";
}
