/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/gui/TreePanel.java,v $
  Date:      $Date: 2005/12/13 16:33:06 $
  Version:   $Revision: 2.7 $
 
 
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

package ovt.gui;

import ovt.*;
import ovt.util.*;
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

public class TreePanel extends JScrollPane implements ActionListener {
    protected OVTCore core;
    protected JTree tree = null;
    protected OVTNode rootNode = null;
    protected DefaultTreeModel treeModel;
    protected OVTTreeCellRenderer renderer;
    
        /** True if the tree consists only from one Root node */
    protected boolean isEmpty = true;

    public TreePanel(OVTCore core) {
        super();
        this.core = core;
        
        //Create the nodes.
        //System.out.println("Creating nodes");
        rootNode = new OVTNode(core, this);
        //System.out.println("finished creating nodes");
        
        treeModel = new DefaultTreeModel((DefaultMutableTreeNode)rootNode);
        tree = new JTree(treeModel);
        tree.setCellRenderer(renderer = new OVTTreeCellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        //tree.putClientProperty("JTree.lineStyle", lineStyle);
        tree.putClientProperty("JTree.lineStyle", "Angled");
        
        tree.addMouseListener(new TreePanelMouseListener());
        
        //setColumnHeaderView(tree);
        setViewportView(tree);
        
        // Expand Satellites tree node
        expandSatellitesNode();
    }
    
    public OVTCore getCore()
    { return core; }
    
  /** notifies the tree model listeners that <CODE>node</CODE> has changed */
    public void nodeChanged(OVTNode node) {
        treeModel.nodeChanged(node);
    }
    
    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String s = "Action event detected";
        System.out.println(s);
    }

    /** Expands Satellites/Cluster tree node */
    public void expandSatellitesNode() {
        for (int i=0; i<treeModel.getChildCount(rootNode); i++) {
            OVTNode treeNode = (OVTNode) treeModel.getChild(rootNode, i);
            OVTObject obj = (OVTObject) treeNode.getUserObject();
            //if (obj.getName().equals("Satellites")) {
            if (obj instanceof Sats) {
                tree.expandRow(i+1);
                break;
            }
        }
    }
    
    /** Expands Satellites/Cluster tree node */
    public void expandClusterNode() {
        for (int i=0; i<treeModel.getChildCount(rootNode); i++) {
            OVTNode treeNode = (OVTNode) treeModel.getChild(rootNode, i);            
            if (treeNode.getUserObject() instanceof Sats) {                
                for (int j=0; j<treeModel.getChildCount(treeNode); j++) {
                  OVTNode treeNode2 = (OVTNode) treeModel.getChild(treeNode, j);
                  if (treeNode2.getUserObject() instanceof ClusterSats) {
                  	tree.expandPath(new TreePath(new Object[]{rootNode, treeNode, treeNode2}));
                	break;
                  }
              }
            }
        }
    }
    
    /*
    class TestMouseAdapter extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            System.out.print("mouseClicked:\t");
            flags(e);
        }
        public void mousePressed(MouseEvent e) {
            System.out.print("mousePressed:\t");
            flags(e);
        }
        public void mouseReleased(MouseEvent e) {
            System.out.print("mouseReleased:\t");
            flags(e);
        }

        private void flags(MouseEvent e) {
            int mod = e.getModifiers();
            if ((mod & e.ALT_GRAPH_MASK) != 0) System.out.print("ALT_GRAPH | ");
            if ((mod & e.ALT_MASK) != 0) System.out.print("ALT | ");
            if ((mod & e.BUTTON1_MASK) != 0) System.out.print("BUTTON1 | ");
            if ((mod & e.BUTTON2_MASK) != 0) System.out.print("BUTTON2 | ");
            if ((mod & e.BUTTON3_MASK) != 0) System.out.print("BUTTON3 | ");
            if ((mod & e.CTRL_MASK) != 0) System.out.print("CTRL | ");
            if ((mod & e.META_MASK) != 0) System.out.print("META | ");
            if ((mod & e.SHIFT_MASK) != 0) System.out.print("SHIFT | ");
            System.out.println("0");
        }
    }*/

    class TreePanelMouseListener extends MouseAdapter {
        
        public void mouseClicked(MouseEvent e) {
            //System.out.println("Mouse CLICKed");
            boolean checkBoxClicked = renderer.isCheckBoxClicked(tree, e);
            if (isLeftButton(e) && e.getClickCount() == 1 && checkBoxClicked) {
                OVTNode node = (OVTNode) tree.getLastSelectedPathComponent();
                if (node != null) {
                    try {
                        VisualObject obj = (VisualObject) node.getUserObject();
                        if (obj.isEnabled()) {
                            obj.setVisible(!obj.isVisible());
                            // render if visible state is changed
                            if (checkBoxClicked) getCore().Render();
                        }
                        tree.repaint();
                        
                    }
                    catch(ClassCastException e2) {
                        System.err.println("Error: non-visual object! " + e2);
                    }
                }
            }
        }
        
        public void mousePressed(MouseEvent e) {
            //System.out.println("Mouse PRESSed");
            int selRow = tree.getRowForLocation(e.getX(), e.getY());
            tree.setSelectionRow(selRow);
        }
        
        public void mouseReleased(MouseEvent e) {
            //System.out.println("Mouse released");
            if (isRightButton(e)) {
                showPopup(e);
            } 
        }

        public boolean isLeftButton(MouseEvent e) {
            return (e.getModifiers() & e.BUTTON1_MASK) != 0;
        }

        public boolean isRightButton(MouseEvent e) {
            return (e.getModifiers() & e.BUTTON3_MASK) != 0;
        }

        
    }
    
    public void showPopup(MouseEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node != null) {
            Object obj = node.getUserObject();
            JPopupMenu menu = null;
            try {
                menu = addMenuItemsFromDescriptors(menu, (DescriptorsSource) obj);
            } catch (ClassCastException e2) {
                System.out.println("This object has no popup menu.");
            }
            try {
                menu = addMenuItemsFromSource(menu, (MenuItemsSource) obj, false);
            } catch (ClassCastException ignore) {}
            if (menu != null ) 
                menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private JPopupMenu addMenuItemsFromDescriptors(JPopupMenu menu, DescriptorsSource source) {
        Descriptors descriptors = source.getDescriptors();
        if (descriptors == null) return menu;
        
        //System.out.println("Descriptors: " + descriptors);
        
        Enumeration e = descriptors.elements();

        while (e.hasMoreElements()) {
            BasicPropertyDescriptor pd = (BasicPropertyDescriptor) e.nextElement();
            //System.out.println("Property=" + pd.getName());
            
            if (pd.isMenuAccessible()) {
                
                try {
                    menu = addMenuItemsFromSource(menu, (MenuItemsSource) pd.getPropertyEditor(), false);
                    // One should render after user changes any paramiter by means of editor
                    // core will render, when recieves event from property editor.
                    try {
                        GUIPropertyEditor guiEd = (GUIPropertyEditor) pd.getPropertyEditor();
                        if (!guiEd.hasListener((GUIPropertyEditorListener)getCore()))
                            guiEd.addGUIPropertyEditorListener((GUIPropertyEditorListener)getCore());
                    } catch (ClassCastException ignore) {}
                    
                } catch (ClassCastException e2) {
                    System.out.println("Property " + pd.getName() + " editor has no menu items." + e2);
                }
            }
        }
        return menu;
    }
    
    private JPopupMenu addMenuItemsFromSource(JPopupMenu menu, MenuItemsSource source, boolean multiple_sep) {
        JMenuItem [] mItem = source.getMenuItems();
        if (menu == null) menu = new JPopupMenu();
        else if (!multiple_sep) addSeparator(menu);
        for (int i=0; i<mItem.length; i++) {
            if (multiple_sep) addSeparator(menu);
            // if item == null -> add separator
            if (mItem[i] != null)
                menu.add(mItem[i]);
            else menu.add(new JSeparator());
        }
        return menu;
    }

    private void addSeparator(JPopupMenu menu) {
        if (menu.getComponentCount() > 0) menu.add(new JSeparator());
    }
    
    public DefaultTreeModel getTreeModel() { // added by Oleg
        return treeModel;
    }
}

/*
  class NodeSelectionListener extends MouseAdapter {
    JTree tree;
    
    NodeSelectionListener(JTree tree) {
      this.tree = tree;
    }
    
    public void mouseClicked(MouseEvent e) {
      int x = e.getX();
      int y = e.getY();
      int row = tree.getRowForLocation(x, y);
      TreePath  path = tree.getPathForRow(row);
      //TreePath  path = tree.getSelectionPath();
      if (path != null) {
        OVTNode node = (OVTNode)path.getLastPathComponent();
        boolean isSelected = ! (node.isSelected());
        node.setSelected(isSelected);
        ((DefaultTreeModel)tree.getModel()).nodeChanged(node);
        // I need revalidate if node is root.  but why?
        if (row == 0) {
          tree.revalidate();
          tree.repaint();
        }
      }
    }
  }
*/
