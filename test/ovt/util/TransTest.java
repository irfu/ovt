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

import org.junit.Test;
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



    public boolean matricesEqual(Matrix3x3 M1, Matrix3x3 M2, double epsilon) {
        double diff = matricesMaxDiff(M1, M2);
        return diff <= epsilon;
    };



    // Test method trans_matrix().
    @Test
    public void test_trans_matrix() {
        final double EPSILON = 1.0e-15;
        System.out.println("EPSILON = " + EPSILON);

        final XYZWindow XYZwin = new XYZWindow();
        XYZwin.start();

        /**
         * IMPLEMENTATION NOTE: OVTCore:Initialize() calls System.setErr and
         * System.setOut which redirect messages to log files. These commands
         * reset them so that stdout and stderr can be used for logging the test
         * code.
         */
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));

        final double mjd = ovt.datatype.Time.Y2000;

        final int[] CS_ARRAY = {
            CoordinateSystem.GEI,
            CoordinateSystem.GSM,
            CoordinateSystem.GEO,
            CoordinateSystem.GSE,
            CoordinateSystem.SM
        };

        OVTCore core = XYZwin.getCore();
        MagProps magProps = new MagProps(core);
        IgrfModel igrf = new IgrfModel(magProps);
        Trans T = new ovt.util.Trans(mjd, igrf);


        // For testing a specific case, e.g. to debug failed tests.
        if (false) {
            int cs1 = 0;
            int cs2 = 1;
            int cs3 = 4;
            Matrix3x3 M12 = T.trans_matrix(cs1, cs2);
            Matrix3x3 M23 = T.trans_matrix(cs2, cs3);
            Matrix3x3 M31 = T.trans_matrix(cs3, cs1);
            Matrix3x3 M = M31.multiply(M23).multiply(M12);

            boolean success = matricesEqual(M, Matrix3x3.IDENTITY_MATRIX, EPSILON);
            if (!success) {
                System.out.println("Too large diff: ("+cs1+", "+cs2+", "+cs3+")");
            }
        }



        /* Transform CS --> same CS
         */
        for (int cs1: CS_ARRAY) {
            Matrix3x3 M = T.trans_matrix(cs1, cs1);

            // ASSERT: Always return identity.
            assertTrue(matricesEqual(M, Matrix3x3.IDENTITY_MATRIX, EPSILON));
        }

        /* Transform CS_1 --> CS_2 --> CS_1
         */
        for (int cs1: CS_ARRAY) {
            for (int cs2: CS_ARRAY) {
                Matrix3x3 M12 = T.trans_matrix(cs1, cs2);
                Matrix3x3 M21 = T.trans_matrix(cs2, cs1);
                Matrix3x3 M = M12.multiply(M21);

                // ASSERT: Inverse = Transpose
                assertTrue(matricesEqual(M12, M21.getInverse(), EPSILON));
                // ASSERT: Pair of mutually inverse matrices.
                assertTrue(matricesEqual(M, Matrix3x3.IDENTITY_MATRIX, EPSILON));
                // ASSERT: No mirroring = Preserve handedness
                assertTrue(Math.abs(M12.getDeterminant() - 1) < EPSILON);
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

                    // ASSERT: "Circle" of transformations should yield unity matrix.
                    boolean success = matricesEqual(M, Matrix3x3.IDENTITY_MATRIX, EPSILON);
                    if (!success) {
                        System.out.println("Too large diff: ("+cs1+", "+cs2+", "+cs3+")");
                    }
                    assertTrue(success);
                }
            }
        }
    }

}
