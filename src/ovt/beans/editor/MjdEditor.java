/*
 * MjdEditor.java
 *
 * Created on June 19, 2002, 10:54 AM
 */

package ovt.beans.editor;

import java.beans.*;

import java.io.File;

import java.awt.Component;

/**
 *
 * @author  yuri
 * @version 
 */
public class MjdEditor extends PropertyEditorSupport {

    /** Creates new MjdEditor */
    public MjdEditor() {
    }

    public void setAsText(String text) throws IllegalArgumentException {
        try {
            setValue(new Double(new ovt.datatype.Time(text).getMjd()));
        } catch (NumberFormatException e2) {
            throw new IllegalArgumentException("Time format: YYYY-MM-DD HH:MM:SS");
        }
    }
    
    public String getAsText() {
        return "" + new ovt.datatype.Time(((Double)getValue()).doubleValue());
    }
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public Component getCustomEditor() {
        return new MjdEditorPanel(this);
    }
}
