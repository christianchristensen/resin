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

import java.io.Serializable;
import java.util.*;

/**
 * pubsub query
 *
 * XEP-0060: http://www.xmpp.org/extensions/xep-0060.html
 *
 * <code><pre>
 * namespace = http://jabber.org/protocol/pubsub
 *
 * element pubsub {
 *   (create, configure?)
 *   | (subscribe?, options?)
 *   | affiliations
 *   | items
 *   | publish
 *   | retract
 *   | subscription
 *   | subscriptions
 *   | unsubscribe
 * }
 *
 * element item {
 *   attribute id?,
 *
 *   other?
 * }
 *
 * element items {
 *   attribute max_items?,
 *   attribute node,
 *   attribute subid?,
 *
 *   item*
 * }
 * </pre></code>
 */
public class PubSubItemsQuery extends PubSubQuery {
  private int _maxItems;
  private String _node;
  private String _subid;

  private PubSubItem []_items;

  public PubSubItemsQuery()
  {
  }

  public PubSubItemsQuery(String node)
  {
    _node = node;
  }

  public PubSubItemsQuery(String node, String subid, int maxItems)
  {
    _node = node;
    _subid = subid;
    _maxItems = maxItems;
  }

  public String getNode()
  {
    return _node;
  }

  public String getSubid()
  {
    return _subid;
  }

  public int getMaxItems()
  {
    return _maxItems;
  }
  
  public PubSubItem []getItems()
  {
    return _items;
  }
  
  public void setItems(PubSubItem []items)
  {
    _items = items;
  }
  
  public void setItemList(ArrayList<PubSubItem> itemList)
  {
    if (itemList != null && itemList.size() > 0) {
      _items = new PubSubItem[itemList.size()];
      itemList.toArray(_items);
    }
    else
      _items = null;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append(getClass().getSimpleName()).append("[");

    if (_node != null)
      sb.append("node=").append(_node);

    if (_subid != null)
      sb.append(",subid=").append(_subid);

    if (_maxItems > 0)
      sb.append(",max-items=").append(_maxItems);

    if (_items != null) {
      for (PubSubItem item : _items) {
	sb.append(",item=").append(item);
      }
    }

    sb.append("]");

    return sb.toString();
  }
}
