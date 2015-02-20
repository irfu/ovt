/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/Julian.java,v $
  Date:      $Date: 2006/02/20 16:06:39 $
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

import ovt.*;

import ovt.util.*;

public class Julian {

public static final double J1950 = 2433282.50;
public static final double J2000 = 2451545.00;
public double integer = -1;               /* separate integer and fractional parts */
public double fraction = -1;              /* are used to minimize roundoff problems */

public Julian(double mjd) {
	Time time = new Time(mjd);
	time.setTime(time.year, time.month, time.day, 0, 0, 0);
	
	double mj0 = time.getMjd();
    
    integer = mj0 + J1950;
    fraction = mjd-mj0; 
}

public static double getMjd(Julian jday) {
	return (jday.integer - J1950) + jday.fraction;
}

public double getMjd() {
	return getMjd(this);
}


/*------------------------------------------------------ 
    FUNCTION:
       greenwich mean sideral time (radians)
       for modified julian day (mjd)
---------------------------------------------------------*/

public static double getGSMTime(Julian mjd) {
	return	Utils.gha(mjd)/Trans.RAD;
}

public double getGSMTime() {
	return getGSMTime(this);
}

}
