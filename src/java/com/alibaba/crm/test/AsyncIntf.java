package com.alibaba.crm.test;

import java.util.concurrent.Future;

public interface AsyncIntf {
	public Future<Integer>  executeLongJob();
	public void testSameClass();
	public void testSelfSync();


}
