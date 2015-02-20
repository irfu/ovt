/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/TextAreaEditorPanel.java,v $
  Date:      $Date: 2003/09/28 17:52:35 $
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
 * TextAreaEditorPanel.java
 *
 * Created on October 28, 2000, 3:13 PM
 */

package ovt.beans;

import ovt.interfaces.*;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
/**
 *
 * @author  Oleg
 * @version
 */
public class TextAreaEditorPanel extends JTextArea implements FocusListener, PropertyChangeListener, KeyListener {
    
    ComponentPropertyEditor editor;
    String lastValue;
    
   /** Creates new TextAreaEditorPanel */
    TextAreaEditorPanel(ComponentPropertyEditor editor, int rows, int cols) {
        super(editor.getAsText(), rows, cols);
        this.editor = editor;

        String toolTip = editor.getPropertyDescriptor().getToolTipText();
        if (toolTip != null) setToolTipText(toolTip);
        
        lastValue = editor.getAsText();
        addFocusListener(this);
        addKeyListener(this);
    }
    
    public void editComplete() {
        //System.out.println("Edit complete");
        try {
            lastValue = getText();
            editor.setAsText(lastValue);
            editor.fireEditingFinished();
        } catch (PropertyVetoException e2) {
            // someone didn't like it
            JOptionPane.showMessageDialog(this, "" + e2.getMessage(), "Input Error", JOptionPane.WARNING_MESSAGE);
            System.out.println("Error editing TextAreaEditorPanel.lastValue="+lastValue);
            setText(lastValue);
            requestFocus();
        }
    }
    
    protected void refresh() {
        lastValue = editor.getAsText();
        //removeFocusListener(this);
        //removeKeyListener(this);
        setText(lastValue);
        //addFocusListener(this);
        //addKeyListener(this);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        refresh();
    }
    
    public void focusGained(final java.awt.event.FocusEvent p1) {
    }
    
    public void focusLost(FocusEvent event) {
        if (!event.isTemporary()) {
            editComplete();
        } else System.out.println("The focus lost event temporary");
    }
    
    private String textWhenPressed = "";
    
    public void keyReleased(KeyEvent ke) {
        if (ke.isActionKey()) return;
        String text = getText();
        if (text.equals(textWhenPressed)) return;
        int pos = getCaretPosition();   // preserve position
        try {
            editor.setAsText(text);
            editor.fireEditingFinished();
            lastValue = getText();
        } catch (PropertyVetoException e2) {}
        try {
            setCaretPosition(pos);          // restore position
        } catch (IllegalArgumentException e3) {}
    }
    
    public void keyPressed(KeyEvent p1) {
        textWhenPressed = getText();
    }
    
    public void keyTyped(KeyEvent p1) {}
    
}
