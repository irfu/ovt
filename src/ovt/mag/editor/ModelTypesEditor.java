/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/editor/ModelTypesEditor.java,v $
  Date:      $Date: 2003/09/28 17:52:44 $
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
 * ModelTypesEditor.java
 *
 * Created on March 24, 2000, 5:58 PM
 */
 
package ovt.mag.editor;

import ovt.util.*;
import ovt.beans.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import java.beans.*;
import javax.swing.*;
import java.awt.*;

/** 
 *
 * @author  root
 * @version 
 */
public class ModelTypesEditor extends WindowPropertyEditor {

  /** Creates new ModelTypesEditor */
  public ModelTypesEditor(BasicPropertyDescriptor pd) {
    super(pd);
  }
  
  
  public Component getComponent() {
    return null;
  }
}
