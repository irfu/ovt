/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/MagProps.java,v $
  Date:      $Date: 2006/03/21 12:15:42 $
  Version:   $Revision: 2.10 $


Copyright (c) 2000-2003 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev, 
Yuri Khotyaintsev)
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
Khotyaintsev

=========================================================================*/

package ovt.mag;


import ovt.*;
import ovt.beans.*;
import ovt.util.*;
import ovt.event.*;
import ovt.datatype.*;
import ovt.interfaces.*;
import ovt.mag.model.*;
import ovt.object.*;

import java.io.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import java.lang.reflect.*;

/** Class contains time independent magnetic field properties
 * properties.
 */
public class MagProps extends OVTObject 
    implements MagModel {

private ovt.OVTCore core;

/** Minimum absolut value of magnetic field */
public static double BMIN = 3.6;
/** Maximum absolut value of magnetic field */
public static double BMAX = 63500;
/** magnetic moment of the earth for igrf1985 model */
public static final double DIPMOM = -30483.03;


public static final int NOMODEL  =  0;
public static final int DIPOLE   = -10;
public static final int IGRF     =  10;
public static final int T87   =  87;
public static final int T89   =  89;
public static final int T96   =  96;
public static final int T2001 =  2001;


/* model = -21.0 ....-25.0      (dipole + tsyganenko 89) <BR>
 *                -11.0 ... -15.0      (dipole + tsyganenko 87)<BR>
 *                -10.0                (dipole)<BR>
 *                 10.0                (igrf)<BR>
 *                 11.0 ...  15.0      (igrf +   tsyganenko 87)<BR>
 *                 21.0 ...  25.0      (igrf + tsyganenko 89)<BR>
 */

//public static double model=-10.0;

/**  arbitrary factor from <code>0.5</code> to <code>1.5</code>
 *  Field = internalField  +  ModelFactor * externalFiels
 *  @see ovt.calc.MagPack#magbv(double[])
 */

public static double modelFactor=1.0;

public static double mSub=11.0;
public static double bSub=13.5;

/** Minimum distance in the tail. Should be negative. */
public static double xlim = -30;

/** Altitude (km) for footprint tracing */
public static double alt = 100;
/** Altitude (RE) for footprint tracing */
protected double footprintAltitude = 100./Const.RE;

/** Holds (ModelType, Model) pairs. Hashtable is */
protected Hashtable models = new Hashtable();

/** Utility field used by bound properties. */
private OVTPropertyChangeSupport propertyChangeSupport =  new OVTPropertyChangeSupport (this);
private MagPropsChangeSupport magPropsChangeSupport = new MagPropsChangeSupport(this);

protected AbstractMagModel internalModel = null;
protected AbstractMagModel externalModel = null;


/* Magnetic Activity */
public static final int KPINDEX     = 1;
public static final int IMF         = 2;
public static final int SWP         = 3;
public static final int DSTINDEX    = 4;
public static final int MACHNUMBER  = 5;
public static final int SW_VELOCITY = 6;
public static final int G1 = 7;
public static final int G2 = 8;
public static final int MAX_ACTIVITY_INDEX = G2;

public static final int MAG_FIELD        = 30;
public static final int INTERNAL_MODEL   = 31;
public static final int EXTERNAL_MODEL   = 32;
public static final int CLIP_ON_MP       = 33;

//public static final int DIPOLE_TILT_COS   = 50;

public static final int IMF_X     = IMF*100 + 0;
public static final int IMF_Y     = IMF*100 + 1;
public static final int IMF_Z     = IMF*100 + 2;

public static final String KPINDEX_STR = "KPIndex";
public static final String IMF_STR = "IMF";
public static final String SWP_STR = "SWP";
public static final String DSTINDEX_STR = "DSTIndex";
public static final String MACHNUMBER_STR = "MachNumber";
public static final String SW_VELOCITY_STR = "SWVelocity";
public static final String G1_STR = "G1";
public static final String G2_STR = "G2";

public static final String MPCLIP = "MP Clipping";  //Added by kono

private MagActivityDataModel[] activity = new MagActivityDataModel[MAX_ACTIVITY_INDEX + 1];

public static final double KPINDEX_DEFAULT = 0;
public static final double[] IMF_DEFAULT = {0,0,0};
public static final double SWP_DEFAULT = 1.8;
public static final double DSTINDEX_DEFAULT = -40;
public static final double MACHNUMBER_DEFAULT = 5.4;
public static final double SW_VELOCITY_DEFAULT = 400; // km/s

public MagActivityDataEditor[] activityEditors = new MagActivityDataEditor[MAX_ACTIVITY_INDEX + 1];

private MagPropsCustomizer magPropsCustomizer= null;

/** Holds value of property customizerVisible. */
private boolean customizerVisible;

/** Magnetopause clipping. */
private static boolean mpClipping = true;

/** Holds value of property internalModelType. */
private int internalModelType = IGRF;

/** Holds value of property externalModelType. */
private int externalModelType = T87;

/** Creates new magProperties. */
public MagProps(OVTCore core) {
  super("MagModels");
  setParent(core);
  Log.log("MagProps :: init ...", 3);
  setIcon(new ImageIcon(OVTCore.getImagesDir()+"magnet.gif"));
  showInTree(false);
  this.core = core;
  
  activity[KPINDEX] = new MagActivityDataModel( KPINDEX, 0, 9, KPINDEX_DEFAULT, "KP Index");
  activity[IMF] = new MagActivityDataModel( IMF, -50, 50, IMF_DEFAULT, new String[]{"Bx [nT]", "By [nT]", "Bz [nT]"});
  activity[SWP] = new MagActivityDataModel( SWP, 0, 50, SWP_DEFAULT, "SWP [nPa]");
  activity[DSTINDEX] = new MagActivityDataModel( DSTINDEX, -500, 50, DSTINDEX_DEFAULT, "DST Index");
  activity[MACHNUMBER] = new MagActivityDataModel( MACHNUMBER, 1, 15, MACHNUMBER_DEFAULT, "Mach Number");
  activity[SW_VELOCITY] = new MagActivityDataModel( SW_VELOCITY, 200, 1200, SW_VELOCITY_DEFAULT, "SW Velocity [km/s]");
  activity[G1] = new MagActivityDataModel( G1, 0, 50, 6, "G1");
  activity[G2] = new MagActivityDataModel( G2, 0, 50, 10, "G2");
  
  
  if (!OVTCore.isServer()) {
      activityEditors[KPINDEX] = new MagActivityDataEditor(activity[KPINDEX], this);
      activityEditors[IMF] = new MagActivityDataEditor(activity[IMF], this);
      activityEditors[SWP] = new MagActivityDataEditor(activity[SWP], this);
      activityEditors[DSTINDEX] = new MagActivityDataEditor(activity[DSTINDEX], this);
      activityEditors[MACHNUMBER] = new MagActivityDataEditor(activity[MACHNUMBER], this);
      activityEditors[SW_VELOCITY] = new MagActivityDataEditor(activity[SW_VELOCITY], this);
      activityEditors[G1] = new MagActivityDataEditor(activity[G1], this);
      activityEditors[G2] = new MagActivityDataEditor(activity[G2], this);
      magPropsCustomizer = new MagPropsCustomizer(this, getCore().getXYZWin());
      addMagPropsChangeListener(magPropsCustomizer);
  }
  customizerVisible = false;
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

public static double getMaxB()
  { return BMAX; }

public static double getMinB()
  { return BMIN; }

public double getModelFactor() {
  return modelFactor;
}

  /** Getter for property internalModelType.
   * @return Value of property internalModelType.
 */
  public int getInternalModelType() {
      return internalModelType;
  }  
  
  /** Setter for property internalModelType.
   * @param internalModelType New value of property internalModelType.
 */
  public void setInternalModelType(int internalModelType)
            throws IllegalArgumentException {

  if (internalModelType!=NOMODEL && internalModelType!=DIPOLE && internalModelType!=IGRF)
      throw new IllegalArgumentException("Invalid internal field model type");

      int oldInternalModelType = this.internalModelType;
      this.internalModelType = internalModelType;
      propertyChangeSupport.firePropertyChange ("internalModelType", new Integer (oldInternalModelType), new Integer (internalModelType));
  }
  
  /** Getter for property externalModelType.
   * @return Value of property externalModelType.
 */
  public int getExternalModelType() {
      return externalModelType;
  }
  
  /** Setter for property externalModelType.
   * @param externalModelType New value of property externalModelType.
 */
  public void setExternalModelType(int externalModelType) {
      System.out.println("Setting exteral model to " + externalModelType);
      if (externalModelType!=NOMODEL && externalModelType!=T87 && 
                externalModelType!=T89 && externalModelType!=T96 && externalModelType!=T2001)
          throw new IllegalArgumentException("Invalid external field model type");
      if (externalModelType == T2001) { // set xlim to -15 ! This model is not valid for x < -15Re
          xlim = -15;
      }
      int oldExternalModelType = this.externalModelType;
      this.externalModelType = externalModelType;
      propertyChangeSupport.firePropertyChange ("externalModelType", new Integer (oldExternalModelType), new Integer (externalModelType));
  }


public AbstractMagModel getModel(int modelType) {
  AbstractMagModel model = (AbstractMagModel)models.get(new Integer(modelType));
  if (model == null) {
    // there is no model of modelType
    // create it
    switch (modelType) {
      case NOMODEL  : model = new NullModel(this); break;
      case DIPOLE   : model = new DipoleModel(this); break;
      case IGRF     : model = new IgrfModel(this); break;
      case T87      : model = new Tsyganenko87(this); break;
      case T89      : model = new Tsyganenko89(this); break;
      case T96      : model = new Tsyganenko96(this); break;
      case T2001    : model = new Tsyganenko2001(this); break;
      default: throw new IllegalArgumentException("Invalid model type :" + modelType);
    }
    models.put(new Integer(modelType), model);
  }
  return model;
}

public IgrfModel getIgrfModel() {
  return (IgrfModel)getModel(IGRF);
}

//added by kono
public void setMPClipping(boolean mode){
  mpClipping = mode;
}

public boolean isMPClipping(){
  return mpClipping;
}

public AbstractMagModel getInternalModel() {
  if (internalModel == null) internalModel = getModel(getInternalModelType());
  return internalModel;
}

public AbstractMagModel getExternalModel() {
  if (externalModel == null) externalModel = getModel(getExternalModelType());
  return externalModel;
}



/** returns magnetic field vector
 * @param gsm Coordinate in GSM
 * @param mjd time
 * @param internalModel internal model type
 * @param externalModel external model type
 * @return magnetic field vector in nT
 */
public double[] bv(double[] gsm, double mjd, int internalModel, int externalModel) {
  Log.log("MagProps.bv(..) executed.",9);
  double[] res = new double[3];
  double[] intbv = getModel(internalModel).bv(gsm, mjd);
  double[] extbv = getModel(externalModel).bv(gsm, mjd);
  for (int i=0; i<3; i++) res[i] = intbv[i] + getModelFactor() * extbv[i];
  return res;
}

/** Returns magnetic field vector using current internal and external field models.
 * @param gsm point in GSM
 * @param mjd time
 * @return magnetic field vector in nT
 */
public double[] bv(double[] gsm,double mjd) {
  return bv(gsm, mjd, getInternalModelType(), getExternalModelType());
}

/** Add a PropertyChangeListener to the listener list.
 * @param l The listener to add.
 */
public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
  propertyChangeSupport.addPropertyChangeListener (l);
}
/** Removes a PropertyChangeListener from the listener list.
 * @param l The listener to remove.
 */
public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
  propertyChangeSupport.removePropertyChangeListener (l);
}
/** Add a VetoableChangeListener to the listener list.
 * @param l The listener to add.
 */
public void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
  vetoableChangeSupport.addVetoableChangeListener (l);
}
/** Removes a VetoableChangeListener from the listener list.
 * @param l The listener to remove.
 */
public void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
  vetoableChangeSupport.removeVetoableChangeListener (l);
}

public void addMagPropsChangeListener (MagPropsChangeListener listener) {
  magPropsChangeSupport.addMagPropsChangeListener (listener);
}

public void removeMagPropsChangeListener (MagPropsChangeListener listener) {
  magPropsChangeSupport.removeMagPropsChangeListener (listener);
}

//----------- DATA ---------------

public double getKPIndex(double mjd) {
   return activity[KPINDEX].getValues(mjd)[0];
}

public double[] getIMF(double mjd) {
  return activity[IMF].getValues(mjd);
}

public double getSWP(double mjd) {
  return activity[SWP].getValues(mjd)[0];
}

public double getDSTIndex(double mjd) {
  return activity[DSTINDEX].getValues(mjd)[0];
}

public double getMachNumber(double mjd) {
  return activity[MACHNUMBER].getValues(mjd)[0];
}

public double getSWVelocity(double mjd) {
  return activity[SW_VELOCITY].getValues(mjd)[0];
}

public double getG1(double mjd) {
  return activity[G1].getValues(mjd)[0];
}

public double getG2(double mjd) {
  return activity[G2].getValues(mjd)[0];
}

public double getSint(double mjd) {
  return getTrans(mjd).getSint();
}

public double getCost(double mjd) {
  return getTrans(mjd).getCost();
}
/**  Returns dipole tilt angle in RADIANS
 * @see #getSint() #getCost()
 */
public double getDipoleTilt(double mjd) {
  return getTrans(mjd).getDipoleTilt();
}


/** Returns footprint altitude (km)
 * @depricated since 0.001 Use #getFootprintAltitude()
 */
public double getAlt() {
  return alt;
}

/** Returns footprint altitude (RE)
 * @returns footprint altitude (RE)
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

  
  /** Getter for property customizerVisible.
   * @return Value of property customizerVisible.
   */
  public boolean isCustomizerVisible() {
    return customizerVisible;
  }
  /** Setter for property customizerVisible.
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
    MagActivityDataModel[] indexes = getIndexesFor(getModelTypes());
    for (int i=0; i<indexes.length; i++)
        if (!Vect.equal(indexes[i].getValues(mjd1)), Vect.equal(indexes[i].getValues(mjd2)))
            return false;
    return true;
  }*/
  
  public static String getActivityName(int index) {
      switch (index) {
          case KPINDEX      : return KPINDEX_STR;
          case DSTINDEX     : return DSTINDEX_STR;
          case MACHNUMBER   : return MACHNUMBER_STR;
          case SWP          : return SWP_STR;
          case IMF          : return IMF_STR;
          case SW_VELOCITY  : return SW_VELOCITY_STR;
          case G1           : return G1_STR;
          case G2           : return G2_STR;
      }
      throw new IllegalArgumentException("Wrong index : " +index);
  }
  
  /** @return activity.
   *  The rule for requesting some component of activity, let's say
   * you need Z component of IMF (IMF[2]). <CODE>key = IMF*100 + 2</CODE>
   */
  public double[] getActivity(int key, double mjd) {
    if (key <= 100) 
            return activity[key].getValues(mjd);
    else {
            int index = key/100;
            int component = key - index*100;
            return new double[]{activity[index].getValues(mjd)[component]};
    }
  }
  
  /** @return a set of key - double[]  pairs. If some components is requested -
   * INTERNAL_MODEL and EXTERNAL_MODEL are alse valid keys.
   */
  public Characteristics getCharacteristics(int[] keys, double mjd) {
    Characteristics res = new Characteristics(mjd);
    double[] values = null;
    for (int i=0; i<keys.length; i++) {
        switch (keys[i]) {
            case INTERNAL_MODEL : values = new double[]{internalModelType};break;
            case EXTERNAL_MODEL : values = new double[]{externalModelType};break;
            case CLIP_ON_MP     : values = new double[]{ isMPClipping() ? 1 : 0 };break; 
            default: values = getActivity(keys[i], mjd);
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
          case T87 : keys = new int[] { KPINDEX, INTERNAL_MODEL, EXTERNAL_MODEL, CLIP_ON_MP }; break;
          case T89 : keys = new int[] { KPINDEX, INTERNAL_MODEL, EXTERNAL_MODEL, CLIP_ON_MP }; break;
          case T96 : keys = new int[] { IMF_Y, IMF_Z, SWP, DSTINDEX, INTERNAL_MODEL, EXTERNAL_MODEL, CLIP_ON_MP }; break;
          case T2001 : keys = new int[] { G1, G2, IMF_Y, IMF_Z, SWP, DSTINDEX, INTERNAL_MODEL, EXTERNAL_MODEL, CLIP_ON_MP }; break;
      }
      // keys are - data name, magnetic field depends on
      return getCharacteristics(keys, mjd);
  }

  
  /** used by XML */
  public MagActivityDataModel getKPIndexDataModel() { return activity[KPINDEX]; }
  /** used by XML */
  public MagActivityDataModel getIMFDataModel() { return activity[IMF]; }  
  /** used by XML */
  public MagActivityDataModel getSWPDataModel() { return activity[SWP]; }  
  /** used by XML */
  public MagActivityDataModel getDSTIndexDataModel() { return activity[DSTINDEX]; }  
  /** used by XML */
  public MagActivityDataModel getMachNumberDataModel() { return activity[MACHNUMBER]; }  
  /** used by XML */
  public MagActivityDataModel getSWVelocityDataModel() { return activity[SW_VELOCITY]; }  
  /** used by XML */
  public MagActivityDataModel getG1DataModel() { return activity[G1]; }  
  /** used by XML */
  public MagActivityDataModel getG2DataModel() { return activity[G2]; }  

}

// ---------------------------------------------------------

class MagPropsChangeSupport {

  private Vector listeners = new Vector();
  private Object source = null;


  /** Creates new MagPropssChangeSupport */
  public MagPropsChangeSupport(Object source) {
    this.source = source;
  }

  public void addMagPropsChangeListener (MagPropsChangeListener listener) {
    listeners.addElement(listener);
  }

  public void removeMagPropsChangeListener (MagPropsChangeListener listener) {
    listeners.removeElement(listener);
  }

  public void fireMagPropsChange(MagPropsEvent evt) {
    Enumeration e = listeners.elements();
    while (e.hasMoreElements())
      ((MagPropsChangeListener)(e.nextElement())).magPropsChanged(evt);
  }

  public void fireMagPropsChange() {
    MagPropsEvent evt = new MagPropsEvent(source, MagProps.MAG_FIELD);
    fireMagPropsChange(evt);
  }

  public boolean hasListener(MagPropsChangeListener listener) {
    return listeners.contains(listener);
  }

}

