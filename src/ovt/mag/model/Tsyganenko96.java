/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/model/Tsyganenko96.java,v $
  Date:      $Date: 2002/04/15 16:13:35 $
  Version:   $Revision: 2.3 $


=========================================================================*/

package ovt.mag.model;

import ovt.mag.*;
import ovt.util.*;
import ovt.interfaces.*;

/* SUBROUTINE T96_01 (IOPT,PARMOD,PS,X,Y,Z,BX,BY,BZ)
C
c     RELEASE DATE OF THIS VERSION:   JUNE 22, 1996.*/

public class Tsyganenko96 extends AbstractMagModel {

  protected static native void tsyganenko96JNI(double[] gsm,double ps, double pdyn,double dst,double byimf,double bzimf,double[] bv);

  public Tsyganenko96(MagProps magProps) {
    super(magProps);
  }
  
  public double[] bv(double[] gsm, double mjd){
    double[] b = new double[3];
    double swp = magProps.getSWP(mjd);
    double[] imf = magProps.getIMF(mjd);
    double bYimf = imf[1];
    double bZimf = imf[2];
    double dst = magProps.getDSTIndex(mjd);
    
    Trans tr = magProps.getTrans(mjd);
    double tilt=tr.getDipoleTilt();
    tsyganenko96JNI(gsm,tilt,swp,dst,bYimf,bZimf,b); // ????
    return b;
  }
  

//  protected static native void tsyganenko96JNI(double[] gsm, double kp, double sint, double cost, double[] bv);

  
  // for native methods
//  static {
//    System.loadLibrary("ovt2g");
//  }

}

