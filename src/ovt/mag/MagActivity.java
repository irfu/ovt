/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/MagActivity.java,v $
  Date:      $Date: 2003/09/28 17:52:43 $
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

package ovt.mag;

/** Time dependent magnetic activity data */
public class MagActivity implements Cloneable {

/** modified Julian day */
   protected double mjd = 12000;   // let it be.

/** KP Index */
   public double KPindex;

/** Interplanetary magnetic field. It has 3 components: Bx, By, Bz */       
   public double[] imf = {0, 0, 0};

/** Solar wind pressue <code>0.5</code> to <code>10</code> nT */
public double PSW = 4;       

/** DSTIndex.  Range from <code>-100</code> to <code>20</code>  nT */
public double DSTindex = -40;  

/** Create the object */
MagActivity() { }

/** Create MagActivity up to mjd. */
public MagActivity(double anmjd) {
	// To be corrected! All data should be recieved from internet/files
	mjd = anmjd;
	KPindex = 0;
	imf = new double[3];
	for (int i=0; i<3; i++) imf[i] = 0;
	PSW = 4;
	DSTindex=-40.0;
}

public Object clone() {
	MagActivity ma = new MagActivity();
	ma.mjd = mjd;
	ma.KPindex = KPindex;
	ma.imf = (double[])imf.clone();
	ma.PSW = PSW;
	ma.DSTindex = DSTindex;
	return ma;
}

}
