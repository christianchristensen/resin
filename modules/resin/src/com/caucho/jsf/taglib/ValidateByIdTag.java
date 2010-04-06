/*
 * Copyright (c) 1998-2010 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
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

package com.caucho.jsf.taglib;

import java.io.*;
import java.util.*;

import javax.el.*;

import javax.faces.*;
import javax.faces.application.*;
import javax.faces.component.*;
import javax.faces.context.*;
import javax.faces.validator.*;
import javax.faces.webapp.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import com.caucho.util.*;

public class ValidateByIdTag extends ValidatorELTag
{
  private ValueExpression _bindingExpr;
  
  private ValueExpression _validatorIdExpr;

  public void setBinding(ValueExpression expr)
  {
    _bindingExpr = expr;
  }

  public void setValidatorId(ValueExpression expr)
  {
    _validatorIdExpr = expr;
  }
  
  protected Validator createValidator()
  {
    FacesContext context = FacesContext.getCurrentInstance();

    Application app = context.getApplication();

    Validator validator = null;

    ELContext elContext = context.getELContext();
      
    if (_bindingExpr != null)
      validator = (Validator) _bindingExpr.getValue(elContext);

    if (validator == null) {
      String id = (String) _validatorIdExpr.getValue(elContext);

      validator = app.createValidator(id);

      if (_bindingExpr != null)
	_bindingExpr.setValue(elContext, validator);
    }

    return validator;
  }
}
