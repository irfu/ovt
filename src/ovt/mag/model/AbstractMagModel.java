/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/model/AbstractMagModel.java,v $
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
 * AbstractMagModel.java
 *
 * Created on den 23 mars 2000, 00:19
 */

package ovt.mag.model;

import ovt.mag.*;
import ovt.util.*;
import ovt.interfaces.*;

/** 
 *
 * @author  Yuri Khotyaintsev
 * @version 
 */

public abstract class AbstractMagModel implements MagModel {
  
  protected MagProps magProps = null;
  
  public AbstractMagModel(MagProps magProps) {
    this.magProps = magProps;
  }
  
  public abstract double[] bv(double[] gsm, double mjd);
  
  public Trans getTrans(double mjd) {
    return magProps.getTrans(mjd);
  }
  
  public void setMagProps(MagProps magProps){
    this.magProps = magProps;
  } 

  public double getSint(double mjd) {
    return magProps.getSint(mjd);
  }
 
  public double getCost(double mjd) {
    return magProps.getCost(mjd);
  } 
  
}
