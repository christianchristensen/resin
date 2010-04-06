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
 * @author Scott Ferguson;
 */

package com.caucho.config.j2ee;

import com.caucho.config.inject.ConfigContext;
import com.caucho.config.program.ConfigProgram;
import com.caucho.config.ConfigException;
import com.caucho.loader.*;
import com.caucho.util.L10N;

import java.lang.reflect.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DestroyListener implements EnvironmentListener
{
  private static final Logger log
    = Logger.getLogger(PreDestroyProgram.class.getName());
  private static final L10N L = new L10N(PreDestroyProgram.class);

  private final Object _bean;
  private final Method _destroy;

  public DestroyListener(Object bean, Method destroy)
  {
    _bean = bean;
    _destroy = destroy;
    _destroy.setAccessible(true);
  }
  
  /**
   * Handles the case where the environment is starting (after init).
   */
  public void environmentConfigure(EnvironmentClassLoader loader)
  {
  }
  
  /**
   * Handles the case where the environment is starting (after init).
   */
  public void environmentBind(EnvironmentClassLoader loader)
  {
  }

  /**
   * Handles the case where the environment is starting (after init).
   */
  public void environmentStart(EnvironmentClassLoader loader)
  {
  }

  /**
   * Handles the case where the environment is stopping
   */
  public void environmentStop(EnvironmentClassLoader loader)
  {
    try {
      _destroy.invoke(_bean);
    } catch (Exception e) {
      log.log(Level.WARNING, e.toString(), e);
    }
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _destroy + "]";
  }
}
