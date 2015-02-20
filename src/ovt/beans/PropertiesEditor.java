/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/PropertiesEditor.java,v $
  Date:      $Date: 2003/09/28 17:52:35 $
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

package ovt.beans;

/** 
 *
 * @author  Yuri Khotyaintsev
 * @version 
 */
public class PropertiesEditor extends MenuPropertyEditor {

  /** Creates new PropertiesEditor with default tags (show/hide)*/
  public PropertiesEditor(BasicPropertyDescriptor pd) {
    super(pd, MenuPropertyEditor.SWITCH);
    initialize(new String[]{"Properties ...", "hide"});
  }
  
  /** Creates new PropertiesEditor with tags (showStr/hideStr) */
  public PropertiesEditor(BasicPropertyDescriptor pd, String showStr, String hideStr) {
    super(pd, MenuPropertyEditor.SWITCH);
    initialize(new String[]{showStr, hideStr});
  }
  
  public PropertiesEditor(BasicPropertyDescriptor pd, int type) {
    super(pd, type);
    initialize(new String[]{"show", "hide"});
  }
  
  protected void initialize(String[] tags) {
    setTags(tags);
    setValues(new Object[]{new Boolean(true), new Boolean(false)});
  }
}
