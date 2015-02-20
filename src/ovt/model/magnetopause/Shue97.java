/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/model/magnetopause/Shue97.java,v $
  Date:      $Date: 2005/12/13 16:34:16 $
  Version:   $Revision: 2.2 $


=========================================================================*/

// Created 5 Apr 2000 11:44 UTC by kono
package ovt.model.magnetopause;

import ovt.mag.*;
import ovt.util.*;
import ovt.interfaces.*;

import ovt.util.Utils;

import java.lang.Math;

public class Shue97  {

  public static double getR(double cosTheta, double mjd, MagProps magProps) {
    return getR(cosTheta, magProps.getSWP(mjd), magProps.getIMF(mjd)[2]);
  }
  
  
   //[bz]=nT, [swp]=nPa, [teta]=radians
   public static double getR(double cosTheta, double swp, double bz){
      final double cc=-1.0/6.6;
      double r, r0, alfa;
      
      alfa=(0.58-0.007*bz)*(1.0+0.024*Math.log(swp));
      r0=(10.22+1.29*Utils.tanh(0.184*(bz+8.14)))*Math.pow(swp,cc);
      r = r0 * Math.pow(2.0/(1.0+cosTheta),alfa);
      return r;
   }
  
  /** Returns a VERY ROUGH estimate of a distance from the point (gsm) to the magnetopause. */
  public static double distance_to_magnetopause(double[] gsm, double swp, double bz) {
    double r = Vect.absv(gsm);
    double cosTheta = gsm[0]/r;
    double r_mpause = getR(cosTheta, swp, bz);
    return r-r_mpause;
  }
  
}
