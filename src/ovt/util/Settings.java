/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/util/Settings.java,v $
Date:      $Date: 2005/12/15 17:25:30 $
Version:   $Revision: 2.10 $


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

/*
 * Settings.java
 *
 * Created on November 17, 2000, 11:35 AM
 */

package ovt.util;

import ovt.*;
import ovt.beans.*;
import ovt.object.*;
import ovt.interfaces.*;
import ovt.datatype.*;
import ovt.gui.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.event.*;

import java.beans.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import javax.swing.*;
import java.lang.*;

import javax.xml.parsers.*;  
import org.xml.sax.*;  
import com.sun.xml.tree.*;

import org.w3c.dom.*;



/**
 *
 * @author  ko
 * @version 
 */
public class Settings {

    public static final String NAME          = "name"; // no need. only GroundBased stations use it.
    public static final String OBJECT        = "OBJECT";
    public static final String AUTO_OBJECT   = "AutoObject";
    public static final String CLASS         = "class";
    public static final String VISIBLE       = "visible";
    public static final String INDEX         = "ElementIndex";
    public static final String ARRAY         = "Array";
    public static final String LENGTH        = "length";
    public static final String ARRAY_ELEMENT = "ArrayElement";
    public static final boolean INDEXED	     = true;
    public static final boolean NOT_INDEXED  = false;
    private static final int DEBUG = 9;
    
    static {
        //PropertyEditorManager.setEditorSearchPath(new String[]{});
        PropertyEditorManager.registerEditor( java.io.File.class, ovt.beans.editor.FileEditor.class );
        PropertyEditorManager.registerEditor( ovt.datatype.Time.class, ovt.beans.editor.TimeEditor.class );
    }
    
    /** Creates new Settings */
    public Settings() {
    }

private static XmlDocument buildDom (OVTObject obj) throws ParserConfigurationException {
    DocumentBuilderFactory factory = new com.sun.xml.parser.DocumentBuilderFactoryImpl();
    DocumentBuilder builder = factory.newDocumentBuilder();
    XmlDocument document = (XmlDocument)builder.newDocument();  // Create from whole cloth

    Element root = (Element)createNode("OVT", obj, null, document, false); 
    document.appendChild (root);
    return document;
}


public static boolean isArray(Class cl) {
    return (cl.getComponentType() != null);
}

/** indexed specifies where should we look - on indexed getter*/
public static boolean isArrayDescriptor(PropertyDescriptor pd, boolean indexed) {
    
    if (indexed) {
        if (!(pd instanceof IndexedPropertyDescriptor)) return false;
        IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor)pd;
        Method indexedWriteMethod = ipd.getIndexedWriteMethod();
        if (indexedWriteMethod  != null && isArray(indexedWriteMethod.getReturnType())) return true;
    } else {
        //Log.log("isArrayDescriptor(pd="+pd.getName()+",indexed="+indexed+")");
        Method writeMethod = pd.getWriteMethod();
        //Log.log("\twriteMethod="+writeMethod);
        //Log.log("\twriteMethod="+writeMethod);
        if (writeMethod != null && isArray(pd.getReadMethod().getReturnType())) return true;
    }
    
    return false; 
}

/** indexed specifies where should we look - on indexed getter*/
public static boolean prefferedIndexType(PropertyDescriptor pd) throws IllegalArgumentException {
    if (pd.getWriteMethod() != null) return NOT_INDEXED;
    
    // pd.getWriteMethod() == null
    
    if (pd instanceof IndexedPropertyDescriptor) 
        if (((IndexedPropertyDescriptor)pd).getIndexedWriteMethod()  != null) return INDEXED;
    
    if (pd.getReadMethod() != null) return NOT_INDEXED;
    
    if (pd instanceof IndexedPropertyDescriptor) 
        if (((IndexedPropertyDescriptor)pd).getIndexedReadMethod()  != null) return INDEXED;
    
    throw new IllegalArgumentException("Confused... Cannot find any method ");
}

public static boolean getArrayDescriptorComponentType(PropertyDescriptor pd) {
    Method writeMethod = pd.getWriteMethod();
    if (writeMethod != null) 
        return isArray(writeMethod.getReturnType());
    else
        return false; 
}


public static boolean isPropertyReadOnly(PropertyDescriptor pd) {
    if (pd instanceof IndexedPropertyDescriptor) {
        IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor)pd;
        return (ipd.getWriteMethod() == null && ipd.getIndexedWriteMethod() == null);
    } else
     return (pd.getWriteMethod() == null);
}


private static Element createArrayNode(String nodeName, Object array, Document document) {
    Log.log("-> createArrayNode("+array.getClass().getComponentType()+"[])", DEBUG);
    Element root = (Element) document.createElement(nodeName);
    int arrayLength = Array.getLength(array);
    root.setAttribute(LENGTH, ""+arrayLength);
    //root.setAttribute(CLASS, array.getClass().getComponentType().getName());
    
    boolean addClassProperty = false;
    for (int i=0; i<arrayLength; i++) {
        Object element = Array.get(array, i);
        // check if arrays's object has the same class as array component type
        // Array can be Object[] and contain ANY class. If so add class=".." property to node.
        boolean addClassAttribute = (!element.getClass().getName().equals(array.getClass().getComponentType().getName()));
        root.appendChild(createNode(ARRAY_ELEMENT, element, null, document, addClassAttribute ));
    }
    return root;
}

private static Element createNode(String nodeName, Object obj, Class propertyEditorClass, Document document, boolean addClassAttribute) {   
    Log.log("\n-----------> createNode(nodeName="+nodeName+", obj="+obj, DEBUG);
        //String objName = Utils.repaceSpaces(obj.getName());
    //Element root = (Element) document.createElement(Utils.replaceSpaces(nodeName));
    
    if (isArray(obj.getClass())) {
        return createArrayNode(nodeName, obj, document);
    }
    
    Element root = (Element) document.createElement(nodeName);
    
    
    // check if this obj can be expressed by text
    PropertyEditor editor = findEditor(propertyEditorClass, obj.getClass());
    
    if (editor != null) {
        Log.log("editor="+editor, DEBUG);
        editor.setValue(obj);
        String valueStr = editor.getAsText();
        root.appendChild(document.createTextNode(valueStr));
        return root;
    }
    
    if (addClassAttribute) root.setAttribute(CLASS, obj.getClass().getName()); 
    
   
    
    // no - it is not possible to express this object by text
    // explore the object !
    
    try {
        Log.log("Introspecting obj.getClass()="+obj.getClass(), DEBUG);
        //BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass()); 
        
        // if this method will throw NullPointerException
        // check the BeanInfo for that class - some PropertyDescriptors may be invalid! 
        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass()); 
        
        // to avoid strrange ordering of p.d. bug
        if (obj instanceof OVTCore) beanInfo = new OVTCoreBeanInfo(); 
        
        
        if (beanInfo == null) {
            Log.err("BeanInfo Not found for "+obj.getClass().getName()+"\n BeanInfoSearchPath:");
            String path[] = Introspector.getBeanInfoSearchPath();
            for (int i=0; i<path.length; i++) Log.err("\t"+path[i]);
            System.exit(-1);
        }
        
        PropertyDescriptor propD[] = beanInfo.getPropertyDescriptors();
        
        for (int i=0; i<propD.length; i++) {
            if (propD[i] == null) {
                Log.err("PropertyDescriptor["+i+"] of "+obj.getClass().getName()+" is NULL!!!"); System.exit(-1);
            }
            if (OVTCore.isServer() && propD[i].isHidden()) continue;
            if (obj instanceof OVTCore) Log.log("Processing property ("+i+") "+root.getNodeName()+" -> "+propD[i].getName(), DEBUG);
            addPropertyToNode(root, propD[i], obj, document);
        }
    } catch (IntrospectionException intr_e) {
        Log.err("BeanInfo class not found for "+obj.getClass().getName());
        intr_e.printStackTrace(System.err);
        System.exit(-1);
    } 
    
    return root; 
}


/** It is supposed that ipd.getPropertyEditorClass() == null !!!!!!!!!! */
private static void addIndexedPropertyToNode(org.w3c.dom.Element root, IndexedPropertyDescriptor ipd, Object bean, Document document) {
    try {
        
        Method indexedReadMethod = ipd.getIndexedReadMethod();
        try {
            for (int i=0; true; i++) {
                Log.log(bean.getClass().getName()+"."+ipd.getName()+"("+i+")",DEBUG);
                Object valueObj = indexedReadMethod.invoke(bean, new Object[]{ new Integer(i) });
                ///Log.log("   value="+valueObj,DEBUG);
                boolean addClassAttribute = (!valueObj.getClass().getName().equals(indexedReadMethod.getReturnType().getName()));
                // ipd.getPropertyEditorClass() or null ??..... here      \/
                Element indexedTag = createNode(ipd.getName(), valueObj, null, document, addClassAttribute);
                indexedTag.setAttribute(INDEX,""+i);
                root.appendChild(indexedTag);
            }
        } catch (InvocationTargetException inv_t_e) {
            Throwable e2 = inv_t_e.getTargetException();
            if (!(e2 instanceof ArrayIndexOutOfBoundsException))
                inv_t_e.printStackTrace(System.err);
        }
        
    } catch (NullPointerException e2) {
        //Log.err("Property descriptor '"+ pd.getName() +"' has no editor", 0);
        Log.err("---------------------SUXX!!!00000000000--------------------"+e2);
    //} catch (InvocationTargetException inv_t_e) { inv_t_e.printStackTrace();
    } catch (IllegalAccessException ill_a_e) { ill_a_e.printStackTrace();
    }

}

//private static void addPropertyToNode(org.w3c.dom.Element root, PropertyDescriptor pd, Object bean, Document document) {
private static void addPropertyToNode(org.w3c.dom.Element root, PropertyDescriptor pd, Object bean, Document document) {
    if (OVTCore.isServer() && pd.isHidden()) return; // hiddern properties are used to hide some props. form server version
    
    // if the property can be expressed as a string - add it as Attribute
    // <nodename .. propertyName="value" .. >
    
    boolean indexed = prefferedIndexType(pd);
    
    if (indexed) {
        addIndexedPropertyToNode(root, (IndexedPropertyDescriptor)pd, bean, document);
        return;
    }
    
    // this is not indexed property
    
    Object propertyValue = getPropertyValue(pd, bean);
    
    // if the propery has editor - it is fantastic ;-) 
    // even array can be written as string: "10,23,45"
    PropertyEditor editor = findEditor(pd);
    if (editor != null) {
        editor.setValue(propertyValue);
        String valueStr = editor.getAsText();
        root.setAttribute(pd.getName(), valueStr);
        return;
    }
    
    Log.log("isArrayDescriptor(pd="+pd.getName()+", NOT_INDEXED)="+isArrayDescriptor(pd, NOT_INDEXED), DEBUG);
    
    if (isArrayDescriptor(pd, NOT_INDEXED)) {
        Log.log("pd="+pd.getName()+" is not indexed array descriptor", DEBUG);
        root.appendChild(createArrayNode(pd.getName(), propertyValue, document));
    } else {
        // this property is not indexed and is not array
        // check if the node can be expressed by text
        
        // if editor does not exist
        // this should be a complicated property which can only be described
        // as a node.
        Log.log(bean.getClass().getName()+"."+pd.getName()+"("+pd.getPropertyType()+") is not a simple property.", DEBUG);
        
        Object valueObj = getPropertyValue(pd, bean);
        
        boolean addClassAttribute = (!valueObj.getClass().getName().equals(pd.getPropertyType().getName()));
        
        root.appendChild(createNode(pd.getName(), valueObj, 
                  pd.getPropertyEditorClass(), document, addClassAttribute));
    }
}



public static void save(OVTObject obj, String xml_file) throws IOException {
    FileOutputStream out = new FileOutputStream(xml_file);
    try {
        // to set UTF-8 encoding :
        // buildDom(obj).write(out, "UTF-8");
        buildDom(obj).write(out);
    } catch (ParserConfigurationException pce) {
        // Parser with specified options can't be built
        System.out.println("Parser Error!" + pce);
        //pce.printStackTrace();
    }
    out.close();
}


    
/** Initializa <CODE>obj</CODE> using settings from file <CODE>xml_file</CODE>
 */
public static void load(String xml_file, OVTObject obj) throws IOException {
    DocumentBuilderFactory factory = new com.sun.xml.parser.DocumentBuilderFactoryImpl();
    try {
           DocumentBuilder builder = factory.newDocumentBuilder();
           Document document = builder.parse( new File(xml_file) );
           // Cast to XmlDocument for write() operation
           // (Not defined until DOM Level 3.)
           XmlDocument xdoc = (XmlDocument) document;
           set(xdoc.getDocumentElement(), obj); // first child is OVT
           
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

/** 
 * Takes data from <CODE>NamedNodeMap map</CODE> to initialize <CODE>obj</CODE>.
 * <p>
 * <CODE>attributes</CODE> contain property-value pairs from the TAG.
 * <p>
 * For ex.: for &lt;OBJECT name=&quot;Kaliamba&quot; weight=&quot;18&quot;&gt;
 * <CODE>map</CODE> will contain name=.. weight=.. data.
 * <p>
 * This method skipps <CODE>VISIBLE</CODE>, <CODE>CLASS</CODE> and <CODE>INDEX</CODE> attributes.
 */    
private static void setAttributes(NamedNodeMap attributes, Object obj, Hashtable descriptors) throws SAXException {
    
    for (int i=0; i<attributes.getLength(); i++) {
        Attr attr = (Attr)attributes.item(i);
        String propertyName = attr.getNodeName();
       // String valueStr = Utils.replaceUnderlines(attr.getValue());
         String valueStr = attr.getValue();
        
        if (propertyName.equals(CLASS)) continue;
        if (propertyName.equals(VISIBLE)) continue;
        if (propertyName.equals(INDEX)) continue; // possible bug..
        
        PropertyDescriptor pd = (PropertyDescriptor)descriptors.get(propertyName);
        
        if (pd != null)
            setPropertyAsText(pd, obj, valueStr);
        else
            throw new SAXException("Property '"+propertyName+"' not found in "+obj.getClass().getName());
    } 
}




/** Retrives attributes from node object and sets them on obj */
private static void set(Element node, Object obj) throws SAXException {
    Log.log(getName(obj), DEBUG);
    //setAttributes(node, obj);
    
    // --------- set attributes ----------
    // set all
    try {
        // try to use PropertyDescriptorManager to get
        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        Hashtable descriptors = new Hashtable(pds.length);
        
        // put all property descriptors to Hashtable
        for (int i=0; i<pds.length; i++) {
            String propertyName = pds[i].getName();
            descriptors.put(pds[i].getName(), pds[i]);
        }
        
        // set all attributes except "visible"
        // "visible" should be set in the very end
        setAttributes(node.getAttributes(), obj, descriptors);
        
        // ----------- Process child nodes --------------
        NodeList nodeList = node.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++) {
            
            if (nodeList.item(i).getNodeType() == Node.TEXT_NODE) {
                //Log.log("*************WARNING**********\nUNPROCESSED nodeList.item("+i+")='"+nodeList.item(i).getNodeValue()+"'");
                continue;
            }
            
            Element childNode = (Element)nodeList.item(i);
            
            String nodeName = childNode.getNodeName();
            Log.log("Processing "+node.getNodeName()+" -> "+nodeName, DEBUG);
            
            PropertyDescriptor pd = (PropertyDescriptor)descriptors.get(nodeName);
            if (pd != null) {
                
                
                boolean indexedProperty = (pd instanceof IndexedPropertyDescriptor && pd.getWriteMethod() == null);
                int index = -1;
                IndexedPropertyDescriptor ipd = null;
                
                if (indexedProperty) {
                    index = new Integer(childNode.getAttribute(INDEX)).intValue();
                    ipd = (IndexedPropertyDescriptor)pd;
                }
                
                if (isPropertyReadOnly(pd)) {
                    Object propertyValueObj = null;
                    
                    if (indexedProperty) 
                        // this property is like  magProps.getDataModel(i);
                        propertyValueObj = getIndexedPropertyValue(ipd, obj, index);
                    else 
                        // this property is like  magProps.getKPIndexDataModel();
                        propertyValueObj = getPropertyValue(pd, obj);
                   
                    if (propertyValueObj != null)
                        set(childNode, propertyValueObj);
                    else
                        throw new SAXException("Object corresponding to node '"+nodeName+"' in "+node.getNodeName()+"("+obj.getClass()+") is null ");
                            
                } else { // pd is not readonly
                    
                    // first - try to get the property Object.
                    // If it is not null - compare it's class with the class specified in
                    // class="..." attribute.
                    
                    if (isSimpleNode(childNode)) {
                        PropertyEditor editor = findEditor(pd);
                        if (editor != null) {
                            String valueStr = childNode.getFirstChild().getNodeValue();
                            if (indexedProperty)
                                setIndexedPropertyAsText(ipd, obj, index, valueStr);
                            else
                                setPropertyAsText(pd, obj, valueStr);
                            
                            continue;
                        } else throw new SAXException("Editor not found for simple node "+childNode);
                    }
                    
                    Object parent = obj;
                    
                    Object propertyValueObj = createObject(childNode, pd, parent);
                    // .getFirstChild()
                    
                    //if (!isArray(propertyValueObj.getClass())
                    //    set(childNode, propertyValueObj);
                    
                    if (indexedProperty)
                        setIndexedPropertyValue(ipd, obj, index, propertyValueObj);
                    else
                        setPropertyValue(pd, obj, propertyValueObj);
                }
            } else
                throw new IllegalArgumentException("Error: node '"+nodeName+"' in '"+node.getNodeName()+"' is invalid for "+obj.getClass());
        } // end of for loop
        
        
        // after we changed the bean we have to fire change methods
        // some objects will change their state from not enabled to enabled
        
        MethodDescriptor methodD[] = beanInfo.getMethodDescriptors();
        for (int i=0; i<methodD.length; i++) {
            try {
                Log.log("Fireing "+obj.getClass().getName()+"."+methodD[i].getMethod().getName(), DEBUG);
                methodD[i].getMethod().invoke(obj, new Object[]{});
            //Log.log("Fireing "+method.getName());
            } catch (IllegalAccessException e2) { e2.printStackTrace();
            } catch (InvocationTargetException e3) { e3.printStackTrace();
            }
        }
        
        // now finaly set "visible" if it exists
        
        String valueStr = node.getAttribute(VISIBLE);
        if (valueStr != null && !"".equals(valueStr)) {
            PropertyDescriptor pd = (PropertyDescriptor)descriptors.get(VISIBLE);
            if (pd != null) setPropertyAsText(pd, obj, valueStr);
            else throw new SAXException("Property '"+VISIBLE+"' not found in "+obj.getClass().getName()+".\n"+node.toString());
        }
        
        
    } catch (IntrospectionException intr_e) {
        // Introspector.getBeanInfo(obj.getClass()) casted exception
        // if the beanInfo object doesnt exist - no problem ;-).. no work ;-)
        intr_e.printStackTrace();
    }
    
}


public static String showSaveDialog(JFrame frameOwner, File defaultFile) {
    JFileChooser chooser = new JFileChooser(defaultFile); 
        chooser.setDialogTitle("Save Settings");
        chooser.setApproveButtonText("Save");
        if (!defaultFile.isDirectory()) chooser.setSelectedFile(defaultFile);
    	
    OvtExtensionFileFilter filter = new OvtExtensionFileFilter(); 
        filter.addExtension(".xml"); 
        filter.setDescription("OVT Config File (*.xml)"); 
        
    chooser.setFileFilter(filter);
    chooser.addChoosableFileFilter(filter);
	
    int returnVal = chooser.showSaveDialog(frameOwner);
	
    if (returnVal == JFileChooser.APPROVE_OPTION) {
        String fname = chooser.getSelectedFile().getAbsolutePath();
        javax.swing.filechooser.FileFilter fileFilter = chooser.getFileFilter();
        // fileFilter can be (*.*). It is not OVTExtensionFilter
        // to avoid ClassCastException - if case ;-)
        if (fileFilter instanceof OvtExtensionFileFilter) {   
            String ext = ((OvtExtensionFileFilter)fileFilter).getExtension();
            if (!fname.endsWith(ext)) fname += ext;
        }
	return fname;
    }
    return null;
}


/** Shows Open Dialog and returns selected filename or <CODE>null</CODE> */
public static String showOpenDialog(JFrame frameOwner, File defaultFile) {
    JFileChooser chooser = new JFileChooser(defaultFile); 
        chooser.setDialogTitle("Load Settings");
        chooser.setApproveButtonText("Load");
        if (!defaultFile.isDirectory()) chooser.setSelectedFile(defaultFile);
	
    OvtExtensionFileFilter filter = new OvtExtensionFileFilter(); 
        filter.addExtension(".xml"); 
        filter.setDescription("OVT Config File (*.xml)"); 
        chooser.setFileFilter(filter);
        chooser.addChoosableFileFilter(filter);
	
    chooser.setLocation(frameOwner.getLocation().x + 100,frameOwner.getLocation().y + 100);
	
    int returnVal = chooser.showOpenDialog(frameOwner);
	
    if(returnVal == JFileChooser.APPROVE_OPTION) {
        String fname = chooser.getSelectedFile().getAbsolutePath();
        return fname;
	
    }
    return null;
}

private static String getName(Object obj) {
    if (obj instanceof NamedObject) return ""+((NamedObject)obj).getName();
    else return obj.toString();
}


/** 
 * Creates array. If componentClass  is not specified in node - pd is used to guess
 * In this stage pd HAS NO editor I hope - otherwise the array should be written as a string
 */ // PropertyDescriptor pd, boolean indexed, 
private static Object createArray(Element arrayNode, Class componentType, Object parent) 
        throws IllegalArgumentException,  SAXException {
    // if (!(arrayNode.getNodeName().equals(ARRAY)))
    //    throw new IllegalArgumentException("This is not "+ARRAY+" node ("+arrayNode.getNodeName()+")");
    
    
    /*try { 
        String className = arrayNode.getAttribute(CLASS);
        if (className != null && !("".equals(className))) 
            cl = Class.forName(className);
    } catch (ClassNotFoundException cl_n_f_e) { cl_n_f_e.printStackTrace(System.err);
    }*/
    
    // no guesses! Please specify everything in the node
    /*if (cl == null) {
        if (preff) cl = pd.getPropertyType().getComponentType(); // the same as ipd.getIndexedPropertyType()
        else cl = pd.getPropertyType();
    }*/
    
    Log.log("->createArray("+arrayNode+", "+componentType.getName()+"[])", DEBUG);
    
    int length = new Integer(arrayNode.getAttribute(LENGTH)).intValue();
    Log.log("--->ArrayLength="+length, DEBUG);
    
    // for multy-dimensional - use <DIMENSIONS><ARRAY><element>10...
    Object array = Array.newInstance(componentType, length);
    
    PropertyEditor editor = findEditor(null, componentType);
    
    NodeList nodeList = arrayNode.getChildNodes();
    int index = 0;
    for (int i=0; i<nodeList.getLength(); i++) {
        Node childNode = nodeList.item(i);
        if (childNode.getNodeType() == Node.TEXT_NODE) continue; // skip strange "       " TextNodes
        Object value = createObject(childNode, componentType, editor, parent); 
        Array.set(array, index++, value);
    }
    return array;
}

private static Object createObject(Node node, PropertyDescriptor pd, Object parent)
                                                                throws SAXException {
    
    //boolean indexedProperty = (pd instanceof IndexedPropertyDescriptor && pd.getWriteMethod() == null);
    
    Class preferredClass = null;
    if (prefferedIndexType(pd)) 
            preferredClass = ((IndexedPropertyDescriptor)pd).getIndexedPropertyType();
        else
            preferredClass = pd.getPropertyType();
    
    return createObject(node, preferredClass, findEditor(pd), parent);
}

/** Returns fully initialized object from node
 * pd is the PropertyDescriptor describing the object we are going to create 
 */
private static Object createObject(Node node, Class preferredClass, PropertyEditor editor, Object parent)
            throws SAXException {
    
    if (node.getNodeType() == Node.TEXT_NODE ) {
        editor.setAsText(node.getNodeValue());
        return editor.getValue();
    } 
    
    // ---------- Node is not a TextNode -----------
    
    
    Element element = (Element)node;
    
    
        
    
    Class cl = null;
    try { 
        String className = element.getAttribute(CLASS);
        if (className != null && !("".equals(className))) 
            cl = Class.forName(className);
    } catch (ClassNotFoundException cl_n_f_e) { cl_n_f_e.printStackTrace(System.err);
    }
    
    if (cl == null) cl = preferredClass;
    
    // the simple node can look like <elementAt index="3" class="java.lang.Double">45.3</elementAt>
    if (isSimpleNode(element)) {
        return createObject(element.getFirstChild(), cl, editor, parent);
    }
    
    if (isArray(cl)) {//node.getNodeName().equals(ARRAY))
        return createArray((Element)node, preferredClass.getComponentType(), parent);
    }
    
    Object obj = createObject(cl, parent);
    set(element, obj);
    return obj;
}


private static Object createObject(Class cl, Object parent) {
    String className = cl.getName();
    Log.log("createObject(className="+className+")", DEBUG);
    try {
        
        // 1. Try to find Constructor (Parent class)
        try {
            Class[] parameterTypes = new Class[]{ parent.getClass() };
            Constructor constructor = cl.getConstructor(parameterTypes);
            Object[] initargs = new Object[]{parent};
            Object obj = constructor.newInstance(initargs);
            return obj;
        } catch (NoSuchMethodException e3) {
            Log.log("Constructor(Parent) "+className+"("+parent.getClass().getName()+") not found.",DEBUG);
        } catch (SecurityException sec_e) {
            Log.log("SecurityException: Constructor(Parent) "+className+"("+parent.getClass().getName()+") not found.",DEBUG);
        }
        
        // 2. Try to find Constructor (OVTCore)
        if (parent instanceof CoreSource) {
            OVTCore core = ((CoreSource)parent).getCore();
            try {
                Class[] parameterTypes = new Class[]{ core.getClass() };
                Constructor constructor = cl.getConstructor(parameterTypes);
                Object[] initargs = new Object[]{ core };
                Object obj = constructor.newInstance(initargs);
                return obj;
            } catch (NoSuchMethodException e3) {
                Log.log("Constructor(OVTCore) "+className+"("+core.getClass().getName()+") not found.",DEBUG);
            } catch (SecurityException sec_e) {
                Log.log("SecurityException: Constructor(OVTCore) "+className+"("+core.getClass().getName()+") not found.",DEBUG);
            }
        }
        
        // 3. Try to find Constructor (void)
        try {
            Class[] parameterTypes = new Class[]{ };
            Constructor constructor = cl.getConstructor(parameterTypes);
            Object[] initargs = new Object[]{};
            Object obj = constructor.newInstance(initargs);
            return obj;
        } catch (NoSuchMethodException e3) {
            Log.log("Constructor(void) "+className+"() not found.",DEBUG);
        } catch (SecurityException sec_e) {
            Log.log("SecurityException: Constructor(void) "+className+"() not found.",DEBUG);
        }
        
    
    // catch the exceptions by constructor.newInstance(initargs);
    } catch (InstantiationException in_e) { in_e.printStackTrace();
    } catch (IllegalAccessException ill_a_e) { ill_a_e.printStackTrace();
    } catch (IllegalArgumentException ill_ar_e) { ill_ar_e.printStackTrace();
    } catch (InvocationTargetException inv_tar_e) { inv_tar_e.printStackTrace();
    }
    Log.err("*******************************************************************");
    Log.err("* No constructor found for "+className+"       *");
    Log.err("*******************************************************************");
    // if nothing was found - exit
    System.exit(-1);
    return null;    
}


public static PropertyEditor findEditor(PropertyDescriptor pd) {
    if (pd instanceof IndexedPropertyDescriptor)
       return findEditor( pd.getPropertyEditorClass(), 
                          ((IndexedPropertyDescriptor)pd).getIndexedPropertyType());
    else
       return findEditor( pd.getPropertyEditorClass(), pd.getPropertyType());
}

/** Tryes to instantiate pd.getPropertyEditorClass()
 *  if it fails - returns PropertyEditorManager.findEditor(pd.getPropertyType())
 */

public static PropertyEditor findEditor(Class propertyEditorClass, Class propertyType) {
    try {
        if (propertyEditorClass != null) {
            Constructor constructor = propertyEditorClass.getConstructor(new Class[]{});
            return (PropertyEditor)constructor.newInstance( new Object[]{} );
        } else { // editorClass == null
            return PropertyEditorManager.findEditor(propertyType);
        }
    } catch (NoSuchMethodException no_such_m_e) { no_such_m_e.printStackTrace(System.err);
    } catch (InvocationTargetException inv_t_e) { inv_t_e.printStackTrace(System.err);
    } catch (IllegalAccessException ill_a_e) { ill_a_e.printStackTrace(System.err);
    } catch (InstantiationException inst_e) { inst_e.printStackTrace(System.err);
    }
    return null;
}



/** Returns true if the node is simple
 * Simple node: &lt;AutoObject&gt; My TEXT value &lt;/AutoObject&gt;
 */
private static boolean isSimpleNode(Element node) {
    NodeList childNodes = node.getChildNodes();
    if (childNodes.getLength() != 1) return false;
    Node child = childNodes.item(0); 
    if ( child.getNodeType() == Node.TEXT_NODE )
        return true;
    else 
        return false;
}

/** returns the value of the property <CODE>pd</CODE> of the bean <CODE>bean</CODE>*/
public static Object getPropertyValue(PropertyDescriptor pd, Object bean) {
    try {
        Method readMethod = pd.getReadMethod();
        return readMethod.invoke(bean, new Object[]{});
    } catch (InvocationTargetException inv_t_e) { Log.err("Property="+pd.getName()+" Bean="+bean.getClass()); inv_t_e.printStackTrace(System.err);
    } catch (IllegalAccessException ill_a_e) { Log.err("Property="+pd.getName()+" Bean="+bean.getClass()); ill_a_e.printStackTrace(System.err);
    } catch (NullPointerException null_p_e) { Log.err("Property="+pd.getName()+" Bean="+bean.getClass()); null_p_e.printStackTrace(System.err);
    }
    return null;
}

/** returns the value of the property <CODE>pd</CODE> of the bean <CODE>bean</CODE>*/
public static void setPropertyValue(PropertyDescriptor pd, Object bean, Object value) {
    try {
        Method writeMethod = pd.getWriteMethod();
        writeMethod.invoke(bean, new Object[]{ value });
    } catch (InvocationTargetException inv_t_e) { Log.err("Property="+pd.getName()+" Bean="+bean.getClass()); inv_t_e.printStackTrace(System.err);
    } catch (IllegalAccessException ill_a_e) { Log.err("Property="+pd.getName()+" Bean="+bean.getClass()); ill_a_e.printStackTrace(System.err);
    } catch (NullPointerException null_p_e) { Log.err("Property="+pd.getName()+" Bean="+bean.getClass()); null_p_e.printStackTrace(System.err);
    }
}

/** returns the value of the property <CODE>pd</CODE> of the bean <CODE>bean</CODE>*/
public static Object getIndexedPropertyValue(IndexedPropertyDescriptor pd, Object bean, int index) {
    try {
        Method readMethod = pd.getIndexedReadMethod();
        return readMethod.invoke(bean, new Object[]{ new Integer(index) });
    } catch (InvocationTargetException inv_t_e) { Log.err("Property="+pd.getName()+" Bean="+bean.getClass()); inv_t_e.printStackTrace(System.err);
    } catch (IllegalAccessException ill_a_e) { Log.err("Property="+pd.getName()+" Bean="+bean.getClass()); ill_a_e.printStackTrace(System.err);
    } catch (NullPointerException null_p_e) { Log.err("Property="+pd.getName()+" Bean="+bean.getClass()); null_p_e.printStackTrace(System.err);
    }
    return null;
}


/** returns the value of the property <CODE>pd</CODE> of the bean <CODE>bean</CODE>*/
public static void setIndexedPropertyValue(IndexedPropertyDescriptor pd, Object bean, int index, Object value) {
    try {
        Method writeMethod = pd.getIndexedWriteMethod();
        if (writeMethod.getParameterTypes()[0].getName().equals("int"))
            writeMethod.invoke(bean, new Object[]{ new Integer(index), value });
        else 
            writeMethod.invoke(bean, new Object[]{ value, new Integer(index) });
            
    } catch (InvocationTargetException inv_t_e) { Log.err("Property="+pd.getName()+" Bean="+bean.getClass()); inv_t_e.printStackTrace(System.err);
    } catch (IllegalAccessException ill_a_e) { Log.err("Property="+pd.getName()+" Bean="+bean.getClass()); ill_a_e.printStackTrace(System.err);
    } catch (NullPointerException null_p_e) { Log.err("Property="+pd.getName()+" Bean="+bean.getClass()); null_p_e.printStackTrace(System.err);
    }
}


private static void setIndexedPropertyAsText(IndexedPropertyDescriptor pd, Object bean, int index, String value) {
    //Log.log("Setting '"+propertyName+"' to '"+value+"'", DEBUG);
    PropertyEditor editor = findEditor(pd);
    editor.setAsText(value);
    setIndexedPropertyValue(pd, bean, index, editor.getValue());
}

private static void setPropertyAsText(PropertyDescriptor pd, Object bean, String value) throws IllegalArgumentException {
    Log.log("Setting "+bean.getClass().getName()+"."+pd.getName()+" to '"+value+"'", DEBUG);
    if (pd instanceof IndexedPropertyDescriptor) {
        throw new IllegalArgumentException("Use another method for IndexedPropertyDescriptor !!!");
    } else {
        PropertyEditor editor = findEditor(pd);
        editor.setAsText(value);
        setPropertyValue(pd, bean, editor.getValue());
    } 
}

} // end of class

/*
 
 <root> 
                   <test>test text</test> 
                   <test>another text</test> 
                   </root>
 
 *
 * to parse it:
 *
 String value = ""; 
                   NodeList children = node.getChildNodes(); 
                   for(int i = 0; i < children.getLength(); i++ ) { 
                     Node ci = children.item(i); 
                     if( ci.getNodeType() == Node.TEXT_NODE ) { 
                       value = value + ci.getNodeValue(); 
                     }
                   }
 
 
 */


    /*
        // set attributes
    if (obj instanceof DescriptorsSource ) {
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
                    //value = Utils.replaceSpaces(pd.getPropertyEditor().getAsText());
                    value = pd.getPropertyEditor().getAsText();
                    if (value != null) root.setAttribute(pd.getName(), value);
                } catch (NullPointerException e2) { 
                    //Log.err("Property descriptor '"+ pd.getName() +"' has no editor", 0);
                    //System.out.println("---------------------SUXX!!!00000000000--------------------"+e2); 
                }
            }
            
            // indexed properties
            e = desc.getIndexedPropertyDescriptors();
            while (e.hasMoreElements()) {
                IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor)e.nextElement();
            }
        }
    }
        
    if (obj instanceof BeansSource) {
            BeansCollection beans = ((BeansSource)obj).getBeanDesriptors();
            if (beans != null) {
                Enumeration e = beans.elements();
                    while (e.hasMoreElements()) {
                        BasicBeanDescriptor bd = (BasicBeanDescriptor)e.nextElement();
                        try {
                            root.appendChild(createNode(bd.getName(), bd.getValue(obj), document, true));
                        } catch (IllegalAccessException e2) { e2.printStackTrace(); System.exit(-1); }
                    }
            }
        }
        
        
    if (recursive) {  
      if (obj instanceof ChildrenSource) {
        Children children = ((ChildrenSource)obj).getChildren();
        if (children != null) {
            Enumeration e = children.elements();
            while (e.hasMoreElements()) {
                try {
                    root.appendChild(createNode((OVTObject)e.nextElement(), document, true));
                } catch (ClassCastException e2) {System.err.println("Cannot create node. " + e2); }
            }
        }
      }
    }*/


// ---------------------- SET ATTTRIBUTES --------------------------

