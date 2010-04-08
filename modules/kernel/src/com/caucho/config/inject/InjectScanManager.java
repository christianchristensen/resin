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

package com.caucho.config.inject;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.caucho.loader.EnvironmentClassLoader;
import com.caucho.loader.enhancer.ScanClass;
import com.caucho.loader.enhancer.ScanListener;
import com.caucho.util.CharBuffer;
import com.caucho.util.L10N;
import com.caucho.vfs.Path;

/**
 * The web beans container for a given environment.
 */
class InjectScanManager
  implements ScanListener
{
  private static final L10N L = new L10N(InjectScanManager.class);
  private static final Logger log
    = Logger.getLogger(InjectScanManager.class.getName());
  
  private InjectManager _injectManager;
  
  private final HashMap<Path,ScanRootContext> _scanRootMap
    = new HashMap<Path,ScanRootContext>();
  
  private final ArrayList<ScanRootContext> _pendingScanRootList
    = new ArrayList<ScanRootContext>();
  
  private final ConcurrentHashMap<String,InjectScanClass> _scanClassMap
    = new ConcurrentHashMap<String,InjectScanClass>();

  InjectScanManager(InjectManager injectManager)
  {
    _injectManager = injectManager;
  }

  /**
   * Returns the injection manager.
   */
  public InjectManager getInjectManager()
  {
    return _injectManager;
  }
  
  //
  // ScanListener

  /**
   * Since CDI doesn't enhance, it's priority 1
   */
  @Override
  public int getScanPriority()
  {
    return 1;
  }
  
  @Override
  public boolean isRootScannable(Path root)
  {
    if (! (root.lookup("META-INF/beans.xml").canRead()
           || (root.getFullPath().endsWith("WEB-INF/classes/")
               && root.lookup("../beans.xml").canRead()))) {
      return false;
    }

    ScanRootContext context = _scanRootMap.get(root);

    if (context == null) {
      context = new ScanRootContext(root);
      _scanRootMap.put(root, context);
      _pendingScanRootList.add(context);
    }

    if (context.isScanComplete())
      return false;
    else {
      if (log.isLoggable(Level.FINER))
        log.finer("CanDI scanning " + root.getURL());

      context.setScanComplete(true);
      return true;
    }
  }

  /**
   * Checks if the class can be a simple class
   */
  @Override
  public ScanClass scanClass(String className, int modifiers)
  {
    // ioc/0j0k - package private allowed
    
    if (Modifier.isInterface(modifiers))
      return null;
    else if (Modifier.isPrivate(modifiers))
      return null;
    else if (Modifier.isAbstract(modifiers))
      return createScanClass(className);
    else
      return createScanClass(className);
  }
  
  InjectScanClass getScanClass(String className)
  {
    return _scanClassMap.get(className);
  }
  
  InjectScanClass createScanClass(String className)
  {
    InjectScanClass scanClass = _scanClassMap.get(className);
    
    if (scanClass == null) {
      className = className.intern();
      
      scanClass = new InjectScanClass(className, this);
      InjectScanClass oldScanClass;
      oldScanClass = _scanClassMap.putIfAbsent(className, scanClass);
      
      if (oldScanClass != null)
        scanClass = oldScanClass;
    }
    
    return scanClass;
  }

  @Override
  public boolean isScanMatchAnnotation(CharBuffer string)
  {
    return false;
  }

  @Override
  public void classMatchEvent(EnvironmentClassLoader loader, 
                              Path root,
                              String className)
  {
  }

}