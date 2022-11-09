/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/MainFieldlineModule.java,v $
  Date:      $Date: 2006/06/21 10:53:47 $
  Version:   $Revision: 2.7 $
 
 
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
 * MainFieldlineModule.java
 *
 * Created on March 30, 2000, 2:52 PM
 */

package ovt.object;

import java.awt.Color;
import ovt.*;
import ovt.beans.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.event.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import vtk.*;

import javax.swing.*;
import java.beans.*;
import java.util.Enumeration;

/**
 *
 * @author  ko
 * @version
 */
public class MainFieldlineModule extends AbstractSatModule
implements MagPropsChangeListener {
    
    Characteristics characteristics = new Characteristics();
 /** Hashtable (mjd, FieldLine) The first element is a hashtable
  * of FL_2_EARTH, the second - FL_2_EQUATOR.
  */
    protected FieldlineCollection[] fieldLines = new FieldlineCollection[2];
    
  /** Holds value of property keep. */
    private boolean keep = false;
    private boolean keep_preffered = true;
    private Color color = Color.blue;
    private boolean scalarcolor = true;
    private FieldlineModule module1;
    private FieldlineModule module2;

    private GUIPropertyEditor keepEditor;
    
  /** Creates new MainFieldlineModule */
    public MainFieldlineModule(Sat sat) {
        super(sat, "Fieldlines", "images/fieldline.gif", true);
        for (int i=0; i<2; i++) fieldLines[i] = new FieldlineCollection();
        
        try {
            BasicPropertyDescriptor pd = new BasicPropertyDescriptor("keep", this);
            pd.setLabel("Keep Fieldlines");
            pd.setDisplayName("Keep Fieldlines");
            keepEditor = new BooleanEditor(pd, MenuPropertyEditor.CHECKBOX);
            //editor.setTags(new String[]{"Dump data", "Hide dumper"});
            addPropertyChangeListener("keep", keepEditor);
            pd.setPropertyEditor(keepEditor);
            getDescriptors().put(pd);

            // FKJN copy from Satellite colour 13May 2015
            pd = new BasicPropertyDescriptor("color", this);
            pd.setLabel("Color...");
            pd.setDisplayName(getParent().getName()+" : "+ getName() +" color");
            ComponentPropertyEditor editor = new ColorPropertyEditor(pd);
            editor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            addPropertyChangeListener("color", editor);
            pd.setPropertyEditor(new WindowedPropertyEditor(editor, getCore().getXYZWin(), "Close"));
            descriptors.put(pd);


            pd = new BasicPropertyDescriptor("scalarcolor", this);
            pd.setLabel("Scalar Colors");
            //pd.setDisplayName("Keep Fieldlines");
            MenuPropertyEditor keepEditor2 = new BooleanEditor(pd, MenuPropertyEditor.CHECKBOX);
            addPropertyChangeListener("scalarcolor", keepEditor2);
            pd.setPropertyEditor(keepEditor2);
            getDescriptors().put(pd);


            
            
        } catch (IntrospectionException e2) {
            System.out.println(getClass().getName() + " -> " + e2.toString());
            System.exit(0);
        }
        
        FieldlineModule module1 = new FieldlineModule(this, FieldlineModule.FL_2_EQUATOR);
        addPropertyChangeListener(module1);
        addChild(module1);
        
        module2 = new FieldlineModule(this, FieldlineModule.FL_2_EARTH);
        addPropertyChangeListener(module2);
        addChild(module2);
    }
    
    public FieldlineCollection getFieldlineCollection(int type) {
        if (!isValid()) update();
        return fieldLines[type];
    }
    
    public Fieldline getFieldline(int type, double mjd) {
        return getFieldlineCollection(type).get(mjd);
    }
    
    /** Returns the point of a maximum magnetic field of fieldlines or null. */
    public MagPoint getBMaxPoint(double mjd) {
    	MagPoint mp1 = getFieldline(FieldlineModule.FL_2_EARTH, mjd).getBMaxPoint();
	MagPoint mp2 = getFieldline(FieldlineModule.FL_2_EQUATOR, mjd).getBMaxPoint();
	MagPoint mp;
	if (mp1 == null) return mp2;
	if (mp2 == null) return mp1;	
	double b1 = Vect.absv2(mp1.bv);
	double b2 = Vect.absv2(mp2.bv);
	return (b1 > b2) ? mp1 : mp2;
    }
    
    /** Returns the point of a minimum magnetic field of fieldlines or null. */
    public MagPoint getBMinPoint(double mjd) {
    	MagPoint mp1 = getFieldline(FieldlineModule.FL_2_EARTH, mjd).getBMinPoint();
	MagPoint mp2 = getFieldline(FieldlineModule.FL_2_EQUATOR, mjd).getBMinPoint();
	MagPoint mp;
	if (mp1 == null) return mp2;
	if (mp2 == null) return mp1;	
	double b1 = Vect.absv2(mp1.bv);
	double b2 = Vect.absv2(mp2.bv);
	return (b1 < b2) ? mp1 : mp2;
    }
    
    protected void update() {
        System.out.print("Computing fieldlines for " + getSat().getName()+" ... ");
        double[] timeMap = getTimeSet().getValues();
        double mjd;
        int i,k;
        Timetable fLines;
        Fieldline[] twoFLines;
        for (k=0; k<2; k++) fieldLines[k].clear();
        
        for (i=0; i<timeMap.length; i++) {
            mjd = timeMap[i];
            //System.out.println(" "+Time.toString(mjd));
            twoFLines = makeFieldlines(mjd);
            for (k=0; k<2; k++) fieldLines[k].put(mjd, twoFLines[k]);
        }
        System.out.println("Done.");
        valid = true;
    }
    
    
 /**
  *
  */
    protected Fieldline[] makeFieldlines(double mjd) {
        Fieldline[] fl;
        double rmin = getMagProps().alt / Const.RE + 1.;
        MagProps magProps = getMagProps();
        
        TrajectoryPoint trp = getTrajectory().get(mjd);
        if (trp == null) System.out.println("NULL TrPoint for "+Time.toString(mjd));
        double[] gsm = trp.get(CoordinateSystem.GSM);
        
        if (gsm == null) System.out.println("NULL POSITION!!!");
        
        Fieldline fl1 = Trace.traceline(magProps, mjd, gsm,  1 * 0.001, 0);
        Fieldline fl2 = Trace.traceline(magProps, mjd, gsm, -1 * 0.001, 0);
        
        boolean first = true; // - first line is FL_2_EARTH
        
        // if two fotprints are close to earth - choose the shirtest line
        if ((Vect.absv(fl1.lastPoint().gsm) < rmin) &&
        (Vect.absv(fl2.lastPoint().gsm) < rmin)) {
            if ((Math.abs(fl1.length()) < Math.abs(fl2.length()))) {
                first = true;
            } else {
                first = false;
            }
            // choose the line, which footprin is close to earth
        } else if (Vect.absv(fl1.lastPoint().gsm) < rmin) {
            first = true;
        } else {
            first = false;
        }
        
        if (first) {
            fl = new Fieldline[]{ fl1, fl2 };
        } else {
            fl = new Fieldline[]{ fl2, fl1 };
        }
        
        return fl;
    }
    
    
    public void magPropsChanged(MagPropsEvent evt) {
  /*Characteristics newCh  = getMagProps().getMagFieldCharacteristics(getMjd());
  if (!newCh.equals(characteristics)) invalidate();*/
        invalidate();
        super.magPropsChanged(evt);
    }
    
    public void timeChanged(TimeEvent evt) {
        if (evt.timeSetChanged()) invalidate();
        super.timeChanged(evt);
    }
    
    public void coordinateSystemChanged(CoordinateSystemEvent evt) {
        if (evt.getWindow() == Const.XYZ) {
            if (evt.getNewCS() == CoordinateSystem.GSM) {
                keepEditor.setEnabled(keep_preffered);
            } else {
                if (evt.getOldCS() == CoordinateSystem.GSM) keep_preffered = isKeep();
                try { setKeep(false); } catch (PropertyVetoException ignore) {}
                keepEditor.setEnabled(false);
            }
        }
        super.coordinateSystemChanged(evt);
    }
    
/** Getter for property keep.
 * @return Value of property keep.
 */
    public boolean isKeep() {
        return keep;
    }
    
/** Setter for property keep.
 * @param keep New value of property keep.
 *
 * @throws PropertyVetoException
 */
    public void setKeep(boolean keep) throws java.beans.PropertyVetoException {
        boolean oldKeep = this.keep;
        if (oldKeep == keep) return; // nothing has changed
        if (getCS() != CoordinateSystem.GSM) throw new PropertyVetoException("Keep fieldlines works only in GSM coordinate system.", null);
        vetoableChangeSupport.fireVetoableChange("keep", new Boolean (oldKeep), new Boolean (keep));
        this.keep = keep;
        propertyChangeSupport.firePropertyChange ("keep", new Boolean (oldKeep), new Boolean (keep));
    }
    
   public boolean isScalarcolor() {
        return scalarcolor;
    }

    public void setScalarcolor(boolean scalarcolor) {
        boolean oldScalarcolor = this.scalarcolor;
        this.scalarcolor = scalarcolor;
        
        
        
        
        
        //vtkActor act = ActorUtils.getActor(this.sat.getFieldline(FieldlineModule.FL_2_EARTH, this.getMjd()));
        //this.module1.getActor(getMjd());
        
        //ActorUtils.getActor(this.sat.getFieldline(FieldlineModule.FL_2_EQUATOR, this.getMjd())).GetMapper().ScalarVisibilityOn();
        //ActorUtils.getActor(this.sat.getFieldline(FieldlineModule.FL_2_EARTH, this.getMjd())).GetMapper().ScalarVisibilityOn();
/*
        Log.log("Creating pretty colours ...", 3);

        //mapper = sat.satelliteModule.actor.GetMapper();
        
            if (scalarcolor) {

                Enumeration e = this.sat.getFieldline(FieldlineModule.FL_2_EQUATOR, this.getMjd()).elements();
                while (e.hasMoreElements())
                    ((vtkFollower)e.nextElement()).GetMapper().ScalarVisibilityOn();    

                e = this.sat.getFieldline(FieldlineModule.FL_2_EARTH, this.getMjd()).elements();
                while (e.hasMoreElements())
                    ((vtkFollower)e.nextElement()).GetMapper().ScalarVisibilityOn();    

                //this.module1.getActor(getMjd()).GetMapper().ScalarVisibilityOn();
               // this.module2.getActor(getMjd()).GetMapper().ScalarVisibilityOn();

//                    ActorUtils.getActor(this.sat.getFieldline(FieldlineModule.FL_2_EARTH, this.getMjd())).GetMapper().ScalarVisibilityOn();
//                    ActorUtils.getActor(this.sat.getFieldline(FieldlineModule.FL_2_EQUATOR, this.getMjd())).GetMapper().ScalarVisibilityOn();

                //sat.magTangentModule.actor.GetMapper().ScalarVisibilityOn();
                }
        //else mapper.SetInputData(tubeFilter.GetOutput());
        else {
                
                Enumeration e = this.sat.getFieldline(FieldlineModule.FL_2_EQUATOR, this.getMjd()).elements();
                while (e.hasMoreElements())
                    ((vtkFollower)e.nextElement()).GetMapper().ScalarVisibilityOff();    
                     
                e = this.sat.getFieldline(FieldlineModule.FL_2_EARTH, this.getMjd()).elements();
                while (e.hasMoreElements())
                    ((vtkFollower)e.nextElement()).GetMapper().ScalarVisibilityOff();    

                    //this.module1.getActor(getMjd()).GetMapper().ScalarVisibilityOff();
                    //this.module2.getActor(getMjd()).GetMapper().ScalarVisibilityOff();
                    //ActorUtils.getActor(this.sat.getFieldline(FieldlineModule.FL_2_EARTH, this.getMjd())).GetMapper().ScalarVisibilityOff();
                    //ActorUtils.getActor(this.sat.getFieldline(FieldlineModule.FL_2_EQUATOR, this.getMjd())).GetMapper().ScalarVisibilityOff();
                //sat.magTangentModule.actor.GetMapper().ScalarVisibilityOff();
                }
                
    propertyChangeSupport.firePropertyChange("scalarcolor", oldScalarcolor, scalarcolor);

        */
    }
  //copied from SingleActorSatModule FKJN
  /** Getter for property color.
   * @return Value of property color.
   */
      public Color getColor() {
          return color;
      }

  /** Setter for property color.
   * @param color New value of property color.
   *
   * @throws PropertyVetoException
   */
      public void setColor(Color color) {
          Color oldColor = this.color;
          this.setScalarcolor(false);
          this.color = color;
//this.getFieldline(int 1, double mjd).get
          //vtkLogLookupTable lut  = new vtkLogLookupTable();
          //lut.SetHueRange(0.6667, 0);
          
          
        //this.sat.getFieldline(FieldlineModule.FL_2_EQUATOR, this.getMjd()).elements();
          
          
          /*
        float[] rgb = ovt.util.Utils.getRGB(getColor());
        Enumeration e = this.sat.getFieldline(FieldlineModule.FL_2_EQUATOR, this.getMjd()).elements();
        while (e.hasMoreElements())
            ((vtkFollower)e.nextElement()).GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);    
        
        e = this.sat.getFieldline(FieldlineModule.FL_2_EARTH, this.getMjd()).elements();
        while (e.hasMoreElements())
            ((vtkFollower)e.nextElement()).GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);    
        
*/

         //   this.module1.getActor(getMjd()).GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);;
         //   this.module2.getActor(getMjd()).GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);;
//          ActorUtils.getActor(this.sat.getFieldline(FieldlineModule.FL_2_EQUATOR, this.getMjd())).GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
//          ActorUtils.getActor(this.sat.getFieldline(FieldlineModule.FL_2_EARTH, this.getMjd())).GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
          
          //this.sat.magTangentModule.actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
          //mapper.SetColorMode(1);
          //mapper.SetLookupTable(lut);
      }
    
}
