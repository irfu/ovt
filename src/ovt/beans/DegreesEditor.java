/*
 * DegreesEditor.java
 *
 * Created on October 23, 2000, 2:24 AM
 */

package ovt.beans;

import ovt.datatype.Degrees;
import java.beans.*;

/**
 *
 * @author  oleg
 * @version 
 */
public class DegreesEditor extends TextFieldEditor {
    
    private double min_value;
    private double max_value;
    
    /** Creates new DoubleEditor */
    public DegreesEditor(BasicPropertyDescriptor pd, double min, double max) {
        super(pd);
        min_value = min;
        max_value = max;
    }
    
    private int ostatok(double val, int divisor)
    {
        val -= (int) val;
        val *= divisor;
        return (int) val;
    }
    
    public String getAsText() { //359°60'60"
        Double dbl = (Double)getValue();
        return Degrees.fromdouble(dbl.doubleValue());
    }
    
    private Double rangeChecked(double val) throws IllegalArgumentException {
        if (val > max_value || val < min_value) {
            throw new IllegalArgumentException ("Value must be in range " + min_value + ".." + max_value);
        }
//DBG   System.out.println("DegreesEditor::rangeChecked(): result = " + val);
        return new Double(val);
    }
    
    public void setAsText(String value) throws PropertyVetoException {
        try
        {
            setValue(rangeChecked(Degrees.todouble(value)));
        } catch (IllegalArgumentException e) {
            throw new PropertyVetoException("Invalid value: " + e.getMessage(), null);
        }
    }
}
