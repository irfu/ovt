/*=========================================================================
 
 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/util/UtilsTest.java $
 Date:      $Date: 2015/09/15 12:00:00 $
 Version:   $Revision: 1.0 $
 
 
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
package ovt.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import static ovt.util.Utils.distanceFromInterval;
import static ovt.util.Utils.findInterval;
import static ovt.util.Utils.findNearestMatch;
import static ovt.util.Utils.getRandomFromString;
import static ovt.util.Utils.linearInterpolation;

/**
 * Test code for miscellaneous ovt.util.Utils functions, but not all.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015
 */
public class UtilsTests {
    
    public static void main(String[] args) {
        test_getCumulativeIntArray();
    }
    

    /**
     * Test code.
     */
    public static void test_findInterval() {
        class Test {

            double[] a;
            double min, max;
            boolean minIncl, maxIncl;
            int[] result;


            Test(double[] a, double min, double max, boolean minIncl, boolean maxIncl, int[] result) {
                this.a = a;
                this.min = min;
                this.max = max;
                this.minIncl = minIncl;
                this.maxIncl = maxIncl;
                this.result = result;
            }
        }
        //===================================
        final List<Test> tests = new ArrayList();
        tests.add(new Test(new double[]{}, 2, 3, true, true, new int[]{0, 0}));
        tests.add(new Test(new double[]{}, 2, 3, false, false, new int[]{0, 0}));
        tests.add(new Test(new double[]{5, 6, 7}, 2, 3, false, false, new int[]{0, 0}));
        tests.add(new Test(new double[]{5, 6, 7}, 2, 3, true, true, new int[]{0, 0}));
        tests.add(new Test(new double[]{5, 6, 7}, 5, 6, true, false, new int[]{0, 1}));
        tests.add(new Test(new double[]{5, 6, 7}, 5, 6, false, true, new int[]{1, 2}));
        tests.add(new Test(new double[]{5, 6, 7}, 5, 6, false, false, new int[]{1, 1}));
        //===================================
        for (Test test : tests) {
            System.out.println("findInterval(" + Arrays.toString(test.a) + ", " + test.min + ", " + test.max + ", " + test.minIncl + ", " + test.maxIncl + ");");

            final int[] actualResult = findInterval(test.a, test.min, test.max, test.minIncl, test.maxIncl);
            if (Arrays.equals(actualResult, test.result)) {
                System.out.println("=== OK");
            } else {
                System.out.println("##############################");
                System.out.println("ERROR: actualResult = " + Arrays.toString(actualResult));
                System.out.println("##############################");
            }
        }
    }


    /**
     * Test code.
     */
    public static void test_findNearestMatch() {
        class Test {

            double[] a;
            double x;
            RoundingMode indexRoundingMode;
            int result;


            Test(double[] a, double divider, RoundingMode indexRoundingMode, int result) {
                this.a = a;
                this.x = divider;
                this.indexRoundingMode = indexRoundingMode;
                this.result = result;
            }
        }
        final List<Test> tests = new ArrayList();

        tests.add(new Test(new double[]{}, 4.0, RoundingMode.CEILING, 0));
        tests.add(new Test(new double[]{}, 4.0, RoundingMode.FLOOR, -1));
        //
        tests.add(new Test(new double[]{5.0}, 4.0, RoundingMode.CEILING, 0));
        tests.add(new Test(new double[]{5.0}, 5.0, RoundingMode.CEILING, 0));
        tests.add(new Test(new double[]{5.0}, 6.0, RoundingMode.CEILING, 1));
        //
        tests.add(new Test(new double[]{5.0}, 4.0, RoundingMode.FLOOR, -1));
        tests.add(new Test(new double[]{5.0}, 5.0, RoundingMode.FLOOR, 0));
        tests.add(new Test(new double[]{5.0}, 6.0, RoundingMode.FLOOR, 0));
        //
        tests.add(new Test(new double[]{1.0, 2.0, 3.0, 4.0}, 0.0, RoundingMode.CEILING, 0));
        tests.add(new Test(new double[]{1.0, 2.0, 3.0, 4.0}, 1.5, RoundingMode.CEILING, 1));
        tests.add(new Test(new double[]{1.0, 2.0, 3.0, 4.0}, 3.0, RoundingMode.CEILING, 2));
        tests.add(new Test(new double[]{1.0, 2.0, 3.0, 4.0}, 5.0, RoundingMode.CEILING, 4));
        //
        tests.add(new Test(new double[]{1.0, 2.0, 3.0, 4.0}, 0.0, RoundingMode.FLOOR, -1));
        tests.add(new Test(new double[]{1.0, 2.0, 3.0, 4.0}, 1.5, RoundingMode.FLOOR, 0));
        tests.add(new Test(new double[]{1.0, 2.0, 3.0, 4.0}, 3.0, RoundingMode.FLOOR, 2));
        tests.add(new Test(new double[]{1.0, 2.0, 3.0, 4.0}, 5.0, RoundingMode.FLOOR, 3));

        for (Test test : tests) {
            System.out.println("findNearestMatch(" + Arrays.toString(test.a) + ", " + test.x + ", " + test.indexRoundingMode + ");");

            final int actualResult = findNearestMatch(test.a, test.x, test.indexRoundingMode);
            if (actualResult == test.result) {
                System.out.println("=== OK");
            } else {
                System.out.println("##############################");
                System.out.println("ERROR: actualResult = " + actualResult);
                System.out.println("##############################");
            }
        }
    }


    /**
     * Test code.
     */
    public static void test_linearInterpolation() {
        class Test {

            // Y_int, dYdX_int are the results.
            final double[] X, Y, X_int, Y_int, dYdX_int;


            public Test(double[] X, double[] Y, double[] X_int, double[] Y_int, double[] dYdX_int) {
                this.X = X;
                this.Y = Y;
                this.X_int = X_int;
                this.Y_int = Y_int;
                this.dYdX_int = dYdX_int;
            }
        }
        //======================================================================
        List<Test> tests = new ArrayList();
        tests.add(new Test(new double[]{10, 15}, new double[]{20, 30}, new double[]{11}, new double[]{22}, new double[]{2}));
        tests.add(new Test(new double[]{10, 15}, new double[]{20, 30}, new double[]{10}, new double[]{20}, new double[]{2}));
        tests.add(new Test(new double[]{10, 15}, new double[]{20, 30}, new double[]{15}, new double[]{30}, new double[]{2}));
        tests.add(new Test(new double[]{10, 15}, new double[]{20, 30}, new double[]{11, 12.5, 13.5}, new double[]{22, 25, 27}, new double[]{2, 2, 2}));
        tests.add(new Test(
                new double[]{10, 15, 25},
                new double[]{20, 30, 35},
                new double[]{10, 11, 12.5, 13.5, 20, 25},
                new double[]{20, 22, 25, 27, 32.5, 35},
                new double[]{2, 2, 2, 2, 0.5, 0.5}));

        for (Test test : tests) {
            final double[] actual_Y_int = Arrays.copyOf(test.Y_int, test.Y_int.length);
            final double[] actual_dYdX_int = Arrays.copyOf(test.dYdX_int, test.dYdX_int.length);
            linearInterpolation(test.X, test.Y, test.X_int, actual_Y_int, actual_dYdX_int);
            if (Arrays.equals(test.Y_int, actual_Y_int) && Arrays.equals(test.dYdX_int, actual_dYdX_int)) {
                System.out.println("OK");
            } else {
                System.out.println("====================================================  ERROR");
            }
        }
    }


    /**
     * Test code.
     */
    public static void test_distanceFromInterval() {
        final int[][] tests = new int[][]{
            {0, 0, 1, 2},
            {0, 0, 0, 1},
            {0, 0, -1, -1},
            {3, 5, 0, -3},
            {3, 5, 2, -1},
            {3, 5, 3, 0},
            {3, 5, 4, 0},
            {3, 5, 5, 1},
            {3, 5, 7, 3}
        };

        for (int[] test : tests) {
            int actualResult = distanceFromInterval(test[0], test[1], test[2]);
            if (actualResult == test[3]) {
                System.out.println("OK");
            } else {
                System.out.println("====================================================  ERROR");
                System.out.println("actualResult = " + actualResult);
                System.out.println("test[3]      = " + test[3]);
            }
        }
    }


    public static void test_getCumulativeIntArray() {
        final ArrayList<Object[]> testList = new ArrayList();
        
        testList.add(new Object[]{new int[]{}, true, new int[]{}});
        testList.add(new Object[]{new int[]{}, false, new int[]{}});

        testList.add(new Object[]{new int[]{-5}, true, new int[]{-5}});
        testList.add(new Object[]{new int[]{-5}, false, new int[]{0}});

        testList.add(new Object[]{new int[]{1, 2, 3}, true, new int[]{1, 3, 6}});
        testList.add(new Object[]{new int[]{1, 2, 3}, false, new int[]{0, 1, 3}});

        for (Object[] test : testList) {
            final boolean ok = Arrays.equals(Utils.getCumulativeIntArray(
                    (int[]) test[0], (boolean) test[1]),
                    (int[]) test[2]);
            if (!ok) {
                throw new AssertionError();
            }
        }
        System.out.println("OK");
    }


    /**
     * Test code. Can see that Random#nextDouble() produces similar results for
     * the first call after initializing with strings with the same beginning
     * (unless the implementation of getRandomFromString() does not make those
     * calls itself).
     */
    public static void test_getRandomFromString() {
        final String cs = "qwerty";
        final String[] strings = {"A" + cs, "B" + cs, "C" + cs, cs + "A", cs + "B", cs + "C"};
        for (String s : strings) {
            final Random r = getRandomFromString(s);
            System.out.println("s = " + s);
            for (int i = 0; i < 5; i++) {
                System.out.println("   r.nextDouble() = " + r.nextDouble());
            }
        }
    }


    public static void test_solveLES_thomasAlgorithm() {
        /*double af[] = {1, 4, 3};
         double b[] = {2, 1, 0, 6};
         double cl[] = {1, 2, 5};
         double r[] = {3, 4, 5, -3};//*/
        double af[] = {0.0};
        double b[] = {1.0, 1.0};
        double cl[] = {0.0};
        double r[] = {1.0, 1.0};//*/
        final int N = b.length;

        final double[] x = Utils.solveLSE_thomasAlgorithm(af, b, cl, r);

        // Matrix multiplication.
        final double[] rp = new double[N];
        rp[0] = b[0] * x[0] + cl[0] * x[1];
        for (int i = 1; i < N - 1; i++) {
            rp[i] = af[i - 1] * x[i - 1] + b[i] * x[i] + cl[i] * x[i + 1];
        }
        rp[N - 1] = af[N - 2] * x[N - 2] + b[N - 1] * x[N - 1];
        System.out.println("r = " + Arrays.toString(r));
        System.out.println("rp = " + Arrays.toString(rp));

        double error = 0;
        double r_abs = 0;
        for (int i = 0; i < N; i++) {
            error += (rp[i] - r[i]) * (rp[i] - r[i]);
            r_abs += r[i] * r[i];
        }
        error = Math.sqrt(error);
        r_abs = Math.sqrt(r_abs);
        System.out.println("r_abs = " + r_abs);
        System.out.println("error = " + error);
    }


    public static void test_find2ndDeriv_cubicSplineInterpolation() {
        /*final double[] x = {3, 4, 50};
         final double[] y = {3, 3, 3}; //*/
        final double[] x = {0, 1};
        final double[] y = {0, 0};//*/
        final double[] ypp = Utils.find2ndDeriv_cubicSplineInterpolation(
                x, y,
                Utils.SplineInterpolationBC.SET_SECOND_DERIV,
                Utils.SplineInterpolationBC.SET_SECOND_DERIV,
                0, 0);
        for (int i = 0; i < x.length; i++) {
            System.out.println("" + x[i] + ", " + y[i] + ", " + ypp[i]);
        }

    }


    /**
     * Test code for cubicSplineInterpolation. Still based on user interaction.
     */
    // PROPOSAL: Add markings for the points which are actually tabulated (for the original tabulated curve).
    public static void test_cubicSplineInterpolation() {
        //final double[] x = {2, 3, 4, 5, 6, 7, 8, 9, 10};
        //final double[] y = {2, 3, 3.1, 2.1, 2, 2, 2, 2, 2};
        /*final double[] x = {2, 3, 3.3, 5, 6, 7, 8};
         final double[] y = {2, 2, 3.1, 2.1, -2, 2, 2};
         //*/
        /*final double[] x = {0, 2, 4, 6, 8, 10, 12, 14};   // Ramp function
         final double[] y = {2, 2, 2, 2, 4, 6, 8, 10};
         //*/
        final double[] x = {0, 1, 4, 6.8, 7.2, 10, 12, 14};
        //final double[] x = {0, 2, 4, 6, 8, 10, 12, 14};
        final double[] y = {2, 2, 2, 2, 4, 4, 4, 4}; // Step function
        //*/
        /*final double[] x = {2, 3.3, 4, 5};
         final double[] y = {2, 3, 4, 2};//*/
        final double[] x_int = Utils.newLinearArray(x[0], x[x.length - 1], x.length + (x.length - 1) * 19);
        //final double[] x_int = {3.69, 3.7, 3.71};
        final double[] y_int_result = new double[x_int.length];
        final double[] yp_int_result = new double[x_int.length];

        Utils.cubicSplineInterpolation(
                x, y, x_int, y_int_result, yp_int_result,
                //Utils.SplineInterpolationBC.SET_SECOND_DERIV,
                Utils.SplineInterpolationBC.EQUAL_SECOND_DERIV,
                Utils.SplineInterpolationBC.SET_SECOND_DERIV
        //Utils.SplineInterpolationBC.EQUAL_SECOND_DERIV
        );

        for (int i = 0; i < x.length; i++) {
            System.out.println("" + x[i] + ", " + y[i]);
        }
        System.out.println("--");
        for (int i = 0; i < x_int.length; i++) {
            System.out.println("" + x_int[i] + ", " + y_int_result[i] + ", " + yp_int_result[i]);
        }

        /*=================================
         Construct window that draw graphs.
         =================================*/
        final JFrame f = new JFrame("Cubic spline test");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new JPanel() {

            public Dimension getPreferredSize() {
                return new Dimension(800, 1000);
            }


            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                final Graphics2D g2 = (Graphics2D) g;
                //final double[] minMax_y = Utils.minMax(y_int_result);
                final double[] minMax_y = Utils.minMax(Utils.concatDoubleArrays(new double[][]{y_int_result, yp_int_result, {0.0}}));
                final double x_size = x[x.length - 1] - x[0];
                final double y_size = (minMax_y[1] - minMax_y[0]);

                // Change coordinate system so that all graphs (in their own units) fit in the window.
                g2.scale(getWidth() / x_size, -getHeight() / y_size);   // NOTE: Change sign on y axis.
                g2.translate(-x[0], -minMax_y[0] - y_size);

                g2.setStroke(new BasicStroke(10.0f / getWidth()));   // Set width of lines.

                drawPolyline(g2, new double[]{x[0], x[x.length - 1]}, new double[]{0, 0}, Color.BLACK);   // Draw y=0
                drawPolyline(g2, x, y, Color.BLACK);                // Plot tabulated function
                drawPolyline(g2, x_int, y_int_result, Color.BLUE);     // Plot spline.
                drawPolyline(g2, x_int, yp_int_result, Color.RED);   // Plot spline derivative
            }


            private void drawPolyline(Graphics2D g2, double mX[], double mY[], Color c) {
                g2.setColor(c);
                Path2D.Double path = new Path2D.Double();
                path.moveTo(mX[0], mY[0]);

                for (int i = 1; i < mX.length; i++) {
                    path.lineTo(mX[i], mY[i]);
                }
                g2.draw(path);
            }

            // Move to Utils?
        });
        f.pack();
        f.setVisible(true);
    }

}
