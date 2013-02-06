package com.alibaba.crm.test.spring.parallel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

public class ParalellReflectUtil {
	public static Future invoke(Object proxy, String methodName, Object... args) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		Class[] classes = new Class[args.length];
		int i=0;
		for (Object obj: args){
			classes[i] = obj.getClass();
		}
		
		Method m = proxy.getClass().getMethod(methodName, classes);
		return (Future)(m.invoke(proxy, args));
	}
	public static Future invoke(Object proxy, String methodName) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		
		Method m = proxy.getClass().getMethod(methodName, new Class[]{});
		return (Future)(m.invoke(proxy));
	}
}
