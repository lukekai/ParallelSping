package com.alibaba.crm.test;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.alibaba.crm.test.annotation.Asynchronized;

@Component
public class CGlibAsyncBean {
	private AsyncTaskExecutor asyncExecutor;

	String aa;
	@Async
	public Future<Integer>  executeLongJob(){
		System.out.println("begin long job");
		long b = System.currentTimeMillis();
		try {
			Thread.sleep(2*1000);
		} catch (InterruptedException e) {
		}
		System.out.println("after long job,inner spend:"+(System.currentTimeMillis()-b));
		return new AsyncResult(1);
		
	}
	
	public void invoke(){
		executeLongJob();
	}
	
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		Future result = this.asyncExecutor.submit(new Callable<Object>() {
			public Object call() throws Exception {
				try {
					aa="dsfd";
					Object result = invocation.proceed();
					if (result instanceof Future) {
						return ((Future) result).get();
					}
				}
				catch (Throwable ex) {
					ReflectionUtils.rethrowException(ex);
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

}
