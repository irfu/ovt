/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/model/Tsyganenko2001.java,v $
  Date:      $Date: 2002/06/30 17:08:08 $
  Version:   $Revision: 1.1 $


=========================================================================*/
/*
 * Tsyganenko2001.java
 *
 * Created on June 29, 2002, 10:48 AM
 */

package ovt.mag.model;

import ovt.mag.*;
import ovt.util.*;
import ovt.interfaces.*;

/**
 *
 * @author  ko
 * @version 
 */
public class Tsyganenko2001 extends AbstractMagModel {

  protected static native void tsyganenko2001JNI(double[] gsm,double ps, 
    double pdyn, double dst, double byimf, double bzimf,
    double G1, double G2, double[] bv);

  public Tsyganenko2001(MagProps magProps) {
    super(magProps);
  }
  
  public double[] bv(double[] gsm, double mjd) {
    double[] bv = new double[3];
    double swp = magProps.getSWP(mjd);
    double[] imf = magProps.getIMF(mjd);
    double bYimf = imf[1];
    double bZimf = imf[2];
    double dst = magProps.getDSTIndex(mjd);
    
    Trans tr = magProps.getTrans(mjd);
    double tilt = tr.getDipoleTilt();
    double G1 = magProps.getG1(mjd);
    double G2 = magProps.getG2(mjd);
    // call gni to get bv
    tsyganenko2001JNI(gsm, tilt, swp, dst, bYimf, bZimf, G1, G2, bv); 
    return bv;
  }
}
