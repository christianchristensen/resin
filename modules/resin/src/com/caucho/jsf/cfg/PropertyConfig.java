/*
 * Copyright (c) 1998-2010 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
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

package com.caucho.jsf.cfg;

import com.caucho.config.types.DescriptionGroupConfig;
import com.caucho.config.program.ConfigProgram;
import java.util.*;

import javax.el.*;

import javax.faces.*;
import javax.faces.application.*;
import javax.faces.component.*;
import javax.faces.component.html.*;
import javax.faces.context.*;
import javax.faces.convert.*;
import javax.faces.el.*;
import javax.faces.event.*;
import javax.faces.validator.*;

import com.caucho.config.*;
import com.caucho.config.j2ee.*;
import com.caucho.util.*;

public class PropertyConfig extends DescriptionGroupConfig
{
  private String _name;
  
  private Class _class;
  
  private String _defaultValue;
  
  private String _suggestedValue;

  public void setName(String name)
  {
    _name = name;
  }

  public void setPropertyName(String name)
  {
    setName(name);
  }

  public void setClass(Class cl)
    throws ConfigException
  {
    Config.checkCanInstantiate(cl);

    _class = cl;
  }

  public void setPropertyClass(Class cl)
    throws ConfigException
  {
    _class = cl;
  }

  public void setDefaultValue(String defaultValue)
  {
    _defaultValue = defaultValue;
  }

  public void setSuggestedValue(String suggestedValue)
  {
    _suggestedValue = suggestedValue;
  }

  public void setPropertyExtension(ConfigProgram program)
    throws ConfigException
  {
  }
}
