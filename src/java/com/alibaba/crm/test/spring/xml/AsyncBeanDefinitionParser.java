package com.alibaba.crm.test.spring.xml;

import javassist.ClassPool;
import javassist.CodeConverter;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

public class AsyncBeanDefinitionParser extends
		AbstractSingleBeanDefinitionParser {

	protected Class getBeanClass(Element element) {
		String className = element.getAttribute("class");
		Class clazz = null;
		try {
			clazz = intecepteClass(className);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}
		return clazz;
	}

	protected void doParse(Element element, BeanDefinitionBuilder bean) {
		// this will never be null since the schema explicitly requires that a
		// value be supplied
		// String pattern = element.getAttribute("pattern");
		// bean.addConstructorArg(pattern);
		//
		// // this however is an optional property
		// String lenient = element.getAttribute("lenient");
		// if (StringUtils.hasText(lenient)) {
		// bean.addPropertyValue("lenient", Boolean.valueOf(lenient));
		// }
	}

	private Class intecepteClass(String className) throws Exception {
		CodeConverter convert = new CodeConverter();
		// convert.

		CtClass ctClass = ClassPool.getDefault().get(className);
		// 需要修改的方法名称
		String mname = "test";
		CtMethod mold = ctClass.getDeclaredMethod(mname);
		// 修改原有的方法名称
		String nname = mname + "$impl";
		mold.setName(nname);
		// 创建新的方法，复制原来的方法
		CtMethod mnew = CtNewMethod.copy(mold, mname, ctClass, null);
		// 主要的注入代码
		StringBuffer body = new StringBuffer();
		body.append("{\nlong start = System.currentTimeMillis();\n");
		// 调用原有代码，类似于method();($$)表示所有的参数
		body.append(nname + "($$);\n");
		body.append("System.out.println(\"Call to method " + mname
				+ " took \" +\n (System.currentTimeMillis()-start) + "
				+ "\" ms.\");\n");

		body.append("}");
		// 替换新方法
		mnew.setBody(body.toString());
		// 增加新方法
		ctClass.addMethod(mnew);
		// 类已经更改，注意不能使用A a=new A();，因为在同一个classloader中，不允许装载同一个类两次
		return ctClass.toClass();
		// A a=(A)ctClass.toClass().newInstance();
		// a.method();

	}
}
