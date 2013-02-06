package com.alibaba.crm.test.spring.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ReflectionUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

import com.alibaba.crm.test.TestObj;
import com.alibaba.crm.test.annotation.Parallel;

public class ParallelBeanDefinitionDecorator implements BeanDefinitionDecorator {
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	public BeanDefinitionHolder decorate(Node source,
			BeanDefinitionHolder holder, ParserContext ctx) {
		String toParallelStr = ((Attr) source).getValue();
		if (toParallelStr.trim().equalsIgnoreCase("true")) {
		      AbstractBeanDefinition definition = ((AbstractBeanDefinition) holder.getBeanDefinition());
		      try {
//				intecepteClass(definition.getBeanClassName());
				parallelClass(definition.getBeanClassName(),holder, ctx);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return holder;
	}
	
	
//	private Class intecepteClass(String className) throws Exception {
//
//		CtClass ctClass = ClassPool.getDefault().get(className);
//		
//		// 需要修改的方法名称
//		String mname = "test";
//		CtMethod mold = ctClass.getDeclaredMethod(mname);
//		
//		// 修改原有的方法名称
//		String nname = mname + "$impl";
//		mold.setName(nname);
//		// 创建新的方法，复制原来的方法
//		CtMethod mnew = CtNewMethod.copy(mold, mname, ctClass, null);
//		// 主要的注入代码
//		StringBuffer body = new StringBuffer();
//		body.append("{\nlong start = System.currentTimeMillis();\n");
//		// 调用原有代码，类似于method();($$)表示所有的参数
//		body.append(nname + "($$);\n");
//		body.append("System.out.println(\"###########Call to method " + mname
//				+ " took \" +\n (System.currentTimeMillis()-start) + "
//				+ "\" ms.\");\n");
//
//		body.append("}");
//		// 替换新方法
//		mnew.setBody(body.toString());
//		// 增加新方法
//		ctClass.addMethod(mnew);
//		// 类已经更改，注意不能使用A a=new A();，因为在同一个classloader中，不允许装载同一个类两次
//		return ctClass.toClass();
//		// A a=(A)ctClass.toClass().newInstance();
//		// a.method();
//
//	}
	private Class parallelClass(String className, BeanDefinitionHolder holder, ParserContext ctx)throws Exception {
		CtClass ctClass = ClassPool.getDefault().get(className);
		List<CtMethod> pMethodList = getParallelAnnotationMethods(ctClass);
		Set<String> annotationNameSet = new HashSet<String>();
		for (CtMethod ctMethod: pMethodList){
			Parallel pa = (Parallel)ctMethod.getAnnotation(Parallel.class);
			if (!annotationNameSet.contains(pa.poolName())){
				CtField poolField = genPoolField(pa.poolName(),ctClass);
				ctClass.addField(poolField);
				ctClass.addMethod(genPoolSetter(poolField));
				
				registerTaskPoolBeanDefinition(ctx, pa);
				annotationNameSet.add(pa.poolName());

			}
			String newName = getNewOriginMethodName(ctMethod);
			ctMethod.setName(newName);
			// 创建新的方法，复制原来的方法
			CtMethod parMethod =  genParallelMethod(ctClass, ctMethod, pa.poolName());
			ctClass.addMethod(parMethod);

		}
		
		createDependencyOnTaskPool(holder, annotationNameSet);

		return ctClass.toClass();
		
	}
	public static String upFirstChar(String fieldName){
		StringBuilder sbName = new StringBuilder();
		char[] tableCharArray = fieldName.toCharArray();
		int i=0;
		for (char c: tableCharArray){
			if (i==0 &&(c>='a' && c<='z')){
				sbName.append((char)(c-32));
			}else{
				sbName.append(c);
			}
			i++;
		}
		return sbName.toString();
	}

	private CtField genPoolField(String poolName, CtClass ctClass) throws CannotCompileException, NotFoundException{
		CtField cf = new CtField(ClassPool.getDefault().get("org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor"),
				poolName,
				ctClass);
		return cf;
	}
	
	
	private CtMethod genPoolSetter(CtField field) throws CannotCompileException{
		CtMethod setMethod = CtNewMethod.setter("set"+upFirstChar(field.getName()), field);
		return setMethod;
	}
	
	private void createDependencyOnTaskPool(BeanDefinitionHolder holder, Set<String> poolBeanNames) {
	      AbstractBeanDefinition definition = ((AbstractBeanDefinition) holder.getBeanDefinition());
	      String[] dependsOn = definition.getDependsOn();
	      if (dependsOn == null) {
	    	  dependsOn = poolBeanNames.toArray(EMPTY_STRING_ARRAY);
	      } else {
	         List<String> dependencies = new ArrayList<String>(Arrays.asList(dependsOn));
	         dependencies.addAll(poolBeanNames);
	         dependsOn = dependencies.toArray(EMPTY_STRING_ARRAY);
	      }
	      definition.setDependsOn(dependsOn);
	   }

	   private String registerTaskPoolBeanDefinition(ParserContext ctx, Parallel para) {
	      
	      if (!ctx.getRegistry().containsBeanDefinition(para.poolName())) {
	         BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(ThreadPoolTaskExecutor.class);
	         bdb.addPropertyValue("corePoolSize", para.corePoolSize());
	         bdb.addPropertyValue("maxPoolSize", para.maxPoolSize());
	         bdb.addPropertyValue("queueCapacity", para.queueCapacity());
	         ctx.getRegistry().registerBeanDefinition(para.poolName(), bdb.getBeanDefinition());
	      }
	      return para.poolName();
	   }
	   private String getNewOriginMethodName(CtMethod originMethod){
			String methodName =originMethod.getName();
			String originName = methodName + "$$origin";
			return originName;
	   }
//		private CtMethod genParallelMethod(CtClass ctClass, CtMethod originMethod, String poolName) throws CannotCompileException, NotFoundException{
//			String methodName =originMethod.getName();
//			String originName = methodName;
//
//			CtMethod mnew = CtNewMethod.copy(originMethod, methodName, ctClass, null);
//			StringBuffer body = new StringBuffer();
//			body.append("\njava.util.concurrent.Future result = this.").append(poolName).append(".submit(new java.util.concurrent.Callable<Object>() {\n");
//			body.append("	public Object call() throws Exception {\n");
//			body.append("		try {\n");
//			body.append("			Object result ="+originName + "($$);\n");
//			body.append("			if (result instanceof java.util.concurrent.Future) {\n");
//			body.append("				return ((java.util.concurrent.Future) result).get();\n");
//			body.append("			}\n");
//			body.append("		}catch (Throwable ex) {\n");
//			body.append("		ReflectionUtils.rethrowException(ex);}\n");
//			body.append("		return null;\n");
//			body.append("		}\n");
//			body.append("	});\n");
//			body.append("	if (java.util.concurrent.Future.class.isAssignableFrom("+originMethod.getReturnType().getClass()+")) {\n");
//			body.append("		return result;}\n");
//			body.append("	else {return null;}\n");
//			body.append("	\n");
//			body.append("	\n");
//			
//			mnew.setBody(body.toString());
//			return mnew;
//
//		}
	
	private List<CtMethod> getParallelAnnotationMethods(CtClass ctClass){
		List<CtMethod> methods = new ArrayList<CtMethod>();
		try {
			for (CtMethod method: ctClass.getMethods()){
				if (method.getAnnotation(Parallel.class)!=null){
					methods.add(method);
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return methods;
	}
	org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor abc;
	public void incoke(){
		{

			java.util.concurrent.Future result = abc.submit(new java.util.concurrent.Callable() {	public Object call() throws Exception {		return null;}});
					}

	}
	public static class poolclass implements java.util.concurrent.Callable{
		ParallelBeanDefinitionDecorator p;
		public poolclass(){
			super();
		}
		public Object call(){
			p.incoke();
			return null;
		}
	}
	private CtClass makeInnerClass(CtClass ctClass, CtMethod originMethod) throws Exception{
		CtClass innerClass = ctClass.makeNestedClass(originMethod.getName()+"Class", true);
		innerClass.addInterface(ClassPool.getDefault().get("java.util.concurrent.Callable"));
		CtField cf = new CtField(ctClass,"originObj",	innerClass);
		CtMethod settMethod = CtNewMethod.setter("setOriginObj", cf);
		innerClass.addField(cf);
		innerClass.addMethod(settMethod);
		StringBuffer body= new StringBuffer();
		body.append("{\n");
		body.append("	System.out.println(\"enter call\");\n");
		body.append("	Object result =originObj."+originMethod.getName() + "($$);\n");
		body.append("	return result;\n");
		body.append("}\n");

		//参数：  1：返回类型  2：方法名称  3：传入参数类型  4：所属类CtClass  
        CtMethod callMethod=new CtMethod(ClassPool.getDefault().get("java.lang.Object"),"call",new CtClass[]{},innerClass);  
        callMethod.setBody(body.toString());
        callMethod.setExceptionTypes(new CtClass[]{ClassPool.getDefault().get("java.lang.Exception")});
		innerClass.addMethod(callMethod);
		CtConstructor c = CtNewConstructor.make(innerClass.getSimpleName()+"(){}\n}", innerClass);
		innerClass.addConstructor(c);
		return innerClass;
	}
	
	private CtMethod genParallelMethod(CtClass ctClass, CtMethod originMethod, String poolName) throws Exception{
		CtClass innerClass = makeInnerClass(ctClass, originMethod);
		innerClass.toClass();
		String methodName =originMethod.getName();
		String originName = methodName;

		CtMethod mnew = CtNewMethod.copy(originMethod, methodName, ctClass, null);
		StringBuffer body = new StringBuffer();
		body.append("{\n");
		body.append(	innerClass.getName()+" call = new "+innerClass.getName()+"();\n");
		body.append("	call.setOriginObj(this);\n");
		body.append("	System.out.println(\"before call\");\n");
		body.append("	if("+poolName+"==null) System.out.println(\"pool is null.\");\n");
		body.append("	\njava.util.concurrent.Future result = ").append(poolName).append(".submit(call);");
		body.append("	return result;\n");
		body.append("}\n");
		
		mnew.setBody(body.toString());
		mnew.setName("test");
		return mnew;

	}

	public static void main(String[] args) throws Exception{
		ParallelBeanDefinitionDecorator p = new ParallelBeanDefinitionDecorator();
		CtClass ctClass = ClassPool.getDefault().get("com.alibaba.crm.test.TestObj");
		CtField poolField = p.genPoolField("abc",ctClass);
		ctClass.addField(poolField);
		ctClass.addMethod(p.genPoolSetter(poolField));
		
		for (CtMethod ctMethod:ctClass.getMethods()){
			if (ctMethod.getName().equals("test")){
				String newName = p.getNewOriginMethodName(ctMethod);
				ctMethod.setName(newName);
			// 	创建新的方法，复制原来的方法
				CtMethod parMethod =  p.genParallelMethod(ctClass, ctMethod, "abc");
				ctClass.addMethod(parMethod);
			}
		}
		
		ctClass.toClass();
		TestObj t = new TestObj("a","b");
		
		t.test();
		


	}

}