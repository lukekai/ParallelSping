package com.alibaba.crm.test.asyn.self;

import java.lang.reflect.Method;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.alibaba.crm.test.AsyncBean;
import com.alibaba.crm.test.annotation.Asynchronized;

@Component
public class AsynchronousAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter{
//	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//		ReflectionUtils.doWithMethods(bean.getClass(), new ReflectionUtils.MethodCallback() {
//			
//			@Override
//			public void doWith(Method method) throws IllegalArgumentException,
//					IllegalAccessException {
//				Asynchronous a = method.getAnnotation(Asynchronous.class);
//				if (a!=null){
//					System.out.println(method.getDeclaringClass().getName()+":"+method.getName() +" is defined to asyncronized");
//				}
//			}
//		} );
//		
//		return bean;
//	}
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		ReflectionUtils.doWithMethods(beanClass, new ReflectionUtils.MethodCallback() {
			
			@Override
			public void doWith(Method method) throws IllegalArgumentException,
					IllegalAccessException {
				Asynchronized a = method.getAnnotation(Asynchronized.class);
				if (a!=null){
					System.out.println(method.getDeclaringClass().getName()+":"+method.getName() +" is defined to asyncronized");
					try {
//						AsyncMethodFactory.injectAsyncMethod(method);
						AsyncMethodFactory.addTiming(method);
//						method.invoke(bean);
						AsyncBean ab = new AsyncBean();
						Method m = ab.getClass().getMethod("executeLongJob", null);
//						m.invoke(ab);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} );
		return null;
	}

	
//	public Object postProcessBeforeInitialization(final Object bean, String beanName) throws BeansException {
//		ReflectionUtils.doWithMethods(bean.getClass(), new ReflectionUtils.MethodCallback() {
//			
//			@Override
//			public void doWith(Method method) throws IllegalArgumentException,
//					IllegalAccessException {
//				Asynchronized a = method.getAnnotation(Asynchronized.class);
//				if (a!=null){
//					System.out.println(method.getDeclaringClass().getName()+":"+method.getName() +" is defined to asyncronized");
//					try {
////						AsyncMethodFactory.injectAsyncMethod(method);
//						AsyncMethodFactory.addTiming(method);
////						method.invoke(bean);
//						AsyncBean ab = new AsyncBean();
//						Method m = ab.getClass().getMethod("executeLongJob", null);
//						m.invoke(ab);
//						
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		} );
//		
//		return bean;
//	}

}
