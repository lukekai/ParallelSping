package com.alibaba.crm.test.spring.parallel;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;


public class ToParallelNamespaceHandler  extends NamespaceHandlerSupport {

   public void init() {
      super.registerBeanDefinitionDecoratorForAttribute("to-parallel",
            new ToParallelBeanDefinitionDecorator());
   }
}
