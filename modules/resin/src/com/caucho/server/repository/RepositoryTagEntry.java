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

package com.caucho.server.repository;

import com.caucho.vfs.*;

import java.util.*;
import java.io.*;

/**
 * Represents a committed tag root
 */
public class RepositoryTagEntry
{
  private final String _sha1; // sha1 of the entry itself
  
  private final String _tag;
  
  private final String _root; // sha1 of the target root
  private final String _parent; // sha1 of the parent tag entry

  /**
   * Create an entry from an existing repository item.
   */
  public RepositoryTagEntry(String sha1,
			String tag,
			String root,
			String parent)
  {
    _tag = tag;
    _root = root;
    _parent = parent;
    
    _sha1 = sha1;
  }

  /**
   * Create a new entry, storing the serialized form in the repository.
   */
  public RepositoryTagEntry(Repository repository,
			String tag,
			String root,
			String parent)
    throws IOException
  {
    _tag = tag;
    _root = root;
    _parent = parent;
    
    TempStream os = new TempStream();
    WriteStream out = new WriteStream(os);

    writeEntry(out);
    out.close();

    InputStream is = os.getInputStream();

    try {
      _sha1 = repository.addInputStream(is);
    } finally {
      is.close();
    }
  }

  /**
   * Create a new entry, storing the serialized form in the repository.
   */
  public RepositoryTagEntry(Repository repository,
			String sha1)
    throws IOException
  {
    _sha1 = sha1;

    InputStream is = repository.openBlob(sha1);

    try {
      ReadStream in = Vfs.openRead(is);
      
      Map<String,String> map = readMap(in);

      in.close();

      _tag = map.get("tag");
      _root = map.get("root");
      _parent = map.get("parent");
    } finally {
      is.close();
    }
  }

  /**
   * Returns the hash of the entry itself
   */
  public String getSha1()
  {
    return _sha1;
  }

  /**
   * Returns the hash of the entry's root
   */
  public String getRoot()
  {
    return _root;
  }

  private Map<String,String> readMap(ReadStream is)
    throws IOException
  {
    HashMap<String,String> map = new HashMap<String,String>();

    String line;

    while ((line = is.readLine()) != null) {
      int p = line.indexOf(": ");

      String key = line.substring(0, p);
      String value = line.substring(p + 2);

      while (value.endsWith("\\")) {
	value = value.substring(0, value.length() - 1);
	line = is.readLine();

	if (line != null)
	  value += line;
      }

      map.put(key, value);
    }

    return map;
  }

  private void writeEntry(WriteStream out)
    throws IOException
  {
    writePair(out, "tag", _tag);
    writePair(out, "root", _root);
    writePair(out, "parent", _parent);
  }

  private void writePair(WriteStream out, String key, String value)
    throws IOException
  {
    if (value == null)
      return;
    
    while (value.endsWith("\\") || value.endsWith("\n"))
      value = value.substring(0, value.length() - 1);

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);

      if (ch != '\n')
	sb.append(ch);
      else
	sb.append("\\\n");
    }
    
    value = sb.toString();

    out.print(key);
    out.print(": ");
    out.println(value);
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _tag + ",root=" + _root + "]";
  }
}