/*=========================================================================
 
 Program:   Orbit Visualization Tool
 Source:    $Source: /stor/devel/ovt2g/ovt/util/Utils.java,v $
 Date:      $Date: 2006/03/21 12:21:15 $
 Version:   $Revision: 2.9 $
 
 
 Copyright (c) 2000-2015 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev,
 Yuri Khotyaintsev, Erik P G Johansson, Fredrik Johansson)
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
 Khotyaintsev, E. P. G. Johansson, F. Johansson)
 
 =========================================================================*/

/*
 * Utils.java
 *
 * Created on March 23, 2000, 2:11 PM
 * 
 * Test code can be found in ovt.util.UtilsTests.java
 */
package ovt.util;

import java.util.*;
import java.io.*;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;

import ovt.*;
import ovt.datatype.*;

/**
 *
 * @author root
 * @version
 */
public class Utils extends Object {

    /**
     * Private constructor to prevent instantiation.
     */
    private Utils() {
    }


    public static float[] getRGB(java.awt.Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        //System.out.println(r+"\t"+g+"\t"+b);
        float[] rgb = new float[]{(float) (r / 255.), (float) (g / 255.), (float) (b / 255.)};
        return rgb;
    }


    /**
     * Creates and returns the array [0, 1, 2, 3, ...., objs.length-1].
     */
    public static int[] getIndexes(Object[] objs) {
        int[] res = new int[objs.length];
        for (int i = 0; i < objs.length; i++) {
            res[i] = i;
        }
        return res;
    }


    public static int[] getHashCodes(Object[] objs) {
        int[] res = new int[objs.length];
        for (int i = 0; i < objs.length; i++) {
            res[i] = objs[i].hashCode();
        }
        return res;
    }


    public static Object[] getObjects(String[] objs) {
        Object[] res = new Object[objs.length];
        for (int i = 0; i < objs.length; i++) {
            res[i] = objs[i];
        }
        return res;
    }


    /**
     * @return The angle in degrees between the vernal equinox (X axis) and the
     * Greenwich meridian. All numbers from pgs B6-B7 of 1984 Alamanc
     */
    public static double gha(Julian jday) {
        double interval, t, jday0, gmst, gha_;
        int ah, am, as;

        t = (jday.integer - Julian.J2000) / 36525;
        // Julian centuries since 2000.0

        gmst = (24110.54841
                + /* Greenwich mean sidereal time */ 8640184.812866 * t
                + /* at midnight of this day = 0h UT */ 0.093104 * t * t
                - /* coeff. are for seconds of time */ 6.2e-6 * t * t * t)
                / 3600.;                    /* 3600 sec. -> hour */


        interval
                = /* siderial hours since midnight */ 1.0027379093 * 24. * (jday.fraction);

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


    /**
     * Calculates unit sun vector (GEI) for modified julian day (mjd)
     */
    public static double[] sunmjd(double mjd) {
        double sunv[] = sun_vect(new Julian(mjd));
        Vect.normf(sunv, 1.0e0);
        return sunv;
    }


    /**
     * vector from earth to sun. NOTE: The use of this function in "sunmjd"
     * implies that the returned value is in GEI.
     */
    public static double[] sun_vect(Julian mjd) {
        return sun_vectJNI(mjd.integer, mjd.fraction);
    }


    protected static native double[] earthJNI(double integer, double fraction);


    protected static native double[] sun_vectJNI(double integer, double fraction);


    /**
     * -------------------------------------------------------------
     */
    public static double fmod360(double x) {
        x = Math.IEEEremainder(x, 360.);
        if (x < 0.) {
            x += 360.;
        }
        return x;
    }


    public static double VABS(double[] v) {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }


    public static String cuttedString(double v, int n_of_digits) {

        int p = (int) Math.pow(10, n_of_digits);
        v = Math.round(v * p);

        //System.out.println("p="+p+"  v="+v);
        v = v / p;

        if (v == Math.round(v)) {

            return Integer.toString((int) v);

        } else {

            return Double.toString(v);

        }

    }


    /**
     * Returns the signum function of the argument; zero if the argument is
     * zero, 1.0 if the argument is greater than zero, -1.0 if the argument is
     * less than zero.
     */
    public static double sign(double a) {
        if (a > 0) {
            return 1;
        } else if (a < 0) {
            return -1;
        } else {
            return 0;
        }
    }


    public static double new_sign(double a, double b) {
        double c;
        double ret_val;
        c = Math.abs(a);
        ret_val = (b < 0.0) ? -c : c;
        return ret_val;
    }


    /**
     * Convert Radians to Degrees
     */
    public static double toDegrees(double angrad) {
        double res = angrad * Const.R_TO_D;
        //System.out.println(res + " "+ Math.toDegrees(angrad));
        return res;
    }


    //Computing hyper function cosh
    public static double cosh(double x) {
        return 0.5 * (Math.exp(x) + Math.exp(-x));
    }


    //Computing hyper function sinh
    public static double sinh(double x) {
        return 0.5 * (Math.exp(x) - Math.exp(-x));
    }


    //Computing hyper function tanh
    public static double tanh(double x) {
        return sinh(x) / cosh(x);
    }


    //******* following code added by kono ********
    public static void eigens(double[] A, double[] RR, double[] E, int N) {
        int IND, L, LL, LM, M, MM, MQ, I, J, K, IA, LQ, IQ, IM, IL, NLI, NMI;
        double ANORM, ANORMX, AIA, THR, ALM, QI, ALL, AMM, X, Y, SINX, SINX2,
                COSX, COSX2, SINCS, AIL, AIM, RLI, RMI, Q, V;
        final double RANGE = 1.0e-10; /*3.0517578e-5;*/

        for (J = 0; J < N * N; J++) {
            RR[J] = 0.0;
        }
        MM = 0;
        for (J = 0; J < N; J++) {
            RR[MM + J] = 1.0;
            MM += N;
        }
        ANORM = 0.0;
        for (I = 0; I < N; I++) {
            for (J = 0; J < N; J++) {
                if (I != J) {
                    IA = I + (J * J + J) / 2;
                    AIA = A[IA];
                    ANORM += AIA * AIA;
                }
            }
        }
        if (ANORM > 0.0) {
            ANORM = Math.sqrt(ANORM + ANORM);
            ANORMX = ANORM * RANGE / N;
            THR = ANORM;
            while (THR > ANORMX) {
                THR = THR / N;
                do {
                    IND = 0;
                    for (L = 0; L < N - 1; L++) {
                        for (M = L + 1; M < N; M++) {
                            MQ = (M * M + M) / 2;
                            LM = L + MQ;
                            ALM = A[LM];
                            if (Math.abs(ALM) < THR) {
                                continue;
                            }
                            IND = 1;
                            LQ = (L * L + L) / 2;
                            LL = L + LQ;
                            MM = M + MQ;
                            ALL = A[LL];
                            AMM = A[MM];
                            X = (ALL - AMM) / 2.0;
                            Y = -ALM / Math.sqrt(ALM * ALM + X * X);
                            if (X < 0.0) {
                                Y = -Y;
                            }
                            SINX = Y / Math.sqrt(2.0 * (1.0 + Math.sqrt(1.0 - Y * Y)));
                            SINX2 = SINX * SINX;
                            COSX = Math.sqrt(1.0 - SINX2);
                            COSX2 = COSX * COSX;
                            SINCS = SINX * COSX;
                            //	   ROTATE L AND M COLUMNS
                            for (I = 0; I < N; I++) {
                                IQ = (I * I + I) / 2;
                                if ((I != M) && (I != L)) {
                                    if (I > M) {
                                        IM = M + IQ;
                                    } else {
                                        IM = I + MQ;
                                    }
                                    if (I >= L) {
                                        IL = L + IQ;
                                    } else {
                                        IL = I + LQ;
                                    }
                                    AIL = A[IL];
                                    AIM = A[IM];
                                    X = AIL * COSX - AIM * SINX;
                                    A[IM] = AIL * SINX + AIM * COSX;
                                    A[IL] = X;
                                }
                                NLI = N * L + I;
                                NMI = N * M + I;
                                RLI = RR[NLI];
                                RMI = RR[NMI];
                                RR[NLI] = RLI * COSX - RMI * SINX;
                                RR[NMI] = RLI * SINX + RMI * COSX;
                            }
                            X = 2.0 * ALM * SINCS;
                            A[LL] = ALL * COSX2 + AMM * SINX2 - X;
                            A[MM] = ALL * SINX2 + AMM * COSX2 + X;
                            A[LM] = (ALL - AMM) * SINCS + ALM * (COSX2 - SINX2);
                        } /* for M=L+1 to N-1 */

                    } /* for L=0 to N-2 */

                } while (IND != 0);
            } /* while THR > ANORMX */

        }
        /* Extract eigenvalues from the reduced matrix */
        L = 0;
        for (J = 1; J <= N; J++) {
            L = L + J;
            E[J - 1] = A[L - 1];
        }
    }


    //******* following code added by kono ********
    /**
     * Calculates axes of ellipsoid.
     *
     * @param N - number of points
     * @param Data Nx3 array of coordinates
     */
    public static double[] getEllipsoid(int N, double[][] Data) {
        double[] R = new double[3 * (3 + 1) / 2];
        double[] res = new double[3], mean = new double[3], V = new double[9];
        int j, k, b, a;
        for (j = 0; j < 3; ++j) {
            mean[j] = 0.0;
            for (a = 0; a < N; ++a) {
                mean[j] += Data[a][j];
            }
            mean[j] /= N;
        }
        for (j = 0; j < 3; ++j) {
            for (k = 0; k <= j; ++k) {
                R[j * (j + 1) / 2 + k] = 0.0;
                for (a = 0; a < N; ++a) {
                    R[j * (j + 1) / 2 + k] += Data[a][j] * Data[a][k];
                }
                R[j * (j + 1) / 2 + k] = R[j * (j + 1) / 2 + k] / N - mean[j] * mean[k];
            }
        }
        eigens(R, V, res, 3);
        for (j = 0; j < 3; ++j) {
            res[j] = 2.0 * Math.sqrt(res[j]); // getting axeses
        }
        for (j = 0; j < 3; ++j) {
            for (int i = j; i < 3; ++i) {
                if (res[j] > res[i]) {
                    double xtmp = res[j];
                    res[j] = res[i];
                    res[i] = xtmp;
                }
            }
        }
        return res;
    }


    //added by kono
    /**
     * Returns max diff.
     *
     * @param 4x3 array
     * @return vector of max differences in coordinates {xi,yi,zi}
     */
    public static double[] maxDiffer(double[][] pos) {
        double[] min = new double[3]; // min position (minx, miny, minz)
        double[] max = new double[3]; // max position (maxx, maxy, maxz)
        int i, j;

        for (j = 0; j < 3; j++) {
            min[j] = pos[0][j]; // take position of 1-static sat as initial min value.
            max[j] = pos[0][j]; // take position of 1-static sat as initial max value.
            for (i = 1; i < 4; i++) { // Iterate over the remaining satellites to find the true min & max values. 
                max[j] = Math.max(pos[i][j], max[j]);
                min[j] = Math.min(pos[i][j], min[j]);
            }
        }
        // d holds maxx - minx, maxy-miny, maxz-minz in (km)
        double[] d = new double[3];
        for (j = 0; j < 3; j++) {
            d[j] = max[j] - min[j];
        }
        return d;
    }


    /**
     * Transformation from equatorial CS to decart. CS
     *
     * @author Alex Kono
     * @return X, Y, Z
     */
    public static double[] astro2xyz(double rac, double dec, double r) {
        double[] xyz = new double[3];
        xyz[0] = r * Math.cos(rac) * Math.cos(dec);
        xyz[1] = r * Math.sin(rac) * Math.cos(dec);
        xyz[2] = r * Math.sin(dec);
        return xyz;
    }


    /**
     * Returns R, Delta, Alpha in degrees r*cos(delta)*cos(phi) = x
     * r*cos(delta)*sin(phi) = y r*sin(delta) = z c phi = atan(y/x) delta =
     * asin(z/r) r = sqrt(x*x + y*y + z*z)
     */
    public static double[] rec2sph(double[] xyz) {
        double irad = 180. / Math.PI;
        double arg, phi, delta;
        int X = 0;
        int Y = 1;
        int Z = 2;
        double radius = Math.sqrt(xyz[0] * xyz[0] + xyz[1] * xyz[1] + xyz[2] * xyz[2]);

        /*if ((xyz[Y] != 0.) && (xyz[X] != 0.)) {
         phi = irad * Math.atan2 (xyz[Y], xyz[X]);
         } else {
         phi = 0;
         }*/
        if ((xyz[Y] == 0.) && (xyz[X] == 0.)) {
            phi = 0;
        } else {
            phi = irad * Math.atan2(xyz[Y], xyz[X]);
        }

        if (phi < 0.) {
            phi = phi + 360.;
        }
        arg = xyz[Z] / radius;

        if (arg < 1.) {
            delta = irad * Math.asin(arg);
        } else {
            delta = 90.;
        }
        return new double[]{radius, delta, phi};
    }


    /**
     * Returns XYZ, Input in degrees. r*cos(delta)*cos(phi) = x
     * r*cos(delta)*sin(phi) = y r*sin(delta) = z c phi = atan(y/x) delta =
     * asin(z/r) r = sqrt(x*x + y*y + z*z)
     */
    public static double[] sph2rec(double[] r_delta_phi) {
        double[] xyz = new double[3];
        double r = r_delta_phi[0];
        double delta = toRadians(r_delta_phi[1]);
        double phi = toRadians(r_delta_phi[2]);

        xyz[0] = r * Math.cos(phi) * Math.cos(delta);
        xyz[1] = r * Math.sin(phi) * Math.cos(delta);
        xyz[2] = r * Math.sin(delta);
        return xyz;
    }


    /**
     * Returns XYZ, Input in degrees. r*cos(delta)*cos(phi) = x
     * r*cos(delta)*sin(phi) = y r*sin(delta) = z c phi = atan(y/x) delta =
     * asin(z/r) r = sqrt(x*x + y*y + z*z)
     */
    public static double[] sph2rec(double r, double delta, double phi) {
        return sph2rec(new double[]{r, delta, phi});
    }


    public static double toRadians(double angle) {
        return angle * Const.D_TO_R;
    }


    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }


    public static String addSpaces(String s, int len) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < len - s.length(); i++) {
            sb.append(' ');
        }
        return sb.toString();
    }


    public static String replaceSpaces(String str) {
        StringBuffer res = new StringBuffer(str);
        int pos = 0;
        while ((pos = str.indexOf(' ', pos + 1)) != -1) {
            res.setCharAt(pos, '_');
        }
        return res.toString();
    }


    public static String replaceUnderlines(String str) {
        StringBuffer res = new StringBuffer(str);
        int pos = 0;
        while ((pos = str.indexOf('_', pos + 1)) != -1) {
            res.setCharAt(pos, ' ');
        }
        return res.toString();
    }


    public static String replace(String s, String s1, String s2) {
        StringBuffer sb = new StringBuffer(s);
        int start = 0;
        for (;;) {
            start = sb.toString().indexOf(s1, start);
            if (start < 0) {
                break;
            }
            sb.replace(start, start + s1.length(), s2);
            start++;
        }
        return sb.toString();
    }


    public static Enumeration sort(Enumeration e) {
        Vector v = new Vector();
        while (e.hasMoreElements()) {
            sortInsert(v, e.nextElement());
        }
        return v.elements();
    }


    public static void sortInsert(Vector v, Object obj) {
        Enumeration e = v.elements();
        for (int i = 0; i < v.size(); i++) {
            Object cur = v.elementAt(i);
            if (obj.toString().compareTo(cur.toString()) < 0) {
                v.insertElementAt(obj, i);
                return;
            }
        }
        v.addElement(obj);
    }


    /**
     * Iff the String str is in the list - return true.
     */
    public static boolean inTheList(String str, String[] list) {
        for (int i = 0; i < list.length; i++) {
            if (str.equals(list[i])) {
                return true;
            }
        }
        return false;
    }


    /**
     * Returns URL of the resource.
     */
    public static java.net.URL findResource(String file) throws FileNotFoundException {
        final java.net.URL url = OVTCore.class.getClassLoader().getResource(file);
        if (url == null) {
            throw new FileNotFoundException("File not found (" + file + ")");
        }
        return url;
    }


    /**
     * Returns the unique filename.
     *
     * @param prefix can be "/tmp/tmpImage"
     * @param suffix typicaly - dot + extension ".bmp"
     * @return String filename, like "/tmp/tmpImage32397371.bmp"
     */
    public static synchronized String getRandomFilename(String prefix, String suffix) {
        Random random = new Random();
        int n = random.nextInt();
        String res = prefix + Math.abs(n) + suffix;
        File file = new File(res);
        if (file.exists()) {
            return getRandomFilename(prefix, suffix);
        } else {
            return res;
        }
    }


    /**
     * Method to copy a file from a source to a destination specifying whether
     * source files may overwrite newer destination files and the last modified
     * time of <code>destFile</code> file should be made equal to the last
     * modified time of <code>sourceFile</code>.
     *
     * @throws IOException
     */
    public static void copyFile(File sourceFile, File destFile, boolean overwrite, boolean preserveLastModified)
            throws IOException {

        if (overwrite || !destFile.exists() || destFile.lastModified() < sourceFile.lastModified()) {

            if (destFile.exists() && destFile.isFile()) {
                destFile.delete();
            }

            // Ensure that parent dir of dest file exists!
            // not using getParentFile method to stay 1.1 compatible.
            File parent = new File(destFile.getParent());
            if (!parent.exists()) {
                parent.mkdirs();
            }

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


    /**
     * Return instance of File based on argument. fileName is interpreted as
     * either of two relative paths depending on what works.
     *
     * @param returnNullForNonexistentFile determine whether to return a File
     * object also for non-existent files (non-directories). NOTE: Disabled for
     * now due to implementation issues. Always "true".
     */
    /*public static File findFile(String fileName, boolean returnNullForNonexistentFile) {
     if (fileName == null) {
     return null;
     }
     File file = new File(OVTCore.getUserDir() + fileName);
     if (!file.exists() | file.isDirectory()) {
     file = null;
     //}
     //if (file == null) {
     ClassLoader classLoader = OVTCore.class.getClassLoader();
     java.net.URL fn = classLoader.getResource(fileName);
     if (fn == null) {
     return null;
     }
     file = new File(fn.getFile());
     if (!file.exists() | file.isDirectory()) {
     file = null;
     }
     }
     return file;
     }//*/
    /**
     * Will only return a File object for something that exists, otherwise null.
     * Hence there is no need for the caller to check if a non-null return File
     * object refers to an existing file. Implemented for backward compatibility
     * with other code.
     */
    /*public static File findFile(String fileName) {
     return findFile(fileName, true);
     }*/
    //
    //
    /**
     * Will only return a File object for a file (non-directory) that already
     * exists, otherwise null. Hence there is no need for the caller to check if
     * a non-null return File object refers to an existing file. NOTE: The
     * function is therefore also NOT suited for suggesting where to create a
     * new file.
     *
     * NOTE: The return result from ClassLoader#getResource can refer to a file
     * inside a ".jar" file.
     */
    /* OLD IMPLEMENTATION 2015-04-24 */
    public static File findFile(String fileName) {
        {
            // DEBUG
            /*Log.log("Utils#findFile(fileName="+fileName+")");
             Log.log("   First suggestion (file path): "+OVTCore.getUserDir() + fileName, 2);
             final java.net.URL tempURL = OVTCore.class.getClassLoader().getResource(fileName);
             final String tempStr = String.valueOf(tempURL);
             Log.log("   Second suggestion (resource): "+tempStr, 2);*/
        }

        if (fileName == null) { //if the filename is bogus, return null
            return null;
        }
        
        File file = new File(System.getProperty("user.dir")+ File.separator + fileName); // check if file is in current working directory
        if (!file.exists() | file.isDirectory()) {
            file = null;
        }
        if (file == null) {
            //System.out.println(System.getProperty("user.dir")+File.separator+  fileName + " not found, checking elsewhere");

            file = new File(OVTCore.getUserDir() + fileName);
            if (!file.exists() | file.isDirectory()) {
                file = null;
            }
            if (file == null) {
                final ClassLoader classLoader = OVTCore.class.getClassLoader();
                final java.net.URL fn = classLoader.getResource(fileName);
                if (fn == null) {
                    return null;
                }
                file = new File(fn.getFile());
                if (!file.exists() | file.isDirectory()) {
                    file = null;
                }
            }
        }
        return file;
    }


    /**
     * Return instance of File based on argument.
     *
     * @param dirName Interpreted as a URL to a dir in the OVT installation
     * directory.
     * @return A File object representing the directory. Null, if (1)
     * dirName==null, or (2) can not interpret dirName as a "resource", or (3)
     * the _directory_ (as represented by the argument) does NOT exist.
     */
    public static File findSysDir(String dirName) {
        if (dirName == null) {
            return null;
        }

        final ClassLoader classLoader = OVTCore.class.getClassLoader();
        final java.net.URL fn = classLoader.getResource(dirName);
        if (fn == null) {
            return null;
        }
        File file = new File(fn.getFile());
        if (!file.exists() | !file.isDirectory()) {
            file = null;
        }
        return file;
    }


    /**
     * Return instance of File based on argument. dirName is interpreted as a
     * relative path under the user's OVT config directory. Will ONLY return a
     * path to an existing directory.
     *
     * @param subdirName
     * @return The directory. Null if the derived path does not exist or is not
     * a directory.
     */
    public static File findUserDir(String subdirName) {
        if (subdirName == null) {
            return null;
        }
        File file = new File(OVTCore.getUserDir() + subdirName);
        if (!file.exists() | !file.isDirectory()) {
            file = null;
        }
        return file;
    }


    /**
     * Download arbitrary file from a URL and save it to disk.
     *
     * @return Number of bytes downloaded.
     */
    public static int downloadURLToFile(String urlStr, File file) throws MalformedURLException, IOException {
        final int OUTPUT_BUFFER_SIZE = 1024 * 1024;
        final int TRANSFER_BUFFER_SIZE = OUTPUT_BUFFER_SIZE;   // Makes sense?

        final URL url = new URL(urlStr);   // throws  MalformedURLException. Does not seem to throw for non-existing URL.
        int bytesReadTotal = 0;

        //final OutputStream out;        
        try (InputStream in = url.openStream(); OutputStream out = new BufferedOutputStream(new FileOutputStream(file), OUTPUT_BUFFER_SIZE)) {

            final byte[] buffer = new byte[TRANSFER_BUFFER_SIZE];
            int bytesReadThisIteration;

            while ((bytesReadThisIteration = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesReadThisIteration);
                bytesReadTotal += bytesReadThisIteration;
            }
            //in.close();   // Should be unnecessary.
            //out.close();  // Should be unnecessary.

        }
        return bytesReadTotal;
    }


    /**
     * Return array that is the concatenation of multiple arrays. Should work
     * for zero-length arrays, including zero-length aa. No null pointers
     * permitted anywhere.
     */
    public static double[] concatDoubleArrays(double[][] aa) {
        int N_a = 0;
        for (double[] ap : aa) {
            if (ap == null) {
                throw new NullPointerException("Array component is null.");
            }
            N_a = N_a + ap.length;
        }
        final double[] a = new double[N_a];

        int i_a = 0;
        for (double[] ap : aa) {
            System.arraycopy(ap, 0, a, i_a, ap.length);
            i_a += ap.length;
        }
        return a;
    }

    /**
     * Entirely analogous with concatDoubleArrays but for int arrays instead.
     */
    public static int[] concatIntArrays(int[][] aa) {
        int N_a = 0;
        for (int[] ap : aa) {
            if (ap == null) {
                throw new NullPointerException("Array component is null.");
            }
            N_a = N_a + ap.length;
        }
        final int[] a = new int[N_a];

        int i_a = 0;
        for (int[] ap : aa) {
            System.arraycopy(ap, 0, a, i_a, ap.length);
            i_a += ap.length;
        }
        return a;
    }

    /**
     * @author Erik P_SI G Johansson
     *
     * Find index of a specified value.<BR>
     * NOTE: Behaviour is undefined for any appearance of NaN or Inf anywhere.
     *
     * @param a Sorted array, monotonically increasing values, i.e. no value
     * occurs multiple times.
     *
     * @return The index i for which a[i]==x if there is any. If x lies between
     * any pair of elements, return the lower/higher index depending on rounding
     * mode. If x lies outside the range of the array, return -1 or a.length
     * depending on rounding mode. Empty arrays return 0 (RoundingMode.CEILING;
     * a.length.) and -1 (RoundingMode.FLOOR; a.length-1) which should be
     * consistent with other behaviour (sic!).
     */
    public static int findNearestMatch(double[] a, double x, RoundingMode indexRoundingMode) {
        /* Argument check.
         ---------------
         Important since RoundingMode has many confusing forms of rounding.
         NOTE: RoundingMode.FLOOR/.CEILING round toward negative/positive
         infinity as opposed to RoundingMode.DOWN/.UP. */
        if ((indexRoundingMode != RoundingMode.FLOOR) && (indexRoundingMode != RoundingMode.CEILING)) {
            throw new IllegalArgumentException("Illegal \"indexRoundingMode\" value.");
        }

        int i = java.util.Arrays.binarySearch(a, x);
        if (i < 0) {
            // CASE: No exact match.
            i = -i - 1;  // "Insertion point" in "Arrays" API documentation <=> rounding up (ceil).
            if (indexRoundingMode == RoundingMode.FLOOR) {
                i--;
            }
        }
        return i;
    }


    /**
     * Find an interval of values in a sorted array.
     *
     * NOTE: Behaviour is undefined for any appearance of NaN or Inf anywhere.
     * NOTE: This function is often used for copying intervals from arrays.
     * Howver, one might still want to keep it separate from the copying so that
     * the derived indices can be used on other arrays than the parameter array.
     *
     * @param a Sorted array, monotonically increasing values, i.e. no value
     * occurs multiple times.
     *
     * @return Pair of elements defining the specified interval of indices into
     * the array "a". The first element defines the lower bound (inclusive), the
     * second element defines the upper bound (exclusive). These will always
     * define a valid interval that can be copied.
     */
    public static int[] findInterval(double[] a, double min, double max, boolean minInclusive, boolean maxInclusive) {
        // Argument checks.
        if (((max == min) & !minInclusive & !maxInclusive) | (max < min)) {
            throw new IllegalArgumentException("Illegal combination of arguments which define no interval.");
        }

        int i_first, i_last;  // Will be assigned value which are always valid indices into "a".
        if (minInclusive) {
            i_first = findNearestMatch(a, min, RoundingMode.CEILING);
        } else {
            i_first = findNearestMatch(a, min, RoundingMode.FLOOR) + 1;
        }
        if (maxInclusive) {
            i_last = findNearestMatch(a, max, RoundingMode.FLOOR);
        } else {
            i_last = findNearestMatch(a, max, RoundingMode.CEILING) - 1;
        }
        return new int[]{i_first, i_last + 1};   // Inclusive-exclusive - Add one to "i_last" to make return value exclusive.
    }


    /**
     * @param a Sorted array, monotonically increasing values, i.e. no value
     * occurs multiple times.
     */
    public static double[] selectArrayIntervalMC(double[] a, double min, double max, boolean minInclusive, boolean maxInclusive) {
        final int[] ii = findInterval(a, min, max, minInclusive, maxInclusive);
        return selectArrayIntervalMC(a, ii[0], ii[1]);
    }


    /**
     * Select interval of indices from array. If specifying the entire source
     * array, then return the source array (same reference; shallow copy), if
     * not, then return new array with copied content. MC = maybe copy (only
     * copy if necessary)
     *
     * @param i_beginInclusive Start index, inclusive.
     * @param i_endExclusive End index, exclusive.
     */
    // PROPOSAL: Add optional flag alwaysCopy/alwaysNewArray.
    public static double[] selectArrayIntervalMC(double[] a, int i_beginInclusive, int i_endExclusive) {
        // Argument checks.
        if ((i_beginInclusive < 0) || (i_endExclusive < i_beginInclusive)) {
            throw new IllegalArgumentException("i_start out of range.");
        } else if (a.length < i_endExclusive) {
            throw new IllegalArgumentException("i_end out of range.");
        }

        if ((i_beginInclusive == 0) & (i_endExclusive == a.length)) {
            return a;
        } else {
            double[] ra = new double[i_endExclusive - i_beginInclusive];  // ra = return array
            System.arraycopy(a, i_beginInclusive, ra, 0, i_endExclusive - i_beginInclusive);
            return ra;
        }
    }


    /**
     * Get "distance from interval".
     *
     * NOTE: For empty intervals (lowerBoundInclusive==upperBoundExclusive), the
     * interval boundaries should really be thought of as being the located
     * between integers.
     *
     * @param lowerBoundInclusive Defines beginning of interval. The actual
     * boundary is located between this integer and the next lower one
     * (important distinction to make understand the behaviour for intervals).
     * @param upperBoundExclusive Defines the upper end of interval. The actual
     * boundary is located between this integer and the next lower one.
     *
     * @return Zero if i inside interval. Negative/positive number if i is
     * lower/higher than interval. The magnitude defines the distance to then
     * nearest integer within the interval.
     */
    public static int distanceFromInterval(int lowerBoundInclusive, int upperBoundExclusive, int i) {
        // Argument checks.
        if (lowerBoundInclusive > upperBoundExclusive) {
            throw new IllegalArgumentException("Illegal arguments.");
        }

        if (i < lowerBoundInclusive) {
            return i - lowerBoundInclusive;   // Always returns negative value.
        } else if (upperBoundExclusive <= i) {
            return i - (upperBoundExclusive - 1);   // Always returns positive value.
        } else {
            return 0;
        }
    }


    /**
     * Construct array with linearly increasing/decreasing values.
     */
    public static double[] newLinearArray(double first, double last, int N) {
        if (N < 0) {
            throw new IllegalArgumentException("Negative N.");
        }

        if (N == 1) {            
            if (first != last) {
                throw new IllegalArgumentException("First and last value can not be different for length=1 array.");
            }
            
            return new double[]{first};
            
        } else {
            // CASE: N==0 or N>=2.
            // NOTE: Does not work for N==1 since it
            // 1) requires first==last
            // 2) results in 0/0 = NaN even when first==last.

            final double[] a = new double[N];
            for (int i = 0; i < N; i++) {
                a[i] = first + (last - first) / (N - 1) * i;
            }
            return a;
        }
    }


    /**
     * Given an array, construct a new same-sized array where the components are
     * the sum of the components up to that index in the original array.
     *
     * @param inclusiveSum Determines whether the sum should include the index
     * itself.
     * @return Array where a component i contains the sum of the components a[0]
     * to a[i-1] or a[i] depending on flag.
     */
    public static int[] getCumulativeIntArray(int[] a, boolean inclusiveSum) {
        final int[] ca = new int[a.length];
        int sum = 0;
        if (inclusiveSum) {
            for (int i = 0; i < a.length; i++) {
                sum = sum + a[i];
                ca[i] = sum;
            }
        } else {
            for (int i = 0; i < a.length; i++) {
                ca[i] = sum;
                sum = sum + a[i];
            }
        }
        return ca;
    }


    /**
     * Find min and max values in array.
     */
    public static double[] minMax(double[] a) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < a.length; i++) {
            min = Math.min(min, a[i]);
            max = Math.max(max, a[i]);
        }
        return new double[]{min, max};
    }


    /**
     * Look for jumps greater or equal to threshold. Return list of indices for
     * which a[i + 1] - a[i] >= minJumpGap. NOTE: Does not check for negative
     * jumps.
     *
     * Behaviour is undefined for NaN, +Inf, -Inf.
     *
     * @a Array of numbers. Not necessarily increasing.
     */
    public static List<Integer> findJumps(double[] a, double minJumpGap) {
        final List<Integer> dataGaps = new ArrayList();
        for (int i = 0; i < a.length - 1; i++) {
            // Check if there is a (positive) jump.
            if (a[i + 1] - a[i] >= minJumpGap) {
                dataGaps.add(i);
            }
        }
        return dataGaps;
    }


    /**
     * Solve linear system of equations (LSE) Ax=r with a tridiagonal matrix A
     * using Thomas' method. NOTE: Not stable for everything, but almost. This
     * is useful for doing cubic spline interpolation.
     *
     * Based on manual derivation (approximately Wikipedia's derivation).
     *
     * @param af Elements to the left of the matrix diagonal. Note that it is
     * one element smaller than the matrix diagonal. Note that the index equals
     * row minus one. (f=first=first row omitted)
     * @param b Matrix diagonal.
     * @param cl Elements to the right of the matrix diagonal. Note that it is
     * one element smaller than the matrix diagonal. (l=last=last row omitted)
     * @param r Right-hand side vector.
     *
     * @return x vector (solution to LSE).
     */
    public static double[] solveLSE_thomasAlgorithm(double[] af, double[] b, double[] cl, double[] r) {
        // Argument check.
        if ((b.length != af.length + 1) | (b.length != cl.length + 1) | (b.length != r.length)) {
            throw new IllegalArgumentException("Array sizes do not match.");
        }

        final int N = b.length;
        final double[] x = new double[N];

        // af has index one lower than in algorithm.
        final double[] cp = new double[N - 1];    // cp = c-prime
        final double[] rp = new double[N];        // rp = r-prime

        // "af[-1] == 0" but this component is not stored.
        cp[0] = cl[0] / b[0];
        rp[0] = r[0] / b[0];

        for (int i = 1; i < N - 1; i++) {
            cp[i] = cl[i] / (b[i] - af[i - 1] * cp[i - 1]);
            rp[i] = (r[i] - af[i - 1] * rp[i - 1]) / (b[i] - af[i - 1] * cp[i - 1]);
        }

        final int k = N - 1;
        // "cp[N - 1] = 0;"  but this component is not stored.
        rp[k] = (r[k] - af[k - 1] * rp[k - 1]) / (b[k] - af[k - 1] * cp[k - 1]);

        x[N - 1] = rp[N - 1];   //   x[N-1] = rp[N-1] - cp[N-1]*x[N]; where x[N] == 0
        for (int i = N - 2; i >= 0; i--) {
            x[i] = rp[i] - cp[i] * x[i + 1];
        }

        return x;
    }

    /**
     * BC = Boundary condition
     */
    public enum SplineInterpolationBC {

        /**
         * Set the second derivative on the boundary to an arbitrary value.
         */
        SET_SECOND_DERIV,
        /**
         * Set the two second derivatives closest to the boundary to be equal to
         * each other.
         */
        EQUAL_SECOND_DERIV
    };


    /**
     * For a tabulated function y(x), find the cubic spline second-derivatives
     * of y(x) at the points x. This is useful when doing cubic spline
     * interpolation.
     *
     * Based on section 3.3 "Cubic Spline Interpolation", "Numerical Recipes in
     * C - The Art of Scientific Computing", Second Edition, 1992, William H.
     * Press, Saul A. Teukolsky, William T. Vetterling, Brian P. Flannery.
     *
     * Naming convention: ypp = y'' = second derivative of y(x) ('=prime).
     *
     * @param yppL Second derivative at x[0] (L=lower boundary).
     * @param yppU Second derivative at x[N-1] (U=Upper boundary).
     */
    public static double[] find2ndDeriv_cubicSplineInterpolation(
            double[] x, double[] y,
            SplineInterpolationBC bcL, SplineInterpolationBC bcU,
            double yppL, double yppU) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Array sizes do not match.");
        } else if (x.length < 2) {
            throw new IllegalArgumentException("Fewer than two data points.");
        } else if (x[1] - x[0] <= 0) {
            throw new IllegalArgumentException("x is not monotonically increasing.");
            // The remainding line segments are checked in the algorithm loop.
        }

        final int N = x.length;
        final double[] af = new double[N - 1];
        final double[] b = new double[N];
        final double[] cl = new double[N - 1];
        final double[] r = new double[N];

        /*==================================================
         Configure linear system of equations to be solved.
         =================================================*/
        // Configure lower boundary.
        // Note: af[0] Does not belong to the boundary.
        if (bcL == SplineInterpolationBC.SET_SECOND_DERIV) {
            b[0] = 1;
            cl[0] = 0;
            r[0] = yppL;
        } else if (bcL == SplineInterpolationBC.EQUAL_SECOND_DERIV) {
            b[0] = 1;
            cl[0] = -1;
            r[0] = 0;
        } else {
            // Check in case new values are added to SplineInterpolationBC.
            throw new IllegalArgumentException("Illegal boundary condition.");
        }

        // Configure interior.
        for (int i = 1; i < N - 1; i++) {
            final double h1 = x[i] - x[i - 1];  // Value could be taken from the previous iteration.
            final double h2 = x[i + 1] - x[i];
            if (h2 <= 0) {
                throw new IllegalArgumentException("x is not monotonically increasing.");
                // The remaining line segment is checked at the beginning of the method.
            }
            af[i - 1] = 1.0 / 6.0 * h1;
            b[i] = 2.0 / 6.0 * (h1 + h2);
            cl[i] = 1.0 / 6.0 * h2;
            r[i] = -(y[i] - y[i - 1]) / h1 + (y[i + 1] - y[i]) / h2;
        }

        // Configure upper boundary.
        // Note: cl[N-2] does not belong to the boundary.
        if (bcU == SplineInterpolationBC.SET_SECOND_DERIV) {
            af[N - 2] = 0;
            b[N - 1] = 1;
            r[N - 1] = yppU;
        } else if (bcU == SplineInterpolationBC.EQUAL_SECOND_DERIV) {
            af[N - 2] = 1;
            b[N - 1] = -1;
            r[N - 1] = 0;
        } else {
            // Check in case new values are added to SplineInterpolationBC.
            throw new IllegalArgumentException("Illegal boundary condition.");
        }

        return solveLSE_thomasAlgorithm(af, b, cl, r);
    }


    /**
     * Use cubic splines for interpolating tabulated curve.
     *
     * If SplineInterpolationBC.SET_SECOND_DERIV is selected, then uses zero for
     * first and last 2nd derivatives.
     *
     * Based on section 3.3 "Cubic Spline Interpolation", "Numerical Recipes in
     * C - The Art of Scientific Computing", Second Edition, 1992, William H.
     * Press, Saul A. Teukolsky, William T. Vetterling, Brian P. Flannery.
     *
     * Naming convention: _int = interpolated (interpolation) point. p = prime =
     * first derivative
     *
     * @param X X values for tabulated function (monotonically increasing).
     * @param Y Y values for tabulated function.
     * @param X_int X values for the interpolated curve.
     * @param Y_int_result Array in which the Y values for the interpolated
     * curve are put.
     * @param Yp_int_result Array in which the derivative for the interpolated
     * curve are put.
     * @param bcL Boundary condition for lower (x) boundary.
     * @param bcU Boundary condition for upper (x) boundary.
     */
    public static void cubicSplineInterpolation(
            double[] X, double[] Y,
            double[] X_int,
            double[] Y_int_result,
            double[] Yp_int_result,
            SplineInterpolationBC bcL, SplineInterpolationBC bcU) {
        /* Naming convention:
         _int = interpolated (interpolation) point.
         Lower case initial = individual nbr (scalar).
         Upper case initial = array.
         */

        // Argument checks
        if ((X.length != Y.length)
                | (X_int.length != Y_int_result.length)
                | (X_int.length != Yp_int_result.length)) {
            throw new IllegalArgumentException("Array sizes do not match.");
        } else if ((X_int[0] < X[0]) | (X[X.length - 1] < X_int[X_int.length - 1])) {
            throw new IllegalArgumentException("X_int contains values outside the x range of the tabulated funcion.");
        }

        final double[] Ypp = find2ndDeriv_cubicSplineInterpolation(X, Y, bcL, bcU, 0, 0);
        final int N = X.length;
        final int N_int = X_int.length;
        final double x_last = X[X.length - 1];

        for (int i_int = 0; i_int < N_int; i_int++) {
            final double x_int = X_int[i_int];
            final int j;
            if (x_int == x_last) {
                // Special treatment of the last (highest) x value which otherwise
                // would generate an index for which the algorithm fails.
                j = N - 2;
            } else {
                // CASE: x_int[i_int] is NOT the last tabulated x value.
                j = Utils.findNearestMatch(X, x_int, RoundingMode.FLOOR);
            }
            final double h = X[j + 1] - X[j];
            final double A = (X[j + 1] - x_int) / h;
            final double B = 1 - A;
            final double C = 1 / 6.0 * (A * A - 1) * A * h * h;
            final double D = 1 / 6.0 * (B * B - 1) * B * h * h;
            Y_int_result[i_int]
                    = A * Y[j] + B * Y[j + 1]
                    + C * Ypp[j] + D * Ypp[j + 1];
            Yp_int_result[i_int]
                    = (Y[j + 1] - Y[j]) / h
                    - (3 * A * A - 1) / 6.0 * h * Ypp[j]
                    + (3 * B * B - 1) / 6.0 * h * Ypp[j + 1];
        }
    }


    /**
     * Linear interpolation of array. Interpolates a sequence/line of points
     * (X[i], Y[i]) to a sequence of points (X_int[i], Y_int[i]).
     *
     * Although the primary purpose of this function is to interpolate orbital
     * positions (one coordinate axis at a time) and calculate approximate
     * velocities, the function itself is generic and the result is in the units
     * used by the caller.
     *
     * NOTE: The dYdX value on points between line segments comes from the
     * preceeding line segment.
     *
     * @param X Array with X values. Values increase monotonically.
     * @param Y Array with one Y value for every X value.
     * @param X_int Array with X values for which interpolated Y values are
     * requested. Values increase monotonically. All values must be within the
     * range of X.
     * @param Y_int Array into which interpolated data will be put.
     * @param dYdX Array into which interpolated data will be put.
     */
    public static void linearInterpolation(
            double[] X, double[] Y,
            double[] X_int, double[] Y_int,
            double[] dYdX_int) {
        // Naming convention:
        //   Suffix "_int" = Interpolated/interpolation data
        //   No suffix = Data to interpolate from
        //   Lower case x/y = Specific points.
        //   Upper case X/Y = Entire array.

        if (X.length < 2) {
            throw new IllegalArgumentException("X array is too short (length<2) for this function.");
        } else if (Y_int.length != X_int.length) {
            throw new IllegalArgumentException("Y_int has an incompatible length.");
        } else if (dYdX_int.length != X_int.length) {
            throw new IllegalArgumentException("dXdX_int has an incompatible length.");
        } else if (X_int[0] < X[0]) {
            throw new IllegalArgumentException("Trying to interpolate outside data, X_int[0] < X[0]");
        } else if (X[X.length - 1] < X_int[X_int.length - 1]) {
            throw new IllegalArgumentException("Trying to interpolate outside data, X[X.length-1] < X_int[X_int.length-1]");
        }

        /* IMPLEMENTATION NOTE: The way of finding the line section kind of
         assumes that X_int.length is of the same order of magnitude as, or
         greater than, X.length to be efficient.*/
        int i2 = 0;
        for (int i_int = 0; i_int < X_int.length; i_int++) {

            // Find the lowest "i2" such that X[i2] >= X_int[i_int] and i2>0.
            while ((X[i2] < X_int[i_int]) || (i2 == 0)) {
                i2++;
            }

            // Interpolate between points (x1, y1) and (x2, y2) and find
            // the derivative at that point.
            final int i1 = i2 - 1;
            final double x_int = X_int[i_int];
            final double x1 = X[i1], x2 = X[i2];
            final double y1 = Y[i1], y2 = Y[i2];
            final double weight2 = (x_int - x1) / (x2 - x1);
            final double weight1 = 1 - weight2;
            Y_int[i_int] = weight1 * y1 + weight2 * y2;
            dYdX_int[i_int] = (y2 - y1) / (x2 - x1);
        }
    }


    public static long double2longSafely(double x) throws IllegalArgumentException {
        if ((x > Long.MAX_VALUE) || (x < Long.MIN_VALUE) || Double.isInfinite(x) || Double.isNaN(x)) {
            throw new IllegalArgumentException("Can not convert the double " + x + " to int.");
        }
        return (long) x;
    }


    /**
     * Returns a Random object seeded with a string.
     */
    public static Random getRandomFromString(String s) {
        /* IMPLEMENTATION NOTE: String#hashCode produces similar results for
         strings with the same beginning. The first call (and maybe second call)
         to nextDouble after initializing with similar random seeds return similar
         results. Therefore one wants to call Random#nextDouble() at least once
         and throw away the result.
         */
        final Random rand = new Random(s.hashCode());
        rand.nextDouble();   // Ignore result.
        rand.nextDouble();   // Ignore result.
        return rand;
    }


    /**
     * Calculate orbital period from a satellite's velocity and distance to
     * Earth at an arbitrarily chosen instant. This should always yield the same
     * result for an idealized elliptical orbit. Only uses SI units.
     *
     * @param r_SI Distance from center of the Earth in meters.
     * @param v_SI Velocity in m/s in non-accelerating frame.
     * @return Orbital period in seconds.
     */
    // PROPOSAL: Incorporate into OrbitalState. Partial result semimajor axis fits in there to.
    public static double orbitalPeriod(double r_SI, double v_SI) {

        /* Derive semimajor axis.
         This can (most likely) be derived from the expression for the effective
         potential for an orbit (average of min & max distance ==> semimajor axis). */
        final double mu = Const.GRAV_CONST * Const.ME;
        final double epsilon = v_SI * v_SI / 2 - mu / r_SI;
        final double a = -mu / (2 * epsilon);  // Semimajor axis

        /* Derive period from semimajor axis.
         Uses Kepler's third law. The constant in Kepler's third law (C_K3) can
         be derived using a circular orbit. */
        final double C_K3 = 4 * Math.PI * Math.PI / (Const.GRAV_CONST * Const.ME);
        final double P = Math.sqrt(C_K3 * a * a * a);

        return P;
    }

    /**
     * Return various orbital state values given instantaneous position and
     * velocity vectors and assuming an elliptical orbit.
     *
     * NOTE: Only uses SI units without prefixes (meter, seconds etc). The
     * suffix "_SI" refers to this.<BR>
     *
     * NOTE: There are orbits which are far from approximately elliptical. SSC
     * Web Services lists at least one satellite in a Lissajous orbit, "ACE" at
     * Sun-Earth L1 (SSC Web Services ID: "ace"). Satellites could also
     * conceivably temporarily (or instantanously) be be on non-sensical
     * elliptical orbits such during launch, which might give not very useful
     * results.<BR>
     *
     * NOTE: r_perigee_SI and r_apogee_SI should be NaN for unbound
     * orbits(?).<BR>
     *
     * IMPLEMENTATION NOTE: These calculations are implemented as one single
     * calculation (and class) since the calculations of various quantities
     * overlap and several are partial results in calculating others anyway.<BR>
     *
     * @param r in m
     * @param v in m/s
     */
    public static class OrbitalState {

        public final double r_perigee_SI;
        public final double r_apogee_SI;
        /**
         * Satellite orbital energy divided by satellite mass (norm=normalized).
         */
        public final double E_orbital_norm_SI;
        /**
         * Satellite angular momentum divided by satellite mass
         * (norm=normalized).
         */
        public final double L_norm_SI;
        /**
         * omega_perigee_SI = angular velocity at r_perigee_SI [Unit: s^-1].
         * Possibly useful for determining minimum required time resolution.
         */
        public final double omega_perigee_SI;
        /**
         * Orbital period. (Unit: seconds)
         */
        public final double P_SI;

        /**
         * Approximate radius of Hill sphere.
         */
        private final double R_HILL_SPHERE_SI = 1.5e9;


        /**
         * Represents orbital state assuming an orbit (elliptic/bound, parabolic
         * & hyperbolic/escape orbits) around a point gravity source.
         *
         * @param r_SI Position vector relative the Earth's center of mass.
         * Unit: meter.
         * @param v_SI Velocity vector in a non-accelerating frame/coordinate
         * system, e.g. GEI. Unit: m/s
         */
        // PROPOSAL: Separate bound from unbound (E>0) orbits explicitly?
        public OrbitalState(double[] r_SI, double[] v_SI) {
            final int DEBUG = 3;

            final double v_abs_SI = Vect.absv(v_SI);
            final double r_abs_SI = Vect.absv(r_SI);
            E_orbital_norm_SI = 0.5 * v_abs_SI * v_abs_SI - Const.GRAV_CONST * Const.ME / r_abs_SI;
            {
                final double[] rxv_SI = new double[3];
                Vect.cross(r_SI, v_SI, rxv_SI);
                L_norm_SI = Vect.absv(rxv_SI);
            }

            /**
             * Can be derived from the expressions for orbital energy and
             * angular momentum (both constant over the orbit) and effective
             * potential.
             */
            final double A = -Const.GRAV_CONST * Const.ME / (2 * E_orbital_norm_SI);
            final double B = Math.sqrt(A * A + L_norm_SI * L_norm_SI / (2 * E_orbital_norm_SI));   // NOTE: Will return NaN for less than zero.
            r_perigee_SI = A - B;
            r_apogee_SI = A + B;

            omega_perigee_SI = L_norm_SI / (r_perigee_SI * r_perigee_SI);
            P_SI = Utils.orbitalPeriod(r_abs_SI, v_abs_SI);

            //Log.log("OrbitalState constructor: r_abs       /R_Earth = " + r_abs_SI / (Const.RE * 1e3), DEBUG);
            //Log.log("                          v_abs                = " + v_abs_SI, DEBUG);
            //Log.log("                          r_perigee_SI/R_Earth = " + r_perigee_SI / (Const.RE * 1e3), DEBUG);
            //Log.log("                          r_apogee_SI /R_Earth = " + r_apogee_SI / (Const.RE * 1e3), DEBUG);
        }


        public boolean isEllipticOrbit() {
            return (E_orbital_norm_SI < 0);
        }


        public boolean isReasonableEllipticOrbit() {
            if (!isEllipticOrbit()) {
                return false;
            }

            // Should be unnecessary. Just for safety.
            if (!Double.isFinite(r_perigee_SI) | !Double.isFinite(r_apogee_SI)) {
                return false;
            }

            // Check if orbit goes below ground level.
            if (r_perigee_SI <= Const.RE * 1e3) {
                return false;
            }

            // Check if orbit goes beyond the Hill sphere, e.g. to a Lagrange point or to Mars.
            if (r_apogee_SI > R_HILL_SPHERE_SI) {
                return false;
            }

            return true;
        }
    }

}
