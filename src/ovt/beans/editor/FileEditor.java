/*
 * FileEditor.java
 *
 * Created on June 17, 2002, 7:24 PM
 */

package ovt.beans.editor;

import java.beans.*;

import java.io.File;
/**
 *
 * @author  yuri
 * @version 
 */
public class FileEditor extends PropertyEditorSupport {

    /** Creates new FileEditor */
    public FileEditor() {
    }
    
    /** Creates new FileEditor */
    public FileEditor(File file) {
        setValue(file);
    }

    public void setAsText(String text) {
        setValue(new File(text));
    }
    
    public String getAsText() {
        return ""+getValue();
    }
}
