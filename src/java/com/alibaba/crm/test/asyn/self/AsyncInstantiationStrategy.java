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
//        //��Ҫ�޸ĵķ������� 
//       String mname = "method";        
//       CtMethod mold = ctClass.getDeclaredMethod(mname); 
//        //�޸�ԭ�еķ������� 
//       String nname = mname + "$impl"; 
//       mold.setName(nname); 
//        //�����µķ���������ԭ���ķ��� 
//       CtMethod mnew = CtNewMethod.copy(mold, mname, ctClass, null); 
//        //��Ҫ��ע����� 
//       StringBuffer body = new StringBuffer(); 
//       body.append("{\nlong start = System.currentTimeMillis();\n"); 
//       //����ԭ�д��룬������method();($$)��ʾ���еĲ��� 
//       body.append(nname + "($$);\n"); 
//       body.append("System.out.println(\"Call to method " 
//                   + mname 
//                   + " took \" +\n (System.currentTimeMillis()-start) + " 
//                   + "\" ms.\");\n"); 
//      
//       body.append("}"); 
//        //�滻�·��� 
//       mnew.setBody(body.toString()); 
//        //�����·��� 
//       ctClass.addMethod(mnew); 
       //���Ѿ����ģ�ע�ⲻ��ʹ��A a=new A();����Ϊ��ͬһ��classloader�У�������װ��ͬһ�������� 
//       A a=(A)ctClass.toClass().newInstance(); 
//       a.method();      
       }
      

}  