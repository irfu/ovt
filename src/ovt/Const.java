/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/Const.java,v $
  Date:      $Date: 2003/09/28 17:52:32 $
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

/* $Source: /stor/devel/ovt2g/ovt/Const.java,v $  $Date: 2003/09/28 17:52:32 $  */
package ovt;

import java.util.*;

public final class Const {

public static final int XYZ  = 0;
public static final int POLAR  = 1;  
  
//!!public static final double WCT = WORLD_COORDINATE_TEXT;
/** degrees to radians */
public static final double D_TO_R = 0.017453293;
/** radians to degrees */
public static final double R_TO_D = 57.29577951; 
/** Magnetic local time (MLT) to radians     */ 
public static final double M_TO_R = 0.261799388; 
public static final double GEX = 45.0;    /* plot extent */
public static final double PEX = 0.75;
/** earth radius */
public static final double RE = 6371.2; 


public static final int HOLLOW  = 0;
public static final int SOLIDI  = 1;
public static final int PATTERN = 2;

public static final int NONE_STRUCT = 50;
public static final int YCUT_STRUCT = 51;
public static final int EDGE_STRUCT = 52;
public static final int EDYC_STRUCT = 53;

public static final int IN_LINE  = 61;
public static final int OUT_LINE = 62;
public static final int IO_LINE  = 63;

public static final int C_BAW  =  0;
public static final int C_RGB  =  1;
public static final int C_SYM  =  2;
public static final int LIN =   1;
public static final int LOG =   0;


// Indexes of coordinate systems



// CoordSystems contains coordinate systems used in GUI

public static final Vector CoordSystems = new Vector();

static {

	
	CoordSystems.addElement("GSM");
	CoordSystems.addElement("GSE");
	CoordSystems.addElement("GEI");
	CoordSystems.addElement("GEO");
	CoordSystems.addElement("SMC");
	CoordSystems.addElement("COR");
	CoordSystems.addElement("ECC");
	
}





//public static final int NORTH =  2;

public static final int NORTHERN_HEM =  0;
public static final int SOUTHERN_HEM =  1;

public static final int N_A_S =  4;

public static final int NSAT1 = 11;
public static final int MAXPLOT = 30;
 
//!!public static final  VABS(v)        sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2])
//!!public static final  VDOT(a,b)      (a[0]*b[0]+a[1]*b[1]+a[2]*b[2])
/** 10 (6) number of angular sectors in 90 deg range */
public static final int NAS   =   6;
/** 15 (6) number of edge points on 0 -Xlim interval */
public static final int LIM    =  6;   
/** number of points on the mpause edge (2*NAS+LIM+1) */
public static final int MAXE = 2*NAS+LIM+1; 
public static final int NPF   =   120;     /* max number of points along a field line */
public static final int NPO   =   120;     /* max number of points on satellite orbit */
/** number of y-plane field lines */
public static final int NPY     = 7;      
/** altitude (km) for footprint tracing */
public static final double FALT  =100.0;    
/** error (RE) in last closed line search */
public static final double ERR_MP = 0.05;     
/** update structure with tilt step ERR_TILT */
public static final double ERR_TILT =4.0;     
/** exterior cusp separation (RE) at cusp_index */
public static final double C_SEPARATION = 1.5; 
public static final int EQ_MIN  =  1;     /* if 1 then the eqatorial cross is minimum of
                           the northern and southern hemisphere.
                           if 0 north and south hemispheres are independent */

}
