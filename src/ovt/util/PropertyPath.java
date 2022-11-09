/*
 * PropertyPath.java
 *
 * Created on October 20, 2000, 12:43 PM
 */

package ovt.util;

/**
 *
 * @author  ko
 * @version 
 */
public class PropertyPath extends Object {
    protected String propPath;
    
    /** Creates new PropertyPath */
    public PropertyPath(String propPath) throws IllegalArgumentException {
        if (propPath.lastIndexOf(".") == -1) 
            throw new IllegalArgumentException("PropertyPath  '"+ propPath +"' is invalid");
        this.propPath = propPath;
    }
    
    public String getPropName() {
        return propPath.substring(propPath.lastIndexOf(".")+1, propPath.length());
    }
    
    public String getObjectPath() {
        int n = propPath.lastIndexOf(".");
        if (n == -1) return ""; // ?? don't know how to be
        return propPath.substring(0, n);
    }
    
    public String toString() {
        return propPath;
    }

}
