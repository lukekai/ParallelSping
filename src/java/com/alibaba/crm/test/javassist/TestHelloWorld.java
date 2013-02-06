package com.alibaba.crm.test.javassist;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import java.lang.StringBuilder;

/**
 * �����������javassist�ڼ������ʱ�򣬸�HelloWorld������sayHello������̬�ļ���ͳ�Ʒ���ʱ��Ĵ���
 * 
 * @author seeeyou
 * 
 */
public class TestHelloWorld {
	public static void main(String[] args) throws NotFoundException,
			InstantiationException, IllegalAccessException,
			CannotCompileException {
//		Class c = HelloWorld.class;
		
		// ����ȡ���ֽ����࣬�����ڵ�ǰ��classpath�У�ʹ��ȫ��
		CtClass ctClass = ClassPool.getDefault().getCtClass(
				"com.alibaba.crm.test.javassist.HelloWorld");
		// ��Ҫ�޸ĵķ�������
		String mname = "sayHello";
		// �¶���һ����������sayHello$impl
		String newMethodName = mname + "$impl";
		// ��ȡ�������
		CtMethod cm = ctClass.getDeclaredMethod(mname);
		cm.setName(newMethodName);// ԭ���ķ����ĸ�����

		// �����µķ���������ԭ���ķ���
		CtMethod newMethod = CtNewMethod.copy(cm, mname, ctClass, null);

		StringBuilder bodyStr = new StringBuilder();
		bodyStr.append("{\nlong start = System.currentTimeMillis();\n");
		// ����ԭ�д��룬������method();($$)��ʾ���еĲ���
		bodyStr.append(newMethodName + "($$);\n");

		bodyStr.append("System.out.println(\"Call to method " + mname
				+ " took \" +\n (System.currentTimeMillis()-start) + "
				+ "\" ms.\");\n");

		bodyStr.append("}");
		// �滻�·���
		newMethod.setBody(bodyStr.toString());
		// �����·���
		ctClass.addMethod(newMethod);

		// ���Ѿ����ģ�ע�ⲻ��ʹ��HelloWorld a=new
		// HelloWorld();����Ϊ��ͬһ��classloader�У�������װ��ͬһ��������

		HelloWorld helloWorld = (HelloWorld) ctClass.toClass().newInstance();
		helloWorld.sayHello("add");
		System.out.println(helloWorld.getClass());

	}
}
