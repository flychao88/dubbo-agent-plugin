package com.dubboagent.utils.extension;

import java.lang.annotation.*;

/**
 * Date:2017/11/27
 *
 * @author:chao.cheng
 **/

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Setting {

    String[] value() default {};
}
