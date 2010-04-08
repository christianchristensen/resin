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

package com.caucho.config.el;

import java.beans.*;
import java.util.*;
import javax.el.*;

import com.caucho.config.inject.CreationalContextImpl;
import com.caucho.config.inject.InjectManager;
import com.caucho.config.xml.XmlConfigContext;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

/**
 * Variable resolution for webbeans variables
 */
public class WebBeansContextResolver extends ELResolver {
  public WebBeansContextResolver()
  {
  }

  public Class<?> getCommonPropertyType(ELContext context,
                                        Object base)
  {
    return Object.class;
  }

  public Iterator<FeatureDescriptor>
    getFeatureDescriptors(ELContext context, Object base)
  {
    ArrayList<FeatureDescriptor> list = new ArrayList<FeatureDescriptor>();

    return list.iterator();
  }

  public Class<?> getType(ELContext context,
                          Object base,
                          Object property)
  {
    Object value = getValue(context, base, property);

    if (value == null)
      return null;
    else
      return value.getClass();
  }

  public Object getValue(ELContext context,
                         Object base,
                         Object property)
    throws PropertyNotFoundException,
           ELException
  {
    if (! (property instanceof String) || base != null)
      return null;

    String name = (String) property;

    InjectManager webBeans = InjectManager.getCurrent();

    if (webBeans == null)
      return null;

    Set<Bean<?>> beans = webBeans.getBeans(name);

    if (beans.size() == 0)
      return null;

    Bean<?> bean = webBeans.resolve(beans);
    
    XmlConfigContext env = XmlConfigContext.getCurrent();
    
    CreationalContext<?> cxt = null;
    
    if (env != null) {
      cxt = env.getCreationalContext();
      
      if (cxt instanceof CreationalContextImpl<?>) {
        CreationalContextImpl<?> cxtImpl = (CreationalContextImpl<?>) cxt;
        
        Object value = cxtImpl.get(bean);
        
        if (value != null)
          return value;
      }
      
      cxt = new CreationalContextImpl(bean, cxt);
    }
    else {
      cxt = new CreationalContextImpl(bean, null);
    }
    
    Object result = webBeans.getReference(bean, bean.getBeanClass(), cxt);

    if (result != null) {
      context.setPropertyResolved(true);

      return result;
    }
    else
      return null;
  }

  public boolean isReadOnly(ELContext context,
                            Object base,
                            Object property)
    throws PropertyNotFoundException,
           ELException
  {
    return true;
  }

  public void setValue(ELContext context,
                       Object base,
                       Object property,
                       Object value)
    throws PropertyNotFoundException,
           PropertyNotWritableException,
           ELException
  {
  }
}
