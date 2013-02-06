package com.alibaba.crm.test.asyn.self;

import java.io.IOException;
import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class AsyncMethodFactory {
	public static void injectAsyncMethod(Method method) throws Exception{
		ClassPool pool = ClassPool.getDefault();
		CtClass cc =  pool.get(method.getDeclaringClass().getName());
		
		
		CtMethod cm = cc.getDeclaredMethod(method.getName(), transferClass(method.getParameterTypes()));
		cc.removeMethod(cm);
		cm.insertBefore("{ System.out.println(\"123456\");}");
		cc.addMethod(cm);
	}
	
	private static  CtClass[] transferClass(Class[] classes) throws NotFoundException{
		if (classes==null)
			return null;
		CtClass[] ctclasses = new CtClass[classes.length];
		ClassPool pool = ClassPool.getDefault();
		for (int i=0;i<classes.length;i++){
			ctclasses[i] = pool.get(classes[i].getName());
		}
		return ctclasses;
	}
	
	
	 public static void addTiming(Method method)
		        throws NotFoundException, CannotCompileException, IOException {
		 
			ClassPool pool = ClassPool.getDefault();
			CtClass clas =  pool.get(method.getDeclaringClass().getName());
			String mname = method.getName();
		 
		        
		        //  get the method information (throws exception if method with
		        //  given name is not declared directly by this class, returns
		        //  arbitrary choice if more than one with the given name)
		        CtMethod mold = clas.getDeclaredMethod(mname);
		        
		        //  rename old method to synthetic name, then duplicate the
		        //  method with original name for use as interceptor
		        String nname = mname+"$impl";
		        mold.setName(nname);
		        CtMethod mnew = CtNewMethod.copy(mold, mname, clas, null);
		        
		        //  start the body text generation by saving the start time
		        //  to a local variable, then call the timed method; the
		        //  actual code generated needs to depend on whether the
		        //  timed method returns a value
		        String type = mold.getReturnType().getName();
		        StringBuffer body = new StringBuffer();
		        body.append("{\nlong start = System.currentTimeMillis();\n");
		        if (!"void".equals(type)) {
		            body.append(type + " result = ");
		        }
		        body.append(nname + "($$);\n");
		        
		        //  finish body text generation with call to print the timing
		        //  information, and return saved value (if not void)
		        body.append("System.out.println(\"Call to method " + mname +
		            " took \" +\n (System.currentTimeMillis()-start) + " +
		            "\" ms.\");\n");
		        if (!"void".equals(type)) {
		            body.append("return result;\n");
		        }
		        body.append("}");
		        
		        //  replace the body of the interceptor method with generated
		        //  code block and add it to class
		        mnew.setBody(body.toString());
		        clas.addMethod(mnew);
		        
		        //  print the generated code block just to show what was done
		        System.out.println("Interceptor method body:");
		        System.out.println(body.toString());
		        clas.writeFile();
		    }
}
