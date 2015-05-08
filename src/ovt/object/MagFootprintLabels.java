/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/MagFootprintLabels.java,v $
  Date:      $Date: 2003/10/21 09:06:35 $
  Version:   $Revision: 1.6 $


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
 * MagFootprintLabels.java
 *
 * Created on July 6, 2001, 11:18 AM
 */

package ovt.object;

import vtk.*;

import ovt.*;
import ovt.gui.*;
import ovt.beans.*;
import ovt.util.*;
import ovt.event.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
/**
 *
 * @author  root
 * @version
 */
public class MagFootprintLabels extends LabelsModule {

    private MagFootprintModule footprints;
    /** Holds value of property prefferedVisibility. */
    private boolean prefferedVisibility = true;

    /** Creates new MagFootprintLabels */
    public MagFootprintLabels(MagFootprintModule footprints) {
        super(footprints.getSat());
        this.footprints = footprints;
        setParent(footprints);
        this.normalActorSize = 0.02;
        // change parent's descriptors
        Descriptors descriptors = super.getDescriptors();
        descriptors.remove("visible"); // remove "Show/Hide" descriptor

        try {

            BasicPropertyDescriptor pd = new BasicPropertyDescriptor("prefferedVisibility", this);
            pd.setDisplayName(getName());

            BasicPropertyEditor editor = new MenuPropertyEditor(pd, MenuPropertyEditor.SWITCH);
            editor.setTags(new String[]{"On", "Off"});
            editor.setValues(new Object[]{new Boolean(true), new Boolean(false)});
            addPropertyChangeListener("prefferedVisibility", editor);
            pd.setPropertyEditor(editor);
            descriptors.put(pd);



        } catch (IntrospectionException e2) {
            System.out.println(getClass().getName() + " -> " + e2.toString());
            System.exit(0);
    }

    }

    protected void validate() {
        labels.removeAllElements();
        //System.out.println("Setting lables...");
        double[] map = getTimeSet().getValues();
        String[] labs = getLabels(map);
        float[] rgb = ovt.util.Utils.getRGB(getColor());
        for (int i=0; i<map.length; i += getGap()) {
            //System.out.println("i= " + i + " length = " + map.length);
            /*
            vtkTextMapper mapper = new vtkTextMapper();

            mapper.SetInput(labs[i]);
            mapper.GetTextProperty().SetFontSize(getFont().getFontSize());
            mapper.GetTextProperty().SetFontFamily(getFont().getFontFamily());

            mapper.GetTextProperty().SetBold  (getFont().bold());
            mapper.GetTextProperty().SetItalic(getFont().italic());
            mapper.GetTextProperty().SetShadow(getFont().shadow());

            mapper.GetTextProperty().SetJustification(getJustification());
            mapper.GetTextProperty().SetVerticalJustification(BOTTOM);
            */
            double mjd = map[i];
            MagPoint[] fp1_fp2 = footprints.getMagFootprints(mjd);

            for (int j=0; j<2; j++) {
                // is it necessary ? if (fp1_fp2 == null) continue;
                if (fp1_fp2[j] == null) continue;
                double[] r = getTrans(mjd).gsm_geo_trans_matrix().multiply(fp1_fp2[j].gsm);
                // place labels under footprints to avoid sensoring of labels by earth surface
                r = Vect.multiply(r, 1.025);
                vtkVectorText atext = new vtkVectorText();
                atext.SetText(labs[i]);
                vtkPolyDataMapper mapper = new vtkPolyDataMapper();
                //mapper.SetInputData(atext.GetOutput());
                mapper.SetInputConnection(atext.GetOutputPort());

                GEOFollower actor = new GEOFollower(r, mjd);
                actor.SetMapper(mapper);
                actor.SetScale(getScale() * normalActorSize);


                actor.AddPosition(r[0], r[1], r[2]);
                actor.SetCamera(getRenderer().GetActiveCamera());

                actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
                actor.GetProperty().SetAmbientColor(rgb[0], rgb[1], rgb[2]);
                actor.GetProperty().SetSpecularColor(rgb[0], rgb[1], rgb[2]);
                actor.GetProperty().SetDiffuseColor(rgb[0], rgb[1], rgb[2]);

                labels.addElement(actor);
            }
        }

        valid = true;
    }

protected void show() {
  super.show();
  rotate();
}

public void setVisible(boolean visible) {
    if (visible) { // to show one must look at a parent's "visibility" state
        if (isPrefferedVisibility()) super.setVisible(visible);
    } else super.setVisible(visible); // no questions if someone whants to hide us
}

public boolean parentIsVisible() {
    //try {
        return ((VisualObject)getParent()).isVisible();
    //} catch (NullPointerException e2) { return false;
    //}
}

/** Getter for property prefferedVisibility.
 * @return Value of property prefferedVisibility.
 */
public boolean isPrefferedVisibility() {
    return prefferedVisibility;
}

/** Setter for property prefferedVisibility.
 * @param prefferedVisibility New value of property prefferedVisibility.
 */
public void setPrefferedVisibility(boolean prefferedVisibility) {
    Log.log("prefferedVisibility="+prefferedVisibility+" parentIsVisible="+parentIsVisible()+" isVisible()="+isVisible(), 8);
    boolean oldPrefferedVisibility = this.prefferedVisibility;
    if (oldPrefferedVisibility == prefferedVisibility) return; // nothing have changed
    this.prefferedVisibility = prefferedVisibility;

    if (prefferedVisibility == true) {
        if (parentIsVisible()) setVisible(true); // show only if the parent is visible
    } else {
        if (isVisible()) setVisible(false); // hide if shown
    }
    propertyChangeSupport.firePropertyChange ("prefferedVisibility", new Boolean (oldPrefferedVisibility), new Boolean (prefferedVisibility));
}

public void rotate() {
    Enumeration e = labels.elements();
    while (e.hasMoreElements()) {
            GEOFollower actor = (GEOFollower)e.nextElement();
            double[]  geoLoc = actor.getGEOLocation();
            Matrix3x3 m3x3 = getTrans(actor.getMjd()).geo_trans_matrix(getPolarCS());
            double polarLoc[] = m3x3.multiply(geoLoc);
            m3x3 = getTrans(getMjd()).trans_matrix(getPolarCS(), getCS());
            double real_loc[] = m3x3.multiply(polarLoc);
            // place labels under footprints to avoid sensoring of labels by earth surface
            real_loc = Vect.multiply(real_loc, 1.025);
            actor.SetPosition(real_loc[0], real_loc[1], real_loc[2]);
    }
}

public void timeChanged(TimeEvent evt) {
    super.timeChanged(evt);
    if (evt.timeSetChanged()) {
        /* this is done by parent
         invalidate();
        if (isVisible()) {
            hide();
            show();
        } */
    } else { // current time changed
        if (isVisible()) rotate();
    }
}

public void coordinateSystemChanged(CoordinateSystemEvent evt) {
    if (isVisible()) rotate();
}

}


/** This class combines vtkFollower and it's geo position.
 * The geo position will be used to update actor's position in space
 */
class GEOFollower extends vtkFollower {


    private double[] geoLoc = { 0, 0, 0};
    private double mjd;

    public GEOFollower(double[] geoLoc, double mjd) {
        super();
        this.geoLoc = geoLoc;
        this.mjd = mjd;
    }

    /*public void setGEOLocation(double[] geoLoc) {
        this.geoLoc = geoLoc;
    }*/

    public double[] getGEOLocation() {
        return geoLoc;
    }

    public double getMjd() {
        return mjd;
    }

}
