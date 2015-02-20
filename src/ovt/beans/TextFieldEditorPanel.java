/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/TextFieldEditorPanel.java,v $
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
 * TextFieldEditorPanel.java
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
 * @author  ko
 * @version
 */
public class TextFieldEditorPanel extends JTextField implements ActionListener,
FocusListener, PropertyChangeListener {
    
    ComponentPropertyEditor editor;
    String lastValue;
    KeyListener keyListener = null;
    
   /** Creates new TextFieldEditorPanel */
    TextFieldEditorPanel(ComponentPropertyEditor editor) {
        super(editor.getAsText());
        Dimension d = getPreferredSize();
        if (d.width < 50) { d.width = 50; setPreferredSize(d); }
        
        String toolTip = editor.getPropertyDescriptor().getToolTipText();
        if (toolTip != null) setToolTipText(toolTip);
        
        this.editor = editor;
        lastValue = editor.getAsText();
        addActionListener(this);
        addFocusListener(this);
    }
    
    public void editComplete() {
        //System.out.println("Edit complete");
        try {
            editor.setAsText(getText());
            editor.fireEditingFinished();
            lastValue = getText();
        } catch (PropertyVetoException e2) {
            // someone didn't like it
            JOptionPane.showMessageDialog(this, "" + e2.getMessage(), "Input Error", JOptionPane.WARNING_MESSAGE);
            System.out.println("Error editing TextFieldEditorPanel. lastValue="+lastValue);
            setText(lastValue);
            requestFocus();
        }
    }
    
    protected void refresh() {
        lastValue = editor.getAsText();
        removeFocusListener(this);
        removeActionListener(this);
        setText(lastValue);
        addActionListener(this);
        addFocusListener(this);
    }
    
    public void actionPerformed(ActionEvent e) {
        editComplete();
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        refresh();
    }
    
    public void focusGained(final java.awt.event.FocusEvent p1) {
    }
    
    public void focusLost(FocusEvent event) {
        //if (!event.isTemporary()) {
            editComplete();
        //} else System.out.println("The focus lost event temporary");
    }
    
/** Getter for property editCompleteOnKey.
 * @return Value of property editCompleteOnKey.
 */
    public boolean isEditCompleteOnKey() {
        return keyListener != null;
    }
    
/** Setter for property editCompleteOnKey.
 * @param editCompleteOnKey New value of property editCompleteOnKey.
 */
    public void setEditCompleteOnKey(boolean editCompleteOnKey) {
        if (editCompleteOnKey) {
            if (keyListener == null) addKeyListener(keyListener = new KeyPressedListener());
        }
        else {
            if (keyListener != null) {removeKeyListener(keyListener); keyListener = null;}
        }
    }
    
    class KeyPressedListener extends KeyAdapter {
        public void keyReleased(KeyEvent e) {
            int pos = getCaretPosition();   // preserve position
            try {
                editor.setAsText(getText());
                editor.fireEditingFinished();
                lastValue = getText();
            } catch (PropertyVetoException e2) {}
            try {
                setCaretPosition(pos);          // restore position
            } catch (IllegalArgumentException e3) {}
        }
    }
}
