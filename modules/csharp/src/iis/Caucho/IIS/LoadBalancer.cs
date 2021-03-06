﻿/*
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
 * @author Alex Rojkov
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net.Sockets;

namespace Caucho.IIS
{
  public class LoadBalancer
  {
    HmuxChannelFactory _pool;
    private Strategy _strategyType = Strategy.ADAPTIVE;

    //supports just one server for now
    public LoadBalancer(String servers)
    {
      int portIdx = servers.LastIndexOf(':');
      String address = servers.Substring(0, portIdx);
      int port = int.Parse(servers.Substring(portIdx + 1, servers.Length - portIdx - 1));

      _pool = new HmuxChannelFactory(address, port);

      Init();
    }

    public void Init()
    {
    }

    public HmuxChannel OpenServer(String sessionId, HmuxChannelFactory xChannelFactory)
    {
      return _pool.OpenServer(sessionId, xChannelFactory);
    }

    public void SetStrategy(Strategy strategy)
    {
      _strategyType = strategy;
    }

    public Strategy GetStrategy()
    {
      return _strategyType;
    }

    public void Destroy()
    {
      _pool.Close();
    }

    public enum Strategy
    {
      ADAPTIVE,
      ROUND_ROBIN
    }
  }
}
