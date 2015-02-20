/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/DumpRecord.java,v $
  Date:      $Date: 2006/06/21 10:53:47 $
  Version:   $Revision: 2.8 $


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

import java.lang.*;
import java.io.*;

/** 
 *
 * @author  kono, ko
 * @version 
 */
public class DumpRecord extends Object{
  
  public static final byte numOfFields = 30;
  
  public static final byte NONE		= 0;
  public static final byte TIME		= 1;
  public static final byte POS_X	= 2;
  public static final byte POS_Y	= 3;
  public static final byte POS_Z	= 4;
  public static final byte[] POS = { POS_X, POS_Y, POS_Z };
  public static final byte R		= 5;
  public static final byte DIST		= 6;
  public static final byte DIFF		= 7;
  public static final byte DIST_TO_BS_ALONG_IMF	= 8;
  public static final byte THETA_IMF_NBS	= 9;
  public static final byte VEL		= 10;
  public static final byte FP1_LAT	= 11;
  public static final byte FP1_LON	= 12;
  public static final byte FP2_LAT	= 13;
  public static final byte FP2_LON	= 14;
  public static final byte[][] FP = { {FP1_LAT, FP1_LON}, {FP2_LAT, FP2_LON}};
  public static final byte DIST_TO_MP	= 15;
  public static final byte DIST_TO_BS	= 16;  
  public static final byte B   		= 17;
  public static final byte B_X		= 18;
  public static final byte B_Y		= 19;
  public static final byte B_Z		= 20;
  public static final byte B_FL_MIN	= 21;
  public static final byte B_FL_MIN_POS_X	= 22;
  public static final byte B_FL_MIN_POS_Y	= 23;
  public static final byte B_FL_MIN_POS_Z	= 24;
  public static final byte B_FL_MAX	= 25;
  
  public static final byte SPIN_B	= 26;
  public static final byte SPIN_V	= 27;
  public static final byte SPIN_S	= 28;
  public static final byte[] SPIN = { SPIN_B, SPIN_V, SPIN_S };
  public static final byte DIP_TILT	= 29;
  
  
  public static final byte SPIN_LABEL = 30;
  public static final byte POS_LABEL = 31;
  public static final byte FOOT_LABEL= 32;
  public static final byte MP_DIST_LABEL = 33;
  
  
  public static final String[] tags = {
  	"None",
        "Time",
        "POS X",
        "POS Y",
        "POS Z",
        "Radius",
        "DIST",
        "DIFF",
	"Dist. to BS along IMF",
        "IMF^N_BS",
        "Velocity",
        "FP1 LAT",
        "FP1 LON",
        "FP2 LAT",
        "FP2 LON",
        "DIST to MP",
        "DIST to BS",
        "B",
        "Bx",
        "By",
        "Bz",
	"B FL MIN",
	"B FL MIN POS X",
        "B FL MIN POS Y",
        "B FL MIN POS Z",
	"B FL MAX",
        "SPIN B",
        "SPIN V",
        "SPIN S",
        "Dipole tilt"
        }; 
        
 public static final String[] recDescr = {
 	"None",
        "Time",
        "X-coordinate",
        "Y-coordinate",
        "Z-coordinate",
        "Radial geocentric distance",
        "Foreshock coordinate DIST",
        "Foreshock coordinate DIFF",
	"Distance to bowshock along IMF",
        "Angle (deg) between IMF and normal to bow shock",
        "S/c velocity",
        "FP1 LAT",
        "FP1 LON",
        "FP2 LAT",
        "FP2 LON",
        "Rough distance to the magnetopause (positive=outside)",
        "Distance to the bow shock (positive=outside)",
        "Terrestrial magnetic field B or IMF if in solar wind (nT)",
        "X-component of the magnetic field Bx (nT)",
        "Y-component of the magnetic field By (nT)",
  	"Z-component of the magnetic field Bz (nT)",
	"Minimal value of a magnetic field of a field line (nT)",
	"X-coordinate of a point of minimal value of magnetic field of a field line",
	"Y-coordinate of a point of minimal value of magnetic field of a field line",
	"Z-coordinate of a point of minimal value of magnetic field of a field line",
	"Maximum value of a magnetic field of a field line (nT)",
        "Angle (deg) between the spin axis and magnetic field vector B",
        "Angle (deg) between the spin axis and velocity vector V",
        "Angle (deg) between the spin axis and the direction towards the Sun",
        "Dipole tilt angle"
        };  
  public static final String strMP_DIST_LABEL = "Distance to magnetopause";
  public static final String strSPIN_LABEL = "Spin axis angles (degrees)";
  
  private String[] fields = new String[numOfFields];
  
  public DumpRecord(){
    for(int i=0; i<numOfFields; ++i)
      switch(i) {
        case MP_DIST_LABEL: setField(i, new String(strMP_DIST_LABEL)); continue;
        case SPIN_LABEL: setField(i, new String(strSPIN_LABEL)); continue;
        default: setField(i, " ");
      }
  }
  
  public DumpRecord(DumpRecord source){
    for(byte i=0;i<numOfFields;++i)
      this.fields[i] = new String(source.fields[i]);
  }
  
  public String getField(int id){
    if(id<0 || id>=numOfFields)
      return "";
    else
      return new String(this.fields[id]);
  }
  
  public void setField(int id,String value){
    if(id>=0 && id<numOfFields && value.length()!=0)
      fields[id] = new String(value);
  }
  
  public Object clone(){
    return new DumpRecord(this);
  }
  
  public void print(){
    System.out.println("Printing DumpRecord:");
    for(int i=0;i<this.numOfFields;++i)
      System.out.println(getField(i));
  }
  
  public static String getName(int type) {
      try {
          return tags[type];
      } catch (IndexOutOfBoundsException e2) {
        throw new IllegalArgumentException("No such record type : '"+type+"'");
      }
  }
  
  public static int getType(String name) {
      for (int i=0; i<numOfFields; i++)
          if (name.equals(tags[i])) return i;
      throw new IllegalArgumentException("No such record type : '"+name+"'");
  }
}
