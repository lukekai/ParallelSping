package com.alibaba.crm.test.spring.parallel;

import org.springframework.aop.SpringProxy;


public interface ParalellizeIntf extends SpringProxy{
	public void setProxyBean(Object obj);
	public void getProxyBean();
	public boolean isProxyInvoke();
	public void openProxyTag();
	public void closeProxyTag();
}
