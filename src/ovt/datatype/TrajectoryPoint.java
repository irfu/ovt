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

OVT Team (https://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
Khotyaintsev

=========================================================================*/

package ovt.datatype;

import ovt.object.CoordinateSystem;

/**
 * The basic data type of the satellite's trajectory.
 */

public class TrajectoryPoint {
   /** Modified Julian day (mjd = 0 for 1950). */
   public double mjd;
   /** GEI coordinates (RE) of the satellite. */
   public double gei[] = new double[3];
   /** GEI velocity, km/s.  */
   public double vei[] = new double[3];
   /** GEO coordinates (RE) of the satellite. */
   public double geo[] = new double[3];
   /** GSM coordinates (RE) of the satellite. */
   public double gsm[] = new double[3];
   /** GSE coordinates (RE) of the satellite. */
   public double gse[] = new double[3];
   /** SM coordinates (RE) of the satellite. */
   public double sm[] = new double[3];
   /** GEID coordinates (RE) of the satellite.
    */
   /* Only private because it was after other coordinate systems and being
    * public was not required.
    */
   private double geid[] = new double[3];

   /* NOTE: Constructor only seems to be called once in OVT. Could move that
    * initialization code (which does all the coordinate transformation) to this
    * constructor in principle and reduce the number of arguments, but that
    * requires selecting the source coordinate system.
    */
   public TrajectoryPoint(
      double mjd,
      double[] gei,
      double[] vei,
      double[] geo,
      double[] gsm,
      double[] gse,
      double[] sm,
      double[] geid
   ) {
       this.mjd = mjd;
       this.gei = gei;
       this.vei = vei;
       this.geo = geo;
       this.gsm = gsm;
       this.gse = gse;
       this.sm  = sm;
       this.geid = geid;
   }

/**
 * Returns point in the coordinate system <code>coordinateSystem</code>
 * @see ovt.Const#GEI
 */

  public double[] get(int coordinateSystem) throws IllegalArgumentException {
    switch (coordinateSystem) {
      case CoordinateSystem.GEI:  return gei;
      case CoordinateSystem.GSM:  return gsm;
      case CoordinateSystem.GSE:  return gse;
      case CoordinateSystem.SM:   return sm;
      //case Const.GSEQ: return "GSEQ";
      case CoordinateSystem.GEO:  return geo;
      /*case Const.SMC:  return "SMC";
      case Const.COR:  return "COR";
      case Const.ECC:  return "ECC";*/

      /* IMPLEMENTATION NOTE: Can not easily use ovt.util.Trans since the
      *  constructor requires an IgrfModel argument, even if the GEI-GEID
      *  transformation itself does not.
      */
      /* IMPLEMENTATION NOTE: Added coordinate system GEID
       * (GEI epoch/mean-of-date) after other coordinate systems. Therefore no
       * corresponding (public!) instance variable since it simply was not been
       * needed.
       */
      case CoordinateSystem.GEID: return geid;
    }
    throw new IllegalArgumentException("Invalid coordinate system '" + coordinateSystem + "'");
  }

}
