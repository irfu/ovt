/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/TLESat.java,v $
  Date:      $Date: 2003/09/28 17:52:52 $
  Version:   $Revision: 1.2 $


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
 * TLESat.java
 *
 * Created on ??????, 10, ??????? 2003, 11:22
 */

package ovt.object;

import ovt.*;

import java.io.*;

/**
 *
 * @author  ko
 */
public class TLESat extends Sat  {
 
  /** Contructor. Also used by xml */
  public TLESat(OVTCore core) {
    super(core);
  }
    
   /** 
    *  Implementation of the parents abstract method.
    * Reads the orbitFile and returns the time for the first and last orbital data in the orbit data file, period of the satellite revolution.
    * 
    *  @return res[0] - firstMjd, res[1] - lastMjd, res[2] - periodDays
    */

protected double[] getFirstLastMjdPeriodSatNumber() throws java.io.IOException {
    return ovt.util.Tle.getFirstLastMjdPeriodSatNumber(orbitFile);
}

/** 
 * Implementation of the parents abstract method. Reads the orbitFIle, computes... returns...
 */
protected  void fill_GEI_VEI(double[] timeMap, double[][] gei_arr, double[][] vei_arr) throws IOException {
    getSatPosJNI(orbitFile.getAbsolutePath(), timeMap, gei_arr, vei_arr, timeMap.length);
}


/**
 * JNI interface to a c routine Java_ovt_object_TLESat_getSatPosJNI(env,obj,jfilename,jmjd,jgei,jvei,jn) 
 * defined at getsatpos.c
 * Returns S/C position and velocity
 * @param filename Orbit Data File
 * @param mjd Time
 * @return S/C position and veocity, error code : 0 - normal execution, -1 - problem occured
 */
public native int getSatPosJNI(String filename, double[] mjd, double[][] gei, double[][] vei, int N);

  
}
