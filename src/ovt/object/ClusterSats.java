/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/ClusterSats.java,v $
  Date:      $Date: 2003/09/28 17:52:45 $
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

OVT Team (http://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
Khotyaintsev

=========================================================================*/

/*
 * ClusterSats.java
 *
 * Created on den 9 april 2000, 17:03
 */
 
package ovt.object;

import ovt.*;
import ovt.gui.*;
import ovt.util.*;
import ovt.beans.*;
import ovt.event.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import java.beans.*;

import java.io.*;

/** 
 *
 * @author  mykola
 * @version 
 */
public class ClusterSats extends VisualObject implements TimeChangeListener, 
  CoordinateSystemChangeListener, MagPropsChangeListener, PropertyChangeListener {

  private ClusterSat sats[] = new ClusterSat[4];
  
/** Holds value of property configurationWindowVisible. */
  private boolean configurationWindowVisible = false;
  protected ClusterConfigurationPanel configurationPanel;
  public String clusterConfOutFileName;

  /** Creates new ClusterSats */
  public ClusterSats(Sats sats) {
    super(sats.getCore(), "Cluster", "images/cluster.gif", true);
    Log.log("ClusterSats :: init ...", 3);
    clusterConfOutFileName = OVTCore.getUserdataDir()+"clusters.conf";
    setParent(sats);
    //setChildren(new Children());
    try {
      BasicPropertyDescriptor pd = new BasicPropertyDescriptor("configurationWindowVisible", this);
      pd.setDisplayName("Cluster Configuration");
      WindowPropertyEditor editor = new WindowPropertyEditor(pd, new String[]{"show configuration", "hide configuration"});
      editor.setModal(false);
      
      if (!OVTCore.isServer()) {
        configurationPanel = new ClusterConfigurationPanel(this, editor);
        editor.setComponent(configurationPanel);
      }
      
      pd.setPropertyEditor(editor);
      addPropertyChangeListener(editor);
      getDescriptors().put(pd);
    } catch (IntrospectionException e2) {}
    
    System.out.println("Loading Cluster Satellites...");
    
    // load sats
    for (int i=0; i<4; i++) {
        String satName = "Cluster "+(i+1);
        File orbitFile = new File(OVTCore.getOrbitDataDir()+"Cluster"+(i+1)+".ltof" );
        System.out.print(Utils.addSpaces(satName, 16) + "\t");
        try {
            ClusterSat sat = new ClusterSat(sats.getCore());
            sat.setName(satName);
            sat.setOrbitFile(orbitFile);
            add(sat);
            System.out.println("[   OK   ] "+new Time(sat.getFirstDataMjd()) +
                        " - "+new Time(sat.getLastDataMjd()));
        } catch (IOException e2) {
            System.out.println("[ FAILED ] (" + orbitFile + ")");
            getCore().sendErrorMessage(e2);
        }
    }
  }
  
  /** Legacy constructor*/
  public void add(ClusterSat sat) {
    addPropertyChangeListener(sat);
    sat.addPropertyChangeListener(this);
    sat.setParent(this);
    if (!sat.isEnabled()) setEnabled(sat.isEnabled());
    sats[sat.getSatNumber()-1] = sat;
    addChild(sat);
  }
  /** Returns ClusterSat by it's number 1..4 . legacy method*/
  public ClusterSat get(int number) {
    if (number>4 || number<1) throw new IllegalArgumentException("Invalid number of Cluster Satellite - "+number);
    return sats[number-1];
  }
  
  /** Returns ClusterSat by it's number 0..3 ;-) */
  public ClusterSat getClusterSat(int number) {
    if (number>3 || number<0) throw new ArrayIndexOutOfBoundsException("Invalid number of Cluster Satellite ("+number+"). Use numbers 0..3");
    return sats[number];
  }
  
  public void timeChanged(TimeEvent evt) {
    for (int i=0; i<4; i++) sats[i].timeChanged(evt);
    if (!OVTCore.isServer()) configurationPanel.timeChanged(evt);
  }
  public void coordinateSystemChanged(CoordinateSystemEvent evt) {
    for (int i=0; i<4; i++) sats[i].coordinateSystemChanged(evt);
  }
  public void magPropsChanged(MagPropsEvent evt) {
    for (int i=0; i<4; i++) sats[i].magPropsChanged(evt);
  }
  
  /** Getter for property configurationWindowVisible.
   * @return Value of property configurationWindowVisible.
   */
  public boolean isConfigurationWindowVisible() {
    return configurationWindowVisible;
  }
  /** Setter for property configurationWindowVisible.
   * @param configurationWindowVisible New value of property configurationWindowVisible.
   */
  public void setConfigurationWindowVisible(boolean configurationWindowVisible) {
    boolean oldConfigurationWindowVisible = this.configurationWindowVisible;
    this.configurationWindowVisible = configurationWindowVisible;
    propertyChangeSupport.firePropertyChange ("configurationWindowVisible", new Boolean (oldConfigurationWindowVisible), new Boolean (configurationWindowVisible));
  }
  
  
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals("enabled")) {
      boolean enabled = ((Boolean)evt.getNewValue()).booleanValue();
      setEnabled(enabled);
    }
  }
}
