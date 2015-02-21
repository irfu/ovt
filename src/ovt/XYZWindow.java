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

//import ovt.*;
import ovt.gui.*;
import ovt.util.*;
import ovt.interfaces.*;

import vtk.*;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;



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
  protected VisualizationPanel renPanel;
  protected vtkRenderer ren;
  // GUI
  public static StatusLine statusLine = new StatusLine();
  protected TreePanel treePanel;
  public boolean windowResizable = true;
  protected XYZMenuBar menuBar;
  protected JSplitPane splitPane;
  
  private final ToolBarContainer toolBarContainer;
  
  protected HTMLBrowser htmlBrowser;
  
  public static final String SETTING_VISUALIZATION_PANEL_WIDTH = "VisualizationPanel.width";
  public static final String SETTING_VISUALIZATION_PANEL_HEIGHT = "VisualizationPanel.height";
  private static final String SETTING_TREE_PANEL_WIDTH = "TreePanel.width";
  private static final String SETTING_XYZWINDOW_WIDTH  = "XYZWindow.width";
  private static final String SETTING_XYZWINDOW_HEIGHT = "XYZWindow.height";
  
  public XYZWindow() {
    super("Orbit Visualization Tool " + OVTCore.VERSION + " (Build "+OVTCore.BUILD+")" );
    try {
      setIconImage (Toolkit.getDefaultToolkit().getImage(OVTCore.class.getClassLoader().getResource("images/ovt.gif")));
    } catch (NullPointerException npe) { Log.err("FileNotFound: images/ovt.gif"); }

    // avoid crush on some win 95 computers
    if (System.getProperty("os.name").equalsIgnoreCase("Windows 98")) windowResizable = false;

    // -----------   set window size ----------
    boolean pack = false;
    try{
      setSize(Integer.parseInt(OVTCore.getGlobalSetting(SETTING_XYZWINDOW_WIDTH)),
        Integer.parseInt(OVTCore.getGlobalSetting(SETTING_XYZWINDOW_HEIGHT))
      );
    } catch(NumberFormatException e2){ 
        pack = true;
    }
    
    // show splashscreen
    splashWindow = new SplashWindow();
    splashWindow.setVisible(true);

//------- create the vtkPanel ----------
    addNotify();
    renPanel = new VisualizationPanel(this);
    addOriginActor();

//------- create the OVTCore ----------
    //core = new OVTCore(renPanel.getRenderWindow(), renPanel.getRenderer());
    core = new OVTCore(this);

    int width = 600;
    int height = 600;
    try{
      width = Integer.parseInt(OVTCore.getGlobalSetting(SETTING_VISUALIZATION_PANEL_WIDTH));
      height = Integer.parseInt(OVTCore.getGlobalSetting(SETTING_VISUALIZATION_PANEL_HEIGHT));
    } catch(NumberFormatException ignore){  }
    
    renPanel.setSize(width, height);
    //renPanel.setMinimumSize(new Dimension(0,0));
    //System.out.println("renPanel.minsize="+renPanel.getMinimumSize());
    //System.out.println("renPanel.preffsize="+renPanel.getPreferredSize());
    //System.out.println("renPanel.maxsize="+renPanel.getMaximumSize());
    
    //renPanel.re
    // set the renderer
    ren = renPanel.getRenderer();
    float[] rgb = ovt.util.Utils.getRGB(core.getBackgroundColor());
    ren.SetBackground(rgb[0], rgb[1], rgb[2]);
    


    JPopupMenu.setDefaultLightWeightPopupEnabled(false);

    menuBar = new XYZMenuBar(getCore(), this);
    setJMenuBar(menuBar);

// ------- Set ContentPane Layout
    Container contentPane = getContentPane();
    
    contentPane.setLayout(new BorderLayout());
    //contentPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    //contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
  
//-------- create the tree panel -----
    treePanel = new TreePanel(getCore());
    int treePanelWidth = treePanel.getPreferredSize().width;
    try{
      treePanelWidth = Integer.parseInt(OVTCore.getGlobalSetting(SETTING_TREE_PANEL_WIDTH));
    } catch(NumberFormatException ignore){  }

    if (treePanelWidth != 0) treePanel.setPreferredSize(new Dimension(treePanelWidth, height));
    treePanel.setMinimumSize(new Dimension(160, 10));
    //System.out.println("treePanel.minsize="+treePanel.getMinimumSize());
    //System.out.println("treePanel.preffsize="+treePanel.getPreferredSize());
    //System.out.println("treePanel.maxsize="+treePanel.getMaximumSize());
  
//--------Create a split pane with the two scroll panes in it
    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,windowResizable);
    splitPane.setLeftComponent(treePanel);
    splitPane.setRightComponent(renPanel);
    splitPane.setOneTouchExpandable(windowResizable);
    splitPane.setDividerSize(6);

    if (treePanelWidth == 0) {
        //splitPane.setDividerLocation(0);
        renPanel.setSize(width - treePanel.getPreferredSize().width, height);
    }
    
    contentPane.add(splitPane, BorderLayout.CENTER);
    
    //System.out.println("splitPane.minsize="+splitPane.getMinimumSize());
    //System.out.println("splitPane.preffsize="+splitPane.getPreferredSize());
    //System.out.println("splitPane.maxsize="+splitPane.getMaximumSize());
    
// ------------- add toolbars -----------  
    toolBarContainer = new ToolBarContainer(core, this);
    
    // sets width and computes and sets height for this width
    toolBarContainer.setPreferredWidth(splitPane.getPreferredSize().width);
    
    //toolBarContainer.updatePreferredSize();
    contentPane.add(toolBarContainer, BorderLayout.SOUTH); 
    
    // add Status Line
    //contentPane.add(statusLine, BorderLayout.NORTH);
    
    //  enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        // create Help Window
    htmlBrowser = new HTMLBrowser(core);
    
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
          quit();
      }
    });
    

    if (pack) pack(); // pack if no settings are present
    
    if (!windowResizable) setResizable(windowResizable);
}

  public void start() {
    //refreshGUI();
    
    Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension windowSize = getSize();
    /*setLocation(scrnSize.width/2 - windowSize.width/2, 
                 scrnSize.height/2 - windowSize.height/2);*/
    setLocation(scrnSize.width/2 - windowSize.width/2, scrnSize.height/2 - windowSize.height/2);
    splashWindow.dispose();
    
    getTreePanel().expandClusterNode();
    
    setVisible(true);
    //polarWindow.setVisible(true);
    //getCore().start(); 
    
  }
  
  /**
   *
   * @return OVTCore instance
   */
    @Override
  public final OVTCore getCore()
  { return core; }

    @Override
  public void actionPerformed(ActionEvent e) {
  }


  public void Render() { 
      renPanel.Render(); 
  }

  public vtkRenderer getRenderer() { 
    return renPanel.getRenderer(); 
  }
   
  public vtkRenderWindow getRenderWindow() { 
    return renPanel.getRenderWindow(); 
  }


  /** Is executed when the window closes */
public void quit() {
    
  try {
    getCore().saveSettings();
  } catch (IOException e2) {
    getCore().sendErrorMessage("Error Saving Settings", e2);
  }
  // save VisualizationPanel's size
  Dimension d = renPanel.getSize();
  if (isResizable()) {
    OVTCore.setGlobalSetting(SETTING_VISUALIZATION_PANEL_WIDTH, ""+d.width);
    OVTCore.setGlobalSetting(SETTING_VISUALIZATION_PANEL_HEIGHT, ""+d.height);
  }
  OVTCore.setGlobalSetting(SETTING_TREE_PANEL_WIDTH, ""+treePanel.getWidth());
  OVTCore.setGlobalSetting(SETTING_XYZWINDOW_WIDTH, ""+getWidth());
  OVTCore.setGlobalSetting(SETTING_XYZWINDOW_HEIGHT, ""+getHeight());
  
  OVTCore.setGlobalSetting("startMjd", ""+getCore().getTimeSettings().getTimeSet().getStartMjd());
  OVTCore.setGlobalSetting("intervalMjd", ""+getCore().getTimeSettings().getTimeSet().getIntervalMjd());
  OVTCore.setGlobalSetting("stepMjd", ""+getCore().getTimeSettings().getTimeSet().getStepMjd());
  OVTCore.setGlobalSetting("currentMjd", ""+getCore().getTimeSettings().getTimeSet().getCurrentMjd());
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
 * Returns <code>true</code> if user pressed <code>Yes</code> 
 * else <code>false</code>.
 * @param none
 * @return see upp!
 * @see javax.swing.JOptionPane#showOptionDialog
 */

  private boolean quitConfirmed() {
        Object[] options = {"Yes", "No"};
        int n = JOptionPane.showOptionDialog(this,"Do you really want to exit?", "Exit", JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,"No");
      return n == JOptionPane.YES_OPTION;
  }

  /** 
   * Main method. Here we launch OVT
   * @param arg
   */
  public static void main(String[] arg) {
    XYZWindow XYZwin = new XYZWindow();
    XYZwin.start();
  }

  public HTMLBrowser getHTMLBrowser() {
    return htmlBrowser;
  }

  protected final void addOriginActor() {
    vtkVectorText atext = new vtkVectorText();
        atext.SetText(". (0, 0, 0)");
    vtkPolyDataMapper mapper = new vtkPolyDataMapper();
        mapper.SetInputData(atext.GetOutput());
    vtkFollower actor = new vtkFollower();
        actor.SetMapper(mapper);
        actor.SetScale(0.02);
        actor.AddPosition(0, 0, 0);
        actor.SetCamera(getRenderer().GetActiveCamera());
        actor.GetProperty().SetColor(0, 0, 0);
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

  public VisualizationPanel getVisualizationPanel() {
    return renPanel;
  }
  
  public XYZMenuBar getXYZMenuBar() {
    return menuBar;
  }
  
  public TreePanel getTreePanel() {
        return treePanel;
  }
  
}

class SplashWindow extends JWindow {
    JLabel imageLabel;
    public SplashWindow() {
        super();
        java.net.URL url = OVTCore.class.getClassLoader().getResource("images/splash.gif");
        if (url == null) { Log.err("FileNotFound: images/splash.gif");return;}
        
        imageLabel = new JLabel(new ImageIcon(url));
            imageLabel.setBorder(BorderFactory.createRaisedBevelBorder());
            Dimension labelSize = imageLabel.getPreferredSize();
            imageLabel.setBounds(0, 0, labelSize.width, labelSize.height);
        
        JLabel label = new JLabel("Version " + OVTCore.VERSION + ", " + OVTCore.RELEASE_DAY );
            label.setFont(new Font("Arial", Font.PLAIN, 12));
            label.setForeground(Color.yellow);
            labelSize = label.getPreferredSize();
            label.setBounds(imageLabel.getPreferredSize().width-labelSize.width-80, 255, labelSize.width, labelSize.height);
            
        JLabel copyrightLabel = new JLabel("Copyright (c) OVT Team, 2000-2009");
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
        //getContentPane().add(imageLabel, BorderLayout.CENTER);
        //getContentPane().add(new JLabel("Thank you, oh MAN!"), BorderLayout.NORTH);
        pack();
        
        // center splash window
        Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(scrnSize.width/2 - windowSize.width/2,
                 scrnSize.height/2 - windowSize.height/2);
    }
    
    
/*
    public void paint(Graphics g) {
        imageLabel.paint(g);
        label.paint(g);
    }*/
    
}
