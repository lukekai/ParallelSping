package com.alibaba.crm.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.crm.test.annotation.Asynchronized;

public class RegularService {

	@Autowired
	private AsyncIntf asyncBean;

	@Asynchronized
	public void executeLongJob() {
		Map<String, Future<Integer>> m= new HashMap<String, Future<Integer>>();
		System.out.println("begin RegularService");
		Future<Integer> fr1 = asyncBean.executeLongJob() ;
		m.put("future1", fr1);
		
		Future<Integer> fr2 = asyncBean.executeLongJob() ;
		m.put("future2", fr2);
		
		System.out.println("after RegularService");
		try {
			int result = fr1.get();
			System.out.println("result is "+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
