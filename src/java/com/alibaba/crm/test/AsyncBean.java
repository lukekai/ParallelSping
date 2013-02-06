package com.alibaba.crm.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.alibaba.crm.test.annotation.Asynchronized;


@Component
@Asynchronized
public class AsyncBean implements AsyncIntf{
	
	public void testSameClass() {
		Map<String, Future<Integer>> m= new HashMap<String, Future<Integer>>();
		System.out.println("begin AsyncBean");
		Future<Integer> fr1 = executeLongJob() ;
		m.put("future1", fr1);
		
		Future<Integer> fr2 = executeLongJob() ;
		m.put("future2", fr2);
		
		System.out.println("after AsyncBean");
		try {
			int result = fr1.get();
			System.out.println("result is "+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
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
	
	@Asynchronized
	public Future<Integer>  selfExecuteLongJob(){
		System.out.println("self begin long job");
		long b = System.currentTimeMillis();
		try {
			Thread.sleep(2*1000);
		} catch (InterruptedException e) {
		}
		System.out.println("after self long job,inner spend:"+(System.currentTimeMillis()-b));
		return new AsyncResult(1);
		
	}


	@Override
	public void testSelfSync() {
		selfExecuteLongJob();
		
	}
}
