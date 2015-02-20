/*
 * TextFieldEditor.java
 *
 * Created on October 28, 2000, 3:41 PM
 */

package ovt.beans;

import java.beans.*;
import java.awt.*;

/**
 *
 * @author  ko
 * @version 
 */
public class TextFieldEditor extends ComponentPropertyEditor {

    /** Creates new TextFieldEditor */
    public TextFieldEditor(BasicPropertyDescriptor pd) {
        super(pd);
    }
    
  public Component getComponent() {
    if (component == null) {
      component = new TextFieldEditorPanel(this);
      addPropertyChangeListener((PropertyChangeListener)component);
    }
    return component;
  }
  
/** Getter for property editCompleteOnKey.
 * @return Value of property editCompleteOnKey.
 */
  public boolean isEditCompleteOnKey() {
      return ((TextFieldEditorPanel) getComponent()).isEditCompleteOnKey();
  }
  
/** Setter for property editCompleteOnKey.
 * @param editCompleteOnKey New value of property editCompleteOnKey.
 */
  public void setEditCompleteOnKey(boolean editCompleteOnKey) {
      ((TextFieldEditorPanel) getComponent()).setEditCompleteOnKey(editCompleteOnKey);
  }
  
}
