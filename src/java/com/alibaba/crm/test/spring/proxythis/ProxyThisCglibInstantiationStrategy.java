package com.alibaba.crm.test.spring.proxythis;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.Annotation;
import java.util.Arrays;
import java.util.Stack;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodProxy;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopContext;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.CglibSubclassingInstantiationStrategy;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.SimpleInstantiationStrategy;

public class ProxyThisCglibInstantiationStrategy extends
		SimpleInstantiationStrategy implements BeanFactoryPostProcessor {
	private CglibSubclassingInstantiationStrategy defaultInstantiationStrategy = new CglibSubclassingInstantiationStrategy();

	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (beanFactory instanceof AbstractAutowireCapableBeanFactory) {
			((AbstractAutowireCapableBeanFactory) beanFactory)
					.setInstantiationStrategy(this);
		}
	}

	protected boolean proxyThis(RootBeanDefinition beanDefinition,
			String beanName) {
		Class<?> beanClass = beanDefinition.getBeanClass();
		Annotation annotation = (Annotation) beanClass
				.getAnnotation(ProxyThis.class);
		if (annotation != null
				&& !beanDefinition.getMethodOverrides().isEmpty())
			throw new UnsupportedOperationException(
					"Method Injection not supported with ProxyThis annotation.");
		return annotation != null;
	}

	@Override
	public Object instantiate(RootBeanDefinition beanDefinition,
			String beanName, BeanFactory owner, Constructor ctor, Object[] args) {
		if (proxyThis(beanDefinition, beanName)) {
			return createProxy(beanDefinition.getBeanClass());
		} else {
			return this.defaultInstantiationStrategy.instantiate(
					beanDefinition, beanName, owner, ctor, args);
		}
	}

	@Override
	public Object instantiate(RootBeanDefinition beanDefinition,
			String beanName, BeanFactory owner) {
		if (proxyThis(beanDefinition, beanName)) {
			return createProxy(beanDefinition.getBeanClass());
		} else {
			return this.defaultInstantiationStrategy.instantiate(
					beanDefinition, beanName, owner);
		}
	}

	protected Object createProxy(final Class<?> beanClass) {
		Enhancer enhancer = new Enhancer();
		enhancer.setCallback(new ProxyThisHandler());
		enhancer.setSuperclass(beanClass);
		return enhancer.create();
	}

	private static class ProxyThisHandler implements
			net.sf.cglib.proxy.MethodInterceptor {

		public Object intercept(Object obj, Method method, Object[] args,
				MethodProxy proxy) throws Throwable {
			if (firstInvoke.get().isEmpty() || firstInvoke.get().peek()) {// from
																			// external
																			// invocation
				firstInvoke.get().push(false);
				try {
					// super.method(args)
					return proxy.invokeSuper(obj, args);
				} finally {
					firstInvoke.get().pop();
				}
			} else { // from inner invocation
				Object targetProxy;
				firstInvoke.get().push(true);
				try {
					try {
						targetProxy = AopContext.currentProxy();
					} catch (IllegalStateException e) {
						return proxy.invokeSuper(obj, args);
					}
					// ((Bean)AopContext.currentProxy()).method(args)
					return proxy.invoke(targetProxy, args);
				} finally {
					firstInvoke.get().pop();
				}
			}
		}

	}

	private static final ThreadLocal<Stack<Boolean>> firstInvoke = new ThreadLocal<Stack<Boolean>>() {
		@Override
		protected Stack<Boolean> initialValue() {
			return new Stack<Boolean>();
		}
	};

	public static class Bean {
		private String name = "default";

		public String getName() {
			return name;
		}

		public String getNameProxy() {
			return this.name;
		}

		public String getNameMethodProxy() {
			return this.getName();
		}
	}

	public static class ReplaceReturnAdvice implements MethodInterceptor {
		public Object returnValue;

		public ReplaceReturnAdvice(Object returnValue) {
			this.returnValue = returnValue;
		}

		public Object invoke(MethodInvocation arg0) throws Throwable {
			return returnValue;
		}

	}

	public static class MethodNamesAdvisor extends
			StaticMethodMatcherPointcutAdvisor {
		private String[] methodnames;

		public MethodNamesAdvisor(Advice advice, String... methodnames) {
			super(advice);
			this.methodnames = methodnames;
		}

		public boolean matches(Method method, Class targetClass) {
			return Arrays.asList(this.methodnames).contains(method.getName());
		}

	}

	public void testInteceptorMethodInvokedFromWithinTarget() {
		ProxyFactory p1 = new ProxyFactory(
				new ProxyThisCglibInstantiationStrategy()
						.createProxy(Bean.class));
		p1.setExposeProxy(true);
		p1.setProxyTargetClass(true);
		p1.addAdvisor(new MethodNamesAdvisor(new ReplaceReturnAdvice("mike"),
				"getName", "setName"));
		Bean b1 = (Bean) p1.getProxy();
		System.out.println(b1.getName());
		System.out.println(b1.getNameMethodProxy());
		System.out.println(b1.getNameProxy());
	}
	public static void main(String[] args){
		ProxyThisCglibInstantiationStrategy p = new ProxyThisCglibInstantiationStrategy();
		p.testInteceptorMethodInvokedFromWithinTarget();
	}
}