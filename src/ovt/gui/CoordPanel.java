/*
 * CoordPanel.java
 * Currently used nowhere, but may be useful in future
 * Created on October 28, 2000, 12:06 PM
 */

package ovt.gui;

import ovt.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;
import ovt.object.editor.*;


import java.io.*;
import java.util.*;
import java.lang.*;
import java.beans.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *
 * @author  ko
 * @version 
 */

/*
        positionPanel = new CoordPanel();
        positionPanel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("values")) {
                  try {
                    double[] position = positionPanel.getValues();
                    camera.setPosition(position);
                  } catch (NumberFormatException e) {
                    // if the position is invalid - restore values.
                    positionPanel.setValues(camera.getPosition());
                  } catch (PropertyVetoException e2) {
                    // if the position is invalid - restore values.
                    positionPanel.setValues(camera.getPosition());
                  }
                }
            }
        });*/

public class CoordPanel extends JPanel implements ActionListener, FocusListener {

    public JTextField[] textField = new JTextField[3];
    
    protected JLabel[] label = new JLabel[3];
    protected String[] cartesianComponents = {"X:", "Y:", "Z:"};
    /** The value, before user change */
    protected String tempValue = "0";
    protected JTextField textFieldBeingEdited = null;
    
    public CoordPanel() {
        for (int i=0; i<3; i++) {
            label[i] = new JLabel(cartesianComponents[i]);
            textField[i]= new JTextField("0.000000");
            textField[i].addActionListener(this);
            textField[i].addFocusListener(this);
        }
        
      // create layout
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2,2,2,2);

        setLayout(gridbag);

        c.gridx = 0;
        c.gridy = 0;
        gridbag.setConstraints(label[0], c);
        add(label[0]);
        
        c.gridx = 1;
        c.gridy = 0;
        gridbag.setConstraints(textField[0], c);
        add(textField[0]);
        
        c.gridx = 0;
        c.gridy = 1;
        gridbag.setConstraints(label[1], c);
        add(label[1]);
        
        c.gridx = 1;
        c.gridy = 1;
        gridbag.setConstraints(textField[1], c);
        add(textField[1]);
        
        c.gridx = 0;
        c.gridy = 2;
        gridbag.setConstraints(label[2], c);
        add(label[2]);
        
        c.gridx = 1;
        c.gridy = 2;
        gridbag.setConstraints(textField[2], c);
        add(textField[2]);
        
    }
    
    public void actionPerformed(ActionEvent evt) {
        System.out.println("action Performed..... oops.");
        firePropertyChange("values", null, null);
    }
    
    public void focusGained(final java.awt.event.FocusEvent evt) {
        textFieldBeingEdited = (JTextField)(evt.getSource());
        tempValue = textFieldBeingEdited.getText();
    }
    
    // one has to consider the case when user changes nothing
    // in the TextField.
    public void focusLost(FocusEvent evt) {
        firePropertyChange("values", null, null);
    }
    
    protected void setValues(double[] values) {
        for (int i=0; i<3; i++) {
            textField[i].removeActionListener(this);
            textField[i].setText(Utils.cuttedString(values[i],2));
            textField[i].addActionListener(this);
        }
    }
    
    public double[] getValues() throws NumberFormatException {
        double[] res = new double[3];
        for (int i=0; i<3; i++) {
            res[i] = (new Double(textField[i].getText())).doubleValue();
        }
        return res;
    }
}
