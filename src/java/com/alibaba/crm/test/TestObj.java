package com.alibaba.crm.test;

import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.AsyncResult;

public class TestObj {
	public TestObj (String a, String b){};
	public Future test(){
		return new AsyncResult(1);
	}
	
}
