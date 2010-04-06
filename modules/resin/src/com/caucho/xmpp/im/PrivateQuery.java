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

package com.caucho.xmpp.im;

import java.io.Serializable;
import java.util.*;

/**
 * private data storage
 *
 * XEP-0049: http://www.xmpp.org/extensions/xep-0049.html
 *
 * <code><pre>
 * namespace = "jabber:iq:private"
 *
 * element query {
 *   other?
 * }
 * </pre></code>
 */
public class PrivateQuery implements Serializable {
  private String _name;
  private String _uri;
  
  private String _data;

  public PrivateQuery()
  {
  }

  public PrivateQuery(String name, String uri, String data)
  {
    _name = name;
    _uri = uri;
    _data = data;
  }

  public String getName()
  {
    return _name;
  }

  public String getUri()
  {
    return _uri;
  }

  public String getData()
  {
    return _data;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName());
    sb.append("[");

    sb.append(_name).append("{").append(_uri).append("}");
    
    sb.append("]");

    return sb.toString();
  }
}
