/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/ElectPot.java,v $
  Date:      $Date: 2003/09/28 17:52:47 $
  Version:   $Revision: 2.8 $


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
 * GjElectPot.java
 * by Grzegorz Juchnikowski
 * based on Magnetopause.java
 * generates vtk actor ilustrating electrical potential over polar regions
 * according to the model Weimer96.
 *
 */

package ovt.object;

import ovt.*;
import ovt.gui.*;
import ovt.mag.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.object.editor.*;
import ovt.datatype.*;
import ovt.interfaces.*;
import ovt.model.Weimer96;
import ovt.util.*;

import vtk.*;

import java.beans.*;
import java.lang.Math;
import java.util.*;
import java.io.*;

import java.awt.event.*;

import javax.swing.*;

public class ElectPot extends VisualObject implements
    TimeChangeListener, CoordinateSystemChangeListener,
    MagPropsChangeListener, MenuItemsSource {

  public static final int SURFACE = 0;
  public static final int WIREFRAME = 1;

  protected vtkActor actor;
  protected vtkFollower[][] text_actor = new vtkFollower[2][2];
  private int[] activityDependsOn = { MagProps.IMF_Y, MagProps.IMF_Z, MagProps.SW_VELOCITY };
  /** Holds Characteristics of this object */
  private Characteristics characteristics = new Characteristics(-1);

/** Holds value of property representation. */
private int representation;

  private OVTCore ovtCore;

  private Weimer96 w96;
  private CGMC g2c, c2g;

  /** Holds value of property resolution in kV. */
  private int resolution = 4;

public ElectPot(OVTCore core) {
  super(core, "Electrical Potential", "images/elpot.gif");
  ovtCore = core;

  Descriptors descriptors = getDescriptors();
  try {
        BasicPropertyDescriptor pd = new BasicPropertyDescriptor("resolution", this);
        pd.setDisplayName("Resolution");
        pd.setLabel("Resolution");
        ComponentPropertyEditor editor = new TextFieldEditor(pd);
        editor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                    public void editingFinished(GUIPropertyEditorEvent evt) {
                        Render();
                    }
                });
        addPropertyChangeListener("resolution", editor);
        pd.setPropertyEditor(new WindowedPropertyEditor(editor, getCore().getXYZWin(), "OK", true));
        descriptors.put(pd);

  } catch (IntrospectionException e2) {
        e2.printStackTrace();
  }

  w96 = new Weimer96();
  g2c = new CGMC(core,"g2c");
  c2g = new CGMC(core,"c2g");
}


public void show() {
  getActors();
  getRenderer().AddActor(actor);
  for( int ip=0; ip<2; ip++ )
  for( int im=0; im<2; im++ ) getRenderer().AddActor(text_actor[ip][im]);
  rotate();
}

public void hide() {
  getRenderer().RemoveActor(actor);
  for( int ip=0; ip<2; ip++ )
  for( int im=0; im<2; im++ ){
    getRenderer().RemoveActor(text_actor[ip][im]);
  }
}


public void setVisible(boolean visible) {
    if (visible != isVisible()) {
      if (visible) show();
      else hide();
      super.setVisible(visible);
    }
}

private double getUT(double mjd){
  //returns UT as a number in a range [0..24)
  double ut = (mjd-Math.floor(mjd))*24;
//System.out.println("ElectPot: UT="+ut);
  return ut;
}

private double[] xyz2flr(double[] xyz){
  //returns: fi (-90..+90), lambda (0..360), R
  double x = xyz[0], y = xyz[1], z = xyz[2];
  double R = Math.sqrt(x*x + y*y + z*z);
  double Rxy = Math.sqrt(x*x + y*y);
  double fi = (R < 0.001) ? 0 : Math.asin(z/R)*180/Math.PI;
  double lambda = (Rxy < 0.0001) ? 0 : Math.atan2(y,x)*180/Math.PI;
  if( lambda < 0 ) lambda += 360;
  double[] flr = {fi,lambda,R};
  return flr;
}

protected void getActors() {

  if (!isValid()) update();

  if (actor != null) return;

  int X=0, Y=1, Z=2;

	actor = new vtkActor();
	for( int ip=0; ip<2; ip++ )
	for( int im=0; im<2; im++ ) text_actor[ip][im] = new vtkFollower();

	vtkPoints points = new vtkPoints();

        vtkFloatArray scalars = new vtkFloatArray();

	double mjd = getMjd();
      int year = Time.getYear(mjd);
      try {
        g2c.loadYear(year);
        c2g.loadYear(year);
      } catch(IOException e) {
        System.out.println(getClass().getName() + " -> " + e.toString());
        return;  //empty actors
      }
	double UT = getUT(mjd);
        // get parameters
      double[] imf = ovtCore.getMagProps().getIMF(mjd);
      double swVelocity = ovtCore.getMagProps().getSWVelocity(mjd);
      double Tilt = ovtCore.getMagProps().getDipoleTilt(mjd);
      // save characteristics
        characteristics.setMjd(getMjd());
        characteristics.put(MagProps.IMF_Y, imf[Y]);
        characteristics.put(MagProps.IMF_Z, imf[Z]);
        characteristics.put(MagProps.SW_VELOCITY, swVelocity);

      Trans trans = getTrans(mjd);

        double phi, theta, dPhi, dTheta;
	int phiResolution = 60, thetaResolution = 60;
	double x, y, z, r = 1.05;
	dPhi   = 360 / phiResolution;
	dTheta = 180 / thetaResolution;
	double cosTheta, sinTheta, cosPhi, sinPhi;
      int i, j;
      int sizex = phiResolution + 1;
      int sizey = thetaResolution + 1;
      int half_sizey = sizey / 2;

      double[] GNP,GSP;   //geogr. coords of Magnetic North and South Poles
      try{
        double[] CNP = {90, 360}, CSP = {-90, 360};
        GNP = c2g.transform(CNP);
        GSP = c2g.transform(CSP);
      }catch(Exception e){
        System.out.println(getClass().getName() + " -> " + e.toString());
        return;  //empty actors
      }

      //1-st array index selects north/south pole, 2-nd - min/max.
      double[][] EE = {{200,-200},{200,-200}};  //min/max el.pot.
      double[][] TT = new double[2][2];  //theta
      double[][] PP = new double[2][2];  //phi
      int pole_index = 0;

	for (theta=0, i=0; i<sizey; theta+=dTheta, i++) {
        if( i == 0 ){                 // initialize Weimer96 for North Pole
          pole_index = 0;
          double Bz = imf[2];
          double By = imf[1];
          double Bt = Math.sqrt(Bz*Bz + By*By);
          double angle = Math.atan2(By,Bz)*180/Math.PI;
          w96.SetModel(angle, Bt, +Tilt, swVelocity);
        }else if ( i == half_sizey ) {  // initialize Weimer96 for South Pole
          pole_index = 1;
          double Bz = imf[2];
          double By = -imf[1];  //see minus?
          double Bt = Math.sqrt(Bz*Bz + By*By);
          double angle = Math.atan2(By,Bz)*180/Math.PI;
          w96.SetModel(angle, Bt, -Tilt, swVelocity);
/* -tilt is important !
    The southern dipole axis has the opposite direction, so the southern
hemisphere is differently enlightened than the northern one in a given
moment,  and accordingly, the potential distribution will be also
appropriately different.
    The mistake was noted by Daniel Weimer ( on the other occasion of our
work).
 Barbara Popielawska*/
        }
        sinTheta = Math.sin(theta*Math.PI/180);
        cosTheta = Math.cos(theta*Math.PI/180);
	  for (phi=0, j=0; j<sizex; phi+=dPhi, j++) {
          z = r * cosTheta;
          x = r * sinTheta * Math.cos(phi*Math.PI/180);
          y = r * sinTheta * Math.sin(phi*Math.PI/180);
          //Weimer96 electrical potential in points of the polar cap
          double elpot;
          try{
            //GC - geographical spherical coordinates (lat,lon)
            double gsm[] = {x,y,z};
            double geo[] = trans.gsm2geo(gsm);
            double[] GC = xyz2flr(geo);

            //CC - corrected geo-mag coordinates (lat,lon)
            double[] CC = g2c.transform(GC);  //this can throw Exception

            //MLT - magnetic local time [0..24)
            double MLT = CGMC.MLT(CGMC.MLTUT(GC, CC, (i < half_sizey) ? GNP : GSP), UT);
            //MLTUT can throw Exception

            //electrical potential
            elpot = w96.EpotVal(Math.abs(CC[0]), MLT);  //this can throw Exception
          }catch(Exception e){
            elpot = 0;
          }

          //deal with min/max
          if( elpot < EE[pole_index][0] ){
            EE[pole_index][0] = elpot;
            TT[pole_index][0] = theta;
            PP[pole_index][0] = phi;
          }
          if( elpot > EE[pole_index][1] ){
            EE[pole_index][1] = elpot;
            TT[pole_index][1] = theta;
            PP[pole_index][1] = phi;
          }

          points.InsertNextPoint(x,y,z);
          scalars.InsertNextValue(-elpot);
//$$$ No metter how the SetScalarRange() is set, (-,+) or (+,-) it always
//displays negatives in red and positives in blue, which is illogical!
//To change this, InsertNextScalar() gets -elpot instead of +elpot..

	  }
	}


	vtkStructuredGrid sgrid = new vtkStructuredGrid();
			sgrid.SetDimensions(sizex, sizey,1);
			sgrid.SetPoints(points);
                  sgrid.GetPointData().SetScalars(scalars);

      vtkContourFilter gfilter2 = new vtkContourFilter();
      gfilter2.SetInputData(sgrid /*gfilter1.GetOutput()*/);//FKJN 8/5 2015 changed all AddInputData & SetInputData to ***InputConnection
      //gfilter2.SetInputConnection(sgrid /*gfilter1.GetOutputPort()*/);
      int izolinesNumber = (int)(120 / getResolution());
      for( int i2=0; i2<=(izolinesNumber*2); i2++ )
        gfilter2.SetValue(i2, (i2-izolinesNumber)*resolution);
/*
      double[][] contourValues = {
        {-60,-50,-40,-30,-20,-10,-3,3,10,20,30,40,50,60},   //full range
        {-30,-25,-20,-15,-10,-5,-1,1,5,10,15,20,25,30}      //reduced range
      };
      int rsel = gjElectPotPropertyEditor.rangeSelector;
      for( int i2=0; i2<contourValues[rsel].length; i2++ )
        gfilter2.SetValue(i2,contourValues[rsel][i2]);
*/

	vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		//mapper.SetInputData(gfilter2.GetOutput());//FKJN 8/5 2015 changed all AddInputData & SetInputData to ***InputConnection
    mapper.SetInputConnection(gfilter2.GetOutputPort());
    mapper.SetScalarRange(-2*resolution, 2*resolution /*scalars.GetRange()*/);

		actor.SetMapper(mapper);


	r = 1.2;
	for( int ip=0; ip<2; ip++ )
	for( int im=0; im<2; im++ ){
		z = r * Math.cos(TT[ip][im]*Math.PI/180);
		x = r * Math.sin(TT[ip][im]*Math.PI/180) * Math.cos(PP[ip][im]*Math.PI/180);
		y = r * Math.sin(TT[ip][im]*Math.PI/180) * Math.sin(PP[ip][im]*Math.PI/180);
		vtkVectorText atext = new vtkVectorText();
			atext.SetText(Integer.toString((int)(EE[ip][im]+0.5)));
		vtkPolyDataMapper mapper2 = new vtkPolyDataMapper();
			mapper2.SetInputData(atext.GetOutput());
			text_actor[ip][im].SetMapper(mapper2);
			text_actor[ip][im].SetScale(0.1, 0.1, 0.1);
			text_actor[ip][im].AddPosition(x,y,z);
			text_actor[ip][im].SetCamera(getRenderer().GetActiveCamera());
			text_actor[ip][im].GetProperty().SetColor(0,0,0);
//		actor.AddPart(act2);
	}
}

public void update() {
  actor = null;
  for( int ip=0; ip<2; ip++ )
  for( int im=0; im<2; im++ ) text_actor[ip][im] = null;
  valid = true;
}

public void rotate() {
  Matrix3x3 m3x3 = getTrans(getMjd()).gsm_trans_matrix(getCS());
  getActors();
  actor.SetUserMatrix(m3x3.getVTKMatrix());
  for( int ip=0; ip<2; ip++ )
  for( int im=0; im<2; im++ ) text_actor[ip][im].SetUserMatrix(m3x3.getVTKMatrix());
}




public void timeChanged(TimeEvent evt) {
    invalidate();
    // check if SWP and MachNumber Changed
  Characteristics newCh = getMagProps().getCharacteristics(activityDependsOn, getMjd());
  if (!characteristics.equals(newCh)) {
      invalidate();
      if (isVisible()) {
            hide();
            show();
      }
  } else if (isVisible()){  hide(); show(); }
}
public void coordinateSystemChanged(CoordinateSystemEvent evt) {
  if (isVisible()) rotate();
}
public void magPropsChanged(MagPropsEvent evt) {
  if (evt.whatChanged() == MagProps.SW_VELOCITY  || evt.whatChanged() == MagProps.IMF) {
    // check if SW Velocity or IMF By/Bz changed
      Characteristics newCh = getMagProps().getCharacteristics(activityDependsOn, getMjd());
      if (!characteristics.equals(newCh)) {
        invalidate();
        if (isVisible()) {
            hide();
            show();
        }
      }
  }
}


public JMenuItem[] getMenuItems() {
    JMenu menu = new JMenu("Depends on");
        menu.setFont(Style.getMenuFont());
    JMenuItem item = new JMenuItem("IMF [By, Bz]...");
        item.setFont(Style.getMenuFont());
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                    //getMagProps().activityEditors[MagProps.IMF].setVisible(true);
                    getMagProps().setActivityEditorVisible(MagProps.IMF, true);
            }
        });
        menu.add(item);
    item = new JMenuItem("SW Velocity...");
        item.setFont(Style.getMenuFont());
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                    //getMagProps().activityEditors[MagProps.SW_VELOCITY].setVisible(true);
                    getMagProps().setActivityEditorVisible(MagProps.SW_VELOCITY, true);
            }
        });
        menu.add(item);
    return new JMenuItem[] { menu };
}

/** Add a PropertyChangeListener to the listener list.
 * @param l The listener to add.
 */
public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    propertyChangeSupport.addPropertyChangeListener (l);
}

/** Removes a PropertyChangeListener from the listener list.
 * @param l The listener to remove.
 */
public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    propertyChangeSupport.removePropertyChangeListener (l);
}

/** Getter for property resolution.
 * @return Value of property resolution.
 */
public int getResolution() {
    return resolution;
}

/** Setter for property resolution.
 * @param resolution New value of property resolution.
 */
public void setResolution(int resolution) throws IllegalArgumentException {
    if (resolution < 1) throw new IllegalArgumentException("Resolution cannot be less than 1");
    if (resolution > 20) throw new IllegalArgumentException("Resolution cannot be more than 20");
    int oldResolution = this.resolution;
    if (resolution == oldResolution) return; // nothing' changed
    this.resolution = resolution;
    invalidate();
    if (isVisible()) {
            hide();
            show();
      }
    propertyChangeSupport.firePropertyChange ("resolution", new Integer (oldResolution), new Integer (resolution));
}

}
