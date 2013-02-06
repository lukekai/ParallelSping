package com.alibaba.crm.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Parallel {
	public String poolName() default "paralleltaskpool";
	
	public int corePoolSize() default 1;
	
	public int maxPoolSize() default 5;
	
	public int queueCapacity() default 10;
}

