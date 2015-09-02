/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /stor/devel/ovt2g/ovt/mag/MagActivityDataEditor.java,v $
 Date:      $Date: 2006/04/19 10:11:53 $
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
 * MagActivityDataEditor.java
 *
 * Created on den 25 mars 2000, 21:57
 */
package ovt.mag;

import ovt.gui.*;
import ovt.util.*;
import ovt.event.*;
import ovt.datatype.*;

import java.io.*;
import java.beans.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Class whose instances represent the editor window for a particular "activity
 * index". The window has one table with one column for the date & time, and an
 * arbitrary number of columns with "index values".
 *
 * @author Yuri Khotyaintsev
 * @version 1.0
 */
public class MagActivityDataEditor extends javax.swing.JFrame {

    // Variables declaration 
    private javax.swing.JScrollPane jScrollPane1;
    private DataTable table;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton insertButton;
    private JButton deleteButton;
    private javax.swing.JButton applyButton;
    private javax.swing.JButton closeButton;
    // End of variables declaration
    private MagActivityDataModel dataModel;
    /**
     * magProps is used to fire MagPropsChangeEvent.
     */
    private MagProps magProps;
    private DataEditorMenuBar menuBar = new DataEditorMenuBar();


    /**
     * Constructor. Creates new form MagActivityDataEditor.
     */
    public MagActivityDataEditor(MagActivityDataModel adataModel, MagProps magProps) {
        this.dataModel = adataModel;
        this.magProps = magProps;

        updateTitle();

        setJMenuBar(menuBar);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        dataModel.addPropertyChangeListener("file", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                updateTitle();
            }
        });

        initComponents();
        pack();
    }


    private void updateTitle() {
        String titlePath = dataModel.getFile().getAbsolutePath();
        final int MAX_TITLE_PATH_LENGTH = 45;    // Longest permitted title path length.

        if (titlePath.length() > MAX_TITLE_PATH_LENGTH) {
            final int startIdx = titlePath.length() - (MAX_TITLE_PATH_LENGTH - 3);
            titlePath = "..." + titlePath.substring(startIdx, titlePath.length());
        }
        setTitle("" + dataModel.getName() + " : " + titlePath);
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the FormEditor.
     */
    private void initComponents() {
        //setIconImage(Toolkit.getDefaultToolkit().getImage(magProps.getCore().getImagesSubdir()+"ovt.gif"));
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new DataTable();
        table.setModel(dataModel);
        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }

                ListSelectionModel lsm
                        = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                    //no rows are selected
                    deleteButton.setEnabled(false);
                } else {
                    deleteButton.setEnabled(true);
                    //final int selectedRow = lsm.getMinSelectionIndex();
                    //System.out.println(selectedRow + " row is selected");
                }
            }
        });

        jPanel1 = new javax.swing.JPanel();
        insertButton = new javax.swing.JButton();
        applyButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        jScrollPane1.setViewportView(table);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.GridLayout(1, 4));

        insertButton.setText("Insert row");
        insertButton.setActionCommand("insertButton");
        insertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertRow();
            }
        }
        );

        jPanel1.add(insertButton);

        deleteButton = new JButton("Delete rows");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteRows();
            }
        });

        jPanel1.add(deleteButton);

        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed();
            }
        }
        );

        jPanel1.add(applyButton);

        closeButton.setText("Close");
        closeButton.setActionCommand("closeButton");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        }
        );

        jPanel1.add(closeButton);
        getRootPane().setDefaultButton(closeButton);

        // select the first line
        table.getSelectionModel().setSelectionInterval(0, 0);
        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);
    }


    private MagActivityDataModel getDataModel() {
        return dataModel;
    }


    private void applyButtonActionPerformed() {
        MagPropsEvent evt = new MagPropsEvent(this, dataModel.getIndex());
        magProps.fireMagPropsChange(evt);
        magProps.getCore().Render();
    }


    private void insertRow() {
        int rw = 0;
        int row = table.getSelectedRow();
        //Log.log("Selected row is " + row, 0);
        if (row >= 0) {
            int l = table.getSelectedRowCount();
            rw = row + l - 1;
        }
        //Log.log("Inserting row after " + rw, 0);
        dataModel.insertRows(rw);
        table.setRowSelectionInterval(rw + 1, rw + 1);
    }


    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
        setVisible(false);
    }


    private void deleteRows() {
        if (table.getSelectedRowCount() > 0) {
            int[] rows = table.getSelectedRows();
            for (int i = 0; i < rows.length; i++) {
                Log.log("Deleting row " + rows[i], 6);
                dataModel.deleteRows(rows[i] - i, rows[i] - i);
            }
            // add default values row if all rows where deleted.
            if (dataModel.getRowCount() == 0) {
                dataModel.reset();
            }
            // select last row if the very last row was removed
            Log.log("Selected row after deleteRows: " + table.getSelectedRow(), 6);
            int rowToSelect = rows[rows.length - 1] - rows.length;
            if (rowToSelect < 0) {
                rowToSelect = 0;
            }
            table.getSelectionModel().setSelectionInterval(rowToSelect, rowToSelect);

        }
        /*  JOptionPane.showMessageDialog(this,
         "No rows selected.",
         "Error",
         JOptionPane.ERROR_MESSAGE); */

    }


    /**
     * Exit the Application.
     */
    private void exitForm(java.awt.event.WindowEvent evt) {
        this.setVisible(false);
    }

    class DataTable extends JTable {

        DataTable() {
        }


        /**
         * After use changes time field, the row will change it's position due
         * to sorting by time. We will find it's new position and select this
         * line.
         */
        public void editingStopped(ChangeEvent e) {
            String newValue = "" + ((DefaultCellEditor) e.getSource()).getCellEditorValue();
            try {
                double mjd = new Time(newValue).getMjd();
                super.editingStopped(e);
                int newRow = getDataModel().getRow(mjd);
                getSelectionModel().setSelectionInterval(newRow, newRow);
            } catch (NumberFormatException e2) {
                // not time field was ditable..
                super.editingStopped(e);
            }
        }
    }

    public class DataEditorMenuBar extends JMenuBar {

        public DataEditorMenuBar() {
            super();
            JMenuItem menuItem;
            Font font = Style.getMenuFont();

            //--------------Build the File menu.-----------------
            JMenu menu = new JMenu("File");
            menu.setFont(font);

            menuItem = new JMenuItem("New...");
            menuItem.setFont(font);
            menuItem.setMnemonic(KeyEvent.VK_N);
            menuItem.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_N, ActionEvent.CTRL_MASK));
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    dataModel.reset();
                }
            });
            menu.add(menuItem);

            menuItem = new JMenuItem("Load...");
            menuItem.setFont(font);
            menuItem.setMnemonic(KeyEvent.VK_O);
            menuItem.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_O, ActionEvent.CTRL_MASK));
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    loadAction();
                }
            });
            menu.add(menuItem);

            menu.addSeparator();

            menuItem = new JMenuItem("Revert");
            menuItem.setFont(font);
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    try {
                        dataModel.load();
                    } catch (IOException e2) {
                        JOptionPane.showMessageDialog(null, "Error loading data from "
                                + dataModel.getFile(), e2.getMessage(), JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            menu.add(menuItem);

            menu.addSeparator();

            menuItem = new JMenuItem("Save");
            menuItem.setFont(font);
            menuItem.setMnemonic(KeyEvent.VK_S);
            menuItem.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_S, ActionEvent.CTRL_MASK));
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    try {
                        dataModel.save();
                    } catch (IOException e2) {
                        JOptionPane.showMessageDialog(null, "Error saving data to "
                                + dataModel.getFile(), e2.getMessage(), JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            menu.add(menuItem);

            menuItem = new JMenuItem("Save As...");
            menuItem.setFont(font);
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    saveAsAction();
                }
            });
            menu.add(menuItem);

            menu.addSeparator();

            menuItem = new JMenuItem("Close");
            menuItem.setFont(font);
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    setVisible(false);
                }
            });
            menu.add(menuItem);

            add(menu);
        }


        private void loadAction() {
            File oldFile = dataModel.getFile();
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Load data...");
            chooser.setSelectedFile(oldFile);

            OvtExtensionFileFilter filter = new OvtExtensionFileFilter("Data Files (*.dat)");
            filter.addExtension(".dat");
            chooser.setFileFilter(filter);
            chooser.addChoosableFileFilter(filter);

            int returnVal = chooser.showDialog(this, "Load");

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                dataModel.setFile(chooser.getSelectedFile());
                try {
                    dataModel.load();
                    table.getSelectionModel().setSelectionInterval(0, 0);
                } catch (IOException e2) {
                    JOptionPane.showMessageDialog(null, "Error loading data from "
                            + dataModel.getFile(), e2.getMessage(), JOptionPane.ERROR_MESSAGE);
                    dataModel.setFile(oldFile);
                }
            }
        }


        private void saveAsAction() {
            File oldFile = dataModel.getFile();
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Data As ...");
            chooser.setSelectedFile(oldFile);

            OvtExtensionFileFilter filter = new OvtExtensionFileFilter("Data Files (*.dat)");
            filter.addExtension(".dat");
            //chooser.setFileFilter(filter);
            chooser.addChoosableFileFilter(filter);

            chooser.setLocation(getLocation().x + getSize().width, getLocation().y);

            int returnVal = chooser.showDialog(this, "Save");

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String fname = chooser.getSelectedFile().getAbsolutePath();
                javax.swing.filechooser.FileFilter fileFilter = chooser.getFileFilter();
                // fileFilter can be (*.*). It is not OVTExtensionFilter
                // to avoid ClassCastException - if case ;-)
                if (fileFilter instanceof OvtExtensionFileFilter) {
                    String ext = ((OvtExtensionFileFilter) fileFilter).getExtension();
                    if (!fname.endsWith(ext)) {
                        fname += ext;
                    }
                }
                dataModel.setFile(new File(fname));
                try {
                    dataModel.save();
                } catch (IOException e2) {
                    JOptionPane.showMessageDialog(null, "Error saving data to "
                            + dataModel.getFile(), e2.getMessage(), JOptionPane.ERROR_MESSAGE);
                    dataModel.setFile(oldFile);
                }
            }
        }
    } // end of inner class DataEditorMenuBar

} // end of MagActivityDataEditor
