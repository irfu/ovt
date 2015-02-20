/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/model/bowshock/Bowshock99Model.java,v $
  Date:      $Date: 2006/03/21 12:19:32 $
  Version:   $Revision: 2.4 $


=========================================================================*/

/**
* This is the OVT model of BowShock according to Farris et al, GRL, v. 18, p.1821, 1991, 
* and Cairns et al., JGR, v.100, p.47,  1995.
*/
package ovt.model.bowshock;

import ovt.util.Vect;
import ovt.util.Ellipsoid;

import java.lang.Math;


public class Bowshock99Model {

   private static final double e = 0.8;
   private static final double e2 = e*e;
   private static final double oe2 = 1-e*e;
   private static final double soe2 = Math.sqrt(1-e*e);
   private static final int X = 0;
   private static final int Y = 1;
   private static final int Z = 2;

  /** [swp]=nPa, [teta]=radians, smn - solar mag. number, normal: smn=5.4
   * @param cosTheta cosine of Theta angle
   * @param swp Solar Wind Pressure [nPa]
   * @param machNumber Solar Mach Number
   * @return distance to bow shock
   */
   public static double getR(double cosTheta, double swp, double machNumber) {      
      return getK(swp, machNumber)/(1.0+e*cosTheta);
   }
   
   private static double getK(double swp, double machNumber) {
     return 19.0*Math.pow(1.8/swp,0.1666666666666)*(1.0+1.1*(machNumber*machNumber+3.0)/(4.0*machNumber*machNumber));
   }
   
   /** Returns the magnetic tangent line parameter x0,
    * for the magnetic tangent line in the form <code>x = (Bx/By)*r + x0</code>,
    * where <code>r = sqrt(y^2 + z^2)</code> 
    */
   public static double getMagTangentX0(double[] imf, double swp, double machNumber, double y, double z) {
   	double sinPhi = imf[Y]/Math.sqrt(imf[Y]*imf[Y]+imf[Z]*imf[Z]);
        double cosPhi = imf[Z]/Math.sqrt(imf[Y]*imf[Y]+imf[Z]*imf[Z]);
        double y2 = - z*sinPhi + y*cosPhi;
        double dxdz = imf[X]/Math.sqrt(imf[Y]*imf[Y] + imf[Z]*imf[Z]);
   	return getMagTangentX0(dxdz, swp, machNumber, y2);
   }
   
   /** y is in the frame where oz || component of imf which is perpendicular to ox */
   private static double getMagTangentX0(double dxdz, double swp, double machNumber, double y) {
   	double k = getK(swp, machNumber);
        double oe2 = 1 - e*e;
        return Math.sqrt((k*k/oe2-y*y)*(1+oe2*dxdz*dxdz)/oe2) - k*e/oe2;
   }
   
   /** Returns the DIST and DIFF coordinate in the foreshock on the basis of location gsm and solar wind params.*/
   public static double[] getDIST_DIFF(double[] gsm, double[] imf, double swp, double machNumber) {
        double[] p = toPrimed(gsm, imf);
        double[] p_tan = getTangentPoint(p[Y], imf, swp, machNumber);
        double sinAlpha	= imf[X] /Vect.absv(imf); // may be as positive as negative
        double cosAlpha	= Math.sqrt(1 - sinAlpha*sinAlpha); // always positive
        return new double[]{ (p[Z] - p_tan[Z])/cosAlpha, (p[Z] - p_tan[Z])*sinAlpha/cosAlpha + p_tan[X] - p[X] };
   }
   
   
   /** Returns coordinates of the magnetic tangent toching the bowshock in primed CS
     * (where oz || component of imf which is perpendicular to ox) 
     */
   
   private static double[] getTangentPoint(double y_prime, double[] imf, double swp, double machNumber) {     
   	double y2 = y_prime*y_prime;
   	double dxdz = imf[X]/Math.sqrt(imf[Y]*imf[Y] + imf[Z]*imf[Z]);   
   	double k = getK(swp, machNumber);
   	double z_tan = - soe2*dxdz*Math.sqrt( (k*k/oe2 - y2)/(1 + oe2*dxdz*dxdz)); // z-coord of tang. line touching the bow-shock
   	double x_tan = Math.sqrt( (k*k/oe2 - y2)/(1 + oe2*dxdz*dxdz))/soe2 - k*e/oe2; // x-coord of tang. line touching
   	return new double[]{x_tan, y_prime, z_tan};
   }
   
   /** Returns coordinates of the magnetic tangent toching the bowshock 
     */
   public static double[] getTangentPoint(double y_gsm, double z_gsm, double[] imf, double swp, double machNumber) {
   	return toGSM(getTangentPoint(toPrimed(new double[]{1., y_gsm, z_gsm}, imf)[1], imf, swp, machNumber), imf);
   }
   
   
   /** Returns the shortest distance from the point (gsm) to the bow shock. eMax is the maximum error. */
   public static double getDistanceToBowshock(double[] gsm, double[] imf, double swp, double machNumber, double eMax) {
   	double k = getK(swp, machNumber);
        double x_shift = k*e/oe2;
        Ellipsoid el = new Ellipsoid(k/soe2, 1-1/soe2);
        return el.getMinDistance(gsm[1], gsm[2], gsm[0] + x_shift, eMax);
   }
   
   /** Returns the distance from the point (gsm) to the BowShock along imf. */
   public static double getDistanceToBowshockAlongIMF(double[] gsm, double[] imf, double swp, double machNumber) {
   	double k = getK(swp, machNumber);
        double x_shift = k*e/oe2;
        Ellipsoid el = new Ellipsoid(k/soe2, 1-1/soe2);
        return el.getDistanceAlongVector(gsm[1], gsm[2], gsm[0] + x_shift, 
		new double[]{imf[1], imf[2], imf[0]});
   }
   
   
   /** Returns the coordinate of intersection of imf vector with origin in gsm
    * with the bow shock.
    *
    * Should return NaN if gsm is not connected to bow shock. Is not checked!!!!
    */
   public static double[] getIMFIntersectionPoint(double[] gsm, double[] imf, double swp, double machNumber) {
   	double d = getDistanceToBowshockAlongIMF(gsm, imf, swp, machNumber);
	return Vect.add(gsm, Vect.multiply(Vect.norm(imf), d));
   }
   
   /** Returns the angle (in radians) between the imf and a shock normal at the point of intersection of imf vector with origin in gsm
    * with the bow shock.
    */
   public static double getThetaIMF_N(double[] gsm, double[] imf, double swp, double machNumber) {
   	double[] inters = getIMFIntersectionPoint(gsm, imf, swp, machNumber);	
	double k = getK(swp, machNumber);        
        Ellipsoid el = new Ellipsoid(k/soe2, 1-1/soe2);
	double[] n = el.getNormal(inters[1], inters[2]);
	return Vect.angleOf2vect(imf, new double[]{n[2], n[0], n[1]});
   }
   
   
   /** Returns true if the point (gsm) is outside the BowShock */
   public static boolean isInSolarWind(double[] gsm, double swp, double machNumber) {
   	double r = Vect.absv(gsm);
   	double cosTheta = gsm[0]/r;
        double r_bowshock = getR(cosTheta, swp, machNumber);
   	return (r-r_bowshock) > -0.01;
   }
   
   /** Reurns coordinate in the primed coordinate system: 
     * where oz || component of imf which is perpendicular to ox 
     */
   private static double[] toPrimed(double[] gsm, double[] imf) {
        // rotate to oz || imf_yoz
   	double sinPhi = imf[Y]/Math.sqrt(imf[Y]*imf[Y]+imf[Z]*imf[Z]);
   	double cosPhi = imf[Z]/Math.sqrt(imf[Y]*imf[Y]+imf[Z]*imf[Z]);
   	double y2 = - gsm[Z]*sinPhi + gsm[Y]*cosPhi;
   	double z2 =   gsm[Z]*cosPhi + gsm[Y]*sinPhi;
        return new double[]{gsm[X], y2, z2};
   } 
   
   private static double[] toGSM(double[] primed, double[] imf) {
        // rotate to oz || imf_yoz
   	double sinPhi = imf[Y]/Math.sqrt(imf[Y]*imf[Y]+imf[Z]*imf[Z]);
   	double cosPhi = imf[Z]/Math.sqrt(imf[Y]*imf[Y]+imf[Z]*imf[Z]);
        double y = primed[Z]*sinPhi + primed[Y]*cosPhi;
        double z = primed[Z]*cosPhi - primed[Y]*sinPhi;
        return new double[]{primed[X], y, z};
   } 

/*   //rotating in GSE on 3.8 degs around Z axis
   public double[] rotatedBS(double swp,double[] inXYZ){
      double[] outXYZ=new double[3];
      double teta,r,vabs1;
      
      teta=Math.atan2(inXYZ[1],inXYZ[0]);
      r=getR(teta,swp);
      for(int i=0;i<3;++i)
         outXYZ[i]=inXYZ*r/;
      return outXYZ;
   }*/
}
