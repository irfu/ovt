/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/Fieldline.java,v $
  Date:      $Date: 2006/06/21 10:53:47 $
  Version:   $Revision: 2.5 $


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

package ovt.datatype;

import ovt.mag.*;
import ovt.util.*;

import vtk.*;

import java.util.Vector;
import java.util.Enumeration;



/** Just nice class ;) */

public class Fieldline implements Cloneable {

public static final int ELEMENTS_IN_POINT = 4;

/** Field line's mjd */
protected double mjd = -1;

/** Collection of MagPoints */
protected Vector points;

/** Field line's length */
protected Vector lengthCollection;

/** Creates a new fieldline */
public Fieldline(double mjd) {
	this.mjd = mjd;
        points = new Vector();
        lengthCollection = new Vector();
}

/** Creates a new fieldline with the initial capacity <CODE>initialCapacity</CODE>*/
public Fieldline(double mjd, int initialCapacity) {
	this.mjd = mjd;
        points = new Vector(initialCapacity);
        lengthCollection = new Vector(initialCapacity);
}


/** Returns x, y, z and scalar 
 * @see #getPoint(int) #get
 */
public void add(MagPoint point, double len) { 
	points.addElement(point); 
	lengthCollection.addElement(new Double(len));
}

/** Returns x, y, z and scalar 
 * @see #point(int) #length(int)
 */
public void add(MagPoint point) { 
	points.addElement(point);
        lengthCollection.addElement(new Double(-1));
}

/** Adds All fl points to this fieldline. Doesn't clone them!
 * @see #point(int) #length(int)
 */
public void add(Fieldline fl) { 
	points.addAll(fl.points);
        lengthCollection.addAll(fl.lengthCollection);
}

/*public void addPoint(double x, double y, double z) 
	{ addPoint(x, y, z, 0); }

public void addPoint(double x, double y, double z, double scalar) {
	double [] point = new double[ELEMENTS_IN_POINT];
	point[0] = x;
	point[1] = y;
	point[2] = z;
	point[3] = scalar;
	addPoint(point);
}


public void insertPoint(int index, double point[]) 
	{ insertElementAt(point, index); }

public void insertPoint(int index, double x, double y, double z, double scalar) {
	double [] point = new double[ELEMENTS_IN_POINT];
	point[0] = x;
	point[1] = y;
	point[2] = z;
	point[3] = scalar;
	insertPoint(index, point);
}
*/	

public MagPoint point(int index) 
	{ return (MagPoint)(points.elementAt(index)); }

public MagPoint firstPoint()
	{ return (MagPoint)(points.firstElement()); }

public MagPoint lastPoint()
	{ return point(points.size() - 1); }

/** Returns mag point of a minimal magnetic field of the field line or null. */
public MagPoint getBMinPoint() {
	MagPoint res = null;
	Enumeration e = points.elements();
	while (e.hasMoreElements()) {
		MagPoint mp = (MagPoint)e.nextElement();
		if (res == null) res = mp;
		if (Vect.absv2(mp.bv) < Vect.absv2(res.bv)) res = mp;
		
	}
	return res; 
}

/** Returns mag point of a maximum magnetic field of the field line or null. */
public MagPoint getBMaxPoint() {
	MagPoint res = null;
	Enumeration e = points.elements();
	while (e.hasMoreElements()) {
		MagPoint mp = (MagPoint)e.nextElement();
		if (res == null) res = mp;
		if (Vect.absv2(mp.bv) > Vect.absv2(res.bv)) res = mp;
		
	}
	return res; 
}

public double [] bv(int index) 
	{ return point(index).bv; }

public double length(int index) 
	{ return ((Double)(lengthCollection.elementAt(index))).doubleValue(); }

public double length()
	{ return ((Double)(lengthCollection.lastElement())).doubleValue(); }

public int size()
	{ return points.size(); }
/** Get vtkPoints in Coordinate System CS.
 * @param coordSystem CoordinateSystem
 */
public vtkPoints getVTKPoints() {
	vtkPoints vtkpoints = new vtkPoints();
	Enumeration e = points.elements();
	double[] a;
	while (e.hasMoreElements()) {
		a = ((MagPoint)(e.nextElement())).gsm;
		vtkpoints.InsertNextPoint(a[0],a[1],a[2]);
	}
	return vtkpoints;
}

public vtkPolyData getVTKPolyData() {
	vtkPolyData profile = new vtkPolyData();
	vtkCellArray lines = new vtkCellArray();
	lines.InsertNextCell(size());
	for(int i=0; i<size(); i++) 
		lines.InsertCellPoint(i);
	profile.SetPoints(getVTKPoints());
	profile.SetLines(lines);
	return profile;
}


public double getMjd()
	{ return mjd; }

public void removeLastPoint() {
	if (points.size() > 0) points.removeElementAt(points.size() - 1);
	if (lengthCollection.size() > 0) lengthCollection.removeElementAt(lengthCollection.size() - 1);
}

public void removeAllElements() {
	points.removeAllElements();
	lengthCollection.removeAllElements();
}

/** Changes points: x = i*x, y = j*y, z = k*z */
public void symetrize(int i, int j, int k, MagProps magProps) {
	double[] gsm;
        MagPoint mp;
	Enumeration e = points.elements();
	while (e.hasMoreElements()) {
		mp = (MagPoint)(e.nextElement());
                gsm = mp.gsm;
		gsm[0] = i*gsm[0];
		gsm[1] = j*gsm[1];
		gsm[2] = k*gsm[2];
                mp.bv = magProps.bv(gsm, getMjd());
	}
}

  /** Returns enumeration of MagPoints */
  public Enumeration elements()
	{ return points.elements(); }

  /** returns point. First point index=0*/
  public MagPoint get(int index) {
    return (MagPoint)points.get(index);
  }      

  /** Cloning, you know, MAN :) */
  public Object clone() {
    Fieldline fl = new Fieldline(getMjd());
	
    Object[] newPoints = Vect.toArray(points);
    Object[] newLengthCollection = Vect.toArray(lengthCollection);
    // just for shure
    int size = (newPoints.length > newLengthCollection.length) ? newLengthCollection.length : newPoints.length;
    
    MagPoint mp;
    double len;
    for (int i=0; i<size; i++) {
      mp = (MagPoint)(((MagPoint)newPoints[i]).clone());
      len = ((Double)(newLengthCollection[i])).doubleValue();
      fl.add(mp, len);
    }
    return fl;
}

public static void main(String arg[]) {

/*	FieldLine fl= new FieldLine(10);
	double[] p = {1, 2, 3};
	for (int i=0; i<10; i++) {
		p = new double[3];
		p[0] = i; p[1] = 2*i; p[2] = 3*i;
		fl.addPoint(p, (double)i);
		
	}
	
	FieldLine newfl = (FieldLine)fl.clone();
	newfl.symetrize(1, -1, 1);
	
	for (int i=0; i<newfl.size(); i++)
		System.out.println("new: ["+i+"] "+
			newfl.point(i)[0]+"\t"+newfl.point(i)[1]+"\t"+newfl.point(i)[2]);
	
	for (int i=0; i<fl.size(); i++)
		System.out.println("old: ["+i+"] "+
			fl.point(i)[0]+"\t"+fl.point(i)[1]+"\t"+fl.point(i)[2]);
	
	for (int i=0; i<fl.size(); i++)
		System.out.println("fp: ["+i+"] "+
			fl.point(i)[0]+"\t"+fl.point(i)[1]+"\t"+fl.point(i)[2]);*/
	/*Vector a = new Vector();
	a.addElement(new Integer(5));
	Vector clone = (Vector)a.clone();
	clone.firstElement()*/
}



}
