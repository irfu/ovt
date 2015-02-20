/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/ycutfield.java,v $
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

package ovt.datatypes;

public class ycutfield       //  ycut
{  
   public static final int NPF = 120;     /* max number of points along a field line */
   public static final int NPY = 7;     /* number of y-plane field lines */

   public double mjd;          /* modified Julian day */
   public double model;        /* magnetic field model */
   public double tilt;         /* dipole tilt angle (deg) */
   public double xlim;		/* minimum distance in the tail */
   public double imf[] = new double[3];       /* interplanetary magnetic field */
   public int method;         /* method=0 no imf, 1=linear, 2=liner vanishing
			   3= normal vanishing */
   public int cusp_index[] = new int[4];    /* index such that edges with index 
                          		gives position of the exterior cusp    */
   public int nedge[] = new int[4];	/* number of points in edges */
   public float edges[][] = new float[4][NPF*3]; /* field lines at edges */
   public int npy[][] = new int[2][NPY];       /* number of points in yplane lines */
   public float yplane[][][] = new float[2][NPY][NPF*3];  /*field lines at 70,75,80,85,90,85,80,75,70 
			 				first 2 lines in NPY mark the cusp  */
   public double cccusp[][] = new double[4][3]; /* corrected coordinates of cusp field lines */
   public double smcusp[][] = new double[4][3]; /* dipole (SMC) coordinates of cusp field lines */
}

