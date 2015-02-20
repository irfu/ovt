/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/event/CoordinateSystemEvent.java,v $
  Date:      $Date: 2003/09/28 17:52:39 $
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
 * CoordinateSystemEvent.java
 *
 * Created on March 20, 2000, 5:40 PM
 */
 
package ovt.event;

import java.beans.*;

/** 
 *
 * @author  root
 * @version 
 */
public class CoordinateSystemEvent extends PropertyChangeEvent {

  protected int window;
  
  /** Creates new CoordinateSystemEvent */
  public CoordinateSystemEvent(Object source, int window, int old_cs, int new_cs) {
    super(source, "coordinateSystem", new Integer(old_cs), new Integer(new_cs));
    this.window = window;
  }
  
  public int getOldCS() {
    return ((Integer)getOldValue()).intValue();
  }
  
  public int getNewCS() {
    return ((Integer)getNewValue()).intValue();
  }
  
  public int getWindow() {
    return window;
  }
  
}
