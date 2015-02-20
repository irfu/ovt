/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/OVTFont.java,v $
  Date:      $Date: 2009/10/27 12:14:36 $
  Version:   $Revision: 2.3 $


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

package ovt.datatype;

import ovt.beans.*;
import ovt.object.*;
import ovt.interfaces.*;

import java.beans.*;

/**
 * All constants were taken from vtkTextMapper.h
 * @author  Oleg
 * @version 
 *
 */
public class OVTFont extends OVTObject {

    public static final int ARIAL    = 0;
    public static final int COURIER  = 1;
    public static final int TIMES    = 2;

    protected static final int[]    fontFamilyValues = { ARIAL,   COURIER,   TIMES };
    protected static final String[] fontFamilyTags   = {"Arial", "Courier", "Times"};

    /** Creates new OVTFont */
    public OVTFont(PropertyChangeListener creator) {
        super("Font");
        setParent((OVTObject)creator);
        if (creator != null)
            addPropertyChangeListener(creator);    // creator will listen to change font properties to update self
    }

    /** Creates new OVTFont */
    public OVTFont() {}

    private Descriptors descriptors = null;
    
    /** Utility field used by bound properties. */
    private OVTPropertyChangeSupport propertyChangeSupport =  new OVTPropertyChangeSupport(this);

    /** Holds value of property bold. */
    private boolean bold = false;
    
    /** Holds value of property italic. */
    private boolean italic = false;
    
    /** Holds value of property shadow. */
    private boolean shadow = true;
    
    /** Holds value of property fontSize. */
    private int fontSize = 12;
    
    /** Holds value of property fontFamily. */
    private int fontFamily = ARIAL;
    
    public Descriptors getDescriptors() {
        if (descriptors == null) {
            try {
                descriptors = new Descriptors();
                
            /* bold property descriptor */
                BasicPropertyDescriptor pd = new BasicPropertyDescriptor("bold", this);
                pd.setTextOnlyAccessible();
                pd.setLabel("Bold");
                GUIPropertyEditor editor = new CheckBoxPropertyEditor(pd);
                addPropertyChangeListener("bold", editor);
                pd.setPropertyEditor(editor);
                descriptors.put(pd);

            /* Italic property descriptor */
                pd = new BasicPropertyDescriptor("italic", this);
                pd.setTextOnlyAccessible();
                pd.setLabel("Italic");
                editor = new CheckBoxPropertyEditor(pd);
                addPropertyChangeListener("italic", editor);
                pd.setPropertyEditor(editor);
                descriptors.put(pd);

            /* Shadow property descriptor */
                pd = new BasicPropertyDescriptor("shadow", this);
                pd.setTextOnlyAccessible();
                pd.setLabel("Shadow");
                pd.setDisplayName("Shadow");
                editor = new CheckBoxPropertyEditor(pd);
                editor.setTags(new String[]{"yes", "no"});
                addPropertyChangeListener("shadow", editor);
                pd.setPropertyEditor(editor);
                descriptors.put(pd);

            /* fontSize property descriptor */
                pd = new BasicPropertyDescriptor("fontSize", this);
                pd.setTextOnlyAccessible();
                pd.setDisplayName("Font size");
                TextFieldEditor tfEditor = new TextFieldEditor(pd);
                tfEditor.setEditCompleteOnKey(true);
                addPropertyChangeListener("fontSize", tfEditor);
                pd.setPropertyEditor(tfEditor);
                descriptors.put(pd);
                
            /* fontFamilyValues property descriptor */
                pd = new BasicPropertyDescriptor("fontFamily", this);
                pd.setTextOnlyAccessible();
                pd.setDisplayName("Font family");
                editor = new ComboBoxPropertyEditor(pd, fontFamilyValues, fontFamilyTags);
                addPropertyChangeListener("fontFamily", editor);
                pd.setPropertyEditor(editor);
                descriptors.put(pd);

            } catch (IntrospectionException e2) {
                System.out.println(getClass().getName() + " -> " + e2.toString());
                System.exit(0);
            }
        }
        return descriptors;
    }
    
    public int bold()   { return bold   ? 1 : 0; }
    public int italic() { return italic ? 1 : 0; }
    public int shadow() { return shadow ? 1 : 0; }
    
    
    /** Add a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener (l);
    }
    /** Add a PropertyChangeListener to the listener list.
     * @param name The property name.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(String name, PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener (name, l);
    }
    /** Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener (l);
    }

    /** Getter for property bold.
     * @return Value of property bold.
     */
    public boolean isBold() {
        return bold;
    }
    
    /** Setter for property bold.
     * @param bold New value of property bold.
     */
    public void setBold(boolean bold) {
        boolean oldBold = this.bold;
        this.bold = bold;
        propertyChangeSupport.firePropertyChange ("bold", new Boolean (oldBold), new Boolean (bold));
    }
    
    /** Getter for property italic.
     * @return Value of property italic.
     */
    public boolean isItalic() {
        return italic;
    }
    
    /** Setter for property italic.
     * @param italic New value of property italic.
     */
    public void setItalic(boolean italic) {
        boolean oldItalic = this.italic;
        this.italic = italic;
        propertyChangeSupport.firePropertyChange ("italic", new Boolean (oldItalic), new Boolean (italic));
    }
    
    /** Getter for property shadow.
     * @return Value of property shadow.
     */
    public boolean isShadow() {
        return shadow;
    }
    
    /** Setter for property shadow.
     * @param shadow New value of property shadow.
     */
    public void setShadow(boolean shadow) {
        boolean oldShadow = this.shadow;
        this.shadow = shadow;
        propertyChangeSupport.firePropertyChange ("shadow", new Boolean (oldShadow), new Boolean (shadow));
    }
    
    /** Getter for property fontSize.
     * @return Value of property fontSize.
     */
    public int getFontSize() {
        return fontSize;
    }
    
    /** Setter for property fontSize.
     * @param fontSize New value of property fontSize.
     */
    public void setFontSize(int fontSize) throws PropertyVetoException {
        if (fontSize < 4 || fontSize > 48) throw new PropertyVetoException("Font size out of range [4..48]", null);
        int oldFontSize = this.fontSize;
        this.fontSize = fontSize;
        propertyChangeSupport.firePropertyChange ("fontSize", new Integer (oldFontSize), new Integer (fontSize));
    }
    
    /** Getter for property fontFamily.
     * @return Value of property fontFamily.
     */
    public int getFontFamily() {
        return fontFamily;
    }
    
    /** Setter for property fontFamily.
     * @param fontFamily New value of property fontFamily.
     */
    public void setFontFamily(int fontFamily) {
        int oldFontFamily = this.fontFamily;
        this.fontFamily = fontFamily;
        propertyChangeSupport.firePropertyChange ("fontFamily", new Integer (oldFontFamily), new Integer (fontFamily));
    }
}
