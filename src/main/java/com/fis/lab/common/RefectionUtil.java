package com.fis.lab.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * @createdDate May 22, 2019
 * @author <a href="mailto:hiepnv14@fpt.com.vn">hiepnv14</a>
 * @version 0.0.1
 */
public class RefectionUtil {
	public static Set<Field> findFields(Class<?> clazz, Class<? extends Annotation> ann){
		Set<Field> fields = new HashSet<Field>();
		for(Field field : clazz.getDeclaredFields()) {
			if(field.isAnnotationPresent(ann)) {
				fields.add(field);
			}
		}
		return fields;
	}
}
