/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/util/TransCollection.java,v $
  Date:      $Date: 2003/09/28 17:52:57 $
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
 * TransCollection.java
 *
 * Created on March 24, 2000, 7:39 PM
 */
 
package ovt.util;

import ovt.mag.*;
import ovt.mag.model.*;
import java.util.*;

/** 
 *
 * @author  root
 * @version 
 */
public class TransCollection extends Hashtable {

  protected IgrfModel igrfModel;
  
  /** Creates new TransCollection */
  public TransCollection(IgrfModel igrfModel) {
    this.igrfModel = igrfModel;
  }

  /** Returns Trans setted up for mjd */
  public Trans getTrans(double mjd) {
    Trans trans = (Trans)(get(new Double(mjd)));
    if (trans == null) {
      // create new Trans
      trans = new Trans(mjd, igrfModel);
      // put to the hashtable
      put(new Double(mjd), trans);
    }
    return trans;
  }
  
}
