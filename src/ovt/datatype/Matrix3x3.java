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

OVT Team (http://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
Khotyaintsev

=========================================================================*/

/*
 * Matrix.java
 *
 * Created on March 23, 2000, 8:15 PM
 */
 
package ovt.datatype;

import vtk.*;

/** 
 *
 * @author  root
 * @version 
 */
public class Matrix3x3 {
/*
  public static final double[][] SINGLE_MATRIX = { {1, 0, 0}, 
                                                      {0, 1, 0},
                                                      {0, 0, 1} };*/
  protected double[][] matrix = { {1, 0, 0}, {0, 1, 0}, {0, 0, 1} };
  
  /** Creates new Matrix */
  public Matrix3x3() {
  }
  
  /** Creates new Matrix from double[3][3] */
  public Matrix3x3(double[][] matrix) {
    set(matrix);
  }
  
  /** Creates new Matrix from Matrix3x3 */
  public Matrix3x3(Matrix3x3 matrix) {
    int i, j;
    for (i=0; i<3; i++)
      for (j=0; j<3; j++)
        set(i, j, matrix.get(i,j));
  }
  
  /** returns vtkMatrix4x4 from double[3][3] */
  public static vtkMatrix4x4 getVTKMatrix(double[][] matrix) {
    vtkMatrix4x4 m = new vtkMatrix4x4();
    int i, j;
    for (i=0; i<3; i++)
      for (j=0; j<3; j++)
        m.SetElement(i, j, matrix[i][j]);
    return m;
  }
  
  /** returns vtkMatrix4x4  */
  public vtkMatrix4x4 getVTKMatrix() {
    return getVTKMatrix(matrix);
  }
  
  public void set(double[][] matrix) {
    int i, j;
    for (i=0; i<3; i++)
      for (j=0; j<3; j++)
        set(i, j, matrix[i][j]);
  }
  
  public static double[][] getSingleMatrix() {
    return new double[][]{ {1, 0, 0}, {0, 1, 0}, {0, 0, 1} };
  }
  
  public double get(int i, int j) {
    return matrix[i][j];
  }
  
  public void set(int i, int j, double value) {
    matrix[i][j] = value;
  }
  
  
  /** As we deal only with ortogonal transformation matrixes, we just transpose them!!! */
  public void invert() {
    transpose();
  }

  public void transpose() {
    int i, j;
    double temp;
    for (i=0; i<3; i++)
      for (j=i+1; j<3; j++) {
        temp = get(i,j);
        set(i, j, get(j,i));
        set(j, i, temp);
      }
  }

  
  public Matrix3x3 getInverse() {
    Matrix3x3 newMatrix = new Matrix3x3(this);
    newMatrix.invert();
    return newMatrix;
  }
  
  /** Multyply matrix by a scalar. The result is a vector. 
   * @returns vector 
   */
  public void multiply(double scalar) {
    //System.out.println("Multyplying by" + scalar);
    double value;
    for(int i=0; i<3; i++)
      for(int j=0; j<3; j++) {
        value = get(i,j);
        set(i,j,value*scalar);
      }
  }
  
  /** Multyply matrix by vector. The result is a vector. 
   * @returns vector 
   */
  public double[] multiply(double[] vector) {    
    double[] res = new double[3];
    for(int i=0; i<3; i++)
      res[i] = get(i,0)*vector[0] + get(i,1)*vector[1] + get(i,2)*vector[2];
    return res;
  }
  
  /** Multyply matrix by matrix 3x3. The result is a Matrix3x3. 
   * @returns vector 
   */
  public Matrix3x3 multiply(Matrix3x3 matrix) {
    Matrix3x3 res = new Matrix3x3();
    double value;
    for(int i=0; i<3; i++)
      for(int j=0; j<3; j++) {
       value = get(i,0)*matrix.get(0,j) + get(i,1)*matrix.get(1,j) + get(i,2)*matrix.get(2,j);
       res.set(i, j, value);
    }
    return res;
  }
  
  public void normalize() {
    double det = getDeterminant();
    multiply(1./Math.pow(det, 1./3));
  }
  
  public double getDeterminant() {
    return get(0,0)*get(1,1)*get(2,2) +
            get(0,1)*get(1,2)*get(2,0) + 
            get(1,0)*get(2,1)*get(0,2) -
            get(0,2)*get(1,1)*get(2,0) - 
            get(0,1)*get(1,0)*get(2,2) -
            get(1,2)*get(2,1)*get(0,0);
  }
  
  /*
  public double[][] getArray() {
    return matrix;
  }*/
  
  public String toString() {
    String res = "";
    for (int i=0; i<3; i++) {
      for (int j=0; j<3; j++)
        res += get(i,j)+"\t";
      res += "\n";
    }
    res+="Det = "+getDeterminant();
    return res;
  }
  
  public Object clone() {
    Matrix3x3 newMatrix = new Matrix3x3(this);
    return newMatrix;
  }
  
  public static void main(String[] args) {
    Matrix3x3 m = new Matrix3x3();
    m.set(0,0,2);
    m.set(1,1,2);
    m.set(2,2,2);
    m.normalize();
    System.out.println("determinant="+m.getDeterminant());
  }
  
}
