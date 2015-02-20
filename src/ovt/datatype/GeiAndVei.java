/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/GeiAndVei.java,v $
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
 * GeiAndVei.java
 *
 * Created on March 23, 2000, 11:07 AM by ko
 */
 
package ovt.datatype;

/** 
 *
 * @author  root
 * @version 
 */
public class GeiAndVei extends Object {
  public double[] gei;
  public double[] vei;
  /** creates new object GeiAndVei*/
  public GeiAndVei() {
    gei = new double[3];
    vei = new double[3];
  }
  public GeiAndVei(GeiAndVei x) {
    gei = new double[3];
    vei = new double[3];
    for(int i=0;i<3;++i){
       gei[i]=x.gei[i];
       vei[i]=x.vei[i];
    }
  }
}
