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
		// ��Ҫ�޸ĵķ�������
		String mname = "test";
		CtMethod mold = ctClass.getDeclaredMethod(mname);
		// �޸�ԭ�еķ�������
		String nname = mname + "$impl";
		mold.setName(nname);
		// �����µķ���������ԭ���ķ���
		CtMethod mnew = CtNewMethod.copy(mold, mname, ctClass, null);
		// ��Ҫ��ע�����
		StringBuffer body = new StringBuffer();
		body.append("{\nlong start = System.currentTimeMillis();\n");
		// ����ԭ�д��룬������method();($$)��ʾ���еĲ���
		body.append(nname + "($$);\n");
		body.append("System.out.println(\"Call to method " + mname
				+ " took \" +\n (System.currentTimeMillis()-start) + "
				+ "\" ms.\");\n");

		body.append("}");
		// �滻�·���
		mnew.setBody(body.toString());
		// �����·���
		ctClass.addMethod(mnew);
		// ���Ѿ����ģ�ע�ⲻ��ʹ��A a=new A();����Ϊ��ͬһ��classloader�У�������װ��ͬһ��������
		return ctClass.toClass();
		// A a=(A)ctClass.toClass().newInstance();
		// a.method();

	}
}
