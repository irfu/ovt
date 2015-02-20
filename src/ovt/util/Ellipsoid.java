/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/util/Ellipsoid.java,v $
Date:      $Date: 2006/03/21 12:19:32 $
Version:   $Revision: 1.2 $


=========================================================================*/

package ovt.util;

/**
* This class is the implementation of the coordinate transformation from cartesian
* coordinate system to ellipsoidic and vice-versa, used to calculate the distance from the point
* to the ellipsoid.
*
* Ellispoid is defined as <code>(r/ae)^2 + (z/(1-f)ae)^2 = 1</code>
*
* The elipsoidic coordinate system is given by <code>lambda</code>, <code>phi</code>, and <code>h</code>,
* where <code>lambda</code> is the angle in the XY plane, <code>phi</code> is the polar angle,
* and <code>h</code> is the distance from the elipsoid surface along the surface normal.
*
* The original class was written by L. MAISONOBE (luc@spaceroots.org) and taken from
* http://www.spaceroots.org/documents/distance
*
*/

public class Ellipsoid {

  private static final double ot = 1.0 / 3.0;

  private final double ae;
  private final double f;
  private final double e2;
  private final double g;
  private final double g2;
  private final double e2ae;
  private final double ae2;



  /** 
   * Create ellispoid <code>(r/ae)^2 + (z/(1-f)ae)^2 = 1 with the main axis along z.</code>
   */
  public Ellipsoid (double ae, double f) {
    this.ae = ae;
    this.f  = f;
    e2      = f * (2.0 - f);
    g       = 1.0 - f;
    g2      = g * g;
    e2ae    = e2 * ae;
    ae2     = ae * ae;
  }

  /** 
   * @param eMax - maximum error
   */
  public double getMinDistance(double x, double y, double z, double eMax) {
    return toEllipsoidic(new Cartesian(x, y, z), eMax).h;
  }
  
  public Cartesian toCartesian (Ellipsoidic ell) {
    double cphi = Math.cos (ell.phi);
    double sphi = Math.sin (ell.phi);
    double n    = ae / Math.sqrt (1.0 - e2 * sphi * sphi);
    double r    = (n + ell.h) * cphi;
    return new Cartesian (r * Math.cos (ell.lambda),
                          r * Math.sin (ell.lambda),
                          (g2 * n + ell.h) * sphi);
  }

  public Ellipsoidic toEllipsoidic(Cartesian cart, double eMax) {
    //throws EllipsoidError {

    // compute some miscellaneous variables outside of the loop
    double z2         = cart.z * cart.z;
    double r2         = cart.x * cart.x + cart.y * cart.y;
    double r          = Math.sqrt(r2);
    double g2r2ma2    = g2 * (r2 - ae2);
    double g2r2ma2mz2 = g2r2ma2 - z2;
    double g2r2ma2pz2 = g2r2ma2 + z2;
    double dist       = Math.sqrt(r2 +z2);
    double threshold  = Math.max(1.0e-14 * dist, eMax);
    boolean inside    = (g2r2ma2pz2 <= 0);

    // point at the center
    if (dist < (1.0e-10 * ae)) {
      return new Ellipsoidic(0.0, 0.5 * Math.PI, -ae * Math.sqrt(1.0 - e2));
    }

    double cz = r / dist;
    double sz = cart.z /dist;
    double t  = cart.z / (dist + r);

    // distance to the ellipse along the current line
    // as the smallest root of a 2nd degree polynom :
    // a k^2 - 2 b k + c = 0
    double a  = 1.0 - e2 * cz * cz;
    double b  = g2 * r * cz + cart.z * sz;
    double c  = g2r2ma2pz2;
    double b2 = b * b;
    double ac = a * c;
    double k   = c / (b + Math.sqrt(b2 - ac));
    double phi = Math.atan2(cart.z - k * sz, g2 * (r - k * cz));

    // point on the ellipse
    if (Math.abs(k) < (1.0e-10 * dist)) {
      return new Ellipsoidic(Math.atan2(cart.y, cart.x), phi, k);
    }
 
    for (int iterations = 0; iterations < 100; ++iterations) {

      // 4th degree normalized polynom describing
      // circle/ellipse intersections
      // tau^4 + b tau^3 + c tau^2 + d tau + e = 0
      // (there is no need to compute e here)
      a        = g2r2ma2pz2 + g2 * (2.0 * r + k) * k;
      b        = -4.0 * k * cart.z / a;
      c        = 2.0 * (g2r2ma2pz2 + (1.0 + e2) * k * k) / a;
      double d = b;

      // reduce the polynom to degree 3 by removing
      // the already known real root
      // tau^3 + b tau^2 + c tau + d = 0
      b += t;
      c += t * b;
      d += t * c;

      // find the other real root
      b2       = b * b;
      double Q = (3.0 * c - b2) / 9.0;
      double R = (b * (9.0 * c - 2.0 * b2) - 27.0 * d) / 54.0;
      double D = Q * Q * Q + R * R;
      double tildeT, tildePhi;
      if (D >= 0) {
        double rootD = Math.sqrt(D);
        double rMr = R - rootD;
        double rPr = R + rootD;
        tildeT = ((rPr > 0) ?  Math.pow(rPr, ot) : -Math.pow(-rPr, ot))
               + ((rMr > 0) ?  Math.pow(rMr, ot) : -Math.pow(-rMr, ot))
               - b * ot;
        double tildeT2   = tildeT * tildeT;
        double tildeT2P1 = 1.0 + tildeT2;
        tildePhi= Math.atan2(cart.z * tildeT2P1 - 2 * k * tildeT,
                             g2 * (r * tildeT2P1 - k * (1.0 - tildeT2)));
      } else {
        Q = -Q;
        double qRoot     = Math.sqrt(Q);
        double theta     = Math.acos(R / (Q * qRoot));
        tildeT           = 2.0 * qRoot * Math.cos(theta * ot) - b * ot;
        double tildeT2   = tildeT * tildeT;
        double tildeT2P1 = 1.0 + tildeT2;
        tildePhi         = Math.atan2(cart.z * tildeT2P1 - 2 * k * tildeT,
                                      g2 * (r * tildeT2P1 - k * (1.0 - tildeT2)));
        if ((tildePhi * phi) < 0) {
          tildeT    = 2.0 * qRoot * Math.cos((theta + 2.0 * Math.PI) * ot) - b * ot;
          tildeT2   = tildeT * tildeT;
          tildeT2P1 = 1.0 + tildeT2;
          tildePhi  = Math.atan2(cart.z * tildeT2P1 - 2 * k * tildeT,
                                 g2 * (r * tildeT2P1 - k * (1.0 - tildeT2)));
          if (tildePhi * phi < 0) {
            tildeT    = 2.0 * qRoot * Math.cos((theta + 4.0 * Math.PI) * ot) - b * ot;
            tildeT2   = tildeT * tildeT;
            tildeT2P1 = 1.0 + tildeT2;
            tildePhi  = Math.atan2(cart.z * tildeT2P1 - 2 * k * tildeT,
                                   g2 * (r * tildeT2P1 - k * (1.0 - tildeT2)));
           }
        }
      }

     // midpoint on the ellipse
      double dPhi  = Math.abs(0.5 * (tildePhi - phi));
      phi  = 0.5 * (phi + tildePhi);
      double cPhi  = Math.cos(phi);
      double sPhi  = Math.sin(phi);
      double coeff = Math.sqrt(1.0 - e2 * sPhi * sPhi);

      // debug
      /* System.out.println(iterations + ": phi = " + Math.toDegrees(phi)
                         + " +/- " + Math.toDegrees(dPhi)
                         + ", k = " + k); */

      if (dPhi < 1.0e-14) {
        return new Ellipsoidic(Math.atan2(cart.y, cart.x),
                               phi,
                               r * cPhi + cart.z * sPhi - ae * coeff);
      }
      
      b = ae / coeff;
      double dR = r - cPhi * b;
      double dZ = cart.z - sPhi * b * g2;
      k = Math.sqrt(dR * dR + dZ * dZ);
      if (inside) {
        k = -k;
      }
      t = dZ / (k + dR);

    }

    throw new IllegalArgumentException("unable to converge in"
                             + " ovt.util.Ellipsoid.toEllipsoidic (Cartesian, eMax)");

  }
  
  /**
   * Reurns min. distance from the point (x,y,z) to the ellipse along the vector vec.
   * 
   * Returns NaN if the line does not intersect with the ellipse.
   * 
   * @param 
   */
  public double getDistanceAlongVector(double x, double y, double z, double[] vec) {
    double[] dir = Vect.norm(vec);
    
    // coefficients of the equations a*d^2 + 2bd + c = 0
    double a = (dir[0]*dir[0] + dir[1]*dir[1] + dir[2]*dir[2]/g2);
    double b = (dir[0]*x + dir[1]*y + dir[2]*z/g2);
    double c = (x*x + y*y + z*z/g2 - ae2);
    
    double descr = b*b - a*c;
    
    if (descr < 0) return Double.NaN; // no intersection
    
    double d1 = (-b + Math.sqrt(descr)) / a;
    double d2 = (-b - Math.sqrt(descr)) / a;
    
    return (Math.abs(d1) > Math.abs(d2)) ? d2 : d1;
  }


  /**
   * Reurns outer normal for positive z.
   * 
   */
  public double[] getNormal(double x, double y) {
    double z = Math.sqrt(ae2 - x*x - y*y)*g;   
    
    //System.out.println("x="+z+" y="+x+" z="+y);
    
    double dzdy = -1*g2*y/z;
    double dzdx = -1*g2*x/z;
    
    return Vect.norm(new double[]{-1*dzdx, -1*dzdy, 1});
  }



  public static void main (String[] args) {
    if (args.length != 5) {
      System.err.println ("usage: java Ellipsoid ae f x y z");
      System.exit (1);
    }

    Ellipsoid e = new Ellipsoid(Double.parseDouble(args[0]),
                                Double.parseDouble(args[1]));
    Cartesian cart1 = new Cartesian(Double.parseDouble(args[2]),
                                    Double.parseDouble(args[3]),
                                    Double.parseDouble(args[4]));

    //try {
      Ellipsoidic ell = e.toEllipsoidic (cart1, 0.0);
      System.out.println("lambda = " + Math.toDegrees(ell.lambda)
                         + ", phi = "+ Math.toDegrees(ell.phi)
                         + ", h = " + ell.h);
      Cartesian cart2 = e.toCartesian (ell);
      double err = Math.sqrt ((cart1.x - cart2.x) * (cart1.x - cart2.x)
                              + (cart1.y - cart2.y) * (cart1.y - cart2.y)
                              + (cart1.z - cart2.z) * (cart1.z - cart2.z));
      System.out.println ("error on reconstructed cartesian coordinates : "
                          + err);
    /*}     catch (EllipsoidError ee) {
      ee.printStackTrace();
      System.exit (1);
    }*/

    System.exit (0);

  }

}

class Cartesian {
  public final double x;
  public final double y;
  public final double z;
  public Cartesian (double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
}

class Ellipsoidic {
  public final double lambda;
  public final double phi;
  public final double h;
  public Ellipsoidic (double lambda, double phi, double h) {
    this.lambda = lambda;
    this.phi    = phi;
    this.h      = h;
  }
}
