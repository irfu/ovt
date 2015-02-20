/*
 * OVTPropertyEditor.java
 *
 * Created on March 27, 2001, 3:23 PM
 */

package ovt.interfaces;

import java.beans.*;
/**
 *
 * @author  ko
 * @version 
 */
public interface OVTPropertyEditor {

    public void setAsText(String text) throws PropertyVetoException;
    
    public String getAsText();
    
    public Object getValue();
}

