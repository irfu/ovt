/*=========================================================================
 
 Program:   Orbit Visualization Tool
 Source:    $Source: /stor/devel/ovt2g/ovt/OVTCore.java,v $
 Date:      $Date: 2009/10/08 20:53:40 $
 Version:   $Revision: 2.25 $
 
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
 
 OVT Team (https://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
 Khotyaintsev
 
 =========================================================================*/
package ovt;

import ovt.gui.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import vtk.*;

import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

/**
 * This is a very "central" class in OVT. (This class does NOT contain the
 * "main(...)" method of OVT though.)
 *
 * This object represents the root node in the "GUI tree" in the left panel.
 * Practically however, it also serves as a central repository for a LOT of
 * information for MANY classes. When loading/saving settings from/to XML files,
 * then this class is the root in (presumably) a tree which is iterated over.
 *
 * NOTE: The application creates one instance of this object which it then
 * passes around to many, many other classes. This is unfortunate since this
 * class is also dependent on many other OVT-specific classes and instances
 * thereof (including of GUI classes) which makes it difficult to instantiate in
 * isolation. This in turn makes it difficult to separately test (instantiate)
 * classes that require having a reference to an instance of OVTCore. The class
 * also has many static members which are used throughout the application.
 * Future modification should be careful with adding more dependence on OVTCore.
 * Should more static members (non-final fields, methods that depend on class
 * state) be turned into instance members?<BR>
 * /Erik P G Johansson 2016-01-22
 *
 * @author Mykola Khotyaintsev
 * @version %I% %E%
 * @see ...
 */
//
// PROPOSAL: Split up class into two classes?!: One which is the root node in the GUI tree,
//           and one which is GUI independent and only supplies information and easy to
//           instantiate separately (for the purpose of testing other classes relying on
//           what is now the single OVTCore class).
//
public final class OVTCore extends OVTObject implements GUIPropertyEditorListener {

    private static final String ROOT_NODE_NAME = "OVT";
    public static final String SIMPLE_APPLICATION_NAME = "Orbit Visualization Tool";
    public static final String VERSION = "3.0";
    public static final String RELEASE_DAY = "April 2016";
    // BUILD incremented to "5" (from "4") 2015-09-14 on request from Yuri Khotyaintsev (for beta version to beta testers?)
    // BUILD incremented to "6" (from "5") 2015-11-11 for Yuri Khotyaintsev's demo version and new beta versions.
    // BUILD incremented to "7" (from "6") 2016-04-14. Intended for the final release version.
    public static final int BUILD = 7;
    public static final String OVT_HOMEPAGE = "https://ovt.irfu.se/";

    private static final String GLOBAL_SETTINGS_FILE_NAME = "ovt.conf";

    /**
     * NOTE: Name of KEY in the settings file. The corresponding VALUE (in the
     * settings file) is a filename. The setting corresponds to the initial file
     * (path) in the save/load state dialog.
     */
    public static final String SETTING_DEFAULT_SAVED_STATE_FILENAME = "StateFileDefault";
    private static final Properties globalProperties = new Properties();
    /**
     * File to which stdout should be directed. null=Print to screen instead of
     * log file.
     */
    private static final String SYSTEM_OUT_FILE_NAME = "system_out.log";
    private static final String SYSTEM_ERR_FILE_NAME = "system_err.log";
    private static final int GLOBAL_LOG_LEVEL = 0;

    // Include RELEASE_DAY? (Might not be updated during development.)
    private static final String LONG_APPLICATION_DESCRIPTION
            = SIMPLE_APPLICATION_NAME + " (OVT), version " + VERSION
            + ", build " + BUILD + " (" + ovt.OVTCore.OVT_HOMEPAGE + ")";

    public static final String HTTP_AGENT_PROPERTY_STRING = LONG_APPLICATION_DESCRIPTION;
    /*+ "; "
     + System.getProperty("os.name") + ", "
     + System.getProperty("os.arch");*/

    private vtkRenderer renderer = null;
    /**
     * @see #setRenderAction(Renderable)
     */
    private RenPanel renPanel = null;
    private TimeSettings timeSettings;

    private final XYZWindow XYZwin;

    private MagProps magProps;
    private TransCollection transCollection;

    private CoordinateSystem coordinateSystem;

    //  Visual Objects
    private SunLight sunLight;
    private Sats sats;
    private Earth earth;
    private Axes axes;
    private Frames frames;
    private Magnetosphere magnetosphere;
    private Magnetopause magnetopause;
    private BowShock bowShock;
    private MagTangent magTangent;
    private Camera camera;
    private GroundStations groundStations;    // Ground-based stations
    private ElectPot electPot;
    private OutputLabel outputLabel;
    private SSCWSLibrary sscwsLib;

    private static boolean guiPresent = false;
    private boolean canDisplayGuiMessages = false;  // Whether error/warning messages can be displayed in a popup.
//    protected boolean isInitialized = false;

    /* Absolute path to directory under the home directory. */
    private static String ovtUserDir;

    /**
     * Holds value of property server.
     */
    private static boolean server = false;

    /**
     * Holds value of property backgroundColor.
     */
    private Color backgroundColor = Color.white;


    /**
     * This constructor is used for offscreen Ren Panel We don't pass
     * OffscreenRenPanel class to make java compiler forget about the Servlet
     * part of OVT and compile OVT Core without complaining with missing
     * classes.
     *
     * @param renPanel
     */
    public OVTCore(RenPanel renPanel) {
        this.XYZwin = null;
        this.renPanel = renPanel;
        this.renderer = renPanel.getRenderer();
        // use renPanel.Render() instead of renderer.Render().

        setServer(true);
        Initialize();
//        isInitialized = true;
        guiPresent = false;
        canDisplayGuiMessages = false;
    }


    public OVTCore(XYZWindow xyzwin) {
        this.XYZwin = xyzwin;
        this.renPanel = xyzwin.getVisualizationPanel();
        this.renderer = renPanel.getRenderer();

        canDisplayGuiMessages = true;

        Initialize();

//        isInitialized = true;
        guiPresent = true;
    }


    public static String getUserDir() {
        return ovtUserDir;
    }


    public static String getDocsSubdir() {
        return "docs" + File.separator;
    }


    public static String getImagesSubdir() {
        return "images" + File.separator;
    }


    public static String getUserdataSubdir() {
        return "userdata" + File.separator;
    }


    public static String getMdataSubdir() {
        return "mdata" + File.separator;
    }


    public static final String getOrbitDataSubdir() {
        return "odata" + File.separator;
    }


    /**
     * Subdirectory for SSCWS orbit cache files.
     */
    public static final String getSSCWSCacheSubdir() {
        return "cache" + File.separator + "sscsats" + File.separator;
    }


    /**
     * Subdirectory for OMNI2 cache files.
     */
    public static final String getOMNI2CacheSubdir() {
        return "cache" + File.separator + "OMNI2" + File.separator;
    }


    public static String getConfSubdir() {
        return "conf" + File.separator;
    }


    public RenPanel getRenPanel() {
        return renPanel;
    }


    public vtkRenderer getRenderer() {
        return renderer;
    }


    /**
     * Uses <CODE>getRenderer.Render()</CODE> unless renderAction is specified.
     *
     * @see #setRenderAction(Rendererable)
     */
    public void Render() {
        //Log.log("Core::Render()");
        //int i = 1/0; // used for tracing multy-rendering problem case
        //if (renderAction != null) renderAction.Render();
        //else getRenderer().Render();
        renPanel.Render();
        setStatus("Done.");
    }


    public Trans getTrans(double mjd) {
        return transCollection.getTrans(mjd);
    }


    /**
     * Load properties from global settings file.
     */
    private static synchronized void loadGlobalSettings() throws IOException {
        final File confFile = Utils.findFile(getConfSubdir() + GLOBAL_SETTINGS_FILE_NAME);     // NOTE: Will not throw Exception if file does not exist.
        if (confFile != null) {

            // NOTE: new FileInputStream(confFile)) will throw NullPointerException (not IOException) if confFile == null.
            try (FileInputStream in = new FileInputStream(confFile)) {
                globalProperties.load(in);
            }
        }
    }


    public synchronized void saveSettings() throws IOException {
        groundStations.save();
    }


    public static synchronized void saveGlobalSettings() throws IOException {
        /* NOTE: Utils.findFile will return null if it can NOT locate an already
         existing file, i.e. it will NOT suggest a path for where to create a new
         config file if none already exists. Therefore, if no old config file
         exists, no new one will be created.
         */
        //File confFile = Utils.findFile(getConfSubdir() + globalSettingsFileName);  
        /*if (confFile == null) {
         throw new IOException("Can not find a global settings file to overwrite. ");
         }*/

        /* Try saving to user directory, otherwise do not save at all. */
        final File confFile = new File(OVTCore.getUserDir() + getConfSubdir() + GLOBAL_SETTINGS_FILE_NAME);

        // Create temporary Properties object that is sorted alphabetically when
        // being saved to file/stream.
        // This makes the settings file easier to read and find changes in.
        // NOTE: Sorts all upper case letter before all lower case letters.
        final Properties sortedProperties = new Properties() {
            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<Object>(super.keySet()));
            }
        };
        sortedProperties.putAll(globalProperties);

        try (FileOutputStream out = new FileOutputStream(confFile)) {
            sortedProperties.store(out, "Configuration file ; " + LONG_APPLICATION_DESCRIPTION);
        }
    }


    // PROPOSAL: Throw exception (custom-made class?) if property can not be found.
    public static String getGlobalSetting(String key) {
        return globalProperties.getProperty(key);
    }


    /**
     * Get global setting.
     *
     * @param defaultValue String value to be returned if there is not property
     * for "key". Note that this must be a string, even if the returned value is
     * converted to e.g. a number.
     */
    public static String getGlobalSetting(String key, String defaultValue) {
        return globalProperties.getProperty(key, defaultValue);
    }


    public static synchronized void setGlobalSetting(String key, String value) {
        globalProperties.put(key, value);
    }


    public void Initialize() {
        Log.setLogLevel(GLOBAL_LOG_LEVEL);

        Log.log("Initializing...", 3);
        setName(ROOT_NODE_NAME);

        try {
            setIcon(new ImageIcon(Utils.findResource("images/ovt.gif")));
        } catch (FileNotFoundException e2) {
            e2.printStackTrace(System.err);
        }

        {
            /*==================================================================
             Derive and assign this.ovtUserDir (String, path) and make sure
             the corresponding actual directory exists.
             --------------------------------------------------------------
             PROPOSAL: Move into OVTCore#getUserDir() (with "caching")?
             ==================================================================*/

            final String osName = System.getProperty("os.name").toLowerCase();
            final boolean isMacOs = osName.startsWith("mac os x");
            if (isMacOs) {
                ovtUserDir = System.getProperty("user.home") + File.separator
                        + "Library" + File.separator
                        + "ovt" + File.separator
                        + VERSION + File.separator;
            } else {
                ovtUserDir = System.getProperty("user.home") + File.separator
                        + ".ovt" + File.separator
                        + VERSION + File.separator;
            }
            final File userDir = new File(ovtUserDir);
            if (!userDir.exists()) {
                if (userDir.mkdirs()) {
                    Log.log("Created:" + ovtUserDir, 3);
                } else {
                    Log.log("Failed to create:" + ovtUserDir, 3);
                }
            }

            // Must create this directory in order to be able to save ovt.conf there.
            final File userConfDir = new File(ovtUserDir + getConfSubdir());
            if (!userConfDir.exists()) {
                if (userConfDir.mkdirs()) {
                    Log.log("Created:" + userConfDir.getAbsolutePath(), 3);
                } else {
                    Log.log("Failed to create:" + userConfDir.getAbsolutePath(), 3);
                }
            }
        }

        //====================================================================
        // Set System.out and System.err (stdout and stderr).
        // NOTE: Can only be set to files AFTER that internal paths have been
        // obtained. Can therefore not be initialized earlier.
        //====================================================================
        try {
            // NOTE: It appears that on MS Windows, one will NOT get an explicit
            // log file (for stdout) without explicitly redirecting System.out.
            // NOTE: OVT should only put files in a directory where it likely has
            // write permission (i.e. not the application directory).
            // NOTE: Can only call OVTCore.getUserDir() after ovtUserDir has been initialized.
            // ==> Not ideal since some log messages have already been created.
            if (SYSTEM_ERR_FILE_NAME != null) {
                System.setErr(new PrintStream(new FileOutputStream(OVTCore.getUserDir() + SYSTEM_ERR_FILE_NAME), true));
            }
            if (SYSTEM_OUT_FILE_NAME != null) {
                System.setOut(new PrintStream(new FileOutputStream(OVTCore.getUserDir() + SYSTEM_OUT_FILE_NAME), true));
            }
            Log.setPrintStream(System.out);
        } catch (FileNotFoundException e) {
            Log.setPrintStream(System.err);   // Has to be done explicitly rather than use the default value.
            Log.logStackTrace(e);
            Log.err("Failed to redirect System.err (stderr) or System.out (stdout) to file.");
        }

        Log.log("Java version: " + System.getProperty("java.version"), 0);

        /* Setting the http user agent for the benefit of NASA SSC, so that they
         * can see who/what (OVT) is using their service (over the internet).
         *
         * NASA SSC documentation:
         * "You are strongly encouraged to have your client set the HTTP User-Agent header (RFC 2068)
         * to a value that identifies your client application in each SSC Web Service request that it makes.
         * This will allow us to measure the usefulness of these services and justify their continued
         * support. It isn't too important what value you use but it's best if it uniquely identifies
         * your application."
         * http://sscweb.gsfc.nasa.gov/WebServices/SOAP/DevelopersKit.html
         */
        System.setProperty("http.agent", HTTP_AGENT_PROPERTY_STRING);

        /*======================================================================
         Load global settings
         --------------------
         NOTE: This code indirectly uses this.ovtUserDir which therefore has to
         have been previously initialized.
         ======================================================================*/
        if (globalProperties.size() == 0) {
            try {
                loadGlobalSettings();
            } catch (IOException e) {
                sendErrorMessage("Error when loading global settings", e);
            }
        }

        // Set time
        timeSettings = new TimeSettings(this);
        Log.log("TimeSettings created.", 3);

        Log.log("Creating MagProps ...", 3);
        magProps = new MagProps(this);
        Log.log("MagProps created.", 3);

        transCollection = new TransCollection(magProps.getIgrfModel());
        Log.log("TransCollection created.", 3);

        // Set coordinate system
        coordinateSystem = new CoordinateSystem(this);
        Log.log("CoordinateSystem created.", 3);
        // Add sunlight
        sunLight = new SunLight(this);
        Log.log("SunLight created.", 3);
        // Set frames
        Log.log("Creating axes ...", 3);
        axes = new Axes(this);
        // Set Earth
        Log.log("Creating Earth ...", 3);
        earth = new Earth(this);

        // Set frames
        Log.log("Creating Frames ...", 3);
        frames = new Frames(this);

        // Set frames
        Log.log("Creating Ground-Based Stations ...", 3);
        groundStations = new GroundStations(this);

        bowShock = new BowShock(this);
        magTangent = new MagTangent(this);

        // Load satellites
        sats = new Sats(this);

        // Set magnetosphere
        Log.log("Creating Magnetosphere ...", 3);
        magnetosphere = new Magnetosphere(this);

        //set magnetopause
        magnetopause = new Magnetopause(this);

        //set electPot
        electPot = new ElectPot(this);

        //set output label
        outputLabel = new OutputLabel(this);
        
        /**
         * Select what to use as a data source for the functionality/code that
         * handles SSC Web Services satellites.
         */
        // The real, nominal data source.
        sscwsLib = new SSCWSLibraryImpl(
                Const.EARLIEST_PERMITTED_GUI_TIME_MJD,
                SSCWSLibraryImpl.DEFAULT_WSDL_URL_STRING,
                SSCWSLibraryImpl.DEFAULT_QNAME_NAMESPACE_URI,
                SSCWSLibraryImpl.DEFAULT_QNAME_LOCAL_PART
        );    
        // Data source emulator for testing.
        //sscwsLib = SSCWSLibraryTestEmulator.DEFAULT_INSTANCE;  

        //magProps = new ovt.mag.MagProps(this);
        Log.log("Throwing timeChangeEvent to everybody ... ", 4);
        timeSettings.addTimeChangeListener(sunLight);
        timeSettings.addTimeChangeListener(earth);
        timeSettings.addTimeChangeListener(bowShock);
        timeSettings.addTimeChangeListener(magTangent);
        timeSettings.addTimeChangeListener(sats);
        timeSettings.addTimeChangeListener(magnetosphere);
        timeSettings.addTimeChangeListener(magnetopause);
        timeSettings.addTimeChangeListener(electPot);
        timeSettings.addTimeChangeListener(outputLabel);

        coordinateSystem.addCoordinateSystemChangeListener(sunLight);
        coordinateSystem.addCoordinateSystemChangeListener(earth);
        coordinateSystem.addCoordinateSystemChangeListener(bowShock);
        coordinateSystem.addCoordinateSystemChangeListener(magTangent);
        coordinateSystem.addCoordinateSystemChangeListener(sats);
        coordinateSystem.addCoordinateSystemChangeListener(magnetosphere);
        coordinateSystem.addCoordinateSystemChangeListener(magnetopause);
        coordinateSystem.addCoordinateSystemChangeListener(electPot);

        magProps.addMagPropsChangeListener(sats);
        magProps.addMagPropsChangeListener(magnetosphere);
        magProps.addMagPropsChangeListener(magnetopause);
        magProps.addMagPropsChangeListener(electPot);
        magProps.addMagPropsChangeListener(bowShock);
        magProps.addMagPropsChangeListener(magTangent);

        // set children
        //
        // not visual
        //addChild(timeSettings);
        //addChild(coordinateSystem);
        //addChild(magProps);
        // visual
        addChild(bowShock);
        addChild(magTangent);
        addChild(magnetopause);
        addChild(magnetosphere);
        addChild(electPot);
        addChild(sats);
        addChild(earth);
        addChild(axes);
        addChild(frames);
        addChild(groundStations);
        addChild(sunLight);
        addChild(outputLabel);

        // camera (creating it after Children tree created because camera
        //         enumerates all objects looking PositionSourse to listen to visible property)
        Log.log("Creating camera ...", 3);
        camera = new Camera(this);
        timeSettings.addTimeChangeListener(camera);
        coordinateSystem.addCoordinateSystemChangeListener(camera);

        // Motify camera about sat's change
        // Will be removed in future (when OVT will be build on start from XML).
        getSats().fireSatsChanged(); // or .getChildren().fireChildrenChanged() doesn't matter
        getGroundBasedStations().getChildren().fireChildrenChanged();

        // load time settings
        try {
            double startMjd = Double.parseDouble(OVTCore.getGlobalSetting("startMjd"));
            double intervalMjd = Double.parseDouble(OVTCore.getGlobalSetting("intervalMjd"));
            double stepMjd = Double.parseDouble(OVTCore.getGlobalSetting("stepMjd"));
            double currentMjd = Double.parseDouble(OVTCore.getGlobalSetting("currentMjd"));
            timeSettings.setTimeSet(new TimeSet(startMjd, intervalMjd, stepMjd, currentMjd));
        } catch (Exception ignore) {
        }

    }


    public TimeSettings getTimeSettings() {
        return timeSettings;
    }


    public double getMjd() {
        return getTimeSettings().getCurrentMjd(); //getTimeSet().
    }


    /**
     * Returns current C
     *
     * @return current CS
     */
    public int getCS() {
        return getCoordinateSystem().getCoordinateSystem();
    }


    /**
     * Returns current C
     *
     * @return polar CS
     */
    public int getPolarCS() {
        return getCoordinateSystem().getPolarCoordinateSystem();
    }


    /**
     * Returns true if everything is initialized. Is used for GUI to check if it
     * is necessary to plot something.
     *
     * @return True if everything is initialized.
     */
//    public boolean isInitialized()
//    { return isInitialized; }
    public boolean canDisplayGuiMessages() {
        // Use GraphicsEnvironment.isHeadless instead?
        return canDisplayGuiMessages;
    }


    // Could almost be converted into an instance method.
    public static boolean isGuiPresent() {
        return guiPresent;
    }


    /**
     * This method is a part of GUIPropertyEditorListener. This listener is
     * added to all editors, to render after user changes some of the
     * parameters.
     *
     * @param evt
     */
    @Override
    public void editingFinished(GUIPropertyEditorEvent evt) {
        Render();
    }


    /**
     * Method to be used when informing the user about the exception occurred.
     * When in GUI produces a popup window with <I>Error</I>
     * as window title and <CODE>e.getMessage()</CODE> as message.
     *
     * @param e Exception
     */
    public void sendErrorMessage(Exception e) {
        sendErrorMessage("Error", e);
    }


    /**
     * Method to be used when informing user about the error occurred. When in
     * GUI produces a popup window with <CODE>msghead</CODE> as window title and
     * <CODE>msg</CODE> as message.
     *
     * @param title Message title. NOTE: Only used for the (alternative) log
     * message.
     * @param e Exception
     */
    public void sendErrorMessage(String title, Exception e) {
//        if (isGuiPresent()){
        if (canDisplayGuiMessages()) {
            new ErrorMessageWindow(XYZwin, e).setVisible(true);   // NOTE: Using internal class.
        } else {
            Log.err(title + ": " + e);
        }
    }


    public void sendErrorMessage(String title, String msg) {
//        if (isGuiPresent()){
        if (canDisplayGuiMessages()) {
            javax.swing.JOptionPane.showMessageDialog(null, msg, title,
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } else {
            Log.err(title + ": " + msg);
        }
    }


    /**
     * Method to be used when warning the user something. When in GUI it
     * produces a popup window with <CODE>msghead</CODE> as window title and
     * <CODE>msg</CODE> as message.
     *
     * NOTE: The word "Warning" is added to the output. Therefore, do not put
     * this in the argument strings. NOTE: No automatic line breaks which makes
     * long messages unwieldy.
     *
     * NOTE: javax.swing.JOptionPane.showMessageDialog can handle HTML which
     * makes some formatting possible, incl. automatic line breaks but then with
     * a fixed width (i.e. not a maximum width, i.e. one which can be greater
     * than the length of the printed message). However, the method
     * implementation concatenates the parameter string with other strings which
     * (probably) makes it impossible to use HTML in the "msg" since the whole
     * string has to be surrounded with {@code <HTML>...</HTML>}(?).
     *
     * NOTE: Not entirely obvious whether the message dialog window should be
     * centered on the screen or on a parent/owner window.
     *
     * @param title Message title
     * @param msg Warning message
     */
    public void sendWarningMessage(String title, String msg) {
//        if (isGuiPresent()){
        if (canDisplayGuiMessages()) {
            javax.swing.JOptionPane.showMessageDialog(
                    XYZwin,
                    //                    "Warning: " + title + "\n" + msg,   // Adds "title" to the message.
                    "Warning: " + msg,
                    title,
                    javax.swing.JOptionPane.WARNING_MESSAGE);
        } else {
            System.out.println(title + ": " + msg);
        }
    }


    public void sendWarningMessage(String title, Exception e) {
        sendWarningMessage(title, e.getMessage());
    }


    public void sendMessage(String msghead, String msg, JFrame parentComponent) {
//        if (isGuiPresent()){
        if (canDisplayGuiMessages()) {
            javax.swing.JOptionPane.showMessageDialog(parentComponent, msg, msghead,
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } else {
            System.out.println(msghead + ":" + msg);
        }
    }


    // Could almost be made into an instance method.
    public static void setStatus(String statusMessage) {
        if (isGuiPresent()) {
            XYZWindow.setStatus(statusMessage);
        } else {
            System.out.println("status : " + statusMessage);
        }
    }


    // for JNI methods
    // Moved to XYZWindow
//    static {
//        System.loadLibrary("ovt-" + VERSION);
//        System.loadLibrary("jawt");
//    }
    /**
     * Detect OS type.
     *
     * @return True if OS=windows.
     */
    public static boolean isUnderWindows() {
        String osName = System.getProperty("os.name");
        return osName.toLowerCase().contains("win");
    }


    /**
     * Getter for property backgroundColor.
     *
     * @return Value of property backgroundColor.
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }


    /**
     * Setter for property backgroundColor.
     *
     * @param backgroundColor New value of property backgroundColor.
     */
    public void setBackgroundColor(Color backgroundColor) {
        Color oldBackgroundColor = this.backgroundColor;
        this.backgroundColor = backgroundColor;
        float[] rgb = Utils.getRGB(backgroundColor);
        renderer.SetBackground(rgb[0], rgb[1], rgb[2]);
        propertyChangeSupport.firePropertyChange("backgroundColor", oldBackgroundColor, backgroundColor);
    }


    public void hideAllVisibleObjects() {
        Enumeration e = getChildren().elements();
        while (e.hasMoreElements()) {
            Object obj = e.nextElement();
            if (obj instanceof VisualObject) {
                ((VisualObject) obj).setVisible(false);
            }
        }
        e = sats.getChildren().elements();
        while (e.hasMoreElements()) {
            Object obj = e.nextElement();
            if (obj instanceof VisualObject) {
                ((VisualObject) obj).setVisible(false);
            }
        }
    }


    /**
     * Returns true if OVT is run in server mode.
     *
     * @return Value of property server.
     */
    public static boolean isServer() {
        return server;
    }


    /**
     * Setter for property server.
     *
     * @param serverMode New value of property server.
     */
    public static void setServer(boolean serverMode) {
        server = serverMode;
    }


    public void setAsText(PropertyPath pp, String value)
            throws PropertyVetoException {
        //System.out.println("pp=" + pp);
        //System.out.println("value=" + value);
        try {
            OVTPropertyEditor editor = getEditor(pp, this);
            //System.out.println("editor=" + editor);
            Log.log("setAsText : " + pp + "=" + value, 5);
            editor.setAsText(value);
        } catch (IllegalArgumentException e) {
            throw new PropertyVetoException(e.getMessage(), null);
        }
    }


    public String getAsText(PropertyPath pp)
            throws IllegalArgumentException {
        OVTPropertyEditor editor = getEditor(pp, this);
        return editor.getAsText();
    }


    public static OVTPropertyEditor getEditor(PropertyPath pp, OVTObject obj)
            throws IllegalArgumentException {
        String objectPath = pp.getObjectPath();
        String propertyName = pp.getPropName();
        // find the object!
        Log.log("getEditor . property='" + propertyName + "' object='" + objectPath + "'", 7);

        DescriptorsSource propertyHolder;
        propertyHolder = obj.getObject(objectPath);     // try to get object from children
        //DBG*/System.out.println("Object found in children: " + propertyHolder);

        Log.log("Found property holder object = " + propertyHolder, 7);
        Descriptors descr = propertyHolder.getDescriptors();
        if (descr == null) {
            throw new IllegalArgumentException(propertyHolder + " has no descriptors.");
        }
        BasicPropertyDescriptor pd = descr.getDescriptor(propertyName);
        if (pd == null) {
            throw new IllegalArgumentException(" Object '" + objectPath + "' has no property '" + propertyName + "'");
        }
        return pd.getPropertyEditor();
    }


    @Override
    public Descriptors getDescriptors() {
        if (descriptors == null) {
            try {
                descriptors = new Descriptors();

                BasicPropertyDescriptor pd = new BasicPropertyDescriptor("backgroundColor", this);
                pd.setMenuAccessible(false);
                pd.setLabel("Space color");
                pd.setDisplayName("Space Color");
                ComponentPropertyEditor editor = new ColorPropertyEditor(pd);
                // Render each time user changes time by means of gui
                editor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                    public void editingFinished(GUIPropertyEditorEvent evt) {
                        Render();
                    }
                });
                addPropertyChangeListener("backgroundColor", editor);
                pd.setPropertyEditor(new WindowedPropertyEditor(editor, getXYZWin(), "Close"));
                descriptors.put(pd);

            } catch (IntrospectionException e2) {
                System.out.println(getClass().getName() + " -> " + e2.toString());
                System.exit(0);
            }
        }
        return descriptors;
    }


    public XYZWindow getXYZWin() {
        return XYZwin;
    }


    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }


    public MagProps getMagProps() {
        return magProps;
    }


    public Camera getCamera() {
        return camera;
    }


    public BowShock getBowShock() {
        return bowShock;
    }


    public MagTangent getMagTangent() {
        return magTangent;
    }


    public Magnetopause getMagnetopause() {
        return magnetopause;
    }


    public Magnetosphere getMagnetosphere() {
        return magnetosphere;
    }


    public ElectPot getElectPot() {
        return electPot;
    }


    public Sats getSats() {
        return sats;
    }


    public Earth getEarth() {
        return earth;
    }


    public Axes getAxes() {
        return axes;
    }


    public Frames getFrames() {
        return frames;
    }


    public GroundStations getGroundBasedStations() {
        return groundStations;
    }


    public SunLight getSunLight() {
        return sunLight;
    }


    public OutputLabel getOutputLabel() {
        return outputLabel;
    }


    public SSCWSLibrary getSscwsLib() {
        return sscwsLib;
    }
    
    
    /**
     * for XML
     */
    public void fireChildrenChanged() {
        getChildren().fireChildrenChanged();
    }


    /**
     * for XML
     *
     * @return FieldlineMapper[]
     */
    public FieldlineMapper[] getFieldlineMappers() {
        // search for FieldlineMapper objects in children
        Vector vect = new Vector();
        Enumeration e = getChildren().elements();
        while (e.hasMoreElements()) {
            Object obj = e.nextElement();
            if (obj instanceof FieldlineMapper) {
                vect.addElement(obj);
            }
        }
        FieldlineMapper[] res = new FieldlineMapper[vect.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = (FieldlineMapper) vect.elementAt(i);
        }
        return res;
    }


    /**
     * for XML
     *
     * @param mappers
     */
    public void setFieldlineMappers(FieldlineMapper[] mappers) {
        // remove all FieldlineMappers
        Enumeration e = getChildren().elements();
        while (e.hasMoreElements()) {
            Object obj = e.nextElement();
            if (obj instanceof FieldlineMapper) {
                ((FieldlineMapper) obj).dispose();
                getChildren().removeChild((FieldlineMapper) obj);
            }
        }
        for (int i = 0; i < mappers.length; i++) {
            Log.log("\n\n\n\n Adding Mapper. Visible=" + mappers[i].isVisible());
            Log.log("\n\n\n\n");
            getChildren().addChild(mappers[i]);
        }
    }
}
