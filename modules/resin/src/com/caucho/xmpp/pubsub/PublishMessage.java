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

package com.caucho.xmpp.pubsub;

import com.caucho.xmpp.pubsub.PubSubItem;
import java.util.*;
import java.io.Serializable;

/**
 * Publish event
 *
 * http://jabber.org/protocol/pubsub#event
 *
 * <code><pre>
 * element event {
 *   collection?
 *   | configuration?
 *   | delete?
 *   | items?
 *   | purge?
 *   | subscription?
 * }
 *
 * element items {
 *   @node,
 *   (item*
 *    | retract*)
 * }
 *
 * element item {
 *   @id?,
 *   @node?,
 *   _extra?
 * }
 * </pre></code>
 */
public class PublishMessage implements Serializable {
  private static final PubSubItem []NULL_ITEMS = new PubSubItem[0];
  
  private String _node;
  
  private PubSubItem []_items = NULL_ITEMS;

  /**
   * Hessian null constructor
   */
  private PublishMessage()
  {
  }

  public PublishMessage(String node)
  {
    _node = node;
  }

  public PublishMessage(String node, PubSubItem []items)
  {
    _node = node;
    _items = items;
  }

  public String getNode()
  {
    return _node;
  }

  public PubSubItem []getItems()
  {
    return _items;
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append(getClass().getSimpleName());
    sb.append("[node=");
    sb.append(_node);

    if (_items != null) {
      for (PubSubItem item : _items) {
	sb.append(",");
	sb.append(item);
      }
    }
    sb.append("]");

    return sb.toString();
  }
}
