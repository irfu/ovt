/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /stor/devel/ovt2g/ovt/datatype/Matrix3x3.java,v $
 Date:      $Date: 2003/09/28 17:52:37 $
 Version:   $Revision: 2.3 $


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

 OVT Team (https://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
 Khotyaintsev

 =========================================================================*/

/*
 * Matrix.java
 *
 * Created on March 23, 2000, 8:15 PM
 */
package ovt.datatype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ovt.util.Vect;
import vtk.*;

/**
 *
 * @author root
 * @version
 */
public class Matrix3x3 {
    /*
     public static final double[][] SINGLE_MATRIX = { {1, 0, 0}, 
     {0, 1, 0},
     {0, 0, 1} };*/

    public static final Matrix3x3 IDENTITY_MATRIX = new Matrix3x3(new double[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 1}});

    /* Matrices used for constructing rotation matrices. (A base for the SO(3) Lie algebra.) */
    private static final Matrix3x3 L_x = new Matrix3x3(new double[][]{{0, 0, 0}, {0, 0, -1}, {0, 1, 0}});
    private static final Matrix3x3 L_y = new Matrix3x3(new double[][]{{0, 0, 1}, {0, 0, 0}, {-1, 0, 0}});
    private static final Matrix3x3 L_z = new Matrix3x3(new double[][]{{0, -1, 0}, {1, 0, 0}, {0, 0, 0}});

    protected double[][] matrix = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};


    /**
     * Creates new Matrix
     */
    public Matrix3x3() {
    }


    /**
     * Creates new Matrix from double[3][3]
     */
    public Matrix3x3(double[][] matrix) {
        set(matrix);
    }


    /**
     * Creates new Matrix from Matrix3x3.
     */
    public Matrix3x3(Matrix3x3 matrix) {
        int i, j;
        for (i = 0; i < 3; i++) {
            for (j = 0; j < 3; j++) {
                set(i, j, matrix.get(i, j));
            }
        }
    }


    /**
     * returns vtkMatrix4x4 from double[3][3]
     */
    public static vtkMatrix4x4 getVTKMatrix(double[][] matrix) {
        vtkMatrix4x4 m = new vtkMatrix4x4();
        int i, j;
        for (i = 0; i < 3; i++) {
            for (j = 0; j < 3; j++) {
                m.SetElement(i, j, matrix[i][j]);
            }
        }
        return m;
    }


    /**
     * returns vtkMatrix4x4
     */
    public vtkMatrix4x4 getVTKMatrix() {
        return getVTKMatrix(matrix);
    }


    public void set(double[][] matrix) {
        int i, j;
        for (i = 0; i < 3; i++) {
            for (j = 0; j < 3; j++) {
                set(i, j, matrix[i][j]);
            }
        }
    }


    public static double[][] getSingleMatrix() {
        return new double[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
    }


    public double get(int i, int j) {
        return matrix[i][j];
    }


    /**
     * @param i Row
     * @param j Column
     */
    public void set(int i, int j, double value) {
        matrix[i][j] = value;
    }


    /**
     * IMPORTANT NOTE: As we only deal with orthogonal transformation matrices,
     * the inverse is identical to the transpose the matrix. Therefore only
     * works for orthogonal matrices.
     */
    public void invert() {
        transpose();
    }


    public void transpose() {
        int i, j;
        double temp;
        for (i = 0; i < 3; i++) {
            for (j = i + 1; j < 3; j++) {
                temp = get(i, j);
                set(i, j, get(j, i));
                set(j, i, temp);
            }
        }
    }


    public Matrix3x3 getInverse() {
        Matrix3x3 newMatrix = new Matrix3x3(this);
        newMatrix.invert();
        return newMatrix;
    }


    /**
     * Multiply matrix by a scalar. The result is a vector.
     *
     * @return vector
     */
    public void multiply(double scalar) {
        double value;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                value = get(i, j);
                set(i, j, value * scalar);
            }
        }
    }


    /**
     * Multiply matrix by vector. The result is a vector.
     *
     * @return vector
     */
    public double[] multiply(double[] vector) {
        double[] res = new double[3];
        for (int i = 0; i < 3; i++) {
            res[i] = get(i, 0) * vector[0] + get(i, 1) * vector[1] + get(i, 2) * vector[2];
        }
        return res;
    }


    /**
     * Multiply matrix by matrix.
     *
     * @return New instance of Matrix3x3.
     */
    public Matrix3x3 multiply(Matrix3x3 matrix) {
        /* NOTE: If matrix indices are interpreted as get(iRow, iCol), then
           A.multiply(B) is equivalent to matrix multiplication A*B (not B*A).
        */
        Matrix3x3 res = new Matrix3x3();
        double value;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                value = get(i, 0) * matrix.get(0, j) +
                        get(i, 1) * matrix.get(1, j) +
                        get(i, 2) * matrix.get(2, j);
                res.set(i, j, value);
            }
        }
        return res;
    }


    /**
     * Add this matrix and another matrix.
     *
     * @return New instance of Matrix3x3.
     */
    public Matrix3x3 add(Matrix3x3 A) {
        final Matrix3x3 sum = new Matrix3x3();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                sum.set(i, j, get(i, j) + A.get(i, j));   // Overwrite old values.
            }
        }
        return sum;
    }


    /**
     * Exponentiate matrix (e^A). This is useful for constructing a rotation
     * matrix around an arbitrary axis.
     *
     * Algorithm: e^A = (1+A/n)^n, where n--> +infinity. Uses n=2^N with N
     * iterations in code since this is easy to implement.
     *
     * @param N Number of iterations to use. More accurate result for higher
     * value. Should _probably_ be chosen such that 2^N >> |det(matrix)| as a
     * necessary requirement, but it is not certain whether this is also
     * sufficient. Has to be a non-negative number.
     * @return New instance of Matrix3x3.
     *
     * @author Erik P G Johansson, erik.johansson@irfu.se
     *
     * Created 2015-09-10.
     */
    public Matrix3x3 exponentiate(int N) {
        if (N < 0) {
            throw new IllegalArgumentException();
        }

        final double twoPowerN = Math.pow(2, N);
        final Matrix3x3 A = new Matrix3x3(this);  // Copy argument to avoid modifying it. Matrix3x3#multiply(double) modifies the matrix in-place later.
        A.multiply(1.0d / twoPowerN);
        Matrix3x3 C = IDENTITY_MATRIX.add(A);

        for (int i = 1; i <= N; i++) {
            // C_0 = Matrix before first iteration
            // C_1 = (C_0)^2 = Matrix after first iteration
            // C_2 = (C_0)^(2*2)
            // C_3 = (C_0)^(2*2*2)
            // And so on ==> 
            // C_i = ... = (C_0)^(2^i) = Matrix after i iterations.
            C = C.multiply(C);
        }

        return C;
    }


    /**
     * Get 3x3 rotation matrix for rotation around an arbitrary axis.
     *
     * @param r Axis around which to rotate. The length of the vector determines
     * the angle of a righthand rotation in radians (in a righthanded coordinate
     * system).
     *
     * @return A new instance of Matrix3x3, "R". If v and v' are vectors where
     * R*v=v', then v' will be a rotated version v.
     *
     * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
     * @since 2015-09-10.
     */
    // PROPOSAL: Add/subtract 2*pi to length of rotation vector to reduce the length of the rotation vector. ==> Reduce number of iterations.
    public static Matrix3x3 getRotationMatrix(double[] r) {
        if (r.length != 3) {
            throw new IllegalArgumentException();
        }

        final Matrix3x3 A_x = new Matrix3x3(L_x);
        final Matrix3x3 A_y = new Matrix3x3(L_y);
        final Matrix3x3 A_z = new Matrix3x3(L_z);
        A_x.multiply(r[0]);
        A_y.multiply(r[1]);
        A_z.multiply(r[2]);
        final Matrix3x3 C = A_x.add(A_y).add(A_z);

        // Estimate number of iterations to use for exponentiation.
        // --------------------------------------------------------
        // Use magnitude of rotation angle instead of matrix.
        // Using natural logarithm instead of 2-log introduces a factor ~1.
        // Using abs^2 introduces a factor ~2.
        final double estimate = Math.max(0.0, Math.log(Vect.absv2(r)));
        final int N = 50 + 50 * ((int) estimate);

        return C.exponentiate(N);
    }


    public void normalize() {
        double det = getDeterminant();
        multiply(1. / Math.pow(det, 1. / 3));
    }


    public double getDeterminant() {
        return get(0, 0) * get(1, 1) * get(2, 2)
                + get(0, 1) * get(1, 2) * get(2, 0)
                + get(1, 0) * get(2, 1) * get(0, 2)
                - get(0, 2) * get(1, 1) * get(2, 0)
                - get(0, 1) * get(1, 0) * get(2, 2)
                - get(1, 2) * get(2, 1) * get(0, 0);
    }


    /**
     * Mostly intended for debugging, or maybe for whether to throw exception.
     *
     * @return True iff exactly all of the matrix components are finite.
     */
    public boolean isFinite() {
        for (int i = 0; i < matrix.length; i++) {
            final double[] vector = matrix[i];
            for (int j = 0; j < vector.length; j++) {
                if (!Double.isFinite(vector[j])) {
                    return false;
                }
            }
        }
        return true;
    }

    /*
     public double[][] getArray() {
     return matrix;
     }*/

    public String toString() {
        String res = "";
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                res += get(i, j) + "\t";
            }
            res += "\n";
        }
        res += "Det = " + getDeterminant();
        return res;
    }


    public Object clone() {
        Matrix3x3 newMatrix = new Matrix3x3(this);
        return newMatrix;
    }

    //##########################################################################

    /**
     * Informal test code.
     */
    public static void main(String[] args) {
        /*Matrix3x3 m = new Matrix3x3();
         m.set(0,0,2);
         m.set(1,1,2);
         m.set(2,2,2);
         m.normalize();
         System.out.println("determinant="+m.getDeterminant());*/
        test_getRotationMatrix();
    }


    /**
     * Manual test code for getRotationMatrix.
     */
    private static void test_getRotationMatrix() {
        // Test inversion? (Multiply matrices for positive and negative rotation.)
        final List<double[]> r_list = new ArrayList();
        {
            //r_list.add(new double[]{Math.PI * 2, 0, 0});
            //r_list.add(new double[]{0, Math.PI * 2, 0});
            //r_list.add(new double[]{0, 0, Math.PI * 2});
        }
        {
            double angle = 0.1;
            r_list.add(new double[]{angle, 0, 0});
            r_list.add(new double[]{0, angle, 0});
            r_list.add(new double[]{0, 0, angle});
        }

        final double[] xbase = {1, 0, 0};
        final double[] ybase = {0, 1, 0};
        final double[] zbase = {0, 0, 1};

        for (double[] r : r_list) {
            Matrix3x3 R = getRotationMatrix(r);
            System.out.println("----");
            System.out.println("r = " + Arrays.toString(r));
            System.out.println("R = \n" + R);
            System.out.println("R*xbase = " + Arrays.toString(R.multiply(xbase)));
            System.out.println("R*ybase = " + Arrays.toString(R.multiply(ybase)));
            System.out.println("R*zbase = " + Arrays.toString(R.multiply(zbase)));
        }
    }

}
