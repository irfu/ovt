/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/gui/StatusLine.java,v $
  Date:      $Date: 2003/09/28 17:52:41 $
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
 * StatusLine.java
 *
 * Created on March 31, 2000, 3:34 PM
 */
 
package ovt.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
/** 
 *
 * @author  ko
 * @version 
 */
public class StatusLine extends JPanel {

  private static final int ONE_SECOND = 1000;
    
  protected JLabel label;
  private String status = " idle ";
  private int progress = 0;
  //private JProgressBar progressBar;
  private ProgressMonitor progressMonitor;
  private Timer timer;
  
  /** Creates new StatusLine */
  public StatusLine() {
    super();
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    //setLayout(new BorderLayout());
    label = new JLabel("Started.");
    label.setFont(new Font("Arial", Font.PLAIN, 12));
    label.setToolTipText("Satus line");
    label.setAlignmentX(LEFT_ALIGNMENT);
    add(label);
    
/*    progressBar = new JProgressBar(0, 100);
    progressBar.setValue(0);
    progressBar.setStringPainted(true);
    add(progressBar); */
    
    setBorder(BorderFactory.createLoweredBevelBorder());
    //Create a timer.
    //timer = new Timer(ONE_SECOND/10, new TimerListener());
    //timer.start();
  }
  
  public void setStatus(String status) {
    this.status = status;
  }
    
  class TimerListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            if (progressMonitor.isCanceled()) { //|| task.done()) {
                progressMonitor.close();
                //task.stop();
                Toolkit.getDefaultToolkit().beep();
                timer.stop();
            } else {
                progressMonitor.setNote(status);
                progress += 2;
                if (progress > 100) progress = 0;
                progressMonitor.setProgress(progress);
                System.out.println("Progress : " + progress);
            }
        }
    }

 public void showProgressMonitor() {
            progressMonitor = new ProgressMonitor(StatusLine.this,
                                      "Running a Long Task",
                                      "", 0, 100);
            progressMonitor.setProgress(0);
            progressMonitor.setMillisToDecideToPopup(2 * ONE_SECOND);

            timer.start();
 }
  
}
