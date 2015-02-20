/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/gui/ClusterConfigurationPanel.java,v $
  Date:      $Date: 2003/09/28 17:52:40 $
  Version:   $Revision: 2.3 $


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
 * ClusterConfigurationPanel.java
 *
 * Created on April 11, 2000, 1:07 PM
 */
 
package ovt.gui;

import ovt.*;
import ovt.gui.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.beans.*;
import ovt.event.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import java.beans.*;

import java.awt.*;
import javax.swing.*;


/** 
 *
 * @author  ko
 * @version 
 */
public class ClusterConfigurationPanel extends JPanel 
    implements PropertyChangeListener, TimeChangeListener {

  protected JTextField[] dist = new JTextField[12];
  protected JLabel[] dist2 = new JLabel[3];  // added by kono
  
  // added by kono
  protected final String[] raws_names={"XYZ box (km)", "Ellipsoid (km)", "FAC box (km)"};
  protected String[] title = {
    "dx = ", "dy = ", "dz = ", 
    "a = ", "b = ", "c = ",
    "dB = ", "dA = ", "dR = "};
  protected ClusterSats clusterSats;
  
  WindowPropertyEditor editor;

  /** Creates new ClusterConfigurationPanel */
  public ClusterConfigurationPanel(ClusterSats clusterSats, WindowPropertyEditor editor) {
    super();
    this.clusterSats = clusterSats;
    this.editor = editor;
    makeInterior();
    //refresh();
  }
  
  protected void makeInterior() {
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(5, 2, 5, 2);
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    setLayout(gridbag);

    // Create components for raws names
    for (int j=0; j<3; j++) {
      //dist[j] = new JTextField(raws_names[j]);
      dist2[j] = new JLabel(raws_names[j]);
      c.gridx = 0;
      c.gridy = j;
      c.gridwidth = 1;
      gridbag.setConstraints(dist2[j], c);
      add(dist2[j]);
    }
    
    // Create components for dx,dy,dz
    for (int j=0; j<3; j++) {
      dist[j+3] = new JTextField(title[j]+"00000");
      c.gridx = j+1;
      c.gridy = 0;
      c.gridwidth = 1;
      gridbag.setConstraints(dist[j+3], c);
      add(dist[j+3]);
    }
    
    //added by kono
    // Create components for a,b,c
    for (int j=0; j<3; j++) {
      dist[j+6] = new JTextField(title[j+3]+"00000");
      c.gridx = j+1;
      c.gridy = 1;
      c.gridwidth = 1;
      gridbag.setConstraints(dist[j+6], c);
      add(dist[j+6]);
    }
    
    // Create components for dB,dA,dR
    for (int j=0; j<3; j++) {
      dist[j+9] = new JTextField(title[j+6]+"00000");
      c.gridx = j+1;
      c.gridy = 2;
      c.gridwidth = 1;
      gridbag.setConstraints(dist[j+9], c);
      add(dist[j+9]);
    }
}

  public void refresh() {
    double[][] pos = new double[4][];
    Sat sat;
    TrajectoryPoint trp;
    int i, j;
    String toFile;
    StringToFileWriter clusterConf = 
      new StringToFileWriter(this.clusterSats.clusterConfOutFileName,true);
    
    toFile = Time.toString(getMjd())+"\t";
    
    for (i=0; i<4; i++) {
      sat = clusterSats.get(i+1);
      trp = sat.getTrajectory().get(getMjd());
      //pos[i] = trp.get(getCS());            //commented by kono (GSM only!)
      pos[i] = trp.get(CoordinateSystem.GSM); //we don't care about current CS!
    }
    
    double[] d = Utils.maxDifffer(pos);

    for (j=0; j<3; j++){
      int tmpx = (int)(d[j]*Const.RE);
      dist[j+3].setText(title[j] + tmpx);
      toFile = toFile.concat(Integer.toString(tmpx)+"\t");
    }
    
    //*********** following code added by kono ************
    //Calculating semiaxises of CLUSTER' ellipsoide
    double[] elipsoid = Utils.getEllipsoide(4,pos);
    for (j=0; j<3; j++) {
      elipsoid[j]*=Const.RE;
      dist[j+6].setText(title[j+3] + (int)elipsoid[j]);
      toFile = toFile.concat(Integer.toString((int)elipsoid[j])+"\t");
    }
    //Calculating "CLUSTER in Field-Aligned Coordinates (FAC)" (dB, dA, dR)
    double[] B,meanB={0,0,0},meanR={0,0,0};
    double[][] e=new double[3][];
    double tmpx;
    for(i=0;i<4;++i){
      B=clusterSats.getCore().getMagProps().bv(pos[i],getMjd()); //in GSM!!!
      for(j=0;j<3;++j){
        meanB[j]+=B[j]/4.0;
        meanR[j]+=pos[i][j]/4.0;   //pos should be in GSM CS !!!
      }
    }
    Vect.normf(meanB,1.0);
    Vect.normf(meanR,1.0);
    e[0]=meanB;                     //e1 = meanB
    e[1]=Vect.cross(meanR,meanB);   //e2 = R x B
    e[2]=Vect.cross(e[0],e[1]);     //e3 = e1 x e2
    
    double[][] facPos=new double[4][];    //CLUSTER's positions in FAC CS
    Matrix3x3 e1e2e3=new Matrix3x3(e);    //Transformation matrix
    for(i=0;i<4;++i)
      facPos[i]=e1e2e3.multiply(pos[i]);  //facPos = E x oldPos
    
    double[] dBdAdR=Utils.maxDifffer(facPos);
    
    for (j=0; j<3; j++){
      int tmpa = (int)(dBdAdR[j]*Const.RE);
      dist[j+9].setText(title[j+6] + tmpa);
      toFile = toFile.concat(Integer.toString(tmpa)+"\t");
    }
    clusterConf.writeString(toFile);
    clusterConf.close();
  }

  public void propertyChange(PropertyChangeEvent evt) {
    //System.out.println("Panel - "+evt.getPropertyName());
    if (evt.getPropertyName().equals("windowVisible")) {
      boolean visible = ((Boolean)evt.getNewValue()).booleanValue();
      if (visible) refresh();
    }
  }
  public void timeChanged(TimeEvent evt) {
    if (editor.getWindow().isVisible()) refresh();
  }
  
  public double getMjd() {
    return clusterSats.getMjd();
  }
  
  
  public int getCS() {
    return clusterSats.getCS();
  }
}
