package com.alibaba.crm.test.spring.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class AsyncNamespaceHandler extends NamespaceHandlerSupport {
    
    public void init() {
        registerBeanDefinitionParser("asyncbean", new AsyncBeanDefinitionParser());        
    }
}