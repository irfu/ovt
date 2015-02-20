/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/ComponentPropertyEditor.java,v $
  Date:      $Date: 2003/09/28 17:52:33 $
  Version:   $Revision: 2.3 $


Copyright (c) 2000-2003 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev, 
Yuri Khotyaintsev)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification is permitted provided that the following conditions are met:

 * No part of the software can be included in any commercial package without
written consent from the OVT team.

 * Redistributions of the source or binary code must retain the above
copyright notice, this list of conditions and the following disclaimer.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS
IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT OR
INDIRECT DAMAGES  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.

OVT Team (http://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
Khotyaintsev

=========================================================================*/

/*
 * ComponentPropertyEditor.java
 *
 * Created on March 7, 2000, 3:32 PM
 */
 
package ovt.beans;

import java.beans.*;
import java.awt.*;

/** 
 *
 * @author  root
 * @version 
 */
public class ComponentPropertyEditor extends GUIPropertyEditor {

  protected Component component = null;
  
  /** Creates new ComponentPropertyEditor */
  public ComponentPropertyEditor(BasicPropertyDescriptor pd) {
    super(pd);
  }

/** Creates new ComponentPropertyEditor */
  public ComponentPropertyEditor(BasicPropertyDescriptor pd, Object[] values, String[] tags) {
    super(pd, values, tags);
  }

/** Creates new ComponentPropertyEditor */
  public ComponentPropertyEditor(BasicPropertyDescriptor pd, int[] values, String[] tags) {
    super(pd, values, tags);
  }
  
  
  /** If the editor needs custom editor it has to be returned here.
   *
   * @return GUI Editor
   */
  public Component getComponent() {
    return component;
  }
  
  public void setComponent(Component comp) {
    component = comp;
    try {
        addPropertyChangeListener((PropertyChangeListener)comp);
    } catch (ClassCastException e2) {
        System.err.println("Error: ComponentPropertyEditor.setComponent : component doesn't implement PropertyChangeListener");
    }
  }
}
