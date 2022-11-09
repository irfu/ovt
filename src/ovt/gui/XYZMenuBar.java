/*=========================================================================
 
 Program:   Orbit Visualization Tool
 
 Copyright (c) 2016 OVT Team 
 (Erik Johansson, Fredrik Johansson, Yuri Khotyaintsev)
 Copyright (c) 2000-2003 OVT Team 
 (Kristof Stasiewicz, Mykola Khotyaintsev, Yuri Khotyaintsev)
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
 Khotyaintsev, E. P. G. Johansson, F. Johansson
 
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
    private final OVTCore core;
    private final XYZWindow xyzWin;
    private JMenuItem importSatelliteMenuItem;                   // Rationalize away?
    private JMenuItem sscwsSatellitesSelectionWindowMenuItem;    // Rationalize away?
    private ovt.object.editor.SettingsEditor renPanelSizeEditor;

    /**
     * NOTE: Text should (probably) fit both importing and opening a TLE file.
     */
    private static final String TLE_WARNING_TITLE = "Using TLE file";
    /**
     * NOTE: Text should (probably) fit both importing and opening a TLE file.
     *
     * Error estimates come from comparing TLE file orbits and SSCWS orbits for
     * somewhat random but "long" time intervals (60-300 days) for freja (5-35
     * km), akebono (62-87 km), and de1 (DE-1) (7-164 km). NOTE: de1.tle is
     * either partially corrupt or is partially (badly) misinterpreted by OVT.
     * Visualizations show several jumps which I have not used in the
     * comparison.<BR>
     * /Erik P G Johansson 2016-01-18
     */
    private static final String TLE_WARNING_MSG = OVTCore.SIMPLE_APPLICATION_NAME
            + " can open TLE files but with position errors of up to ca 200 km.";


    public XYZMenuBar(XYZWindow xyzwin) {
        super();
        this.core = xyzwin.getCore();
        xyzWin = xyzwin;
        JMenuItem menuItem;

        //-----------
        // File menu
        //-----------
        JMenu menu = new JMenu("File");
        menu.setFont(font);

        menuItem = new JMenuItem("Export Image...");
        //menuItem.setAccelerator(KeyStroke.getKeyStroke(
        //        KeyEvent.VK_E, ActionEvent.CTRL_MASK)); // Set shortcut Ctrl-E. Added to aid debugging/testing.

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
                final String defaultFile = OVTCore.getGlobalSetting(OVTCore.SETTING_DEFAULT_SAVED_STATE_FILENAME, core.getUserDir());
                final String filePath = Settings.showOpenDialog(xyzWin, new File(defaultFile));
                if (filePath != null) {
                    try {
                        // hide all objects
                        //Settings.load(core.getConfSubdir() + "hideall.xml", core);
                        getCore().hideAllVisibleObjects();
                        // load new settings
                        Settings.load(filePath, core);

                        xyzWin.getTreePanel().expandSatellitesNode();
                        OVTCore.setGlobalSetting(OVTCore.SETTING_DEFAULT_SAVED_STATE_FILENAME, filePath);
                        core.Render();
                    } catch (IOException e2) {
                        core.sendErrorMessage("Error loading settings", e2);
                    }
                }
            }
        });

        menu.add(menuItem);

        menuItem = new JMenuItem("Save State...");
        menuItem.setFont(font);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                final String defaultFile = OVTCore.getGlobalSetting(OVTCore.SETTING_DEFAULT_SAVED_STATE_FILENAME, core.getUserDir());
                final String filePath = Settings.showSaveDialog(xyzWin, new File(defaultFile));
                if (filePath != null) {
                    try {
                        Settings.save(core, filePath);
                        OVTCore.setGlobalSetting(OVTCore.SETTING_DEFAULT_SAVED_STATE_FILENAME, filePath);
                    } catch (IOException e2) {
                        core.sendErrorMessage("Error saving settings", e2);
                    }
                }
            }
        });

        menu.add(menuItem);

        //menu.addSeparator();
        //menu.add(menuItem);
        //if ( xyzWin.windowResizable ){
//        menu.addSeparator();
//
//        // NOTE: Menu item is disabled since it is not fully implemented.
//        menuItem = new JMenuItem("Print...");
//        menuItem.setFont(font);
//        menuItem.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                ImageOperations.print(getCore());
//            }
//        });
//        menu.add(menuItem);
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
                KeyEvent.VK_V, ActionEvent.CTRL_MASK));   // Ctrl-V. Collides with "Paste" keyboard shortcut (Windows).
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

        menuItem = new JMenuItem("OMNI2 Settings...");
        menuItem.setFont(Style.getMenuFont());
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                getCore().getMagProps().setOMNI2CustomizerVisible(true);
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

                final String url = "file:" + getCore().getUserDir() + getCore().getDocsSubdir() + "about.html";
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
                    if (sat instanceof TLESat) {
                        // Only show warning message if (1) the file was successfully
                        // imported, and (2) the file was a TLE file.
                        sendTLEWarningMessage();
                    }
                    core.getSats().addSatAction(sat);
                }
            }
        });

        return menuItem;
    }


    public JMenuItem createSSCWSSatellitesSelectionWindowMenuItem() {
        final JMenuItem menuItem = new JMenuItem("Select SSC-based Satellites...");
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
                final JMenu satsMenu = (JMenu) event.getSource();
                satsMenu.removeAll();                 // NOTE: Remove all previously existing MenuItems!

                // Import Satellite ...
                satsMenu.add(importSatelliteMenuItem);

                // SSC Satellites Selection Window
                satsMenu.add(sscwsSatellitesSelectionWindowMenuItem);

                // List of TLE/LTOF (file-based) satellites.
                satsMenu.addSeparator();
                JMenuItem[] items = createSatsMenuItemList_LTOF_TLE();
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

        /* NOTE: Exceptions are likely to be thrown here rather than later,
         * inside addSSCWSSatAction and removeSSCWSSatAction. */
        final String satName = getCore().getSscwsLib().getSatelliteInfo(SSCWS_satID).name;  // throws IOException, NoSuchSatelliteException

        // Create -------- ActionListener --------
        final ActionListener actionListener = (ActionEvent evt) -> {
            final JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) evt.getSource();

            if (menuItem.isSelected()) {
                // Create Sat and add it to OVTCore.Sats.
                xyzWin.addSSCWSSatAction(SSCWS_satID);
            } else {
                // Remove Sat from OVTCore.Sats .
                xyzWin.removeSSCWSSatAction(SSCWS_satID);
            }
            core.Render();
        };
        // --------   --------

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
                return text1.compareToIgnoreCase(text2);      // NOTE: Does not adapt to locale.
                // Can use String#compareTo but that (it seems) treats all upper case as coming before all lower case characters.
            }
        });
    }


    /**
     * Return array of JMenuItems representing LTOF and TLE files in standard
     * orbit data directories. Each JMenuItem is a
     * JCheckBoxMenuItem with a satellite's name, and is checked (checkbox is
     * checked) if sat is added to {@link ovt.object.Sats Sats}.
     *
     * NOTE: Special case when treating the Cluster[1-4].ltof files.
     *
     * @return Array with JMenuItem to put in JMenu.
     */
    public JMenuItem[] createSatsMenuItemList_LTOF_TLE() {
        /*=====================================
         Look for files in odata/ in user home.
         =====================================*/
        File[] allFiles = new File[0];   // Default value: empty array, to which files are "added".
        {
            /**
             * Filter files in directory based on file suffix.
             * 
             * NOTE: Explicitly excludes files "Cluster[1-4].ltof" since they
             * are automatically opened by ClusterSats (or some code in the
             * neighbourhood) and are therefore treated specially somewhere
             * else.
             */
            final FilenameFilter filenameFilter
                    = (File dir, String file)
                    -> file.endsWith(".tle") || (file.endsWith(".ltof") && !(file.matches("Cluster[1-4].ltof")));

            // Look for files in user account orbit data directory.
            final File userOrbitDir = Utils.findUserDir(OVTCore.getOrbitDataSubdir());
            if (userOrbitDir != null) {
                final File[] userFiles = userOrbitDir.listFiles(filenameFilter);
                allFiles = Utils.concat(allFiles, userFiles);
            } else {
                Log.log("Can not find directory (Utils.findUserDir(\"" + OVTCore.getOrbitDataSubdir() + "\")).");
            }

            // Look for files in system level orbit data directory.
            final File sysOrbitDir = Utils.findSysDir(OVTCore.getOrbitDataSubdir());
            if (sysOrbitDir != null) {
                final File[] sysFiles = sysOrbitDir.listFiles(filenameFilter);
                allFiles = Utils.concat(allFiles, sysFiles);
            } else {
                Log.log("Can not find directory (Utils.findSysDir(\"" + OVTCore.getOrbitDataSubdir() + "\")).");
            }
        }

        /*if (allFiles == null) {
         return null;
         }*/
        final JMenuItem[] menuItems = new JMenuItem[allFiles.length];

        //=========================================================================
        // Create one instance of ActionListener that is used for ALL Sat in list.
        //=========================================================================
        final ActionListener actionListener = (ActionEvent evt) -> {
            final JCheckBoxMenuItem item = (JCheckBoxMenuItem) evt.getSource();
            final String satName = item.getText();    // Figure out which Sat is referred to.
            
            if (item.isSelected()) {
                // CASE: Menu item has been selected.
                // Create Sat and ADD it to OVTCore.Sats.

                try {
                    final Sat sat;
                    // Check if TLE file exists.
                    final String satFilePathPrefix = OVTCore.getOrbitDataSubdir() + Utils.replaceSpaces(satName);
                    File file = Utils.findFile(satFilePathPrefix + ".tle");
                    if (file == null) {
                        // Check if LTOF file exists.
                        file = Utils.findFile(satFilePathPrefix + ".ltof");
                        if (file == null) {
                            throw new IOException("Orbit file " + satFilePathPrefix + ".tle/.ltof not found");
                        }
                        sat = new LTOFSat(getCore());
                    } else {
                        sendTLEWarningMessage();
                        sat = new TLESat(getCore());
                    }
                    sat.setName(satName);
                    sat.setOrbitFile(file);

                    core.getSats().addSatAction(sat);
                } catch (IOException e2) {
                    core.sendErrorMessage(e2);
                }
            } else {
                // CASE: Menu item has been DE-selected.
                // REMOVE Sat from OVTCore.Sats.
                xyzWin.removeSatByNameAction(satName);
            }
        };
        //=========================================================================

        // Iterate over all files found and add one JCheckBoxMenuItem for each
        // one using the ActionListener created above.
        for (int i = 0; i < allFiles.length; i++) {
            final String filename = allFiles[i].getName();
            final String satName = Utils.replaceUnderlines(filename.substring(0, filename.lastIndexOf('.')));
            final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(satName);
            menuItem.setFont(font);
            menuItem.setSelected(core.getSats().getChildren().containsChild(satName));   // Select if sat is already added to OVT.
            // NOTE: Use previously constructed ActionListener. Same ActionListener for ALL menu items.
            menuItem.addActionListener(actionListener);
            
            menuItems[i] = menuItem;
        }

        sortMenuItemsByText(menuItems);

        return menuItems;
    }


    /**
     * Called when needing to warn the user that OVT only handles TLE files
     * approximately correctly. It is useful to have this as a separate method
     * if one is uncertain of when to trigger the warning in the code (maybe
     * even in multiple places?) and might move it.
     */
    public void sendTLEWarningMessage() {
        core.sendWarningMessage(
                TLE_WARNING_TITLE,
                TLE_WARNING_MSG);
    }


    // VW = ? (Volkswagen?!)
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
        //magProps.activityEditors[activityIndex].setVisible(true);
        magProps.setActivityEditorVisible(activityIndex, true);
    }

}
