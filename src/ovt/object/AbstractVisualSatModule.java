/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/AbstractVisualSatModule.java,v $
  Date:      $Date: 2003/09/28 17:52:45 $
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
 * AbstractVisualSatModule.java
 *
 * Created on March 30, 2000, 4:03 PM
 */
 
package ovt.object;

import ovt.*;
import ovt.mag.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import vtk.*;

import java.beans.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;

/** 
 *
 * @author  ko
 * @version 
 */
public class AbstractVisualSatModule extends AbstractSatModule {


  /** Creates new AbstractVisualSatModule */
  public AbstractVisualSatModule(Sat sat, String name, String iconFilename) {
    super(sat, name, iconFilename);
  }
  
  /** Creates new AbstractVisualSatModule */
  public AbstractVisualSatModule(Sat sat, String name) {
    super(sat, name);
  }
  
  /** Creates new AbstractVisualSatModule
  public AbstractVisualSatModule(Sat sat) {
    super(sat);
  } */
  
  protected vtkActor getActor() {
    return null;
  }
  
  
  protected void show() {
    if (canBeVisible() && !actorIsInRenderer()) getRenderer().AddActor(getActor());
  }

  protected void hide() {
    if (actorIsInRenderer()) getRenderer().RemoveActor(getActor());
  }
  
  protected boolean actorIsInRenderer() { 
    int positionInList = getRenderer().GetActors().IsItemPresent(getActor());
    return (positionInList != 0); 
  }
}
