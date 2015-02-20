/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/util/CGMC.java,v $
  Date:      $Date: 2003/09/28 17:52:54 $
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
 * CGMC.java
 * by Grzegorz Juchnikowski
 * recalculation of coordinates:
 * Corrected Geo-magnetic  <--->  Geographical
 * and functions for Magnetic Local Time.
 * Needs two files: geo2cor.dat and cor2geo.dat, which were calculated
 * with the Fortran program cgmc.for for year 2000.
 */

package ovt.util;

import java.io.*;
import java.lang.Math.*;


import ovt.*;

public class CGMC {

  public native void newFiles(String path, int year);

  private boolean initialized;
  private int year;
  private int nlat,nlon;
  private double lat1,lon1,dlat,dlon;
  private float[][] alat,alon;
  private String path,typ;


  private void cCGMC(String _path, String _typ) {
    path = _path;
    typ = _typ;
    initialized = false;
    year = -1;
  }

  //this constructor is used for testing
  public CGMC(String _path, String _typ){ cCGMC(_path,_typ); }

  //this constructor is used in OVT
  public CGMC(OVTCore core, String _typ){ cCGMC(core.getUserdataDir(),_typ); }


  public void loadYear(int _year) throws IOException {
    //typ can be "g2c" or "c2g"
    if( initialized && (_year == year) ) return;
    initialized = false;
    year = _year;
    double lat,lon,flat,flon;
    int ilat,ilon;
    String fn = path+typ+"_"+year+".dat";
System.out.println("CGMC: Reading file \""+fn+"\"");
    ElectPotFileReader f;
    try{
      f = new ElectPotFileReader(fn);
    }catch(IOException e){
System.out.println("CGMC: newFiles(\""+path+"\","+year+") - start");
      try {
        newFiles(path,year);
System.out.println("CGMC: newFiles() - finish");
      }catch(Exception e2){
System.out.println(getClass().getName() + " -> " + e2.toString());
        throw new IOException(e2.toString());
      }
      f = new ElectPotFileReader(fn);
    }
    lat1 = f.GetNumber();
    nlat = (int)f.GetNumber();
    dlat = f.GetNumber();
    lon1 = f.GetNumber();
    nlon = (int)f.GetNumber();
    dlon = f.GetNumber();
    f.SkipEOL();
    alat = new float[nlat+1][nlon+1];
    alon = new float[nlat+1][nlon+1];
    for( ilat=0; ilat<nlat; ilat++ ){
      lat = ilat*dlat+lat1;
      for( ilon=0; ilon<nlon; ilon++ ){
        flat = f.GetNumber();
        flon = f.GetNumber();
        lon = ilon*dlon+lon1;
        if( (flat != lat) || (flon != lon) )throw new IOException("data file \""+fn+"\" corrupted");
        alat[ilat][ilon] = (float)f.GetNumber();
        alon[ilat][ilon] = (float)f.GetNumber();
        f.SkipEOL();
      }
    }
    f.Close();
    for( ilat=0; ilat<nlat; ilat++ ){
      alat[ilat][nlon] = alat[ilat][nlon-1];
      alon[ilat][nlon] = alon[ilat][nlon-1];
    }
    for( ilon=0; ilon<nlon; ilon++ ){
      alat[nlat][ilon] = alat[nlat-1][ilon];
      alon[nlat][ilon] = alon[nlat-1][ilon];
    }
    alat[nlat][nlon] = alat[nlat-1][nlon-1];
    alon[nlat][nlon] = alon[nlat-1][nlon-1];
    initialized = true;
  }

  public double[] transform(double[] latlon) throws Exception {
    int ilat,ilon;
    double lat,lon,plat,plon,a11,a12,a21,a22;
    if(! initialized ) throw new Exception("CGMC not initialized");
    lat = latlon[0];
    lon = latlon[1];
    while( lon < 0 ) lon += 360;
    while( lon >= 360 ) lon -= 360;
    if( (lat < -90) || (lat > 90) ) throw new Exception("error latitude="+lat);
    plat = (lat-lat1)/dlat;
    ilat = (int)plat;
    plat = plat - ilat;
    plon = (lon-lon1)/dlon;
    ilon = (int)plon;
    plon = plon - ilon;
    double[] result = new double[2];
    a11 = alat[ilat][ilon];
    a12 = alat[ilat][ilon+1];
    a21 = alat[ilat+1][ilon];
    a22 = alat[ilat+1][ilon+1];
    if( (a11 > 900) || (a12 > 900) || (a21 > 900) || (a22 > 900) ) throw new Exception("equatorial zone");
    result[0] = a11*(1-plat)*(1-plon) + a12*(1-plat)*plon
              + a21*plat*(1-plon)     + a22*plat*plon;
    a11 = alon[ilat][ilon];
    a12 = alon[ilat][ilon+1];
    if( a12 < a11 ) a12 += 360;
    a21 = alon[ilat+1][ilon];
    a22 = alon[ilat+1][ilon+1];
    if( a22 < a21 ) a22 += 360;
    if( (a11 > 900) || (a12 > 900) || (a21 > 900) || (a22 > 900) ) throw new Exception("equatorial zone");
    result[1] = a11*(1-plat)*(1-plon) + a12*(1-plat)*plon
              + a21*plat*(1-plon)     + a22*plat*plon;
    if( result[1] >= 360 ) result[1] -= 360;
    return result;
  }

  public static double MLT(double mltut, double ut){
/* (comments from Weimer)
C Next we can compute Corrected magnetic local time assuming that it changes linearly as 
C the UTtime elapses from the UTMidnight
C C_MLT=REAL_UT-UTMidnight
C if C_MLT lt 0.0 then C_MLT=24.0+C_MLT
C UT==UTMidnight
*/
    double mlt = ut-mltut;
    if( mlt < 0 ) mlt += 24;
    return mlt;
  }


  public static double MLTUT(double[] S, double[] C, double[] P) throws Exception {
/* (comments from Weimer)
C Below is the NSSDC procedure - probably better
C INPUT: geographic latitude and longitude(S[0], S[1]) of a given point (at a given altitude)
C	 corrected magnetic latitude of this point (C[0])
C	 (corrected) magnetic pole  geographic latitude and longitude at the  
C         altitude of the point (P[0],P[1]) 
C	 PLA,PLO can be computed from CORGEO with the input with CLA=-90.0 (south) or 90.0 (north)
C 	 and CLO 360.0, and the appropriate RH (geocentric distance of the point)
C OUTPUT is the UT hour of magnetic local midnight at the given corrected MLAT
C  *********************************************************************
C  Calculates the MLT midnight in UT hours 

C  Definition of the MLT midnight (MLTMN) here is different from the 
C  approach described elsewhere. This definition does not take into 
C  account the geomagnetic meridian of the subsolar point which causes
C  seasonal variations of the MLTMN in UT time. The latter approach is
C  perfectly applicable to the dipole or eccentric dipole magnetic 
C  coordinates but it fails with the CGM coordinates because there are 
C  forbidden areas near the geomagnetic equator where CGM coordinates 
C  cannot be calculated by definition [e.g., Gustafsson et al., JATP, 
C  54, 1609, 1992].

C  In this code the MLT midnight is defined as location of a given point
C  on (or above) the Earth's surface strictly behind the North (South) 
C  CGM pole in such the Sun, the pole, and the point are lined up.

C  This approach was originally proposed and coded by Boris Belov 
C  sometime in the beginning of 1980s; here it is slightly edited by 
C  Vladimir Papitashvili in February 1999.

C  Ignore points which nearly coincide with the geographic or CGM poles
C  within 0.01 degree in latitudes; this also takes care if SLA or CLA 
C  are dummy values (e.g., 999.99)
*/
    double sla = S[0], slo = S[1], cla = C[0], pla = P[0], plo = P[1];
    double RAD = Math.PI/180;
    if( (Math.abs(sla) >= 89.99) || (Math.abs(cla) >= 89.99) ) return 0;
    if( (pla*cla) < 0 ) throw new Exception("MLTUT: PLAT and CLAT not in the same hemisphere");
//  Solve the spherical triangle
    double qq = plo*RAD, qt = slo*RAD;
    double cff = (90-Math.abs(pla))*RAD;
    if( cff < 0.0000001 ) cff = 0.0000001;
    double cft = ((pla*sla > 0) ? 90-Math.abs(sla) : 90+Math.abs(sla)) * RAD;
    if( cft < 0.0000001 ) cft = 0.0000001;
    double a = Math.sin(cff) / Math.sin(cft);
    double y = a*Math.sin(qq) - Math.sin(qt);
    double x = Math.cos(qt) - a*Math.cos(qq);
    double ut = Math.atan2(y,x);
    if( ut < 0 ) ut += 2*Math.PI;
    double bp = Math.sin(cff)*Math.cos(qq+ut), bt = Math.sin(cft)*Math.cos(qt+ut);
    ut *= 12/Math.PI;
    if( bp >= bt ){
      if( ut < 12 ) ut += 12;
      if( ut > 12 ) ut -= 12;
    }
    return ut;
  }

/*
      SUBROUTINE MLTUT(SLA,SLO,CLA,PLA,PLO,UT)

      if(abs(sla).ge.89.99.or.abs(cla).ge.89.99) then
        UT = 99.99
        return
      endif

      TPI = 6.283185307
      RAD = 0.017453293
       sp = 1.
       ss = 1.
      if(sign(sp,pla).ne.sign(ss,cla)) then
        write(*,2) pla,cla 
   2    format(/
     +  'WARNING - The CGM pole PLA = ',f6.2,' and station CLAT = ',
     +  f6.2,' are not in the same hemisphere: MLTMN is incorrect!')
      endif

C  Solve the spherical triangle

         QQ = PLO*RAD
        CFF = 90. - abs(PLA)
        CFF = CFF*RAD
      IF(CFF.LT.0.0000001) CFF=0.0000001

      if(sign(sp,pla).eq.sign(ss,sla)) then
        CFT = 90. - abs(SLA)
                                       else
        CFT = 90. + abs(SLA)
      endif
      
          CFT = CFT*RAD
      IF(CFT.LT.0.0000001) CFT=0.0000001
      
        QT = SLO*RAD
         A = SIN(CFF)/SIN(CFT)
         Y = A*SIN(QQ) - SIN(QT)
         X = COS(QT) - A*COS(QQ)
        UT = ATAN2(Y,X)
      
        IF(UT.LT.0.) UT = UT + TPI
       QQU = QQ + UT
       QTU = QT + UT
        BP = SIN(CFF)*COS(QQU)
        BT = SIN(CFT)*COS(QTU)
        UT = UT/RAD
        UT = UT/15.
      IF(BP.LT.BT) GOTO 10
      
        IF(UT.LT.12.) UT = UT + 12.
      IF(UT.GT.12.) UT = UT - 12.
   
  10  CONTINUE 

      RETURN
      END
*/

  public static void main(String[] arg){
//    System.out.println("atan2(1,2)="+Math.atan2(1,2));  //=0.4636476 rad from X, x=2,y=1
    CGMC g2c = new CGMC("","g2c"), c2g = new CGMC("","c2g");
    try{
      g2c.loadYear(2000);
    }catch(IOException e){
      System.out.println(e.toString());
    }
    try{
      c2g.loadYear(2000);
    }catch(IOException e){
      System.out.println(e.toString());
    }
    try{
      double[] A = {65.23, 123.45};
      System.out.println("glat="+A[0]+"  glon="+A[1]);
      double[] C = g2c.transform(A);
      System.out.println("clat="+C[0]+"  clon="+C[1]);
      double[] B = c2g.transform(C);
      System.out.println("glat="+B[0]+"  glon="+B[1]);
      double[] NP = {90.0,360.0};
      double[] P = g2c.transform(NP);
      System.out.println("plat="+P[0]+"  plon="+P[1]);
      double mltut = MLTUT(A,C,P);
      System.out.println("MLTUT="+mltut);
    }catch(Exception e){
      System.out.println(e.toString());
    }
  }

}
