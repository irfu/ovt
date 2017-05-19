/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/util/Trans.java,v $
Date:      $Date: 2003/09/28 17:52:57 $
Version:   $Revision: 2.9 $


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

/**
 * Trans.java
 * 
 * Supplies functions for among other things coordinate transformations.
 * One possible source/reference for coordinate transformations is
 * "Space Physics Coordinate Transformations: A User Guide", M. A. Hapgood,
 * Planet. Space Sci., Vol. 40, No. 5. pp. 711-717, 1992
 *
 * Created on March 24, 2000, 1:18 PM
 */

package ovt.util;

import ovt.object.*;
import ovt.util.*;
import ovt.mag.model.*;
import ovt.mag.*;
import ovt.Const;
import ovt.datatype.*;

/**
 * This class holds all transformation stuff.
 * @author  root
 * @version
 */
public class Trans {

  /** Degrees in 1 radian */
  public static final double RAD = 57.295779513;
  /** equals to 0 */
  public static final int MAGNETIC_DIPOLE  = 0;
  /** equals to 1 */
  public static final int ECCENTRIC_DIPOLE = 1; 

  // Excentric dipole coordinates derived from IGRF model
  protected double[] Eccrr;
  protected double[] Eccdx;
  protected double[] Eccdy;
  protected double[] Eccdz;

  /**  Transformat matrix geo to gsm
   * @see #setGSM(double)
   */
  //protected double Ggsm[]  = { 1, 0, 0, 0, 1, 0, 0, 0, 1};

  /** Transformation matrix from sm to gsm */
  protected Matrix3x3 sm_gsm;

  /** Transformation matrix from geo to gsm */
  protected Matrix3x3 geo_gsm;

  /** Transformation matrix from geo to gei */
  protected Matrix3x3 geo_gei;

  /** Transformation matrix from gei to gsm */
  protected Matrix3x3 gei_gsm;

  /** Transformation matrix from gei to gse */
  protected Matrix3x3 gei_gse;

  /** Sine of dipole tilt. By default dipole tilt set to zero
   * @see #getSint()
   */
  protected double sint = 0;
  /** Cosine of dipole tilt. By default dipole tilt set to zero
   * @see #getCost()
   */
  protected double cost = 1;

  protected double mjd;

  protected IgrfModel igrfModel;

  /** Creates new Trans */
  public Trans(double mjd, IgrfModel igrf) {
    igrfModel = igrf;
    // set excentric dipole coordinated
    Eccrr = igrf.getEccrr(mjd);
    Eccdx = igrf.getEccdx(mjd);
    Eccdy = igrf.getEccdy(mjd);
    Eccdz = igrf.getEccdz(mjd);

    sint = getDipoleTiltSine(mjd, Eccdz);
    cost = Math.sqrt(1 - sint * sint);
    sm_gsm  = sm_gsm_trans_matrix(mjd, Eccdz);
    geo_gsm = geo_gsm_trans_matrix(mjd, Eccdz);
    geo_gei = geo_gei_trans_matrix(mjd);
    gei_gsm = gei_gsm_trans_matrix(mjd, Eccdz);
    gei_gse = gei_gse_trans_matrix(mjd);

    this.mjd = mjd;
  }


  /** Returns MagPacks mjd
   * @see #setMjd(double)
   */
  public double getMjd() {
    return mjd;
  }

  /** Returns the sine of the dipole tilt angle
   * @see #getCost() #getDipoleTilt()
   */
  public double getSint()
{ return sint; }

  /** Returns the cosine of the dipole tilt angle
   * @see #getSint() #getDipoleTilt()
   */
  public double getCost()
{ return cost; }

  /**  Returns dipole tilt angle in RADIANS
   * @see #getSint() #getCost()
   */
  public double  getDipoleTilt()
{ return Math.asin(getSint()); }



  public static double getDipoleTiltSine(double mjd, double[] Eccdz) {
    double sunv[] = Utils.sunmjd(mjd);
    double[][] temp = new double[3][];
    temp[0] = gei2geo(sunv, mjd);
    // set sine and cosine of dipole tilt angle
    double sint = Vect.dot(temp[0], Eccdz);
    return sint;
  }

  public double[] gei2geo(double gei[]) {
    return gei_geo_trans_matrix().multiply(gei);
  }

  public static double[] gei2geo(double gei[], double mjd) {
    return gei_geo_trans_matrix(mjd).multiply(gei);
  }

  public double[] geo2gei(double geo[]) {
    return geo_gei_trans_matrix().multiply(geo);
  }

  public double[] geo2gsm(double geo[]) {
    return geo_gsm_trans_matrix().multiply(geo);
  }

  public double[] gsm2geo(double gsm[]) {
    return gsm_geo_trans_matrix().multiply(gsm);
  }

  public double[] gsm2sm(double gsm[]) {
    return gsm_sm_trans_matrix().multiply(gsm);
  }

  /* ------------------------------------------------------
  FUNCTION:
  transforms gei to geo when ic=+1
  or  geo to gei when ic=-1
  input:
  mjd - modified julian day
  geo or gei
  output:
  gei or geo


  // Old style, shoud be protected!
  public static void gei_geo(double gei[], double geo[], double mjd, int ic) {



  double  theta, ct, st;

  //  changed SEP 94  theta = fmod(mjd, 1.0) * TPI + gmstime(mjd);

  theta = Time.getGSMTime(mjd);

  st = Math.sin(theta);
  ct = Math.cos(theta);

  if (ic > 0) {
  geo[0] = ct * gei[0] + st * gei[1];
  geo[1] = -st * gei[0] + ct * gei[1];
  geo[2] = gei[2];
  } else {
  gei[0] = ct * geo[0] - st * geo[1];
  gei[1] = st * geo[0] + ct * geo[1];
  gei[2] = geo[2];
  }
  }


   */

  public Matrix3x3 sm_trans_matrix(int toCS) {
    switch (toCS) {
      case CoordinateSystem.SM  : return new Matrix3x3();
      case CoordinateSystem.GEO : return sm_geo_trans_matrix();
      case CoordinateSystem.GEI : return sm_gei_trans_matrix();
      case CoordinateSystem.GSM : return sm_gsm_trans_matrix();
      case CoordinateSystem.GSE : return sm_gse_trans_matrix();
    }
    throw new IllegalArgumentException("Wrong CS '"+toCS+"'");
  }


  public Matrix3x3 geo_trans_matrix(int toCS) {
    switch (toCS) {
      case CoordinateSystem.GEO : return new Matrix3x3();
      case CoordinateSystem.SM  : return geo_sm_trans_matrix();
      case CoordinateSystem.GEI : return geo_gei_trans_matrix();
      case CoordinateSystem.GSM : return geo_gsm_trans_matrix();
      case CoordinateSystem.GSE : return geo_gse_trans_matrix();
    }
    throw new IllegalArgumentException("Wrong CS '"+toCS+"'");
  }

  public Matrix3x3 gsm_trans_matrix(int toCS) {
    switch (toCS) {
      case CoordinateSystem.GSM : return new Matrix3x3();
      case CoordinateSystem.SM  : return gsm_sm_trans_matrix();
      case CoordinateSystem.GEO : return gsm_geo_trans_matrix();
      case CoordinateSystem.GEI : return gsm_gei_trans_matrix();
      case CoordinateSystem.GSE : return gsm_gse_trans_matrix();
    }
    throw new IllegalArgumentException("Wrong CS '"+toCS+"'");
  }

  public Matrix3x3 gse_trans_matrix(int toCS) {
    switch (toCS) {
      case CoordinateSystem.GSM : return gse_gsm_trans_matrix();
      case CoordinateSystem.SM  : return gse_sm_trans_matrix();
      case CoordinateSystem.GEO : return gse_geo_trans_matrix();
      case CoordinateSystem.GEI : return gse_gei_trans_matrix();
      case CoordinateSystem.GSE : return new Matrix3x3();
    }
    throw new IllegalArgumentException("Wrong CS '"+toCS+"'");
  }

  public Matrix3x3 trans_matrix(int fromCS, int toCS) {
    if (fromCS == toCS) return new Matrix3x3();
    switch (fromCS) {
      case CoordinateSystem.GEO : return geo_trans_matrix(toCS);
      case CoordinateSystem.SM  : return sm_trans_matrix(toCS);
      case CoordinateSystem.GSM : return gsm_trans_matrix(toCS);
      case CoordinateSystem.GSE : return gse_trans_matrix(toCS);
    }
    throw new IllegalArgumentException("Wrong fromCS '"+fromCS+"'");
  }

  //  GEO  ->

  public Matrix3x3 geo_gsm_trans_matrix() {
    return geo_gsm;
  }

  /** */
  public Matrix3x3 geo_gei_trans_matrix() {
    return geo_gei;
  }

  /** */
  public Matrix3x3 geo_gse_trans_matrix() {
    /*Matrix3x3 gei_gsm3x3 = gei_gsm_trans_matrix();
    Matrix3x3 geo_gei3x3 = geo_gei_trans_matrix();
    Matrix3x3 res = gei_gsm3x3.multiply(geo_gei3x3);
    System.out.println("gei_gsm\n" + gei_gsm3x3.toString());
    System.out.println("geo_gei\n" + geo_gei3x3.toString());
    System.out.println("result\n" + res.toString());
     */
    return gei_gse_trans_matrix().multiply(geo_gei_trans_matrix());
  }
  
  public Matrix3x3 gse_geo_trans_matrix() {
      return geo_gse_trans_matrix().getInverse();
  }

  //  SM  ->

  /** */
  public Matrix3x3 sm_gsm_trans_matrix() {
    return sm_gsm;
  }

  /** */
  public Matrix3x3 sm_gse_trans_matrix() {
    return gsm_gse_trans_matrix().multiply(sm_gsm_trans_matrix());
  }
  
  public Matrix3x3 gse_sm_trans_matrix() {
    return sm_gse_trans_matrix().getInverse();
  }

  /** */
  public Matrix3x3 sm_geo_trans_matrix() {
    return gsm_geo_trans_matrix().multiply(sm_gsm_trans_matrix());
  }

  /** */
  public Matrix3x3 sm_gei_trans_matrix() {
    return gsm_gei_trans_matrix().multiply(sm_gsm_trans_matrix());
  }

  //  GEI  ->

  /** */
  public Matrix3x3 gei_sm_trans_matrix() {
    return sm_gei_trans_matrix().getInverse();
  }

  /** */
  public Matrix3x3 gei_geo_trans_matrix() {
    return geo_gei_trans_matrix().getInverse();
  }

  public static Matrix3x3 gei_geo_trans_matrix(double mjd) {
    return geo_gei_trans_matrix(mjd).getInverse();
  }

  /** */
  public Matrix3x3 gei_gsm_trans_matrix() {
    return gei_gsm;
  }

  /** */
  public Matrix3x3 gei_gse_trans_matrix() {
    return gei_gse;
  }

  /** */
  public Matrix3x3 gse_gei_trans_matrix() {
    return gei_gse_trans_matrix().getInverse();
  }
  
  //  GSM  ->

  /** */
  public Matrix3x3 gsm_sm_trans_matrix() {
    return sm_gsm_trans_matrix().getInverse();
  }

  /** */
  public Matrix3x3 gsm_geo_trans_matrix() {
    return geo_gsm_trans_matrix().getInverse();
  }

  /** */
  public Matrix3x3 gsm_gei_trans_matrix() {
    return gei_gsm_trans_matrix().getInverse();
  }

  /** */
  public Matrix3x3 gsm_gse_trans_matrix() {
    return gei_gse_trans_matrix().multiply(gsm_gei_trans_matrix());
  }

  /** */
  public Matrix3x3 gse_gsm_trans_matrix() {
    return gsm_gse_trans_matrix().getInverse();
  }
  
  //    GEO ->

  /** */
  public Matrix3x3 geo_sm_trans_matrix() {
    return sm_geo_trans_matrix().getInverse();
  }

  //   ------     GEO  ->  GEI    ------

  public static Matrix3x3 geo_gei_trans_matrix(double mjd) {
    double[][] m = new double[3][3];
    double  theta, ct, st;
    //  changed SEP 94 theta = fmod(mjd, 1.0) * TPI + gmstime(mjd);
    theta = Time.getGSMTime(mjd);
    st = Math.sin(theta);
    ct = Math.cos(theta);
    //System.out.println("ct="+ct+" st="+st);

    int i, j;
    for (i=0; i<3; i++) {
        for (j=0; j<3; j++) {
            m[i][j] = 0;
        }
    }

    m[0][0] = ct;  m[0][1] = -st;
    m[1][0] = st;  m[1][1] = ct;
    m[2][2] = 1;
    return new Matrix3x3(m);
  }


  //   ------     GEO  ->  GSM    ------

  /** @param mjd time
   *  @param Eccdz excentric dipole coordinates???
   */
  protected static Matrix3x3 geo_gsm_trans_matrix(double mjd, double[] Eccdz) {
    final double sunv[] = Utils.sunmjd(mjd);
    final double[][] temp = new double[3][];
    temp[0] = gei2geo(sunv, mjd);
    temp[1] = Vect.crossn(Eccdz, temp[0]);
    temp[2] = Vect.crossn(temp[0], temp[1]);
    Matrix3x3 m = new Matrix3x3(temp);
    return m;
  }

  // -------- GEI  ->  GSE  ---------

  public static Matrix3x3 gei_gse_trans_matrix(double mjd) {
    final double[][] gei_gse = new double[3][];
    final double eqlipt[] = {   0.0, -0.398, 0.917 };
    gei_gse[0] = Utils.sunmjd(mjd);                    // NOTE: Time dependent.
    gei_gse[1] = Vect.crossn(eqlipt, gei_gse[0]);
    gei_gse[2] = Vect.crossn(gei_gse[0], gei_gse[1]);
    final Matrix3x3 m = new Matrix3x3(gei_gse);
    return m;
  }

  public double[] gei2gse(double[] gei) {
    return gei_gse.multiply(gei);
  }

  public static Matrix3x3 gei_gsm_trans_matrix(double mjd, double[] Eccdz) {
    double[][] geigsm = new double[3][];
    double[] sunv = Utils.sunmjd(mjd);
    // find  dipole axis in GEI  changed SEP 94
    double theta =  Time.getGSMTime(mjd);
    double st = Math.sin(theta);
    double ct = Math.cos(theta);
    // dipole vector in GEI
    double[] dipgei = new double[3];
    dipgei[0] = ct * Eccdz[0] - st * Eccdz[1];
    dipgei[1] = st * Eccdz[0] + ct * Eccdz[1];
    dipgei[2] = Eccdz[2];
    geigsm[1] = Vect.crossn(dipgei, sunv);
    geigsm[2] = Vect.crossn(sunv, geigsm[1]);
    geigsm[0] = Vect.crossn(geigsm[1], geigsm[2]);
    Matrix3x3 m = new Matrix3x3(geigsm);
    return m;
  }

  // -------- GEI  ->  SM  ---------

  /*
  This is the old convension SM coordinates transformation.
  we don't need it any more

  public static Matrix3x3 gei_sm_trans_matrix(double mjd, double[] Eccdz) {
  double[][] geism = new double[3][];
  double[] sunv = Utils.sunmjd(mjd);
  // find  dipole axis in GEI  changed SEP 94
  double theta =  Time.getGSMTime(mjd);
  double st = Math.sin(theta);
  double ct = Math.cos(theta);
  // dipole vector in GEI
  double[] dipgei = new double[3];
  dipgei[0] = ct * Eccdz[0] - st * Eccdz[1];
  dipgei[1] = st * Eccdz[0] + ct * Eccdz[1];
  dipgei[2] = Eccdz[2];
  geism[1] = Vect.crossn(dipgei, sunv);
  geism[0] = Vect.crossn(geism[1], dipgei);
  geism[2] = Vect.crossn(geism[0], geism[1]);
  Matrix3x3 m = new Matrix3x3(geism);
  return m;
  }*/

  //   ------     GEI  ->  GSEQ   ------

  public static Matrix3x3 gei_gseq_trans_matrix(double mjd) {
    double[][] geigseq = new double[3][];
  double[]   rotsun = { 0.122, -0.424, 0.899 };
    geigseq[0] = Utils.sunmjd(mjd);
    geigseq[1] = Vect.crossn(rotsun, geigseq[0]);
    geigseq[2] = Vect.crossn(geigseq[0], geigseq[1]);
    Matrix3x3 m = new Matrix3x3(geigseq);
    return m;
  }



  // -------- SM  ->  GSM  ---------

  /** @param mjd time
   */
  protected static Matrix3x3 sm_gsm_trans_matrix(double mjd, double[] Eccdz) {
    Matrix3x3 m = new Matrix3x3();

    /*gsm[0] = sm[0] * Cost + sm[2] * Sint;
    gsm[1] = sm[1];
    gsm[2] = -sm[0] * Sint + sm[2] * Cost;*/

    double st =  getDipoleTiltSine(mjd, Eccdz);
    double ct = Math.sqrt(1 - st*st);

    m.set(0, 0, ct);  m.set(0, 2, st);
    m.set(2, 0, -st); m.set(2, 2, ct);

    return m;
  }
  /**
   * transform geo(3) to gma(3) 
   * flag=0 magnetic dipole (MAGNETIC_DIPOLE)
   *     =1 eccentric dipole (ECCENTRIC_DIPOLE)
   */
  public double[] geo2gma(double geo[], int flag) {
    double gma[] = new double[3];
    geo_gma(flag, geo, gma, 1);
    return gma;
  }

    /**
   * transform gma(3) to geo(3) 
   * flag=0 magnetic dipole MAGNETIC_DIPOLE)
   *     =1 eccentric dipole (ECCENTRIC_DIPOLE)
   */
  public double[] gma2geo(double gma[], int flag) {
    double geo[] = new double[3];
    geo_gma(flag, geo, gma, -1);
    return geo;
  }

  /**
   * transform geo(3) to gma(3) if idir =1 and vice versa if idir=-1
   * flag=0 magnetic dipole MAGNETIC_DIPOLE)
   *     =1 eccentric dipole (ECCENTRIC_DIPOLE)
   */
  public void geo_gma(int flag, double[] geo, double[] gma, int idir) {

    double  tmp[] = new double[3];
    double  ff;

    // Function Body
    ff = (flag == 1) ? 1.0 : 0.0;

    if (idir > 0) {
      for (int i=0; i<3; i++)
      tmp[i] = geo[i] - Eccrr[i];
      gma[0] = Vect.dot(Eccdx, tmp);
      gma[1] = Vect.dot(Eccdy, tmp);
      gma[2] = Vect.dot(Eccdz, tmp);
    } else {
      geo[0] = Eccdx[0] * gma[0] + Eccdy[0] * gma[1] + Eccdz[0] * gma[2] + Eccrr[0] * ff;
      geo[1] = Eccdx[1] * gma[0] + Eccdy[1] * gma[1] + Eccdz[1] * gma[2] + Eccrr[1] * ff;
      geo[2] = Eccdx[2] * gma[0] + Eccdy[2] * gma[1] + Eccdz[2] * gma[2] + Eccrr[2] * ff;
    }
  }

  /************************************************************************
  compute corrected magnetic coordinates at the reference altitude alt
  input:
  mjd:     modified julian day (use fdate() to find mjd! )
  geo(3):  geographic position (cartesian) in units of re=6371.2 km
  alt:     reference altitude (km)
  output:
  mlat:    corrected magnetic latitude (deg)
  mlong:   corrected magnetic longitude (deg)
  mlt:     corrected magnetic local time (hours)
  ell:     L value (equatorial distance of the field line igrf)
   *************************************************************************/
  public static double[] corrgma(MagProps magProps, double mjdx,double[] geo, double alt){
  double[] res = {0,0,0}; //OUTPUT: mlat, mlong, mlt
    double  gmst, r, mlat, mlong, mlt, ell;
    double[]  ft, sv, foot;
    double  sig;
    double[]  spos;
    double  cos2;
    Trans trans=magProps.getTrans(mjdx);

    ft=trans.geo2gma(geo,MAGNETIC_DIPOLE);
    sig = (ft[2]<0.0) ? -1.0 : 1.0;
    sv=trans.igrfModel.bvGEO(geo,mjdx);
    r = Vect.dot(geo, sv) * ft[2];
    if (r > 0.) {
      /*mlat = 0.;
      mlong = 0.;
      mlt = 0.;
      ell = 1.;*/
      res[0]=res[1]=res[2]=0.0;
      return res;
    }


    boolean isIGRF = true;
    int currentExternalModel = magProps.getExternalModelType();
    if(magProps.getInternalModelType()!=MagProps.IGRF) {
        magProps.setExternalModelType(MagProps.NOMODEL);
        isIGRF = false;
    }

    Log.err("ovt.util.Tans.corrgma is strange!!! It changes models!");
    
    double[] gsmx=trans.geo2gsm(geo);
    Fieldline fieldLine =
      Trace.traceline(magProps,mjdx,gsmx,0.0,10.0,0,magProps.getXlim(),Const.NPF);
    MagPoint magPoint=fieldLine.lastPoint();
    foot=trans.gsm2geo(magPoint.gsm);
    ft=trans.geo2gma(foot, MAGNETIC_DIPOLE);
    r = Vect.absv(ft);

    if (isIGRF==false) magProps.setExternalModelType(currentExternalModel);
      

    cos2 = (ft[0] * ft[0] + ft[1] * ft[1]) / (r * r);

    if (cos2 < 4.8e-7)
    cos2 = 4.8e-7;

    // --------->  cos(89.96)^2=4.8d-7

    ell = r / cos2;

    spos=Utils.sunmjd(mjdx);

    Matrix3x3 mtrx=trans.gei_geo_trans_matrix(); //GEI -> GEO matrix
    sv = mtrx.multiply(spos);
    spos=trans.geo2gma(sv,MAGNETIC_DIPOLE);

    r = Math.sqrt((alt / ovt.Const.RE + 1.0) / ell);

    if (r>1.0) r = 1.0;

    /*mlat = acos(r) * 180. /  PI * sig;
    mlong = atan2(ft[1], ft[0]) * 180. / PI;
    mlt = (atan2(ft[1], ft[0]) - atan2(spos[1], spos[0])) * 12. / PI + 36.;
    mlt = fmod(*mlt, 24.0);*/

    res[0] = Math.acos(r) * sig;
    res[1] = Math.atan2(ft[1], ft[0]);
    res[2] = Math.atan2(ft[1], ft[0]) - Math.atan2(spos[1], spos[0])+Math.PI;
    return res;
  }


  /*
  protected void setGsmAndDipoleTilt(double mjd) {
  if (Math.abs(mjd - getMjd()) < 1e-4) return;

  int i;
  double  gmst;
  double  tilt;

  //System.out.println("Year = "+Time.getYear(mjd));

  if (Time.getYear(mjd) > 1981) {
  double sunv[] = Utils.sunmjd(mjd);
  double[][] temp = new double[3][];
  temp[0] = gei2geo(sunv, mjd);
  // set sine and cosine of dipole tilt angle
  sint = Vect.dot(temp[0], Eccdz);
  cost = Math.sqrt(1 - sint * sint);

  temp[1] = Vect.crossn(Eccdz, temp[0]);
  temp[2] = Vect.crossn(temp[0], temp[1]);

  // set geo_gsm transformation matrix

  geo_gsm.set(temp);

  for (i=0; i<3; i++) Ggsm[i]   = temp[0][i];
  for (i=0; i<3; i++) Ggsm[i+3] = temp[1][i];
  for (i=0; i<3; i++) Ggsm[i+6] = temp[2][i];

  } else {
  System.out.println("setGSM: dipole tilt set to zero\n");
  sint = 0.;
  cost = 1.;

  for (i=0; i<9; i++)
  Ggsm[i] = 0;
  for (i=0; i<3; i++)
  Eccrr[i] = 0;

  Ggsm[0] = 1.;
  Ggsm[4] = 1.;
  Ggsm[8] = 1.;
  }
  }*/

  /** Returns [0] - Magnetic Latitude, 
   * [1] - Magnetic Local Time
   */
  public static double[] xyz2MlatMlt(double[] xyz) {
    int X = 0; int Y = 1; int Z = 2;
    double mlat, mlt;
    double r = Vect.absv(xyz);
    // theta is a angle between XOY and R
    double sint = xyz[2]/r;
    double cost = Math.sqrt(1-sint*sint);

    double theta = Math.asin(sint);
    
    mlat = Utils.toDegrees(theta);
    // 12h : X, 18h : Y , 24(0)h : -X, 6h : -Y
    mlt = 12 + 12*Math.atan2(xyz[Y], xyz[X])/Math.PI;
    if (Math.abs(mlt) == 24) mlt = 0;
  return new double[]{mlat, mlt};
  }

  /** Minimum altitude is 0. */
  public static double[] mlat_mlt2xyz(double mlat, double mlt, double alt_RE) {
    return Utils.sph2rec(1.+alt_RE, mlat, 15.*(mlt-12.)); 
  }

  

  /** Returns [0] - Geographic Latitude, [1] - Geographic Longitude in degrees */
  public static double[] xyz2LatLon(double[] xyz) {
    int X = 0; int Y = 1; int Z = 2;
    double irad = 180./Math.PI;
    double  phi, delta;
    double radius = Math.sqrt (xyz[0] * xyz[0] + xyz[1] * xyz[1] + xyz[2] * xyz[2]);
    
    if ((xyz[Y] == 0.) && (xyz[X] == 0.))
        phi = 0;
    else
        phi = irad * Math.atan2 (xyz[Y], xyz[X]);
        
        
    if (phi < 0.) phi = phi + 360.;
    
    double arg = xyz[Z] / radius;
        
    if (arg < 1.) {
        delta = irad * Math.asin (arg);
    } else {
        delta = 90.;
    }
    
    return new double[]{ delta, phi };
  }

  /** Minimum altitude is 0. */
  public static double[] lat_lon2xyz(double lat, double lon, double alt_RE) {
    return Utils.sph2rec(1.+alt_RE, lat, lon); 
  }
  
  public static void main(String[] args){
    double[] r = { -1, -1, 0};
    for (double mlt=0;mlt<=24;mlt+=1.) {
        double[] xyz = Trans.mlat_mlt2xyz(0, mlt, 0.);
        double[] res = Trans.xyz2MlatMlt(xyz);
        System.out.println(""+(int)(mlt)+" -> "+res[0]+"deg, "+res[1]+"h");    
    }
  }


}
