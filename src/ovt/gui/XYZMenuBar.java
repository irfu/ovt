/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/gui/XYZMenuBar.java,v $
  Date:      $Date: 2005/12/13 16:32:52 $
  Version:   $Revision: 2.14 $


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
import ovt.mag.*;
import ovt.util.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.io.*;
import java.util.*;


public class XYZMenuBar extends JMenuBar {
    private Font font = Style.getMenuFont();
    public OVTCore core;
    public XYZWindow xyzWin;
    private JMenuItem importSatelliteMenuItem;
    private ovt.object.editor.SettingsEditor renPanelSizeEditor;
    
public XYZMenuBar(OVTCore acore, XYZWindow xyzwin) {
  super();
  this.core = acore;
  xyzWin = xyzwin;
  JMenuItem menuItem;
  JRadioButtonMenuItem rbMenuItem;
  JCheckBoxMenuItem cbMenuItem;
  String ItemName;

  


//--------------Build the File menu.-----------------

	JMenu menu = new JMenu("File");
	menu.setFont(font);
        
	menuItem = new JMenuItem("Export Image...");
        menuItem.setFont(font);
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            ImageOperations.exportImageDialog(getCore());
          }
        });
	
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Load Settings...");
          menuItem.setFont(font);
          menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String defaultFile = OVTCore.getGlobalSetting(OVTCore.DEFAULT_SETTINGS_FILE, core.getConfDir());
                String file = Settings.showOpenDialog(xyzWin, new File(defaultFile));
                if (file != null) {
                    try {
                        // hide all objects
                        //Settings.load(core.getConfDir() + "hideall.xml", core);
                        getCore().hideAllVisibleObjects();
                        // load new settings
                        Settings.load(file, core);
                        
                        xyzWin.getTreePanel().expandSatellitesNode();
                        OVTCore.setGlobalSetting(OVTCore.DEFAULT_SETTINGS_FILE, file);
                        core.Render();
                    } catch (IOException e2) {
                        core.sendErrorMessage("Error Loading Settings", e2);
                    }
                }
            }
          });

        menu.add(menuItem);
          
        menuItem = new JMenuItem("Save Settings...");
        menuItem.setFont(font);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String defaultFile = OVTCore.getGlobalSetting(OVTCore.DEFAULT_SETTINGS_FILE, core.getConfDir());
                String file = Settings.showSaveDialog(xyzWin, new File(defaultFile));
                if (file != null) {
                    try {
                        Settings.save(core, file);
                        OVTCore.setGlobalSetting(OVTCore.DEFAULT_SETTINGS_FILE, file);
                    } catch (IOException e2) {
                        core.sendErrorMessage("Error Saving Settings", e2);
                    }
                }
            }
        });
        
        menu.add(menuItem);
        
        //menu.addSeparator();
        //menu.add(menuItem);
        
        
        
          
        //if ( xyzWin.windowResizable ){
          menu.addSeparator();
          
          menuItem = new JMenuItem("Print...");
          menuItem.setFont(font);
          menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
              ImageOperations.print(getCore());
            }
          });
          menu.add(menuItem);
        //}

        menu.addSeparator();

        menuItem = new JMenuItem("Exit");
        menuItem.setFont(font);
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            getVW().quit();
          }
        });
        menu.add(menuItem);
        
        add(menu);
        
        
        
        // Satellites
        
        add(createSatsMenu());
        
        
        
        // Options
        menu = new JMenu("Options");
        menu.setFont(font);
        
        // View Control
        
        menuItem = new JMenuItem("View Control...");
        menuItem.setFont(font);
        menuItem.setMnemonic(KeyEvent.VK_V);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            getCore().getCamera().setCustomizerVisible(true);
          }
        });
        menu.add(menuItem);
        
        menu.addSeparator();
        
        // Magnetic Field 
        
        menuItem = new JMenuItem("Magnetic Field...");
        menuItem.setFont(font);
        menuItem.setMnemonic(KeyEvent.VK_V);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_M, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            getCore().getMagProps().setCustomizerVisible(true);
          }
        });
        menu.add(menuItem);
        
        
        menu.addSeparator();
        
        // Activity indexes
        
        MagProps magProps = getCore().getMagProps();
        for (int i=1; i<=MagProps.MAX_ACTIVITY_INDEX; i++) {
          menuItem = new ActivityDataMenuItem(magProps, i);
          menu.add(menuItem);
        }

	menu.addSeparator();
        
        // Space Colour
        
        menuItem = ((MenuItemsSource)getCore().getDescriptors().getDescriptor("backgroundColor").getPropertyEditor()).getMenuItems()[0];
        menu.add(menuItem);
        
        
        
        if ( !xyzWin.windowResizable ){
          renPanelSizeEditor = new ovt.object.editor.SettingsEditor(xyzWin,true);
          menu.addSeparator();

          menuItem = new JMenuItem("Visualization Panel Size...");
          menuItem.setFont(font);
          menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
              renPanelSizeEditor.setVisible(true);
            }
          });

          menu.add(menuItem);
        }
        
        
        add(menu);
        
        
        // --------------- Add -------------
        
        menu = new JMenu("Add FL Mapper");
        menu.setFont(font);
        
        // GB FL [GEO]
        
        menuItem = new JMenuItem("Bind to GEO");
        menuItem.setFont(font);
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
             FieldlineMapper flm = new FieldlineMapper(getCore());
             flm.setBindCS(CoordinateSystem.GEO);
             flm.setName("New FL Mapper [GEO]");
             getCore().getChildren().addChild(flm);
             getCore().getChildren().fireChildAdded(flm);
             flm.setCustomizerVisible(true);
          }
        });
        menu.add(menuItem);
        
        // GB FL [SM]
        
        menuItem = new JMenuItem("Bind to SMC");
        menuItem.setFont(font);
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
             FieldlineMapper flm = new FieldlineMapper(getCore());
             flm.setBindCS(CoordinateSystem.SM);
             flm.setName("New FL Mapper [SMC]");
             getCore().getChildren().addChild(flm);
             getCore().getChildren().fireChildAdded(flm);
             flm.setCustomizerVisible(true);
          }
        });
        menu.add(menuItem);
        
        add(menu);

        // Help
        
        menu = new JMenu("Help");
        menu.setFont(font);
        
        menuItem = new JMenuItem("About");
        menuItem.setFont(font);
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            HTMLBrowser hw = getVW().getHTMLBrowser();
            String url = "file:"+getCore().getDocsDir()+"about.html";
            //String url = "http://www.yahoo.com";
            try {
              hw.setPage(url);
            } catch (IOException e) {e.printStackTrace();}
            hw.setVisible(true);
          }
        });
        menu.add(menuItem);
        
        add(menu);

}

public JMenuItem createImportSatelliteMenuItem() {
    JMenuItem menuItem = new JMenuItem("Import Satellite ...");
        menuItem.setFont(Style.getMenuFont());
        menuItem.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent evt) {
                ImportSatelliteWizard wizard = new ImportSatelliteWizard(core.getSats(), xyzWin);
                Sat sat = wizard.start();
                if (sat != null) {
                    core.getSats().addSat(sat);
                    core.getSats().getChildren().fireChildAdded(sat);
                    core.Render();
                }
             }
        });
    return menuItem;
}

private JMenu createSatsMenu() {
    if (importSatelliteMenuItem == null) importSatelliteMenuItem = createImportSatelliteMenuItem();
    JMenu menu = new JMenu("Satellites");
    menu.setFont(font);
    menu.addMenuListener( new MenuListener() {
        public void menuCanceled(MenuEvent e) {}
                
        public void menuDeselected(MenuEvent e) {}
                
        public void menuSelected(MenuEvent e) {
            JMenu satsMenu = (JMenu)e.getSource();
            satsMenu.removeAll();
            // Import Satellite ...
            satsMenu.add(importSatelliteMenuItem);
            satsMenu.addSeparator();
            // List of satellites
            JMenuItem[] items = createSatsList();
            for (int i=0; i<items.length; i++) satsMenu.add(items[i]);
        }
    });
    return menu;
}        

/** Each  JMenuItem is a JCheckBoxMenuItem with a satellite's name, 
 * is checked if sat is added to  {@link ovt.object.Sats Sats}.
 */
public JMenuItem[] createSatsList() {
    File[] files = new File(OVTCore.getOrbitDataDir()).listFiles( new FilenameFilter() {
         public boolean accept(File dir, String file) {
            return file.endsWith(".tle") || (file.endsWith(".ltof") && !file.startsWith("Cluster"));
         }
     });

     JMenuItem[] items = new JMenuItem[files.length];
     
     ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem)evt.getSource();
                String satname = item.getText();
                if (item.isSelected()) { // create Sat and add it to OVTCore.Sats
                    
                    try {
                        Sat sat;
                        // check if TLE file exists
                        File file = new File(OVTCore.getOrbitDataDir()+Utils.replaceSpaces(satname)+".tle");
                        if (file.exists())
                            sat = new TLESat(getCore());
                        else { // check if LTOF file exists
                            file =  new File(OVTCore.getOrbitDataDir()+Utils.replaceSpaces(satname)+".ltof");
                            if (file.exists())
                                sat = new LTOFSat(getCore());
                            else
                                throw new IOException("Orbit file "+OVTCore.getOrbitDataDir()+Utils.replaceSpaces(satname)+".tle/.ltof not found");
                        }
                        sat.setName(satname);
                        sat.setOrbitFile(file);
                        core.getSats().addSat(sat);
                        core.getSats().getChildren().fireChildAdded(sat);
                    } catch (IOException e2) {
                        core.sendErrorMessage(e2);
                    }
                } else {                 // remove Sat from OVTCore.Sats
                    Sat sat = (Sat)core.getSats().getChildren().getChild(satname);
                    core.getSats().removeSat(sat);
                    core.getSats().getChildren().fireChildRemoved(sat); // notify TreePanel, Camera maybe..
                }
                core.Render();
            }
        };

     for (int i=0; i<files.length; i++) { 
         String filename = files[i].getName();
         String satName = Utils.replaceUnderlines(filename.substring(0, filename.lastIndexOf('.')));
         JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(satName);
         menuItem.setFont(font);
         menuItem.setSelected(core.getSats().getChildren().containsChild(satName)); // select if sat is already added to OVT
         menuItem.addActionListener(actionListener);
         items[i] = menuItem;
     }
        
     return items;   
}


protected XYZWindow getVW()
	{ return xyzWin; }

protected OVTCore getCore()
	{ return xyzWin.getCore(); }
        
        
        
}

class ActivityDataMenuItem extends JMenuItem implements ActionListener {

    private int activityIndex;
    private MagProps magProps;
    ActivityDataMenuItem(MagProps mp, int activityIndex) {
        super();
        setText(mp.getActivityName(activityIndex) + "...");
        setFont(Style.getMenuFont());
        this.activityIndex = activityIndex;
        this.magProps = mp;
        addActionListener(this);
    }
    
    public void actionPerformed(ActionEvent evt) {
            magProps.activityEditors[activityIndex].setVisible(true);
    }
    
}
