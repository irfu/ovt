/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /stor/devel/ovt2g/ovt/mag/MagProps.java,v $
 Date:      $Date: 2015/10/14 10:23:00 $
 Version:   $Revision: 2.10 $


 Copyright (c) 2000-2015 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev, 
 Yuri Khotyaintsev, Erik P. G. Johansson, Fredrik Johansson)
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification is permitted provided that the following conditions are met:

 * No part of the software can be included in any commercial package without
 written consent from the OVT team.

 * Redistributions of the source or binary code must retain the above
 copyright notice, this list of conditions and the following disclaimer.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS
 IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT OR
 INDIRECT DAMAGES  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.

 OVT Team (http://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
 Khotyaintsev, E. P. G. Johansson, F. Johansson

 =========================================================================*/
package ovt.mag;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ovt.*;
import ovt.beans.*;
import ovt.util.*;
import ovt.event.*;
import ovt.datatype.*;
import ovt.interfaces.*;
import ovt.mag.model.*;
import ovt.object.*;

import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.*;

/**
 * Class contains time INdependent magnetic field properties and references to
 * the (currently) eight time-DEpendent activity indexes
 * (MagActivityEditorDataModel, MagActivityDataEditor).
 *
 * MagProps = Magnetic properties.
 *
 * NOTE: The words "magnetic", "index", and "activity" are somewhat misleading.
 * Not everything referred to is magnetic (e.g. the Mach number, SW pressure)
 * and not all "indexes" are non-dimensional and scalar (e.g. the magnetic
 * field).
 */
public class MagProps extends OVTObject implements MagModel, MagPropsInterface {

    private ovt.OVTCore core;

    private static final int DEBUG = 2;

    /**
     * Minimum absolute value of magnetic field
     */
    public static double BMIN = 3.6;
    /**
     * Maximum absolute value of magnetic field
     */
    public static double BMAX = 63500;
    /**
     * Magnetic moment of the earth for igrf1985 model
     */
    public static final double DIPMOM = -30483.03;

    public static final int NOMODEL = 0;
    public static final int DIPOLE = -10;
    public static final int IGRF = 10;
    public static final int T87 = 87;
    public static final int T89 = 89;
    public static final int T96 = 96;
    public static final int T2001 = 2001;


    /* model = -21.0 ....-25.0      (dipole + tsyganenko 89) <BR>
     *                -11.0 ... -15.0      (dipole + tsyganenko 87)<BR>
     *                -10.0                (dipole)<BR>
     *                 10.0                (igrf)<BR>
     *                 11.0 ...  15.0      (igrf +   tsyganenko 87)<BR>
     *                 21.0 ...  25.0      (igrf + tsyganenko 89)<BR>
     */
//public static double model=-10.0;
    /**
     * Arbitrary factor from <code>0.5</code> to <code>1.5</code> Field =
     * internalField + ModelFactor * externalFiels
     *
     * @see ovt.calc.MagPack#magbv(double[])
     */
    public static double modelFactor = 1.0;   // Make private, final, non-static?

    public static double mSub = 11.0;
    public static double bSub = 13.5;

    private static final double xlim_default = -30;

    /**
     * Minimum distance in the tail. Should be negative. Should be made into a
     * private instance variable (i.e. non-static) with a getValues method?
     */
    public double xlim = xlim_default;

    /**
     * Altitude (km) for footprint tracing. Make private?
     */
    public static final double alt = 100;
    /**
     * Altitude (RE) for footprint tracing
     */
    private static final double footprintAltitude = 100. / Const.RE;

    /**
     * Holds (ModelType, Model) pairs. Hashtable is
     */
    protected Hashtable models = new Hashtable();

    private static final DataSourceChoice INITIAL_DATA_SOURCE_CHOICE = DataSourceChoice.MAG_ACTIVITY_EDITOR;

    /**
     * Utility field used by bound properties.
     *
     * NOTE: Refers to java.beans.PropertyChangeListener, i.e. associated with
     * Java Beans.
     *
     * Appears to be used only for when changing internal and external (magnetic
     * field) model. /Erik P G Johansson 2015-10-09
     */
    private OVTPropertyChangeSupport propertyChangeSupport = new OVTPropertyChangeSupport(this);

    /**
     * Appears to be used only for when "activity" "Apply" buttons are pressed.
     */
    private MagPropsChangeSupport magPropsChangeSupport = new MagPropsChangeSupport(this);

    protected AbstractMagModel internalModel = null;
    protected AbstractMagModel externalModel = null;


    /* Indices representing activity indexes (values that represent some form of quantity in some physical model). */
    public static final int KPINDEX = 1;
    public static final int IMF = 2;
    public static final int SWP = 3;
    public static final int DSTINDEX = 4;
    public static final int MACHNUMBER = 5;
    public static final int SW_VELOCITY = 6;
    public static final int G1 = 7;
    public static final int G2 = 8;
    public static final int MAX_ACTIVITY_INDEX = G2;   // Highest/last index for a activity index. Used for iterating.

    public static final int MAG_FIELD = 30;    // What does this signify? Constant is only used once in OVT.
    public static final int INTERNAL_MODEL = 31;
    public static final int EXTERNAL_MODEL = 32;
    public static final int CLIP_ON_MP = 33;

//public static final int DIPOLE_TILT_COS   = 50;
    public static final int IMF_X = IMF * 100 + 0;
    public static final int IMF_Y = IMF * 100 + 1;
    public static final int IMF_Z = IMF * 100 + 2;

    public static final String KPINDEX_STR = "KPIndex";
    public static final String IMF_STR = "IMF";
    public static final String SWP_STR = "SWP";
    public static final String DSTINDEX_STR = "DSTIndex";
    public static final String MACHNUMBER_STR = "MachNumber";
    public static final String SW_VELOCITY_STR = "SWVelocity";
    public static final String G1_STR = "G1";
    public static final String G2_STR = "G2";

    public static final String MPCLIP = "MP Clipping";  //Added by kono

    /**
     * List of data models tied to the editor window for manually editing tables
     * (or text files) with activity data.
     */
    private final MagActivityEditorDataModel[] activityEditorDataModels = new MagActivityEditorDataModel[MAX_ACTIVITY_INDEX + 1];

    /**
     * List of data models for choosing between table/editors, and OMNI2 data.
     */
    private final Map<Integer, ActivityEditorOrOMNI2_DataModel> activityEditorOrOMNI2_dataModels = new HashMap();

    /**
     * List of data sources for "activity" data. The "activity" data that the
     * rest of OVT uses is read from these instances.
     */
    private final MagActivityDataSource[] activityDataSources = new MagActivityDataSource[MAX_ACTIVITY_INDEX + 1];

    /**
     * Editor window for manually editing a table of "activity" values (for one
     * "index") over time.
     */
    private final MagActivityDataEditor[] activityEditors = new MagActivityDataEditor[MAX_ACTIVITY_INDEX + 1];

    private static final double KPINDEX_DEFAULT = 0;
    private static final double[] IMF_DEFAULT = {0, 0, 0};
    private static final double SWP_DEFAULT = 1.8;
    private static final double DSTINDEX_DEFAULT = -40;
    private static final double MACHNUMBER_DEFAULT = 5.4;
    private static final double SW_VELOCITY_DEFAULT = 400;   // Unit: km/s
    private static final double G1_DEFAULT = 6;
    private static final double G2_DEFAULT = 10;

    // Map (associative array) with default values. Makes it easier to iterate
    // over and obtain value given an index.
    private static final Map<Integer, double[]> ACTIVITY_DEFAULTS = new HashMap();
    // Map with strings representing units for those activity indices which have a unit (others are not set).
    // Not used everywhere yet since other hardcoded constants do exist (2015-10-23).
    private static final Map<Integer, String> UNIT_STRINGS = new HashMap();


    {
        ACTIVITY_DEFAULTS.put(KPINDEX, new double[]{KPINDEX_DEFAULT});
        ACTIVITY_DEFAULTS.put(IMF, IMF_DEFAULT);
        ACTIVITY_DEFAULTS.put(SWP, new double[]{SWP_DEFAULT});
        ACTIVITY_DEFAULTS.put(DSTINDEX, new double[]{DSTINDEX_DEFAULT});
        ACTIVITY_DEFAULTS.put(MACHNUMBER, new double[]{MACHNUMBER_DEFAULT});
        ACTIVITY_DEFAULTS.put(SW_VELOCITY, new double[]{SW_VELOCITY_DEFAULT});
        ACTIVITY_DEFAULTS.put(G1, new double[]{G1_DEFAULT});
        ACTIVITY_DEFAULTS.put(G2, new double[]{G2_DEFAULT});
        UNIT_STRINGS.put(IMF, "nT");
        UNIT_STRINGS.put(SWP, "nPa");
        UNIT_STRINGS.put(SW_VELOCITY, "km/s");
    }

    private static final int[] ACTIVITY_INDICES_OMNI2_AVAILABLE = {KPINDEX, IMF, SWP, DSTINDEX, MACHNUMBER, SW_VELOCITY};

    private MagPropsCustomizer magPropsCustomizer = null;

    /**
     * Holds value of property customizerVisible.
     */
    private boolean customizerVisible;

    /**
     * Magnetopause clipping.
     */
    private static boolean mpClipping = true;

    /**
     * Holds value of property internalModelType.
     */
    private int internalModelType = IGRF;

    /**
     * Holds value of property externalModelType.
     */
    private int externalModelType = T87;

    /**
     * Select what to use as a raw data source for the functionality/code that
     * handles OMNI2 data. All OMNI2 data should pass through this class. Can
     * choose an emulator with made-up data for testing purposes.
     *
     * See comments in OMNI2RawDataSource and OMNI2RawDataSourceImpl.
     */
    private static final OMNI2RawDataSource OMNI2_RAW_DATA_SOURCE = new OMNI2RawDataSourceImpl(
            new File(OVTCore.getUserDir() + OVTCore.getOMNI2CacheSubdir()));
//    private static final OMNI2RawDataSource OMNI2_RAW_DATA_SOURCE = new OMNI2RawDataSourceTestEmulator();

    private static final double MAX_TIME_TO_VALUE_DAYS = 1.0 / 24.0;

    /**
     * Select what to use as a (non-raw) data source for the functionality/code
     * that handles OMNI2 data. All OMNI2 data should pass through this class.
     * See comments in OMNI2DataSource.
     */
    private static final OMNI2DataSource OMNI2_DATA_SOURCE = new OMNI2DataSource(
            OMNI2_RAW_DATA_SOURCE, MAX_TIME_TO_VALUE_DAYS);


    /**
     * Creates new magProperties.
     */
    public MagProps(OVTCore core) {
        super("MagModels");
        setParent(core);
        //Log.log("MagProps :: init ...", 3);
        setIcon(new ImageIcon(OVTCore.getImagesSubdir() + "magnet.gif"));
        showInTree(false);
        this.core = core;

        activityEditorDataModels[KPINDEX] = new MagActivityEditorDataModel(KPINDEX, 0, 9, KPINDEX_DEFAULT, "Kp Index");
        activityEditorDataModels[IMF] = new MagActivityEditorDataModel(IMF, -50, 50, IMF_DEFAULT, new String[]{"Bx [nT]", "By [nT]", "Bz [nT]"});
        activityEditorDataModels[SWP] = new MagActivityEditorDataModel(SWP, 0, 50, SWP_DEFAULT, "SWP [nPa]");
        activityEditorDataModels[DSTINDEX] = new MagActivityEditorDataModel(DSTINDEX, -500, 50, DSTINDEX_DEFAULT, "DST Index");
        activityEditorDataModels[MACHNUMBER] = new MagActivityEditorDataModel(MACHNUMBER, 1, 15, MACHNUMBER_DEFAULT, "Magnetosonic Mach Number");
        activityEditorDataModels[SW_VELOCITY] = new MagActivityEditorDataModel(SW_VELOCITY, 200, 1200, SW_VELOCITY_DEFAULT, "SW Velocity [km/s]");
        activityEditorDataModels[G1] = new MagActivityEditorDataModel(G1, 0, 50, G1_DEFAULT, "G1");
        activityEditorDataModels[G2] = new MagActivityEditorDataModel(G2, 0, 50, G2_DEFAULT, "G2");

        /*============================================
         * Initialize activityDataSources and
         * activityEditorOrOMNI2_dataModels.
         ============================================*/
        for (int activityIndex : ACTIVITY_INDICES_OMNI2_AVAILABLE) {
            final ActivityEditorOrOMNI2_DataSource dataSource = new ActivityEditorOrOMNI2_DataSource(
                    this,
                    activityEditorDataModels[activityIndex],
                    INITIAL_DATA_SOURCE_CHOICE);
            activityEditorOrOMNI2_dataModels.put(activityIndex, dataSource);

            dataSource.addMagPropsChangeListener(new MagPropsChangeListener() {

                @Override
                public void magPropsChanged(MagPropsEvent evt) {
                    // NOTE: The codes references MagProps INSTANCE variables (not static variables).

                    getActivity_clearCache(evt.whatChanged());
                    MagProps.this.fireMagPropsChange(evt);   // Pass on the event to listeners.
                    MagProps.this.getCore().Render();
                }
            });
            activityDataSources[activityIndex] = dataSource;
        }
        activityDataSources[G1] = activityEditorDataModels[G1];
        activityDataSources[G2] = activityEditorDataModels[G2];

        /*============================
         * Initialize activityEditors
         ===========================*/
        if (!OVTCore.isServer()) {
            for (int index : ACTIVITY_INDICES_OMNI2_AVAILABLE) {
                activityEditors[index] = new MagActivityDataEditor(
                        activityEditorDataModels[index],
                        this,
                        (ActivityEditorOrOMNI2_DataSource) activityDataSources[index]);
            }
            activityEditors[G1] = new MagActivityDataEditor(activityEditorDataModels[G1], this, null);
            activityEditors[G2] = new MagActivityDataEditor(activityEditorDataModels[G2], this, null);
            magPropsCustomizer = new MagPropsCustomizer(this, getCore().getXYZWin());
            addMagPropsChangeListener(magPropsCustomizer);
        }
        customizerVisible = false;
    }


    /**
     * IMPLEMENTATION NOTE: Only clears the getActivity cache for a specific
     * activity index. Important to be able to only clear a specific activity
     * index since the code uses the cache for deciding whether to display GUI
     * error messages.
     */
    public void getActivity_clearCache(int activityIndex) {
        MagProps.this.getActivity_cachedReturnValues.remove(activityIndex);
    }


    public OVTCore getCore() {
        return core;
    }


    public Trans getTrans(double mjd) {
        return getCore().getTrans(mjd);
    }

    /*public String getName(){
     return "ovt.mag.MagProps";
     }*/

    public static double getMaxB() {
        return BMAX;
    }


    public static double getMinB() {
        return BMIN;
    }


    public double getModelFactor() {
        return modelFactor;
    }


    /**
     * Getter for property internalModelType.
     *
     * @return Value of property internalModelType.
     */
    public int getInternalModelType() {
        return internalModelType;
    }


    /**
     * Setter for property internalModelType.
     *
     * @param internalModelType New value of property internalModelType.
     */
    public void setInternalModelType(int internalModelType)
            throws IllegalArgumentException {

        if (internalModelType != NOMODEL && internalModelType != DIPOLE && internalModelType != IGRF) {
            throw new IllegalArgumentException("Invalid internal field model type");
        }

        int oldInternalModelType = this.internalModelType;
        this.internalModelType = internalModelType;
        propertyChangeSupport.firePropertyChange("internalModelType", oldInternalModelType, internalModelType);
    }


    /**
     * Getter for property externalModelType.
     *
     * @return Value of property externalModelType.
     */
    public int getExternalModelType() {
        return externalModelType;
    }


    /**
     * Setter for property externalModelType.
     *
     * @param externalModelType New value of property externalModelType.
     */
    public void setExternalModelType(int externalModelType) {
        System.out.println("Setting external model to " + externalModelType);
        if (externalModelType != NOMODEL && externalModelType != T87
                && externalModelType != T89 && externalModelType != T96 && externalModelType != T2001) {
            throw new IllegalArgumentException("Invalid external field model type");
        }
        if (externalModelType == T2001) { // set xlim to -15 ! This model is not valid for x < -15Re
            xlim = -15;
        } else {
            xlim = xlim_default;
        }
        int oldExternalModelType = this.externalModelType;
        this.externalModelType = externalModelType;
        propertyChangeSupport.firePropertyChange("externalModelType", oldExternalModelType, externalModelType);
    }


    public AbstractMagModel getModel(int modelType) {
        AbstractMagModel model = (AbstractMagModel) models.get(new Integer(modelType));
        if (model == null) {
            // there is no model of modelType
            // create it
            switch (modelType) {
                case NOMODEL:
                    model = new NullModel(this);
                    break;
                case DIPOLE:
                    model = new DipoleModel(this);
                    break;
                case IGRF:
                    model = new IgrfModel(this);
                    break;
                case T87:
                    model = new Tsyganenko87(this);
                    break;
                case T89:
                    model = new Tsyganenko89(this);
                    break;
                case T96:
                    model = new Tsyganenko96(this);
                    break;
                case T2001:
                    model = new Tsyganenko2001(this);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid model type :" + modelType);
            }
            models.put(modelType, model);
        }
        return model;
    }


    public IgrfModel getIgrfModel() {
        return (IgrfModel) getModel(IGRF);
    }

//added by kono

    public void setMPClipping(boolean mode) {
        mpClipping = mode;
    }


    public boolean isMPClipping() {
        return mpClipping;
    }


    // Appears to be unused. /Erik P G Johansson 2015-10-07
    public AbstractMagModel getInternalModel() {
        if (internalModel == null) {
            internalModel = getModel(getInternalModelType());
        }
        return internalModel;
    }


    // Appears to be unused. /Erik P G Johansson 2015-10-07
    public AbstractMagModel getExternalModel() {
        if (externalModel == null) {
            externalModel = getModel(getExternalModelType());
        }
        return externalModel;
    }


    /**
     * Return magnetic field vector.
     *
     * @param gsm Coordinate in GSM
     * @param mjd time
     * @param internalModel internal model type
     * @param externalModel external model type
     * @return Magnetic field vector in nT
     */
    public double[] bv(double[] gsm, double mjd, int internalModel, int externalModel) {
        //Log.log("MagProps.bv(..) executed.",2);
        final double[] result = new double[3];
        final double[] intbv = getModel(internalModel).bv(gsm, mjd);
        final double[] extbv = getModel(externalModel).bv(gsm, mjd);
        for (int i = 0; i < 3; i++) {
            result[i] = intbv[i] + getModelFactor() * extbv[i];
        }
        return result;
    }


    /**
     * Returns magnetic field vector using current internal and external field
     * models.
     *
     * @param gsm point in GSM
     * @param mjd time
     * @return magnetic field vector in nT
     */
    @Override    // Interface MagModel
    public double[] bv(double[] gsm, double mjd) {
        return bv(gsm, mjd, getInternalModelType(), getExternalModelType());
    }


    /**
     * Add a PropertyChangeListener to the listener list.
     *
     * @param l The listener to add.
     */
    @Override
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }


    /**
     * Removes a PropertyChangeListener from the listener list.
     *
     * @param l The listener to remove.
     */
    @Override
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }


    /**
     * Add a VetoableChangeListener to the listener list.
     *
     * @param l The listener to add.
     */
    @Override
    public void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
        vetoableChangeSupport.addVetoableChangeListener(l);
    }


    /**
     * Removes a VetoableChangeListener from the listener list.
     *
     * @param l The listener to remove.
     */
    @Override
    public void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
        vetoableChangeSupport.removeVetoableChangeListener(l);
    }


    public void addMagPropsChangeListener(MagPropsChangeListener listener) {
        magPropsChangeSupport.addMagPropsChangeListener(listener);
    }


    public void removeMagPropsChangeListener(MagPropsChangeListener listener) {
        magPropsChangeSupport.removeMagPropsChangeListener(listener);
    }

//----------- DATA ---------------

    public double getKPIndex(double mjd) {
        //Log.log(this.getClass().getSimpleName()+"#getKPIndex("+mjd+"<=>"+new Time(mjd)+")", 2);   // DEBUG
        //final double value = activityEditorDataModels[KPINDEX].getValues(mjd)[0];
        final double value = getActivity(KPINDEX, mjd)[0];
        //Log.log("   value="+value, 2);   // DEBUG
        return value;
    }


    /**
     * Which coordinate system?
     */
    public double[] getIMF(double mjd) {
        return getActivity(IMF, mjd);
    }


    public double getSWP(double mjd) {
        return getActivity(SWP, mjd)[0];
    }


    public double getDSTIndex(double mjd) {
        return getActivity(DSTINDEX, mjd)[0];
    }


    public double getMachNumber(double mjd) {
        //Log.log(this.getClass().getSimpleName() + "#getMachNumber(" + mjd + "<=>" + new Time(mjd) + ")", 2);
        final double value = getActivity(MACHNUMBER, mjd)[0];
        //Log.log("   value=" + value, 2);   // DEBUG
        return value;
    }


    public double getSWVelocity(double mjd) {
        return getActivity(SW_VELOCITY, mjd)[0];
    }


    public double getG1(double mjd) {
        return getActivity(G1, mjd)[0];
    }


    public double getG2(double mjd) {
        return getActivity(G2, mjd)[0];
    }


    public double getSint(double mjd) {
        return getTrans(mjd).getSint();
    }


    public double getCost(double mjd) {
        return getTrans(mjd).getCost();
    }


    /**
     * Returns dipole tilt angle in RADIANS
     *
     * @see #getSint() #getCost()
     */
    public double getDipoleTilt(double mjd) {
        return getTrans(mjd).getDipoleTilt();
    }


    /**
     * Returns footprint altitude (km)
     *
     * @deprecated since 0.001 Use #getFootprintAltitude()
     */
    public double getAlt() {
        return alt;
    }


    /**
     * Returns footprint altitude (RE)
     *
     * @return footprint altitude (RE)
     */
    public double getFootprintAltitude() {
        //System.out.println("getFootprintAlt is Broken.");
        return footprintAltitude;
    }


    public double getXlim() {
        return xlim;
    }


    public Descriptors getDescriptors() {
        if (descriptors == null) {
            // Add default property descriptor for visible property.    
            // each visual object can be hidden or shown.
            try {

                descriptors = new Descriptors();

                BasicPropertyDescriptor pd = new BasicPropertyDescriptor("mPClipping", this);
                pd.setDisplayName("Clipping on Magnetopause");
                pd.setLabel("clip on magnetopause");

                BasicPropertyEditor editor = new CheckBoxPropertyEditor(pd);
                addPropertyChangeListener("mPClipping", editor);
                pd.setPropertyEditor(editor);
                descriptors.put(pd);

                pd = new BasicPropertyDescriptor("internalModelType", this);
                pd.setToolTipText("Internal model");
                pd.setDisplayName("Internal");
                editor = new ComboBoxPropertyEditor(pd, new int[]{DIPOLE, IGRF}, new String[]{"Dipole", "IGRF"});
                addPropertyChangeListener("internalModelType", editor);
                addPropertyChangeListener(editor);
                pd.setPropertyEditor(editor);
                descriptors.put(pd);

                pd = new BasicPropertyDescriptor("externalModelType", this);
                pd.setToolTipText("External model");
                pd.setDisplayName("External");
                editor = new ComboBoxPropertyEditor(pd, new int[]{T87, T89, T96}, new String[]{"Tsyganenko 87", "Tsyganenko 89", "Tsyganenko 96"});
                addPropertyChangeListener("externalModelType", editor);
                addPropertyChangeListener(editor);
                pd.setPropertyEditor(editor);
                descriptors.put(pd);

            } catch (IntrospectionException e2) {
                System.out.println(getClass().getName() + " -> " + e2.toString());
                System.exit(0);
            }
        }
        return descriptors;
    }


    /**
     * Getter for property customizerVisible.
     *
     * @return Value of property customizerVisible.
     */
    public boolean isCustomizerVisible() {
        return customizerVisible;
    }


    /**
     * Setter for property customizerVisible.
     *
     * @param customizerVisible New value of property customizerVisible.
     */
    public void setCustomizerVisible(boolean customizerVisible) {
        magPropsCustomizer.setVisible(customizerVisible);
        this.customizerVisible = customizerVisible;
    }


    public void fireMagPropsChange() {
        magPropsChangeSupport.fireMagPropsChange();
    }


    public void fireMagPropsChange(MagPropsEvent evt) {
        magPropsChangeSupport.fireMagPropsChange(evt);
    }
    /*
     public boolean magFieldConstant(double mjd1, double mjd2) {
     MagActivityEditorDataModel[] indexes = getIndexesFor(getModelTypes());
     for (int i=0; i<indexes.length; i++)
     if (!Vect.equal(indexes[i].getValues(mjd1)), Vect.equal(indexes[i].getValues(mjd2)))
     return false;
     return true;
     }*/


    public static String getActivityName(int index) {
        switch (index) {
            case KPINDEX:
                return KPINDEX_STR;
            case DSTINDEX:
                return DSTINDEX_STR;
            case MACHNUMBER:
                return MACHNUMBER_STR;
            case SWP:
                return SWP_STR;
            case IMF:
                return IMF_STR;
            case SW_VELOCITY:
                return SW_VELOCITY_STR;
            case G1:
                return G1_STR;
            case G2:
                return G2_STR;
        }
        throw new IllegalArgumentException("Illegal index : " + index);
    }


    /**
     * @return For activity indices that have a unit, return a string. Otherwise
     * null.
     */
    public static String getUnitString(int activityIndex) {
        return UNIT_STRINGS.get(activityIndex);
    }

    /**
     * Cache for the method "getActivity". Cached values are valid for the point
     * in time getActivity_cachedMjd.
     */
    private final Map<Integer, double[]> getActivity_cachedReturnValues = new HashMap();
    private double getActivity_cachedMjd = Double.NaN;


    /**
     * Get either (1) all values (array) for specific activity index, or (2) one
     * specific value for a specific activity index.
     *
     * NOTE: It appears that all reading of "activity" data by OVT goes through
     * this method. Note that the method is still private though. /Erik P G
     * Johansson 2015-10-07.
     *
     * NOTE: Uses an internal cache to speed up calls. Empirically, we know that
     * the same call is made many times in a row. This is a good place to have a
     * cache since it covers both sources of activity data,
     * MagActivityEditorDataModel and OMNI2. Note that the cache has to be
     * partially cleared when changing data source.
     *
     * NOTE: This method HANDLES ERRORS with error messages in the GUI. Note
     * that it also uses the cache to decide whether to display error messages.
     * Only triggers an error message once for every activity key before
     * changing time/mjd (clearing the cache). The cache thus makes sure that
     * multiple calls for the same activity key will still only trigger one
     * error message. (Is this really appropriate?)
     *
     * @param key Specify which activity variable that is sought, and optionally
     * which component of that variable. The rule for requesting some component
     * of activity, let's say you need Z component of IMF (IMF[2]).
     * <CODE>key = IMF*100 + 2</CODE>
     * @return Activity values.
     */
    private double[] getActivity(int key, double mjd) {
        //Log.log(this.getClass().getSimpleName()+"#getActivity("+key+", "+mjd+"<=>"+new Time(mjd)+")", 2);

        /**
         * Try to use a cached value first.<BR>
         * --------------------------------<BR>
         * IMPLEMENTATION NOTE: Construct chosen to minimize the number of
         * Integer objects created and the number of calls to Map#containsKey
         * (none) and Map#get.
         */
        if (mjd == getActivity_cachedMjd) {
            // CASE: The cache contains values for the same time (mjd).
            final double[] returnValue = getActivity_cachedReturnValues.get(key);
            if (returnValue != null) {
                // CASE: There is a relevant value in the cache.
//                Log.log(getClass().getSimpleName() + "#getActivity(" + key + ", " + mjd + ") = "
//                        + Arrays.toString(returnValue) + "   // Cached value", DEBUG);

                return returnValue;
            }
            // CASE: mjd is the same as for cache, but there was no cached value
            // for this particular "key".
            // ==> Keep cached values, and continue.
        } else {
            // CASE: mjd has changed from what is in the cache.
            // ==> Dismiss all cached values.
            getActivity_cachedMjd = mjd;
            getActivity_cachedReturnValues.clear();
        }

        /*=======================================================================
         * CASE: Could not use the cache. ==> Retrieve value(s) from the source.
         ======================================================================*/
        double[] returnValue;
        int index = -1;
        try {

            if (key <= 100) {
                index = key;
                returnValue = activityDataSources[index].getValues(mjd);
                //final double[] values = activityDataSources[key].getValues(mjd);
                //Log.log("   double[] values = "+Arrays.toString(values), 2);
                //return values;
            } else {
                index = key / 100;
                final int component = key - index * 100;
                //return new double[]{activityDataSources[index].getValues(mjd)[component]};
                returnValue = new double[]{activityDataSources[index].getValues(mjd)[component]};
            }

        } catch (OMNI2DataSource.ValueNotFoundException ex) {
            returnValue = ACTIVITY_DEFAULTS.get(index);

            /* CASE: Some visualization in OVT requires an activity value and OVT is
             * configured to obtain it from OMNI2 where it can not be found.
             */
            final String msg = "Can not find value (" + getActivityName(index) + ")"
                    + " for the specified time in the OMNI2 database.\n"
                    + "Using a default value " + Arrays.toString(returnValue) + " instead.";
            // NOTE: Excludes ex.getMessage() from the message to keep it short.            
            // NOTE: The return value is an array, in particular IMF, and must be prepared to print several values.
            getCore().sendWarningMessage("Can not find activity value required for visualizations", msg);
            Log.log(msg, DEBUG);

        } catch (IOException ex) {
            returnValue = ACTIVITY_DEFAULTS.get(index);

            final String msg = "I/O error when trying to obtain OMNI2 value (" + getActivityName(index) + ").\n"
                    + "Using a default value " + Arrays.toString(returnValue) + " instead. - "
                    + ex.getMessage();
            getCore().sendErrorMessage("Can not find activity value required for visualizations", msg);
            Log.log(msg, DEBUG);
        }

        /**
         * NOTE: Also caches default values! Partly to avoid multiple error
         * messages for the same index & time, but also since the underlying
         * data sources are not expected to change during a session anyway.<BR>
         * NOTE: Store deep copy of the values to make sure they are not altered
         * after the fact. MagActivityEditorDataModel#getValues is known to have
         * returned references to arrays which have later changed (2015-10-23)
         * when the table was updated.
         */
        getActivity_cachedReturnValues.put(key, returnValue.clone());

        // NOTE: This log value comes AFTER any log value in MagActivityEditorDataModel#getValues.
//        Log.log(getClass().getSimpleName() + "#getActivity(" + key + ", " + mjd + ") = "
//                + Arrays.toString(returnValue) + "   // (Non-cached value)", DEBUG);
        return returnValue.clone();
    }


    /**
     * Partly analogous to getActivity but always takes data from OMNI2. Its
     * main purpose is to translate from the activity indices that MagProps
     * defines and the OMNI2Data.FieldID values (file columns) that OMNI2 code
     * uses.
     *
     * NOTE: Throws exceptions as opposed to getActivity which handles
     * exceptions (gives error/warning messages).
     */
    // Move to OMNI2DataSource?!
    public double[] getActivityOMNI2(int activityIndex, double mjd)
            throws OMNI2DataSource.ValueNotFoundException, IOException {
        boolean getIMFvector = false;
        final OMNI2Data.FieldID fieldID;
        switch (activityIndex) {
            case KPINDEX:
                fieldID = OMNI2Data.FieldID.Kp;
                break;
            case IMF:
                fieldID = OMNI2Data.FieldID.IMFx_nT_GSM_GSE;   // Value should not be relevant since getIMFvector==true.
                getIMFvector = true;
                break;
            case SWP:
                fieldID = OMNI2Data.FieldID.SW_ram_pressure_nP;
                break;
            case DSTINDEX:
                fieldID = OMNI2Data.FieldID.DST;
                break;
            case MACHNUMBER:
                fieldID = OMNI2Data.FieldID.SW_M_ms;
                break;
            case SW_VELOCITY:
                fieldID = OMNI2Data.FieldID.SW_velocity_kms;
                break;
            default:
                // NOTE: Will yield exception for G1, G2 since these are not in
                // the OMNI2 data (or at least not read from OMNI2 data.).
                // /2015-10-14
                throw new IllegalArgumentException();
        }

        return OMNI2_DATA_SOURCE.getValues(mjd, fieldID, getIMFvector);
    }


    /**
     * @return A set of key - double[] pairs. If some components is requested -
     * INTERNAL_MODEL and EXTERNAL_MODEL are alse valid keys.
     */
    public Characteristics getCharacteristics(int[] keys, double mjd) {
        final Characteristics res = new Characteristics(mjd);
        double[] values = null;
        for (int i = 0; i < keys.length; i++) {
            switch (keys[i]) {
                case INTERNAL_MODEL:
                    values = new double[]{internalModelType};
                    break;
                case EXTERNAL_MODEL:
                    values = new double[]{externalModelType};
                    break;
                case CLIP_ON_MP:
                    values = new double[]{isMPClipping() ? 1 : 0};
                    break;
                default:
                    values = getActivity(keys[i], mjd);
            }
            res.put(keys[i], values);
        }
        return res.getInstance();
    }


    /**
     * @Returns characteristics of magnetic field for mjd.
     *
     */
    public Characteristics getMagFieldCharacteristics(double mjd) {
        int[] keys = null;
        switch (externalModelType) {
            case T87:
                keys = new int[]{KPINDEX, INTERNAL_MODEL, EXTERNAL_MODEL, CLIP_ON_MP};
                break;
            case T89:
                keys = new int[]{KPINDEX, INTERNAL_MODEL, EXTERNAL_MODEL, CLIP_ON_MP};
                break;
            case T96:
                keys = new int[]{IMF_Y, IMF_Z, SWP, DSTINDEX, INTERNAL_MODEL, EXTERNAL_MODEL, CLIP_ON_MP};
                break;
            case T2001:
                keys = new int[]{G1, G2, IMF_Y, IMF_Z, SWP, DSTINDEX, INTERNAL_MODEL, EXTERNAL_MODEL, CLIP_ON_MP};
                break;
        }
        // keys are - data name, magnetic field depends on
        return getCharacteristics(keys, mjd);
    }


    @Override
    public ActivityEditorOrOMNI2_DataModel getActivityEditorOrOMNI2_DataModel(int activityIndex) {
        return activityEditorOrOMNI2_dataModels.get(activityIndex);
    }


    public void setActivityEditorVisible(int index, boolean makeVisible) {
        activityEditors[index].setVisible(makeVisible);
    }


    public void setActivityEditorLocation(int index, int x, int y) {
        activityEditors[index].setLocation(x, y);
    }


    /**
     * used by XML
     */
    public MagActivityEditorDataModel getKPIndexDataModel() {
        return activityEditorDataModels[KPINDEX];
    }


    /**
     * used by XML
     */
    public MagActivityEditorDataModel getIMFDataModel() {
        return activityEditorDataModels[IMF];
    }


    /**
     * used by XML
     */
    public MagActivityEditorDataModel getSWPDataModel() {
        return activityEditorDataModels[SWP];
    }


    /**
     * used by XML
     */
    public MagActivityEditorDataModel getDSTIndexDataModel() {
        return activityEditorDataModels[DSTINDEX];
    }


    /**
     * used by XML
     */
    public MagActivityEditorDataModel getMachNumberDataModel() {
        return activityEditorDataModels[MACHNUMBER];
    }


    /**
     * used by XML
     */
    public MagActivityEditorDataModel getSWVelocityDataModel() {
        return activityEditorDataModels[SW_VELOCITY];
    }


    /**
     * used by XML
     */
    public MagActivityEditorDataModel getG1DataModel() {
        return activityEditorDataModels[G1];
    }


    /**
     * used by XML
     */
    public MagActivityEditorDataModel getG2DataModel() {
        return activityEditorDataModels[G2];
    }


    /**
     * Intended to be read by "XML" (Java Beans used for save/load settings).
     *
     * IMPLEMENTATION NOTE: Ideally we would want to return something like
     * {@code Map<Integer, DataSourceChoice>} but doing that with Java Beans
     * seems to be complicated and require an overkill solution (too much code)
     * for our purposes. Instead we convert the result to a string which can
     * easily be handled as a Java Beans property by default.<BR>
     * /Erik P G Johansson 2015-10-27
     */
    public String getActivityEditorOrOMNI2Choices() {
//        Log.log(getClass().getSimpleName() + "#getActivityEditorOrOMNI2Choices()", DEBUG);
        final StringBuilder s = new StringBuilder();
        for (int activityIndex : ACTIVITY_INDICES_OMNI2_AVAILABLE) {
            final DataSourceChoice choice = activityEditorOrOMNI2_dataModels.get(activityIndex).getDataSourceChoice();
            String choiceStr;

            if (choice == DataSourceChoice.MAG_ACTIVITY_EDITOR) {
                choiceStr = "Table_editor";   // E=Editor.
            } else if (choice == DataSourceChoice.OMNI2) {
                choiceStr = "OMNI2";   // O=OMNI2
            } else {
                throw new NoSuchElementException("activityEditorOrOMNI2_dataModels.get(index).getDataSourceChoice() returned \"" + choice + "\"."
                        + " This should never be able to happen and indicates a pure code bug.");
            }

            if (s.length() != 0) {
                s.append(';');
            }
            s.append(activityIndex + "=" + choiceStr);
        }
//        Log.log("   s=\"" + s + "\"", DEBUG);
        return s.toString();
    }


    /**
     * Intended to be called by "XML" (Java Beans used for save/load settings).
     *
     * IMPLEMENTATION NOTE: The implementation should preferably be prepared for
     * that "choices" may contain activity index values that are not used in
     * this version of OVT and that those indices should be ignored. This way
     * one could (with some luck) get some basic backward compatibility with
     * settings saved in an earlier version of OVT with OMNI2 support for a
     * different set of "activity variables" (assuming the meaning of index
     * values has not changed).
     */
    public synchronized void setActivityEditorOrOMNI2Choices(String s) {
//        Log.log(getClass().getSimpleName() + "#setActivityEditorOrOMNI2Choices(...)", DEBUG);
        final StringTokenizer st = new StringTokenizer(s, "=;");
        while (st.hasMoreTokens()) {
            final int activityIndex = Integer.parseInt(st.nextToken());
            final String choiceStr = st.nextToken();

            final DataSourceChoice choice;
            if (choiceStr.equals("Table_editor")) {
                choice = DataSourceChoice.MAG_ACTIVITY_EDITOR;
            } else if (choiceStr.equals("OMNI2")) {
                choice = DataSourceChoice.OMNI2;
            } else {
                throw new NoSuchElementException("Found string choiceStr=\"" + choiceStr + "\" which can not be parsed.");
            }

            final MagProps.ActivityEditorOrOMNI2_DataModel dataModel = activityEditorOrOMNI2_dataModels.get(activityIndex);
            if (dataModel != null) {
//                Log.log("   activityIndex=" + activityIndex + "; choice=" + choice, DEBUG);
                dataModel.setDataSourceChoice(choice);
            }
        }
    }

    //##########################################################################
    /**
     * Implementations serve as a data source for "activity" data for a given
     * (fixed) "index".
     */
    public interface MagActivityDataSource {

        /**
         * Returns the relevant "activity" value(s) for an arbitrary point in
         * time. Not that the implementation gets to choose which data point(s)
         * (when in time) to use for the request time.
         */
        public double[] getValues(double mjd) throws OMNI2DataSource.ValueNotFoundException, IOException;

    }

    //##########################################################################
    public enum DataSourceChoice {

        MAG_ACTIVITY_EDITOR, OMNI2
    }

    //##########################################################################
    public interface ActivityEditorOrOMNI2_DataModel {

        public DataSourceChoice getDataSourceChoice();


        public void setDataSourceChoice(DataSourceChoice choice);


        public void addMagPropsChangeListener(MagPropsChangeListener listener);
    }

    /**
     * Class which is a data source for "activity data" and can switch between
     * taking data from (1) a MagActivityEditorDataModel and (2) OMNI2 data.<BR>
     *
     * IMPORTANT: Also serves as data model for the variable/flag/checkbox
     * (visible in the GUI) that decides whether activity data (for a specific
     * activity/index) should be taken from the editor/table, or OMNI2 data.<BR>
     */
    static class ActivityEditorOrOMNI2_DataSource implements MagActivityDataSource, ActivityEditorOrOMNI2_DataModel {

        // TODO: Make sure works with save/load settings (Java Beans).
        private final MagActivityEditorDataModel editorDataModel;
        private final MagProps magProps;
        private final MagPropsChangeSupport magPropsChangeSupport = new MagPropsChangeSupport(this);
        private final int activityIndex;

        /**
         * Flag for where to take data from.
         */
        private DataSourceChoice dataSourceChoice;


        public ActivityEditorOrOMNI2_DataSource(
                MagProps mMagProps,
                MagActivityEditorDataModel mEditorDataModel,
                double mDefaultValue,
                DataSourceChoice initialDataSourceChoice) {

            this(mMagProps, mEditorDataModel, initialDataSourceChoice);
        }


        /**
         * Footnote: Gets the activity index from the data model.
         */
        public ActivityEditorOrOMNI2_DataSource(
                MagProps mMagProps,
                MagActivityEditorDataModel mEditorDataModel,
                DataSourceChoice initialDataSourceChoice) {

            magProps = mMagProps;
            editorDataModel = mEditorDataModel;
            dataSourceChoice = initialDataSourceChoice;
            activityIndex = editorDataModel.getIndex();

        }


        @Override
        public void addMagPropsChangeListener(MagPropsChangeListener listener) {
            magPropsChangeSupport.addMagPropsChangeListener(listener);
        }


        public void setDataSourceChoice(DataSourceChoice choice) {
            Log.log(getClass().getSimpleName() + "#setDataSourceChoice(" + choice + ")"
                    + "   // activityIndex=" + activityIndex
                    + " (" + magProps.getActivityName(activityIndex) + ")", 2);

            // Avoid doing anything unnecessarily, in particular calling listeners.
            if (this.dataSourceChoice == choice) {
                return;
            }
            this.dataSourceChoice = choice;

            final MagPropsEvent evt = new MagPropsEvent(this, editorDataModel.getIndex());
            magPropsChangeSupport.fireMagPropsChange(evt);
        }


        public DataSourceChoice getDataSourceChoice() {
            return dataSourceChoice;
        }


        public double[] getValues(double mjd) throws OMNI2DataSource.ValueNotFoundException, IOException {
            if (dataSourceChoice == DataSourceChoice.MAG_ACTIVITY_EDITOR) {

                return editorDataModel.getValues(mjd);

            } else if (dataSourceChoice == DataSourceChoice.OMNI2) {

                return magProps.getActivityOMNI2(editorDataModel.getIndex(), mjd);

            } else {
                throw new RuntimeException("OVT code bug.");
            }
        }
    }

//##############################################################################
    /**
     * Class for a JCheckBox that chooses between using data from OMNI2 or the
     * editor/table (MagActivityDataEditor).
     */
    public static class ActivityEditorOrOMNI2_CheckBox extends JCheckBox {

        ActivityEditorOrOMNI2_CheckBox(
                String text,
                MagProps.ActivityEditorOrOMNI2_DataModel dataModel) {

            setText(text);

            //------------------------------------------------------------------
            // Make this checkbox react to changes in the data model.
            //magProps.addMagPropsChangeListener(new MagPropsChangeListener() {
            dataModel.addMagPropsChangeListener(new MagPropsChangeListener() {
                // NOTE: Important that this method does not trigger any call to "listeners" (in practise
                // MagProps) since that would trigger MagProps into calling its listeners, i.e. this instance.
                public void magPropsChanged(MagPropsEvent evt) {
                    if (evt.getSource() == dataModel) {
                        // NOTE: Unnecessary to check evt.whatChanged() since
                        // the source object implies it.

                        /**
                         * Java API: "Sets the state of the button. Note that
                         * this method does not trigger an actionEvent."
                         */
                        setSelected(dataModel.getDataSourceChoice() == MagProps.DataSourceChoice.OMNI2);
                    }
                }
            });
            //------------------------------------------------------------------
            // Make the data model react to changes in (the state of) this checkbox.
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
//                    Log.log("Editor/OMNI2 checkbox~actionPerformed : isSelected() == "
//                            + ActivityEditorOrOMNI2_CheckBox.this.isSelected(), DEBUG);
                    //Log.log("e = "+e, DEBUG);

                    if (ActivityEditorOrOMNI2_CheckBox.this.isSelected()) {
                        dataModel.setDataSourceChoice(MagProps.DataSourceChoice.OMNI2);
                    } else {
                        dataModel.setDataSourceChoice(MagProps.DataSourceChoice.MAG_ACTIVITY_EDITOR);
                    }
                }
            });
        }
    }

}

//##############################################################################
/* Could probably be made private.
 * Do not confuse with ovt.beans.MagPropsChangeSupport which is almost identical.
 * If this class is made private and maybe static, make sure that the compiler does not
 * confusee it with ovt.beans.MagPropsChangeSupport.
 */
class MagPropsChangeSupport {

    private Vector listeners = new Vector();
    private Object source = null;


    /**
     * Creates new MagPropsChangeSupport
     */
    public MagPropsChangeSupport(Object source) {
        this.source = source;
    }


    public void addMagPropsChangeListener(MagPropsChangeListener listener) {
        listeners.addElement(listener);
    }


    public void removeMagPropsChangeListener(MagPropsChangeListener listener) {
        listeners.removeElement(listener);
    }


    public void fireMagPropsChange(MagPropsEvent evt) {
        Enumeration e = listeners.elements();
        while (e.hasMoreElements()) {
            // NOTE: A separate variable is useful for inspecting variables when debugging.
            final MagPropsChangeListener magPropsChangeListener = (MagPropsChangeListener) e.nextElement();
            magPropsChangeListener.magPropsChanged(evt);
        }
    }


    public void fireMagPropsChange() {
        MagPropsEvent evt = new MagPropsEvent(source, MagProps.MAG_FIELD);
        fireMagPropsChange(evt);
    }


    public boolean hasListener(MagPropsChangeListener listener) {
        return listeners.contains(listener);
    }

}
