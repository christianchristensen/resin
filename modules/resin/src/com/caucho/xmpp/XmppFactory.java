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

package com.caucho.xmpp;

import com.caucho.xmpp.im.Text;
import com.caucho.xmpp.im.ImPresence;
import com.caucho.xmpp.im.ImMessage;
import com.caucho.bam.*;
import com.caucho.vfs.*;
import com.caucho.xml.stream.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.xml.stream.*;

/**
 * Creates XmppReaders and XmppWriters.
 */
public class XmppFactory
{
  private static final Logger log
    = Logger.getLogger(XmppFactory.class.getName());

  private XmppMarshalFactory _marshalFactory = new XmppMarshalFactory();

  public XmppWriter newWriter(OutputStream os)
  {
    WriteStream wOut = Vfs.openWrite(os);
    
    XmppStreamWriterImpl out
      = new XmppStreamWriterImpl(wOut, _marshalFactory);
    
    XmppWriterImpl writerImpl = new XmppWriterImpl(null, out);

    return new XmppWriter(writerImpl);
  }
  
  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + "]";
  }
}
