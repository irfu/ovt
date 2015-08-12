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
import ovt.interfaces.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.io.*;
import ovt.util.SSCWSLibrary.SSCWSSatelliteInfo;

public class XYZMenuBar extends JMenuBar {

    final private Font font = Style.getMenuFont();
    private OVTCore core;
    public XYZWindow xyzWin;
    private JMenuItem importSatelliteMenuItem;
    private ovt.object.editor.SettingsEditor renPanelSizeEditor;


    public XYZMenuBar(XYZWindow xyzwin) {
        super();
        this.core = xyzwin.getCore();
        xyzWin = xyzwin;
        JMenuItem menuItem;
        JRadioButtonMenuItem rbMenuItem;
        JCheckBoxMenuItem cbMenuItem;
        String ItemName;

        //-----------
        // File menu
        //-----------
        JMenu menu = new JMenu("File");
        menu.setFont(font);

        menuItem = new JMenuItem("Export Image...");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_E, ActionEvent.CTRL_MASK));  // Set accelerator Ctrl-E.

        menuItem.setFont(font);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                ImageOperations.exportImageDialog(getCore());
            }
        });

        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Load Settings...");
        menuItem.setFont(font);
        menuItem.addActionListener(new ActionListener() {
            @Override
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

        //------------
        // Satellites
        //------------
        add(createSatsMenu());

        //---------
        // Options
        //---------
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
        for (int i = 1; i <= MagProps.MAX_ACTIVITY_INDEX; i++) {
            menuItem = new ActivityDataMenuItem(magProps, i);
            menu.add(menuItem);
        }

        menu.addSeparator();

        // Space Colour
        menuItem = ((MenuItemsSource) getCore().getDescriptors().getDescriptor("backgroundColor").getPropertyEditor()).getMenuItems()[0];
        menu.add(menuItem);

        if (!xyzWin.windowResizable) {
            renPanelSizeEditor = new ovt.object.editor.SettingsEditor(xyzWin, true);
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

        // ---------------
        //  Add FL Mapper
        // ---------------
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

        //------
        // Help
        //------
        menu = new JMenu("Help");
        menu.setFont(font);

        menuItem = new JMenuItem("About");
        menuItem.setFont(font);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                HTMLBrowser hw = getVW().getHTMLBrowser();
                String url = "file:" + getCore().getDocsDir() + "about.html";
                //String url = "http://www.yahoo.com";
                try {
                    hw.setPage(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                hw.setVisible(true);
            }
        });
        menu.add(menuItem);

        add(menu);
    }


    public JMenuItem createImportSatelliteMenuItem() {
        JMenuItem menuItem = new JMenuItem("Import Satellite ...");
        menuItem.setFont(Style.getMenuFont());
        menuItem.addActionListener(new ActionListener() {
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


    /**
     * Create Satellites menu data structure. NOTE: The method only creates a JMenu and a
     * MenuListener immediately, but no JMenuItems. The actual JMenuItems are
     * created and added dynamically to the JMenu every time the "Satellites"
     * menu is clicked. Any previous JMenuItems are destroyed first.
     */
    private JMenu createSatsMenu() {
        if (importSatelliteMenuItem == null) {
            importSatelliteMenuItem = createImportSatelliteMenuItem();
        }
        final JMenu menu = new JMenu("Satellites");
        menu.setFont(font);
        menu.addMenuListener(new MenuListener() {
            public void menuCanceled(MenuEvent event) {
            }


            public void menuDeselected(MenuEvent event) {
            }


            public void menuSelected(MenuEvent event) {
                JMenu satsMenu = (JMenu) event.getSource();
                satsMenu.removeAll();                 // NOTE: Remove all previously existing MenuItems!

                // Import Satellite ...
                satsMenu.add(importSatelliteMenuItem);

                // List of satellites
                satsMenu.addSeparator();
                JMenuItem[] items = createSatsList();
                for (JMenuItem item : items) {
                    satsMenu.add(item);
                }

                // DEBUG
                satsMenu.addSeparator();
                try {
                    for (SSCWSSatelliteInfo satInfo : OVTCore.SSCWS_LIBRARY.getAllSatelliteInfo()) {
                        satsMenu.add(createSSCWSTestSatMenuItem_TEST(satInfo.ID));
                    }
                } catch (IOException exc) {
                    getCore().sendErrorMessage(exc);
                }
            }
        });
        return menu;
    }


    /**
     * TEST / DEBUG
     *
     * Create menu item for _ONE_ SSC WS satellite to put in the data structures
     * describing the GUI.
     *
     * NOTE: Maybe inefficient to have one ActionListener per satellite. Add all
     * satellites at once as with the preexisting OVT file-based satellites?
     */
    public JMenuItem createSSCWSTestSatMenuItem_TEST(String SSCWS_satID) {
        JMenuItem newMenuItem = null;
        try {
            final String satName = OVTCore.SSCWS_LIBRARY.getSatelliteInfo(SSCWS_satID).name;

            final ActionListener actionListener = (ActionEvent evt) -> {
                final JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) evt.getSource();

                if (menuItem.isSelected()) { // create Sat and add it to OVTCore.Sats
                    addSSCWSSatAction(SSCWS_satID);
                } else {
                    // remove Sat from OVTCore.Sats 
                    removeSSCWSSatAction(SSCWS_satID);
                }
                core.Render();
            };

            newMenuItem = new JCheckBoxMenuItem(satName);
            newMenuItem.setFont(Style.getMenuFont());
            newMenuItem.addActionListener(actionListener);
            newMenuItem.setSelected(core.getSats().getChildren().containsChild(satName));  // Select if sat is already added to OVT.
        } catch (IOException e) {
            getCore().sendErrorMessage(e);
        }
        return newMenuItem;
    }


    /**
     * Method that represents the action of adding a SSC Web Services satellite
     * to the "GUI tree", as if this action was triggered in the
     * GUI.
     *
     * @param SSCWS_satID SSCWS_satID The satellite ID string used by SSC Web
     * Services to reference satellites, SatelliteDescription#getId().
     */
    public void addSSCWSSatAction(String SSCWS_satID) {
        final Sat sat;
        try {
            sat = new SSCWSSat(getCore(), OVTCore.SSCWS_LIBRARY, SSCWS_satID);

            /* NOTE: The string value appears in the GUI tree node, but is also
             used to find the satellite when removing it from the tree(?). */
            sat.setName(OVTCore.SSCWS_LIBRARY.getSatelliteInfo(SSCWS_satID).name);
            sat.setOrbitFile(null);
        } catch (IOException e) {
            getCore().sendErrorMessage(e);
            return;
        }
        getCore().getSats().addSat(sat);
        getCore().getSats().getChildren().fireChildAdded(sat);
    }


    /**
     * Method that represents the action of removing a SSC Web Services
     * satellite from the "GUI tree", as if this action was
     * triggered in the GUI.
     *
     * @param SSCWS_satID SSCWS_satID The satellite ID string used by SSC Web
     * Services to reference satellites, SatelliteDescription#getId().
     */
    public void removeSSCWSSatAction(String SSCWS_satID) {
        try {
            final String satName = OVTCore.SSCWS_LIBRARY.getSatelliteInfo(SSCWS_satID).name;
            final Sat sat = (Sat) core.getSats().getChildren().getChild(satName);
            core.getSats().removeSat(sat);
            core.getSats().getChildren().fireChildRemoved(sat); // notify TreePanel, Camera maybe.
        } catch (IOException e) {
            getCore().sendErrorMessage(e);
        }
    }


    /**
     * Each JMenuItem is a JCheckBoxMenuItem with a satellite's name, is checked
     * if sat is added to {@link ovt.object.Sats Sats}.
     *
     * @return Array with JMenuItem to put in JMenu.
     */
    public JMenuItem[] createSatsList() {
        File[] files = null;
        final FilenameFilter filter = (File dir, String file) -> file.endsWith(".tle")
                || (file.endsWith(".ltof") && !file.startsWith("Cluster"));
        
        // Look for files in odata in user home.
        final File userOrbitDir = Utils.findUserDir(OVTCore.getOrbitDataDir());
        if (userOrbitDir != null) {
            files = userOrbitDir.listFiles(filter);
        }
        
        // Look for files in system level odata.
        final File sysOrbitDir = Utils.findSysDir(OVTCore.getOrbitDataDir());
        if (sysOrbitDir != null) {
            File[] sysFiles = sysOrbitDir.listFiles(filter);
            if (files == null) {
                files = sysFiles;
            } else {
                files = Utils.concat(files, sysFiles);
            }
        }

        JMenuItem[] items = null;
        if (files == null) {
            return items;
        } else {
            items = new JMenuItem[files.length];
        }

        // ---------------------------------------------------
        // Create one ActionListener used for all Sat in list.
        // ---------------------------------------------------
        final ActionListener actionListener = (ActionEvent evt) -> {
            final JCheckBoxMenuItem item = (JCheckBoxMenuItem) evt.getSource();
            final String satname = item.getText();    // Figure out which Sat is referred to.
            if (item.isSelected()) {
                // Create Sat and ADD it to OVTCore.Sats

                try {
                    Sat sat;
                    // check if TLE file exists
                    String satName = OVTCore.getOrbitDataDir() + Utils.replaceSpaces(satname);
                    File file = Utils.findFile(satName + ".tle");
                    if (file == null) {
                        // check if LTOF file exists
                        file = Utils.findFile(satName + ".ltof");
                        if (file == null) {
                            throw new IOException("Orbit file " + satName + ".tle/.ltof not found");
                        } else {
                            sat = new LTOFSat(getCore());
                        }
                    } else {
                        sat = new TLESat(getCore());
                    }
                    sat.setName(satname);
                    sat.setOrbitFile(file);
                    
                    core.getSats().addSat(sat);
                    core.getSats().getChildren().fireChildAdded(sat);
                } catch (IOException e2) {
                    core.sendErrorMessage(e2);
                }
            } else {
                // REMOVE Sat from OVTCore.Sats
                final Sat sat = (Sat) core.getSats().getChildren().getChild(satname);
                core.getSats().removeSat(sat);
                core.getSats().getChildren().fireChildRemoved(sat); // notify TreePanel, Camera maybe..
            }
            core.Render();
        };

        // Iterate over all found files and add one JCheckBoxMenuItem for each
        // one using the ActionListener created above.
        for (int i = 0; i < files.length; i++) {
            final String filename = files[i].getName();
            final String satName = Utils.replaceUnderlines(filename.substring(0, filename.lastIndexOf('.')));
            final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(satName);
            menuItem.setFont(font);
            menuItem.setSelected(core.getSats().getChildren().containsChild(satName)); // select if sat is already added to OVT
            menuItem.addActionListener(actionListener);   // NOTE: Use previously constructed ActionListener.
            items[i] = menuItem;
        }

        return items;
    }


    protected XYZWindow getVW() {
        return xyzWin;
    }


    protected OVTCore getCore() {
        return xyzWin.getCore();

    }

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
