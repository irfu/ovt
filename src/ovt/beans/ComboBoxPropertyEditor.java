/*
 * ComboBoxPropretyEditor.java
 *
 * Created on October 25, 2000, 1:15 PM
 */

package ovt.beans;

import java.beans.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

/**
 *
 * @author  ko
 * @version
 */
public class ComboBoxPropertyEditor extends ComponentPropertyEditor {

    /** Creates new ComboBoxPropretyEditor */
  public ComboBoxPropertyEditor(BasicPropertyDescriptor pd, Object[] values, String[] tags) {
    super(pd, values, tags);
  }

  /** Creates new ComboBoxPropretyEditor */
  public ComboBoxPropertyEditor(BasicPropertyDescriptor pd, int[] values, String[] tags) {
    super(pd, values, tags);
  }


  public Component getComponent() {
    if (component == null) {
      component = new ComboBoxEditorPanel(this);
      addPropertyChangeListener((PropertyChangeListener)component);
    }
    return component;
  }

}

class ComboBoxEditorPanel extends JComboBox implements PropertyChangeListener {
  ComboBoxPropertyEditor editor;

  /** Creates new ComboBoxEditorPanel */
  ComboBoxEditorPanel(ComboBoxPropertyEditor editor) {
    super(editor.getTags());
    setMinimumSize(getPreferredSize());
    setToolTipText(editor.getPropertyDescriptor().getToolTipText());
    this.editor = editor;
    addActionListener(this);
    refresh();
  }

  public void refresh() {
    removeActionListener(this);
    setSelectedItem(editor.getAsText());
    addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() instanceof JComboBox) {
      JComboBox cb = (JComboBox)e.getSource();

      String item = (String)cb.getSelectedItem();
      try {
        editor.setAsText(item);
        editor.fireEditingFinished();
      } catch (PropertyVetoException e2) {
        e2.printStackTrace();
      }
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    String prName = evt.getPropertyName();
    //System.out.println("Received change of : " + prName);
    //System.out.println("My prName is " + editor.getPropertyName());
    if (prName.equals("tags")) {

        // Remove all "tags" (menu alternatives) in ComboBox and replace them
        // with the tags in "editor".
        removeActionListener(this);
        removeAllItems();
        String[] tags = editor.getTags();
        for (int i=0; i<tags.length; i++) {
            addItem(tags[i]);
        }
        setSelectedItem(editor.getAsText());
        addActionListener(this);
    } else if (prName.equals(editor.getPropertyName()))
        refresh();
  }
}
