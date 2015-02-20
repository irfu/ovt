/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/WindowedPropertyEditor.java,v $
  Date:      $Date: 2003/09/28 17:52:36 $
  Version:   $Revision: 2.6 $
 
 
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
 * WindowedPropertyEditor.java
 *
 * Created on March 27, 2001, 2:59 PM
 */

package ovt.beans;

import ovt.gui.*;
import ovt.util.*;
import ovt.interfaces.*;

import java.beans.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import java.lang.Cloneable;
import java.lang.reflect.*;

/**
 *
 * @author  ko
 * @version
 */
public class WindowedPropertyEditor implements OVTPropertyEditor, MenuItemsSource {
    
    protected ComponentPropertyEditor editor;
    private MenuPropertyEditor visibilityEditor;
    private PropertyEditorWindow window = null;
    protected OVTPropertyChangeSupport propertySupport = new OVTPropertyChangeSupport ( this );
    
    /** Holds value of property modal. */
    private boolean modal = false;
    
    /** Holds value of property buttonText. */
    private String buttonText = null;
    private JFrame owner = null;
   /** Holds value of property showLabel. By default <CODE>false</CODE>. */
    private boolean showLabel = false;

    
    public WindowedPropertyEditor(ComponentPropertyEditor editor, JFrame owner) {
        this.editor = editor;
        this.owner = owner;
        String label = editor.getPropertyLabel();
        initialize(MenuPropertyEditor.SWITCH, new String[]{label, label} , new boolean[]{true, true});
        // listen to "label" change
        editor.getPropertyDescriptor().addPropertyChangeListener("label", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String label = WindowedPropertyEditor.this.editor.getPropertyDescriptor().getLabel();
                visibilityEditor.setTags(new String[]{label, label});
            }
        });
    }
    
    public WindowedPropertyEditor(ComponentPropertyEditor editor, JFrame owner, String buttonText) {
        this.editor = editor;
        this.buttonText = buttonText;
        this.owner = owner;
        String label = editor.getPropertyLabel();
        initialize(MenuPropertyEditor.SWITCH, new String[]{label, label} , new boolean[]{true, true});
                // listen to "label" change
        editor.getPropertyDescriptor().addPropertyChangeListener("label", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String label = WindowedPropertyEditor.this.editor.getPropertyDescriptor().getLabel();
                visibilityEditor.setTags(new String[]{label, label});
            }
        });

    }
    
    public WindowedPropertyEditor(ComponentPropertyEditor editor, JFrame owner, String buttonText, boolean showLabel) {
        this.editor = editor;
        this.buttonText = buttonText;
        this.owner = owner;
        this.showLabel = showLabel;
        String label = editor.getPropertyLabel();
        initialize(MenuPropertyEditor.SWITCH, new String[]{label, label} , new boolean[]{true, true});
                // listen to "label" change
        editor.getPropertyDescriptor().addPropertyChangeListener("label", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String label = WindowedPropertyEditor.this.editor.getPropertyDescriptor().getLabel();
                visibilityEditor.setTags(new String[]{label, label});
            }
        });

    }
    
    public WindowedPropertyEditor(ComponentPropertyEditor editor, JFrame owner, String[] showHideTags) {
        this.editor = editor;
        this.owner = owner;
        initialize(MenuPropertyEditor.SWITCH, showHideTags, new boolean[]{true, false});
                // listen to "label" change
        editor.getPropertyDescriptor().addPropertyChangeListener("label", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String label = WindowedPropertyEditor.this.editor.getPropertyDescriptor().getLabel();
                visibilityEditor.setTags(new String[]{label, label});
            }
        });

    }
    
    public JFrame getOwner() {
        return owner;
    }
    
    protected void initialize(int type, String[] showHideTags, boolean[] values) {
        if (showHideTags[0].equals(showHideTags[1]) && !showHideTags[0].endsWith("...")) {
            showHideTags[0] += "...";
            showHideTags[1] = showHideTags[0];
        }
        try {
            BasicPropertyDescriptor prop_descr = new BasicPropertyDescriptor("visible", this);
            visibilityEditor = new MenuPropertyEditor(prop_descr, type);
            visibilityEditor.setTags(showHideTags);
            visibilityEditor.setValues(new Object[]{new Boolean(values[0]), new Boolean(values[1])});
            //prop_descr.setPropertyEditor(visibilityEditor);
            addPropertyChangeListener("visible", visibilityEditor);
            
        } catch (IntrospectionException e2) {System.out.println(""+e2);}
        //addGUIPropertyEditorListener(this);
    }
  
    public ComponentPropertyEditor getInnerPropertyEditor() {
        return editor;
    }
    
    
  /** By default makes
   *
   * @return GUI Editor
   */
    public Window getWindow() {
        if (window == null) {
            // Create JDialog by default.
            window = new PropertyEditorWindow(getOwner(), this, isModal());
            addPropertyChangeListener(window);
        }
        return window;
    }
    
    public void setVisible(boolean value) {
        /*if (value == true  &&  isVisible() == true) {
            window.toFront(); // popup window
            return;
        }*/
        getWindow().setVisible(value);
    }
    
    public boolean isVisible() {
        if (window == null) return false;
        else return window.isVisible();
    }
    
    public void dispose() {
        if (window != null) window.dispose();
    }
    
    public JMenuItem[] getMenuItems() {
        return visibilityEditor.getMenuItems();
    }
    
    public void addPropertyChangeListener (PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener (listener);
    }
    
    
    public void addPropertyChangeListener (String property, PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener (property, listener);
    }
    
    public void removePropertyChangeListener (PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener (listener);
    }
    
    
    public String getAsText() {
        return editor.getAsText();
    }
    
    public void setAsText(String text) throws PropertyVetoException {
        editor.setAsText(text);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        //System.out.println(getClass().getName()+".prpropertyChange event : "+ evt.getPropertyName());
        if (evt.getPropertyName().equals("enabled")) {
            //System.out.println("Recieved event enabled!!!!!!!!");
            boolean enabled = ((Boolean)evt.getNewValue()).booleanValue();
            editor.setEnabled(enabled);
        } else
            // Tell all editor, to update itself.
            propertySupport.firePropertyChange(evt);
    }
    
    
    public ComponentPropertyEditor getInnerEditor() {
        return editor;
    }
    
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertySupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    
  /** Getter for property modal.
   * @return Value of property modal.
   */
    public boolean isModal() {
        return modal;
    }
    
  /** Setter for property modal.
   * @param modal New value of property modal.
   */
    public void setModal(boolean modal) {
        this.modal = modal;
    }
    
  /** Getter for property buttonText.
   * @return Value of property buttonText.
   */
    public String getButtonText() {
        return buttonText;
    }
    
  /** Setter for property buttonText.
   * @param buttonText New value of property buttonText.
   */
    public void setButtonText(String buttonText) {
    }
    
    
    /** Getter for property showLabel.
     * @return Value of property showLabel.
 */
    public boolean showLabel() {
        return showLabel;
    }
    
    /** Setter for property showLabel.
     * @param showLabel New value of property showLabel.
 */
    public void showLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }
    
    public Object getValue() {
        return editor.getValue();
    }
    
}

class PropertyEditorWindow extends JDialog implements PropertyChangeListener {
    
    WindowedPropertyEditor editor = null;
    
    
  /** Creates new DefaultComponentPropertyEditorWindow */
    public PropertyEditorWindow(JFrame owner, WindowedPropertyEditor editor, boolean modal) {
        super(owner, modal);
        this.editor = editor;
        setTitle(editor.getInnerEditor().getPropertyDisplayName());
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout(5, 5));
        //getContentPane().setB(20, 20, 20, 20);
        
        Component comp = editor.getInnerEditor().getComponent();
        comp.setBounds(20, 20, 20, 20);
        getContentPane().add(comp, BorderLayout.CENTER);
        
        String sLabel = editor.getInnerEditor().getPropertyLabel();
        if (editor.showLabel()  && sLabel != null) {
            if (sLabel != "") {
                JLabel label = new JLabel(sLabel);
                getContentPane().add(label, BorderLayout.WEST);
            }
        }
        if (editor.getButtonText() != null) {
            JButton button = new JButton(editor.getButtonText());
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    setVisible(false);
                }
            });
            getContentPane().add(button, BorderLayout.SOUTH);
        }
        pack();
        // set location in center of screen
        Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(scrnSize.width /2 - windowSize.width /2,
        scrnSize.height/2 - windowSize.height/2);
    }
    
    
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName().equals("enabled")) setVisible(false);
    }
    
    protected void refresh() {}
    
    public void setVisible(boolean visible) {
        if (visible  &&  isVisible()) {
            // if (isMinimized - unminimize.
            toFront(); //popup the window
            return;
        }
        super.setVisible(visible);
        editor.firePropertyChange("visible", null, null);
    }
    
    
}
