/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/util/Vect.java,v $
  Date:      $Date: 2006/03/21 12:20:27 $
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

package ovt.util;

import java.util.*;

public class Vect {

  public static double[] add(double[] vector1, double[] vector2) {
    double[] res = new double[vector1.length];
    for (int i=0; i<vector1.length; i++)
      res[i] = vector1[i] + vector2[i];
    return res;
  }
  
  //added by kono
  /** Result = vector1 - vector2 */
  public static double[] sub(double[] vector1, double[] vector2) {
    double[] res = new double[vector1.length];
    for (int i=0; i<vector1.length; i++)
      res[i] = vector1[i] - vector2[i];
    return res;
  }
  
  /** Returns the angle between two vectors in the range of 0 to pi.
   * Returns pi/2 if any of vectors has a zero size.
   */
  public static double angleOf2vect(double[] v1, double[] v2){
     double abs1x2=absv(v1)*absv(v2);
     if (abs1x2 == 0.0) return 0.5*Math.PI;
     //double[] v3=cross(v1,v2);
     //double dotprd=dot(v1,v2); //v3 = v1 x v2
     //return Math.atan2(absv(v3),dotprd/abs1x2);
     return Math.acos(dot(v1, v2)/abs1x2);
  }
 
  
  /** Returns the angle between two vectors v1 and v2 in the range of -pi to pi.
   * The rotationAxis is used to determine if the angle
   * is negative or positive. It is not precise, it just indicates the positive angle
   * rotation direction.
   * Returns pi/2 if any of vectors has a zero size.
   * @param v1 first vector [3]
   * @param v2 second vector [3]
   * @param rotationAxis the rotational vector omega [3]
   * @return the angle between two vectors v1 and v2 in the range of -pi to pi.
   */
  public static double angleOf2vect(double[] v1, double[] v2, 
                               double[] rotationAxis){
     double abs1x2=absv(v1)*absv(v2);
     if(abs1x2==0.0)
        return 0.5*Math.PI;
     // factorize v2 to parallel and perpendicular components to v1
     
     double[] e_par  = norm(v1);
     double v2_par_magnitude  = dot(v2,e_par);
     
     double[] e3 = crossn(v1,v2);
     
     double[] e_perp = crossn(e3,e_par);
     
     double v2_perp_magnitude = dot(v2,e_perp);
     
     // project vasis vector e3 to rotation Axis (dot(.,.)), if projection < 0
     // it means that basis vector e3 was wrongly directed (it should be reverted)
     // one could revert it before or revert the result. We revert the result.
     if (dot(e3,rotationAxis) < 0) v2_perp_magnitude = -1*v2_perp_magnitude;
     
     /*System.out.println("par="+v2_par_magnitude+" perp="+v2_perp_magnitude +
        " abs="+Math.sqrt(v2_par_magnitude*v2_par_magnitude +
        v2_perp_magnitude*v2_perp_magnitude));*/
          
     return Math.atan2( v2_perp_magnitude, v2_par_magnitude );
  }

  
  public static double cosAngle(double[] v1, double[] v2){
    double v3_2 = absv2(sub(v1, v2));
    double v1_2 = absv2(v1);
    double v2_2 = absv2(v2);
    return (v1_2 + v2_2 - v3_2)/(2. * Math.sqrt(v1_2) * Math.sqrt(v2_2));
  }


  
  public static double[] multiply(double[] vector, double v) {
    double[] res = new double[vector.length];
    for (int i=0; i<vector.length; i++)
      res[i] = vector[i] * v;
    return res;
  }
  
public static double absv(double vec[]) {

	double sum = 0;

    for (int i=0; i<3; i++)
        sum += vec[i]*vec[i];
		
    return Math.sqrt(sum);
}

public static double absv2(double vec[]) {

	double sum = 0;

    for (int i=0; i<3; i++)
        sum += vec[i]*vec[i];
		
    return sum;
}


/** change the length of vector vec(3) to norm */
public static void normf(double vec[], double norm) {
    double s;

    /* Function Body */

    s = absv(vec);

    /*for (d1 = vec; d1 < vec + 3; ++d1)
        *d1 *= norm / s;*/
	for (int i=0; i<3; i++)
		vec[i] *= norm / s;
}

/** Get the vector normalized by norm */
public static double[] norm(double vec[], double norm) {
    double res[] = new double[3];
    for (int i=0; i<3; i++) res[i] = vec[i];
    normf(res, norm);
    return res;
}


/** Get the vector normalized by 1 */
public static double[] norm(double vec[]) {
    double res[] = new double[3];
    for (int i=0; i<3; i++) res[i] = vec[i];
    normf(res, 1.);
    return res;
}


/* scalar product of two vectors */

public static double  dot(double a[], double b[]) {

    double  ret_val = 0.0;

	for(int i=0; i<3; i++)
		ret_val += a[i] * b[i]; 

    return (ret_val);
}

  /** vector product c =  a x b */
  public static double[] cross(double a[], double b[]) {
    double c[] = new double[3];
    c[0] = a[1] * b[2] - a[2] * b[1];
    c[1] = a[2] * b[0] - a[0] * b[2];
    c[2] = a[0] * b[1] - a[1] * b[0];
    return c;
  }


/** vector product c =  a x b 
 * @depricated since 0.0001
 */
public static void cross(double a[], double b[], double c[]) {
	
	c[0] = a[1] * b[2] - a[2] * b[1];
    c[1] = a[2] * b[0] - a[0] * b[2];
    c[2] = a[0] * b[1] - a[1] * b[0];
	
}


/** normalized vector product */
public static double[] crossn(double a[], double b[]) {
  double c[] = new double[3];
  double x;
  cross(a, b, c);
  x = absv(c);

    if (x >= 1e-30)
  for (int i=0; i<3; i++) 
	c[i] = c[i] / x;
    return c;
}


/** normalized vector product */
public static void crossn(double a[], double b[], double c[]) {

    double x;

    cross(a, b, c);
    x = absv(c);

    if (x >= 1e-30)
		for (int i=0; i<3; i++) 
			c[i] = c[i] / x;

  }

/** Returns true if vectors are equal */
public static boolean equal(double[] vector1, double[] vector2) {
    for (int i=0; i<3; i++)
        if (vector1[i] != vector2[i]) return false;
    return true;
}

/** Returns true if vectors are equal within the eps tolorance */
public static boolean equal(double[] vector1, double[] vector2, double eps) {
    if ((Math.abs(vector1[0] - vector2[0]) < eps) && 
        (Math.abs(vector1[1] - vector2[1]) < eps) && 
        (Math.abs(vector1[2] - vector2[2]) < eps)) return true;
    else return false;
}



  /** Returns the array of Vector's elements. Used for JAK1.1 compatibility.
   * JDK 1.1 has no Vector.toArray() method.
   */
  public static Object[] toArray(Vector vect) {
    if (vect.size() == 0) return null;
    Object[] res = new Object[vect.size()];
    int i=0;
    Enumeration e = vect.elements();
    while(e.hasMoreElements()) {
      //System.out.println(i + "\t[" + vect.size() + "]");
      res[i] = e.nextElement();
      i++;
    }
    return res;
  }

    /** Returns the array of Vector's elements. Used for JAK1.1 compatibility.
   * JDK 1.1 has no Vector.toArray() method.
   */
  public static Object[] toObjectArray(double[] vect) {
    if (vect.length == 0) return null;
    Object[] res = new Object[vect.length];
    for (int i=0; i< vect.length; i++)
        res[i] = new Double(vect[i]);
    return res;
  }


public static String toString(double[] vect) {
    String res="";
    for (int i=0; i<vect.length; i++) res += vect[i] + " ";
    return res;
}

/** @return <CODE>true</CODE> if vect contains value */
public static boolean contains(int[] vect, int value) {
    for (int i=0; i<vect.length; i++)
        if (vect[i] == value) return true;
    return false;
}

  public static void main(String[] args){
    
    double[] x = new double[]{ 1, 0, 0};
    double[] y = new double[]{ 2, 2, 0};
    System.out.println("angleof2vect="+angleOf2vect(x,y)/Math.PI*180.);
  }

}
