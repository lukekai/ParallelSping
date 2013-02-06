package com.alibaba.crm.test.spring.parallel;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.Ordered;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

public class ParallelExecutionInterceptor  implements MethodInterceptor, Ordered {

	private final AsyncTaskExecutor asyncExecutor;


	/**
	 * Create a new AsyncExecutionInterceptor.
	 * @param asyncExecutor the Spring AsyncTaskExecutor to delegate to
	 */
	public ParallelExecutionInterceptor(AsyncTaskExecutor asyncExecutor) {
		Assert.notNull(asyncExecutor, "TaskExecutor must not be null");
		this.asyncExecutor = asyncExecutor;
	}

	/**
	 * Create a new AsyncExecutionInterceptor.
	 * @param asyncExecutor the <code>java.util.concurrent</code> Executor
	 * to delegate to (typically a {@link java.util.concurrent.ExecutorService}
	 */
	public ParallelExecutionInterceptor(Executor asyncExecutor) {
		this.asyncExecutor = new TaskExecutorAdapter(asyncExecutor);
	}


	public Object invoke(final MethodInvocation invocation) throws Throwable {
		Future result = this.asyncExecutor.submit(new Callable<Object>() {
			public Object call() throws Exception {
				ParalellizeIntf pi = (ParalellizeIntf)invocation.getThis();
				pi.openProxyTag();
				try {
					Object result = invocation.proceed();
					if (result instanceof Future) {
						return ((Future) result).get();
					}
				}
				catch (Throwable ex) {
					ReflectionUtils.rethrowException(ex);
				}finally{
					pi.closeProxyTag();
				}
				return null;
			}
		});
		if (Future.class.isAssignableFrom(invocation.getMethod().getReturnType())) {
			return result;
		}
		else {
			return null;
		}
	}

	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
