/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/TextAreaEditor.java,v $
  Date:      $Date: 2009/10/27 12:14:36 $
  Version:   $Revision: 2.4 $
 
 
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

package ovt.beans;

import java.beans.*;
import java.awt.*;
import javax.swing.*;

/**
 *
 * @author  Oleg
 * @version
 */
public class TextAreaEditor extends ComponentPropertyEditor {
    
    private int rows, cols;
    
    /** Creates new TextAreaEditor */
    public TextAreaEditor(BasicPropertyDescriptor pd, int rows, int cols) {
        super(pd);
        this.rows = rows;
        this.cols = cols;
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new TextAreaEditorPanel(this, rows, cols);
            addPropertyChangeListener((PropertyChangeListener)component);
        }
        return component;
    }
}
