/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/MagPropsCustomizer.java,v $
  Date:      $Date: 2003/09/28 17:52:43 $
  Version:   $Revision: 2.2 $


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
 * MagPropsEditor.java
 *
 * Class for a window that offers magnetic field-related settings.
 *
 * Created on den 3 april 2000, 16:06
 */

package ovt.mag;

import ovt.util.*;
import ovt.event.*;
import ovt.interfaces.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Class representing window for settings for magnetic field and "magnetic
 * activity indexes".
 *
 * @author  yuri
 * @version
 */
public class MagPropsCustomizer extends JFrame implements MagPropsChangeListener, ChangeListener {
    private final MagProps magProps;

  /** Creates new form MagPropsEditor */
  public MagPropsCustomizer(MagProps magProps, JFrame owner) {
    super("Magnetic Field");
    
    Log.log("MagPropsEditor :: init ...", 3);
    this.magProps = magProps;
    
    
    //getContentPane().setLayout(new FlowLayout(FlowLayout.TRAILING)); */
    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
    initComponents ();
    refresh();
    pack ();
    setResizable(false);
    setLocation(owner.getLocation().x + owner.getSize().width, owner.getLocation().y);
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the FormEditor.
   */
  private void initComponents () {
    try {
        setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(Utils.findResource("images/magnet.gif")));
    } catch (java.io.FileNotFoundException e2) { e2.printStackTrace(System.err); }
    
    activityPanel = new javax.swing.JPanel ();
    kpindexLabel = new javax.swing.JLabel ();
    kpindexEditBtn = new javax.swing.JButton ();
    imfLabel = new javax.swing.JLabel ();
    imfEditBtn = new javax.swing.JButton ();
    swpLabel = new javax.swing.JLabel ();
    swpEditBtn = new javax.swing.JButton ();
    dstindexLabel = new javax.swing.JLabel ();
    dstindexEditBtn = new javax.swing.JButton ();
    buttonPanel = new javax.swing.JPanel ();
    OKBtn = new javax.swing.JButton ();
    cancelBtn = new javax.swing.JButton ();
    applyBtn = new javax.swing.JButton ();
    modelsPanel = new javax.swing.JPanel ();
    internalPanel = new javax.swing.JPanel ();
    externalPanel = new javax.swing.JPanel ();
    
    addWindowListener (new java.awt.event.WindowAdapter () {
      public void windowClosing (java.awt.event.WindowEvent evt) {
        exitForm (evt);
      }
    }
    );

    activityPanel.setLayout (new java.awt.GridLayout (6, 2));
    activityPanel.setBorder (new javax.swing.border.TitledBorder(
    new javax.swing.border.EtchedBorder(java.awt.Color.white, new java.awt.Color (134, 134, 134)),
    "Activity", 1, 2, new java.awt.Font ("Dialog", 0, 11), java.awt.Color.black));
    activityPanel.setName ("Activity");

      kpindexLabel.setAlignmentX (10.0F);
      kpindexLabel.setText (ovt.mag.MagProps.KPINDEX_STR);
      kpindexLabel.setHorizontalAlignment (javax.swing.SwingConstants.LEFT);
  
      activityPanel.add (kpindexLabel);
  
      kpindexEditBtn.setPreferredSize (kpindexEditBtn.getMinimumSize ());
      kpindexEditBtn.setMaximumSize (kpindexEditBtn.getPreferredSize ());
      kpindexEditBtn.setText ("Edit ...");
      kpindexEditBtn.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          kpindexEditBtnActionPerformed (evt);
        }
      }
      );
  
      activityPanel.add (kpindexEditBtn);
  
      imfLabel.setText (ovt.mag.MagProps.IMF_STR);
      imfLabel.setHorizontalAlignment (javax.swing.SwingConstants.LEFT);
  
      activityPanel.add (imfLabel);
  
      imfEditBtn.setText ("Edit ...");
      imfEditBtn.setEnabled (false);
      imfEditBtn.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          imfEditBtnActionPerformed (evt);
        }
      }
      );
  
      activityPanel.add (imfEditBtn);
  
      swpLabel.setText (ovt.mag.MagProps.SWP_STR);
      swpLabel.setHorizontalAlignment (javax.swing.SwingConstants.LEFT);
  
      activityPanel.add (swpLabel);
  
      swpEditBtn.setText ("Edit ...");
      swpEditBtn.setEnabled (false);
      swpEditBtn.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          swpEditBtnActionPerformed (evt);
        }
      }
      );
  
      activityPanel.add (swpEditBtn);
  
      dstindexLabel.setText (ovt.mag.MagProps.DSTINDEX_STR);
      dstindexLabel.setHorizontalAlignment (javax.swing.SwingConstants.LEFT);
  
      activityPanel.add (dstindexLabel);
  
      dstindexEditBtn.setText ("Edit ...");
      dstindexEditBtn.setEnabled (false);
      dstindexEditBtn.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          dstindexEditBtnActionPerformed (evt);
        }
      } );
  
      activityPanel.add (dstindexEditBtn);
  
      // G1
      
      activityPanel.add (new JLabel("G1"));
  
      G1EditBtn = new JButton("Edit ...");
      G1EditBtn.setEnabled (false);
      G1EditBtn.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
            java.awt.Point pp = getLocation();
            //magProps.activityEditors[MagProps.G1].setLocation(pp.x + 10, pp.y + 10);
            //magProps.activityEditors[MagProps.G1].setVisible(true);
            magProps.setActivityEditorLocation(MagProps.G1, pp.x + 10, pp.y + 10);
            magProps.setActivityEditorVisible(MagProps.G1, true);
            
        }
      } );
  
      activityPanel.add (G1EditBtn);
      
      // G1
      
      activityPanel.add (new JLabel("G2"));
  
      G2EditBtn = new JButton("Edit ...");
      G2EditBtn.setEnabled (false);
      G2EditBtn.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
            java.awt.Point pp = getLocation();
            //magProps.activityEditors[MagProps.G2].setLocation(pp.x + 10, pp.y + 10);
            //magProps.activityEditors[MagProps.G2].setVisible(true);
            magProps.setActivityEditorLocation(MagProps.G2, pp.x + 10, pp.y + 10);
            magProps.setActivityEditorVisible(MagProps.G2, true);
        }
      } );
  
      activityPanel.add (G2EditBtn);
  
    
    
    buttonPanel.setLayout (new java.awt.GridLayout (1, 3));

      OKBtn.setText ("OK");
      getRootPane().setDefaultButton(OKBtn);
      OKBtn.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          OKBtnActionPerformed ();
        }
      }
      );
  
      buttonPanel.add (OKBtn);
  
      cancelBtn.setText ("Cancel");
      cancelBtn.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          cancelBtnActionPerformed (evt);
        }
      }
      );
  
      buttonPanel.add (cancelBtn);
  
      applyBtn.setText ("Apply");
      applyBtn.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          applyBtnActionPerformed ();
        }
      }
      );
  
      buttonPanel.add (applyBtn);
  

    modelsPanel.setLayout (new java.awt.GridLayout (1, 2));
        modelsPanel.add (getInternalModelPanel());
        modelsPanel.add (getExternalModelPanel());
  
    getContentPane ().add (modelsPanel);
    getContentPane ().add (activityPanel);
    getContentPane ().add(getMPClippingPanel());
    getContentPane ().add (buttonPanel);
  }

  private JPanel getInternalModelPanel() {
      JPanel panel = new JPanel();
      panel.setLayout (new java.awt.GridLayout (2, 1));
      panel.setBorder (new javax.swing.border.TitledBorder(
      new javax.swing.border.EtchedBorder(java.awt.Color.white, new java.awt.Color (134, 134, 134)),
      "Internal Model", 1, 2, new java.awt.Font ("Dialog", 0, 11), java.awt.Color.black));
      
      /*Component intModelComp = ((ComponentPropertyEditor)magProps.getDescriptors().getDescriptor("internalModelType").getPropertyEditor()).getComponent();
      panel.add(intModelComp);*/
      
      ButtonGroup group = new ButtonGroup();
      
      igrfRButton = new JRadioButton ("IGRF");
      igrfRButton.addChangeListener ( this );
      group.add(igrfRButton);
      
      dipoleRButton = new JRadioButton ("Dipole");
      dipoleRButton.addChangeListener ( this );
      group.add(dipoleRButton);
      
      panel.add (igrfRButton);
      panel.add (dipoleRButton);
      return panel;
  }
  
  private JPanel getExternalModelPanel() {
      JPanel panel = new JPanel();
      panel.setLayout (new java.awt.GridLayout (4, 1));
      panel.setBorder (new javax.swing.border.TitledBorder(
      new javax.swing.border.EtchedBorder(java.awt.Color.white, new java.awt.Color (134, 134, 134)),
      "External model", 3, 2, new java.awt.Font ("Dialog", 0, 11), java.awt.Color.black));
      /*Component comp = ((ComponentPropertyEditor)magProps.getDescriptors().getDescriptor("externalModelType").getPropertyEditor()).getComponent();
      panel.add(comp);*/
      ButtonGroup group = new ButtonGroup();
      
        t87RButton = new JRadioButton ("Tsyganenko 87");
        group.add(t87RButton);
        t87RButton.addChangeListener ( this );
        panel.add (t87RButton);
        
        t89RButton = new JRadioButton ("Tsyganenko 89");
        group.add(t89RButton);
        t89RButton.addChangeListener (this);
        panel.add (t89RButton);
        
        t96RButton = new JRadioButton ("Tsyganenko 96");
        group.add(t96RButton);
        t96RButton.addChangeListener ( this );
        panel.add (t96RButton); 
        
        t2001RButton = new JRadioButton ("Tsyganenko 2001");
        group.add(t2001RButton);
        t2001RButton.addChangeListener ( this );
        panel.add (t2001RButton); 
        
      return panel;
  }
  
  private JPanel getMPClippingPanel() {
     JPanel panel = new JPanel();
     panel.setBorder(new javax.swing.border.TitledBorder(
        new javax.swing.border.EtchedBorder(java.awt.Color.white, new java.awt.Color (134, 134, 134)),
        "Clipping", 1, 2, new java.awt.Font ("Dialog", 0, 11), java.awt.Color.black));
     //mpClippingChB = (JCheckBox)((ComponentPropertyEditor)magProps.getDescriptors().getDescriptor("mPClipping").getPropertyEditor()).getComponent();
     
     mpClippingChB = new JCheckBox("Clip on magnetopause");
     
     panel.add(mpClippingChB);
     return panel;
  }
  
  private void doCancel() {
    setVisible(false);
    refresh();
  }

  private void cancelBtnActionPerformed (java.awt.event.ActionEvent evt) {
    doCancel();
  }

  private void OKBtnActionPerformed () {
    setVisible(false);
    applyBtnActionPerformed();
  }

  /** Execute actions that should follow when pressing the "Apply" button. */
  private void applyBtnActionPerformed () {
      if (valuesChanged()) {
        magProps.setInternalModelType(getIntModel());
        magProps.setExternalModelType(getExtModel());
        magProps.setMPClipping(isMPClipping());
        magProps.fireMagPropsChange();
        magProps.getCore().Render();
      }
  }

  private void dstindexEditBtnActionPerformed (java.awt.event.ActionEvent evt) {
    java.awt.Point pp = this.getLocation();
    //magProps.activityEditors[MagProps.DSTINDEX].setLocation(pp.x + 10, pp.y + 10);
    //magProps.activityEditors[MagProps.DSTINDEX].setVisible(true);
    magProps.setActivityEditorLocation(MagProps.DSTINDEX, pp.x + 10, pp.y + 10);
    magProps.setActivityEditorVisible(MagProps.DSTINDEX, true);
  }

  private void swpEditBtnActionPerformed (java.awt.event.ActionEvent evt) {
    java.awt.Point pp = this.getLocation();
    //magProps.activityEditors[MagProps.SWP].setLocation(pp.x + 10, pp.y + 10);
    //magProps.activityEditors[MagProps.SWP].setVisible(true);
    magProps.setActivityEditorLocation(MagProps.SWP, pp.x + 10, pp.y + 10);
    magProps.setActivityEditorVisible(MagProps.SWP, true);
  }

  private void imfEditBtnActionPerformed (java.awt.event.ActionEvent evt) {
    java.awt.Point pp = this.getLocation();
    //magProps.activityEditors[MagProps.IMF].setLocation(pp.x + 10, pp.y + 10);
    //magProps.activityEditors[MagProps.IMF].setVisible(true);
    magProps.setActivityEditorLocation(MagProps.IMF, pp.x + 10, pp.y + 10);
    magProps.setActivityEditorVisible(MagProps.IMF, true);
  }

  private void kpindexEditBtnActionPerformed (java.awt.event.ActionEvent evt) {
    java.awt.Point pp = this.getLocation();
    //magProps.activityEditors[MagProps.KPINDEX].setLocation(pp.x + 10, pp.y + 10);
    //magProps.activityEditors[MagProps.KPINDEX].setVisible(true);
    magProps.setActivityEditorLocation(MagProps.KPINDEX, pp.x + 10, pp.y + 10);
    magProps.setActivityEditorVisible(MagProps.KPINDEX, true);
  }

    @Override
  public void stateChanged(javax.swing.event.ChangeEvent evt) {
      refreshActivityButtons();
      refreshApplyButtonState();
  }

  
  

  /** Exit the Application */
  private void exitForm(java.awt.event.WindowEvent evt) {
    doCancel();
  }

    @Override
  public void magPropsChanged(MagPropsEvent evt) {
      refresh();
  }  



  
  private boolean valuesChanged() {
    return ( getIntModel() != magProps.getInternalModelType() ||
                getExtModel() != magProps.getExternalModelType() ||
                isMPClipping() != magProps.isMPClipping() );
  }
  
  // Variables declaration - do not modify
  private javax.swing.JPanel activityPanel;
  private javax.swing.JLabel kpindexLabel;
  private javax.swing.JButton kpindexEditBtn;
  private javax.swing.JLabel imfLabel;
  private javax.swing.JButton imfEditBtn;
  private javax.swing.JLabel swpLabel;
  private javax.swing.JButton swpEditBtn;
  private javax.swing.JLabel dstindexLabel;
  private javax.swing.JButton dstindexEditBtn;
  private javax.swing.JButton G1EditBtn;
  private javax.swing.JButton G2EditBtn;
  private javax.swing.JPanel buttonPanel;
  private javax.swing.JButton OKBtn;
  private javax.swing.JButton cancelBtn;
  private javax.swing.JButton applyBtn;
  private javax.swing.JPanel modelsPanel;
  private javax.swing.JPanel internalPanel;
  private javax.swing.JRadioButton igrfRButton;
  private javax.swing.JRadioButton dipoleRButton;
  private javax.swing.JPanel externalPanel;
  private javax.swing.JRadioButton t87RButton;
  private javax.swing.JRadioButton t89RButton;
  private javax.swing.JRadioButton t96RButton;
  private javax.swing.JRadioButton t2001RButton;
  private JCheckBox mpClippingChB;
  // End of variables declaration

  private void refresh() {
      t87RButton.removeChangeListener(this);
      t89RButton.removeChangeListener(this);
      t96RButton.removeChangeListener(this);
      t2001RButton.removeChangeListener(this);
      igrfRButton.removeChangeListener(this);
      dipoleRButton.removeChangeListener(this);
      mpClippingChB.removeChangeListener(this);
      
      t87RButton.setSelected (magProps.getExternalModelType()== MagProps.T87);
      t89RButton.setSelected (magProps.getExternalModelType()== MagProps.T89);
      t96RButton.setSelected (magProps.getExternalModelType()== MagProps.T96);
      t2001RButton.setSelected (magProps.getExternalModelType()== MagProps.T2001);
      igrfRButton.setSelected (magProps.getInternalModelType()== MagProps.IGRF);
      dipoleRButton.setSelected (magProps.getInternalModelType()== MagProps.DIPOLE);
      mpClippingChB.setSelected(magProps.isMPClipping());
      
      t87RButton.addChangeListener(this);
      t89RButton.addChangeListener(this);
      t96RButton.addChangeListener(this);
      t2001RButton.addChangeListener(this);
      igrfRButton.addChangeListener(this);
      dipoleRButton.addChangeListener(this);
      mpClippingChB.addChangeListener(this);
      
      refreshActivityButtons();
      refreshApplyButtonState();
  }
  
  private void refreshApplyButtonState() {
    applyBtn.setEnabled(valuesChanged());
  }
  
  /** Disable/enable buttonts depending on the chosen external model. */
  private void refreshActivityButtons() {
      final int extModel = getExtModel();
      switch (extModel) {
          case MagProps.T87 : 
              kpindexEditBtn.setEnabled (true);
              imfEditBtn.setEnabled (false);
              swpEditBtn.setEnabled (false);
              dstindexEditBtn.setEnabled (false);
              G1EditBtn.setEnabled (false);
              G2EditBtn.setEnabled (false);
              break;
          case MagProps.T89 : 
              kpindexEditBtn.setEnabled (true);
              imfEditBtn.setEnabled (false);
              swpEditBtn.setEnabled (false);
              dstindexEditBtn.setEnabled (false);
              G1EditBtn.setEnabled (false);
              G2EditBtn.setEnabled (false);
              break;    
          case MagProps.T96 : 
              kpindexEditBtn.setEnabled (false);
              imfEditBtn.setEnabled (true);
              swpEditBtn.setEnabled (true);
              dstindexEditBtn.setEnabled (true);
              G1EditBtn.setEnabled (false);
              G2EditBtn.setEnabled (false);
              break;
          case MagProps.T2001 : 
              kpindexEditBtn.setEnabled (false);
              imfEditBtn.setEnabled (true);
              swpEditBtn.setEnabled (true);
              dstindexEditBtn.setEnabled (true);
              G1EditBtn.setEnabled (true);
              G2EditBtn.setEnabled (true);
              break;    
      }
  }
  
  private int getIntModel() {
    if ( igrfRButton.isSelected() ) return MagProps.IGRF;
    else return MagProps.DIPOLE;
  }
  
  private int getExtModel() {
    if ( t87RButton.isSelected() ) return MagProps.T87;
    else if ( t89RButton.isSelected() )  return MagProps.T89;
    else if ( t96RButton.isSelected() )  return MagProps.T96;
    else return MagProps.T2001;
  }
  
  private boolean isMPClipping() {
    return mpClippingChB.isSelected();
  }
  
 
  
}


