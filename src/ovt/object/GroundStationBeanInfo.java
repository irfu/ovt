package ovt.object;

import java.beans.*;

public class GroundStationBeanInfo extends SimpleBeanInfo {
    

    // Bean descriptor //GEN-FIRST:BeanDescriptor
    private static BeanDescriptor beanDescriptor = new BeanDescriptor  ( GroundStation.class , null );

    private static BeanDescriptor getBdescriptor(){
        return beanDescriptor;
    }

    static {//GEN-HEADEREND:BeanDescriptor
        
        // Here you can add code for customizing the BeanDescriptor.
        
}//GEN-LAST:BeanDescriptor
    
    
    // Property identifiers //GEN-FIRST:Properties
    private static final int PROPERTY_visible = 0;
    private static final int PROPERTY_scale = 1;
    private static final int PROPERTY_customizerVisible = 2;
    private static final int PROPERTY_longitude = 3;
    private static final int PROPERTY_name = 4;
    private static final int PROPERTY_latitude = 5;
    private static final int PROPERTY_color = 6;
    private static final int PROPERTY_type = 7;

    // Property array 
    private static PropertyDescriptor[] properties = new PropertyDescriptor[8];

    private static PropertyDescriptor[] getPdescriptor(){
        return properties;
    }

    static {
        try {
            properties[PROPERTY_visible] = new PropertyDescriptor ( "visible", GroundStation.class, "isVisible", "setVisible" );
            properties[PROPERTY_scale] = new PropertyDescriptor ( "scale", GroundStation.class, "getScale", "setScale" );
            properties[PROPERTY_customizerVisible] = new PropertyDescriptor ( "customizerVisible", GroundStation.class, "isCustomizerVisible", "setCustomizerVisible" );
            properties[PROPERTY_longitude] = new PropertyDescriptor ( "longitude", GroundStation.class, "getLongitude", "setLongitude" );
            properties[PROPERTY_name] = new PropertyDescriptor ( "name", GroundStation.class, "getName", "setName" );
            properties[PROPERTY_latitude] = new PropertyDescriptor ( "latitude", GroundStation.class, "getLatitude", "setLatitude" );
            properties[PROPERTY_color] = new PropertyDescriptor ( "color", GroundStation.class, "getColor", "setColor" );
            properties[PROPERTY_type] = new PropertyDescriptor ( "type", GroundStation.class, "getType", "setType" );
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Properties
        
        // Here you can add code for customizing the properties array.
        
}//GEN-LAST:Properties
    
    // EventSet identifiers//GEN-FIRST:Events

    // EventSet array
    private static EventSetDescriptor[] eventSets = new EventSetDescriptor[0];

    private static EventSetDescriptor[] getEdescriptor(){
        return eventSets;
    }
//GEN-HEADEREND:Events
        
        // Here you can add code for customizing the event sets array.
        
  //GEN-LAST:Events
    
    // Method identifiers //GEN-FIRST:Methods

    // Method array 
    private static MethodDescriptor[] methods = new MethodDescriptor[0];

    private static MethodDescriptor[] getMdescriptor(){
        return methods;
    }
//GEN-HEADEREND:Methods
        
        // Here you can add code for customizing the methods array.
        
  //GEN-LAST:Methods
    
    
    private static final int defaultPropertyIndex = -1;//GEN-BEGIN:Idx
    private static final int defaultEventIndex = -1;//GEN-END:Idx
    
    
 //GEN-FIRST:Superclass
    
    // Here you can add code for customizing the Superclass BeanInfo.
    
 //GEN-LAST:Superclass
    
    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     *
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        return getBdescriptor();
    }
    
    /**
     * Gets the bean's <code>PropertyDescriptor</code>s.
     *
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean.  May return null if the
     * information should be obtained by automatic analysis.
     * <p>
     * If a property is indexed, then its entry in the result array will
     * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
     * A client of getPropertyDescriptors can use "instanceof" to check
     * if a given PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return getPdescriptor();
    }
    
    /**
     * Gets the bean's <code>EventSetDescriptor</code>s.
     *
     * @return  An array of EventSetDescriptors describing the kinds of
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        return getEdescriptor();
    }
    
    /**
     * Gets the bean's <code>MethodDescriptor</code>s.
     *
     * @return  An array of MethodDescriptors describing the methods
     * implemented by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public MethodDescriptor[] getMethodDescriptors() {
        return getMdescriptor();
    }
    
    /**
     * A bean may have a "default" property that is the property that will
     * mostly commonly be initially chosen for update by human's who are
     * customizing the bean.
     * @return  Index of default property in the PropertyDescriptor array
     * 		returned by getPropertyDescriptors.
     * <P>	Returns -1 if there is no default property.
     */
    public int getDefaultPropertyIndex() {
        return defaultPropertyIndex;
    }
    
    /**
     * A bean may have a "default" event that is the event that will
     * mostly commonly be used by human's when using the bean.
     * @return Index of default event in the EventSetDescriptor array
     *		returned by getEventSetDescriptors.
     * <P>	Returns -1 if there is no default event.
     */
    public int getDefaultEventIndex() {
        return defaultEventIndex;
    }
}

