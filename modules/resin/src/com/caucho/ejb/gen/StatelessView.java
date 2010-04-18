/*
 * Copyright (c) 1998-2010 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.ejb.gen;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.TimedObject;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

import com.caucho.config.ConfigException;
import com.caucho.config.gen.BusinessMethodGenerator;
import com.caucho.config.gen.LifecycleInterceptor;
import com.caucho.config.gen.View;
import com.caucho.inject.Module;
import com.caucho.java.JavaWriter;
import com.caucho.util.L10N;

/**
 * Represents any stateless view.
 */
@Module
public class StatelessView<X,T> extends View<X,T> {
  private static final L10N L = new L10N(StatelessView.class);

  private StatelessGenerator<X> _statelessBean;

  private ArrayList<BusinessMethodGenerator<X,T>> _businessMethods
    = new ArrayList<BusinessMethodGenerator<X,T>>();

  private String _timeoutMethod;

  private LifecycleInterceptor _postConstructInterceptor;
  private LifecycleInterceptor _preDestroyInterceptor;

  public StatelessView(StatelessGenerator<X> bean, AnnotatedType<T> api)
  {
    super(bean, api);

    _statelessBean = bean;
  }

  public StatelessGenerator<X> getStatelessBean()
  {
    return _statelessBean;
  }

  public String getContextClassName()
  {
    return getStatelessBean().getClassName();
  }

  @Override
  public String getViewClassName()
  {
    return getViewClass().getJavaClass().getSimpleName() + "__EJBLocal";
  }

  @Override
  public String getBeanClassName()
  {
    // XXX: 4.0.7 CDI TCK package-private issues
    // return getViewClass().getSimpleName() + "__Bean";
    // return getStatelessBean().getClassName();
    return getBeanClass().getJavaClass().getName();
  }

  /**
   * Returns the introspected methods
   */
  @Override
  public ArrayList<BusinessMethodGenerator<X,T>> getMethods()
  {
    return _businessMethods;
  }

  /**
   * Introspects the APIs methods, producing a business method for each.
   */
  @Override
  public void introspect()
  {
    introspectImpl();

    introspectLifecycle(getBeanClass().getJavaClass());

    _postConstructInterceptor = new LifecycleInterceptor(PostConstruct.class);
    _postConstructInterceptor.introspect(getBeanClass());

    _preDestroyInterceptor = new LifecycleInterceptor(PreDestroy.class);
    _preDestroyInterceptor.introspect(getBeanClass());

    // XXX: type is incorrect here. Should be moved to stateless generator?
    introspectTimer((AnnotatedType<T>) getBeanClass());
  }

  /**
   * Introspects the APIs methods, producing a business method for
   * each.
   */
  private void introspectImpl()
  {
    AnnotatedType<T> apiClass = getViewClass();
    
    for (AnnotatedMethod<? super T> apiMethod : apiClass.getMethods()) {
      Method javaMethod = apiMethod.getJavaMember();
      
      if (javaMethod.getDeclaringClass().equals(Object.class))
        continue;
      if (javaMethod.getDeclaringClass().getName().startsWith("javax.ejb."))
        continue;
 
      if (javaMethod.getName().startsWith("ejb")) {
        throw new ConfigException(L.l("{0}: '{1}' must not start with 'ejb'.  The EJB spec reserves all methods starting with ejb.",
                                      javaMethod.getDeclaringClass(),
                                      javaMethod.getName()));
      }

      addBusinessMethod(apiMethod);
    }
  }

  /**
   * Introspects the lifecycle methods
   */
  public void introspectLifecycle(Class<?> cl)
  {
    if (cl == null || cl.equals(Object.class))
      return;

    for (Method method : cl.getDeclaredMethods()) {
      if (method.isAnnotationPresent(PostConstruct.class)) {
      }
    }

    introspectLifecycle(cl.getSuperclass());
  }

  /**
   * Introspects the lifecycle methods
   */
  public void introspectTimer(AnnotatedType<T> apiClass)
  {
    Class<T> cl = apiClass.getJavaClass();

    if (cl == null || cl.equals(Object.class))
      return;

    if (TimedObject.class.isAssignableFrom(cl)) {
      _timeoutMethod = "ejbTimeout";
      return;
    }

    for (AnnotatedMethod<? super T> apiMethod : apiClass.getMethods()) {
      Method method = apiMethod.getJavaMember();

      if (method.isAnnotationPresent(Timeout.class)) {
        if ((method.getParameterTypes().length != 0)
            && (method.getParameterTypes().length != 1
                || ! Timer.class.equals(method.getParameterTypes()[0]))) {
          throw new ConfigException(L.l(
              "{0}: timeout method '{1}' does not have a (Timer) parameter", cl
                  .getName(), method.getName()));
        }

        _timeoutMethod = method.getName();

        addBusinessMethod(apiMethod);
      }
    }
  }

  protected void addBusinessMethod(AnnotatedMethod<? super T> apiMethod)
  {
    int index = _businessMethods.size();
      
    BusinessMethodGenerator<X,T> bizMethod = createMethod(apiMethod, index);
      
    if (bizMethod != null) {
      bizMethod.introspect(bizMethod.getApiMethod(),
                           bizMethod.getImplMethod());
      
      _businessMethods.add(bizMethod);
    }
  }

  protected BusinessMethodGenerator<X,T>
  createMethod(AnnotatedMethod<? super T> apiMethod,
               int index)
  {
    Method javaMethod = apiMethod.getJavaMember();
    
    if (javaMethod.getDeclaringClass().getName().startsWith("javax.ejb.")) {
      return null;
    }
    
    AnnotatedMethod<? super X> implMethod = getMethod(apiMethod);

    if (implMethod == null) {
      throw new ConfigException(L.l("'{0}' method '{1}' has no corresponding implementation in '{2}'",
                                    javaMethod.getDeclaringClass().getSimpleName(),
                                    javaMethod.getName(),
                                    getBeanClass().getJavaClass().getName()));
    }

    StatelessMethod<X,T> bizMethod
      = new StatelessMethod<X,T>(getBeanClass(),
                                 getBeanClassName(),
                                 this,
                                 apiMethod,
                                 implMethod,
                                 index);

    return bizMethod;
  }
  
  //
  // code generation
  //

  /**
   * Generates the view code.
   */
  @Override
  public void generate(JavaWriter out) throws IOException
  {
    // generateBean(out);

    generateProxy(out);
  }

  protected void generateExtends(JavaWriter out)
    throws IOException
  {
    out.println("extends StatelessObject");
  }

  /**
   * Generates prologue for the context.
   */
  public void generateContextPrologue(JavaWriter out) throws IOException
  {
    String localVar = "_local_" + getViewClass().getJavaClass().getSimpleName();

    out.println();
    out.println("private " + getViewClassName() + " " + localVar + ";");
  }

  /**
   * Generates context home's constructor
   */
  @Override
  public void generateContextHomeConstructor(JavaWriter out) throws IOException
  {
    String localVar = "_local_" + getViewClass().getJavaClass().getSimpleName();

    out.println(localVar + " = new " + getViewClassName() + "(this);");
  }

  /**
   * Generates code to create the provider
   */
  public void generateCreateProvider(JavaWriter out, String var)
      throws IOException
  {
    String localVar = "_local_" + getViewClass().getJavaClass().getSimpleName();

    out.println();
    out.println("if (" + var + " == " + getViewClass().getJavaClass().getName() + ".class)");
    out.println("  return " + localVar + ";");
  }

  /**
   * Generates code to create the provider
   */
  @Override
  public void generateDestroy(JavaWriter out) throws IOException
  {
    String localVar = "_local_" + getViewClass().getJavaClass().getSimpleName();

    out.println();
    out.println(localVar + ".destroy();");
  }

  /**
   * Generates code to create an instance
   */
  @Override
  public void generateNewInstance(JavaWriter out) throws IOException
  {
    String localVar = "_local_" + getViewClass().getJavaClass().getSimpleName();

    out.print(localVar + "._ejb_begin()");
  }

  /**
   * Generates code to free an instance
   */
  @Override
  public void generateFreeInstance(JavaWriter out, String bean)
      throws IOException
  {
    String localVar = "_local_" + getViewClass().getJavaClass().getSimpleName();

    out.println(localVar + "._ejb_free(" + bean + ");");
  }

  /**
   * Generates code for a bean (pooled) instance.
   */
  public void generateBean(JavaWriter out) throws IOException
  {
    // XXX: disabled during 4.0.7 work for CDI TCK issues 
    if (true)
      return;
    
    out.println();
    out.println("public static class " + getBeanClassName());
    out.println("  extends " + getBeanClass().getJavaClass().getName());
    out.println("{");
    out.pushDepth();

    out.println("private transient " + getViewClassName() + " _context;");

    HashMap<String,Object> map = new HashMap<String,Object>();
    generateBeanPrologue(out, map);

    generatePostConstruct(out);

    _postConstructInterceptor.generatePrologue(out, map);
    _preDestroyInterceptor.generatePrologue(out, map);

    out.println();
    out.println(getBeanClassName() + "(" + getViewClassName() + " context)");
    out.println("{");
    out.pushDepth();
    out.println("_context = context;");

    map = new HashMap<String,Object>();
    generateBeanConstructor(out, map);
    _postConstructInterceptor.generateConstructor(out, map);
    _preDestroyInterceptor.generateConstructor(out, map);

    _postConstructInterceptor.generateCall(out);

    out.popDepth();
    out.println("}");

    // generateBusinessMethods(out);

    out.popDepth();
    out.println("}");
  }

  /**
   * Generates the local/remote proxy.
   */
  public void generateProxy(JavaWriter out) throws IOException
  {
    out.println();
    out.println("public static class " + getViewClassName());
    generateExtends(out);
    out.print("  implements " + getViewClass().getJavaClass().getName());
    out.println(", StatelessProvider");
    out.println("{");
    out.pushDepth();

    // out.println();
    // out.println("com.caucho.ejb.xa.EjbTransactionManager _xaManager;");

    /*
    out.println();
    out.println("private static final com.caucho.ejb.gen.XAManager _xa");
    out.println("  = new com.caucho.ejb.gen.XAManager();");
    */

    out.println();
    out.println("private " + getBean().getClassName() + " _context;");
    out.println("private final StatelessPool<" + getBeanClassName() + "> _statelessPool;");

    out.println();
    out.println(getViewClassName() + "(" + getBean().getClassName()
        + " context)");
    out.println("{");
    out.pushDepth();
    generateSuper(out, "context.getStatelessManager(), "
                  + getViewClass().getJavaClass().getName() + ".class");
    out.println("_context = context;");

    out.println("_statelessPool = context.getStatelessPool(this);");
    out.popDepth();
    out.println("}");

    out.println("public " + getBeanClassName()
                + " __caucho_new()");
    out.println("{");
    // XXX: 4.0.7 temp for CDI TCK package-private issues
    // out.println("  return new " + getBeanClassName() + "(this);");
    // out.println("  throw new IllegalStateException();");
    out.println("  return new " + getBeanClass().getJavaClass().getName() + "();");
    out.println("}");

    out.println("public void __caucho_preDestroy(Object instance)");
    out.println("{");
    out.println("}");

    out.println("public void __caucho_postConstruct(Object instance)");
    out.println("{");
    out.println("}");

    out.println();
    out.println("public " + getViewClass().getJavaClass().getName()
                + " __caucho_get()");
    out.println("{");
    out.println("  return this;");
    out.println("}");

    generateProxyPool(out);

    generateBusinessMethods(out);

    /*
     * for (BusinessMethodGenerator bizMethod : getMethods()) { out.println();
     *
     * bizMethod.generateHeader(out); out.println("{"); out.pushDepth();
     *
     * out.println("Thread thread = Thread.currentThread();");
     * out.println("ClassLoader oldLoader = thread.getContextClassLoader();");
     * out.println(); out.println("try {"); out.pushDepth();out.println(
     * "thread.setContextClassLoader(getStatelessManager().getClassLoader());");
     * out.println();
     *
     * generateProxyCall(out, bizMethod.getImplMethod());
     *
     * out.popDepth(); out.println("} finally {");
     * out.println("  thread.setContextClassLoader(oldLoader);");
     * out.println("}");
     *
     * out.popDepth(); out.println("}"); }
     */

    generateBean(out);

    out.popDepth();
    out.println("}");
  }

  public void generateProxyPool(JavaWriter out) throws IOException
  {
    String beanClass = getBeanClassName();

    out.println();
    out.println("final " + beanClass + " _ejb_begin()");
    out.println("{");
    out.pushDepth();

    out.println("return _statelessPool.allocate();");

    /*
    out.println(beanClass + " bean;");
    out.println("synchronized (this) {");
    out.println("  if (_freeBeanTop > 0) {");
    out.println("    bean = _freeBeanStack[--_freeBeanTop];");
    out.println("    return bean;");
    out.println("  }");
    out.println("}");
    out.println();
    out.println("try {");
    out.println("  bean = new " + beanClass + "(this);");

    Class implClass = getBean().getBeanClass().getJavaClass();

    if (SessionBean.class.isAssignableFrom(implClass)) {
      out.println("  bean.setSessionContext(_context);");
    }

    out.println("  getStatelessManager().initInstance(bean);");

    if (getBean().hasMethod("ejbCreate", new Class[0])) {
      // ejb/0fe0: ejbCreate can be private, out.println("  bean.ejbCreate();");
      out.println("  bean.ejbCreate();");
    }

    out.println("  return bean;");
    out.println("} catch (Exception e) {");
    out.println("  throw com.caucho.ejb.EJBExceptionWrapper.create(e);");
    out.println("}");
    */
    out.popDepth();
    out.println("}");

    String baseClass = getBeanClass().getJavaClass().getName();

    out.println();
    out.println("final void _ejb_free(" + baseClass + " bean)");
    out.println("  throws javax.ejb.EJBException");
    out.println("{");
    out.pushDepth();

    out.println("_statelessPool.free((" + beanClass + ") bean);");
    /*
    out.println("if (bean == null)");
    out.println("  return;");
    out.println();
    out.println("synchronized (this) {");
    out.println("  if (_freeBeanTop < _freeBeanStack.length) {");
    out.println("    _freeBeanStack[_freeBeanTop++] = bean;");
    out.println("    return;");
    out.println("  }");
    out.println("}");

    out.println("_server.destroyInstance(bean);");
    */

    out.popDepth();
    out.println("}");

    out.println();
    out.println("final void _ejb_destroy(" + beanClass + " bean)");
    out.println("  throws javax.ejb.EJBException");
    out.println("{");
    out.pushDepth();

    out.println("_statelessPool.destroy(bean);");

    out.popDepth();
    out.println("}");

    out.println();
    out.println("final void _ejb_discard(" + beanClass + " bean)");
    out.println("  throws javax.ejb.EJBException");
    out.println("{");
    out.pushDepth();

    out.println("_statelessPool.discard(bean);");

    out.popDepth();
    out.println("}");

    out.println();
    out.println("public void destroy()");
    out.println("{");
    out.pushDepth();

    out.println("_statelessPool.destroy();");

    out.popDepth();
    out.println("}");
  }

  public void generateProxyCall(JavaWriter out, Method implMethod)
      throws IOException
  {
    if (! void.class.equals(implMethod.getReturnType())) {
      out.printClass(implMethod.getReturnType());
      out.println(" result;");
    }

    out.println(getBeanClassName() + " bean = _statelessPool.allocate();");

    if (! void.class.equals(implMethod.getReturnType()))
      out.print("result = ");

    out.print("bean." + implMethod.getName() + "(");

    Class<?>[] types = implMethod.getParameterTypes();
    for (int i = 0; i < types.length; i++) {
      if (i != 0)
        out.print(", ");

      out.print(" a" + i);
    }

    out.println(");");

    out.println("_ejb_free(bean);");

    if (!void.class.equals(implMethod.getReturnType()))
      out.println("return result;");
  }

  protected void generateSuper(JavaWriter out, String serverVar)
      throws IOException
  {
    out.println("super(" + serverVar + ");");
  }

  @Override
  public void generateTimer(JavaWriter out) throws IOException
  {
    if (_timeoutMethod != null) {
      String localVar = "_local_" + getViewClass().getJavaClass().getSimpleName();

      out.println(getBeanClass().getJavaClass().getName() + " bean = "
                  + localVar
                  + "._ejb_begin();");
      out.println("bean." + _timeoutMethod + "(timer);");
      // XXX: needs try-finally
      out.println(localVar + "._ejb_free(bean);");
    }
  }
}
