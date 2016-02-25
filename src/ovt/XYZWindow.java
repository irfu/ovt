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
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import ovt.object.Camera;
import ovt.object.SSCWSSat;
import ovt.object.Sat;
import vtk.rendering.jogl.vtkAbstractJoglComponent;

/**
 * Represents OVT's main window. Contains OVT's global "main" method.
 *
 * PROPOSAL: Optionally set the debug level (Log#setDebugLevel) using a command
 * line argument if there is one?
 *
 * PROPOSAL: Move add/removeSatAction, add/removeSSCWSSatAction,
 * sscwsSatAlreadyAdded, getSSCWSSats to OVTCore? They do use a lot of calls to
 * getCore().
 */
public class XYZWindow extends JFrame implements ActionListener, CoreSource {

    /**
     * NOTE: All loading of native libraries has been collected to this place so
     * that (1) loading is always executed at the launch of the application ==>
     * Can not use native code for unitialized libraries ==> Less risk of
     * errors, (2) debugging/understanding when loading native libraries becomes
     * easier.<BR>
     * NOTE: It is not ideal (but understandable) to load libraries in a static
     * initializer since (1) can not throw exceptions and catch them outside,
     * (2) can not assume that logs are initialized.
     */
    static {
        try {
            Log.log("Loading VTK native libraries", 0);

            // NOTE: vtkNativeLibrary.LoadAllNativeLibraries doc says:
            // "@return true if all library have been successfully loaded"
            // Detect failure without try-catch exceptions.
            final boolean allNativeLibrariesLoaded = vtkNativeLibrary.LoadAllNativeLibraries();  // Separate variable only to make the meaning of the return value clear.
            if (!allNativeLibrariesLoaded) {

                for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
                    if (lib.IsLoaded()) {
                        final String msg = lib.GetLibraryName() + " loaded";
                        Log.log(msg, 0);
                        System.out.println(msg);
                    } else {
                        final String msg = lib.GetLibraryName() + " <----- NOT loaded";
                        Log.err(msg);
                        System.out.println(msg);
                    }
                }
                System.out.println("Make sure the search path is correct:");

            }

            // Log important paths
            // Potentially very useful for debugging problems with loading native files.
            // java.library.path : Paths to where Java will search for native libraries.
            // vtk.lib.dir : Found inside the VTK code. Is added to
            //               java.library.path by vtkNativeLibrary#LoadLibrary (if non-null).
            final String s1 = "java.library.path = " + System.getProperty("java.library.path");
            final String s2 = "vtk.lib.dir       = " + System.getProperty("vtk.lib.dir");
            Log.log(s1, 0);
            Log.log(s2, 0);
            System.out.println(s1);
            System.out.println(s2);

            vtkNativeLibrary.DisableOutputWindow(null);

            /* NOTE: On Linux, System#loadLibrary will prefix the library name
             * with "lib" to find the filename (possibly plus library versioning).
            
             * NOTE: Uncertain what native library "jawt" is for. Is presumably
             * the "Java AWT" library that comes with java. If it is for "Java AWT", 
             * does not Java load it automatically then?!!
            */
            Log.log("Loading native library " + "ovt-" + OVTCore.VERSION, 0);
            System.loadLibrary("ovt-" + OVTCore.VERSION);
            Log.log("Loading native library " + "jawt", 0);
            System.loadLibrary("jawt");

        } catch (SecurityException | UnsatisfiedLinkError | NullPointerException e) {
            final String title = "Failed to load native library";
            Log.err(title);
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error: " + title + "\n" + e.getMessage(), title,
                    javax.swing.JOptionPane.ERROR_MESSAGE);

            throw e;   // Throw again to quit.
        }

        /*
         //Solution of problem for linux dist? http://public.kitware.com/pipermail/vtkusers/2015-March/090424.html

         dir == ('../directory/to/the/VTK/DLLs');
         File[] files = dir.listFiles();
         if (files != null) {
         for (int i = 0; i < files.length; i++) {
         // only the lib-name needed, without file extension
         System.loadLibrary(files[i].getName().substring(0,files[i].getName().length()-4));
         if (files[i].isDirectory()) {
         listDir(files[i]);
         }
         }
         }
         */
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

    private final SSCWSSatellitesBookmarksModel sscwsBookmarks = new SSCWSSatellitesBookmarksModel();

    public static final String SETTING_VISUALIZATION_PANEL_WIDTH = "VisualizationPanel.width";
    public static final String SETTING_VISUALIZATION_PANEL_HEIGHT = "VisualizationPanel.height";
    private static final String SETTING_TREE_PANEL_WIDTH = "TreePanel.width";
    private static final String SETTING_XYZWINDOW_WIDTH = "XYZWindow.width";
    private static final String SETTING_XYZWINDOW_HEIGHT = "XYZWindow.height";
    private static final String SETTING_XYZWINDOW_ORIGIN_X = "XYZWindow.originx";
    private static final String SETTING_XYZWINDOW_ORIGIN_Y = "XYZWindow.originy";
    private static final String SETTING_BOOKMARKED_SSCWS_SATELLITE_IDS = "SSCWSSatellites.Bookmarks";


    public XYZWindow() {
        super(OVTCore.SIMPLE_APPLICATION_NAME + " " + OVTCore.VERSION + " (Build " + OVTCore.BUILD + ")");
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(OVTCore.class.getClassLoader().getResource("images/ovt.gif")));
        } catch (NullPointerException e) {
            Log.err("FileNotFound: images/ovt.gif");
        }

        // Avoid crash on some Win 95 computers.
        if (System.getProperty("os.name").equalsIgnoreCase("Windows 98")) {
            windowResizable = false;
        }

        // Show splash screen
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
         * One must therefore call OVTCore.getGlobalSetting(..) AFTER this command.
         * NOTE: OVTCore(..) makes use of <XYZWindow.this>.renPanel. Therefore
         * renPanel has to have been set BEFORE this command.
         * (NOTE: This is why one should not leak "this" from within a constructor.)
         * NOTE: OVTCore initializes the logs and log files.
         */
        core = new OVTCore(this);

        // Set the renderer
        ren = renPanel.getRenderer();
        final float[] rgb = ovt.util.Utils.getRGB(core.getBackgroundColor());
        ren.SetBackground(rgb[0], rgb[1], rgb[2]);

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        menuBar = new XYZMenuBar(this);
        setJMenuBar(menuBar);

// ----------- Set window size ----------
        // NOTE: The initial window position is set in XYZWindow#start.
        boolean pack = false;
        try {
            setSize(
                    new Dimension(
                            Integer.parseInt(OVTCore.getGlobalSetting(SETTING_XYZWINDOW_WIDTH)),
                            Integer.parseInt(OVTCore.getGlobalSetting(SETTING_XYZWINDOW_HEIGHT)))
            );
        } catch (NumberFormatException e2) {
            final Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();

            setPreferredSize(new Dimension(scrSize.width / 2, scrSize.height / 2));
            //setPreferredSize(new Dimension(DEFAULT_XYZWINDOW_WIDTH, DEFAULT_XYZWINDOW_HEIGHT));
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
            //treePanel.setPreferredSize(new Dimension(treePanelWidth, height));
            treePanel.setPreferredSize(new Dimension(treePanelWidth, getPreferredSize().height));
        }
        treePanel.setMinimumSize(new Dimension(250, 10));

//--------Create a split pane with the two scroll panes in it
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, windowResizable);
        splitPane.setLeftComponent(treePanel);
        splitPane.setRightComponent(renPanel.getComponent());
        splitPane.setOneTouchExpandable(windowResizable);
        splitPane.setDividerSize(6);
        if (treePanelWidth == 0) {
            //renPanel.setSize(width - treePanel.getPreferredSize().width, height);     // NOTE: renPanel.setSize seems unnecessary.
            renPanel.setSize(this.getPreferredSize().width - treePanel.getWidth(), getPreferredSize().height);     // NOTE: renPanel.setSize seems unnecessary.
        }
        contentPane.add(splitPane, BorderLayout.CENTER);

// ------------- Add toolbars -----------
        toolBarContainer = new ToolBarContainer(core, this);
        // sets width and computes and sets height for this width
        toolBarContainer.setPreferredWidth(splitPane.getPreferredSize().width);
        contentPane.add(toolBarContainer, BorderLayout.SOUTH);

        // create Help Window
        htmlBrowser = new HTMLBrowser(core);

        // Create data model (used by GUI) for SSC bookmarks.
        sscwsBookmarks.loadFromGlobalSettingsValue(OVTCore.getGlobalSetting(SETTING_BOOKMARKED_SSCWS_SATELLITE_IDS));

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
    }


    public void start() {

        try {
            final int x = Integer.parseInt(OVTCore.getGlobalSetting(SETTING_XYZWINDOW_ORIGIN_X));
            final int y = Integer.parseInt(OVTCore.getGlobalSetting(SETTING_XYZWINDOW_ORIGIN_Y));

            // On Linux/KDE: It appears that this method always sets the window
            // inside the screen. Therefore one does not need to check for this.
            setLocation(x, y);
        } catch (NumberFormatException e) {
            Utils.setInitialWindowPosition(this, null);
        }

        splashWindow.dispose();

        getTreePanel().expandClusterNode();

        setVisible(true);
        renPanel.resetCamera();
        renPanel.getComponent().requestFocus();

        // Set what the camera should look at initially, and what it should follow.
        core.getCamera().setViewTo(core.getEarth());

        // 1) Set the camera's "ViewFrom" property, to give the 
        // camera position a good initial VALUE: View Earth from the X axis.
        // 2) Set the camera's "ViewFrom" property AGAIN, to give the camera position a
        // good (matter of taste) initial default BEHAVIOUR: Set the ViewFrom property to "Custom",
        // so that it does not automatcally snap back to a view from X axis when rotated, time changed etc.
        // See implementation and use of method Camera#update().
        // /Erik P G Johansson 2015-11-10
        core.getCamera().setViewFrom(Camera.VIEW_FROM_X);   //  Fixed origin bug. /Fredrik Johansson 2015-0x-xx
        core.getCamera().setViewFrom(Camera.VIEW_CUSTOM);

        // Using PARALLEL projection initially leads to camera clipping problems for unknown reason.
        // See comments on bug in class "Camera".
        core.getCamera().setProjection(Camera.PERSPECTIVE_PROJECTION); // Fix clipping bug Fredrik Johansson 15Sept2015
        //core.getCamera().setProjection(Camera.PARALLEL_PROJECTION);
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
     * Is executed when the window closes.
     */
    public void quit() {

        for (SSCWSSat sat : getSSCWSSats()) {
            sat.trySaveCacheToFile();
        }

        try {
            getCore().saveSettings();
        } catch (IOException e2) {
            getCore().sendErrorMessage("Error Saving Settings", e2);
        }

        // save VisualizationPanel's size
        final Dimension d = renPanel.getComponent().getSize();
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
        OVTCore.setGlobalSetting(SETTING_BOOKMARKED_SSCWS_SATELLITE_IDS, sscwsBookmarks.getGlobalSettingsValue());

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
     * Main method for entire OVT. This is where OVT is launched.
     *
     * @param arg Command-line arguments
     */
    public static void main(String[] arg) {
        try {

            final XYZWindow XYZwin = new XYZWindow();
            XYZwin.start();
            //XYZwin.quit();   // DEBUG

        } catch (Throwable e) {
            // NOTE: NOT using OVTCore#sendErrorMessage since the code should not
            // rely on (OVTCore) "core" being successfully initialized here.
            //
            // NOTE: Not ideal since this does not take OVTCore#canDisplayGuiMessages
            // into account like OVTCore#sendErrorMessage does.
            // Use ovt.gui.ErrorMessageWindow?
            //
            // NOTE: Does NOT seem to catch crashing native code (Mac OS) but it
            // has been observed to catch erroneous versions of vtk.jar (Mac OS).
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(null,
                        "Fatal error: " + e.getClass().getCanonicalName() + "; " + e.getMessage(),
                        "Fatal error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
            throw e;
        }
    }


    public HTMLBrowser getHTMLBrowser() {
        return htmlBrowser;
    }


    /**
     * NOTE: Creates the JFrame-based object the first time it is requested and
     * then "caches it".
     *
     * NOTE: Takes care of error message.
     *
     * @return null if error when creating the window (may not be able to
     * download list of satellites). Otherwise a reference to the window.
     */
    public SSCWSSatellitesSelectionWindow getSSCWSSatellitesSelectionWindow() {
        if (sscwsSatellitesSelectionWindow == null) {
            try {
                final SSCWSSatellitesSelectionWindow temp = new SSCWSSatellitesSelectionWindow(
                        OVTCore.SSCWS_LIBRARY, getCore(), this.getSSCWSBookmarksModel());
                sscwsSatellitesSelectionWindow = temp;
            } catch (IOException e) {
                /**
                 * NOTE: Important to catch IOException here.
                 */
                getCore().sendErrorMessage(e);
            }
        }
        return sscwsSatellitesSelectionWindow;
    }


    public SSCWSSatellitesBookmarksModel getSSCWSBookmarksModel() {
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
     * Method representing the action of adding a satellite (of any type: LTOF,
     * TLE, SSCWS) to the GUI tree panel, as if this action was triggered by a
     * user event in the GUI.
     */
    // PROPOSAL: Move to Sats?!! Move to OVTCore? (Not getCore().Render().)
    // PROPOSAL: Check for satellite with the same name.
    public void addSatAction(Sat sat) {
        final String satName = sat.getName();
        final Sat preExistingSat = (Sat) getCore().getSats().getChildren().getChild(satName);  // null if there is no such satellite.
        if (preExistingSat != null) {
            // NOTE: Important to specify that we are speaking of adding a
            // satellite to the "GUI tree", not importing a file or any other form "adding".
            //getCore().sendErrorMessage("Error", "Can not add satellite in the GUI since there already is a satellite with the same name (\"" + satName + "\").");
            return;
        }

        getCore().getSats().addSat(sat);
        getCore().getSats().getChildren().fireChildAdded(sat);

        /* Include here or let the caller call it?!
         * Having the caller call this might be more efficient.
         * Including here might also have implications if automatically calling this function during launch/initialization.
         * Excluding here implies that it is not equivalent to an entire user-triggered "action" and thus the method name is slightly wrong.
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
            sat = new SSCWSSat(getCore(), SSCWS_satID);

            /* NOTE: The string value appears in the GUI tree node, but is also
             used to find the satellite when removing it from the tree(?). */
            sat.setName(SSCWSSat.deriveNameFromSSCWSSatID(SSCWS_satID));
            sat.setOrbitFile(null);   // The only valid parameter value.
        } catch (IOException | SSCWSLibrary.NoSuchSatelliteException e) {
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
        } catch (IOException | SSCWSLibrary.NoSuchSatelliteException e) {
            getCore().sendErrorMessage(e);
        }
    }


    /**
     * @param True if-and-only-if there is a SSCWSSat object corresponding to
     * the argument in the GUI tree.
     */
    public boolean sscwsSatAlreadyAdded(String SSCWS_satID) throws IOException, SSCWSLibrary.NoSuchSatelliteException {
        // NOTE: Implementation assumes there is only one Sat by that exact name.
        final Sat sat = (Sat) getCore().getSats().getChildren().getChild(SSCWSSat.deriveNameFromSSCWSSatID(SSCWS_satID));
        return (sat instanceof SSCWSSat);
    }


    /**
     * Obtain list of SSCWSSat objects added to the "GUI tree".
     */
    public List<SSCWSSat> getSSCWSSats() {
        final Object[] satObjects = getCore().getSats().getChildren().toArray();
        final List<SSCWSSat> sscwsSatList = new ArrayList();
        for (Object satObj : satObjects) {
            if (satObj instanceof SSCWSSat) {
                sscwsSatList.add((SSCWSSat) satObj);
            }
        }
        return sscwsSatList;
    }

}   // XYZWindow

class SplashWindow extends JWindow {

    private final JLabel imageLabel;


    public SplashWindow() {
        super();
        java.net.URL url = OVTCore.class.getClassLoader().getResource("images/splash.gif");
        if (url == null) {
            Log.err("FileNotFound: images/splash.gif");
            imageLabel = null;
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

        // Center splash window
        Utils.setInitialWindowPosition(this, null);
    }

}
