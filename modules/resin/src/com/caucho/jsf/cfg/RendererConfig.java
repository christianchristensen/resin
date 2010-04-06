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

import javax.annotation.*;

import javax.el.*;

import javax.faces.*;
import javax.faces.application.*;
import javax.faces.component.*;
import javax.faces.component.html.*;
import javax.faces.context.*;
import javax.faces.convert.*;
import javax.faces.el.*;
import javax.faces.event.*;
import javax.faces.render.*;
import javax.faces.validator.*;

import com.caucho.config.*;
import com.caucho.config.j2ee.*;
import com.caucho.jsf.el.*;
import com.caucho.util.*;

public class RendererConfig extends DescriptionGroupConfig
{
  private static final L10N L = new L10N(RendererConfig.class);

  private String _family;
  private String _type;
  private Class _class;

  private Renderer _renderer;

  public void setFamily(String family)
  {
    _family = family;
  }

  public void setComponentFamily(String family)
  {
    setFamily(family);
  }

  public void setType(String type)
  {
    _type = type;
  }

  public void setRendererType(String type)
  {
    setType(type);
  }

  public void setClass(Class cl)
  {
    Config.validate(cl, Renderer.class);
    
    _class = cl;
  }

  public void setRendererClass(Class cl)
  {
    setClass(cl);
  }

  public void addFacet(FacetConfig facet)
    throws ConfigException
  {
  }

  public void addAttribute(AttributeConfig attribute)
    throws ConfigException
  {
  }

  public void setRendererExtension(ConfigProgram program)
    throws ConfigException
  {
  }

  @PostConstruct
  public void init()
    throws InstantiationException, IllegalAccessException
  {
    _renderer = (Renderer) _class.newInstance();
  }
  
  public void configure(RenderKit renderKit)
  {
    renderKit.addRenderer(_family, _type, _renderer);
  }
}
