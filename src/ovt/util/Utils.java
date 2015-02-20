/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/util/Utils.java,v $
  Date:      $Date: 2006/03/21 12:21:15 $
  Version:   $Revision: 2.9 $
 
 
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
 * Utils.java
 *
 * Created on March 23, 2000, 2:11 PM
 */

package ovt.util;

import java.util.*;
import java.io.*;

import ovt.*;
import ovt.datatype.*;

/**
 *
 * @author  root
 * @version
 */
public class Utils extends Object {
    
  /** Creates new Utils */
    public Utils() {
    }
    
    public static float[] getRGB(java.awt.Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        //System.out.println(r+"\t"+g+"\t"+b);
        float[] rgb = new float[]{ (float)(r/255.), (float)(g/255.), (float)(b/255.)};
        return rgb;
    }
    
  /** Returns the array 0, 1, 2, 3, ...., objs.length */
    public static int[] getIndexes(Object[] objs) {
        int[] res = new int[objs.length];
        for (int i=0; i<objs.length; i++)
            res[i] = i;
        return res;
    }
    
    public static int[] getHashCodes(Object[] objs) {
        int[] res = new int[objs.length];
        for (int i=0; i<objs.length; i++)
            res[i] = objs[i].hashCode();
        return res;
    }
    
    public static Object[] getObjects(String[] objs) {
        Object[] res = new Object[objs.length];
        for (int i=0; i<objs.length; i++)
            res[i] = objs[i];
        return res;
    }
    
   /**
    * @returns the angle in degrees between the vernal equinox (X axis) and the
    * Greewich meridian. All numbers from pgs B6-B7 of 1984 Alamanc
    */
    public static double gha(Julian jday) {
        double  interval, t, jday0, gmst, gha_;
        int   ah, am, as;
        
        t = (jday.integer - Julian.J2000) / 36525;
        // Julian centuries since 2000.0
        
        gmst = (
        24110.54841 +             /* Greenwich mean sidereal time */
        8640184.812866 * t +      /* at midnight of this day = 0h UT */
        0.093104 * t * t -        /* coeff. are for seconds of time */
        6.2e-6 * t * t * t)
        / 3600.;                    /* 3600 sec. -> hour */
        
        
        interval =                    /* siderial hours since midnight */
        1.0027379093 * 24.* (jday.fraction);
        
  /*debug_flag = 0;
  if (debug_flag > 0) {
    printf (" gha day = %10.2lf  %15.5lf\n",
        jday -> integer, jday -> fraction);
   
    deg2hms (gmst * 15., &ah, &am, &as);
    printf (" gmst = %02d:%02d:%02d\n", ah, am, as);
    deg2hms (interval * 15., &ah, &am, &as);
    printf (" interval = %02d:%02d:%02d\n", ah, am, as);
  }*/
        
        gmst += interval;      //       add in interval since midnight
        
        gha_ = Utils.fmod360(gmst * 15.);                // hrs => degrees, make modulo 360
        // (15 deg/hr)
        return gha_;
    }
    
    
/** Calculates unit sun vector (GEI) for modified julian day (mjd) */
    public static double [] sunmjd (double mjd) {
        double sunv[] = sun_vect(new Julian(mjd));
        Vect.normf(sunv,1.0e0);
        return sunv;
    }
    
/** vector from earth to sun */
    public static double[] sun_vect(Julian mjd) {
        return sun_vectJNI(mjd.integer, mjd.fraction);
    }
    
    
    
    protected static native double[] earthJNI (double integer, double fraction);
    
    protected static native double[] sun_vectJNI (double integer, double fraction);
    
    
  /**-------------------------------------------------------------*/
    public static double  fmod360 (double x) {
        x = Math.IEEEremainder(x, 360.);
        if (x < 0.)
            x += 360.;
        return x;
    }
    
    public static double VABS(double[] v) {
        return Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
    }
    
    public static String cuttedString(double v, int n_of_digits) {
        
        int p = (int)Math.pow(10, n_of_digits);
        v = Math.round(v*p);
        
        //System.out.println("p="+p+"  v="+v);
        
        v = v/p;
        
        if (v == Math.round(v)) {
            
            return Integer.toString((int)v);
            
        } else {
            
            return Double.toString(v);
            
        }
        
    }
   /**
     * Returns the signum function of the argument; 
     * zero if the argument is zero, 1.0 if the argument is greater than zero, 
     * -1.0 if the argument is less than zero.
     */     
    public static double sign(double a){
      if (a > 0) return 1;
      else if (a<0) return -1;
      else return 0;    
    }
    
    public static double  new_sign(double a, double b){
        double  c;
        double  ret_val;
        c = Math.abs(a);
        ret_val = ( b < 0.0) ? -c : c;
        return ret_val;
    }
    
  /** Convert Radians to Degrees */
    public static double toDegrees(double angrad) {
        double res = angrad*Const.R_TO_D;
        //System.out.println(res + " "+ Math.toDegrees(angrad));
        return res;
    }
    
    //Computing hyper function cosh
    public static double cosh(double x){
        return 0.5*(Math.exp(x)+Math.exp(-x));
    }
    
    //Computing hyper function sinh
    public static double sinh(double x){
        return 0.5*(Math.exp(x)-Math.exp(-x));
    }
    
    //Computing hyper function tanh
    public static double tanh(double x){
        return sinh(x)/cosh(x);
    }
    
    //******* following code added by kono ********
    public static void eigens(double[] A,double[] RR, double[] E,int N){
        int IND, L, LL, LM, M, MM, MQ, I, J, K, IA, LQ, IQ, IM, IL, NLI, NMI;
        double ANORM, ANORMX, AIA, THR, ALM, QI, ALL, AMM, X, Y, SINX, SINX2,
        COSX, COSX2, SINCS, AIL, AIM, RLI, RMI, Q, V;
        final double RANGE = 1.0e-10; /*3.0517578e-5;*/
        
        for( J=0; J<N*N; J++ )
            RR[J] = 0.0;
        MM = 0;
        for( J=0; J<N; J++ ){
            RR[MM + J] = 1.0;
            MM += N;
        }
        ANORM=0.0;
        for( I=0; I<N; I++ ){
            for( J=0; J<N; J++ ){
                if( I != J ){
                    IA = I + (J*J+J)/2;
                    AIA = A[IA];
                    ANORM += AIA * AIA;
                }
            }
        }
        if( ANORM > 0.0 ){
            ANORM = Math.sqrt( ANORM + ANORM );
            ANORMX = ANORM * RANGE / N;
            THR = ANORM;
            while( THR > ANORMX ){
                THR=THR/N;
                do{
                    IND = 0;
                    for( L=0; L<N-1; L++ ){
                        for( M=L+1; M<N; M++ ){
                            MQ=(M*M+M)/2;
                            LM=L+MQ;
                            ALM=A[LM];
                            if( Math.abs(ALM) < THR )
                                continue;
                            IND=1;
                            LQ=(L*L+L)/2;
                            LL=L+LQ;
                            MM=M+MQ;
                            ALL=A[LL];
                            AMM=A[MM];
                            X=(ALL-AMM)/2.0;
                            Y=-ALM/Math.sqrt(ALM*ALM+X*X);
                            if(X < 0.0)
                                Y=-Y;
                            SINX = Y / Math.sqrt( 2.0 * (1.0 + Math.sqrt( 1.0-Y*Y)) );
                            SINX2=SINX*SINX;
                            COSX=Math.sqrt(1.0-SINX2);
                            COSX2=COSX*COSX;
                            SINCS=SINX*COSX;
                            //	   ROTATE L AND M COLUMNS
                            for( I=0; I<N; I++ ){
                                IQ=(I*I+I)/2;
                                if( (I != M) && (I != L) ){
                                    if(I > M) IM=M+IQ;
                                    else IM=I+MQ;
                                    if(I >= L) IL=L+IQ;
                                    else IL=I+LQ;
                                    AIL=A[IL];
                                    AIM=A[IM];
                                    X=AIL*COSX-AIM*SINX;
                                    A[IM]=AIL*SINX+AIM*COSX;
                                    A[IL]=X;
                                }
                                NLI = N*L + I;
                                NMI = N*M + I;
                                RLI = RR[ NLI ];
                                RMI = RR[ NMI ];
                                RR[NLI]=RLI*COSX-RMI*SINX;
                                RR[NMI]=RLI*SINX+RMI*COSX;
                            }
                            X=2.0*ALM*SINCS;
                            A[LL]=ALL*COSX2+AMM*SINX2-X;
                            A[MM]=ALL*SINX2+AMM*COSX2+X;
                            A[LM]=(ALL-AMM)*SINCS+ALM*(COSX2-SINX2);
                        } /* for M=L+1 to N-1 */
                    } /* for L=0 to N-2 */
                }
                while( IND != 0 );
            } /* while THR > ANORMX */
        }
/* Extract eigenvalues from the reduced matrix */
        L=0;
        for( J=1; J<=N; J++ ){
            L=L+J;
            E[J-1]=A[L-1];
        }
    }
    
    //******* following code added by kono ********
   /** Calculates axes of ellipsoide
    * @param N - number of points
    * @param Data Nx3 array of coordinates
    */
    public static double[] getEllipsoide(int N, double[][] Data){
        double[] R=new double [3*(3+1)/2];
        double[] res=new double[3], mean=new double[3], V=new double[9];
        int j,k,b,a;
        for(j=0;j<3;++j){
            mean[j]=0.0;
            for(a=0;a<N;++a)
                mean[j]+=Data[a][j];
            mean[j]/=N;
        }
        for(j=0;j<3;++j)
            for(k=0;k<=j;++k){
                R[j*(j+1)/2+k]=0.0;
                for(a=0;a<N;++a)
                    R[j*(j+1)/2+k]+=Data[a][j]*Data[a][k];
                R[j*(j+1)/2+k]=R[j*(j+1)/2+k]/N-mean[j]*mean[k];
            }
        eigens(R,V,res,3);
        for(j=0;j<3;++j)
            res[j]=2.0*Math.sqrt(res[j]); // getting axeses
        for(j=0;j<3;++j)
            for(int i=j;i<3;++i)
                if(res[j]>res[i]){
                    double xtmp=res[j];
                    res[j]=res[i];
                    res[i]=xtmp;
                }
        return res;
    }
    
    //added by kono
  /** Returns max diff.
   * @param 4x3 array
   * @return vector of max differences in coordinates {xi,yi,zi}
   */
    public static double[] maxDifffer(double[][] pos){
        double[] min = new double[3]; // min position (minx, miny, minz)
        double[] max = new double[3]; // max position (maxx, maxy, maxz)
        int i,j;
        
        for (j=0; j<3; j++) {
            min[j] = pos[0][j]; // take position of 1-static sat as minimum.
            max[j] = pos[0][j]; // take position of 1-static sat as max.
            for (i=1; i<4; i++) {
                max[j] = Math.max(pos[i][j], max[j]);
                min[j] = Math.min(pos[i][j], min[j]);
            }
        }
        // d holds maxx - minx, maxy-miny, maxz-minz in (km)
        double[] d = new double[3];
        for (j=0; j<3; j++)
            d[j] = max[j] - min[j];
        return d;
    }
    
  /** Transformation from equatorial CS to decart. CS
   * @author Alex Kono
   * @return X, Y, Z
   */
    public static double[] astro2xyz(double rac, double dec, double r){
        double[] xyz=new double[3];
        xyz[0]=r*Math.cos(rac)*Math.cos(dec);
        xyz[1]=r*Math.sin(rac)*Math.cos(dec);
        xyz[2]=r*Math.sin(dec);
        return xyz;
    }
    
    
/** Returns R, Delta, Alpha in degrees
 *       r*cos(delta)*cos(phi) = x
 *       r*cos(delta)*sin(phi) = y
 *       r*sin(delta)            = z
 *       c       phi = atan(y/x)
 *       delta = asin(z/r)
 *       r = sqrt(x*x + y*y + z*z)
 */
    public static double[] rec2sph (double[] xyz) {
        double irad = 180./Math.PI;
        double  arg, phi, delta;
        int X = 0; int Y = 1; int Z = 2;
        double radius = Math.sqrt (xyz[0] * xyz[0] + xyz[1] * xyz[1] + xyz[2] * xyz[2]);
        
  /*if ((xyz[Y] != 0.) && (xyz[X] != 0.)) {
    phi = irad * Math.atan2 (xyz[Y], xyz[X]);
  } else {
    phi = 0;
  }*/
        
        if ((xyz[Y] == 0.) && (xyz[X] == 0.))
            phi = 0;
        else
            phi = irad * Math.atan2 (xyz[Y], xyz[X]);
        
        
        if (phi < 0.) {
            phi = phi + 360.;
        }
        arg = xyz[Z] / radius;
        
        if (arg < 1.) {
            delta = irad * Math.asin (arg);
        } else {
            delta = 90.;
        }
        return new double[]{ radius, delta, phi };
    }
    
/** Returns XYZ, Input in degrees.
 *       r*cos(delta)*cos(phi) = x
 *       r*cos(delta)*sin(phi) = y
 *       r*sin(delta)            = z
 *       c       phi = atan(y/x)
 *       delta = asin(z/r)
 *       r = sqrt(x*x + y*y + z*z)
 */
    public static double[] sph2rec (double[] r_delta_phi) {
        double[] xyz = new double[3];
        double r = r_delta_phi[0];
        double delta = toRadians( r_delta_phi[1] );
        double phi = toRadians( r_delta_phi[2] );
        
        xyz[0] = r*Math.cos(phi)*Math.cos(delta);
        xyz[1] = r*Math.sin(phi)*Math.cos(delta);
        xyz[2] = r*Math.sin(delta);
        return xyz;
    }

/** Returns XYZ, Input in degrees.
 *       r*cos(delta)*cos(phi) = x
 *       r*cos(delta)*sin(phi) = y
 *       r*sin(delta)            = z
 *       c       phi = atan(y/x)
 *       delta = asin(z/r)
 *       r = sqrt(x*x + y*y + z*z)
 */
    public static double[] sph2rec (double r, double delta, double phi) {
        return sph2rec(new double[]{r, delta, phi});
    }
    
    
    
    public static double toRadians(double angle) {
        return angle*Const.D_TO_R;
    }
    
/** Returns the unique filename.
 * @param prefix can be "/tmp/tmpImage"
 * @param sufix typicaly - dot + extension ".bmp"
 * @return String filename, like "/tmp/tmpImage32397371.bmp"
 */
    public static synchronized String getRandomFilename(String prefix, String sufix) {
        Random random = new Random();
        int n = random.nextInt();
        String res = prefix + Math.abs(n) + sufix;
        File file = new File(res);
        if (file.exists())
            return getRandomFilename(prefix, sufix);
        else
            return res;
    }
    
/**
 * Method to copy a file from a source to a
 * destination specifying if
 * source files may overwrite newer destination files and the
 * last modified time of <code>destFile</code> file should be made equal
 * to the last modified time of <code>sourceFile</code>.
 *
 * @throws IOException
 */
    public static void copyFile(File sourceFile, File destFile,  boolean overwrite, boolean preserveLastModified)
    throws IOException {
        
        if (overwrite || !destFile.exists() || destFile.lastModified() < sourceFile.lastModified()) {
            
            if (destFile.exists() && destFile.isFile()) destFile.delete();
            
            
            // ensure that parent dir of dest file exists!
            // not using getParentFile method to stay 1.1 compat
            File parent = new File(destFile.getParent());
            if (!parent.exists()) parent.mkdirs();
            
            FileInputStream in = new FileInputStream(sourceFile);
            FileOutputStream out = new FileOutputStream(destFile);
            
            byte[] buffer = new byte[8 * 1024];
            int count = 0;
            do {
                out.write(buffer, 0, count);
                count = in.read(buffer, 0, buffer.length);
            } while (count != -1);
            
            in.close();
            out.close();
            
            
            if (preserveLastModified) {
                destFile.setLastModified(sourceFile.lastModified());
            }
        }
    }

    public static String addSpaces(String s, int len) {
        StringBuffer sb = new StringBuffer(s);
        for (int i=0; i<len-s.length(); i++) sb.append(' ');
        return sb.toString();
    }

    
    public static String replaceSpaces(String str) {
        StringBuffer res = new StringBuffer(str);
        int pos = 0;
        while ( (pos = str.indexOf(' ', pos+1)) != -1)
            res.setCharAt(pos, '_');
        return res.toString();
    }
    
    public static String replaceUnderlines(String str) {
        StringBuffer res = new StringBuffer(str);
        int pos = 0;
        while ( (pos = str.indexOf('_', pos+1)) != -1)
            res.setCharAt(pos, ' ');
        return res.toString();
    }
    
    public static String replace(String s, String s1, String s2) {
        StringBuffer sb = new StringBuffer(s);
        int start = 0;
        for (;;) {
            start = sb.toString().indexOf(s1, start);
            if (start < 0) break;
            sb.replace(start, start + s1.length(), s2);
            start++;
        }
        return sb.toString();
    }
    
    public static void main(String[] args) {
        getRGB(java.awt.Color.white);
    }
    
    public static Enumeration sort(Enumeration e) {
        Vector v = new Vector();
        while (e.hasMoreElements()) sortInsert(v, e.nextElement());
        return v.elements();
    }
    
    public static void sortInsert(Vector v, Object obj) {
        Enumeration e = v.elements();
        for (int i=0; i<v.size(); i++) {
            Object cur = v.elementAt(i);
            if (obj.toString().compareTo(cur.toString()) < 0) {
                v.insertElementAt(obj, i);
                return;
            }
        }
        v.addElement(obj);
    }
    
    
    
    /** If the String str is in the list - return true. */
    public static boolean inTheList(String str, String[] list) {
        for (int i=0; i<list.length; i++) 
            if (str.equals(list[i])) return true;
        return false;
    }
    
    /** Returns URL of the resource */
    public static java.net.URL findResource(String file) throws FileNotFoundException {
        java.net.URL url = OVTCore.class.getClassLoader().getSystemResource(file);
        if (url == null) throw new FileNotFoundException("File not found ("+file+")");
        return url;
    }
}
