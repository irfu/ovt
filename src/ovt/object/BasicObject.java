/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/BasicObject.java,v $
  Date:      $Date: 2003/09/28 17:52:45 $
  Version:   $Revision: 2.4 $


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
 * BasicObject.java
 *
 * Created on March 10, 2000, 5:23 PM
 */
 
package ovt.object;

import ovt.*;
import ovt.mag.*;
import ovt.beans.*;
import ovt.util.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import vtk.*;

import java.util.*;


/** 
 *
 * @author  root
 * @version 
 */
public class BasicObject extends OVTObject implements CoreSource, Renderable  {

  /** Holds value of property core. */
  private OVTCore core = null;
  
  /** Creates new BasicObject */
  public BasicObject(OVTCore core, String name) {
    super(name);
    this.core = core;
  }
  
  /** Creates new BasicObject */
  public BasicObject(OVTCore core) {
    this.core = core;
  }
  
  /** Getter for property core.
   * @return Value of property core.
   */
  public OVTCore getCore() {
    return core;
  }
  
  public int getCS() {
    return core.getCS();
  }
  
  public int getPolarCS() {
    return core.getPolarCS();
  }
  
  /** Returns current time */
  public double getMjd() {
    return core.getMjd();
  }
  
  /** Returns current time index. 0 - corresponds to startMjd*/
  public int getMjdIndex() {
    return core.getTimeSettings().getTimeSet().getCurrentMjdIndex();
  }
  
  /** Returns the number of mjd values in the current time settings */
  public int getNumberOfMjdValues() {
    return core.getTimeSettings().getTimeSet().getNumberOfValues();
  }
  
  /** Returns the maximum mjdIndex (which corresponds to stopMjd)*/
  public int getMaxMjdIndex() {
    return core.getTimeSettings().getTimeSet().getNumberOfValues() - 1;
  }
  
  public Trans getTrans(double mjd) {
    return core.getTrans(mjd);
  }
  
  public MagProps getMagProps() {
    return core.getMagProps();
  }

  
  /** Getter for renderer.
   * @return Value of property renderer.
   */
  public vtkRenderer getRenderer() {
    return core.getRenderer();
  }
  
  public void Render() {
    core.Render();
  }
  
}
