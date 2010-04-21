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

package com.caucho.ejb.inject;

import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;

import com.caucho.config.inject.BeanWrapper;
import com.caucho.config.inject.ManagedBeanImpl;
import com.caucho.config.inject.ScopeAdapterBean;
import com.caucho.inject.Module;

/**
 * Internal implementation for a Bean
 */
@Module
abstract public class SessionBeanImpl<X> extends BeanWrapper<X>
  implements ScopeAdapterBean<X>, PassivationCapable, EjbGeneratedBean
{
  public SessionBeanImpl(ManagedBeanImpl<X> bean)
  {
    super(bean.getBeanManager(), bean);
  }
  
  @Override
  abstract public Set<Type> getTypes();

  @Override
  public X getScopeAdapter(Bean<?> topBean, CreationalContext<X> context)
  {
    return null;
  }

  @Override
  public X create(CreationalContext<X> context)
  {
    throw new UnsupportedOperationException(getClass().getName());
  }
  
  @Override
  public void destroy(X instance, CreationalContext<X> context)
  {
  }

  /**
   * Returns the injection points.
   */
  public Set<InjectionPoint> getInjectionPoints()
  {
    return getBean().getInjectionPoints();
  }
}
