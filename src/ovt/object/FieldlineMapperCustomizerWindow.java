/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/object/FieldlineMapperCustomizerWindow.java,v $
Date:      $Date: 2003/09/28 17:52:47 $
Version:   $Revision: 1.5 $


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
 * FieldlineMapperCustomizer.java
 *
 * Created on June 25, 2002, 5:33 PM
 */

package ovt.object;

import ovt.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.beans.editor.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;
import ovt.object.editor.*;

import vtk.*;

import java.io.*;
import java.util.*;
import java.lang.*;
import java.beans.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.border.*;


/**
 *
 * @author  ko
 * @version 
 */
public class FieldlineMapperCustomizerWindow extends JFrame implements Customizer {
    
    private static final int DEBUG=9;
    /** helps to create a name (ID) for the new point. It should be "new"+(newRowID++) */
    private int newRowID = 0;
    private FieldlineMapper fieldlineMapper;
    
    private StringEditor nameEditor;
    private JTable table;
    
    private javax.swing.JButton insertButton, deleteButton, applyButton,closeButton,moveUpButton,moveDownButton;

public FieldlineMapperCustomizerWindow() {
    super();
    try {
        setIconImage (Toolkit.getDefaultToolkit().getImage(Utils.findResource("images/fl_mapper.gif")));
    } catch (FileNotFoundException e2) { e2.printStackTrace(System.err); }
    
    // name editor
    nameEditor = new StringEditor();
    nameEditor.addPropertyChangeListener( new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            fieldlineMapper.setName(nameEditor.getAsText());
            firePropertyChange("name", null, null);
        }
    });

    // create table (TableModel editor)
    table = new JTable ();
    
    // add selection listener
    table.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
            //Ignore extra messages.
            if (e.getValueIsAdjusting()) return;

            ListSelectionModel lsm = (ListSelectionModel)e.getSource();

            if (lsm.isSelectionEmpty()) {
                //no rows are selected
                deleteButton.setEnabled(false);
                moveUpButton.setEnabled(false);
                moveDownButton.setEnabled(false);
            } else {
                deleteButton.setEnabled(true);
                // if only one row is selected - enable move buttons
                if (lsm.getMaxSelectionIndex() == lsm.getMinSelectionIndex()) {
                    boolean upEnabled = true; boolean downEnabled = true;
                    if (lsm.getMinSelectionIndex() == table.getRowCount()-1) 
                        downEnabled = false;
                    if (lsm.getMinSelectionIndex() == 0) upEnabled = false;

                    moveUpButton.setEnabled(upEnabled);
                    moveDownButton.setEnabled(downEnabled);
                } else {
                    moveUpButton.setEnabled(false);
                    moveDownButton.setEnabled(false);
                }
                int selectedRow = lsm.getMinSelectionIndex();
                Log.log("getMinSelectionIndex="+lsm.getMinSelectionIndex()+" getMaxSelectionIndex="+lsm.getMaxSelectionIndex(),DEBUG);
            }
        }
    });

    initComponents();
    pack();
}

public void setObject(Object bean) {
    if (!(bean instanceof FieldlineMapper)) 
        throw new IllegalArgumentException("This object is not FieldlineMapper ("+bean+")");
    this.fieldlineMapper = (FieldlineMapper)bean;
    nameEditor.setValue(fieldlineMapper.getName());
    table.setModel(fieldlineMapper.getStartPointsDataModel());
    setTitle("Field Line Mapper "+CoordinateSystem.getCoordSystem(fieldlineMapper.getBindCS())+" - Based");
}

private DefaultTableModel getTableModel() {
    return fieldlineMapper.getStartPointsDataModel();
}

private void initComponents () {
    //setIconImage(Toolkit.getDefaultToolkit().getImage(magProps.getCore().getImagesDir()+"ovt.gif"));
    
    // create name panel
    
    JPanel namePanel = new JPanel();
    namePanel.setLayout(new BoxLayout(namePanel,BoxLayout.X_AXIS));
    
    namePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    namePanel.add(new JLabel("Name:"));
    namePanel.add(Box.createHorizontalStrut(5));
    JTextField tf = nameEditor.getStringEditorPanel();
    tf.setPreferredSize( new Dimension(150, tf.getPreferredSize().height));
    namePanel.add(tf); // name property
    namePanel.add(Box.createHorizontalGlue());
    
    JPanel buttonsPanel = createButtonsPanel();
    
    JScrollPane jScrollPane1 = new JScrollPane ();
    jScrollPane1.setViewportView (table);
    jScrollPane1.setBorder(BorderFactory.createTitledBorder("Fieldline start points"));
    
    // adjust the height of Table
    jScrollPane1.setPreferredSize( 
        new Dimension( jScrollPane1.getPreferredSize().width, 
                       buttonsPanel.getPreferredSize().height*2)
    );

    // add all those panels to content pane
    
    getContentPane ().add (namePanel, java.awt.BorderLayout.NORTH);
    
    getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);
    
    getContentPane ().add (buttonsPanel, java.awt.BorderLayout.EAST);

}

private JPanel createButtonsPanel() {
    JPanel jPanel = new JPanel ();
    jPanel.setLayout(new java.awt.GridLayout (6, 1));
    
    moveUpButton = new JButton ("Move Up");
    moveUpButton.setEnabled(false);
    moveUpButton.addActionListener (new java.awt.event.ActionListener () {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        int i = table.getSelectedRow();
        if (i-1 >= 0) {
            getTableModel().moveRow(i,i,i-1);
            table.getSelectionModel().setSelectionInterval(i-1,i-1);
        }
      }
    }
    );

    jPanel.add (moveUpButton);

    moveDownButton = new JButton ("Move Down");
    moveDownButton.setEnabled(false);
    moveDownButton.addActionListener (new java.awt.event.ActionListener () {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        int i = table.getSelectedRow();
        if (i+1 < getTableModel().getRowCount()) {
            getTableModel().moveRow(i,i,i+1);
            table.getSelectionModel().setSelectionInterval(i+1,i+1);
        }
      }
    }
    );

    jPanel.add (moveDownButton);

    
    insertButton = new JButton ("Insert row");
    insertButton.addActionListener (new java.awt.event.ActionListener () {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        insertRow();
      }
    }
    );

    jPanel.add (insertButton);

    deleteButton = new JButton ("Remove row(s)");
    deleteButton.setEnabled(false);
    deleteButton.addActionListener (new java.awt.event.ActionListener () {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        deleteRows();
      }
    });

    jPanel.add (deleteButton);

    applyButton = new JButton ("Apply");
    applyButton.addActionListener (new java.awt.event.ActionListener () {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        applyButtonActionPerformed ();
      }
    }
    );

    jPanel.add (applyButton);

    closeButton = new JButton ("Close");
    closeButton.addActionListener (new java.awt.event.ActionListener () {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        closeButtonActionPerformed (evt);
      }
    }
    );

    jPanel.add (closeButton);
    getRootPane().setDefaultButton(closeButton);
    
    return jPanel;
}

private void applyButtonActionPerformed () {
    if (fieldlineMapper.updateStartPoints()) {
        if (fieldlineMapper.isVisible()) {
            fieldlineMapper.hide();
            fieldlineMapper.show();
            fieldlineMapper.Render();
        }
    }
    //MagPropsEvent evt = new MagPropsEvent(this, dataModel.getIndex());
    //magProps.fireMagPropsChange(evt);
    //getCore().Render();
}

private void insertRow() {
  int rw = 0;
  int row = table.getSelectedRow();
  //Log.log("Selected row is " + row, 0);
  if ( row >= 0 ){
    int l = table.getSelectedRowCount();
    rw = row + l - 1;
  }

  if (table.getRowCount() == 0) row = -1;

  Log.log("Inserting row after " + row, DEBUG);  

  getTableModel().insertRow(row+1, new Object[]{"new"+(newRowID++), "", ""});
  
  table.setRowSelectionInterval(row+1,row+1);
 }

private void closeButtonActionPerformed (java.awt.event.ActionEvent evt) {
  setVisible(false);
}

private void deleteRows() {
  if (table.getSelectedRowCount() > 0) {
    int[] rows = table.getSelectedRows();
    for (int i=rows.length-1; i>=0; i--) {
        Log.log("Deleting row " + rows[i], DEBUG);
        getTableModel().removeRow(rows[i]);
    }
    // add default values row if all rows where deleted.
    //if (dataModel.getRowCount() == 0) dataModel.reset();
    // select last row if the very last row was removed
    Log.log("Selected row after deleteRows: " + table.getSelectedRow(), DEBUG);
    int rowToSelect = rows[rows.length - 1] - rows.length;
    if (rowToSelect < 0) rowToSelect = 0;
    table.getSelectionModel().setSelectionInterval(rowToSelect, rowToSelect);
    
  } 
  /*  JOptionPane.showMessageDialog(this,
                             "No rows selected.",
                             "Error",
                             JOptionPane.ERROR_MESSAGE); */
  
}

  /** Exit the Application */
  private void exitForm(java.awt.event.WindowEvent evt) {
    this.setVisible(false);
  }

  
}
