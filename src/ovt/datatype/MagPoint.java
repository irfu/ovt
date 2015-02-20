/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/MagPoint.java,v $
  Date:      $Date: 2003/09/28 17:52:37 $
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
 * MagPoint.java
 *
 * Created on March 17, 2000, 3:13 PM
 */
 
package ovt.datatype;

import ovt.mag.*;
import ovt.interfaces.*;
/** 
 *
 * @author  root
 * @version 
 */
public class MagPoint extends Object {
  
  public double mjd;
  public double[] gsm;
  public double[] bv;

  /** Creates new MagPoint */
  public MagPoint(double[] gsm, double[] bv, double mjd) {
    this.mjd = mjd;
    this.gsm = gsm;
    this.bv = bv;
  }
  
  public MagPoint(double[] gsm, MagModel magModel, double mjd) {
    this.mjd = mjd;
    this.gsm = gsm;
    this.bv = magModel.bv(gsm, mjd);
  }
  
  public Object clone() {
    return new MagPoint(gsm, bv, mjd);
  }
}
