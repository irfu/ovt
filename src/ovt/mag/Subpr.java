/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/Subpr.java,v $
  Date:      $Date: 2003/09/28 17:52:44 $
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

package ovt.mag;

public class Subpr {

/* bsh subsolar distance 
	gsm[3] - position vector  
  output:
	rv[3] - bow shock vector (GSM) in the direction of gsm[3]
	rho   - cylindrical radius of the bow shock for x=gsm[0] 
		 from Howe and Binsack, JGR, 77, 1972. No aberration */
public static native double rv_bow_shock(double bsubx, double gsm[],
															 double rv[]);

/*  JNI to d_from_bsh
	returns distance (units RE) from gsm to the bow shock in the 
    spherical radial direction for x>0 and cylindical radial for x<0.
    ( positive distance outside the bow shock). Based on the formula
    from Howe and Binsack, JGR, 77, 3334, 1972. No aberration.
*/
public static native double getDistanceFromBsh(double[] gsm, double Bsub); 



//static {
//    System.loadLibrary("ovt2g");
//}

}
