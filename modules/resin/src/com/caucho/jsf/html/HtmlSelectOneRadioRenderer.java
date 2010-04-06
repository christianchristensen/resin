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

package com.caucho.jsf.html;

import com.caucho.util.Html;

import java.io.*;
import java.util.*;

import javax.faces.component.*;
import javax.faces.component.html.*;
import javax.faces.context.*;
import javax.faces.model.*;
import javax.faces.render.*;

/**
 * The HTML selectOne/radio renderer
 */
class HtmlSelectOneRadioRenderer extends SelectRenderer
{
  public static final Renderer RENDERER
    = new HtmlSelectOneRadioRenderer();

  /**
   * True if the renderer is responsible for rendering the children.
   */
  @Override
  public boolean getRendersChildren()
  {
    return true;
  }

  /**
   * Decodes the data from the form.
   */
  @Override
  public void decode(FacesContext context, UIComponent component)
  {
    String clientId = component.getClientId(context);

    ExternalContext ext = context.getExternalContext();
    Map<String,String[]> paramMap = ext.getRequestParameterValuesMap();

    String []value = paramMap.get(clientId);

    if (value != null && value.length > 0)
       //31d6
      ((EditableValueHolder) component).setSubmittedValue(value[0]);
    else
      ((EditableValueHolder) component).setSubmittedValue("");
  }
  
  /**
   * Renders the open tag for the text.
   */
  @Override
  public void encodeBegin(FacesContext context, UIComponent component)
    throws IOException
  {
    ResponseWriter out = context.getResponseWriter();

    String id = component.getId();

    String accesskey;
    int border;
    String dir;
    boolean disabled;
    String disabledClass;
    String enabledClass;
    String lang;
    String layout;
    
    String onblur;
    String onchange;
    String onclick;
    String ondblclick;
    String onfocus;
    
    String onkeydown;
    String onkeypress;
    String onkeyup;
    
    String onmousedown;
    String onmousemove;
    String onmouseout;
    String onmouseover;
    String onmouseup;
    
    String onselect;

    boolean readonly;
    String style;
    String styleClass;
    String tabindex;
    String title;
    Object value;

    if (component instanceof HtmlSelectOneRadio) {
      HtmlSelectOneRadio htmlComponent
	= (HtmlSelectOneRadio) component;

      accesskey = htmlComponent.getAccesskey();
      border = htmlComponent.getBorder();
      dir = htmlComponent.getDir();
      disabled = htmlComponent.isDisabled();
      disabledClass = htmlComponent.getDisabledClass();
      enabledClass = htmlComponent.getEnabledClass();
      lang = htmlComponent.getLang();
      layout = htmlComponent.getLayout();
      
      onblur = htmlComponent.getOnblur();
      onchange = htmlComponent.getOnchange();
      onclick = htmlComponent.getOnclick();
      ondblclick = htmlComponent.getOndblclick();
      onfocus = htmlComponent.getOnfocus();
      
      onkeydown = htmlComponent.getOnkeydown();
      onkeypress = htmlComponent.getOnkeypress();
      onkeyup = htmlComponent.getOnkeyup();
      
      onmousedown = htmlComponent.getOnmousedown();
      onmousemove = htmlComponent.getOnmousemove();
      onmouseout = htmlComponent.getOnmouseout();
      onmouseover = htmlComponent.getOnmouseover();
      onmouseup = htmlComponent.getOnmouseup();
      
      onselect = htmlComponent.getOnselect();
      
      readonly = htmlComponent.isReadonly();
      style = htmlComponent.getStyle();
      styleClass = htmlComponent.getStyleClass();
      tabindex = htmlComponent.getTabindex();
      title = htmlComponent.getTitle();

      value = htmlComponent.getValue();
    }
    else {
      Map<String,Object> attrMap = component.getAttributes();
      Integer iValue;
    
      accesskey = (String) attrMap.get("accesskey");

      iValue = (Integer) attrMap.get("border");
      border = iValue != null ? iValue : 0;
      dir = (String) attrMap.get("dir");
      disabled = Boolean.TRUE.equals(attrMap.get("disabled"));
      disabledClass = (String) attrMap.get("disabledClass");
      enabledClass = (String) attrMap.get("enabledClass");
      lang = (String) attrMap.get("lang");
      layout = (String) attrMap.get("layout");
      
      onblur = (String) attrMap.get("onblur");
      onchange = (String) attrMap.get("onchange");
      onclick = (String) attrMap.get("onclick");
      ondblclick = (String) attrMap.get("ondblclick");
      onfocus = (String) attrMap.get("onfocus");
      
      onkeydown = (String) attrMap.get("onkeydown");
      onkeypress = (String) attrMap.get("onkeypress");
      onkeyup = (String) attrMap.get("onkeyup");
      
      onmousedown = (String) attrMap.get("onmousedown");
      onmousemove = (String) attrMap.get("onmousemove");
      onmouseout = (String) attrMap.get("onmouseout");
      onmouseover = (String) attrMap.get("onmouseover");
      onmouseup = (String) attrMap.get("onmouseup");
      
      onselect = (String) attrMap.get("onselect");

      readonly = Boolean.TRUE.equals(attrMap.get("readonly"));
      style = (String) attrMap.get("style");
      styleClass = (String) attrMap.get("styleClass");
      tabindex = (String) attrMap.get("tabindex");
      title = (String) attrMap.get("title");
      
      value = attrMap.get("value");
    }

    UIViewRoot viewRoot = context.getViewRoot();
    
    out.startElement("table", component);

    if (border > 0)
      out.writeAttribute("border", border, "border");

    if (style != null)
      out.writeAttribute("style", style, "style");

    if (styleClass != null)
      out.writeAttribute("class", styleClass, "class");

    String clientId = component.getClientId(context);

    if (disabled)
      out.writeAttribute("disabled", "disabled", "disabled");

    out.write("\n");

    if (! "pageDirection".equals(layout)) {
      out.startElement("tr", component);
      out.write("\n");
    }

    List<SelectItem> items = accrueSelectItems(component);

    for (int i = 0; i < items.size(); i++) {
      SelectItem selectItem = items.get(i);

      String childId = clientId + ":" + i;
	
      if ("pageDirection".equals(layout)) {
	out.startElement("tr", component);
	out.write("\n");
      }
      
      out.startElement("td", component);
      out.startElement("input", component);
      out.writeAttribute("id", childId, "id");
      out.writeAttribute("name", clientId, "name");
      out.writeAttribute("type", "radio", "type");

      if (selectItem.isDisabled() || disabled)
	out.writeAttribute("disabled", "disabled", "disabled");

      if (value instanceof String) {
	String values = (String) value;

	if (value.equals(selectItem.getValue())) {
	  out.writeAttribute("checked", "checked", "value");
	}
      }

      if (accesskey != null)
	out.writeAttribute("accesskey", accesskey, "accesskey");

      if (dir != null)
	out.writeAttribute("dir", dir, "dir");

      if (lang != null)
	out.writeAttribute("lang", lang, "lang");

      if (onblur != null)
	out.writeAttribute("onblur", onblur, "onblur");

      if (onchange != null)
	out.writeAttribute("onchange", onchange, "onchange");

      if (onclick != null)
	out.writeAttribute("onclick", onclick, "onclick");

      if (ondblclick != null)
	out.writeAttribute("ondblclick", ondblclick, "ondblclick");

      if (onfocus != null)
	out.writeAttribute("onfocus", onfocus, "onfocus");

      if (onkeydown != null)
	out.writeAttribute("onkeydown", onkeydown, "onkeydown");

      if (onkeypress != null)
	out.writeAttribute("onkeypress", onkeypress, "onkeypress");

      if (onkeyup != null)
	out.writeAttribute("onkeyup", onkeyup, "onkeyup");

      if (onmousedown != null)
	out.writeAttribute("onmousedown", onmousedown, "onmousedown");

      if (onmousemove != null)
	out.writeAttribute("onmousemove", onmousemove, "onmousemove");

      if (onmouseout != null)
	out.writeAttribute("onmouseout", onmouseout, "onmouseout");

      if (onmouseover != null)
	out.writeAttribute("onmouseover", onmouseover, "onmouseover");

      if (onmouseup != null)
	out.writeAttribute("onmouseup", onmouseup, "onmouseup");

      if (onselect != null)
	out.writeAttribute("onselect", onselect, "onselect");

      if (readonly)
	out.writeAttribute("readonly", "readonly", "readonly");

      if (tabindex != null)
	out.writeAttribute("tabindex", tabindex, "tabindex");

      if (title != null)
	out.writeAttribute("title", title, "title");

      Object itemValue = selectItem.getValue();
      if (itemValue != null)
	out.writeAttribute("value", String.valueOf(itemValue), "value");
      
      out.endElement("input");

      out.startElement("label", component);
      out.writeAttribute("for", childId, "for");

      if (selectItem.isDisabled() || disabled) {
	if (disabledClass != null)
	  out.writeAttribute("class", disabledClass, "disabledClass");
      }
      else {
	if (enabledClass != null)
	  out.writeAttribute("class", enabledClass, "enabledClass");
      }

      String label = selectItem.getLabel();
      if (label == null)
        label = String.valueOf(selectItem.getValue());

      if (selectItem.isEscape())
        label = Html.escapeHtml(label);

      out.writeText(" ", component, null);
      out.writeText(label, "itemLabel");
      out.endElement("label");
      
      out.endElement("td");
      out.write("\n");

      if ("pageDirection".equals(layout)) {
	out.endElement("tr");
        out.write("\n");
      }
    }

    if (! "pageDirection".equals(layout)) {
      out.endElement("tr");
      out.write("\n");
    }

    out.endElement("table");
    out.write("\n");

    for (UIComponent child : component.getChildren()) {
      if (child instanceof UIComponent)
        child.encodeAll(context);
    }    
  }

  /**
   * Renders the content for the component.
   */
  @Override
  public void encodeChildren(FacesContext context, UIComponent component)
    throws IOException
  {
  }

  /**
   * Renders the closing tag for the component.
   */
  @Override
  public void encodeEnd(FacesContext context, UIComponent component)
    throws IOException
  {
  }

  public String toString()
  {
    return "HtmlInputTextRenderer[]";
  }
}
