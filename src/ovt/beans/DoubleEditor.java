/*
 * DoubleEditor.java
 *
 * Created on October 23, 2000, 2:24 AM
 */

package ovt.beans;

import java.beans.*;
import java.text.*;

/**
 *
 * @author  ko
 * @version 
 */
public class DoubleEditor extends TextFieldEditor {
    
    private DecimalFormat format;
    
    /** Creates new DoubleEditor */
    public DoubleEditor(BasicPropertyDescriptor pd, int precision) {
        super(pd);
        format = new DecimalFormat("#.#");
        //format.setMinimumFractionDigits(0);
        //format.setPositivePrefix(" ");
        setPrecision(precision);
    }
    
    public String getAsText() {
        double value = ((Double)getValue()).doubleValue();
        String s = format.format(value);
        char dot = format.getDecimalFormatSymbols().getDecimalSeparator();
        if (s.indexOf(dot) != -1) {
            while (s.endsWith("0")) s = s.substring(0, s.length() - 1);
            if (s.charAt(s.length()-1) == dot) s = s.substring(0, s.length() - 1);
        }
        return s;
    }
    
    public void setAsText(String value) throws PropertyVetoException {
        //System.out.println("!!!!! value: [" + value + "]");
        Number n = format.parse(value, new ParsePosition(0));
        //System.out.println("!!!!! parsed number: " + n);
        if (n == null) throw new PropertyVetoException("Invalid value: " + value, null);
        Double dbl = new Double(n.doubleValue());
        if (format.format(dbl.doubleValue()).equals(getAsText())) return; // no change
        setValue(dbl);
    }
    
    
    /** Getter for property precision.
     * @return Value of property precision.
     */
    public int getPrecision() {
        return format.getMaximumFractionDigits();
    }
    /** Setter the number of digits after dot.
     * @param precision New value of property precision.
     */
    public void setPrecision(int precision) {
        int oldPrecision = getPrecision();
        format.setMaximumFractionDigits(precision);
    }
}
