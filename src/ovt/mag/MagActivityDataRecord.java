/*
 * MagActivityDataRecord.java
 *
 * Created on June 17, 2002, 1:42 PM
 */

package ovt.mag;

import ovt.datatype.*;
import ovt.util.*;


/**
 *
 * @author  yuri
 * @version 
 */
public class MagActivityDataRecord {
    
    
    public double[] values;
    
    /** Holds value of property time. */
    public ovt.datatype.Time time;
    
    /** Constructor is for internal use only! Do not use it! */
    public MagActivityDataRecord() {
    }

    MagActivityDataRecord(Time time, double[] values){
        this.time = time;
        this.values = values;
    }
    
    MagActivityDataRecord(double mjd, double[] values){
        this.time = new Time(mjd);
        this.values = values;
    }



    /** Indexed getter for property valueAt.
 * @param index Index of the property.
 * @return Value of the property at <CODE>index</CODE>.
 */
    public double[] getValues() {
        return values;
    }
    
    /** Indexed setter for property valueAt.
 * @param index Index of the property.
 * @param valueAt New value of the property at <CODE>index</CODE>.
 */
    public void setValues(double[] values) {
        this.values = values;
    }
    
    
    public Object get(int i) {
        if (i == 0) return time;
        else return new Double(values[i-1]);
    }
    
    
    
    
    public Object clone() {
        double[] newValues = new double[values.length];
        for (int i=0; i<values.length; i++) newValues[i] = values[i];
        return new MagActivityDataRecord((Time)time.clone(), newValues);
    }
    
    /** Getter for property time.
 * @return Value of property time.
 */
    public ovt.datatype.Time getTime() {
        return time;
    }
    
    /** Setter for property time.
 * @param time New value of property time.
 */
    public void setTime(ovt.datatype.Time time) {
        this.time = time;
    }

}
