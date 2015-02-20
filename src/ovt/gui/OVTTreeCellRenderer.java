/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/gui/OVTTreeCellRenderer.java,v $
  Date:      $Date: 2003/09/28 17:52:41 $
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
 * OVTTreeCellRenderer.java
 *
 * Created on March 31, 2000, 12:29 PM
 */

package ovt.gui;

import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.plaf.ColorUIResource;

/**
 *
 * @author
 * @version
 */

public class OVTTreeCellRenderer extends JPanel implements TreeCellRenderer {
    protected JCheckBox check;
    protected TreeLabel label;
    
    public OVTTreeCellRenderer() {
        setLayout(null);
        check = new JCheckBox();
        check.setBackground(UIManager.getColor("Tree.textBackground"));
        add(label = new TreeLabel());
        label.setForeground(UIManager.getColor("Tree.textForeground"));
    }
    
    public boolean isCheckBoxClicked(JTree tree, MouseEvent e)
    {
        if (getComponentCount() < 2) return false;
        int x = e.getX();
        int y = e.getY();
        int row = tree.getClosestRowForLocation(x,y);
        if (row == -1) return false;
        Rectangle row_bounds = tree.getRowBounds(row);
        x -= row_bounds.x;
        y -= row_bounds.y;
        Dimension d = check.getPreferredSize();
        return (x > 0 && y > 0 && x < d.width && y < d.height);
    }
    
    public Component getTreeCellRendererComponent(JTree tree, Object value,
    boolean isSelected, boolean expanded,
    boolean leaf, int row, boolean hasFocus) {
        String  stringValue = tree.convertValueToText(value, isSelected,
        expanded, leaf, row, hasFocus);
        setEnabled(tree.isEnabled());
        OVTNode node = (OVTNode) value;
        OVTTreeNode userObject = (OVTTreeNode) node.getUserObject();
        
        boolean isVisual = userObject instanceof VisualObject;
        boolean enabled = userObject.isEnabled();

        check.setEnabled(enabled);
        if (isVisual) add(check); else remove(check);
        
        if (isVisual) {
            check.setSelected(((VisualObject)userObject).isVisible());
        }
        
        label.setFont(new Font("SansSerif", enabled ? Font.PLAIN : Font.ITALIC, 10));
        label.setText(stringValue);
        label.setSelected(isSelected);
        label.setFocus(hasFocus);
        label.setIcon(userObject.getIcon());
        return this;
    }
    
    public Dimension getPreferredSize() {
        Dimension d_check = check.getPreferredSize();
        Dimension d_label = label.getPreferredSize();
        return new Dimension(d_check.width  + d_label.width,
            (d_check.height > d_label.height ? d_check.height : d_label.height));
    }
    
    public void doLayout() {
        Dimension d_check = check.getPreferredSize();
        if (getComponentCount() == 1) {
            d_check.width = 0;
            d_check.height = 0;
        }
        Dimension d_label = label.getPreferredSize();
        int y_check = 0;
        int y_label = 0;
        if (d_check.height < d_label.height) {
            y_check = (d_label.height - d_check.height)/2;
        } else {
            y_label = (d_check.height - d_label.height)/2;
        }
        check.setLocation(0,y_check);
        check.setBounds(0,y_check,d_check.width,d_check.height);
        label.setLocation(d_check.width,y_label);
        label.setBounds(d_check.width,y_label,d_label.width,d_label.height);
    }
    
    public void setBackground(Color color) {
        if (color instanceof ColorUIResource)
            color = null;
        super.setBackground(color);
    }
    
    public class TreeLabel extends JLabel {
        boolean isSelected;
        boolean hasFocus;
        
        public TreeLabel() {
        }
        
        public void setBackground(Color color) {
            if(color instanceof ColorUIResource)
                color = null;
            super.setBackground(color);
        }
        
        public void paint(Graphics g) {
            String str;
            if ((str = getText()) != null) {
                if (0 < str.length()) {
                    if (isSelected) {
                        g.setColor(UIManager.getColor("Tree.selectionBackground"));
                    } else {
                        g.setColor(UIManager.getColor("Tree.textBackground"));
                    }
                    Dimension d = this.getPreferredSize();
                    int imageOffset = 0;
                    Icon currentI = getIcon();
                    if (currentI != null) {
                        imageOffset = currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
                    }
                    g.fillRect(imageOffset, 0, d.width -1 - imageOffset, d.height);
                    if (hasFocus) {
                        g.setColor(UIManager.getColor("Tree.selectionBorderColor"));
                        g.drawRect(imageOffset, 0, d.width -1 - imageOffset, d.height -1);
                    }
                }
            }
            super.paint(g);
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }
        
        public void setFocus(boolean hasFocus) {
            this.hasFocus = hasFocus;
        }
    }
}







/*
 public class OVTTreeCellRenderer extends DefaultTreeCellRenderer {
//public class OVTTreeCellRenderer extends JPanel implements TreeCellRenderer {
 
    public Component getTreeCellRendererComponent(JTree tree, Object value,
    boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
    {
        Component comp = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        try {
 
            OVTNode node = (OVTNode)value;
            OVTTreeNode userObject = (OVTTreeNode)node.getUserObject();
            // set enabled
            boolean enabled = userObject.isEnabled();
            int fontType;
            if (enabled) fontType = Font.PLAIN; else fontType = Font.ITALIC;
            setFont(new Font("SansSerif", fontType, 10));
            // set icon
            Icon icon = userObject.getIcon();
            if (icon != null) setIcon(icon);
 
            if (userObject instanceof VisualObject) {
                JPanel panel = new JPanel();
                JCheckBox chBox = new JCheckBox();
                chBox.setBackground(Color.white);
                chBox.setEnabled(true);
                panel.add(chBox);
                panel.add(this);
 
                //this.setLayout(new FlowLayout());
                //this.add(chBox);
 
                //JLabel ll = new JLabel("Hello world");
                //ll.setEnabled(true);
 
                //chBox.set
                //JPanel panel = new JPanel();
                //panel.setLayout(new FlowLayout());
                //panel.add(chBox);
                //panel.add(this);
                //panel.setBackground(Color.white);
                //panel.setEnabled(true);
                //this.setEnabled(false);
                return panel;
            }
        } catch (ClassCastException e2) {
            System.out.println("class cast exception "+value.getClass().getName());
        }
        return this;
    }
}
 */
