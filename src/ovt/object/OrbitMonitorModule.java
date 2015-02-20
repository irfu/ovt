/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/OrbitMonitorModule.java,v $
  Date:      $Date: 2006/06/21 11:40:13 $
  Version:   $Revision: 2.15 $


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
import ovt.beans.*;
import ovt.util.*;
import ovt.event.*;
import ovt.datatype.*;
import ovt.interfaces.*;
import ovt.model.bowshock.*;
import ovt.model.magnetopause.*;

import java.beans.*;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

/**
 * A window, which shows satelite's position, velosity, footprints, etc.
 */ 

public class OrbitMonitorModule extends AbstractSatModule {

  private static final String km = "km";
  private static final String RE = "RE";

  /** format for position, distance */
  private DecimalFormat format = new DecimalFormat();
  /** format for B field */
  private DecimalFormat bFormat = new DecimalFormat();
  /** format for footprints */
  private DecimalFormat footprintFormat = new DecimalFormat();
  /** format for spin */
  private DecimalFormat spinFormat = new DecimalFormat();
  /** format for time */
  private TimeFormat timeFormat = new TimeFormat();
  
  protected JTextField x, y, z, r, v, b, time, dist, diff, dist_to_mpause,
  dist_to_bowshock, dist_to_bowshock_along_imf, theta_imf_bowshock_normal;
  protected JTextField[] spin = new JTextField[3];
  protected JTextField[][] fp = new JTextField[2][2]; // footprint
  protected JLabel[][] fpL = new JLabel[2][2]; // footprint labels
  protected JLabel position, footprint;
  protected JFrame orbitMonitor = null;
  private DumpRecord dumpRecord = new DumpRecord();
  private JButton showhideDumperButton;
  private Dumper dumper = new Dumper(this);
  
  /** Holds value of property distanceUnit. */
  private String distanceUnit = "RE";
  /** Holds value of property dumperVisible. */
  private boolean dumperVisible = false;
 
  
public OrbitMonitorModule(Sat sat) {
  super(sat, "OrbitMonitor");
  showInTree(false);
  DecimalFormatSymbols symb = new DecimalFormatSymbols();
  symb.setNaN("");  
  format.setPositivePrefix(" ");
  format.setMinimumFractionDigits(2);
  format.setMaximumFractionDigits(2);
  format.setDecimalFormatSymbols(symb);
  
  // set format for B
  bFormat.setPositivePrefix(" ");
  bFormat.setMinimumFractionDigits(0);
  bFormat.setMaximumFractionDigits(0);
  bFormat.setDecimalFormatSymbols(symb);
  // format footprints
  footprintFormat.setPositivePrefix(" ");
  footprintFormat.setMinimumFractionDigits(2);
  footprintFormat.setMaximumFractionDigits(2);
  footprintFormat.setDecimalFormatSymbols(symb);
  // format spin
  spinFormat.setPositivePrefix(" ");
  spinFormat.setMinimumFractionDigits(0);
  spinFormat.setMaximumFractionDigits(0);
  spinFormat.setDecimalFormatSymbols(symb);
  
  // create descriptors
  try {
        Descriptors descriptors = getDescriptors();
        // distanceUnit
        BasicPropertyDescriptor pd = new BasicPropertyDescriptor("distanceUnit", this);
        pd.setDisplayName("Distance unit");
        pd.setLabel("unit");
        
        String[] values = { RE, km };
        GUIPropertyEditor editor = new ComboBoxPropertyEditor(pd, values, values);
        pd.setPropertyEditor(editor);
        addPropertyChangeListener("distanceUnit", editor);
        descriptors.put(pd);
        
        // distance fraction digits
        pd = new BasicPropertyDescriptor("distanceFractionDigits", this);
        pd.setDisplayName("Distance fraction format");
        pd.setLabel("unit");
        
        String[] tags = { ".", ".0", ".00", ".000", ".0000" };
        editor = new ComboBoxPropertyEditor(pd, Utils.getIndexes(tags), tags);
        pd.setPropertyEditor(editor);
        addPropertyChangeListener("distanceFractionDigits", editor);
        descriptors.put(pd);
        //pd.setTextOnlyAccessible();
        
        // distance fraction digits
        pd = new BasicPropertyDescriptor("footprintFractionDigits", this);
        pd.setDisplayName("Footprint fraction format");
        editor = new ComboBoxPropertyEditor(pd, Utils.getIndexes(tags), tags);
        pd.setPropertyEditor(editor);
        addPropertyChangeListener("footprintFractionDigits", editor);
        descriptors.put(pd);
        
        // -- Time format :
        // date format
        pd = new BasicPropertyDescriptor("dateFormat", this);
        pd.setDisplayName("Date format");
        editor = new ComboBoxPropertyEditor(pd, TimeFormat.getDateFormats(), TimeFormat.getDateFormatNames());
        pd.setPropertyEditor(editor);
        addPropertyChangeListener("dateFormat", editor);
        descriptors.put(pd);
        
        
    } catch (IntrospectionException e2) {
        System.out.println(getClass().getName() + " -> " + e2.toString());
        System.exit(0);
    }
    /*
    if (!OVTCore.isServer()) {
        customizer = new TimeSettingsCustomizer(this);
        
        customizer.addPropertyChangeListener("visible", new PropertyChangeListener() {
           public void propertyChange(PropertyChangeEvent evt) {
              firePropertyChange("customizerVisible", null, null);
           } 
        });
    }*/
  
}

public Dumper getDumper() {
    return dumper;
}

protected JFrame getOrbitMonitor() {
  if (orbitMonitor == null) {
    orbitMonitor = new JFrame(getSat().getName());
    try {
      orbitMonitor.setIconImage(Toolkit.getDefaultToolkit().getImage(OVTCore.class.getClassLoader().getSystemResource("images/orbitmonitor.gif")));
    } catch (NullPointerException npe) { Log.err("FileNotFound: images/orbitmonitor.gif"); }
    orbitMonitor.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    //orbitMonitor.addWindowListener(new WindowListener() {});
    orbitMonitor.getContentPane().setLayout(new BorderLayout(0,0));
    JPanel monitorPanel = new JPanel();
    setInterior(monitorPanel);
    
    orbitMonitor.getContentPane().add(monitorPanel, BorderLayout.CENTER);
    
    orbitMonitor.pack();
    orbitMonitor.setResizable(false);
  }
  return orbitMonitor;
}

public void setVisible(boolean visible) {
  if (OVTCore.isServer()) return; // ignore "potugi" of seting orbitMonitor visible ;-)
  getOrbitMonitor().setVisible(visible);
  super.setVisible(visible);
  if (isVisible()) refresh();
}

/** Create interior  */
protected void setInterior(Container container) {
        
        GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	c.insets = new Insets(5, 2, 5, 2);
	c.anchor = GridBagConstraints.WEST;
	c.fill = GridBagConstraints.HORIZONTAL;
	container.setLayout(gridbag);

	// Create components

	time = new JTextField("2000-01-01 00:00:00");
	  time.setHorizontalAlignment(JTextField.CENTER);
        
	c.gridx = 0;
	c.gridy = 0;
	c.gridwidth = 1;
	gridbag.setConstraints(time, c);
	container.add(time);
	
	c.gridx = 0;
	c.gridy = 1;
	c.gridwidth = 1;
	JPanel comp = getPositionPanel();
	gridbag.setConstraints(comp, c);
	container.add(comp);
	
        c.gridx = 0;
	c.gridy = 2;
        c.gridwidth = 1;
	comp = getDistancePanel();
	gridbag.setConstraints(comp, c);
	container.add(comp);
        
 	c.gridx = 0;
	c.gridy = 3;
        c.gridwidth = 1;
        comp = getFootprintPanel();
	gridbag.setConstraints(comp, c);
	container.add(comp);
 
        c.gridx = 0;
	c.gridy = 4;
        c.gridwidth = 1;
	comp = getVBPanel();
	gridbag.setConstraints(comp, c);
	container.add(comp);
        
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        comp = getSpinPanel();
        gridbag.setConstraints(comp, c);
	container.add(comp);
        
        c.gridx = 0;
	c.gridy = 6;
        c.gridwidth = 1;
	JPanel panel = new JPanel();
        JButton button = new JButton("Format...");
        button.setToolTipText("Change units, precision, etc.");
        button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                new OrbitMonitorSettingsWindow(getMyself()).setVisible(true);
            }
        });
        panel.add(button);
        
        button = new JButton("Close");
        button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
            }
        });
        panel.add(button);
        
        showhideDumperButton = new JButton("Dump >>");
        showhideDumperButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                setDumperVisible(!isDumperVisible()); // switch ">>" / "<<"
            }
        });
        panel.add(showhideDumperButton);
        
	gridbag.setConstraints(panel, c);
	container.add(panel);
        
}

private OrbitMonitorModule getMyself() {
    return this;
}

protected JPanel getPositionPanel() {

	JPanel container = new JPanel();
	container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
	//container.setBorder(BorderFactory.createEtchedBorder());
        
        position = new JLabel("(Re) [GSM]");
        JPanel posLabelPanel = new JPanel();
        posLabelPanel.setLayout(new BoxLayout(posLabelPanel, BoxLayout.X_AXIS));
        posLabelPanel.add(new JLabel("Position: X Y Z R"));
        posLabelPanel.add(Box.createHorizontalGlue());
        posLabelPanel.add(position);
        
	x = new JTextField("-200.23");
          x.setHorizontalAlignment(JTextField.RIGHT);
          x.setToolTipText(DumpRecord.recDescr[DumpRecord.POS_X]);
	y = new JTextField("-200.23");
          y.setHorizontalAlignment(JTextField.RIGHT);
          y.setToolTipText(DumpRecord.recDescr[DumpRecord.POS_Y]);
	z = new JTextField("-200.23");
          z.setHorizontalAlignment(JTextField.RIGHT);
          z.setToolTipText(DumpRecord.recDescr[DumpRecord.POS_Z]);
        r = new JTextField("200.23");
          r.setHorizontalAlignment(JTextField.RIGHT);
          r.setToolTipText("R = sqrt(X^2 + Y^2 + Z^2)");
        
        dist = new JTextField("200.23");
          dist.setHorizontalAlignment(JTextField.RIGHT);
          dist.setToolTipText(DumpRecord.recDescr[DumpRecord.DIST]);
        diff = new JTextField("200.23");
         diff.setHorizontalAlignment(JTextField.RIGHT);
         diff.setToolTipText(DumpRecord.recDescr[DumpRecord.DIFF]);
        JLabel distL = new JLabel("DIST=");
          distL.setFont(dist.getFont());
          distL.setHorizontalAlignment(JLabel.RIGHT);
          distL.setToolTipText(DumpRecord.recDescr[DumpRecord.DIST]);
        JLabel diffL = new JLabel("DIFF=");
          diffL.setFont(dist.getFont());
          diffL.setHorizontalAlignment(JLabel.RIGHT);
          diffL.setToolTipText(DumpRecord.recDescr[DumpRecord.DIFF]);


        dist_to_bowshock_along_imf = new JTextField();
          dist_to_bowshock_along_imf.setHorizontalAlignment(JTextField.RIGHT);
          dist_to_bowshock_along_imf.setToolTipText(DumpRecord.recDescr[DumpRecord.DIST_TO_BS_ALONG_IMF]);
        theta_imf_bowshock_normal = new JTextField();
          theta_imf_bowshock_normal.setHorizontalAlignment(JTextField.RIGHT);
          theta_imf_bowshock_normal.setToolTipText(DumpRecord.recDescr[DumpRecord.THETA_IMF_NBS]);
        JLabel dist_to_bowshock_along_imfL = new JLabel("DIST BS IMF=");
          dist_to_bowshock_along_imfL.setFont(dist.getFont());
          dist_to_bowshock_along_imfL.setHorizontalAlignment(JLabel.RIGHT);
          dist_to_bowshock_along_imfL.setToolTipText(DumpRecord.recDescr[DumpRecord.DIST_TO_BS_ALONG_IMF]);
        JLabel theta_imf_bowshock_normalL = new JLabel("IMF^N_BS=");
          theta_imf_bowshock_normalL.setFont(dist.getFont());
          theta_imf_bowshock_normalL.setHorizontalAlignment(JLabel.RIGHT);
          theta_imf_bowshock_normalL.setToolTipText(DumpRecord.recDescr[DumpRecord.THETA_IMF_NBS]);
  
  
        JPanel posPanel = new JPanel();
        posPanel.setLayout(new GridLayout(3,4));
        posPanel.add(x);
        posPanel.add(y);
        posPanel.add(z);
        posPanel.add(r);
        posPanel.add(distL);
        posPanel.add(dist);
        posPanel.add(diffL);
        posPanel.add(diff);
        posPanel.add(dist_to_bowshock_along_imfL);
        posPanel.add(dist_to_bowshock_along_imf);
        posPanel.add(theta_imf_bowshock_normalL);
        posPanel.add(theta_imf_bowshock_normal);
	
	container.add(posLabelPanel);
        container.add(posPanel);	
	
	return container;
}

protected JPanel getVBPanel() {

	JPanel container = new JPanel();
	container.setLayout(new GridLayout(1,4));
	
        
	v = new JTextField("10 km/s");
          v.setHorizontalAlignment(JTextField.RIGHT);
	  v.setToolTipText(DumpRecord.recDescr[DumpRecord.VEL]);
        b = new JTextField("10.23 nT");
          b.setHorizontalAlignment(JTextField.RIGHT);
	  b.setToolTipText(DumpRecord.recDescr[DumpRecord.B]);

        JLabel label = new JLabel("V=");
          label.setFont(v.getFont());
          label.setHorizontalAlignment(JLabel.RIGHT);
          label.setToolTipText(DumpRecord.recDescr[DumpRecord.VEL]);
        container.add(label);
        container.add(v);
        
        label = new JLabel("B=");
          label.setFont(v.getFont());
          label.setHorizontalAlignment(JLabel.RIGHT);
          label.setToolTipText(DumpRecord.recDescr[DumpRecord.B]);
        
	container.add(label);
        container.add(b);

	return container;
}


protected JPanel getFootprintPanel() {
	JPanel container = new JPanel();
	container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
	
	// Create components
	
        footprint = new JLabel("(deg,deg) [GEO]");
        JPanel fpLabelPanel = new JPanel();
        fpLabelPanel.setLayout(new BoxLayout(fpLabelPanel, BoxLayout.X_AXIS));
        fpLabelPanel.add(new JLabel("Footprints at 100 km"));
        fpLabelPanel.add(Box.createHorizontalGlue());
        fpLabelPanel.add(footprint);
        container.add(fpLabelPanel);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2,4));
        for (int i=0; i<2; i++) {          
          for (int j=0; j<2; j++) {            
	    fp[i][j] = new JTextField("66.6");
            fp[i][j].setHorizontalAlignment(JTextField.RIGHT);
            fpL[i][j] = new JLabel("lll=");
            fpL[i][j].setHorizontalAlignment(JTextField.RIGHT);
            fpL[i][j].setFont(fp[0][0].getFont());
            panel.add(fpL[i][j]);
            panel.add(fp[i][j]);
          }
        }
        container.add(panel);
	return container;
}


protected JPanel getDistancePanel() {

	JPanel container = new JPanel();
	container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        
        dist_to_mpause  = new JTextField("200.23");
          dist_to_mpause.setHorizontalAlignment(JTextField.RIGHT);
          dist_to_mpause.setToolTipText(DumpRecord.recDescr[DumpRecord.DIST_TO_MP]);
	dist_to_bowshock = new JTextField("200.23");
          dist_to_bowshock.setHorizontalAlignment(JTextField.RIGHT);
          dist_to_bowshock.setToolTipText(DumpRecord.recDescr[DumpRecord.DIST_TO_BS]);
        
        JLabel title = new JLabel("Distance to", JLabel.LEFT);        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        titlePanel.add(title);
        titlePanel.add(Box.createHorizontalGlue());
        container.add(titlePanel);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1,4));
        
        JLabel label = new JLabel("BS=");
          label.setFont(dist_to_bowshock.getFont());
          label.setHorizontalAlignment(JLabel.RIGHT);
          label.setToolTipText(DumpRecord.recDescr[DumpRecord.DIST_TO_BS]);
        
        panel.add(label);
	panel.add(dist_to_bowshock);
	
	label = new JLabel("MP=");
          label.setFont(dist_to_mpause.getFont());
          label.setHorizontalAlignment(JLabel.RIGHT);
          label.setToolTipText(DumpRecord.recDescr[DumpRecord.DIST_TO_MP]);
        
	panel.add(label);
	panel.add(dist_to_mpause);
        
        
        container.add(panel);
	return container;
}



protected JPanel getSpinPanel() { 

	JPanel container = new JPanel();
	container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        
	JLabel title = new JLabel("Spin axis angles: B V S (deg)");
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        titlePanel.add(title);
        titlePanel.add(Box.createHorizontalGlue());
        container.add(titlePanel);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1,4));
        panel.add(new JPanel());
	for (int i=0; i<3; i++) {
          spin[i] = new JTextField("20");
          spin[i].setHorizontalAlignment(JTextField.RIGHT);
          spin[i].setToolTipText(DumpRecord.recDescr[DumpRecord.SPIN[i]]);
          panel.add(spin[i]);
        }
        
        container.add(panel);
	return container;
}



// is also executed from Sats

public void refresh() {
      int[] records = new int[DumpRecord.numOfFields];
      for (int i=0; i<DumpRecord.numOfFields; i++) records[i] = i;
      double[] values = getValues(records, getMjd());

      time.setText(timeFormat.format(values[DumpRecord.TIME]));
      position.setText("("+getDistanceUnit()+") ["+CoordinateSystem.getCoordSystem(getCS())+"]");
      
      x.setText(format.format(values[DumpRecord.POS_X]));
      y.setText(format.format(values[DumpRecord.POS_Y]));
      z.setText(format.format(values[DumpRecord.POS_Z]));
      r.setText(format.format(values[DumpRecord.R]));
      v.setText(format.format(values[DumpRecord.VEL]) + " km/s");
      b.setText(bFormat.format(values[DumpRecord.B]) + " nT");
      
      // footprints
      String latUnit = "deg", lonUnit = "deg", latTitle="lat", lonTitle="lon";
      if (getPolarCS() == CoordinateSystem.SM) {
          latUnit = "deg"; lonUnit = "h";
          latTitle="lat"; lonTitle="mlt";
      }
      footprint.setText("("+latUnit+", "+lonUnit+") ["+CoordinateSystem.getCoordSystem(getPolarCS())+"]");
      for (int i=0; i<2; i++) {
          fpL[i][0].setText(latTitle+"=");
          fpL[i][1].setText(lonTitle+"=");
          fp[i][0].setText(footprintFormat.format(values[DumpRecord.FP[i][0]]));
          fp[i][1].setText(footprintFormat.format(values[DumpRecord.FP[i][1]]));
      }
      // distance to
      dist_to_mpause.setText(format.format(values[DumpRecord.DIST_TO_MP]));
      dist_to_bowshock.setText(format.format(values[DumpRecord.DIST_TO_BS]));
      
      // foreshock
      dist.setText(format.format(values[DumpRecord.DIST]));
      diff.setText(format.format(values[DumpRecord.DIFF]));
      dist_to_bowshock_along_imf.setText(format.format(values[DumpRecord.DIST_TO_BS_ALONG_IMF]));
      theta_imf_bowshock_normal.setText(spinFormat.format(values[DumpRecord.THETA_IMF_NBS]));
      
      
      // spin
      for (int i=0; i<3; i++) spin[i].setText(spinFormat.format(values[DumpRecord.SPIN[i]]));            
}



public double[] getValues(int[] records, double mjd) {
  int inSolarWind = -1; // indicates weather a s/c is in solar wind
  //Log.log("getValues() for "+new Time(mjd)+" "+mjd);
  double[] res = new double[records.length];
  
  TrajectoryPoint trp = getTrajectory().get(mjd);
  if (trp == null) { return null; }
    
  // final double NO_FP = Double.MAX_VALUE;  
  
  double mult = 1.; // for different units (RE, km)
  if (getDistanceUnit().equals(km)) mult = Const.RE;
  
  double[] pos, bv = null, dist_diff = null;
  double[][] fp = null;
  double[] spinAngles = null;
  boolean spinAnglesPresent = true;
  boolean fieldlinesPresent = true; 
  double flBMin = Double.NaN, flBMax = Double.NaN;
  double[] flBMinPos = null; // flBMaxPos;
      
  pos = trp.get(getCS());
    
  for (int n=0; n<records.length; n++) {
    // find out if the sat is in solar wind
    if ((records[n] == DumpRecord.DIST  ||
         records[n] == DumpRecord.DIFF  ||
	 records[n] == DumpRecord.DIST_TO_BS_ALONG_IMF  ||
	 records[n] == DumpRecord.THETA_IMF_NBS ||
	 records[n] == DumpRecord.B  ||
         records[n] == DumpRecord.B_X ||
         records[n] == DumpRecord.B_Y ||
         records[n] == DumpRecord.B_Z) 
	 && inSolarWind == -1) {    
    		inSolarWind = (Bowshock99Model.isInSolarWind(trp.gsm,
			getMagProps().getSWP(mjd), getMagProps().getMachNumber(mjd))) ? 1 : 0;
    }
    
    // calculate footprints if they are not already calculated  
    if ((records[n] == DumpRecord.FP1_LAT  ||
        records[n] == DumpRecord.FP2_LAT ||
        records[n] == DumpRecord.FP1_LON  ||
        records[n] == DumpRecord.FP2_LON ) && fp == null) {
            fp = new double[2][2];
            MagPoint[] magPoints = getMagFootprints(mjd);
            MagPoint mp;
            for (int j=0; j<2; j++) {
                mp = magPoints[j];
                if (mp != null) {
                    double[] a = getTrans(mjd).gsm_trans_matrix(getPolarCS()).multiply(mp.gsm);
	            if (getPolarCS() == CoordinateSystem.GEO) 
                        fp[j] = Trans.xyz2LatLon(a); // { deg , deg}
                    else if (getPolarCS() == CoordinateSystem.SM) 
                        fp[j] = Trans.xyz2MlatMlt(a); //{"mlat ", "mlt " }
                } else fp[j] = new double[]{Double.NaN, Double.NaN}; // no fp.
            }
            // order footprints in the correct order:
            // first - [0] - maximum(latitude), second - [1] - min(lat)
            // 1st - north hem, 2nd - south hem
            if (fp[0][0] == Double.NaN) {
                fp = (fp[1][0] > 0) ? new double[][]{ fp[1], fp[0] } : 
                                             new double[][]{ fp[0], fp[1] };
            } else if (fp[1][0] == Double.NaN) {
                fp = (fp[0][0] > 0) ? new double[][]{ fp[0], fp[1] } : 
                                             new double[][]{ fp[1], fp[0] };
            } else {
                fp = (fp[0][0] > fp[1][0]) ? new double[][]{ fp[0], fp[1] } : 
                                             new double[][]{ fp[1], fp[0] };
            }
    }
    
    if ((records[n] == DumpRecord.B_FL_MIN  ||
        records[n] == DumpRecord.B_FL_MIN_POS_X  ||
        records[n] == DumpRecord.B_FL_MIN_POS_Y  ||
	records[n] == DumpRecord.B_FL_MIN_POS_Z) && fieldlinesPresent) {
	    MagPoint mp = getSat().getMainFieldlineModule().getBMinPoint(mjd);
	    if (mp == null) fieldlinesPresent = false;
	    else {
	    	flBMin = Vect.absv(mp.bv);
	    	flBMinPos = getTrans(mjd).gsm_trans_matrix(getCS()).multiply(mp.gsm);
	    }
    }
    
    if ((records[n] == DumpRecord.B_FL_MAX) && fieldlinesPresent) {
	    MagPoint mp = getSat().getMainFieldlineModule().getBMaxPoint(mjd);
	    if (mp == null) fieldlinesPresent = false;
	    else {
	    	flBMax = Vect.absv(mp.bv);
	    	//flBMaxPos = getTrans(mjd).gsm_trans_matrix(getPolarCS()).multiply(mp.gsm);
	    }
    }
    
    if ((records[n] == DumpRecord.SPIN_B  ||
        records[n] == DumpRecord.SPIN_V  ||
        records[n] == DumpRecord.SPIN_S) && spinAngles == null && spinAnglesPresent) {
            spinAngles = getSat().getSpinAngles();
            if (spinAngles == null) spinAnglesPresent = false;
    }
    
    if ((records[n] == DumpRecord.B  ||
        records[n] == DumpRecord.B_X ||
        records[n] == DumpRecord.B_Y ||
        records[n] == DumpRecord.B_Z) && bv == null) {
	    if (inSolarWind == 1)
	    	bv = getMagProps().getIMF(mjd);
            else
	        bv = getMagProps().bv(trp.gsm, mjd);
    }
    
    if ((records[n] == DumpRecord.DIST  ||
        records[n] == DumpRecord.DIFF) && dist_diff == null) {
        dist_diff = Bowshock99Model.getDIST_DIFF(trp.gsm, getMagProps().getIMF(mjd), getMagProps().getSWP(mjd), getMagProps().getMachNumber(mjd));
    }
    
    
    // by default... 
    res[n] = Double.NaN;
    switch (records[n]) {
        case DumpRecord.TIME :	res[n] = mjd; break;
        case DumpRecord.POS_X : res[n] = pos[0]*mult; break;
        case DumpRecord.POS_Y : res[n] = pos[1]*mult; break;
        case DumpRecord.POS_Z : res[n] = pos[2]*mult; break;
        case DumpRecord.R     : res[n] = Vect.absv(trp.gei)*mult; break;
        case DumpRecord.VEL  : res[n] = Vect.absv(trp.vei); break;
        case DumpRecord.B    : res[n] = Vect.absv(bv); break;
        case DumpRecord.B_X  : res[n] = bv[0]; break;
        case DumpRecord.B_Y  : res[n] = bv[1]; break;
        case DumpRecord.B_Z  : res[n] = bv[2]; break;
	case DumpRecord.B_FL_MIN : 
	    res[n] = (fieldlinesPresent) ? flBMin : Double.NaN; 
	    break;
	case DumpRecord.B_FL_MIN_POS_X : 
	    res[n] = (fieldlinesPresent) ? flBMinPos[0]*mult : Double.NaN; 
	    break;
	case DumpRecord.B_FL_MIN_POS_Y : 
	    res[n] = (fieldlinesPresent) ? flBMinPos[1]*mult : Double.NaN; 
	    break;
	case DumpRecord.B_FL_MIN_POS_Z : 
	    res[n] = (fieldlinesPresent) ? flBMinPos[2]*mult : Double.NaN; 
	    break;
	case DumpRecord.B_FL_MAX : 
	    res[n] = (fieldlinesPresent) ? flBMax : Double.NaN; 
	    break;    
        case DumpRecord.DIST_TO_MP : 
            // distance to mpause
            res[n] = mult*Shue97.distance_to_magnetopause(trp.gsm, getMagProps().getSWP(mjd), getMagProps().getIMF(mjd)[2]);
            break;
        case DumpRecord.DIST_TO_BS : 
            // distance to bowshock
            res[n] = mult*Bowshock99Model.getDistanceToBowshock(trp.gsm, getMagProps().getIMF(mjd), getMagProps().getSWP(mjd), getMagProps().getMachNumber(mjd), 0.01);
	    break;
        case DumpRecord.FP1_LAT : 
            res[n] = fp[0][0];
            break;
        case DumpRecord.FP1_LON : 
            res[n] = fp[0][1];
            break;
        case DumpRecord.FP2_LAT : 
            res[n] = fp[1][0];
            break;
        case DumpRecord.FP2_LON : 
            res[n] = fp[1][1];
            break;
        case DumpRecord.SPIN_B : 
            res[n] = (spinAnglesPresent) ? spinAngles[0]*180./Math.PI : Double.NaN;
            break;    
        case DumpRecord.SPIN_V : 
            res[n] = (spinAnglesPresent) ? spinAngles[1]*180./Math.PI : Double.NaN;
            break;    
        case DumpRecord.SPIN_S : 
            res[n] = (spinAnglesPresent) ? spinAngles[2]*180./Math.PI : Double.NaN;
            break;    
        case DumpRecord.DIP_TILT : 
            res[n] = getTrans(mjd).getDipoleTilt();
            break;
        case DumpRecord.DIST : 
            res[n] = (inSolarWind == 1) ? dist_diff[0]*mult : Double.NaN;
            break;
        case DumpRecord.DIFF : 
            res[n] = (inSolarWind == 1) ? dist_diff[1]*mult : Double.NaN;
            break;
	case DumpRecord.DIST_TO_BS_ALONG_IMF : 
            res[n] = (inSolarWind == 1) ? Bowshock99Model.getDistanceToBowshockAlongIMF(trp.gsm, getMagProps().getIMF(mjd), getMagProps().getSWP(mjd), getMagProps().getMachNumber(mjd))*mult : Double.NaN;
            break;
        case DumpRecord.THETA_IMF_NBS : 
            if (inSolarWind == 1) {
	    	  res[n] = Utils.toDegrees(Bowshock99Model.getThetaIMF_N(trp.gsm, getMagProps().getIMF(mjd), getMagProps().getSWP(mjd), getMagProps().getMachNumber(mjd)));
		 
	    } else {
		  res[n] = Double.NaN;
	    }
            break;    
	/*defalut:  WHY THIS STATEMENT IS UNREACHABLE!?!!----------
	    throw new IllegalArgumentException("Wrong record id "+records[n]);*/
      }
            
    }
    return res;
}



// ----------- events ----------------

public void timeChanged(TimeEvent e) { 
  if (isVisible()) refresh();
}

public void coordinateSystemChanged(CoordinateSystemEvent evt) { 
  if (isVisible()) refresh();
}

public void magPropsChanged(MagPropsEvent evt) {
  if (isVisible()) refresh();
}


public DumpRecord getDumpRecord(){
  return (DumpRecord)this.dumpRecord.clone();
}

/** Getter for property distanceUnit.
 * @return Value of property distanceUnit.
 */
public String getDistanceUnit() {
    return distanceUnit;
}

/** Setter for property distanceUnit.
 * @param distanceUnit New value of property distanceUnit.
 */
public void setDistanceUnit(String distanceUnit) {
    if (!distanceUnit.equals(RE) && !distanceUnit.equals(km))
        throw new IllegalArgumentException("Invalid distance unit : '"+distanceUnit+"'");
    String oldDistanceUnit = this.distanceUnit;
    this.distanceUnit = distanceUnit;
    propertyChangeSupport.firePropertyChange ("distanceUnit", oldDistanceUnit, distanceUnit);
    if (isVisible()) refresh();
}

/** Getter for property distanceFractionDigits.
 * @return Value of property distanceFractionDigits.
 */
public int getDistanceFractionDigits() {
    return format.getMinimumFractionDigits(); // or max, doesn't metter
}

/** Setter for property distanceFractionDigits.
 * @param distanceFractionDigits New value of property distanceFractionDigits.
 */
public void setDistanceFractionDigits(int distanceFractionDigits) {
    if (distanceFractionDigits < 0 || distanceFractionDigits > 4)
        throw new IllegalArgumentException("Precision can be between 0 and 4");
    int oldDistanceFractionDigits = getDistanceFractionDigits();
    format.setMinimumFractionDigits(distanceFractionDigits);
    format.setMaximumFractionDigits(distanceFractionDigits);
    propertyChangeSupport.firePropertyChange ("distanceFractionDigits", new Integer (oldDistanceFractionDigits), new Integer (distanceFractionDigits));
    if (isVisible()) refresh();
}

/** Getter for property distanceFractionDigits.
 * @return Value of property distanceFractionDigits.
 */
public int getFootprintFractionDigits() {
    return format.getMinimumFractionDigits(); // or max, doesn't metter
}

/** Setter for property distanceFractionDigits.
 * @param distanceFractionDigits New value of property distanceFractionDigits.
 */
public void setFootprintFractionDigits(int fractionDigits) {
    if (fractionDigits < 0 || fractionDigits > 4)
        throw new IllegalArgumentException("Precision can be between 0 and 4");
    int oldfractionDigits = getDistanceFractionDigits();
    footprintFormat.setMinimumFractionDigits(fractionDigits);
    footprintFormat.setMaximumFractionDigits(fractionDigits);
    propertyChangeSupport.firePropertyChange ("distanceFractionDigits", new Integer (oldfractionDigits), new Integer (fractionDigits));
    if (isVisible()) refresh();
}

/** Getter for property dumperVisible.
 * @return Value of property dumperVisible.
 */
public boolean isDumperVisible() {
    return dumperVisible;
}

/** Setter for property dumperVisible.
 * @param dumperVisible New value of property dumperVisible.
 */
public void setDumperVisible(boolean dumperVisible) {
    if (this.dumperVisible == dumperVisible) return; // nothing have changed
    boolean oldDumperVisible = this.dumperVisible;
    this.dumperVisible = dumperVisible;
    Container cont = getOrbitMonitor().getContentPane();
    if (dumperVisible) {
        JPanel panel = dumper.getPanel();
        cont.add(panel, BorderLayout.SOUTH);
        showhideDumperButton.setText("<< Dumper");
        //panel.setMaximumSize(panel.getSize());
        getOrbitMonitor().setResizable(true);
        getOrbitMonitor().pack();
        getOrbitMonitor().setResizable(false);
    } else {
        cont.remove(dumper.getPanel());
        showhideDumperButton.setText("Dumper >>");  
        getOrbitMonitor().setResizable(true);
        getOrbitMonitor().pack();
        getOrbitMonitor().setResizable(false);
    }
    propertyChangeSupport.firePropertyChange ("dumperVisible", new Boolean (oldDumperVisible), new Boolean (dumperVisible));
}

public TimeFormat getTimeFormat() {
    return timeFormat;
}


/** Getter for property dateFormat.
 * @return Value of property dateFormat.
 */
public int getDateFormat() {
    return timeFormat.getDateFormat();
}

/** Setter for property dateFormat.
 * @param dateFormat New value of property dateFormat.
 */
public void setDateFormat(int dateFormat) throws IllegalArgumentException {
    int oldDateFormat = getDateFormat();
    timeFormat.setDateFormat(dateFormat);
    propertyChangeSupport.firePropertyChange ("dateFormat", new Integer (oldDateFormat), new Integer (dateFormat));
    if (isVisible()) refresh();
}

}

class OrbitMonitorSettingsWindow extends JDialog {
    
    OrbitMonitorSettingsWindow(OrbitMonitorModule orbitMonitorModule) {
        //super((JDialog)orbitMonitorModule.getOrbitMonitor(), "Orbit Monitor Settings", true);
        super(orbitMonitorModule.getCore().getXYZWin(), "Orbit Monitor Format", true);
        Container cont = getContentPane();
        //create layout : new java.awt.GridLayout (4, 1, 5, 5)
        cont.setLayout(new GridLayout (4, 1, 5, 5));
        
        // -------- Distance panel -------
        Descriptors desc = orbitMonitorModule.getDescriptors();
        JComboBox dateFormatCB = (JComboBox)((ComponentPropertyEditor)(desc.getDescriptor("dateFormat").getPropertyEditor())).getComponent();
        JComboBox distUnitsCB = (JComboBox)((ComponentPropertyEditor)(desc.getDescriptor("distanceUnit").getPropertyEditor())).getComponent();
        JComboBox distFractionDigitsCB = (JComboBox)((ComponentPropertyEditor)(desc.getDescriptor("distanceFractionDigits").getPropertyEditor())).getComponent();
        JComboBox footprintFractionDigitsCB = (JComboBox)((ComponentPropertyEditor)(desc.getDescriptor("footprintFractionDigits").getPropertyEditor())).getComponent();
        
        JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder("Time"));
            panel.add(dateFormatCB);
        cont.add(panel);
        
        panel = new JPanel();
            panel.setBorder(new TitledBorder("Distance"));
            panel.add(new JLabel("unit : "));
            panel.add(distUnitsCB);
            panel.add(new JLabel(" precision : "));
            panel.add(distFractionDigitsCB);
        cont.add(panel);
        
        panel = new JPanel();
            panel.setBorder(new TitledBorder("Footprints"));
            panel.add(new JLabel(" precision : "));
            panel.add(footprintFractionDigitsCB);
        cont.add(panel);
        
        // ------------------- ok button ----------------
        
        JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    dispose();
                }
            });
            okButton.setMargin(new Insets(10, 40, 10, 40));
            okButton.setVerticalAlignment(SwingConstants.CENTER);
            panel = new JPanel();
            panel.add(okButton);
        cont.add(panel);
        
        pack();
        //setResizable(false);
        
        // senter the window
        Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(scrnSize.width/2 - windowSize.width/2,
                 scrnSize.height/2 - windowSize.height/2);
    }
    
}



