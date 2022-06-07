/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/beans/editor/IntervalEditorPanel.java,v $
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

OVT Team (https://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
Khotyaintsev

=========================================================================*/

/*
 * IntervalEditorPanel.java
 *
 * Created on June 21, 2002, 4:18 PM
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

import java.awt.Toolkit;
import ovt.util.Log;


/**
 *
 * @author  ko
 * @version
 */
public class IntervalEditorPanel extends JTextField
        implements Syncer, PropertyChangeListener {

    private static final int DEBUG = 2;

    private IntervalEditor editor;
    private DocumentListener documentListener;

/** Creates new IntervalEditorPanel */
public IntervalEditorPanel(IntervalEditor ed) {
    this.editor = ed;
    editor.addPropertyChangeListener( this );

    setDocument( new IntervalDocument() );

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

/** _Try_ to interpret the string in the textfield as an interval. Fail silently. */
private void applyChangesSilently() {
    editor.removePropertyChangeListener(IntervalEditorPanel.this);
    try {
        final String s = getText();
        //Log.log(getClass().getSimpleName()+"#applyChangesSilently", DEBUG);
        //Log.log("   s = \""+s+"\"", DEBUG);
        editor.setAsText(s);
    } catch (IllegalArgumentException ignore) {
    }
    editor.addPropertyChangeListener(IntervalEditorPanel.this);
}

/** Listen to editor's propertyChange event. */
public void propertyChange(PropertyChangeEvent evt) {
    // oldPropertyValue is changed only from outside
    getDocument().removeDocumentListener( documentListener );
    final String s = editor.getAsText();
    //Log.log(getClass().getSimpleName()+"#propertyChange", DEBUG);
    //Log.log("   s = \""+s+"\"", DEBUG);
    setText(s);  // NOTE: Must use text that can be parsed into an interval again.
    getDocument().addDocumentListener( documentListener );
}


public void sync() throws SyncException {
    editor.removePropertyChangeListener(IntervalEditorPanel.this);
    try {
        editor.setAsText(getText());
    } catch (IllegalArgumentException e2) {
        editor.addPropertyChangeListener(IntervalEditorPanel.this);
        throw new SyncException(this, e2);
    }
    editor.addPropertyChangeListener(IntervalEditorPanel.this);
}

}

//##############################################################################

class IntervalDocument extends DefaultStyledDocument {

    /** Called whenever the user types something into the text field
     * (and other text field updates too). All calls to setText seem to
     * indirectly lead to a call to this function.
     *
     * @param str The string that "someone" tried to INSERT.
     */
    public void insertString(int offs, String str, AttributeSet a)
        throws BadLocationException {

        // Insert string into text field, but only if the string contains permitted characters.
        if ( onlyContainsPermittedCharacters(str) )
            super.insertString(offs, str, a);   // Triggers call to DocumentListener#insertUpdate.
        else
            Toolkit.getDefaultToolkit().beep();
    }

    private static boolean onlyContainsPermittedCharacters(String intervalStr){
      if (intervalStr != null ) {
        for (int i=0;i<intervalStr.length();i++) {
            if (!java.lang.Character.isDigit(intervalStr.charAt(i)) &&
                !java.lang.Character.isSpaceChar(intervalStr.charAt(i)) &&
                (intervalStr.charAt(i) != 'd') &&
                (intervalStr.charAt(i) != 'h') &&
                (intervalStr.charAt(i) != 'm') &&
                (intervalStr.charAt(i) != 's')) {
            return false;
            }
        }
      }

      return true;
    }
}
