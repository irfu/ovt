/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/Sats.java,v $
  Date:      $Date: 2003/09/28 17:52:51 $
  Version:   $Revision: 2.6 $
 
 
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

package ovt.object;

import ovt.*;
import ovt.util.*;
import ovt.event.*;
import ovt.datatype.*;
import ovt.interfaces.*;


import java.util.*;

import javax.swing.*;

/**
 * Class which somehow represents the instances of "Sat" (without the final "s")
 * which are in the GUI tree.
 */
public class Sats extends BasicObject implements TimeChangeListener, 
    CoordinateSystemChangeListener, MagPropsChangeListener, MenuItemsSource {
    
    private final ClusterSats clusterSats;
    
//    private double minLaunchMjd = Double.MAX_VALUE;
//    public static String satsConfigFile;
    
    /** Holds value of property sat. */
    private Vector sats = new Vector();
    
    //public boolean isInPlotList = false;
    //protected static boolean nothingIsShown = true;
    
    
    /** Constructor. Directory with Sat-data files is passed to it.
     * Set default satellites.
     * 
     * @param core 
     */
    public Sats(OVTCore core) {
        super(core, "Satellites");
        Log.log("Sats :: init ...", 3);
        try {
          setIcon(new ImageIcon(Utils.findResource("images/satellites.gif")));
        } catch (java.io.FileNotFoundException exc) {
            exc.printStackTrace(System.err);
        }
//        satsConfigFile = getCore().getConfSubdir() + "sats.conf";
        
        clusterSats = new ClusterSats(this);
        addChild(clusterSats);
    }
    
    
    /*public Hashtable getVisibleSats() {
        Hashtable res = new Hashtable();
        Enumeration e = getChildren().elements();
        while (e.hasMoreElements()) {
            Object obj = e.nextElement();
            Sat sat = (Sat)obj;
            
            if (sat.isVisible()) res.put(sat.getName(), sat);
        }
        return res;
    }*/
    
    
  /** Returns a hash table of all sats. */
    /*public Children getAllSats() {
        Children res = new Children(this);
        Enumeration e = getChildren().elements();
        Enumeration e2;
        OVTObject obj, obj2;
        while (e.hasMoreElements()) {
            obj = (OVTObject)e.nextElement();
            if (obj instanceof Sat) {
                res.addChild(obj);
            } else if (obj instanceof ClusterSats) {
                // add all clauster sats
                e2 = ((ClusterSats)obj).getChildren().elements();
                while (e2.hasMoreElements()) {
                    obj2 = (OVTObject)e2.nextElement();
                    res.addChild(obj2);
                }
            }
        }
        return res;
    }*/
    
    
    public void timeChanged(TimeEvent evt) {
        Enumeration e = getChildren().elements();
        while (e.hasMoreElements()) {
            try {
                ((TimeChangeListener)(e.nextElement())).timeChanged(evt);
            } catch (ClassCastException e2) {
                System.out.println("this sat doesn't care about time..");
            }
        }
    }
    
    /** Tell all sats about cs change. */
    public void coordinateSystemChanged(CoordinateSystemEvent evt) {
        clusterSats.coordinateSystemChanged(evt);
        
        Enumeration e = sats.elements();
        while (e.hasMoreElements()) {
            try {
                ((Sat)(e.nextElement())).coordinateSystemChanged(evt);
            } catch (ClassCastException e2) {
                System.out.println("this sat doesn't care about cs..");
            }
        }
    }
    
    public void magPropsChanged(MagPropsEvent evt) {
        clusterSats.magPropsChanged(evt);
        
        Enumeration e = sats.elements();
        while (e.hasMoreElements()) {
            try {
                ((MagPropsChangeListener)(e.nextElement())).magPropsChanged(evt);
            } catch (ClassCastException e2) {
                System.out.println("this sat doesn't care about magProps..");
            }
        }
    }
    
    public int getNumberOfSats() {
        return sats.size();
    }
    
    public void setNumberOfSats(int size) {
        // remove all sats
        Enumeration e = sats.elements();
        while (e.hasMoreElements()) removeSat((Sat)e.nextElement());
        // set the size
        sats.setSize(size);
        children.setSize(size + 1); // +1 because of clusterSats
        //addChild(clusterSats); - they are always on the first place
    }
    
    /** Indexed getter for property sat.
     * @param index Index of the property.
     * @return Value of the property at <CODE>index</CODE>.
 */
    public Sat getSat(int index) {
        return (Sat) sats.get(index);
    }
    
    /** Indexed setter for property sat.
     * @param index Index of the property.
     * @param sat New value of the property at <CODE>index</CODE>.
 */
    public void setSat(int index, Sat sat) {
        sat.setParent(this);
        sats.setElementAt(sat, index);
        children.setChildAt(index + 1, sat); // +1 because of clusterSats
    }
    
    public void addSat(Sat sat) {
        sats.addElement(sat);
        addChild(sat);
        //children.fireChildAdded(sat);
    }
    
    public void removeSat(Sat sat) {
        sat.dispose();
        //Log.log("Tot mem. before removing = "+Runtime.getRuntime().totalMemory());
        sats.removeElement(sat);
        getChildren().removeChild(sat);   // getChildren() defined in OVTObject.
        //Log.log("Tot mem. after removing = "+Runtime.getRuntime().totalMemory());
        //System.gc();
        //Log.log("Tot mem. after garb. col = "+Runtime.getRuntime().totalMemory());
        //children.fireChildRemoved(sat);
    }
    
    
    /**
     * Method representing the action of adding a satellite (of any type: LTOF,
     * TLE, SSCWS) to the GUI tree panel, as if this action was triggered by a
     * user event in the GUI.
     */
    // PROPOSAL: Check for satellite with the same name.
    public void addSatAction(Sat sat) {
        final String satName = sat.getName();
        final Sat preExistingSat = (Sat) getCore().getSats().getChildren().getChild(satName);   // null if there is no such satellite.
        if (preExistingSat != null) {
            // NOTE: Important to specify that we are speaking of adding a
            // satellite to the "GUI tree", not importing a file or any other form "adding".
            //getCore().sendErrorMessage("Error", "Can not add satellite in the GUI since there already is a satellite with the same name (\"" + satName + "\").");
            return;
        }

        getCore().getSats().addSat(sat);
        getCore().getSats().getChildren().fireChildAdded(sat);

        // TENTATIVE BUGFIX 2016-03-01: View Control (Camera.java) --> To (viewTo) should
        // contain this satellite if it is visible.
        sat.addPropertyChangeListener("visible", getCore().getCamera().getViewToObjectsVisibilityChangeListener());  // Add camera to listeners of new satellite visibility

        /* Include here or let the caller call it?!
         * Having the caller call this might be more efficient.
         * Including here might also have implications if automatically calling this function during launch/initialization.
         * Excluding here implies that it is not equivalent to an entire user-triggered "action" and thus the method name is slightly wrong.
         */
        //getCore().Render();
    }


    /**
     * Method representing the action of removing a satellite (of any type:
     * LTOF, TLE, SSCWS) to the GUI tree panel, as if this action was triggered
     * by a user event in the GUI.
     */
    public void removeSatAction(Sat sat) {
        getCore().getSats().removeSat(sat);
        getCore().getSats().getChildren().fireChildRemoved(sat); // for TreePanel
        
        // Remove camera from listeners of new satellite visibility. Needed?
        //sat.removePropertyChangeListener("visible", getCore().getCamera().getViewToObjectsVisibilityChangeListener());

        /**
         * Include here or let the caller call it?! Having the caller call this
         * might be more efficient. (Including here might also have implications
         * if automatically calling this function during launch/initialization.)
         * Excluding here implies that it is not equivalent to an entire
         * user-triggered "action" and thus the method name is slightly wrong.
         */
//        getCore().Render();
    }
    
    
    /** Is run by XML parser to tell treePanel to update Satellites node */
    public void fireSatsChanged() {
        children.fireChildrenChanged();
    }
    
    /** Used by XML. */
    public ClusterSats getClusterSats() { return clusterSats; }
    
    @Override
    public JMenuItem[] getMenuItems() {
        final JMenuItem[] sats = getCore().getXYZWin().getXYZMenuBar().createSatsMenuItemList_LTOF_TLE();
        final JMenuItem[] res = new JMenuItem[sats.length + 2];
        res[0] = getCore().getXYZWin().getXYZMenuBar().createImportSatelliteMenuItem();
        res[1] = null; // separator
        
        // Copy "sats" to "res" but with indices shifted (incremented) by 2.
        System.arraycopy(sats, 0, res, 2, sats.length);
        return res;
    }
}

/*  -- just to remember how to use JOptionPane.showInputDialog
  
File[] files = new File(OVTCore.getOrbitDataSubdir()).listFiles( new FilenameFilter() {
                 public boolean accept(File dir, String file) {
                    return file.endsWith(".tle");
                 }
             });
             Vector satList = new Vector();
             for (int i=0; i<files.length; i++) { 
                 String filename = files[i].getName();
                 String satName = filename.substring(0, filename.lastIndexOf('.'));
                 // add to the list if sat is not already added
                 if (!getChildren().containsChild(satName)) satList.addElement(satName); 
             }
             Object[] selectionValues = satList.toArray();
             String filename = (String)JOptionPane.showInputDialog(getCore().getXYZWin(), //Component parentComponent,
                                     "Select Satellite", // Object message
                                     "Select Satellite", // String title
                                     JOptionPane.OK_CANCEL_OPTION, //int messageType
                                     null, // Icon icon
                                     selectionValues, // Object[] selectionValues,
                                     selectionValues[0]); //, Object initialSelectionValue
             if (filename != null) {
                Sat sat = new Sat(getCore());
                try {
                    sat.setName(filename);
                    sat.setOrbitFile(new File(OVTCore.getOrbitDataSubdir()+filename+".tle"));
                    addSat(sat);
                    getChildren().fireChildAdded(sat);
                } catch (IOException e2) {
                    getCore().sendErrorMessage(e2);
                }
                
            }
 */
