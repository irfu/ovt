/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/model/Tsyganenko87.java,v $
  Date:      $Date: 2001/06/21 14:17:42 $
  Version:   $Revision: 2.1 $

=========================================================================*/

package ovt.mag.model;

import ovt.mag.*;
import ovt.interfaces.*;

public class Tsyganenko87 extends AbstractMagModel {

  public Tsyganenko87(MagProps magProps) {
    super(magProps);
  }
  
  public double[] bv(double[] gsm, double mjd) {
    double[] b = new double[3];
    tsyganenko87JNI(gsm, magProps.getKPIndex(mjd), getSint(mjd), getCost(mjd), b);
    return b;
  }
  

protected static native void tsyganenko87JNI(double[] gsm, double kp, double sint, double cost, double[] bv);

}

