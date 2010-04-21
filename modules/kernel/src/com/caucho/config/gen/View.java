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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import javax.decorator.Decorator;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.interceptor.Interceptor;
import javax.interceptor.InterceptorBinding;

import com.caucho.inject.Module;
import com.caucho.java.JavaWriter;

/**
 * Represents a public interface to a bean, e.g. a local stateful view
 */
@Module
abstract public class View<X,T> {
  protected final BeanGenerator<X> _bean;
  protected final AnnotatedType<T> _viewClass;

  protected ArrayList<Annotation> _interceptorBindings;

  protected View(BeanGenerator<X> bean, AnnotatedType<T> viewClass)
  {
    _bean = bean;
    _viewClass = viewClass;

    _bean.addDependency(viewClass.getJavaClass());
  }

  /**
   * Returns the owning bean.
   */
  protected BeanGenerator<X> getBean()
  {
    return _bean;
  }

  /**
   * Returns the bean's ejbclass
   */
  protected AnnotatedType<X> getBeanClass()
  {
    return _bean.getBeanClass();
  }

  public String getBeanClassName()
  {
    return getViewClassName();
  }

  /**
   * Returns the API class.
   */
  public AnnotatedType<T> getViewClass()
  {
    return _viewClass;
  }

  public String getViewClassName()
  {
    throw new UnsupportedOperationException(getClass().getName());
  }

  public boolean isRemote()
  {
    return false;
  }
  
  public boolean isProxy()
  {
    return false;
  }

  /**
   * Returns any interceptor bindings
   */
  public ArrayList<Annotation> getInterceptorBindings()
  {
    return _interceptorBindings;
  }

  /**
   * Introspects the view
   */
  public void introspect()
  {
    introspectClass(getViewClass());
  }

  protected void introspectClass(AnnotatedType<T> cl)
  {
    if (cl.isAnnotationPresent(Interceptor.class)
        || cl.isAnnotationPresent(Decorator.class)) {
      return;
    }

    ArrayList<Annotation> interceptorBindingList
      = new ArrayList<Annotation>();

    ArrayList<Annotation> xmlInterceptorBindings = getInterceptorBindings();

    if (xmlInterceptorBindings != null) {
      for (Annotation ann : xmlInterceptorBindings) {
        interceptorBindingList.add(ann);
      }
    }
    else {
      for (Annotation ann : cl.getAnnotations()) {
        Class<?> annType = ann.annotationType();

        if (annType.isAnnotationPresent(Stereotype.class)) {
          for (Annotation sAnn : ann.annotationType().getAnnotations()) {
            Class<?> sAnnType = sAnn.annotationType();

            if (sAnnType.isAnnotationPresent(InterceptorBinding.class)) {
              interceptorBindingList.add(sAnn);
            }
          }
        }

        if (annType.isAnnotationPresent(InterceptorBinding.class)) {
          interceptorBindingList.add(ann);
        }
      }
    }

    _interceptorBindings = interceptorBindingList;
  }

  /**
   * Returns the introspected methods
   */
  public ArrayList<? extends BusinessMethodGenerator<X,T>> getMethods()
  {
    throw new UnsupportedOperationException(getClass().getName());
  }

  /**
   * Returns any around-invoke method
   */
  public AnnotatedMethod<? super X> getAroundInvokeMethod()
  {
    return getBean().getAroundInvokeMethod();
  }

  /**
   * Generates prologue for the context.
   */
  public void generateContextPrologue(JavaWriter out)
    throws IOException
  {

  }

  /**
   * Generates context home's constructor
   */
  public void generateContextHomeConstructor(JavaWriter out)
    throws IOException
  {
  }

  /**
   * Generates context object's constructor
   */
  public void generateContextObjectConstructor(JavaWriter out)
    throws IOException
  {
  }

  /**
   * Generates timer code
   */
  public void generateTimer(JavaWriter out)
    throws IOException
  {
  }

  /**
   * Generates a new bean instance.
   */
  public void generateNewInstance(JavaWriter out)
    throws IOException
  {
    throw new UnsupportedOperationException(getClass().getName());
  }

  /**
   * Frees a bean instance.
   */
  public void generateFreeInstance(JavaWriter out, String name)
    throws IOException
  {
    throw new UnsupportedOperationException(getClass().getName());
  }

  /**
   * Generates any global destroy
   */
  public void generateDestroy(JavaWriter out)
    throws IOException
  {
  }

  /**
   * Generates the view code.
   */
  abstract public void generate(JavaWriter out)
    throws IOException;

  /**
   * Generates constructor additions
   */
  public void generateProxyConstructor(JavaWriter out)
    throws IOException
  {
    HashMap<String,Object> map = new HashMap<String,Object>();
    generateProxyConstructor(out, map);
  }

  /**
   * Generates constructor additions
   */
  public void generateProxyConstructor(JavaWriter out, 
                                       HashMap<String,Object> map)
    throws IOException
  {
    for (BusinessMethodGenerator<X,T> method : getMethods()) {
      method.generateProxyConstructor(out, map);
    }
  }

  /**
   * Generates constructor additions
   */
  public void generateBeanConstructor(JavaWriter out)
    throws IOException
  {
    HashMap<String,Object> map = new HashMap<String,Object>();
    generateBeanConstructor(out, map);
  }

  /**
   * Generates constructor additions
   */
  public void generateBeanConstructor(JavaWriter out, 
                                      HashMap<String,Object> map)
    throws IOException
  {
    for (BusinessMethodGenerator<X,T> method : getMethods()) {
      method.generateBeanConstructor(out, map);
    }
  }

  /**
   * Generates prologue additions
   */
  public void generateBeanPrologue(JavaWriter out)
    throws IOException
  {
    generateBeanPrologue(out, new HashMap<String,Object>());
  }

  /**
   * Generates prologue additions
   */
  public void generateBeanPrologue(JavaWriter out, 
                                   HashMap<String,Object> map)
    throws IOException
  {
    for (BusinessMethodGenerator<X,T> method : getMethods()) {
      method.generateBeanPrologue(out, map);
    }
  }

  protected void generatePostConstruct(JavaWriter out)
     throws IOException
   {
     out.println();
     out.println("private void __caucho_postConstruct()");
     out.println("{");
     out.pushDepth();

     HashMap<String,Object> map = new HashMap<String,Object>();
     for (BusinessMethodGenerator<X,T> method : getMethods()) {
       method.generatePostConstruct(out, map);
     }

     out.popDepth();
     out.println("}");
   }


  /**
   * Generates view's business methods
   */
  public void generateBusinessMethods(JavaWriter out)
    throws IOException
  {
    HashMap<String,Object> map = new HashMap<String,Object>();
    for (BusinessMethodGenerator<X,T> method : getMethods()) {
      method.generate(out, map);
    }
  }
  
  protected AnnotatedMethod<? super X> 
  getMethod(AnnotatedMethod<? super T> method)
  {
    Method javaMethod = method.getJavaMember();
    
    return getMethod(getBeanClass(), 
                     javaMethod.getName(), 
                     javaMethod.getParameterTypes());
  }
  
  protected <Z> AnnotatedMethod<? super Z> getMethod(AnnotatedType<Z> type,
                                                     Method javaMethod)
  {
    return getMethod(type, javaMethod.getName(), javaMethod.getParameterTypes());
  }
  
  protected <Z> AnnotatedMethod<? super Z> getMethod(AnnotatedType<Z> type,
                                                     String methodName,
                                                     Class<?> []params)
  {
    for (AnnotatedMethod<? super Z> annMethod : type.getMethods()) {
      Method method = annMethod.getJavaMember();
      
      if (! method.getName().equals(methodName))
        continue;
      
      if (isMatch(method.getParameterTypes(), params))
        return annMethod;
    }
    
    return null;
  }
  
  private boolean isMatch(Class<?> []typesA, Class<?> []typesB)
  {
    if (typesA.length != typesB.length)
      return false;
    
    for (int i = typesA.length - 1; i >= 0; i--) {
      if (! typesA[i].equals(typesB[i]))
        return false;
    }
    
    return true;
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + getBeanClass() + "]";
  }
}
