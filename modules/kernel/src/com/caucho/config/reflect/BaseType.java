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

package com.caucho.config.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;

import com.caucho.config.inject.InjectManager;
import com.caucho.inject.Module;

/**
 * type matching the web bean
 */
@Module
abstract public class BaseType
{
  private static final BaseType []NULL_PARAM = new BaseType[0];
  
  public static BaseType create(Type type, 
                                HashMap<String,BaseType> paramMap)
  {
    if (type instanceof Class<?>) {
      Class<?> cl = (Class<?>) type;
      
      TypeVariable<?> []typeParam = cl.getTypeParameters();
      
      if (typeParam == null || typeParam.length == 0)
	return new ClassType(cl);

      BaseType []args = new BaseType[typeParam.length];

      HashMap<String,BaseType> newParamMap = new HashMap<String,BaseType>();

      for (int i = 0; i < args.length; i++) {
	args[i] = ClassType.OBJECT_TYPE;
	
	if (args[i] == null) {
	  throw new NullPointerException("unsupported BaseType: " + type);
	}
	
	newParamMap.put(typeParam[i].getName(), args[i]);
      }

      return new ParamType(cl, args, newParamMap);
    }
    else if (type instanceof ParameterizedType) {
      ParameterizedType pType = (ParameterizedType) type;

      Class<?> rawType = (Class<?>) pType.getRawType();

      Type []typeArgs = pType.getActualTypeArguments();
      
      BaseType []args = new BaseType[typeArgs.length];

      for (int i = 0; i < args.length; i++) {
	args[i] = create(typeArgs[i], paramMap);
	
	if (args[i] == null) {
	  throw new NullPointerException("unsupported BaseType: " + type);
	}
      }
      
      HashMap<String,BaseType> newParamMap = new HashMap<String,BaseType>();
      
      TypeVariable<?> []typeVars = rawType.getTypeParameters();

      for (int i = 0; i < typeVars.length; i++) {
	newParamMap.put(typeVars[i].getName(), args[i]);
      }

      return new ParamType(rawType, args, newParamMap);
    }
    else if (type instanceof GenericArrayType) {
      GenericArrayType aType = (GenericArrayType) type;

      BaseType baseType = create(aType.getGenericComponentType(), paramMap);
      Class<?> rawType = Array.newInstance(baseType.getRawClass(), 0).getClass();
      
      return new ArrayType(baseType, rawType);
    }
    else if (type instanceof TypeVariable<?>) {
      TypeVariable<?> aType = (TypeVariable<?>) type;

      BaseType actualType = null;

      if (paramMap != null)
	actualType = (BaseType) paramMap.get(aType.getName());

      if (actualType != null)
	return actualType;

      BaseType []baseBounds;

      if (aType.getBounds() != null) {
	Type []bounds = aType.getBounds();
	
	baseBounds = new BaseType[bounds.length];

	for (int i = 0; i < bounds.length; i++)
	  baseBounds[i] = create(bounds[i], paramMap);
      }
      else
	baseBounds = new BaseType[0];
      
      return new VarType(aType.getName(), baseBounds);
    }
    else if (type instanceof WildcardType) {
      WildcardType aType = (WildcardType) type;

      BaseType []lowerBounds = toBaseType(aType.getLowerBounds(), paramMap);
      BaseType []upperBounds = toBaseType(aType.getUpperBounds(), paramMap);
      
      return new WildcardTypeImpl(lowerBounds, upperBounds);
    }
    
    else {
      throw new NullPointerException("unsupported BaseType: " + type + " " + type.getClass());
    }
  }

  /**
   * Create a class-based type, where any parameters are filled with the
   * variables, not Object.
   */
  public static BaseType createClass(Class<?> type)
  {
    TypeVariable<?> []typeParam = type.getTypeParameters();
      
    if (typeParam == null || typeParam.length == 0)
      return new ClassType(type);

    BaseType []args = new BaseType[typeParam.length];

    HashMap<String,BaseType> newParamMap = new HashMap<String,BaseType>();

    for (int i = 0; i < args.length; i++) {
      args[i] = create(typeParam[i], newParamMap);
	
      if (args[i] == null) {
	throw new NullPointerException("unsupported BaseType: " + type);
      }
	
      newParamMap.put(typeParam[i].getName(), args[i]);
    }

    return new ParamType(type, args, newParamMap);
  }

  private static BaseType []toBaseType(Type []types,
                                       HashMap<String,BaseType> paramMap)
  {
    if (types == null)
      return NULL_PARAM;
    
    BaseType []baseTypes = new BaseType[types.length];

    for (int i = 0; i < types.length; i++) {
      baseTypes[i] = create(types[i], paramMap);
    }

    return baseTypes;
  }
  
  abstract public Class<?> getRawClass();

  public HashMap<String,BaseType> getParamMap()
  {
    return null;
  }

  public BaseType []getParameters()
  {
    return NULL_PARAM;
  }

  public boolean isWildcard()
  {
    return false;
  }

  abstract public boolean isMatch(Type type);

  public boolean isAssignableFrom(BaseType type)
  {
    throw new UnsupportedOperationException(getClass().getName());
  }

  /**
   * Assignable as a parameter.
   */
  public boolean isParamAssignableFrom(BaseType type)
  {
    return isAssignableFrom(type);
  }

  public Type toType()
  {
    throw new UnsupportedOperationException(getClass().getName());
  }

  public BaseType findClass(InjectManager manager, Class<?> cl)
  {
    throw new UnsupportedOperationException(getClass().getName());
  }

  public String getSimpleName()
  {
    return getRawClass().getSimpleName();
  }
  
  public String toString()
  {
    return getRawClass().getName();
  }
}
