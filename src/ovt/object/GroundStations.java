/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/GroundStations.java,v $
  Date:      $Date: 2003/09/28 17:52:48 $
  Version:   $Revision: 2.9 $


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

package ovt.object;

import ovt.*;
import ovt.gui.Style;
import ovt.mag.*;
import ovt.util.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import vtk.*;

import java.io.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;

import javax.xml.parsers.*;  
import org.xml.sax.*;  
import com.sun.xml.tree.*;

import org.w3c.dom.*;


/**
 *
 * @author  oleg
 * @version
 */
public class GroundStations extends VisualObject implements MenuItemsSource {

    /** if <CODE>true</CODE> it is not possible to rename, remove*/
    private boolean isRootNode = false;
    private static final String xml_file = OVTCore.getUserdataDir()+"gb_stations.xml";
    
    public GroundStations(OVTCore core) {
        super(core, "Ground based stations", "images/gb_stations.gif", true); // VisualObject constructor
                    // ^-> to OVTObject::setName();
        setParent(core);
        isRootNode = true;
        try {
            load();
        }
        catch (IOException e) {
            getCore().sendErrorMessage("Error loading ground stations config", e);
        }
        
    }
    
    /** 
     */
    public GroundStations(GroundStations groundStations) {
        super(groundStations.getCore(), "Ground based stations", "images/gb_stations.gif", true); // VisualObject constructor
        setParent(groundStations);
    }
    
    public GroundStations(OVTCore core, String name) {
        super(core, name, "images/gb_stations.gif", true); // VisualObject constructor
        setParent(core);
    }

    
/** Source of menu items available in this object
 * @return array of <CODE>JMenuItem</CODE>
 */    
    public JMenuItem[] getMenuItems() {
        JMenuItem item = new JMenuItem("Add Station...");
        item.setFont(Style.getMenuFont());
        item.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                GroundStation gs = new GroundStation(GroundStations.this, "New Station");
                addChild(gs);
                gs.addPropertyChangeListener("name", getCore().getCamera().getViewToObjectsNameChangeListener());  // add camera to listeners of new ground station name
                gs.addPropertyChangeListener("visible", getCore().getCamera().getViewToObjectsVisibilityChangeListener());  // add camera to listeners of new ground station visibility
                gs.setCustomizerVisible(true);
            }
        });
        JMenuItem item2 = new JMenuItem("Add Group...");
        item2.setFont(Style.getMenuFont());
        item2.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                GroundStations gs = new GroundStations(getCore(), "New Group");
                addChild(gs);
                //gs.setCustomizerVisible(true);
            }
        });
        
        if (isRootNode) return new JMenuItem[] {item, item2};
        // if not - continue
        JMenuItem item3 = new JMenuItem("Remove");
        item3.setFont(Style.getMenuFont());
        item3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {       
                if (isVisible()) setVisible(false);
                /*if (customizer != null) {
                    customizer.dispose();
                    customizer = null;
                }*/
                removeSelf();
            }
        });
        return new JMenuItem[] {item, item2, item3};
    }

    public void removeSelf() { // by oleg
        //DBG*    System.out.println("OVTObject::removeSelf()");
        OVTObject parent = this.getParent();
        if (parent == null) {
            System.err.println("OVTObject::removeSelf(): can't remove node (parent == null).");
            return;
        }
        dispose();
        parent.getChildren().remove(this);
        parent.getChildren().fireChildRemoved(this);
    }
    
    /** Saves groung stations properties to config
     * @throws IOException
     */    
    public void save() throws IOException {
        GroundbasedStationsDOM.save(this, xml_file);
    }

    /** Loads groung stations properties from config
     * @throws IOException
     */    
   public void load() throws IOException {
        GroundbasedStationsDOM.load(xml_file, this);
   }
/*
// plain text file reader
 public void load() throws IOException {
        BufferedReader f = new BufferedReader(
                new FileReader(getCore().getConfDir() + "gstats.conf"));
        int typ = GroundStation.UNKNOWN; // unknown
        while (f.ready()) {
            String newline = f.readLine();
            System.out.println(newline);
            if (newline == null) break;
            StringTokenizer st = new StringTokenizer(newline, " ");
            if (newline.equalsIgnoreCase("Radars")) {
                typ = GroundStation.RADAR;
                continue;
            } else if (newline.equalsIgnoreCase("Magnetometers")) {
                typ = GroundStation.MAGNETOMETER;
                continue;
            }
            
            try {
                String name = st.nextToken();
                double lat  = new Double(st.nextToken()).doubleValue();
                double lon  = new Double(st.nextToken()).doubleValue();
                GroundStation gs = new GroundStation(this, name);
                gs.setType(typ);
                gs.setLatitude(lat);
                gs.setLongitude(lon);
                addChild(gs);
            }
            catch(Exception e)
            {
                f.close();
                return;
            }
        }
        f.close();
    }*/
    
/*   
   
    public void timeChanged(TimeEvent evt) {
        Enumeration e = getChildren().elements();
        while (e.hasMoreElements()) {
            try {
                ((TimeChangeListener)(e.nextElement())).timeChanged(evt);
            } catch (ClassCastException e2) {
                System.out.println("this ground station doesn't care about time..");
            }
        }
    }
    
    public void coordinateSystemChanged(CoordinateSystemEvent evt) {
        Enumeration e = getChildren().elements();
        while (e.hasMoreElements()) {
            try {
                ((CoordinateSystemChangeListener)(e.nextElement())).coordinateSystemChanged(evt);
            } catch (ClassCastException e2) {
                System.out.println("this ground station doesn't care about cs..");
            }
        }
    } */
    
    public Descriptors getDescriptors() {
        
        if (descriptors == null) {
            try {
                TextFieldEditor editor;
                descriptors = super.getDescriptors();
                
                if (isRootNode) return descriptors; // do not have any descriptors!

        /* add property descriptor for name*/
                BasicPropertyDescriptor pd = new BasicPropertyDescriptor("name", this);
                pd.setLabel("Rename");
                pd.setDisplayName("Groundbased Stations Group Name");
                editor = new TextFieldEditor(pd);
                //editor.setEditCompleteOnKey(true);
                addPropertyChangeListener("name", editor);
                pd.setPropertyEditor(new WindowedPropertyEditor(editor, getCore().getXYZWin(), "OK", true));
                descriptors.put(pd);
                
            } catch (IntrospectionException e2) {
                System.out.println(getClass().getName() + " -> " + e2.toString());
                System.exit(0);
            }
        }
        return descriptors;
    }
    
    
    
    /** for XML */
    public Object[] getGroundStations() {
        return getChildren().toArray();
    }
    
    /** for XML */
    public void setGroundStations(Object[] stations) {
        // remove all children
        Enumeration e = getChildren().elements();
        while (e.hasMoreElements()) {
            OVTObject obj = (OVTObject)e.nextElement();
            obj.dispose();
            getChildren().removeChild(obj);
        }
        getChildren().setSize(stations.length);
        for (int i=0; i<stations.length; i++) getChildren().setChildAt(i,(OVTObject)stations[i]);
    }
}


/** 
 *
 *                  class GroundbasedStationsDOM
 *
 */



class GroundbasedStationsDOM {

    private static final String GROUND_BASED_STATION = "GroundBasedStation";
    private static final String GROUND_BASED_STATIONS = "GroundBasedStations";
    private static final int DEBUG = 10;
    
    private static XmlDocument buildDom (GroundStations stations) throws ParserConfigurationException {
        DocumentBuilderFactory factory = new com.sun.xml.parser.DocumentBuilderFactoryImpl();
        DocumentBuilder builder = factory.newDocumentBuilder();
        XmlDocument document = (XmlDocument)builder.newDocument();  // Create from whole cloth

        Element root = (Element)getNode( stations, document); 
        document.appendChild (root);
        return document;
}
    
private static Node getNode(OVTObject obj, Document document) {
    String nodeName = null;
    if (obj instanceof GroundStations) nodeName = GROUND_BASED_STATIONS;
    else if (obj instanceof GroundStation) nodeName = GROUND_BASED_STATION;
    
    Log.log("object='"+nodeName+"'", DEBUG);
    Element root = (Element) document.createElement(nodeName);
     
    // set name attribute
    root.setAttribute(Settings.NAME, obj.getName());
        
     // set other attributes
     Descriptors desc = ((DescriptorsSource)obj).getDescriptors();
        if (desc != null) {
            Enumeration e = desc.elements();
            BasicPropertyDescriptor pd;
            String value;
            while (e.hasMoreElements()) {
                pd = (BasicPropertyDescriptor)e.nextElement();
                
                if (pd.isDerived()) continue; // skip derived properties
                
                try {
                    Log.log(pd.getName() + "=" + pd.getPropertyEditor().getAsText() + "', ", DEBUG);
                    value = pd.getPropertyEditor().getAsText();
                    if (value != null) root.setAttribute(pd.getName(), value);
                } catch (NullPointerException e2) { 
                    //Log.err("Property descriptor '"+ pd.getName() +"' has no editor", 0);
                }
            }
        }

        // add obj's children to root node
        Children children = obj.getChildren();
        if (children != null) {
            Enumeration e = children.elements();
            while (e.hasMoreElements()) 
                root.appendChild(getNode((OVTObject)e.nextElement(), document));
        }
    
    return root;
}

    
    public static void save(GroundStations stations, String xml_file) throws IOException {
        FileOutputStream out = new FileOutputStream(xml_file);
        try {
            buildDom(stations).write(out);
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            System.out.println("Parser Error!" + pce);
            //pce.printStackTrace();
        }
        out.close();
    }
    
    
    

public static void load(String xml_file, GroundStations stations) throws IOException {
    DocumentBuilderFactory factory = new com.sun.xml.parser.DocumentBuilderFactoryImpl();
    try {
           DocumentBuilder builder = factory.newDocumentBuilder();
           Document document = builder.parse( new File(xml_file) );
           // Cast to XmlDocument for write() operation
           // (Not defined until DOM Level 3.)
	   //Log.log("document="+document);
           XmlDocument xdoc = (XmlDocument) document;
           set(xdoc.getDocumentElement(), stations); // first child 
           
         } catch (SAXParseException spe) {
           // Error generated by the parser
           System.out.println ("\n** Parsing error" 
              + ", line " + spe.getLineNumber ()
              + ", uri " + spe.getSystemId ());
           System.out.println("   " + spe.getMessage() );

           // Use the contained exception, if any
           Exception  x = spe;
           if (spe.getException() != null)
               x = spe.getException();
           x.printStackTrace();
           throw new IOException("Bad File Format : " + spe.getMessage());
        } catch (SAXException sxe) {
           // Error generated by this application
           // (or a parser-initialization error)
           Exception  x = sxe;
           if (sxe.getException() != null)
               x = sxe.getException();
               x.printStackTrace();
           throw new IOException("Bad File Format : " + sxe.getMessage());
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
            throw new IOException("Bad File Format : " + pce.getMessage());
        }
            
    }    

    
private static void setAttributes(NamedNodeMap map, OVTObject obj) {
    
        Descriptors desc = ((DescriptorsSource)obj).getDescriptors();
        if (desc != null) {
            for (int i=0; i<map.getLength(); i++) {
                Attr attr = (Attr)map.item(i);
                String propertyName = attr.getNodeName();
                String value = attr.getValue();
                
                if (propertyName.equals(Settings.NAME)) continue; //map.removeNamedItem(nodeName);
                Log.log("Setting '"+propertyName+"' to '"+value+"'", DEBUG);
                
                BasicPropertyDescriptor pd = desc.getDescriptor(propertyName);
                if (pd != null) {
                    if (value != null  || value != "null") {
                        try {
                            pd.getPropertyEditor().setAsText(value);
                        } catch (PropertyVetoException e2) { 
                            Log.err("Error setting " + propertyName + " in " + obj.getName() +
                                " : " + e2.getMessage(), 0);
                        }
                    } else 
                    Log.err("Property '" + propertyName + "' doesnt exist in " + obj.getName(), 0);
                }
            }
       }
}

private static void set(Node node, BasicObject obj) {
    
    setAttributes(node.getAttributes(), obj);
    
        NodeList nodeList = node.getChildNodes();
        BasicObject childObj = null; 
        ChildrenSource mama = obj;
        for (int i=0; i<nodeList.getLength(); i++) {
            try {
                Element childNode = (Element)nodeList.item(i);
            
                String nodeName = childNode.getNodeName(); 
                    Log.log("Node["+i+"] : '"+nodeName+"'", DEBUG);
                    if (nodeName.equals("#text")) {
                        continue;
                    }
                if (nodeName.equals(GROUND_BASED_STATIONS)) 
                        childObj = new GroundStations(obj.getCore(), getAttribute(Settings.NAME, childNode));
                else if (nodeName.equals(GROUND_BASED_STATION)) 
                        childObj = new GroundStation((GroundStations)obj, getAttribute(Settings.NAME, childNode));
                obj.addChild(childObj);
                set(childNode, childObj);
             } catch (ClassCastException ignore) { }
        }
    
}

public static String getAttribute(String attrName, Node node) {
    return node.getAttributes().getNamedItem(attrName).getNodeValue();
}

}
