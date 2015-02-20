/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/beans/FileEditor.java,v $
Date:      $Date: 2003/09/28 17:52:33 $
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
 * FileEditor.java
 *
 * Created on November 22, 2000, 3:21 PM
 */

package ovt.beans;


import ovt.util.*;
import ovt.event.*;
import ovt.interfaces.*;

import java.io.*;
import java.beans.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 *
 * @author  ko
 * @version 
 */
public class FileEditor extends GUIPropertyEditor implements MenuItemsSource {
  MenuPropertyEditor visibilityEditor;
  String title = "Choose File";
  String approveButtonText = "OK";
  Vector filters = new Vector();
    
  /** Creates new FileEditor */
  public FileEditor(BasicPropertyDescriptor pd) {
    super(pd);
    try {
      BasicPropertyDescriptor prop_descr = new BasicPropertyDescriptor("openDialogVisible", this);
      prop_descr.setLabel(getPropertyLabel());
      visibilityEditor = new MenuPropertyEditor(prop_descr, MenuPropertyEditor.SWITCH);
      String item = pd.getDisplayName();
      visibilityEditor.setTags(new String[]{item, "hide " + item});
      visibilityEditor.setValues(new Object[]{new Boolean(true), new Boolean(false)});
      //prop_descr.setPropertyEditor(visibilityEditor);
      addPropertyChangeListener(visibilityEditor);
      
    } catch (IntrospectionException e2) {System.out.println(""+e2);}
  }
  
  public void addExtensionFilter(OvtExtensionFileFilter filter) {
    filters.addElement(filter);
  }
  
  public void setApproveButtonText(String approveButtonText) {
    this.approveButtonText = approveButtonText;
  }
  
  public void setTitle(String title) {
    this.title = title;
  }
  
  public void setAsText(String text) throws PropertyVetoException {
    setValue(new File(text));
  }
  
  public String getAsText() {
    File file = getFile();
    if (file != null) return file.getPath(); // or absolutPath() ?
    else return null;
  }
  
  /** Getter for property file.
 * @return Value of property file.
 */
private File getFile() {
  return (File)getValue();
}
  
  /** Setter for property file.
 * @param file New value of property file.
 */
private void setFile(File file) throws PropertyVetoException {
  File oldFile = getFile();
  setValue(file);
}
  
public void setOpenDialogVisible(boolean value) {
  if (value == true) {
    
    JFileChooser chooser = new JFileChooser(); 
    
    File file = getFile();
    if (file != null) chooser.setCurrentDirectory(file);
    
    chooser.setDialogTitle(title);

    Enumeration e = filters.elements();
    OvtExtensionFileFilter filter;
    while (e.hasMoreElements()) {
        filter = (OvtExtensionFileFilter)e.nextElement();
         chooser.setFileFilter(filter);
         chooser.addChoosableFileFilter(filter);
    }
    
    JFrame frameOwner = getFrameOwner();
    if (frameOwner != null)
        chooser.setLocation(frameOwner.getLocation().x+frameOwner.getSize().width, frameOwner.getLocation().y);
        

    int returnVal = chooser.showDialog(frameOwner, approveButtonText);
	
    if(returnVal == JFileChooser.APPROVE_OPTION) {
        try {
            setFile(chooser.getSelectedFile());// .getAbsoluteFile()
        } catch (PropertyVetoException ignore) { ignore.printStackTrace(); }
    }
    }
  // ? propertySupport.firePropertyChange("openDialogVisible", new Boolean(oldvalue), new Boolean(windowVisible));
}
  
  public boolean isOpenDialogVisible() {
    return false;
  }
  
public JMenuItem[] getMenuItems() {
  return visibilityEditor.getMenuItems();
}

}
