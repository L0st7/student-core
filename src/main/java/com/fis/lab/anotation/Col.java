package com.fis.lab.anotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @createdDate May 22, 2019
 * @author <a href="mailto:hiepnv14@fpt.com.vn">hiepnv14</a>
 * @version 0.0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Col {
	String name() default "";
}
