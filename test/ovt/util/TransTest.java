/*=========================================================================

Program:   Orbit Visualization Tool

Copyright (c) 2022 OVT Team
(Erik Johansson, Yuri Khotyaintsev)
Copyright (c) 2016 OVT Team
(Erik Johansson, Fredrik Johansson, Yuri Khotyaintsev)
Copyright (c) 2000-2003 OVT Team
(Kristof Stasiewicz, Mykola Khotyaintsev, Yuri Khotyaintsev)
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

OVT Team (https://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
Khotyaintsev, E. P. G. Johansson, F. Johansson

=========================================================================*/

package ovt.util;

import java.io.FileOutputStream;
import java.io.FileDescriptor;
import java.io.PrintStream;
import java.util.*;

import org.junit.*;
import static org.junit.Assert.*;

import ovt.OVTCore;
import ovt.datatype.Matrix3x3;
import ovt.mag.MagProps;
import ovt.mag.model.IgrfModel;
import ovt.object.CoordinateSystem;
import ovt.XYZWindow;



/**
 * Test code for ovt.util.Trans.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2022
 */
public class TransTest {

    private static IgrfModel igrf;

    /**
     * MJD values which can be tested (iterated over).
     */
    final private double[] MJD_ARRAY = {
        ovt.datatype.Time.Y1980,
        ovt.datatype.Time.Y2000,
        ovt.datatype.Time.getMjd(2020, 1, 1, 0, 0, 0)
    };

    /**
     * List of "all" coordinate systems (CS) to test (iterate over).
     */
    final private int[] CS_ARRAY = {
        CoordinateSystem.GEI,
        CoordinateSystem.GSM,
        CoordinateSystem.GEO,
        CoordinateSystem.GSE,
        CoordinateSystem.SM,
        CoordinateSystem.GEID,
    };



    /** Array of test vectors, that should be used as coordinates in arbitrary
     *  coordinate system.
     */
    private static final ArrayList<double[]> VECTOR_ARRAY = new ArrayList<double[]>();
    static {
        // Initialize VECTOR_ARRAY.
        double[] X_ARRAY = {0.0, 15e3};
        ArrayList<double[]> VECTOR_ARRAY = new ArrayList<double[]>();
        for (double x: X_ARRAY) {
            for (double y: X_ARRAY) {
                for (double z: X_ARRAY) {
                    if (x != 0 | y!= 0 | z!= 0) {
                        // CASE: Vector is not zero/(0, 0, 0).
                        VECTOR_ARRAY.add(new double[] {x, y, z});
                    }
                }
            }
        }
    }



    /** Help function for tests. Derive absolute max difference between elements
     *  in any two matrices.*/
    public double matricesMaxDiff(Matrix3x3 M1, Matrix3x3 M2) {
        final int[] MATRIX_INDICES = {0, 1, 2};

        double diff = 0;
        for (int i: MATRIX_INDICES) {
            for (int j: MATRIX_INDICES) {
                double compDiff = Math.abs(M1.get(i,j) - M2.get(i,j));

                diff = Math.max(diff, compDiff);
            }
        }

        return diff;
    };



    /** Help function for tests. Determine whether two matrices can regarded as
     * numerically equal. */
    public boolean matricesEqual(Matrix3x3 M1, Matrix3x3 M2, double epsilon) {
        double diff = matricesMaxDiff(M1, M2);
        return diff <= epsilon;
    };



    /** Initialization done once, for all tests (@BeforeClass instead of
     *  @Before). In particular, the creation of XYZWindow is very slow.
     */
    @BeforeClass
    public static void beforeClass() {
        XYZWindow XYZwin = new XYZWindow();
        XYZwin.start();
        OVTCore core = XYZwin.getCore();
        MagProps magProps = new MagProps(core);
        igrf = new IgrfModel(magProps);

        /**
         * IMPLEMENTATION NOTE: OVTCore:Initialize() calls System.setErr() and
         * System.setOut() which redirect messages to log files. These commands
         * reset them so that stdout and stderr can be used for logging the test
         * code.
         */
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
    }



    /** Tests for method ovt.util.Trans.trans_matrix() which obtains the
     *  coordinate transformation matrix between any two coordinate systems. */
    @Test
    public void test_trans_matrix() {
        final double EPSILON_ERROR = 1.6e-15;
        final double EPSILON_PRINT = 1.6e-15;
        System.out.println("test_trans_matrix(): EPSILON_ERROR = " + EPSILON_ERROR);
        System.out.println("test_trans_matrix(): EPSILON_PRINT = " + EPSILON_PRINT);

        for (double mjd: MJD_ARRAY) {

            Trans T = new ovt.util.Trans(mjd, igrf);

            // MANUAL TEST
            // For testing a specific case, e.g. to debug failed tests.
            if (false) {
                int cs1 = 0;
                int cs2 = 1;
                int cs3 = 4;
                Matrix3x3 M12 = T.trans_matrix(cs1, cs2);
                Matrix3x3 M23 = T.trans_matrix(cs2, cs3);
                Matrix3x3 M31 = T.trans_matrix(cs3, cs1);
                Matrix3x3 M = M31.multiply(M23).multiply(M12);

                boolean success = matricesEqual(M, Matrix3x3.IDENTITY_MATRIX, EPSILON_ERROR);
                if (!success) {
                    System.out.println("test_trans_matrix(): Too large diff: ("+cs1+", "+cs2+", "+cs3+")");
                }
            }

            /* Transform CS_x --> CS_x (identity transformation)
             */
            for (int cs1: CS_ARRAY) {
                Matrix3x3 M = T.trans_matrix(cs1, cs1);

                // ASSERT: Always return identity.
                assertTrue(matricesEqual(M, Matrix3x3.IDENTITY_MATRIX, EPSILON_ERROR));
            }

            /* Transform CS_1 --> CS_2 --> CS_1
             */
            for (int cs1: CS_ARRAY) {
                for (int cs2: CS_ARRAY) {
                    Matrix3x3 M12 = T.trans_matrix(cs1, cs2);
                    Matrix3x3 M21 = T.trans_matrix(cs2, cs1);
                    Matrix3x3 M = M12.multiply(M21);

                    // ASSERT: Inverse = Transpose
                    assertTrue(matricesEqual(M12, M21.getInverse(), EPSILON_ERROR));
                    // ASSERT: Pair of mutually inverse matrices.
                    assertTrue(matricesEqual(M, Matrix3x3.IDENTITY_MATRIX, EPSILON_ERROR));
                    // ASSERT: No mirroring = Preserve handedness
                    assertTrue(Math.abs(M12.getDeterminant() - 1) <= EPSILON_ERROR);
                }
            }

            /* Transform CS_1 --> CS_2 --> CS_3 --> CS_1
             */
            for (int cs1: CS_ARRAY) {
                for (int cs2: CS_ARRAY) {
                    for (int cs3: CS_ARRAY) {
                        Matrix3x3 M12 = T.trans_matrix(cs1, cs2);
                        Matrix3x3 M23 = T.trans_matrix(cs2, cs3);
                        Matrix3x3 M31 = T.trans_matrix(cs3, cs1);

                        // NOTE: Order of matrix multiplication.
                        Matrix3x3 M = M31.multiply(M23).multiply(M12);

                        // ASSERT: "Circle" of transformations should yield the
                        //         unity matrix.
                        double diff = matricesMaxDiff(M, Matrix3x3.IDENTITY_MATRIX);
                        if (diff >= EPSILON_PRINT) {
                            System.out.println("test_trans_matrix(): Large diff="+diff+" for coordinate systems: ("+cs1+", "+cs2+", "+cs3+")");
                        }
                        assertTrue(diff <= EPSILON_ERROR);
                    }
                }
            }

        }    // for mjd
    }    // test_trans_matrix() {



    /**
     * Help function for tests. Tests if set of coordinates in multiple
     * coordinate systems correspond to the same point in space. Calculates the
     * distance between stored values and values calculated from other
     * coordinate systems.
     *
     * @param mjd
     * @param coordinates
     */
    // NOTE: Not JUnit test function.
    public void test_coordinates(double mjd, Map<Integer, double[]> coordinates) {
        //final double EPSILON_KM_ERROR = 141.0;   // Too large, but that is what OVT seems to manage right now.
        //final double EPSILON_KM_PRINT = 100.0;
        final double EPSILON_RAD_ERROR = 0.001343;   // Too large, but that is what OVT seems to manage right now.
        final double EPSILON_RAD_PRINT = 0.0012;
        //System.out.println("test_coordinates(): EPSILON_ERROR = " + EPSILON_ERROR);
        //System.out.println("test_coordinates(): EPSILON_PRINT = " + EPSILON_PRINT);

        for (int cs1: coordinates.keySet()) {
            for (int cs2: coordinates.keySet()) {
                //if (cs1 == cs2) {
                if (cs1 >= cs2) {
                    /* Skip (1) identity transformations, and (2) reverse
                    *  transformations. Other tests test for these cases
                    *  indirectly.
                    *  NOTE: It can be useful to enable reverse transformations
                    *  when debugging in case the actual-expected difference is
                    *  easier to understand in one transformation direction.
                    */
                    continue;
                }
                Trans T = new ovt.util.Trans(mjd, igrf);
                double[] x1 = coordinates.get(cs1);
                double[] expX2 = coordinates.get(cs2);
                double[] actX2 = T.trans_coordinates(cs1, cs2, x1);

                final double[] diff = Vect.sub(actX2, expX2);
                final double diffAngle = Vect.angleOf2vect(actX2, expX2);

                //if (Vect.absv(diff) >= EPSILON_KM_PRINT) {
                if (diffAngle >= EPSILON_RAD_PRINT) {
                    TimeFormat tf = new TimeFormat();
                    tf.setDateFormat(TimeFormat.ISO);

                    System.out.printf("test_coordinates(): Large difference for%n");
                    System.out.printf("    transformation cs1=%d (%s) to cs2=%d (%s).%n",
                            cs1, CoordinateSystem.getCoordSystem(cs1),
                            cs2, CoordinateSystem.getCoordSystem(cs2));
                    System.out.printf("    mjd=%f (%s)%n", mjd, tf.format(mjd));
                    System.out.printf("    actX2      =(%10.2f, %10.2f, %10.2f), abs=%10.2f%n", actX2[0], actX2[1], actX2[2], Vect.absv(actX2));
                    System.out.printf("    expX2      =(%10.2f, %10.2f, %10.2f), abs=%10.2f%n", expX2[0], expX2[1], expX2[2], Vect.absv(expX2));
                    System.out.printf("    actX2-expX2=(%10.2f, %10.2f, %10.2f), abs=%10.2f%n", diff[0],  diff[1],  diff[2],  Vect.absv(diff ));
                    System.out.printf("    angle(actX2,expX2)=%f [rad]%n", diffAngle);
                    /* IMPLEMENTATION NOTE: Can be useful(?) to print explicit
                       vectors for analyzing the difference (debugging).*/
                }
                //assertTrue(Vect.absv(diff) <= EPSILON_KM_ERROR);
                assertTrue(diffAngle <= EPSILON_RAD_ERROR);
            }
        }
    }


    /**
      * Test explicit coordinate transformations.
      *
      *
      *
      * NOTE: Method is built to be extended with more hardcoded data which is
      * not yet available. /Erik P G Johansson, 2022-06-10.
    */
    @Test
    public void test_trans_coordinates() {

        Map<Integer, double[]> c = new HashMap();

        /*
        Test data points from Patrick Daly, MPS, e-mail 2022-06-09.
        ===========================================================
        Requested Day 2015-12-23, Spacecraft 4
        File: c:\\users\daly\.ovt\3.0\odata\\cluster4.ltof
        --------------------------------------------------------
           GSE positions (km) and velocities (km/s)
           UTC     Orbit |     X        Y        Z        R    |
        02:30:00 2427.68 |  43646.3  90965.2  -3828.7 100966.9 |
        --------------------------------------------------------
        [cut]

        Try GSM on SC4: this does not seem to be corrected yet!!
        --------------------------------------------------------
        Cluster Orbit Data:
        Requested Day 2015-12-23, Spacecraft 4
        File: c:\\users\daly\.ovt\3.0\odata\\cluster4.ltof
        --------------------------------------------------------
           GSM positions (km) and velocities (km/s)
           UTC     Orbit |     X        Y        Z        R    |
        02:30:00 2427.68 |  43646.3  90777.2   6987.1 100966.9 |
        --------------------------------------------------------
        */
        c.clear();
        c.put(CoordinateSystem.GSE, new double[] {43646.3, 90965.2, -3828.7});
        c.put(CoordinateSystem.GSM, new double[] {43646.3, 90777.2,  6987.1});
        test_coordinates(ovt.datatype.Time.getMjd(2015,12,23, 2,30,0), c);

        // 2022-06-16_____Patrick_Daly_MPS_test_coordinates/check_2005-12-23_C1.lis
        // Patrick W Daly, MPS, 2022-06-16
        c.clear();
        c.put(CoordinateSystem.GEI,  new double[] {113963.0, -15439.8, -24170.4});
        c.put(CoordinateSystem.GEID, new double[] {113997.6, -15287.4, -24104.2});
        c.put(CoordinateSystem.GSE,  new double[] {27436.4,  113138.4, -16034.6});
        c.put(CoordinateSystem.GSM,  new double[] {27436.4,  112752.6, -18554.3});
        c.put(CoordinateSystem.SM,   new double[] {22379.7,  112752.6, -24416.5});
        test_coordinates(ovt.datatype.Time.getMjd(2005,12,23, 16,0,0), c);

        // 2022-06-16_____Patrick_Daly_MPS_test_coordinates/check_2010-10-15_C2.lis
        // Patrick W Daly, MPS, 2022-06-16
        c.clear();
        c.put(CoordinateSystem.GEI,  new double[] { 27514.9, -19991.0, -41825.3});
        c.put(CoordinateSystem.GEID, new double[] { 27606.9, -19924.5, -41796.4});
        c.put(CoordinateSystem.GSE,  new double[] {-12442.3,  42728.1, -30423.0});
        c.put(CoordinateSystem.GSM,  new double[] {-12442.3,  23378.3, -46954.3});
        c.put(CoordinateSystem.SM,   new double[] {-13112.9,  23378.3, -46771.5});
        test_coordinates(ovt.datatype.Time.getMjd(2010,10,15, 14,0,0), c);

        // 2022-06-16_____Patrick_Daly_MPS_test_coordinates/check_2015-08-17_C4.lis
        // Patrick W Daly, MPS, 2022-06-16
        c.clear();
        c.put(CoordinateSystem.GEI,  new double[] {14316.4,  46385.4, -50613.8});
        c.put(CoordinateSystem.GEID, new double[] {14231.1,  46435.3, -50592.1});
        c.put(CoordinateSystem.GSE,  new double[] { 1682.8, -26554.1, -64887.3});
        c.put(CoordinateSystem.GSM,  new double[] { 1682.8, -48967.4, -50176.5});
        c.put(CoordinateSystem.SM,   new double[] { 5380.9, -48967.4, -49915.5});
        test_coordinates(ovt.datatype.Time.getMjd(2015,8,17, 6,0,0), c);

        // 2022-06-16_____Patrick_Daly_MPS_test_coordinates/check_2020-05-22_C3.lis
        // Patrick W Daly, MPS, 2022-06-16
        c.clear();
        c.put(CoordinateSystem.GEI,  new double[] {98287.1,  -35975.1,  2455.4});
        c.put(CoordinateSystem.GEID, new double[] {98445.1,  -35526.6,  2650.3});
        c.put(CoordinateSystem.GSE,  new double[] {18910.0, -101630.3, 16561.7});
        c.put(CoordinateSystem.GSM,  new double[] {18910.0, -102603.8,  8687.0});
        c.put(CoordinateSystem.SM,   new double[] {16225.5, -102603.8, 13030.2});
        test_coordinates(ovt.datatype.Time.getMjd(2020,5,22, 8,0,0), c);
    }    // test_trans_coordinates()



    /** Test that the various xxx2yyy() methods (e.g. gei2geo()) are consistent
     *  with trans_coordinates().
     *
     *  IMPLEMENTATION NOTE: This should eventually be not very useful since the
     *  xxx2yyy() can be (and are planned to be) implemented by
     *  trans_coordinates(). This test code is built to verify that the code
     *  change does not change behaviour.
     */
    @Test
    public void test_xxx2yyy() {
        double EPSILON_ERROR = 0.0;
        double EPSILON_PRINT = 0.000001;

        for (double mjd: MJD_ARRAY) {
            Trans T = new ovt.util.Trans(mjd, igrf);
            for (double[] v1: VECTOR_ARRAY) {
                for (int iFunc=0; iFunc<8; iFunc++) {

                    double[] actV2 = {};
                    int cs1 = -1, cs2 = -1;

                    switch(iFunc) {
                        case 0:
                            actV2 = T.gei2geo(v1);
                            cs1 = CoordinateSystem.GEI;
                            cs2 = CoordinateSystem.GEO;
                            break;
                        case 1:
                            actV2 = T.gei2geo(v1, mjd);
                            cs1 = CoordinateSystem.GEI;
                            cs2 = CoordinateSystem.GEO;
                            break;
                        case 2:
                            actV2 = T.geo2gei(v1);
                            cs1 = CoordinateSystem.GEO;
                            cs2 = CoordinateSystem.GEI;
                            break;
                        case 3:
                            actV2 = T.geo2gsm(v1);
                            cs1 = CoordinateSystem.GEO;
                            cs2 = CoordinateSystem.GSM;
                            break;
                        case 4:
                            actV2 = T.gsm2geo(v1);
                            cs1 = CoordinateSystem.GSM;
                            cs2 = CoordinateSystem.GEO;
                            break;
                        case 5:
                            actV2 = T.gsm2sm(v1);
                            cs1 = CoordinateSystem.GSM;
                            cs2 = CoordinateSystem.SM;
                            break;
                        case 6:
                            actV2 = T.gei2gse(v1);
                            cs1 = CoordinateSystem.GEI;
                            cs2 = CoordinateSystem.GSE;
                            break;
                        case 7:
                            actV2 = T.gei2geid(v1);
                            cs1 = CoordinateSystem.GEI;
                            cs2 = CoordinateSystem.GEID;
                            break;
                        default:
                            assert false;
                    }

                    double[] expV2 = T.trans_coordinates(cs1, cs2, v1);
                    double distance = Vect.distance(actV2, expV2);
                    if (distance >= EPSILON_PRINT) {
                        System.out.println("test_xxx2yyy(): Large distance="+distance+" for cs1="+cs1+", cs2="+cs2+", iFunc="+iFunc+", mjd="+mjd+", v1=("+v1[0]+", "+v1[1]+", "+v1[2]+").");
                    }
                    assertTrue(distance <= EPSILON_ERROR);
                }
            }
        }
    }

}
