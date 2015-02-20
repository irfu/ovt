/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/TrajectoryPoint.java,v $
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

package ovt.datatype;

import ovt.object.CoordinateSystem;

/**
 * The basic data type of the satellite's trajectory. 
 */

public class TrajectoryPoint {
/** modified Julian day (mjd = 0 for 1950) */
   public double mjd;   
/** GEI coordinates (RE) of the satellite */
   public double gei[] = new double[3];       
/** GEI velocity  km/s  */
   public double vei[] = new double[3];
/** GEO coordinates (RE) of the satellite */   
   public double geo[] = new double[3];       
/** GSM coordinates (RE) of the satellite */
   public double gsm[] = new double[3];
/** GSE coordinates (RE) of the satellite */      
   public double gse[] = new double[3];	
/** SM coordinates (RE) of the satellite */      
   public double sm[] = new double[3];	
   

/**
 * Returns point in the coordinate system <code>coordinateSystem</code>
 * @see ovt.Const#GEI
 */

  public double[] get(int coordinateSystem) throws IllegalArgumentException {
    switch (coordinateSystem) {
    	case CoordinateSystem.GEI:  return gei;
    	case CoordinateSystem.GSM:  return gsm;
    	case CoordinateSystem.GSE:  return gse;
        case CoordinateSystem.SM:  return sm;
    	//case Const.GSEQ: return "GSEQ";
    	case CoordinateSystem.GEO:  return geo;
    	/*case Const.SMC:  return "SMC";
    	case Const.COR:  return "COR";
    	case Const.ECC:  return "ECC";*/
    }
    throw new IllegalArgumentException("Invalid coord system '" + coordinateSystem + "'");
  }

}       


