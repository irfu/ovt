package ovt.object;

import java.beans.*;

public class LTOFSatBeanInfo extends SimpleBeanInfo {

    // Bean descriptor//GEN-FIRST:BeanDescriptor
    /*lazy BeanDescriptor*/
    private static BeanDescriptor getBdescriptor(){
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( ovt.object.LTOFSat.class , null ); // NOI18N//GEN-HEADEREND:BeanDescriptor

        // Here you can add code for customizing the BeanDescriptor.

        return beanDescriptor;     }//GEN-LAST:BeanDescriptor


    // Property identifiers//GEN-FIRST:Properties
    private static final int PROPERTY_dataModules = 0;
    private static final int PROPERTY_labelsModule = 1;
    private static final int PROPERTY_magFootprintModule = 2;
    private static final int PROPERTY_magTangentModule = 3;
    private static final int PROPERTY_mainFieldlineModule = 4;
    private static final int PROPERTY_name = 5;
    private static final int PROPERTY_orbitFile = 6;
    private static final int PROPERTY_orbitModule = 7;
    private static final int PROPERTY_orbitMonitorModule = 8;
    private static final int PROPERTY_satelliteModule = 9;

    // Property array
    /*lazy PropertyDescriptor*/
    private static PropertyDescriptor[] getPdescriptor(){
        PropertyDescriptor[] properties = new PropertyDescriptor[10];

        try {
            properties[PROPERTY_dataModules] = new PropertyDescriptor ( "dataModules", ovt.object.LTOFSat.class, "getDataModules", "setDataModules" ); // NOI18N
            properties[PROPERTY_labelsModule] = new PropertyDescriptor ( "labelsModule", ovt.object.LTOFSat.class, "getLabelsModule", null ); // NOI18N
            properties[PROPERTY_magFootprintModule] = new PropertyDescriptor ( "magFootprintModule", ovt.object.LTOFSat.class, "getMagFootprintModule", null ); // NOI18N
            properties[PROPERTY_magTangentModule] = new PropertyDescriptor ( "magTangentModule", ovt.object.LTOFSat.class, "getMagTangentModule", null ); // NOI18N
            properties[PROPERTY_mainFieldlineModule] = new PropertyDescriptor ( "mainFieldlineModule", ovt.object.LTOFSat.class, "getMainFieldlineModule", null ); // NOI18N
            properties[PROPERTY_name] = new PropertyDescriptor ( "name", ovt.object.LTOFSat.class, "getName", "setName" ); // NOI18N
            properties[PROPERTY_orbitFile] = new PropertyDescriptor ( "orbitFile", ovt.object.LTOFSat.class, "getOrbitFile", "setOrbitFile" ); // NOI18N
            properties[PROPERTY_orbitModule] = new PropertyDescriptor ( "orbitModule", ovt.object.LTOFSat.class, "getOrbitModule", null ); // NOI18N
            properties[PROPERTY_orbitMonitorModule] = new PropertyDescriptor ( "orbitMonitorModule", ovt.object.LTOFSat.class, "getOrbitMonitorModule", null ); // NOI18N
            properties[PROPERTY_satelliteModule] = new PropertyDescriptor ( "satelliteModule", ovt.object.LTOFSat.class, "getSatelliteModule", null ); // NOI18N
        }
        catch(IntrospectionException e) {
            e.printStackTrace();
        }//GEN-HEADEREND:Properties

        // Here you can add code for customizing the properties array.

        return properties;     }//GEN-LAST:Properties

    // EventSet identifiers//GEN-FIRST:Events

    // EventSet array
    /*lazy EventSetDescriptor*/
    private static EventSetDescriptor[] getEdescriptor(){
        EventSetDescriptor[] eventSets = new EventSetDescriptor[0];//GEN-HEADEREND:Events

        // Here you can add code for customizing the event sets array.

        return eventSets;     }//GEN-LAST:Events

    // Method identifiers//GEN-FIRST:Methods

    // Method array
    /*lazy MethodDescriptor*/
    private static MethodDescriptor[] getMdescriptor(){
        MethodDescriptor[] methods = new MethodDescriptor[0];//GEN-HEADEREND:Methods

        // Here you can add code for customizing the methods array.

        return methods;     }//GEN-LAST:Methods


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

