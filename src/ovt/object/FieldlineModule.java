/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/FieldlineModule.java,v $
  Date:      $Date: 2006/03/21 12:14:57 $
  Version:   $Revision: 2.7 $
 
 
Copyright (c) 2000-2003 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev,
Yuri Khotyaintsev)
All rights reserved.
 
Redistribution and use in source and binary forms, with or without
modification is permitted provided that the following conditions are met:
 
 * No part of the software can be included in any commercial package without
wri{{       tten consent from the OVT team.
 
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

package ovt.object;

import ovt.*;
import ovt.util.*;
import ovt.mag.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.datatype.*;
import ovt.interfaces.*;
import ovt.model.bowshock.*;

import vtk.*;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.beans.*;


/**
 * Visualizes fieldlines starting from the satellite's orbit. 
 * 
 * @author Mykola Khotyaintsev
 * @see ovt.object.Sat#getMainFieldlineModule()
 */
public class FieldlineModule extends AbstractVisualSatModule
implements MagPropsChangeListener {

    
    public static final double LENGH_OF_IMF_LINE = 300;
    
    public static final int FL_2_EARTH   =  0;
    public static final int FL_2_EQUATOR  =  1;
    
    protected int fl_type = FL_2_EARTH;
    
    private MainFieldlineModule mainModule;
    
    protected vtkActor actor = null;
    
    private Hashtable actors = new Hashtable();
    private Hashtable actors_to_show = new Hashtable();
    private double actorsMjd;
    
    
    
/** Create a FieldLinesSatModule */
    
    public FieldlineModule(MainFieldlineModule mainModule, int fl_type) {
        super(mainModule.getSat(), "error name");
        this.fl_type = fl_type;
        switch (fl_type) { 
            case FL_2_EARTH   :   name = "toEarth"; break;
            case FL_2_EQUATOR :   name = "toEquator"; break;
            default: throw new IllegalArgumentException("Wrong FL type : "+fl_type);
        }
        setName(name);
        try {
          setIcon(new ImageIcon(Utils.findResource("images/fieldline"+name+".gif")));
        } catch (java.io.FileNotFoundException e2) { e2.printStackTrace(System.err); }
        setParent(mainModule);
        this.mainModule = mainModule;
    }
    
    
    
  /** Returns the field line type.
   * @return Field line type. {@see ovt.object.Sat}
   */
    public int getFLType() {
        return fl_type;
    }
    
    protected Fieldline getFieldline(int type, double mjd) {
        return getSat().getFieldline(type, mjd);
    }
    
    protected vtkActor getActor() {
        return getActor(getMjd());
    }
    
    private vtkActor getActor(double mjd) {
        if (!isValid()) validate();
        // look in hashtable
        vtkActor act = (vtkActor)actors.get(new Double(mjd));
        if (act == null)  { // nothing found in hashtable
	    if (isInSolarWind()) { // sat is in solar wind
	        act = makeIMFLineActor();
	    } else { // sat is in magnetosphere/magnetosheath
                Fieldline fl = getFieldline(getFLType(), getMjd());
                if (fl == null) {
                    System.out.println("fl = null");
                    return null;
                }
		//System.out.println(" size = "+fl.size());
                act = ActorUtils.getActor(fl);
	    }                
            actors.put(new Double(mjd), act);
        }
        return act;
    }
    
    public void show() {
        if (!isValid()) validate();
        if (!keep()  ||  getCS() != CoordinateSystem.GSM) actors_to_show.clear();
        actor = getActor(getMjd()); //  the line for this mjd
        if (actor != null) actors_to_show.put(new Double(getMjd()), actor);
        updateToCurrentCS();
        Enumeration e = actors_to_show.elements();
        while (e.hasMoreElements())
            getRenderer().AddActor((vtkActor)e.nextElement());
    }
    
    public void hide() {
        Enumeration e = actors_to_show.elements();
        while (e.hasMoreElements())
            getRenderer().RemoveActor((vtkActor)e.nextElement());
        
    }
    
    public void updateToCurrentCS() {
        //System.out.println("Update to current c.s.s.");
        Matrix3x3 trm = getTrans(getMjd()).gsm_trans_matrix(getCS());
        Enumeration e = actors_to_show.elements();
        while (e.hasMoreElements())
            ((vtkActor)e.nextElement()).SetUserMatrix(trm.getVTKMatrix());
    }
    
    public void validate() {
        actors.clear();
        actors_to_show.clear();
        valid = true;
    }
    
    public void timeChanged(TimeEvent evt) {
        if (evt.timeSetChanged()) {
            invalidate();
            //System.out.println("FTIME CHANGE EVENTT!!!");
        }
        
        if (isVisible()) {
            hide();
            show();
        }
    }
    
    public void setVisible(boolean visible) {
        if (isVisible() != visible) {
            if (visible) show();
            else hide();
            super.setVisible(visible);
        }
    }
    
    public void coordinateSystemChanged(CoordinateSystemEvent evt) {
        if (evt.getWindow() == Const.XYZ) {
            if (isVisible()) {
                hide();
                show();
            }
        }
    }
    
    
    public void magPropsChanged(MagPropsEvent evt) {
        invalidate();
        if (isVisible()) {
            hide();
            show();
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("keep")) {
            boolean keep = ((Boolean)evt.getNewValue()).booleanValue();
            if (!keep)
                if (isVisible()) {
                    hide();
                    show();
                }
        }
        super.propertyChange(evt);
    }
    
    private boolean keep() {
        
        return mainModule.isKeep();
    }

/** Create IMF field line actor  */    
private vtkActor makeIMFLineActor() {
    	vtkPolyData profile = new vtkPolyData();
        vtkCellArray lines = new vtkCellArray();
        
        vtkPoints points = new vtkPoints();
        // insert two points
        points.InsertNextPoint(0, 0, 0);
        points.InsertNextPoint(1, 0, 0);
      
       	lines.InsertNextCell(2);
	lines.InsertCellPoint(0);
	lines.InsertCellPoint(1);
	
	profile.SetPoints(points);
	profile.SetLines(lines);
	
	vtkPolyDataMapper mapper = new vtkPolyDataMapper();
	mapper.SetInputData(profile);
        
        vtkActor actor = new vtkActor();
        actor.SetMapper(mapper);
	
	// typical IMF is 5-10nT, which is much smaller then magnetospheric
	// magnetic field => we should colour it in blue
        actor.GetProperty().SetColor(0, 0, 1.); 


    double[] imf = getMagProps().getIMF(getMjd());
    double swp = getMagProps().getSWP(getMjd());        
    double machNumber = getMagProps().getMachNumber(getMjd());
    
    double[] gsm1 = getPositionGSM();
    double[] gsm2 = null; // null for compiler not to complain
    
    double dist_bs = Bowshock99Model.getDistanceToBowshockAlongIMF(gsm1, imf, swp, machNumber);
    
    
    if (Double.isNaN(dist_bs)) { // imf does not intersect with bow shock
    	// to be implemented here :
	// project imf on the direction to bow shock
	// and find out the sign of "dist_bs"
	if (Vect.angleOf2vect(imf, gsm1) > Math.PI/2) // imf points towards the Earth
	  dist_bs = LENGH_OF_IMF_LINE;
	else
	  dist_bs = -1*LENGH_OF_IMF_LINE;
    } 
    
    if (fl_type == FL_2_EARTH) { // imf line towards bow shock
        gsm2 = Vect.add(gsm1, Vect.multiply(Vect.norm(imf), dist_bs));
    } else { 
        gsm2 = Vect.add(gsm1, Vect.multiply(Vect.norm(imf), -1*Utils.sign(dist_bs)*LENGH_OF_IMF_LINE));
    }
    
    points.SetPoint(0, gsm1[0], gsm1[1], gsm1[2]);
    points.SetPoint(1, gsm2[0], gsm2[1], gsm2[2]);
    
    return actor;
}
    
}
