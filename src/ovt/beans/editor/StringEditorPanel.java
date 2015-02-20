/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/beans/editor/StringEditorPanel.java,v $
Date:      $Date: 2003/09/28 17:52:36 $
Version:   $Revision: 1.2 $


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
 * StringEditorPanel.java
 *
 * Created on June 25, 2002, 6:31 PM
 */

package ovt.beans.editor;

import java.util.*;
import java.lang.*;
import java.beans.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*; 
import javax.swing.plaf.*;

import java.awt.Toolkit;


/**
 *
 * @author  ko
 * @version 
 */
public class StringEditorPanel extends JTextField implements PropertyChangeListener, Syncer {
    private PropertyEditor editor;
    private DocumentListener documentListener;
    
/** Creates new IntervalEditorPanel */
public StringEditorPanel(PropertyEditor ed) {
    this.editor = ed;
    editor.addPropertyChangeListener( this );
    
    //setDocument( new IntervalDocument() );
    
    documentListener = new DocumentListener() {
        public void changedUpdate(DocumentEvent evt) {
        }
        public void insertUpdate(DocumentEvent evt) {
            applyChangesSilently();
        }
        public void removeUpdate(DocumentEvent evt) {
            applyChangesSilently();
        }
    };
    
    getDocument().addDocumentListener( documentListener );
}

private void applyChangesSilently() {
    editor.removePropertyChangeListener(this);
    try {
        editor.setAsText(getText());
    } catch (IllegalArgumentException ignore) {
    }
    editor.addPropertyChangeListener(this);
}

/** listen to editor's propertyChange event */
public void propertyChange(PropertyChangeEvent evt) {
    // oldPropertyValue is changed only from outside
    getDocument().removeDocumentListener( documentListener );
    setText(editor.getAsText());
    getDocument().addDocumentListener( documentListener );
}


public void sync() throws SyncException {
    editor.removePropertyChangeListener(this);
    try {
        editor.setAsText(getText());
    } catch (IllegalArgumentException e2) {
        editor.addPropertyChangeListener(this);
        throw new SyncException(this, e2);
    }
    editor.addPropertyChangeListener(this);
}

}
