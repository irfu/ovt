/*
 * TimeEditor.java
 *
 * Created on June 17, 2002, 3:04 PM
 */

package ovt.beans.editor;

import ovt.datatype.*;

import java.beans.*;

/**
 *
 * @author  ko
 * @version 
 */
public class TimeEditor extends PropertyEditorSupport {

    /** Creates new TimeEditor */
    public TimeEditor() {
    }

    
    public String getAsText() {
        Time time = (Time)getValue();
        return time.toString();
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(new Time(text));
    }
}
