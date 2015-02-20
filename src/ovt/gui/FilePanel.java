/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/gui/FilePanel.java,v $
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
 * FilePanel.java
 *
 * Created on June 29, 2002, 7:27 PM
 */

package ovt.gui;

import ovt.util.OvtExtensionFileFilter;

import java.beans.*;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

/**
 *
 * @author  ko
 * @version 
 */
public class FilePanel extends JPanel {

private JTextField fileTF;
private Vector fileFilters = new Vector();
private String title = "";
private boolean allowOnlyExistingFile;

/** Holds value of property acceptAllFileFilterUsed. */
private boolean acceptAllFileFilterUsed;

/** Creates file choosing panel with a textfield and a button "Browse..". After the click on "Browse..." the FileChooser dialog with the title <code>title</code> pops up.
 * allowNewFile indicates weather only existing file can be specified in the file chooser dialog.
 */
public FilePanel(String title, boolean allowOnlyExistingFile) {
    this.title = title;
    this.allowOnlyExistingFile = allowOnlyExistingFile;
    setLayout(new BoxLayout (this, BoxLayout.X_AXIS));
    
    fileTF = new JTextField("", 20);
   // fileTF.setInputVerifier(new FilenameVerifier());
    add(fileTF);

    JButton button = new JButton("Browse...");
    button.setMaximumSize(button.getMinimumSize());
    button.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt) {
            chooseFile();
        }
    });
    add(button);

}

protected JTextField getTextField() {
    return fileTF;
}

public File getFile() {
    return new File(fileTF.getText());
}

public void setFile(File file) {
    fileTF.setText(file.getAbsolutePath());
    firePropertyChange("file", null, null);
}

private void chooseFile() {

    //String startDir = getFile();
    JFileChooser chooser = new JFileChooser(getFile());
    
    chooser.setDialogTitle(title);
    chooser.setAcceptAllFileFilterUsed(acceptAllFileFilterUsed);
    
    Enumeration e = fileFilters.elements();
    OvtExtensionFileFilter filter;
    while (e.hasMoreElements()) {
        filter = (OvtExtensionFileFilter)e.nextElement();
         chooser.setFileFilter(filter);
         chooser.addChoosableFileFilter(filter);
    }
    
    

    
    int returnVal = chooser.showDialog(this, "OK");
    if (returnVal == JFileChooser.APPROVE_OPTION) {
        File tmpFile = chooser.getSelectedFile();
        if ( tmpFile.exists() ) {
            if ( tmpFile.isDirectory() ) {
                JOptionPane.showMessageDialog(this,
                "File is a directory.",
                "File error",
                JOptionPane.ERROR_MESSAGE);
                return;
            }
            if ( !tmpFile.canRead() ) {
                JOptionPane.showMessageDialog(this,
                "File is not readable.",
                "File error",
                JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        setFile(tmpFile);
        fileTF.setMaximumSize(fileTF.getSize());
        //System.out.println("Set size: " + fileTF.getSize());
        fileTF.setPreferredSize(fileTF.getSize());
        // fileTF.setText(file.toString()); - in setFIle
    }
}



public void addExtensionFilter(OvtExtensionFileFilter filter) {
    fileFilters.addElement(filter);
}

/** Getter for property acceptAllFileFilterUsed.
 * @return Value of property acceptAllFileFilterUsed.
 *
 */
public boolean isAcceptAllFileFilterUsed() {
    return this.acceptAllFileFilterUsed;
}

/** Setter for property acceptAllFileFilterUsed.
 * @param acceptAllFileFilterUsed New value of property acceptAllFileFilterUsed.
 *
 */
public void setAcceptAllFileFilterUsed(boolean acceptAllFileFilterUsed) {
    this.acceptAllFileFilterUsed = acceptAllFileFilterUsed;
}

/* can be used for  fileTF
 
class FilenameVerifier extends InputVerifier {
    public boolean verify(JComponent input) {
               JTextField tf = (JTextField) input;
               return allowOnlyExistingFile ? new File (tf.getText()).exists() : true;
     }
} */

}
