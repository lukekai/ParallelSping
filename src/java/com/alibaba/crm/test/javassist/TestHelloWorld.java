package com.alibaba.crm.test.javassist;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import java.lang.StringBuilder;

/**
 * 这个例子里用javassist在加载类的时候，给HelloWorld这个类的sayHello方法动态的加入统计方法时间的代码
 * 
 * @author seeeyou
 * 
 */
public class TestHelloWorld {
	public static void main(String[] args) throws NotFoundException,
			InstantiationException, IllegalAccessException,
			CannotCompileException {
//		Class c = HelloWorld.class;
		
		// 用于取得字节码类，必须在当前的classpath中，使用全称
		CtClass ctClass = ClassPool.getDefault().getCtClass(
				"com.alibaba.crm.test.javassist.HelloWorld");
		// 需要修改的方法名称
		String mname = "sayHello";
		// 新定义一个方法叫做sayHello$impl
		String newMethodName = mname + "$impl";
		// 获取这个方法
		CtMethod cm = ctClass.getDeclaredMethod(mname);
		cm.setName(newMethodName);// 原来的方法改个名字

		// 创建新的方法，复制原来的方法
		CtMethod newMethod = CtNewMethod.copy(cm, mname, ctClass, null);

		StringBuilder bodyStr = new StringBuilder();
		bodyStr.append("{\nlong start = System.currentTimeMillis();\n");
		// 调用原有代码，类似于method();($$)表示所有的参数
		bodyStr.append(newMethodName + "($$);\n");

		bodyStr.append("System.out.println(\"Call to method " + mname
				+ " took \" +\n (System.currentTimeMillis()-start) + "
				+ "\" ms.\");\n");

		bodyStr.append("}");
		// 替换新方法
		newMethod.setBody(bodyStr.toString());
		// 增加新方法
		ctClass.addMethod(newMethod);

		// 类已经更改，注意不能使用HelloWorld a=new
		// HelloWorld();，因为在同一个classloader中，不允许装载同一个类两次

		HelloWorld helloWorld = (HelloWorld) ctClass.toClass().newInstance();
		helloWorld.sayHello("add");
		System.out.println(helloWorld.getClass());

	}
}
