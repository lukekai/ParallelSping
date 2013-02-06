package com.alibaba.crm.test.spring.parallel;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.util.Assert;

import com.alibaba.crm.test.annotation.Parallel;

public class ParallelAnnotationAdvisor  extends AbstractPointcutAdvisor {

	private Advice advice;

	private Pointcut pointcut;


	/**
	 * Create a new ConcurrencyAnnotationBeanPostProcessor for bean-style configuration.
	 */
	public ParallelAnnotationAdvisor() {
		this(new SimpleAsyncTaskExecutor());
	}

	/**
	 * Create a new ConcurrencyAnnotationBeanPostProcessor for the given task executor.
	 * @param executor the task executor to use for asynchronous methods
	 */
	@SuppressWarnings("unchecked")
	public ParallelAnnotationAdvisor(Executor executor) {
		Set<Class<? extends Annotation>> parallelAnnotationTypes = new LinkedHashSet<Class<? extends Annotation>>(2);
		parallelAnnotationTypes.add(Parallel.class);
		ClassLoader cl = ParallelAnnotationAdvisor.class.getClassLoader();
		this.advice = buildAdvice(executor);
		this.pointcut = buildPointcut(parallelAnnotationTypes);
	}

	/**
	 * Specify the task executor to use for asynchronous methods.
	 */
	public void setTaskExecutor(Executor executor) {
		this.advice = buildAdvice(executor);
	}

	/**
	 * Set the 'async' annotation type.
	 * <p>The default async annotation type is the {@link Async} annotation, as well
	 * as the EJB 3.1 <code>javax.ejb.Asynchronous</code> annotation (if present).
	 * <p>This setter property exists so that developers can provide their own
	 * (non-Spring-specific) annotation type to indicate that a method is to
	 * be executed asynchronously.
	 * @param asyncAnnotationType the desired annotation type
	 */
	public void setParallelAnnotationType(Class<? extends Annotation> asyncAnnotationType) {
		Assert.notNull(asyncAnnotationType, "'asyncAnnotationType' must not be null");
		Set<Class<? extends Annotation>> asyncAnnotationTypes = new HashSet<Class<? extends Annotation>>();
		asyncAnnotationTypes.add(asyncAnnotationType);
		this.pointcut = buildPointcut(asyncAnnotationTypes);
	}


	public Advice getAdvice() {
		return this.advice;
	}

	public Pointcut getPointcut() {
		return this.pointcut;
	}


	protected Advice buildAdvice(Executor executor) {
		if (executor instanceof AsyncTaskExecutor) {
			return new ParallelExecutionInterceptor((AsyncTaskExecutor) executor);
		}
		else {
			return new ParallelExecutionInterceptor(executor);
		}
	}

	/**
	 * Calculate a pointcut for the given target class, if any.
	 * @param targetClass the class to introspect
	 * @return the applicable Pointcut object, or <code>null</code> if none
	 */
	protected Pointcut buildPointcut(Set<Class<? extends Annotation>> asyncAnnotationTypes) {
		ComposablePointcut result = null;
		for (Class<? extends Annotation> asyncAnnotationType : asyncAnnotationTypes) {
			Pointcut cpc = new AnnotationMatchingPointcut(asyncAnnotationType, true);
			Pointcut mpc = new AnnotationMatchingPointcut(null, asyncAnnotationType);
			if (result == null) {
				result = new ComposablePointcut(cpc).union(mpc);
			}
			else {
				result.union(cpc).union(mpc);
			}
		}
		return result;
	}

}
