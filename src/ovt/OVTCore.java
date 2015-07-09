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
 
OVT Team (http://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
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
 * This is the main class of the OVT. 
 * @author Mykola Khotyaintsev
 * @version %I% %E%
 * @see ...
 */

public final class OVTCore extends OVTObject implements
GUIPropertyEditorListener {
    
    public static final String VERSION = "3.0";
    public static final String RELEASE_DAY = "March 2015";
    public static final int BUILD = 4;
    public static final String globalSettingsFileName = "ovt.conf";
    public static final String DEFAULT_SETTINGS_FILE = "DefaultSettingsFile";
    public static final Properties globalProperties = new Properties();
    public static int DEBUG = 0;
    
    /**
     * Select what to use as a data source for the functionality/code that
     * handles SSC Web Services satellites.
    */
    public final static SSCWSLibrary SSCWS_LIBRARY = SSCWSLibraryImpl.DEFAULT_INSTANCE;   // The real data source.
    //public final static SSCWSLibrary SSCWS_LIBRARY = SSCWSLibraryTestEmulator.DEFAULT_INSTANCE;  // Data source emulator for testing.

    
    private vtkRenderer renderer = null;
    /**
     * @see #setRenderAction(Renderable)
     */
    private RenPanel renPanel = null;
    private TimeSettings timeSettings;
    
    /** @see #getProperties()
     */
    // protected Properties properties = new Properties();
    
    private final XYZWindow XYZwin;
    //protected int coordSystem = Const.GSM; // XYZ -  GSM, GSE, GEI, ..
    
    private static boolean guiPresent = false;
    
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
    private GroundStations groundStations;    // ground based stations
    private ElectPot electPot;
    private OutputLabel outputLabel;
    
    protected boolean isInitialized = false;
    
    public static String ovtUserDir;
    
    public static final String ovtHomePage ="http://ovt.irfu.se/";
    
    /** Holds value of property server. */
    private static boolean server = false;
    
    /** Holds value of property backgroundColor. */
    private Color backgroundColor = Color.white;
    
    /** This constructor is used for offscreen Ren Panel
     * We don't pass OffscreenRenPanel class to make
     * java compiler forget about the Servlet part of OVT
     * and compile OVT Core without complaining with missing
     * classes.
   * @param renPanel
     */
    public OVTCore(RenPanel renPanel) {
        this.XYZwin = null;
        this.renPanel = renPanel;
        this.renderer = renPanel.getRenderer();
        // use renPanel.Render() instead of renderer.Render().
               
        setServer(true);
        Initialize();
        isInitialized = true;
        guiPresent = false;
    }
    
    public OVTCore(XYZWindow xyzwin) {
        this.XYZwin = xyzwin;
        this.renPanel = xyzwin.getVisualizationPanel();
        this.renderer = renPanel.getRenderer();
        
        Initialize();
        
        isInitialized = true;
        guiPresent = true;
    }
    
    public static String getUserDir(){
        return ovtUserDir;
    }
    
    public static String getDocsDir(){
        return "docs" + File.separator;
    }
    
    public static String getImagesDir(){
        return "images" + File.separator;
    }
    
    public static String getUserdataDir(){
        return "userdata" + File.separator;
    }
    
    public static String getMdataDir(){
        return "mdata" + File.separator;
    }
    
    public static final String getOrbitDataDir(){
        return "odata" + File.separator;
    }
    
    public static String getConfDir(){
        return "conf" + File.separator;
    }
    
    public RenPanel getRenPanel() { return renPanel;  }
    
    public vtkRenderer getRenderer()  {
        return renderer;
    }
    
    /** Uses <CODE>getRenderer.Render()</CODE> unless renderAction is specified.
     *@see #setRenderAction(Rendererable)
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
    
    /** Load properties from {@link #ovtPropertiesFile }
     */
    private static synchronized void loadGlobalSettings() throws IOException {
        File confFile = Utils.findFile(getConfDir() + globalSettingsFileName);     // NOTE: Will not throw Exception if file does not exist.
        
        if (confFile != null) {
            
            // NOTE: new FileInputStream(confFile)) will throw NullPointerException (not IOException) if confFile == null.
            try (FileInputStream in = new FileInputStream(confFile)) {
                if (confFile != null) {
                    globalProperties.load(in);
                }
            }
        }
    }

    public synchronized void saveSettings() throws IOException {
        groundStations.save();
    }

    public static synchronized void saveGlobalSettings() throws IOException {
        /* NOTE: Utils.findFile will return null if it can NOT locate an already
        existing file, i.e. it will NOT suggest a path for where to create a new
        config file if none already exists. Therefore, then, if no old config file
        exists, no new one will be created. */
        //File confFile = Utils.findFile(getConfDir() + globalSettingsFileName);  
        /*if (confFile == null) {
            throw new IOException("Can not find a global settings file to overwrite. ");
        }*/
        
        /* Try saving to user directory, otherwise do not save at all. */
        File confFile = new File(OVTCore.getUserDir() + getConfDir() + globalSettingsFileName);

        try (FileOutputStream out = new FileOutputStream(confFile)) {
            globalProperties.save(out, "OVT properties file.");
        }
    }

    public static String getGlobalSetting(String key) {
        return globalProperties.getProperty(key);
    }
    
    public static String getGlobalSetting(String key, String defaultValue) {
        return globalProperties.getProperty(key, defaultValue);
    }
    
    public static synchronized void setGlobalSetting(String key, String value) {
        globalProperties.put(key, value);
    }
    
    
    public void Initialize() {
        Log.setDebugLevel(2);
        Log.log("Initializing...", 3);
        setName("OVT");
	try {
            setIcon(new ImageIcon(Utils.findResource("images/ovt.gif")));
	} catch (FileNotFoundException e2) { e2.printStackTrace(System.err); }
        
        String osName;
        osName = System.getProperty("os.name").toLowerCase();
        boolean isMacOs = osName.startsWith("mac os x");
        if (isMacOs) 
        {
          ovtUserDir = System.getProperty("user.home") + File.separator + 
                  "Library" + File.separator + "ovt" + File.separator + 
                  VERSION + File.separator;
        } else {
          ovtUserDir = System.getProperty("user.home") + File.separator + 
                  ".ovt" + File.separator + VERSION + File.separator;
        }
        File userDir = new File(ovtUserDir);
        if (!userDir.exists()) {
          if (userDir.mkdirs()) {
            Log.log("Created:" + ovtUserDir,3);
          } else {
            Log.log("Failed to create:" + ovtUserDir,3);
          }
        }
        
        File userConfDir = new File(ovtUserDir + getConfDir());   // Must create this directory in order to be able to save ovt.conf there.
        if (!userConfDir.exists()) {
          if (userConfDir.mkdirs()) {
            Log.log("Created:" + userConfDir.getAbsolutePath(),3);
          } else {
            Log.log("Failed to create:" + userConfDir.getAbsolutePath(),3);
          }
        }

        
        
        /* Load global settings
           NOTE: This code indirectly uses ovtUserDir which therefore has to
           have been previously initialized. */
        if (globalProperties.size() == 0) {
            try {
                loadGlobalSettings();
            } catch (IOException e) {
                sendErrorMessage("Error Loading Global Settings", e);
            }
        }
        
        Log.log("Creating MagProps ...", 3);
        magProps = new MagProps(this);
        Log.log("MagProps created.", 3);
        transCollection = new TransCollection(magProps.getIgrfModel());
        Log.log("TransCollection created.", 3);
        // set time
        timeSettings = new TimeSettings(this);
        Log.log("TimeSettings created.", 3);
        // set coordinate system
        coordinateSystem = new CoordinateSystem(this);
        Log.log("CoordinateSystems created.", 3);
        // add sun light
        sunLight = new SunLight(this);
        Log.log("SunLight created.", 3);
        // set frames
        Log.log("Creating axes ...", 3);
        axes = new Axes(this);
        // set Earth
        Log.log("Creating Earth ...", 3);
        earth = new Earth(this);
        
        // Set frames
        Log.log("Creating Frames ...", 3);
        frames = new Frames(this);
        
        // Set frames
        Log.log("Creating Ground-Based-Stations ...", 3);
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
        
        
        //magProps.addMagPropsChangeListener(bowShock);
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
        
        // notify camera about sat's change
        // will be removed in future (when OVT will be build on start from XML)
        getSats().fireSatsChanged(); // or .getChildren().fireChildrenChanged() doesn't matter
        getGroundBasedStations().getChildren().fireChildrenChanged();
	
	// load time settings
	try{
          double startMjd = Double.parseDouble(OVTCore.getGlobalSetting("startMjd"));	  
	  double intervalMjd = Double.parseDouble(OVTCore.getGlobalSetting("intervalMjd"));
	  double stepMjd = Double.parseDouble(OVTCore.getGlobalSetting("stepMjd"));
	  double currentMjd = Double.parseDouble(OVTCore.getGlobalSetting("currentMjd"));
	  timeSettings.setTimeSet(new TimeSet(startMjd, intervalMjd, stepMjd, currentMjd));
        } catch(Exception ignore){}
        
    }
    
    public TimeSettings getTimeSettings()
    { return timeSettings; }
    
    public double getMjd() {
        return getTimeSettings().getCurrentMjd(); //getTimeSet().
    }
    
    /** Returns current C
   * @return current CS*/
    public int getCS() {
        return getCoordinateSystem().getCoordinateSystem();
    }
    
    /** Returns current C
   * @return polar CS*/
    public int getPolarCS() {
        return getCoordinateSystem().getPolarCoordinateSystem();
    }
    
    
    /** Returns true if everything is initialized. Is used for GUI to check if it is necesarry to plot smthng
   * @return true if everything is initialized*/
    public boolean isInitialized()
    { return isInitialized; }
    
    
    public static boolean isGuiPresent() {
        return guiPresent;
    }

  /**
   * this method is a part of GUIPropertyEditorListener
   * this listener is added to all editors, to render after
   * user changes some of the parameters
   * 
   * @param evt
   */
    @Override
      public void editingFinished(GUIPropertyEditorEvent evt) {
        Render();
    }
    

    
    /** Method to be used when iforming user about the exception occured.
     *When in GUI produces a popup window with <I>Error</I>
     *as window title and <CODE>e.getMessage()</CODE> as message.
     * @param e Exception
     */
    public void sendErrorMessage(Exception e) {
        sendErrorMessage("Error", e);
    }
    /** Method to be used when informing user about the error occured
     *When in GUI produces a popup window with <CODE>msghead</CODE>
     *as window title and <CODE>msg</CODE> as message.
   * @param title Message title
   * @param e Exceprion
     */
    public void sendErrorMessage(String title, Exception e) {
        if (isGuiPresent() == true){
            new ErrorMessageWindow(XYZwin, e).setVisible(true);
        } else {
            Log.err(title + ":" + e);
        }
    }
    
        public void sendErrorMessage(String title, String msg) {
        if (isGuiPresent() == true){
            javax.swing.JOptionPane.showMessageDialog(null, msg, title,
            javax.swing.JOptionPane.ERROR_MESSAGE);
        } else {
            Log.err(title + ":" + msg);
        }
    }

    /** Method to be used when informing user about the warning
     *When in GUI produces a popup window with <CODE>msghead</CODE>
     *as window title and <CODE>msg</CODE> as message.
     * @param title message title
     * @param msg Warning message
     */
    public void sendWarningMessage(String title, String msg) {
        if (isGuiPresent() == true){
            javax.swing.JOptionPane.showMessageDialog(null, title + ": " + msg, title,
            javax.swing.JOptionPane.WARNING_MESSAGE);
        } else {
            System.out.println(title + ": " + msg);
        }
    }
    
    public void sendWarningMessage(String title, Exception e) {
        sendWarningMessage(title, e.getMessage());
    }
    
    public static void sendMessage(String msghead, String msg) {
        if (isGuiPresent() == true){
            javax.swing.JOptionPane.showMessageDialog(null, msg, msghead,
            javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } else {
            System.out.println(msghead + ":" + msg);
        }
    }
    
    public static void setStatus(String statusMessage) {
        if (isGuiPresent() == true){
            XYZWindow.setStatus(statusMessage);
        } else {
            System.out.println("status : " + statusMessage);
        }
    }
    // for JNI methods
    static {
        System.loadLibrary("ovt-"+VERSION);
        System.loadLibrary("jawt");
    }
    
    /** Detect OS type
    * @return  true if OS=windows*/
    public static boolean isUnderWindows() {
        String osName = System.getProperty("os.name");
        return osName.toLowerCase().contains("win");
    }
    
/** Getter for property backgroundColor.
 * @return Value of property backgroundColor.
 */
    public Color getBackgroundColor() {
        return backgroundColor;
    }
    
/** Setter for property backgroundColor.
 * @param backgroundColor New value of property backgroundColor.
 */
    public void setBackgroundColor(Color backgroundColor) {
        Color oldBackgroundColor = this.backgroundColor;
        this.backgroundColor = backgroundColor;
        float[] rgb = Utils.getRGB(backgroundColor);
        renderer.SetBackground(rgb[0], rgb[1], rgb[2]);
        propertyChangeSupport.firePropertyChange ("backgroundColor", oldBackgroundColor, backgroundColor);
    }  
    
    
    public void hideAllVisibleObjects() {
        Enumeration e = getChildren().elements();
        while (e.hasMoreElements()) {
            Object obj = e.nextElement();
            if (obj instanceof VisualObject) ((VisualObject)obj).setVisible(false);
        }
        e = sats.getChildren().elements();
        while (e.hasMoreElements()) {
            Object obj = e.nextElement();
            if (obj instanceof VisualObject) ((VisualObject)obj).setVisible(false);
        }
    }
    
    /** Returns true if OVT is runed in server mode
     * @return Value of property server.
     */
    public static boolean isServer() {
        return server;
    }
    /** Setter for property server.
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
        if (descr == null) throw new IllegalArgumentException(propertyHolder + " has no descriptors.");
        BasicPropertyDescriptor pd = descr.getDescriptor(propertyName);
        if (pd == null) throw new IllegalArgumentException(" Object '" + objectPath + "' has no property '" + propertyName + "'");
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
    
    
    public XYZWindow getXYZWin() { return XYZwin; }
    public CoordinateSystem getCoordinateSystem() { return coordinateSystem; }
    public MagProps getMagProps() { return magProps; }
    public Camera getCamera() { return camera; }
    public BowShock getBowShock() { return bowShock; }
    public MagTangent getMagTangent() { return magTangent; }
    public Magnetopause getMagnetopause() { return magnetopause; }
    public Magnetosphere getMagnetosphere() { return magnetosphere; }
    public ElectPot getElectPot() { return electPot; }
    public Sats getSats()  { return sats; }
    public Earth getEarth() { return earth; }
    public Axes getAxes() { return axes; }
    public Frames getFrames() { return frames; }
    public GroundStations getGroundBasedStations() { return groundStations; }
    public SunLight getSunLight() { return sunLight; }
    public OutputLabel getOutputLabel() {  return outputLabel;  }
    
    /** for XML */
    public void fireChildrenChanged() {
        getChildren().fireChildrenChanged();
    }
    
    /** for XML
   * @return FieldlineMapper[] */
    public FieldlineMapper[] getFieldlineMappers() {
        // search for FieldlineMapper objects in children
        Vector vect = new Vector();
        Enumeration e = getChildren().elements();
        while (e.hasMoreElements()) {
            Object obj = e.nextElement();
            if (obj instanceof FieldlineMapper) vect.addElement(obj);
        }
        FieldlineMapper[] res = new FieldlineMapper[vect.size()];
        for (int i=0; i<res.length; i++) res[i] = (FieldlineMapper)vect.elementAt(i);
        return res;
    }
    
    /** for XML
   * @param mappers */
    public void setFieldlineMappers(FieldlineMapper[] mappers) {
        // remove all FieldlineMappers
        Enumeration e = getChildren().elements();
        while (e.hasMoreElements()) {
            Object obj = e.nextElement();
            if (obj instanceof FieldlineMapper) {
                ((FieldlineMapper)obj).dispose();
                getChildren().removeChild((FieldlineMapper)obj);
            }
        }
        for (int i=0; i<mappers.length; i++) {
            Log.log("\n\n\n\n Adding Mapper. Visible="+mappers[i].isVisible());
            Log.log("\n\n\n\n");
            getChildren().addChild(mappers[i]);
        }
    }
}
