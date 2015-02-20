/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/Trace.java,v $
  Date:      $Date: 2003/09/28 17:52:44 $
  Version:   $Revision: 2.6 $


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
 * Trace.java
 *
 * Created on March 24, 2000, 1:26 PM
 */
 
package ovt.mag;

import ovt.*;
import ovt.mag.*;
import ovt.model.magnetopause.*;
import ovt.util.*;
import ovt.event.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;


/** 
 *
 * @author  root
 * @version 
 */
public class Trace extends Object {

/**  is 0 - automatic (optimal) step size */
public static final int  AUTO_STEP_SIZE = 0;
/**  is 1 - <CODE>step size = step * sqrt(r)</CODE> (enforced) */
public static final int  SQRT_STEP_SIZE = 1;
   
  // field-line tracing constants
public static final double  EPS = 0.001;
public static final double  HMIN = 0.001;
public static final double  FF = 1.01;
public static final int  MAXCOUNT = 300;
public static final double  STMIN = 0.01;

public static final double      STEPMAX = 1.0;

public static final int MXX = 200; //max number of points in tracelin
public static final int    NMAX = 3;
public static final int    IMAX = 11;
public static final int    NUSE = 7;
public static final double STEPMIN = 0.002;
public static final double RMAX = 100.0;

public static final int KPINDEX  = 0;
public static final int SINT     = 1;
public static final int COST     = 2;

public static final int DSTINDEX = 0;
public static final int IMF_Y    = 1;
public static final int TILT     = 2;
public static final int SWP      = 3;
public static final int IMF_Z    = 4;
public static final int G1       = 5;
public static final int G2       = 6;


  protected MagProps magProps;
  
  /** Creates new Trace */
  public Trace(MagProps magProps) {
    
  }
  
//------------------- field-line tracing  ---------------------------

protected static native void mdirectoryJNI(String Dir);

static {
    mdirectoryJNI(OVTCore.getMdataDir());
}



public static void lastline (MagProps magProps, double mjd, double rv[], double dir[], int idir, double alt) {
        lastline(magProps, mjd, rv, dir, MagProps.xlim, alt, idir, Const.ERR_MP);
}

// mjd time
// rv initial position in gsm
// alt altitude of footprint (if alt = 0 tracing is made to equatorial plane or xlim)
// step '+'- parrallel to b; '-' - antiparrallel to b
// is 0 - automatic (optimal) step size; 1 - step size = step * sqrt(r) (enforced)
// xlim maximum distance in the tail
// mx maximum number of points
// IM - internal(igrf or dipol) EM - external(T87,89,96) model 
// Factor - Model Factor
// isMPClip = 1 if Magn.Paus Clipping is performed else isMPClip = 0;
// datTs[] - data array for Tsyganenko's models 87,89,96 years
//         for T87,T89 models: KPIndex, Sint,Cost (t- tilt);
//         for T96 model:      SWP, DSTIndex,Imf_z,Imf_y,TILT
//         if isMPClip = 1 :   SWP,Imf_z for all models
protected static native void lastlineJNI(double mjd, double[] rv, double[] dir, double xlim,double alt, 
      int idir, double epst, int IM, int EM, double Factor, int isMPClip, double[] datTs);

/******************************************************************
 FUNCTION:
  first closed field line starting from rv(3) in the direcion dir(3) 
  mjd,model,rv(3)  =  standard variables
  dir(3):  search direction from rv(3) 
  alt (km) altitude of required footprint
  foot(3) :  footprint position (re)
    idir= 1 north 
        =-1 south
  epst = tolerance (re) for determination of the origin of last closed rv(3)
******************************************************************/
public static void lastline(MagProps magProps, double mjd, double rv[], double dir[], double xlim, 
                                double alt, int idir, double epst) {
  // Local variables 


  int ExternalModel = magProps.getExternalModelType();
  int InternalModel = magProps.getInternalModelType();
  double Factor = magProps.getModelFactor();
  boolean isMPClipping = magProps.isMPClipping();
  int isMPClip = (isMPClipping) ? 1 : 0 ;

// Arguments for Tsyg_xx() functions
  double[] dataTsxx = new double[7];
  double[] imf = null;

  // I don't really get this...
  if(isMPClipping || ExternalModel == magProps.T96 || ExternalModel == magProps.T2001){
    imf = magProps.getIMF(mjd);
    dataTsxx[IMF_Z] = imf[2];
    dataTsxx[SWP] = magProps.getSWP(mjd);
  }

  if (ExternalModel == magProps.T96 || ExternalModel == magProps.T2001) {
    dataTsxx[IMF_Y] = imf[1];                     
    dataTsxx[DSTINDEX] = magProps.getDSTIndex(mjd);
    Trans tr = magProps.getTrans(mjd);
    dataTsxx[TILT] = tr.getDipoleTilt();
    if (ExternalModel == magProps.T2001) {
        //  G1 and G2 
        dataTsxx[G1] = magProps.getG1(mjd);
        dataTsxx[G2] = magProps.getG2(mjd);
    }
  }
  else{     //ExternalModel == T87 or T89 Model
    dataTsxx[KPINDEX] = magProps.getKPIndex(mjd);
    dataTsxx[SINT] = magProps.getSint(mjd);
    dataTsxx[COST] = magProps.getCost(mjd);
  }
  
  lastlineJNI(mjd,rv,dir,xlim,alt,idir,epst,InternalModel,ExternalModel,Factor,isMPClip ,dataTsxx);
}




// was : returns -1 outside mag 0= not enough steps (mx) 1=ok  

public static Fieldline traceline(MagProps magProps, double mjd, double[] rv, double step, int is) {
        return traceline(magProps, mjd, rv, magProps.getAlt(), step, is, magProps.getXlim(), Const.NPF);
}

public static Fieldline traceline(MagProps magProps, double mjd, double[] rv, double step, int is, double alt) {
        return traceline(magProps, mjd, rv, magProps.getAlt(), step, is, magProps.getXlim(), Const.NPF);
}



/**
 * mjd time
 * rv initial position in gsm
 * alt altitude of footprint (if alt = 0 tracing is made to equatorial plane or xlim)
 * step '+'- parrallel to b; '-' - antiparrallel to b
 * is 0 - automatic (optimal) step size; 1 - step size = step * sqrt(r) (enforced)
 * xlim maximum distance in the tail
 * mx maximum number of points
 * xx,yy,zz - gsm ;  ss - length;   bx,by,bz  -  bv( gsm,mjd ) where gsm_i = {xx[i],yy[i],zz[i]);
 * IM - internal(igrf or dipol) EM - external(T87,89,96) model 
 * Factor - Model Factor
 * isMPClip = 1 if Magn.Paus Clipping is performed else isMPClip = 0;
 * n[0] - out: number points in field line
 * datTs[] - data array for Tsyganenko's models 87,89,96 years
 *         for T87,T89 models: KPIndex, Sint,Cost (t- tilt);
 *         for T96 model:      SWP, DSTIndex,Imf_z,Imf_y,TILT
 *         if isMPClip = 1 :   SWP,Imf_z for all models
 */

protected static native void tracelineJNI(double mjd, double[] rv, double alt, double step, int is, double xlim, int mx,
   double[] xx,double[] yy, double[] zz, double[] ss, double[] bx, double[] by,double[] bz,
   int IM, int EM, double Factor, int isMPClip, int[] n, double[] datTs);


/** returns fieldline
 * @param magModel Magnetic model to use for tracing
 * @param mjd time
 * @param rv initial position in gsm
 * @param alt altitude of footprint (if alt = 0 tracing is made to equatorial plane or xlim)
 * @param step <CODE>+</CODE> - parrallel to <B>b</B>
 * <CODE>-</CODE> - antiparrallel to <B>b</B>
 * @param is <CODE>0 - automatic </CODE>(optimal) step size
 * <CODE>1 - step size = step * sqrt(r)</CODE> (enforced)
 * @param xlim maximum distance in the tail
 * @param mx maximum number of points
 * @return Field Line
 */
public static Fieldline traceline(MagProps magProps, double mjd, double[] rv, 
                double alt, double step,int is, double xlim, int mx) {
  // Local variables 

  

  MagModel magModel = (MagModel)magProps;

  int ExternalModel = magProps.getExternalModelType();
  int InternalModel = magProps.getInternalModelType();
  double Factor = magProps.getModelFactor();
  boolean isMPClipping = magProps.isMPClipping();
  int isMPClip = (isMPClipping) ? 1 : 0 ;

// Arguments for Tsyg_xx() functions
  double[] dataTsxx = new double[7];
  double[] imf = null;

  if(isMPClipping || ExternalModel == magProps.T96 || ExternalModel == magProps.T2001){
    imf = magProps.getIMF(mjd);
    dataTsxx[IMF_Z] = imf[2];
    dataTsxx[SWP] = magProps.getSWP(mjd);
  }

  if (ExternalModel == magProps.T96 || ExternalModel == magProps.T2001) {
    dataTsxx[IMF_Y] = imf[1];                     
    dataTsxx[DSTINDEX] = magProps.getDSTIndex(mjd);
    Trans tr = magProps.getTrans(mjd);
    dataTsxx[TILT] = tr.getDipoleTilt();
    if (ExternalModel == magProps.T2001) {
        // STHIS ARE THE DUMMY G1 and G2 !!!
        dataTsxx[G1] = 6;
        dataTsxx[G2] = 10;
    }
  }
  else{     //ExternalModel == T87 or T89 Model
    dataTsxx[KPINDEX] = magProps.getKPIndex(mjd);
    dataTsxx[SINT] = magProps.getSint(mjd);
    dataTsxx[COST] = magProps.getCost(mjd);
  }

// out: n[0] - number points in field line (for converting into Fieldline)
  int[] n = new int[1];
// xx,yy,zz - gsm ;  ss - length;   bx,by,bz  -  bv( gsm,mjd )
  double[] xx = new double[MXX]; 
  double[] yy = new double[MXX]; 
  double[] zz = new double[MXX]; 
  double[] ss = new double[MXX]; 
  double[] bx = new double[MXX]; 
  double[] by = new double[MXX]; 
  double[] bz = new double[MXX]; 

  tracelineJNI(mjd,rv,alt,step,is,xlim,mx, xx,yy,zz,ss,bx,by,bz,InternalModel,ExternalModel,Factor,isMPClip,n,dataTsxx);

// get xx,yy,zz,ss,bx,by,bz .... and transform it into fieldline f_l
  // create a fieldline with predefined initial capacity
  Fieldline f_l = new Fieldline(mjd, n[0]);
  
  double[] r = new double[3];
  double[] b = new double[3];
  for(int i=0; i<n[0]; i++){
    r[0] = xx[i];  
    r[1] = yy[i];  
    r[2] = zz[i];  
    b[0] = bx[i];  
    b[1] = by[i];  
    b[2] = bz[i];  
    
    f_l.add(new MagPoint( (double[])r.clone(), (double[])b.clone(),  mjd), ss[i]);
  }
  return f_l;
}


/*
public MagPoint foot_ns(double[] rv, int idir) {
        return foot_ns(rv, MagProps.xlim, MagProps.alt, idir) ;
}*/

/** 
 * Returns magnetic footprint in the north (idir=1) or south (idir=-1) 
 * @param rv(3) initial position (re),
 *  alt = altitude (km), north (idir=1) or south (idir=-1)
 *  @return foot print (3) in (re) 
 */

public static MagPoint foot_ns(MagProps magProps, double mjd, double[] rv, double xlim, double alt, int idir) {
        
    // Function Body 

    if (Vect.absv(rv) < 1) {
        System.err.println("foot_ns: Input vector below the surf");
        System.exit (-1);
    }

    double step = (idir < 0) ? -0.1 : 0.1;

  int ExternalModel = magProps.getExternalModelType();
  int InternalModel = magProps.getInternalModelType();
  double Factor = magProps.getModelFactor();
  boolean isMPClipping = magProps.isMPClipping();
  int isMPClip = (isMPClipping) ? 1 : 0 ;

// Arguments for Tsyg_xx() functions
  double[] dataTsxx = new double[7];
  double[] imf = null;

  if(isMPClipping || ExternalModel == magProps.T96){
    imf = magProps.getIMF(mjd);
    dataTsxx[IMF_Z] = imf[2];
    dataTsxx[SWP] = magProps.getSWP(mjd);
  }

  if(ExternalModel == magProps.T96){
    dataTsxx[IMF_Y] = imf[1];                     
    dataTsxx[DSTINDEX] = magProps.getDSTIndex(mjd);
    Trans tr = magProps.getTrans(mjd);
    dataTsxx[TILT] = tr.getDipoleTilt(); 
  }
  else{     //ExternalModel == T87 or T89 Model
    dataTsxx[KPINDEX] = magProps.getKPIndex(mjd);
    dataTsxx[SINT] = magProps.getSint(mjd);
    dataTsxx[COST] = magProps.getCost(mjd);
  }

// out: n[0] - number points in field line
  int[] n = new int[1];
// xx,yy,zz - gsm ;  ss - length;   bx,by,bz  -  bv( gsm,mjd )
  double[] xx = new double[MXX]; 
  double[] yy = new double[MXX]; 
  double[] zz = new double[MXX]; 
  double[] ss = new double[MXX]; 
  double[] bx = new double[MXX]; 
  double[] by = new double[MXX]; 
  double[] bz = new double[MXX]; 


    tracelineJNI(mjd,rv,alt,step,0,xlim,MXX, xx,yy,zz,ss,bx,by,bz,InternalModel,ExternalModel,Factor,isMPClip,n,dataTsxx);

// get last field line point from xx,yy,zz,ss,bx,by,bz ... and transform it into MagPoint 

    if (n[0] > 0){ 
        double[] r = new double[3];
        double[] b = new double[3];
        r[0] = xx[ n[0]-1 ];  
        r[1] = yy[ n[0]-1 ];  
        r[2] = zz[ n[0]-1 ];  
        b[0] = bx[ n[0]-1 ];  
        b[1] = by[ n[0]-1 ];  
        b[2] = bz[ n[0]-1 ];  
        return new MagPoint(r,b,mjd);
    }
    else {
      double[] gsm = new double[]{ 999.0, 999.0, 999 }; 
      double[] bv = new double[]{ 0, 0, 0 }; 
      System.err.println("foot_ns: wrong traceline");
      return new MagPoint(gsm, bv, mjd);
    }
}

}



//-----------------------------------------------------------------------




/* right hand side for field line tracing with bsstep */

interface RightHand {

// WHY DO WE NEED double s ?????

public int rhand(double s, double r[], double drds[]);

}




class rhanda implements RightHand {
  protected MagModel magModel;
  protected double mjd;
rhanda(MagModel magModel, double mjd) { 
  this.magModel = magModel;
  this.mjd = mjd;
}
public int rhand(double s, double r[], double drds[]) {
    double  r2, rv[] = new double[3];
    int i, error = 0;

    /* right hand side for field line tracing with bsstep */

    for (i=0; i<3; i++) rv[i] = r[i];
    
    //double bv[] = magModel.igrf(rv); !!!!!!
    double bv[] = magModel.bv(rv, mjd);
    double b = Vect.absv(bv);
    for (i=0; i<3; i++) drds[i] = bv[i] / b;
    return error;
}

}
        

/*  contribution of the ring current to EXTERNAL field (nt) */
/*  tsyganenko long                                         */

class rhandb implements RightHand {
  protected MagModel magModel;
  protected double mjd;
rhandb(MagModel magModel, double mjd) { 
  this.magModel = magModel;
  this.mjd = mjd;
}
public int rhand(double s, double r[], double drds[]) {
    double  r2, rv[] = new double[3];
    int i, error=0;
    for (i=0; i<3; i++) rv[i] = r[i];

    double bv[] = magModel.bv(rv, mjd);
    double b = Vect.absv(bv);
    for (i=0; i<3; i++) drds[i] = bv[i] / b;

    /* return error code if outside the magnetosphere */

        // before magnetopause
    if ((rv[0] > 13) || (rv[0] < -52)) error = 1;

    r2 = rv[1] * rv[1] + rv[2] * rv[2];
    if (r2 > Trace.RMAX * Trace.RMAX) error = 1;
    return error;
}

}

// output[0] = x;
//               [1] = hnext
//               [2] = error;


class BsstepOutput {

        public double x=0;
        public double hnext = 0;
        public int error=0;

BsstepOutput(double ex, double hanext, int err) {
        x = ex;
        hnext = hanext;
        error = err;
}
}

