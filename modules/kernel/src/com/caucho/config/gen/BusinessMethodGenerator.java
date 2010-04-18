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
package com.caucho.config.gen;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;

import javax.ejb.ApplicationException;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

import com.caucho.java.JavaWriter;

/**
 * Represents a business method
 */
public class BusinessMethodGenerator<X,T> implements EjbCallChain<X,T> {
  private final View<X,T> _view;

  private final AnnotatedMethod<? super T> _apiMethod;
  private final AnnotatedMethod<? super X> _implMethod;

  private SecurityCallChain<X,T> _security;
  // private SchedulingCallChain _scheduling;
  private LockCallChain<X,T> _lock;
  private XaCallChain<X,T> _xa;
  private InterceptorCallChain<X,T> _interceptor;
  private AsynchronousCallChain<X,T> _async;

  private EjbCallChain<X,T> _next;

  public BusinessMethodGenerator(View<X,T> view, 
                                 AnnotatedMethod<? super T> apiMethod,
                                 AnnotatedMethod<? super X> implMethod, 
                                 int index)
  {
    _view = view;
    
    _apiMethod = apiMethod;
    _implMethod = implMethod;

    _next = createTailCallChain();

    _next = _interceptor = new InterceptorCallChain<X,T>(_next, this, view);
    _next = _xa = createXa(_next);
    _next = _lock = new LockCallChain<X,T>(this, _next);
    // _scheduling = new SchedulingCallChain(this, _lock);
    _next = _async = new AsynchronousCallChain<X,T>(this, _next);
    _next = _security = new SecurityCallChain<X,T>(this, _next);
  }

  protected EjbCallChain<X,T> createTailCallChain()
  {
    return new MethodTailCallChain<X,T>(this);
  }

  protected XaCallChain<X,T> createXa(EjbCallChain<X,T> next)
  {
    return new XaCallChain<X,T>(this, next);
  }

  /**
   * Returns the owning view.
   */
  public View<X,T> getView()
  {
    return _view;
  }

  /**
   * Returns the bean's ejbclass
   */
  protected AnnotatedType<X> getBeanClass()
  {
    return _view.getBeanClass();
  }
  
  protected AnnotatedType<T> getApiType()
  {
    return _view.getViewClass();
  }

  /**
   * Returns the implementation method
   */
  public AnnotatedMethod<? super X> getImplMethod()
  {
    return _implMethod;
  }

  /**
   * Returns the api method
   */
  public AnnotatedMethod<? super T> getApiMethod()
  {
    return _apiMethod;
  }

  /**
   * Returns true if the business method has any active XA annotation.
   */
  public boolean hasXA()
  {
    return _xa.isEnhanced();
  }
  
  public boolean isAsync()
  {
    return _async.isAsync();
  }

  /**
   * Set true for a remove method
   */
  public void setRemove(boolean isRemove)
  {
  }

  /**
   * Set true for a remove method
   */
  public void setRemoveRetainIfException(boolean isRemoveRetainIfException)
  {
  }

  public boolean isXaContainerManaged()
  {
    return true;
  }

  /**
   * Returns the xa call chain
   */
  public XaCallChain<X,T> getXa()
  {
    return _xa;
  }

  /**
   * Returns the security call chain
   */
  public SecurityCallChain<X,T> getSecurity()
  {
    return _security;
  }

  /**
   * Returns the interceptor call chain
   */
  public InterceptorCallChain<X,T> getInterceptor()
  {
    return _interceptor;
  }

  /**
   * Returns true if any interceptors enhance the business method
   */
  public boolean isEnhanced()
  {
    if (_security.isEnhanced())
      return true;
    else if (_xa.isEnhanced())
      return true;
    else if (_interceptor.isEnhanced())
      return true;
    else if (_lock.isEnhanced())
      return true;
    else if (_async.isEnhanced())
      return true;
    /*
    else if (_scheduling.isEnhanced()) {
      return true;
    }
    */

    return false;
  }

  //
  // introspection
  //

  @Override
  public void introspect(AnnotatedMethod<? super T> apiMethod, 
                         AnnotatedMethod<? super X> implMethod)
  {
    _security.introspect(apiMethod, implMethod);
    // _scheduling.introspect(apiMethod, implMethod);
    _lock.introspect(apiMethod, implMethod);
    _xa.introspect(apiMethod, implMethod);
    _interceptor.introspect(apiMethod, implMethod);
    _async.introspect(apiMethod, implMethod);
  }

  //
  // bean instance interception
  //

  /**
   * Generates interception for the bean instance constructo
   */
  @Override
  public void generateBeanPrologue(JavaWriter out,
                                   HashMap<String,Object> map)
    throws IOException
  {
    _next.generateBeanPrologue(out, map);

    // generateTail(out);
  }

  /**
   * Generates interception for the bean instance constructo
   */
  @Override
  public void generateBeanConstructor(JavaWriter out,
                                      HashMap<String,Object> map)
    throws IOException
  {
    _next.generateBeanConstructor(out, map);
  }

  /**
   * Generates interception for the bean @PostConstruct
   */
  @Override
  public void generatePostConstruct(JavaWriter out,
                                    HashMap<String,Object> map)
    throws IOException
  {
    _next.generatePostConstruct(out, map);
  }

  //
  // business method interception
  //

  //
  // generation for the actual method
  //

  /**
   * Generates the overridden method.
   */
  public final void generate(JavaWriter out,
                             HashMap<String,Object> prologueMap)
    throws IOException
  {
    if (! isEnhanced())
      return;

    generateMethodPrologue(out, prologueMap);
    
    String suffix = "";
    
    if (isAsync()) {
      suffix = "__caucho_async";
      
      generateHeader(out, "");

      out.println("{");
      out.pushDepth();

      generateAsync(out);

      out.popDepth();
      out.println("}");
    }

    generateHeader(out, suffix);

    out.println("{");
    out.pushDepth();

    generateContent(out);

    out.popDepth();
    out.println("}");
  }

  /**
   * Generates class code before the class itself
   *
   * <code><pre>
   * [method-prologue]
   * MyType myCall(...)
   * {
   *   ...
   * }
   * </pre></code>
   */
  @Override
  public void generateMethodPrologue(JavaWriter out,
                                     HashMap<String,Object> map)
    throws IOException
  {
    _next.generateMethodPrologue(out, map);
  }

  /**
   * Generates the method's signature before the call:
   *
   * <code><pre>
   * MyValue myCall(int a0, String, a1, ...)
   *   throws MyException, ...
   * </pre><?code>
  */
  public void generateHeader(JavaWriter out,
                             String suffix)
    throws IOException
  {
    out.println();
    
    Method apiJavaMethod = _apiMethod.getJavaMember();
    int apiModifiers = apiJavaMethod.getModifiers();
    
    if (Modifier.isPublic(apiModifiers))
      out.print("public ");
    else if (Modifier.isProtected(apiModifiers))
      out.print("protected ");
    else
      throw new IllegalStateException(_apiMethod.toString()
          + " must be public or protected");

    out.printClass(apiJavaMethod.getReturnType());
    out.print(" ");
    out.print(apiJavaMethod.getName() + suffix);
    out.print("(");

    Class<?>[] types = apiJavaMethod.getParameterTypes();
    for (int i = 0; i < types.length; i++) {
      Class<?> type = types[i];

      if (i != 0)
        out.print(", ");

      if (i == types.length - 1 && type.isArray() && isVarArgs(_apiMethod)) {
        out.printClass(type.getComponentType());
        out.print("...");
      } else
        out.printClass(type);

      out.print(" a" + i);
    }

    out.println(")");
    if (_implMethod != null)
      generateThrows(out, _implMethod.getJavaMember().getExceptionTypes());
    else
      generateThrows(out, apiJavaMethod.getExceptionTypes());
  }
  
  private boolean isVarArgs(AnnotatedMethod<?> annMethod)
  {
    return annMethod.getJavaMember().isVarArgs();
  }

  /**
   * Generates the method's "throws" declaration in the
   * method signature.
   *
   * @param out generated Java output
   * @param exnCls the exception classes
   */
  protected void generateThrows(JavaWriter out,
                                Class<?>[] exnCls)
    throws IOException
  {
    if (exnCls.length == 0)
      return;

    out.print(" throws ");

    for (int i = 0; i < exnCls.length; i++) {
      if (i != 0)
        out.print(", ");

      out.printClass(exnCls[i]);
    }
    out.println();
  }

  /**
   * Generates the dispatch body for an async call
   */
  @Override
  public void generateAsync(JavaWriter out)
    throws IOException
  {
    _next.generateAsync(out);
  }
  
  /**
   * Generates the body of the method.
   *
   * <code><pre>
   * MyType myMethod(...)
   * {
   *   [pre-try]
   *   try {
   *     [pre-call]
   *     [call]   // retValue = super.myMethod(...)
   *     [post-call]
   *     return retValue;
   *   } catch (RuntimeException e) {
   *     [system-exception]
   *     throw e;
   *   } catch (Exception e) {
   *     [application-exception]
   *     throw e;
   *   } finally {
   *     [finally]
   *   }
   * </pre></code>
   */
  protected void generateContent(JavaWriter out)
    throws IOException
  {
    generatePreTry(out);

    out.println();
    out.println("try {");
    out.pushDepth();

    Method javaApiMethod = _apiMethod.getJavaMember();
    
    if (! void.class.equals(javaApiMethod.getReturnType())) {
      out.printClass(javaApiMethod.getReturnType());
      out.println(" result;");
    }

    generatePreCall(out);

    generateCall(out);

    generatePostCall(out);

    if (! void.class.equals(javaApiMethod.getReturnType()))
      out.println("return result;");

    out.popDepth();

    generateExceptions(out);

    out.println("} finally {");
    out.pushDepth();

    generateFinally(out);

    out.popDepth();
    out.println("}");
  }

  /**
   * Generates method code before the "try" block.
   */
  @Override
  public void generatePreTry(JavaWriter out)
    throws IOException
  {
    _next.generatePreTry(out);
  }

  /**
   * Generates the underlying bean instance
   */
  @Override
  public void generatePreCall(JavaWriter out)
    throws IOException
  {
    _next.generatePreCall(out);
  }

  @Override
  public void generateCall(JavaWriter out)
    throws IOException
  {
    _next.generateCall(out);
  }

  /**
   * Generates the underlying bean instance
   */
  @Override
  public void generatePostCall(JavaWriter out)
    throws IOException
  {
    _next.generatePostCall(out);
  }

  private void generateExceptions(JavaWriter out)
    throws IOException
  {
    HashSet<Class<?>> exceptionSet
      = new HashSet<Class<?>>();

    for (Class<?> exn : getExceptionTypes()) {
      exceptionSet.add(exn);
    }

    exceptionSet.add(RuntimeException.class);

    Class<?> exn;
    while ((exn = selectException(exceptionSet)) != null) {
      boolean isSystemException
        = (RuntimeException.class.isAssignableFrom(exn)
            && ! exn.isAnnotationPresent(ApplicationException.class));

      out.println("} catch (" + exn.getName() + " e) {");
      out.pushDepth();

      if (isSystemException)
        generateSystemException(out, exn);
      else
        generateApplicationException(out, exn);

      out.println();
      out.println("throw e;");

      out.popDepth();
    }
  }

  /**
   * Generates code for a system exception
   */
  @Override
  public void generateApplicationException(JavaWriter out,
                                           Class<?> exn)
    throws IOException
  {
    _next.generateApplicationException(out, exn);
  }

  /**
   * Generates code for a system exception
   */
  @Override
  public void generateSystemException(JavaWriter out,
                                      Class<?> exn)
    throws IOException
  {
    _next.generateSystemException(out, exn);
  }

  /**
   * Generates method code in the finally block.
   */
  @Override
  public void generateFinally(JavaWriter out)
    throws IOException
  {
    _next.generateFinally(out);
  }

  //
  // specific overrides to handle different bean types
  //

  public void generateTailCall(JavaWriter out)
    throws IOException
  {
    generateTailCall(out, getSuper());
  }

  /**
   * Generates the call to the implementation bean.
   *
   * @param superVar java code to reference the implementation
   */
  public void generateTailCall(JavaWriter out, String superVar)
    throws IOException
  {
    out.println();

    Method javaImplMethod = _implMethod.getJavaMember();
    
    if (! void.class.equals(javaImplMethod.getReturnType())) {
      out.print("result = ");
    }

    out.print(superVar + "." + javaImplMethod.getName() + "(");

    Class<?>[] types = javaImplMethod.getParameterTypes();
    for (int i = 0; i < types.length; i++) {
      if (i != 0)
        out.print(", ");

      out.print(" a" + i);
    }

    out.println(");");

    /*
    // ejb/12b0
    if (! "super".equals(superVar))
      generatePostCall(out);
    */
  }

  /**
   * Generates the underlying bean instance
   */
  protected String getSuper() throws IOException
  {
    return "super";
  }

  /**
   * Generates the underlying bean instance
   */
  protected void generateThis(JavaWriter out) throws IOException
  {
    out.print("this");
  }

  /**
   * Generates the underlying bean instance
   */
  protected void generateBean(JavaWriter out) throws IOException
  {
    generateThis(out);
  }

  //
  // interceptor generators
  //

  // XXX: move to InterceptorCallChain
  public void generateInterceptorTarget(JavaWriter out) throws IOException
  {
    generateTail(out);
  }

  protected void generateTail(JavaWriter out)
    throws IOException
  {
    if (_interceptor.isEnhanced()) {
      Method javaImplMethod = _implMethod.getJavaMember();
      Method javaApiMethod = _apiMethod.getJavaMember();
      
      out.println();
      out.print("private ");
      out.printClass(javaImplMethod.getReturnType());
      out.print(" __caucho_");
      out.print(javaApiMethod.getName());
      out.print("(");

      Class<?>[] types = javaImplMethod.getParameterTypes();
      for (int i = 0; i < types.length; i++) {
        Class<?> type = types[i];

        if (i != 0)
          out.print(", ");

        out.printClass(type);
        out.print(" a" + i);
      }

      out.println(")");
      generateThrows(out, javaImplMethod.getExceptionTypes());
      out.println();
      out.println("{");
      out.pushDepth();

      if (! void.class.equals(javaApiMethod.getReturnType())) {
        out.printClass(javaApiMethod.getReturnType());
        out.println(" result;");
      }

      generateTailCall(out, "super");

      if (! void.class.equals(javaImplMethod.getReturnType()))
        out.println("return result;");

      out.popDepth();
      out.println("}");
    }
  }

  //
  // utilities
  //

  protected boolean hasException(Class<?> exn)
  {
    Method javaImplMethod = _implMethod.getJavaMember();
    
    for (Class<?> apiExn : javaImplMethod.getExceptionTypes()) {
      if (apiExn.isAssignableFrom(exn))
        return true;
    }

    return false;
  }

  boolean matches(String name, Class<?>[] parameterTypes)
  {
    Method javaApiMethod = _apiMethod.getJavaMember();
    
    if (! javaApiMethod.getName().equals(name))
      return false;

    Class<?>[] methodTypes = javaApiMethod.getParameterTypes();
    if (methodTypes.length != parameterTypes.length)
      return false;

    for (int i = 0; i < parameterTypes.length; i++) {
      if (!methodTypes[i].equals(parameterTypes[i]))
        return false;
    }

    return true;
  }

  private Class<?> selectException(HashSet<Class<?>> exnSet)
  {
    for (Class<?> exn : exnSet) {
      if (isMostSpecific(exn, exnSet)) {
        exnSet.remove(exn);

        return exn;
      }
    }

    return null;
  }

  private boolean isMostSpecific(Class<?> exn, HashSet<Class<?>> exnSet)
  {
    for (Class<?> testExn : exnSet) {
      if (exn == testExn)
        continue;

      if (exn.isAssignableFrom(testExn))
        return false;
    }

    return true;
  }

  protected Class<?>[] getExceptionTypes()
  {
    if (_implMethod != null)
      return _implMethod.getJavaMember().getExceptionTypes();
    else
      return _apiMethod.getJavaMember().getExceptionTypes();
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
      return true;
    else if (!(o instanceof BusinessMethodGenerator<?,?>))
      return false;

    BusinessMethodGenerator<?,?> bizMethod = (BusinessMethodGenerator<?,?>) o;

    return _apiMethod.getJavaMember().getName().equals(bizMethod._apiMethod.getJavaMember().getName());
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _apiMethod + "]";
  }
}
