/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/model/DipoleModel.java,v $
  Date:      $Date: 2001/06/21 14:17:41 $
  Version:   $Revision: 2.1 $


=========================================================================*/

/*
 * Dipole.java
 *
 * Created on den 24 mars 2000, 00:19
 */
package ovt.mag.model;

import ovt.mag.*;

/** 
 *
 * @author  Mykola Khotyaintsev
 * @version 
 */
public class DipoleModel extends AbstractMagModel {

  public DipoleModel(MagProps magProps) {
    super(magProps);
  }
  
  public double[] bv(double[] gsm, double mjd){
    return dipol(gsm, getSint(mjd), getCost(mjd));  
  }


/* ------------------------------------------------------------ 
   FUNCTION: 
      compute dipole field (nt) in gsm coordinates 
   input: 
      sint, cost: sine and cosine of the tilt angle 
      gsm(3)      position vector (earth radii, re) 
   output:
      bv(3)     : field vector (nanotesla) 
   ------------------------------------------------------------ 
   dipmom = magnetic moment of the earth for igrf1985 model */


protected static double[] dipol(double gsm[], double sint, double cost) {
    
    double  b, x, y, z, r2, bx, by, bz;
    double bv[] = new double[3];
	
    /* Function Body */

    x = gsm[0] * cost - gsm[2] * sint;
    y = gsm[1];
    z = gsm[0] * sint + gsm[2] * cost;

    r2 = x * x + y * y + z * z;
    b = MagProps.DIPMOM / r2 / r2 / Math.sqrt(r2);

    bx = 3.0 * x  * z * b;
    by = 3.0 * y  * z * b;
    bz = (3.0 * z * z - r2) * b;

    bv[0] = bx * cost + bz * sint;
    bv[1] = by;
    bv[2] = -bx * sint + bz * cost;

    return bv;
}


}
