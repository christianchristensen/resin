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
 *   Free SoftwareFoundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Emil Ong
 */

package com.caucho.ejb.embeddable;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.InitialContext;

import com.caucho.config.inject.InjectManager;
import com.caucho.ejb.manager.EjbEnvironmentListener;
import com.caucho.env.jpa.ListenerPersistenceEnvironment;
import com.caucho.inject.Module;
import com.caucho.loader.Environment;
import com.caucho.loader.EnvironmentClassLoader;
import com.caucho.naming.AbstractModel;
import com.caucho.naming.ContextImpl;
import com.caucho.naming.InitialContextFactoryImpl;
import com.caucho.resin.ResinBeanContainer;
import com.caucho.server.e_app.EnterpriseApplication;
import com.caucho.server.webbeans.ResinWebBeansProducer;

/**
 * Interface for the EJBClient.
 */
@Module
public class EJBContainerImpl extends EJBContainer {
  private Context _context;
  private EnvironmentClassLoader _classLoader;
  private InjectManager _injectManager;
  private EnterpriseApplication _application;

  public EJBContainerImpl()
    throws EJBException
  {
    this(null);
  }

  public EJBContainerImpl(String name)
    throws EJBException
  {
    preInit(name);
  }

  void addModule(String modulePath)
  {
  }

  void preInit(String name)
  {
    if (_application != null)
      return;
    
    _application = EnterpriseApplication.create(name);
    
    _classLoader = _application.getClassLoader();
    _injectManager = InjectManager.create(_classLoader);
    _injectManager.addManagedBean(_injectManager.createManagedBean(ResinWebBeansProducer.class));

    Thread thread = Thread.currentThread();
    ClassLoader oldLoader = thread.getContextClassLoader();

    try {
      thread.setContextClassLoader(_classLoader);

      Environment.init();

      Environment.addChildLoaderListener(new ListenerPersistenceEnvironment());
      Environment.addChildLoaderListener(new EjbEnvironmentListener());

      // XXX initialcontextfactory broken when set by non-resin container
      AbstractModel model = InitialContextFactoryImpl.createRoot();
      _context = new ContextImpl(model, null);
    } finally {
      thread.setContextClassLoader(oldLoader);
    }
  }

  void start()
  {
    Thread thread = Thread.currentThread();
    ClassLoader oldLoader = thread.getContextClassLoader();

    try {
      thread.setContextClassLoader(_classLoader);
      
      _classLoader.scanRoot();
      _application.start();
    } finally {
      thread.setContextClassLoader(oldLoader);
    }
  }
  
  @Override
  public Context getContext()
  {
    return _context;
  }

  @Override
  public void close()
  {
    _classLoader.destroy();
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _application.getName() + "]";
  }
}
