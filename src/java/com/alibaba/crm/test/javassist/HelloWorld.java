package com.alibaba.crm.test.javassist;

public class HelloWorld implements java.io.Serializable{

	public static void sayHello(String hh) {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("hello world");
	}

}