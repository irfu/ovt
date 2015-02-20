/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/gui/MenuUtils.java,v $
  Date:      $Date: 2003/09/28 17:52:41 $
  Version:   $Revision: 1.4 $


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
 * MenuUtils.java
 *
 * Created on June 22, 2001, 2:30 AM
 */

package ovt.gui;

import ovt.*;
import ovt.beans.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 *
 * @author  root
 * @version 
 */
public class MenuUtils extends Object {

    /** Creates new MenuUtils */
    public MenuUtils() {
    }
    
    public static void addMenuItemsFromDescriptors(JMenu menu, DescriptorsSource source, OVTCore core) {
        Descriptors descriptors = source.getDescriptors();
        if (descriptors == null) return;
        
        //System.out.println("Descriptors: " + descriptors);
        
        Enumeration e = descriptors.elements();

        while (e.hasMoreElements()) {
            BasicPropertyDescriptor pd = (BasicPropertyDescriptor) e.nextElement();
            //System.out.println("Property=" + pd.getName());
            
            if (pd.isMenuAccessible()) {
                
                try {
                    addMenuItemsFromSource(menu, (MenuItemsSource) pd.getPropertyEditor(), false);
                    // One should render after user changes any paramiter by means of editor
                    // core will render, when recieves event from property editor.
                    try {
                        GUIPropertyEditor guiEd = (GUIPropertyEditor) pd.getPropertyEditor();
                        if (!guiEd.hasListener((GUIPropertyEditorListener)core))
                            guiEd.addGUIPropertyEditorListener((GUIPropertyEditorListener)core);
                    } catch (ClassCastException ignore) {}
                    
                } catch (ClassCastException e2) {
                    System.out.println("Property " + pd.getName() + " editor has no menu items." + e2);
                }
            }
        }
    }
    
    public static void addMenuItemsFromSource(JMenu menu, MenuItemsSource source, boolean multiple_sep) {
        JMenuItem [] mItem = source.getMenuItems();
        if (menu == null) menu = new JMenu();
        else if (!multiple_sep) addSeparator(menu);
        for (int i=0; i<mItem.length; i++) {
            if (multiple_sep) addSeparator(menu);
            menu.add(mItem[i]);
            //ovt.util.Log.log("item:"+mItem[i].getText());
        }
    }

    public static void addMenuItemsFromSource(JMenu menu, MenuItemsSource source) {
        //ovt.util.Log.log("addMenuItemsFromSource 1 + "+source);
        JMenuItem [] mItem = source.getMenuItems();
        //ovt.util.Log.log("addMenuItemsFromSource 2 length="+mItem.length);
        for (int i=0; i<mItem.length; i++) {
            menu.add(mItem[i]);
            //ovt.util.Log.log("item:"+mItem[i].getText());
        }
    }
    
    
    public static void addSeparator(JMenu menu) {
        if (menu.getComponentCount() > 0) menu.add(new JSeparator());
    }

}
