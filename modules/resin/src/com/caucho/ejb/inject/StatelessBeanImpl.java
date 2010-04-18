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
import java.util.LinkedHashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;

import com.caucho.config.inject.ManagedBeanImpl;
import com.caucho.ejb.session.StatelessManager;
import com.caucho.ejb.session.StatelessProvider;
import com.caucho.inject.Module;

/**
 * Internal implementation for a Bean
 */
@Module
public class StatelessBeanImpl<X> extends SessionBeanImpl<X>
{
  private StatelessProvider<X> _producer;
  private LinkedHashSet<Type> _types = new LinkedHashSet<Type>();

  public StatelessBeanImpl(StatelessManager<X> server,
			   ManagedBeanImpl<X> bean,
			   Class<?> api,
                           StatelessProvider<X> producer)
  {
    super(bean);
    
    _producer = producer;

    if (producer == null)
      throw new NullPointerException();

    _types.add(api);
  }

  @Override
  public X create(CreationalContext<X> context)
  {
    return (X) _producer.__caucho_get();
  }
  
  @Override
  public Set<Type> getTypes()
  {
    return _types;
  }
}
