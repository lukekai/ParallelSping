package com.alibaba.crm.test.spring.parallel;

import java.lang.annotation.Annotation;
import java.util.concurrent.Executor;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.AsyncAnnotationAdvisor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

@Component
public class ParallelAnnotationBeanPostProcessor extends ProxyConfig implements
		BeanPostProcessor, BeanClassLoaderAware, InitializingBean, Ordered {

	private Class<? extends Annotation> parallelAnnotationType;

	private Executor executor;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	private ParallelAnnotationAdvisor parallelAnnotationAdvisor;

	/**
	 * This should run after all other post-processors, so that it can just add
	 * an advisor to existing proxies rather than double-proxy.
	 */
	private int order = Ordered.LOWEST_PRECEDENCE;

	/**
	 * Set the 'async' annotation type to be detected at either class or method
	 * level. By default, both the {@link Async} annotation and the EJB 3.1
	 * <code>javax.ejb.Asynchronous</code> annotation will be detected.
	 * <p>
	 * This setter property exists so that developers can provide their own
	 * (non-Spring-specific) annotation type to indicate that a method (or all
	 * methods of a given class) should be invoked asynchronously.
	 * 
	 * @param asyncAnnotationType
	 *            the desired annotation type
	 */
	public void setParallelAnnotationType(
			Class<? extends Annotation> parallelAnnotationType) {
		Assert.notNull(parallelAnnotationType,
				"'parallelAnnotationType' must not be null");
		this.parallelAnnotationType = parallelAnnotationType;
	}

	/**
	 * Set the {@link Executor} to use when invoking methods asynchronously.
	 */
	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	public void afterPropertiesSet() {
		this.parallelAnnotationAdvisor = (this.executor != null ? new ParallelAnnotationAdvisor(
				this.executor) : new ParallelAnnotationAdvisor());
		if (this.parallelAnnotationType != null) {
			this.parallelAnnotationAdvisor
					.setParallelAnnotationType(this.parallelAnnotationType);
		}
	}

	public int getOrder() {
		return this.order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) {
		if (bean instanceof AopInfrastructureBean) {
			// Ignore AOP infrastructure such as scoped proxies.
			return bean;
		}
		Class<?> targetClass = AopUtils.getTargetClass(bean);
		if (AopUtils.canApply(this.parallelAnnotationAdvisor, targetClass)) {
			if (bean instanceof Advised) {
				((Advised) bean).addAdvisor(0, this.parallelAnnotationAdvisor);
				return bean;
			} else {
				ProxyFactory proxyFactory = new ProxyFactory(bean);
				// Copy our properties (proxyTargetClass etc) inherited from
				// ProxyConfig.
				proxyFactory.copyFrom(this);
				proxyFactory.addAdvisor(this.parallelAnnotationAdvisor);
				
				if (!isInterfaced(bean))
					proxyFactory.setProxyTargetClass(true);
				
				Object obj = proxyFactory.getProxy(this.beanClassLoader);
				((ParalellizeIntf)obj).setProxyBean(obj);
				return obj;
			}
		} else {
			// No parallel proxy needed.
			return bean;
		}
	}
	public boolean isInterfaced(Object bean){
		Class[] intfs =  bean.getClass().getInterfaces();
		if (intfs.length==1 && intfs[0].equals(ParalellizeIntf.class)){
			return false;
		}else{
			return true;
		}
	}

}
