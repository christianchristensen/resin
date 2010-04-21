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

package com.caucho.cloud.deploy;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.caucho.inject.Module;
import com.caucho.network.server.AbstractNetworkService;
import com.caucho.network.server.NetworkServer;
import com.caucho.network.server.NetworkService;

/**
 * Interface for a service registered with the Resin Server.
 */
@Module
public class DeployNetworkService extends AbstractNetworkService
{
  public static final int START_PRIORITY
    = NetworkService.START_PRIORITY_DEFAULT;
  public static final int STOP_PRIORITY
    = NetworkService.STOP_PRIORITY_DEFAULT;
  
  private final ConcurrentHashMap<String,DeployTagItem> _deployMap
    = new ConcurrentHashMap<String,DeployTagItem>();
  
  private final List<DeployTagListener> _tagListeners
    = new CopyOnWriteArrayList<DeployTagListener>();
  
  //
  // Returns the current service if available.
  //
  public static DeployNetworkService getCurrent()
  {
    NetworkServer server = NetworkServer.getCurrent();
    
    if (server != null)
      return server.getService(DeployNetworkService.class);
    else
      return null;
  }
  //
  // tag management
  //
  
  /**
   * Adds a tag
   */
  public void addTag(String tagName)
  {
    DeployTagItem item = new DeployTagItem(tagName);
    
    DeployTagItem oldItem = _deployMap.putIfAbsent(tagName, item);
    if (oldItem != null)
      return;
    
    for (DeployTagListener listener : _tagListeners) {
      listener.onTagAdd(tagName);
    }
  }
  
  /**
   * Removes a tag
   */
  public void removeTag(String tagName)
  {
    DeployTagItem oldItem = _deployMap.remove(tagName);
    
    if (oldItem != null) {
      for (DeployTagListener listener : _tagListeners) {
        listener.onTagRemove(tagName);
      }
    }
  }
  
  /**
   * Returns the tags in the deployment
   */
  public Set<String> getTagNames()
  {
    return _deployMap.keySet();
  }
  
  /**
   * Returns the tag item
   */
  public DeployTagItem getTagItem(String tag)
  {
    return _deployMap.get(tag);
  }
  
  //
  // tag listeners
  //
  
  /**
   * Adds a tag listener
   */
  public void addTagListener(DeployTagListener listener)
  {
    _tagListeners.add(listener);
    
    for (String tag : _deployMap.keySet()) {
      listener.onTagAdd(tag);
    }
  }
  
  /**
   * Removes a tag listener
   */
  public void removeTagListener(DeployTagListener listener)
  {
    _tagListeners.remove(listener);
  }
  
  //
  // NetworkService methods
  //

  /**
   * Returns the start priority order for the deploy service. Currently,
   * it has no dependencies, so it uses the start priority.
   */
  @Override
  public int getStartPriority()
  {
    return START_PRIORITY;
  }

  /**
   * Returns the stop priority order for the deploy service.
   */
  @Override
  public int getStopPriority()
  {
    return STOP_PRIORITY;
  }
}
