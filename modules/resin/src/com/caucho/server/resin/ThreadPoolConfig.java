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
 *   Free SoftwareFoundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.server.resin;

import com.caucho.util.L10N;
import com.caucho.util.ThreadPool;

import javax.annotation.PostConstruct;

public class ThreadPoolConfig {
  private static final L10N L = new L10N(ThreadPoolConfig.class);

  private static final int THREAD_GAP = 5;

  private int _threadMax = 256;
  private int _threadSpareMin = 5;

  public void setThreadMax(int threadMax)
  {
    _threadMax = threadMax;
  }

  /**
   * The minimum number of spare threads waiting for a connection.
   */
  public void setSpareThreadMin(int threadSpareMin)
  {
    _threadSpareMin = threadSpareMin;
  }

  @PostConstruct
  public void init()
  {
    ThreadPool threadPool = ThreadPool.getThreadPool();
    
    threadPool.setThreadMax(_threadMax);
    threadPool.setThreadIdleMax(_threadSpareMin + THREAD_GAP);
    threadPool.setThreadIdleMin(_threadSpareMin);
  }
}
