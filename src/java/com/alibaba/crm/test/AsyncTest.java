package com.alibaba.crm.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AsyncTest {
	
	public static void testAsync(){
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(new String[] {
		"beans.xml"});
		//RegularService regularService =(RegularService)appContext.getBean("regularService");
		//long b = System.currentTimeMillis();
		//regularService.executeLongJob();
		//regularService.executeLongJob();
		//long e = System.currentTimeMillis();
		//System.out.println("outter invoke time :"+(e-b));
		
		
		//AsyncIntf ab = appContext.getBean("asyncBean", AsyncIntf.class);
		//ab.testSelfSync();
		//ab.testSameClass();
		
		AsyncIntf ab = appContext.getBean("asyncBean", AsyncIntf.class);
		ab.executeLongJob();
		//ab.invoke();
	}
	
	public static void testParallel(){
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(new String[] {
		"beans-parallel.xml"});
		ParallelBean ab = appContext.getBean("parallelBean", ParallelBean.class);
		ab.executeLongJob();
		System.out.println("outter invoke");
	}
	
	
	public static void main(String[] args){
		testParallel();
	}

}
