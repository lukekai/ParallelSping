package com.alibaba.crm.test.asyn.self;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.CglibSubclassingInstantiationStrategy;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.SimpleInstantiationStrategy;
import org.springframework.stereotype.Component;

import com.alibaba.crm.test.annotation.Asynchronized;

@Component
public class AsyncInstantiationStrategy extends SimpleInstantiationStrategy implements BeanFactoryPostProcessor {  
    private CglibSubclassingInstantiationStrategy defaultInstantiationStrategy = new CglibSubclassingInstantiationStrategy();  
  
    public void postProcessBeanFactory(  
            ConfigurableListableBeanFactory beanFactory) throws BeansException {  
        if (beanFactory instanceof AbstractAutowireCapableBeanFactory) {  
            ((AbstractAutowireCapableBeanFactory)beanFactory).setInstantiationStrategy(this);  
        }  
    }  
      
    protected boolean proxyThis(RootBeanDefinition beanDefinition, String beanName) {  
        Class<?> beanClass = beanDefinition.getBeanClass();  
        Annotation annotation = beanClass.getAnnotation(Asynchronized.class);  
        if (annotation != null && !beanDefinition.getMethodOverrides().isEmpty())   
            throw new UnsupportedOperationException("Method Injection not supported with ProxyThis annotation.");  
        return annotation != null;  
    }  
  
    @Override  
    public Object instantiate(RootBeanDefinition beanDefinition,  
            String beanName, BeanFactory owner, Constructor ctor, Object[] args) {  
        if (proxyThis(beanDefinition, beanName)) {  
            return createProxy(beanDefinition.getBeanClass());  
        }  
        else {  
            return this.defaultInstantiationStrategy.instantiate(beanDefinition, beanName, owner, ctor, args);  
        }  
    }  
  
    @Override  
    public Object instantiate(RootBeanDefinition beanDefinition,  
            String beanName, BeanFactory owner) {  
        if (proxyThis(beanDefinition, beanName)) {  
            return createProxy(beanDefinition.getBeanClass());  
        }  
        else {  
            return this.defaultInstantiationStrategy.instantiate(beanDefinition, beanName, owner);  
        }  
    }  
      
    protected Object createProxy(final Class<?> beanClass) {
    	return null;
//    	CodeConverter convert = new CodeConverter();
////    	convert.
//    	
//    	CtClass ctClass = ClassPool.getDefault().get(beanClass.getCanonicalName()); 
//        //需要修改的方法名称 
//       String mname = "method";        
//       CtMethod mold = ctClass.getDeclaredMethod(mname); 
//        //修改原有的方法名称 
//       String nname = mname + "$impl"; 
//       mold.setName(nname); 
//        //创建新的方法，复制原来的方法 
//       CtMethod mnew = CtNewMethod.copy(mold, mname, ctClass, null); 
//        //主要的注入代码 
//       StringBuffer body = new StringBuffer(); 
//       body.append("{\nlong start = System.currentTimeMillis();\n"); 
//       //调用原有代码，类似于method();($$)表示所有的参数 
//       body.append(nname + "($$);\n"); 
//       body.append("System.out.println(\"Call to method " 
//                   + mname 
//                   + " took \" +\n (System.currentTimeMillis()-start) + " 
//                   + "\" ms.\");\n"); 
//      
//       body.append("}"); 
//        //替换新方法 
//       mnew.setBody(body.toString()); 
//        //增加新方法 
//       ctClass.addMethod(mnew); 
       //类已经更改，注意不能使用A a=new A();，因为在同一个classloader中，不允许装载同一个类两次 
//       A a=(A)ctClass.toClass().newInstance(); 
//       a.method();      
       }
      

}  