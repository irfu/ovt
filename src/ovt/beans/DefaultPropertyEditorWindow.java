/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/DefaultPropertyEditorWindow.java,v $
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
 * DefaultComponentPropertyEditorWindow.java
 *
 * Created on March 7, 2000, 3:53 PM
 */
 
package ovt.beans;


import java.beans.*;
import java.awt.*;
import javax.swing.*;


/** 
 *
 * @author  root
 * @version 
 */
public class DefaultPropertyEditorWindow extends JDialog implements PropertyChangeListener {

  WindowPropertyEditor editor = null;
  
  /** Creates new DefaultComponentPropertyEditorWindow */
  public DefaultPropertyEditorWindow(JFrame owner, WindowPropertyEditor editor, boolean modal) {
    super(owner, modal);
    this.editor = editor;
    setTitle(editor.getPropertyDescriptor().getDisplayName());
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    getContentPane().add(editor.getComponent());
    pack();
    

    if (owner != null) {
        Point loc = owner.getLocationOnScreen();
        loc.x += 16;
        loc.y += 16;
        setLocation(loc);
    }
    else {
        // set location in center of screen
        Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(scrnSize.width /2 - windowSize.width /2,
                    scrnSize.height/2 - windowSize.height/2);
    }
  }
 
  
  public void propertyChange(PropertyChangeEvent pce) {
    if (pce.getPropertyName().equals("enabled")) setVisible(false);
  }

  protected void refresh() {}
  
  public void setVisible(boolean visible) {
    editor.setWindowVisible(visible);
    super.setVisible(visible);
  }
  
}
