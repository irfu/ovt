/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /stor/devel/ovt2g/ovt/XYZWindow.java,v $
 Date:      $Date: 2009/10/23 22:10:03 $
 Version:   $Revision: 2.15 $


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
package ovt;

import ovt.gui.*;
import ovt.util.*;
import ovt.interfaces.*;

import vtk.*;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;
import ovt.object.Camera;
import ovt.object.SSCWSSat;
import ovt.object.Sat;
import vtk.rendering.jogl.vtkAbstractJoglComponent;

/**
 * Represents OVT's main window.
 */
public class XYZWindow extends JFrame implements ActionListener, CoreSource {

    static {
        if (!vtkNativeLibrary.LoadAllNativeLibraries()) {
            for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
                if (!lib.IsLoaded()) {
                    System.out.println(lib.GetLibraryName() + " not loaded");
                }
            }
        }
        vtkNativeLibrary.DisableOutputWindow(null);
    }

    protected OVTCore core;
    private SplashWindow splashWindow;

    // VTK stuff
    protected final JOGLVisPanel renPanel;
    protected vtkRenderer ren;
    // GUI
    public static StatusLine statusLine = new StatusLine();
    protected TreePanel treePanel;
    public boolean windowResizable = true;
    protected XYZMenuBar menuBar;
    protected JSplitPane splitPane;
    private final ToolBarContainer toolBarContainer;
    protected HTMLBrowser htmlBrowser;
    private SSCWSSatellitesSelectionWindow sscwsSatellitesSelectionWindow;

    private final SSCWSSatellitesBookmarks sscwsBookmarks = new SSCWSSatellitesBookmarks();

    public static final String SETTING_VISUALIZATION_PANEL_WIDTH = "VisualizationPanel.width";
    public static final String SETTING_VISUALIZATION_PANEL_HEIGHT = "VisualizationPanel.height";
    private static final String SETTING_TREE_PANEL_WIDTH = "TreePanel.width";
    private static final String SETTING_XYZWINDOW_WIDTH = "XYZWindow.width";
    private static final String SETTING_XYZWINDOW_HEIGHT = "XYZWindow.height";
    private static final String SETTING_XYZWINDOW_ORIGIN_X = "XYZWindow.originx";
    private static final String SETTING_XYZWINDOW_ORIGIN_Y = "XYZWindow.originy";
    private static final String SETTINGS_BOOKMARKED_SSCWS_SATELLITE_IDS = "SSCWSSatellites.Bookmarks";

    private static final int DEFAULT_VISUALIZATION_PANEL_WIDTH = 600;
    private static final int DEFAULT_VISUALIZATION_PANEL_HEIGHT = 600;


    public XYZWindow() {
        super("Orbit Visualization Tool " + OVTCore.VERSION + " (Build " + OVTCore.BUILD + ")");
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(OVTCore.class.getClassLoader().getResource("images/ovt.gif")));
        } catch (NullPointerException npe) {
            Log.err("FileNotFound: images/ovt.gif");
        }

        // Avoid crash on some Win 95 computers.
        if (System.getProperty("os.name").equalsIgnoreCase("Windows 98")) {
            windowResizable = false;
        }

        // show splashscreen
        splashWindow = new SplashWindow();
        splashWindow.setVisible(true);

//------- create the vtkPanel ----------
    /* Javadoc for <java.awt.Frame.this>.addNotify():
         "Makes this Frame displayable by connecting it to a native screen
         resource. Making a frame displayable will cause any of its children
         to be made displayable. This method is called internally by the toolkit
         and should not be called directly by programs." */
        addNotify();

        //renPanel = new VisualizationPanel(this);
        renPanel = new JOGLVisPanel();
        addOriginActor();
        vtkAbstractJoglComponent.attachOrientationAxes(renPanel);

//------- create the OVTCore ----------
        /* NOTE: This loads "GlobalSettings" (static in OVTCore class).
         One must therefore call OVTCore.getGlobalSetting(..) AFTER this command.
         NOTE: OVTCore(..) makes use of <XYZWindow.this>.renPanel. Therefore
         renPanel has to have been set BEFORE this command.
         (NOTE: This is why one should not leak "this" from within a constructor.) */
        core = new OVTCore(this);

        int width = DEFAULT_VISUALIZATION_PANEL_WIDTH;
        int height = DEFAULT_VISUALIZATION_PANEL_HEIGHT;
        try {
            width = Integer.parseInt(OVTCore.getGlobalSetting(SETTING_VISUALIZATION_PANEL_WIDTH));
            height = Integer.parseInt(OVTCore.getGlobalSetting(SETTING_VISUALIZATION_PANEL_HEIGHT));
        } catch (NumberFormatException ignore) {
        }
        renPanel.setSize(width, height);             // NOTE: renPanel.setSize seems unnecessary.

        // set the renderer
        ren = renPanel.getRenderer();
        float[] rgb = ovt.util.Utils.getRGB(core.getBackgroundColor());
        ren.SetBackground(rgb[0], rgb[1], rgb[2]);

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        menuBar = new XYZMenuBar(this);
        setJMenuBar(menuBar);

// ----------- Set window size ----------
        boolean pack = false;
        try {
            setSize(Integer.parseInt(OVTCore.getGlobalSetting(SETTING_XYZWINDOW_WIDTH)),
                    Integer.parseInt(OVTCore.getGlobalSetting(SETTING_XYZWINDOW_HEIGHT))
            );
        } catch (NumberFormatException e2) {
            pack = true;
        }

// ------- Set ContentPane Layout
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

//-------- Create the tree panel -----
        treePanel = new TreePanel(getCore());
        int treePanelWidth = treePanel.getPreferredSize().width;
        try {
            treePanelWidth = Integer.parseInt(OVTCore.getGlobalSetting(SETTING_TREE_PANEL_WIDTH));
        } catch (NumberFormatException ignore) {
        }

        if (treePanelWidth != 0) {
            treePanel.setPreferredSize(new Dimension(treePanelWidth, height));
        }
        treePanel.setMinimumSize(new Dimension(200, 10));

//--------Create a split pane with the two scroll panes in it
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, windowResizable);
        splitPane.setLeftComponent(treePanel);
        splitPane.setRightComponent(renPanel.getComponent());
        splitPane.setOneTouchExpandable(windowResizable);
        splitPane.setDividerSize(6);
        if (treePanelWidth == 0) {
            renPanel.setSize(width - treePanel.getPreferredSize().width, height);     // NOTE: renPanel.setSize seems unnecessary.
        }
        contentPane.add(splitPane, BorderLayout.CENTER);

// ------------- add toolbars -----------
        toolBarContainer = new ToolBarContainer(core, this);
        // sets width and computes and sets height for this width
        toolBarContainer.setPreferredWidth(splitPane.getPreferredSize().width);
        contentPane.add(toolBarContainer, BorderLayout.SOUTH);

        // create Help Window
        htmlBrowser = new HTMLBrowser(core);

        sscwsBookmarks.loadFromGlobalSettingsValue(OVTCore.getGlobalSetting(SETTINGS_BOOKMARKED_SSCWS_SATELLITE_IDS));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });

        if (pack) {
            pack(); // pack if no settings are present
        }
        if (!windowResizable) {
            setResizable(windowResizable);
        }

        // TEST/DEBUG
        // Automatically add satellites in the GUI (add to the left-hand GUI tree) as if the user had done it.
        //addSSCWSSatAction("Enterprise");
        //menuBar.removeSSCWSSatAction("Enterprise");   // REMOVE SATELLITE
        //menuBar.addSSCWSSatAction("DataGapSat");
        //menuBar.addSSCWSSatAction("DownloadFailSat");
        //addSSCWSSatAction("ZzzzSat10");
        //addSSCWSSatAction("ZzzzSat11");
        //addSSCWSSatAction("ZzzzSat12");
        //addSSCWSSatAction("ZzzzSat13");
        //---
        //menuBar.addSSCWSSatAction("doublestar1");
        //menuBar.addSSCWSSatAction("cluster1");
    }


    public void start() {
        //refreshGUI();

        final Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension windowSize = getSize();

        try {
            final int x = Integer.parseInt(OVTCore.getGlobalSetting(SETTING_XYZWINDOW_ORIGIN_X));
            final int y = Integer.parseInt(OVTCore.getGlobalSetting(SETTING_XYZWINDOW_ORIGIN_Y));

            // On Linux/KDE: It appears that this method always sets the window
            // inside the screen. Therefore one does not need to check for this.            
            setLocation(x, y);
        } catch (NumberFormatException e2) {
            setLocation(scrnSize.width / 2 - windowSize.width / 2, scrnSize.height / 2 - windowSize.height / 2);
        }

        splashWindow.dispose();

        getTreePanel().expandClusterNode();

        setVisible(true);
        renPanel.resetCamera();
        renPanel.getComponent().requestFocus();
        core.getCamera().setViewFrom(Camera.VIEW_FROM_X);
        core.getCamera().setProjection(1);
        core.Render();

    }


    /**
     * @return OVTCore instance
     */
    @Override
    public final OVTCore getCore() {
        return core;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
    }


    public void Render() {
        renPanel.Render();
    }


    public JOGLVisPanel getRenpanel() {
        return renPanel;
    }


    public vtkRenderer getRenderer() {
        return renPanel.getRenderer();
    }


    public vtkRenderWindow getRenderWindow() {
        return renPanel.getRenderWindow();
    }


    /**
     * Is executed when the window closes
     */
    public void quit() {

        try {
            getCore().saveSettings();
        } catch (IOException e2) {
            getCore().sendErrorMessage("Error Saving Settings", e2);
        }
        // save VisualizationPanel's size
        Dimension d = renPanel.getComponent().getSize();
        if (isResizable()) {
            OVTCore.setGlobalSetting(SETTING_VISUALIZATION_PANEL_WIDTH, "" + d.width);
            OVTCore.setGlobalSetting(SETTING_VISUALIZATION_PANEL_HEIGHT, "" + d.height);
        }
        OVTCore.setGlobalSetting(SETTING_TREE_PANEL_WIDTH, "" + treePanel.getWidth());
        OVTCore.setGlobalSetting(SETTING_XYZWINDOW_WIDTH, "" + getWidth());
        OVTCore.setGlobalSetting(SETTING_XYZWINDOW_HEIGHT, "" + getHeight());

        OVTCore.setGlobalSetting(SETTING_XYZWINDOW_ORIGIN_X, "" + getX());
        OVTCore.setGlobalSetting(SETTING_XYZWINDOW_ORIGIN_Y, "" + getY());

        OVTCore.setGlobalSetting("startMjd", "" + getCore().getTimeSettings().getTimeSet().getStartMjd());
        OVTCore.setGlobalSetting("intervalMjd", "" + getCore().getTimeSettings().getTimeSet().getIntervalMjd());
        OVTCore.setGlobalSetting("stepMjd", "" + getCore().getTimeSettings().getTimeSet().getStepMjd());
        OVTCore.setGlobalSetting("currentMjd", "" + getCore().getTimeSettings().getTimeSet().getCurrentMjd());
        OVTCore.setGlobalSetting(SETTINGS_BOOKMARKED_SSCWS_SATELLITE_IDS, sscwsBookmarks.getGlobalSettingsValue());
        try {
            OVTCore.saveGlobalSettings();
        } catch (IOException e2) {
            core.sendErrorMessage("Error Saving Settings", e2);
        }
        System.exit(0);
    }


    public static void setStatus(String statusMessage) {
        statusLine.setStatus(statusMessage);
    }


    /**
     * Returns <code>true</code> if user pressed <code>Yes</code> else
     * <code>false</code>.
     *
     * @param none
     * @return see upp!
     * @see javax.swing.JOptionPane#showOptionDialog
     */
    private boolean quitConfirmed() {
        Object[] options = {"Yes", "No"};
        int n = JOptionPane.showOptionDialog(this, "Do you really want to exit?", "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, "No");
        return n == JOptionPane.YES_OPTION;
    }


    /**
     * Main method. Here we launch OVT
     *
     * @param arg
     */
    public static void main(String[] arg) {
        XYZWindow XYZwin = new XYZWindow();
        XYZwin.start();
        //XYZwin.quit();   // DEBUG

    }


    public HTMLBrowser getHTMLBrowser() {
        return htmlBrowser;
    }


    public SSCWSSatellitesBookmarks getSSCWSBookmarks() {
        return sscwsBookmarks;
    }


    protected final void addOriginActor() {
        vtkVectorText atext = new vtkVectorText();
        atext.SetText(". (0, 0, 0)");
        vtkPolyDataMapper mapper = new vtkPolyDataMapper();
        mapper.SetInputConnection(atext.GetOutputPort());
        vtkFollower actor = new vtkFollower();
        actor.SetMapper(mapper);
        actor.SetScale(0.02);
        actor.AddPosition(0, 0, 0);
        actor.SetCamera(getRenderer().GetActiveCamera());
        actor.GetProperty().SetColor(1, 0, 0);
        getRenderer().AddActor(actor);
    }

    /*
     public void setRenWinSize(int width, int height) {
     renPanel.setSize(width, height);
     treePanel.setSize((int)treePanel.getSize().width,height+50);
     pack();
     }
     */

    public boolean isWindowResizable() {
        return windowResizable;
    }


    public RenPanel getVisualizationPanel() {
        return renPanel;
    }


    public XYZMenuBar getXYZMenuBar() {
        return menuBar;
    }


    public TreePanel getTreePanel() {
        return treePanel;
    }


    /**
     * NOTE: Creates the JFrame-based object the first time it is requested and
     * then "caches it".
     */
    public SSCWSSatellitesSelectionWindow getSSCWSSatellitesSelectionWindow() throws IOException {
        if (sscwsSatellitesSelectionWindow == null) {
            sscwsSatellitesSelectionWindow = new SSCWSSatellitesSelectionWindow(
                    OVTCore.SSCWS_LIBRARY, getCore(), this.getSSCWSBookmarks());
        }
        return sscwsSatellitesSelectionWindow;
    }


    /**
     * Have SSCWSSatellitesSelectionWindow set the global settings, but ONLY if
     * it has already been instantiated. One could call
     * getSSCWSSatellitesSelectionWindow().setGlobalSettings() directly instead
     * but that would instantiate the window (if it has not already been done)
     * which in turn requires network traffic which slows down (and risks
     * creating error messsages) which is unnecessary if the user does not work
     * with SSCWS satellites.
     */
    /*public void sscwsSatellitesSelectionWindow_setGlobalSettings() {
     if (sscwsSatellitesSelectionWindow != null) {
     sscwsSatellitesSelectionWindow.setGlobalSettings();
     }
     }*/
    /**
     * Method representing the action of adding a satellite (of any type: LTOF,
     * TLE, SSCWS) to the GUI tree panel, as if this action was triggered by a
     * user event in the GUI.
     */
    // PROPOSAL: Move to Sats?!! (Not getCore().Render().)
    // PROPOSAL: Check for satellite with the same name.
    public void addSatAction(Sat sat) {
        final String satName = sat.getName();
        final Sat preExistingSat = (Sat) getCore().getSats().getChildren().getChild(satName);  // null if there is no such satellite.
        if (preExistingSat != null) {
            // NOTE: Important to specify that we are speaking of adding a
            // satellite to "GUI tree", not importing a file or any other form "adding".
            //getCore().sendErrorMessage("Error", "Can not add satellite in the GUI since there already is a satellite with the same name (\"" + satName + "\").");
            return;
        }

        getCore().getSats().addSat(sat);
        getCore().getSats().getChildren().fireChildAdded(sat);

        /* Include here or let the caller call it?!
         Having the caller call this might be more efficient.
         Including here might also have implications if automatically calling this function during launch/initialization.            
         Excluding here implies that it is not equivalent to an entire user-triggered "action" and thus the method name is slightly wrong.
         */
        getCore().Render();
    }


    /**
     * Method representing the action of removing a satellite (of any type:
     * LTOF, TLE, SSCWS) to the GUI tree panel, as if this action was triggered
     * by a user event in the GUI.
     *
     * @param satName The satellite's name (in the GUI), i.e. Sat.getName().
     */
    public void removeSatAction(String satName) {
        final Sat sat = (Sat) core.getSats().getChildren().getChild(satName);  // null if there is no such satellite.
        if (sat == null) {
            //getCore().sendErrorMessage("Error", "Can not find satellite to remove from GUI (\"" + satName + "\").");
            return;
        }
        getCore().getSats().removeSat(sat);
        getCore().getSats().getChildren().fireChildRemoved(sat); // notify TreePanel, Camera maybe..

        /* Include here or let the caller call it?!
         Having the caller call this might be more efficient.
         (Including here might also have implications if automatically calling this function during launch/initialization.)
         Excluding here implies that it is not equivalent to an entire user-triggered "action" and thus the method name is slightly wrong.
         */
        getCore().Render();
    }


    /**
     * Method that represents the action of adding a SSC Web Services satellite
     * to the "GUI tree", as if this action was triggered by a user event in the
     * GUI.
     *
     * NOTE: This method does not check whether the satellite is already in the
     * GUI tree. It is possible to add the same satellite multiple times,
     * resulting in multiple tree nodes.
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
            sat.setName(SSCWSSat.deriveNameFromSSCWSSatID(SSCWS_satID));
            sat.setOrbitFile(null);
        } catch (IOException e) {
            getCore().sendErrorMessage(e);
            return;
        }
        addSatAction(sat);
    }


    /**
     * Method that represents the action of removing a SSC Web Services
     * satellite from the "GUI tree", as if this action was triggered in the
     * GUI.
     *
     * @param SSCWS_satID SSCWS_satID The satellite ID string used by SSC Web
     * Services to reference satellites, SatelliteDescription#getId().
     */
    public void removeSSCWSSatAction(String SSCWS_satID) {
        try {
            if (!sscwsSatAlreadyAdded(SSCWS_satID)) {
                // NOTE: This check will also capture the case of sat==null.
                //getCore().sendErrorMessage("Error", "Can not find (SSC-based) satellite to remove (SCWS_satID=\""+SSCWS_satID+").");
            } else {
                removeSatAction(SSCWSSat.deriveNameFromSSCWSSatID(SSCWS_satID));
            }
            /*getCore().getSats().removeSat(sat);
             getCore().getSats().getChildren().fireChildRemoved(sat); // notify TreePanel, Camera maybe.*/
        } catch (IOException e) {
            getCore().sendErrorMessage(e);
        }
    }


    /**
     * @param True iff there is a SSCWSSat object corresponding to the argument
     * in the GUI tree.
     */
    public boolean sscwsSatAlreadyAdded(String SSCWS_satID) throws IOException {
        // NOTE: Implementation assumes there is only one Sat by that exact name.
        final Sat sat = (Sat) getCore().getSats().getChildren().getChild(SSCWSSat.deriveNameFromSSCWSSatID(SSCWS_satID));
        return (sat instanceof SSCWSSat);
    }

}   // XYZWindow

class SplashWindow extends JWindow {

    JLabel imageLabel;


    public SplashWindow() {
        super();
        java.net.URL url = OVTCore.class.getClassLoader().getResource("images/splash.gif");
        if (url == null) {
            Log.err("FileNotFound: images/splash.gif");
            return;
        }

        imageLabel = new JLabel(new ImageIcon(url));
        imageLabel.setBorder(BorderFactory.createRaisedBevelBorder());
        Dimension labelSize = imageLabel.getPreferredSize();
        imageLabel.setBounds(0, 0, labelSize.width, labelSize.height);

        JLabel label = new JLabel("Version " + OVTCore.VERSION + ", " + OVTCore.RELEASE_DAY);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(Color.yellow);
        labelSize = label.getPreferredSize();
        label.setBounds(imageLabel.getPreferredSize().width - labelSize.width - 80, 255, labelSize.width, labelSize.height);

        JLabel copyrightLabel = new JLabel("Copyright (c) OVT Team, 2000-2015");
        copyrightLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        copyrightLabel.setForeground(Color.white);
        labelSize = copyrightLabel.getPreferredSize();
        copyrightLabel.setBounds(
                imageLabel.getPreferredSize().width - labelSize.width - 81,
                290, labelSize.width, labelSize.height);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(imageLabel.getPreferredSize());
        layeredPane.add(imageLabel, 0, 1);
        layeredPane.add(label, 1, 0);
        layeredPane.add(copyrightLabel, new Integer(2));

        setSize(imageLabel.getPreferredSize());
        getContentPane().add(layeredPane, BorderLayout.CENTER);
        pack();

        // center splash window
        Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(scrnSize.width / 2 - windowSize.width / 2,
                scrnSize.height / 2 - windowSize.height / 2);
    }

}
