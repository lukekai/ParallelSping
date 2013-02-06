package com.alibaba.crm.test;

import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.AsyncResult;

import com.alibaba.crm.test.annotation.Parallel;

public class ParallelBean {
	@Parallel
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
}
