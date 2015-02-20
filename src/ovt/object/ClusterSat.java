/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/ClusterSat.java,v $
  Date:      $Date: 2005/02/09 13:13:20 $
  Version:   $Revision: 2.13 $


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

package ovt.object;

import ovt.*;
import ovt.gui.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.event.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import java.lang.Runtime;


public class ClusterSat extends LTOFSat {

 public ClusterSat(OVTCore core) {
    super(core);
    try {
        setIcon(new javax.swing.ImageIcon(Utils.findResource("images/cluster_sat.gif")));
    } catch (FileNotFoundException e2) { e2.printStackTrace(System.err); }
 }
 
 public void setOrbitFile(File orbitFile) throws IOException {
     super.setOrbitFile(orbitFile);
    // color cluster satellites according to the convension
    Color color = null;
    switch (getSatNumber()) {
        case 1 :  color = Color.black; 
                        break;
        case 2 :  color = Color.red; 
                        break;
        case 3 :  color = Color.green; 
                        break;
        case 4 : color = Color.magenta; 
                       break;
        default: throw new IOException("This is not a Cluster LTOF. The number of a s/c should be 1-4.");
    }
    getSatelliteModule().setColor(color); 
    getOrbitModule().setColor(color); 
    getMagFootprintModule().setColor(color); 
  }
 


 /**
  * Redefinition of a parent's method. 
  * @return spin vector in gei, vector antiparallel to Z-GSE if no spin data available 
  */
  protected double[] getSpinVectGEI(double mjd) {
    double[] res = super.getSpinVectGEI(mjd);
    if (res == null) {
        //System.out.println("Spin for "+ new Time(mjd) +" is not available. Setting it to Z-GSE");
        Matrix3x3 mtrx = getTrans(mjd).gse_gei_trans_matrix();
        res = mtrx.multiply(new double[]{0, 0, -1});
    } //else System.out.println("Spin for "+ new Time(mjd) +" is available.");
    return res;
  }
  
  /** redefinition of previous method. 
   * @return true
   */
  public boolean isSpinAvailable(){
    return true;
  }

  /** returns super.getMenuItems() with the invalidated "Remove" item. */
  public JMenuItem[] getMenuItems() { 
     JMenuItem[] items = super.getMenuItems();
     items[6].setEnabled(false);
     return items;
  }
 
 
}
