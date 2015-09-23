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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.List;
import ovt.util.SSCWSLibrary.NoSuchSatelliteException;

public class XYZMenuBar extends JMenuBar {

    private final Font font = Style.getMenuFont();
    private OVTCore core;
    private final XYZWindow xyzWin;
    private JMenuItem importSatelliteMenuItem;                   // Rationalize away?
    private JMenuItem sscwsSatellitesSelectionWindowMenuItem;    // Rationalize away?
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

        menuItem = new JMenuItem("Load State...");
        menuItem.setFont(font);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String defaultFile = OVTCore.getGlobalSetting(OVTCore.DEFAULT_SETTINGS_FILE, core.getConfSubdir());
                String file = Settings.showOpenDialog(xyzWin, new File(defaultFile));
                if (file != null) {
                    try {
                        // hide all objects
                        //Settings.load(core.getConfSubdir() + "hideall.xml", core);
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

        menuItem = new JMenuItem("Save State...");
        menuItem.setFont(font);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String defaultFile = OVTCore.getGlobalSetting(OVTCore.DEFAULT_SETTINGS_FILE, core.getUserDir());
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

        // JMenuItems for the activity indexes (multiple menu items, one per index).
        final MagProps magProps = getCore().getMagProps();
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
                final FieldlineMapper flm = new FieldlineMapper(getCore());
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
                final FieldlineMapper flm = new FieldlineMapper(getCore());
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
                final HTMLBrowser hw = getVW().getHTMLBrowser();
                
                final String url = "file:" +getCore().getUserDir()+ getCore().getDocsSubdir() + "about.html";
                //final String url = "file:" + getCore().getDocsSubdir() + "about.html"; FKJN edit 15Sept 2015
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
        final JMenuItem menuItem = new JMenuItem("Import Satellite File...");
        menuItem.setFont(Style.getMenuFont());

        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                final ImportSatelliteWizard wizard = new ImportSatelliteWizard(core.getSats(), xyzWin);
                final Sat sat = wizard.start();
                if (sat != null) {
                    xyzWin.addSatAction(sat);
                }
            }
        });

        return menuItem;
    }


    public JMenuItem createSSCWSSatellitesSelectionWindowMenuItem() {
        final JMenuItem menuItem = new JMenuItem("Select SSC-based Satellites ...");
        menuItem.setFont(Style.getMenuFont());

        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                final SSCWSSatellitesSelectionWindow sscwsWin = getCore().getXYZWin().getSSCWSSatellitesSelectionWindow();
                if (sscwsWin != null) {
                    sscwsWin.setVisible(true);
                }
            }
        });

        return menuItem;
    }


    /**
     * Create Satellites menu data structure.<BR>
     *
     * NOTE: The method only creates a JMenu and a MenuListener immediately, but
     * no JMenuItems. The actual JMenuItems are created and added dynamically to
     * the JMenu every time the "Satellites" menu is clicked. Any previous
     * JMenuItems are destroyed first.
     */
    private JMenu createSatsMenu() {
        if (importSatelliteMenuItem == null) {
            importSatelliteMenuItem = createImportSatelliteMenuItem();
        }
        if (sscwsSatellitesSelectionWindowMenuItem == null) {
            sscwsSatellitesSelectionWindowMenuItem = createSSCWSSatellitesSelectionWindowMenuItem();
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

                // SSC Satellites Selection Window
                satsMenu.add(sscwsSatellitesSelectionWindowMenuItem);

                // List of TLE/LTOF (file-based) satellites.
                satsMenu.addSeparator();
                JMenuItem[] items = createLTOF_TLESatsMenuItemList();
                for (JMenuItem item : items) {
                    satsMenu.add(item);
                }

                // List of SSCWS satellites (online data).
                satsMenu.addSeparator();
                items = createSSCWSSatsMenuItemsList();
                for (JMenuItem item : items) {
                    satsMenu.add(item);
                }
            }
        });
        return menu;
    }


    /**
     * Create one menu item for _ONE_ SSC WS satellite which the caller can put
     * in the data structures describing the GUI.
     *
     * @return Always returns valid JMenuItem (if no Exception).
     * @throws IOException for every failure: (1) SSCWS_satID does not exist, or
     * (2) obtaining the online SSCWS satellites list fails (indirect).
     */
    // Throw exceptions on failure and let the caller decide what to do?
    private JMenuItem createSSCWSSatMenuItem(String SSCWS_satID) throws IOException, SSCWSLibrary.NoSuchSatelliteException {
        final String satName;

        /* NOTE: Exceptions are likely to be thrown here rather than later,
         * inside addSSCWSSatAction and removeSSCWSSatAction. */
        satName = OVTCore.SSCWS_LIBRARY.getSatelliteInfo(SSCWS_satID).name;  // throws IOException, NoSuchSatelliteException

        final ActionListener actionListener = (ActionEvent evt) -> {
            final JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) evt.getSource();

            if (menuItem.isSelected()) { // create Sat and add it to OVTCore.Sats
                xyzWin.addSSCWSSatAction(SSCWS_satID);
            } else {
                // remove Sat from OVTCore.Sats 
                xyzWin.removeSSCWSSatAction(SSCWS_satID);
            }
            core.Render();
        };

        final JMenuItem newMenuItem = new JCheckBoxMenuItem(satName);
        newMenuItem.setFont(Style.getMenuFont());
        newMenuItem.addActionListener(actionListener);

        // Select if sat is already added to OVT.
        newMenuItem.setSelected(xyzWin.sscwsSatAlreadyAdded(SSCWS_satID));   // throws IOException if can not get satellites list.
        return newMenuItem;

    }


    private JMenuItem[] createSSCWSSatsMenuItemsList() {
        final Set<String> satIDs = xyzWin.getSSCWSBookmarksModel().getBookmarkedSSCWSSatIds();

        final List<JMenuItem> menuItems = new ArrayList();

        for (String satID : satIDs) {
            try {
                final JMenuItem menuItem = createSSCWSSatMenuItem(satID);
                menuItems.add(menuItem);
            } catch (IOException e) {
                /**
                 * Fail silently (does not throw Exception) and stop trying
                 * again if it can not create menu item due to network failure.
                 * SSCWSLibrary. Case 2: Network failure (can not indirectly
                 * download satellites list). We do not want a popup every time
                 * the user tries to open the menu. The already written log
                 * error message (stdout) for the error is OK.
                 */
                System.out.println("ERROR: " + e.getMessage());
                break;   // Do not try again with other satellite.
            } catch (NoSuchSatelliteException e) {
                /**
                 * It is best to just fail to display menu items for satellites
                 * which existence in the satellite list can not be confirmed.
                 * This tends to happen if one switches between implementations
                 * of SSCWSLibrary. Should still continue with other satellites.
                 */
                System.out.println("ERROR: " + e.getMessage());
            }
        }
        // NOTE: Does NOT remove the satellite ID from the bookmarks if it
        // is invalid. See comments in the SSCWSSatelliteBookmarks class.
        final JMenuItem[] menuItemsArray = new JMenuItem[menuItems.size()];
        menuItems.toArray(menuItemsArray);
        sortMenuItemsByText(menuItemsArray);
        return menuItemsArray;
    }


    /**
     * @param menuItems Array of menu items that is to sorted. The result is
     * also stored in this array.
     */
    private void sortMenuItemsByText(JMenuItem[] menuItems) {
        // Sort menu items based on text.
        Arrays.sort(menuItems, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                final String text1 = ((JMenuItem) o1).getText();
                final String text2 = ((JMenuItem) o2).getText();
                return text1.compareToIgnoreCase(text2);      // Does not adapts to locale.
                // Can use String#compareTo but that (it seems) treats all upper case as coming before all lower case characters.
            }
        });
    }


    /**
     * Each JMenuItem is a JCheckBoxMenuItem with a satellite's name, is checked
     * if sat is added to {@link ovt.object.Sats Sats}.
     *
     * @return Array with JMenuItem to put in JMenu.
     */
    public JMenuItem[] createLTOF_TLESatsMenuItemList() {
        /*=====================================
         Look for files in odata in user home.
         =====================================*/
        File[] files = null;
        {
            final FilenameFilter filter
                    = (File dir, String file)
                    -> file.endsWith(".tle") || (file.endsWith(".ltof") && !file.startsWith("Cluster"));

            final File userOrbitDir = Utils.findUserDir(OVTCore.getOrbitDataSubdir());
            if (userOrbitDir != null) {
                files = userOrbitDir.listFiles(filter);
            }

            // Look for files in system level odata.
            final File sysOrbitDir = Utils.findSysDir(OVTCore.getOrbitDataSubdir());
            if (sysOrbitDir != null) {
                File[] sysFiles = sysOrbitDir.listFiles(filter);
                if (files == null) {
                    files = sysFiles;
                } else {
                    files = Utils.concat(files, sysFiles);
                }
            }
        }

        if (files == null) {
            return null;
        }
        final JMenuItem[] menuItems = new JMenuItem[files.length];

        // ---------------------------------------------------
        // Create one ActionListener used for all Sat in list.
        // ---------------------------------------------------
        final ActionListener actionListener = (ActionEvent evt) -> {
            final JCheckBoxMenuItem item = (JCheckBoxMenuItem) evt.getSource();
            final String satname = item.getText();    // Figure out which Sat is referred to.
            if (item.isSelected()) {
                // Create Sat and ADD it to OVTCore.Sats.

                try {
                    final Sat sat;
                    // Check if TLE file exists.
                    final String satFilePathPrefix = OVTCore.getOrbitDataSubdir() + Utils.replaceSpaces(satname);
                    File file = Utils.findFile(satFilePathPrefix + ".tle");
                    if (file == null) {
                        // Check if LTOF file exists
                        file = Utils.findFile(satFilePathPrefix + ".ltof");
                        if (file == null) {
                            throw new IOException("Orbit file " + satFilePathPrefix + ".tle/.ltof not found");
                        }
                        sat = new LTOFSat(getCore());
                    } else {
                        sat = new TLESat(getCore());
                    }
                    sat.setName(satname);
                    sat.setOrbitFile(file);

                    xyzWin.addSatAction(sat);
                } catch (IOException e2) {
                    core.sendErrorMessage(e2);
                }
            } else {
                // REMOVE Sat from OVTCore.Sats.
                xyzWin.removeSatAction(satname);
            }
        };

        // Iterate over all files found and add one JCheckBoxMenuItem for each
        // one using the ActionListener created above.
        for (int i = 0; i < files.length; i++) {
            final String filename = files[i].getName();
            final String satName = Utils.replaceUnderlines(filename.substring(0, filename.lastIndexOf('.')));
            final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(satName);
            menuItem.setFont(font);
            menuItem.setSelected(core.getSats().getChildren().containsChild(satName)); // select if sat is already added to OVT
            menuItem.addActionListener(actionListener);   // NOTE: Use previously constructed ActionListener.
            menuItems[i] = menuItem;
        }

        sortMenuItemsByText(menuItems);

        return menuItems;
    }


    protected XYZWindow getVW() {
        return xyzWin;
    }


    protected OVTCore getCore() {
        return xyzWin.getCore();

    }

}

/**
 * JMenuItem for any of the (magnetic) activity indexes.
 */
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
