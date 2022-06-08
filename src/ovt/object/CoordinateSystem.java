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

OVT Team (https://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
Khotyaintsev

=========================================================================*/

/*
 * CoordinateSystems.java
 *
 * NOTE: OVT originally supported only GEI J2000.0 (and not GEI
 * epoch-of-date). It therefore referred to GEI J2000.0 as only "GEI" throughout
 * the source code and still mostly does. /Erik P G Johansson 2022-06-07
 *
 * Created on March 20, 2000, 4:50 PM
 */
 
package ovt.object;

import java.beans.*;
import ovt.*;
import ovt.beans.*;
import ovt.event.*;
import ovt.interfaces.*;


/** 
 *
 * @author  root
 * @version 
 */
public class CoordinateSystem extends BasicObject {

  /** <B>Geocentric Equatorial Inertial (J2000.0)</B><BR>
   * <B>X</B> - axis points from the Earth toward the first
   *            point of Aries (the position of Sun at the vernal equinox) at J2000.0<BR>
   * <B>Z</B> - axis is parallel to the rotation axis of the Earth and points northward<BR>
   * <B>Y</B> - axis completes the right-handed orthogonal set
   *            <CODE><B>Y</B> = <B>Z</B> x <B>X</B></CODE>.
   *
   * Note: Not to be confused with GEI epoch-of-date/mean-of-date.
   */
  public static final int GEI =  0;
  
  /** <B>Geocentric Solar Magnetospheric</B><BR>
   * <B>X</B> - axis points toward the Sun<BR>
   * <B>Y</B> - axis is perpendicular to the Earth's magnetic dipole (<B>M</B>, pointing
   *            southward) and the sunward direction (<B>S</B>) so that
   *            <CODE>(<B>Y</B> = <B>S</B> x <B>M</B>)</CODE><BR>
   * <B>Z</B> - axis completes the right-handed orthogonal set
   *            <CODE>(<B>Z</B> = <B>X</B> x <B>Y</B>)</CODE>.
   */
  public static final int GSM =  1;
  
  /** <B>Geocentric Solar Ecliptic</B><BR>
   * <B>X</B> - axis points toward the Sun<BR>
   * <B>Z</B> - axis points toward the ecliptic north pole<BR>
   * <B>Y</B> - axis points toward dusk, the direction that opposes planetary motion.
   */
  public static final int GSE =  2;
  
  public static final int GSEQ = 3;
  
  /** <B>Geographic</B><BR>
   * <B>X</B> - axis is in the Earth equatorial plane and passes through
   *            Greenwich meridian<BR>
   * <B>Z</B> - axis is parallel to the rotation axis of the Earth and points northward<BR>
   * <B>Y</B> - axis completes the right-handed orthogonal set
   *            <CODE>(<B>Y</B> = <B>Z</B> x <B>X</B>)</CODE>.
   */
  public static final int GEO =  4;
  
  /** <B>Solar Magnetospheric</B><BR>
   * <B>Z</B> - axis is along the magnetic dipole and points northward<BR> 
   * <B>Y</B> - axis <CODE>(<B>Y</B> = <B>Z</B> x <B>SUN</B>)</CODE><BR>
   * <B>X</B> - axis completes the right-handed system
   *            <CODE>(<B>X</B> = <B>Y</B> x <B>Z</B>)</CODE><BR>
   * The angle between the <B>Z</B><SUB>SM</SUB> and <B>Z</B><SUB>GSM</SUB> is
   * the dipole tilt angle (positive toward the sun)
   */
  public static final int SM =   5;
  
  /** <B>Corrected</B><BR>
   * <B>Magnetic Local Time</B><BR>
   * <B>Magnetic Latitude</B> Not used
   */
  public static final int CORR = 6;
  public static final int ECC =  7;
  /** GeoMagnetic */
  public static final int GMA =  8;

  /** <B>Geocentric Equatorial Inertial (epoch-of-date/mean-of-date)</B><BR>
   * <B>X</B> - axis points from the Earth toward the first
   *            point of Aries (the position of Sun at the vernal equinox) at epoch-of-date<BR>
   * <B>Z</B> - axis is parallel to the rotation axis of the Earth and points northward<BR>
   * <B>Y</B> - axis completes the right-handed orthogonal set
   *            <CODE><B>Y</B> = <B>Z</B> x <B>X</B></CODE>.
   *
   * Note: Not to be confused with GEI J2000.0.
   */
  public static final int GEID = 9;

  
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
    propertyChangeSupport.firePropertyChange(
            "coordinateSystem",
            new Integer(oldCoordinateSystem),
            new Integer(coordinateSystem)
    );
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
    propertyChangeSupport.firePropertyChange(
            "polarCoordinateSystem",
            new Integer(oldPolarCoordinateSystem),
            new Integer(polarCoordinateSystem)
    );
    csChangeSupport.fireCoordinateSystemChange(Const.POLAR, oldPolarCoordinateSystem, polarCoordinateSystem);
  }

  public String[] getCoordinateSystemNames() {
    int[] csl = getCoordinateSystemsList();
    String[] names = new String[csl.length];
    for (int i=0; i<csl.length; i++) 
        names[i] = getCoordSystem(csl[i]);
    return names;
  } 

  /** Determines which coordinate systems are available in the GUI (and maybe
   *  more).
   */
  public static int[] getCoordinateSystemsList() {
      // NOTE: Puts GEID immediately after GEI (as opposed to the otherwise used
      // coordinate system order) since they are related and should therefore be
      // close to each other in the GUI.
    return new int[]{GEI, GEID, GSM, GSE, SM, GEO};
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

  /**
   * Return human-readable string that describes a coordinate system. The string
   * will be used in OVT's coordinate system selector drop-down menu at the
   * lower left.
   *
   * @param n
   * @return
   */
  public static String getCoordSystem(int n) {
      /*
      E-mail from Erik Johansson <erjo@irfu.se> to
      Daly Dr., Patrick <daly@mps.mpg.de> 2022-06-07
      """"""""
      A more trivial and formal question: For the purpose of the OVT, both for
      the user interface but also for the documentation (including the source
      code), what are the best names/terms to use for these coordinate systems,
      e.g. in the drop down menu inside OVT? I want to try to be consistent to
      avoid confusion for technical things like this (it usually pays off when
      writing software) and I don't want to confuse the regular user. Since
      these two coordinate systems are basically two close variants of the same
      idea for a coordinate system, I figure that not everyone is aware of the
      distinction between them (and as your original e-mail implies). Therefore
      I am tempted to list them them both as "GEI" and then indicate the exact 
      "variant" within brackets as below. This would indicate/hint to a novice
      that they are closely related and similar coordinate systems.

      "GEI (J2000.0)"
      "GEI (epoch-of-date)"

      However, I have seen multiple names for in particular the latter: GEI TOD,
      GEI true-of-date, GEI mean-of-date, GEI epoch-of-date, and possibly other
      terms too.

      Do you have any opinion on this? Is there a natural or most common naming
      convention?
      """"""""

      E-mail reply from
      Mike Hapgood - STFC UKRI <mike.hapgood@stfc.ac.uk> to
      Erik Johansson <erjo@irfu.se> 2022-06-09
      """"""""
      Many thanks to Pat for copying me into this discussion. I think Erik's
      proposal to put the GEI variant in brackets is a good one - as he notes it
      makes it clear that these are variants of the same concept. I think
      "epoch-of-date" is the best name in that it reflects that astronomers use
      "epoch" as a term for reference points in time. So epoch J2000 is now the
      standard reference now in use by astronomers, e.g. to specify positions in
      the sky of stars and other astronomical objects. (The astronomical
      coordinates of right ascension and declination are GEI expressed in polar
      rather than cartesian form.) Previously astronomers used an epoch termed
      B1950 for this purpose, changing to J2000 late in the last century.

      The other terms used in place of "epoch-of-date" probably indicate fine
      details that I doubt that we need to worry about, e.g. differences between
      what astronomers term true and mean time.
      """"""""
      */
    switch (n) {
      case GEI:  return "GEI (J2000.0)";
      case GSM:  return "GSM";
      case GSE:  return "GSE";
      case GSEQ: return "GSEQ";
      case GEO:  return "GEO";
      case SM :  return "SMC"; // this is correct!
                //case CORR:  return "CORR";
      case ECC:  return "ECC";
      case GEID: return "GEI (epoch-of-date)";
    }
    throw new IllegalArgumentException("Invalid coordinate system index.");
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
