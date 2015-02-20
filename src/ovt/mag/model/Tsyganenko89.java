/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/model/Tsyganenko89.java,v $
  Date:      $Date: 2002/04/15 16:13:35 $
  Version:   $Revision: 2.2 $


=========================================================================*/

package ovt.mag.model;

import ovt.mag.*;
import ovt.interfaces.*;

public class Tsyganenko89 extends AbstractMagModel {

  public Tsyganenko89(MagProps magProps) {
    super(magProps);
  }
  
  public double[] bv(double[] gsm, double mjd) {
    double[] b = new double[3];
    tsyganenko89JNI(gsm, magProps.getKPIndex(mjd), getSint(mjd), getCost(mjd), b);
    return b;
  }
  

  protected static native void tsyganenko89JNI(double[] gsm, double kp, double sint, double cost, double[] bv);

  // for native methods
//  static {
//    System.loadLibrary("ovt2g");
//  }

}

