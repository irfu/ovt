/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/CoordinateSystem.java,v $
  Date:      $Date: 2003/09/28 17:52:46 $
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

/*
 * CoordinateSystems.java
 *
 * Created on March 20, 2000, 4:50 PM
 */
 
package ovt.object;

import ovt.*;
import ovt.beans.*;
import ovt.event.*;
import ovt.datatype.*;
import ovt.interfaces.*;
import ovt.object.editor.*;

import java.beans.*;

import java.util.*;
import java.lang.reflect.*;


/** 
 *
 * @author  root
 * @version 
 */
public class CoordinateSystem extends BasicObject {

  /** <B>Geocentric Equatorial Inertial</B><BR>
   * <B>X</B> - axis points from the Earth toward the first
   * point of Aries (the position of Sun at the vernal equinox)<BR>
   * <B>Z</B> - axis is parallel to the rotation axis of the Earth and points northward<BR>
   * <B>Y</B> - axis completes the right-handed orthogonal set <CODE><B>Y</B> = <B>Z</B> x <B>X</B></CODE>.
   */
  public static final int GEI =   0;
  
  /** <B>Geocentric Solar Magnetospheric</B><BR>
   * <B>X</B> - axis points toward the Sun<BR>
   * <B>Y</B> - axis is perpendicular to the Earth's magnetic dipole (<B>M</B>, pointing
   * southward) and the sunward direction (<B>S</B>) so that <CODE>(<B>Y</B> = <B>S</B> x <B>M</B>)</CODE><BR>
   * <B>Z</B> - axis completes the right-handed orthogonal set <CODE>(<B>Z</B> = <B>X</B> x <B>Y</B>)</CODE>.
   */
  public static final int GSM =   1;
  
  /** <B>Geocentric Solar Ecliptic</B><BR>
   * <B>X</B> - axis points toward the Sun<BR>
   * <B>Z</B> - axis points toward the ecliptic north pole<BR>
   * <B>Y</B> - axis points toward dusk, the direction that opposes planetary motion.
   */
  public static final int GSE =   2;
  
  public static final int GSEQ =  3;
  
  /** <B>Geographic</B><BR>
   * <B>X</B> - axis is in the Earth equatorial plane and passes through
   * Greenwich meridian<BR>
   * <B>Z</B> - axis is parallel to the rotation axis of the Earth and points northward<BR>
   * <B>Y</B> - axis completes the right-handed orthogonal set <CODE>(<B>Y</B> = <B>Z</B> x <B>X</B>)</CODE>.
   */
  public static final int GEO =   4;
  
  /** <B>Solar Magnetospheric</B><BR>
   * <B>Z</B> - axis is along the magnetic dipole and points northward<BR> 
   * <B>Y</B> - axis <CODE>(<B>Y</B> = <B>Z</B> x <B>SUN</B>)</CODE><BR>
   * <B>X</B> - axis completes the right-handed system <CODE>(<B>X</B> = <B>Y</B> x <B>Z</B>)</CODE><BR>
   * The angle between the <B>Z</B><SUB>SM</SUB> and <B>Z</B><SUB>GSM</SUB> is
   * the dipole tilt angle (positive toward the sun)
   */
  public static final int SM =   5;
  
  /** <B>Corrected</B><BR>
   * <B>Magnetic Local Time</B><BR>
   * <B>Magnetic Latitude</B> Not used
   */
  public static final int CORR =   6;
  public static final int ECC =   7;
  /** GeoMagnetic*/
  public static final int GMA =   8;
  
  
  /** Holds value of property coordinateSystem. */
  private int coordinateSystem = GSM;

  /** Utility field used by bound properties. */
  private CoordinateSystemChangeSupport csChangeSupport = new CoordinateSystemChangeSupport (this);
  
  /** Holds value of property polarCoordinateSystem. */
  private int polarCoordinateSystem = GEO;
  
  /** Creates new CoordinateSystems */
  public CoordinateSystem(OVTCore core) {
    super(core, "CoordinateSystems");
    showInTree(false);
    setParent(core); // to have a full name "OVT.CoordinateSystems"
  }
  
  public void addCoordinateSystemChangeListener (CoordinateSystemChangeListener listener) {
    csChangeSupport.addCoordinateSystemChangeListener (listener);
  }

  public void removeCoordinateSystemChangeListener (CoordinateSystemChangeListener listener) {
    csChangeSupport.removeCoordinateSystemChangeListener (listener);
  }
  
  /** Add a PropertyChangeListener to the listener list.
   * @param l The listener to add.
   */
  public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    propertyChangeSupport.addPropertyChangeListener (l);
  }
  
  /** Removes a PropertyChangeListener from the listener list. Doesn't work!
   * @param l The listener to remove.
   */
  public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    propertyChangeSupport.removePropertyChangeListener (l);
  }
  
  /** Getter for property coordinateSystem.
   * @return Value of property coordinateSystem.
   */
  public int getCoordinateSystem() {
    return coordinateSystem;
  }
  
  /** Setter for property coordinateSystem.
   * @param coordinateSystem New value of property coordinateSystem.
   */
  public void setCoordinateSystem(int coordinateSystem) {
    //System.out.println("----------- setCoordinateSystem ------- to " + getCoordSystem(coordinateSystem));
    int oldCoordinateSystem = this.coordinateSystem;
    this.coordinateSystem = coordinateSystem;
    propertyChangeSupport.firePropertyChange ("coordinateSystem", new Integer (oldCoordinateSystem), new Integer (coordinateSystem));
    csChangeSupport.fireCoordinateSystemChange(Const.XYZ, oldCoordinateSystem, coordinateSystem);
  }

  /** Getter for property polarCoordinateSystem.
   * @return Value of property polarCoordinateSystem.
   */
  public int getPolarCoordinateSystem() {
    return polarCoordinateSystem;
  }
  /** Setter for property polarCoordinateSystem.
   * @param polarCoordinateSystem New value of property polarCoordinateSystem.
   */
  public void setPolarCoordinateSystem(int polarCoordinateSystem) {
    int oldPolarCoordinateSystem = this.polarCoordinateSystem;
    this.polarCoordinateSystem = polarCoordinateSystem;
    propertyChangeSupport.firePropertyChange ("polarCoordinateSystem", new Integer (oldPolarCoordinateSystem), new Integer (polarCoordinateSystem));
    csChangeSupport.fireCoordinateSystemChange(Const.POLAR, oldPolarCoordinateSystem, polarCoordinateSystem);
  }

  public String[] getCoordinateSystemNames() {
    int[] csl = getCoordinateSystemsList();
    String[] names = new String[csl.length];
    for (int i=0; i<csl.length; i++) 
        names[i] = getCoordSystem(csl[i]);
    return names;
  } 

  public static int[] getCoordinateSystemsList() {
    return new int[]{ GEI, GSM, GSE, SM, GEO};
  }

  public String[] getPolarCoordinateSystemNames() {
    int[] csl = getPolarCoordinateSystemsList();
    String[] names = new String[csl.length];
    for (int i=0; i<csl.length; i++) 
        names[i] = getCoordSystem(csl[i]);
    return names;
  } 
  

  public int[] getPolarCoordinateSystemsList() {
    return new int[]{ SM, GEO};
  }
  
  public static String getCoordSystem(int n) {
    switch (n) {
		case GEI:  return "GEI";
		case GSM:  return "GSM";
		case GSE:  return "GSE";
		case GSEQ: return "GSEQ";
		case GEO:  return "GEO";
		case SM :  return "SMC"; // this is correct!
                //case CORR:  return "CORR";
		case ECC:  return "ECC";
    }
    throw new IllegalArgumentException("invalid coord system index");
  }

  public Descriptors getDescriptors() {
    if (descriptors == null) {
      try {
        descriptors = new Descriptors();
        // coordinate system
        BasicPropertyDescriptor pd = new BasicPropertyDescriptor("coordinateSystem", this);
        pd.setDisplayName("Coordinate System");
	pd.setToolTipText("Space Coordinate System");
        
        GUIPropertyEditor editor = new ComboBoxPropertyEditor(pd, getCoordinateSystemsList(), getCoordinateSystemNames());
        // Render each time user changes cs by means of gui
        editor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
          public void editingFinished(GUIPropertyEditorEvent evt) {
            Render();
          }
        });
        
        addPropertyChangeListener("coordinateSystem", editor);
        pd.setPropertyEditor(editor);
        descriptors.put(pd);
        
        // ----- PolarCoordinateSystem
        pd = new BasicPropertyDescriptor("polarCoordinateSystem", this);
        pd.setDisplayName("Polar Coordinate System");
        pd.setToolTipText("The Coordinate System of the Earth Surface");
        
        editor = new ComboBoxPropertyEditor(pd, getPolarCoordinateSystemsList(), getPolarCoordinateSystemNames());
        // Render each time user changes cs by means of gui
        editor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
          public void editingFinished(GUIPropertyEditorEvent evt) {
            Render();
          }
        });
        addPropertyChangeListener("polarCoordinateSystem", editor);
        pd.setPropertyEditor(editor);
        descriptors.put(pd);
                
      } catch (IntrospectionException e2) {
        System.out.println(getClass().getName() + " -> " + e2.toString());
        System.exit(0);
      }
      
      
    }
    return descriptors;
  }
  
}
