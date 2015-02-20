/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/event/TimeEvent.java,v $
  Date:      $Date: 2003/09/28 17:52:39 $
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
 * TimeEvent.java
 *
 * Created on March 8, 2000, 1:39 PM
 */
 
package ovt.event;

import ovt.datatype.*;

import java.beans.*;
import java.util.*;
/** 
 *
 * @author  root
 * @version 
 */
public class TimeEvent extends java.util.EventObject {
  
  public final static int TIME_SET    = 0;
  public final static int CURRENT_MJD = 1;
  
  private int whatChanged;
  private TimeSet timeSet;
  
  /** Creates new TimeEvent */
  public TimeEvent(Object source, int whatChanged, TimeSet newTimeSet) {
      super(source);
      this.whatChanged = whatChanged;
      this.timeSet = newTimeSet;
  }
  
  public boolean timeSetChanged() {
    return (whatChanged == TIME_SET);
  }
  
  public boolean currentMjdChanged() {
    return (whatChanged == TIME_SET || whatChanged == CURRENT_MJD);
  }
  
  public TimeSet getTimeSet() {
    return timeSet;
  }
  
}
