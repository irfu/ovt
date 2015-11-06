/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/Magnetosphere.java,v $
  Date:      $Date: 2003/09/28 17:52:49 $
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

package ovt.object;

import ovt.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.beans.*;
import ovt.event.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import vtk.*;

import java.beans.*;
import java.lang.Math;
import java.util.*;

import java.awt.*;
/**
 * This class cares about plotting the magnetosphere.
 * 
 * NOTE: Contains two classes with similarily-named methods: Magnetosphere and
 * MagnetosphereActor. Do not confuse the two.
 */

public class Magnetosphere extends VisualObject implements
   TimeChangeListener, CoordinateSystemChangeListener, MagPropsChangeListener {


   private static final int DEBUG = 9;
  //Hashtable mpause_edge;
 // Fieldline f_line[][] = new Fieldline[MAXE][4]; /* coordinates of f-lines */

  /** Holds fieldslines starting on x = Xlim. Does not include lines
   * lines starting from y = 0, x = 0
   */
  Fieldline tail_fl[][] = new Fieldline[N_OF_TAIL_FLS][4];
  /** Holds fieldslines starting on x = [0, Xlim]  */
  Fieldline lim_fl[][] = new Fieldline[N_OF_LIM_FLS][4];
  /** Holds fieldslines from the front of magnetosphere. Does not include lines
   * lines starting from y = 0, x = 0
   */
  Fieldline front_fl[][] = new Fieldline[N_OF_FRONT_FLS][4];
  /** Holds fieldslines from y=0. The first two are from front, the second two
   * are from tail. Their locations:<BR>
   * 0,2 - north hemisphere
   * 1,3 - south hemisphere
   */
  Fieldline y_eq_zero_fl[] = new Fieldline[4];

  /** 15 (6) number of angular sectors on x = Xlim, in one quadrant */
  public static final int N_OF_TAIL_AS = 3; //6
  /** Number of fieldslines starting on x = Xlim. Does not include lines
   * lines starting from y = 0, x = 0. In one quadrant.
   */
  public static final int N_OF_TAIL_FLS = N_OF_TAIL_AS - 1;
  /** 15 (6) number of edge points on [0, -Xlim] interval */
  public static final int N_OF_LIM_FLS = 4; //6
  /** See tail */
  public static final int N_OF_FRONT_AS = 3; //6
  /** See tail */
  public static final int N_OF_FRONT_FLS = N_OF_FRONT_AS-1; //6

  private MagnetosphereActor actor = null;
  private vtkPolyData polyData;
  private vtkTubeFilter tubeFilter;

  protected MagnetosphereActorCollection actorCollection = new MagnetosphereActorCollection();
  protected Hashtable characteristicsCollection = new Hashtable();
  protected vtkPoints points;
  protected vtkFloatArray scalars;
  protected vtkCellArray lines;

  /** holds the time, for which the shown magnetosphere was calculated. */
  private double actorsMjd = -1;

   protected double bmin = 7.26, bmax = 57458; // min and max absv(Magnetic field)


   // they are updated in prepareData()
   /** 10 (6) Number of Angular Sectors in 90 deg range (NAS) (XOY, XO-Y)*/
   public static final int NAS = 3;  //6

   /** number of points on the mpause edge */
  // public static final int MAXE = 2*NAS+LIM+1;
   /** number of field lines */
   //public static final int MAXFL = 2*(NAS+LIM)+4*NAS;
   /** max number of points along a field line */
   //public static final int NPF = 120;
   /** number of y-plane field lines !!! strange...*/
   //public static final int NPY = 7;
	/** modified Julian day */
   private double mjd;

                   /* all coordinates are in GSM in units of RE
                           index 0 = north dusk
                                 1 = north dawn
                                 2 = south dawn
                                 3 = south dusk
                         */




// field-line tracing constants
public static final double  EPS		=	0.001;
public static final double  HMIN	=	0.001;
public static final double  FF		=	1.01;
public static final int  MAXCOUNT	=	300;
public static final double  STMIN	=	0.01;

/** Holds value of property fieldlineRadiusScale. */
private double fieldlineRadiusScale = 1.;

/** Holds value of property color. */
 private Color color = Color.blue;


/** Holds value of property wireframe. */
private boolean wireframe = true;   // Default value when launching application.

private boolean scalarcolor = true;

public Magnetosphere(OVTCore core) {
	super(core, "Magnetosphere", "images/magnetosphere.gif");
}

public void setMjd(double mjd) {
  this.mjd = mjd;
  calculate();
  prepareData();
}

public void validate() {
  if (!isValid()) {
    //actorCollection.clear();
    valid = true;
  }
}



private void calculate() {

    Fieldline f_l;

    double[]   gsm  = new double[3],
	           dir  = new double[3],
		   foot = new double[3];
        //,			 imf  = new double[3];
    double  step;

    double  x, r, sigy, sigz;
    int i, ii, m, is, n = 0, sense, option, method = 1;
    //int  zero = 0 , minus = (-1); // static
    double  stepin = 0.5, dangle = 90.0 *Const.D_TO_R / N_OF_FRONT_AS, tail_dangle = 90.0 *Const.D_TO_R / N_OF_TAIL_AS;
    double  altc = 4.0*Const.RE;
    double  mlat = 0, mlt = 0, mlong = 0, ell = 0, dx, xmin;



    // clean data structures
    try {
        for(i=0; i<4; i++) y_eq_zero_fl[i].removeAllElements();
        for(is=0; is<4; is++) {
          for(i=0; i<N_OF_FRONT_FLS; i++) front_fl[i][is].removeAllElements();
          for(i=0; i<N_OF_LIM_FLS; i++) lim_fl[i][is].removeAllElements();
          for(i=0; i<N_OF_TAIL_FLS; i++) tail_fl[i][is].removeAllElements();
          y_eq_zero_fl[is].removeAllElements();
        }
    } catch (NullPointerException e) {}


    dx = getMagProps().xlim / N_OF_LIM_FLS;
    // ?? why  - 10 ???
    //xmin = getMagProps().xlim - 10.0;
    xmin = getMagProps().xlim;

    System.out.print("Computing magnetosphere boundary [4] ... ");
    getCore().setStatus("Computing magnetosphere boundary ... ");
    //XYZWindow.statusLine.showProgressMonitor();

    // ---  CREATE Y_EQ_ZERO_FLS ---
    for (is=0; is<4; is++) {
        //sigy = (is == 1 || is == 2) ? -1.0 : 1.0;
        // tracing direction: is = 0,2 (in mag field direction)
        // is = 1,3 (oposite to mag field direction)
        sigz = (is == 1 || is == 3) ? -1.0 : 1.0;
        sense = (int)sigz;

        if (is==0 || is ==1) {
            r = 12.0;
            dir[0] = gsm[0] = r;
            dir[1] = gsm[1] = 0;
            dir[2] = gsm[2] = 0.0;
        } else { // (is==2 || is ==3)
            // We are at xlim.
            r = 23.9 * Math.atan(Math.sqrt((getMagProps().mSub - getMagProps().xlim) / 15.9));
            gsm[0] = getMagProps().xlim;
            dir[1] = gsm[1] = 0;
            dir[2] = gsm[2] = sigz * r;
            dir[0] = 0.0;
        }

	Trace.lastline(getMagProps(), getMjd(), gsm, dir, xmin, altc, sense, Const.ERR_MP);
        Log.log("y=0 : ["+is+"] "+gsm[0]+"\t"+gsm[1]+"\t"+gsm[2]+"\n",DEBUG);
        step = sigz * stepin;
        f_l = Trace.traceline(getMagProps(), getMjd(), gsm, step, Trace.SQRT_STEP_SIZE);
        n = f_l.size();
        Log.log("["+is+"] -> "+ n +" points!",DEBUG);
        //for (i=0; i<3; i++) dir[i] = f_l.lastPoint().gsm[i];

        y_eq_zero_fl[is] = f_l;
    }
    for (is = 0; is < 4; is++) {

        sigy = (is == 1 || is == 2) ? -1.0 : 1.0;
        sigz = (is == 2 || is == 3) ? -1.0 : 1.0;
        sense = (int)sigz;

	System.out.print(" " + (is+1));
        getCore().setStatus("Computing magnetosphere boundary "+is+" of 4 ");

           for (m = 0; m < N_OF_FRONT_FLS; m++) {
               r = 8.0 + 6.0 * m / N_OF_FRONT_AS;
               dir[0] = gsm[0] = r * Math.cos(sigy * dangle * (m + 1));
               dir[1] = gsm[1] = r * Math.sin(sigy * dangle * (m + 1));
               dir[2] = gsm[2] = 0.0;

	       Trace.lastline(getMagProps(), getMjd(), gsm, dir, xmin, altc, sense, Const.ERR_MP);
               Log.log("Front : ["+is+","+m+"] "+gsm[0]+"\t"+gsm[1]+"\t"+gsm[2]+"\n",DEBUG);
	       step = sigz * stepin;
               f_l = Trace.traceline(getMagProps(), getMjd(), gsm, step, Trace.SQRT_STEP_SIZE);
	       n = f_l.size();
               Log.log("["+m+"] -> "+ n +" points!",DEBUG);
               for (i=0; i<3; i++) dir[i] = f_l.lastPoint().gsm[i];
               front_fl[m][is] = f_l;
           }
           //  LIM
           for (m = 0; m < N_OF_LIM_FLS; m++) {
               x = dx * m;
               gsm[0] = x;
               gsm[1] = sigy * 23.9 * Math.atan(Math.sqrt((getMagProps().mSub - x) / 15.9));
               /* Binsack Howe formula */
               dir[0] = dir[2] = gsm[2] = 0.0;
               dir[1] = sigy * 2.0;
               Trace.lastline(getMagProps(), getMjd(), gsm, dir, xmin, altc, sense, Const.ERR_MP);
               Log.log("LIM : ["+is+","+m+"] "+gsm[0]+"\t"+gsm[1]+"\t"+gsm[2]+"\n",DEBUG);
	       step = sigz * stepin;
               f_l = Trace.traceline(getMagProps(), getMjd(), gsm, step, Trace.SQRT_STEP_SIZE);
	       n = f_l.size();
               Log.log("["+m+"] -> "+ n +" points!",DEBUG);
               for (i=0; i<3; i++) dir[i] = f_l.lastPoint().gsm[i];
               lim_fl[m][is] = f_l;
           }
           // TAIL
           for (m = 0; m < N_OF_TAIL_FLS; m++) {
	       // We are at xlim.
               r = 23.9 * Math.atan(Math.sqrt((getMagProps().mSub - getMagProps().xlim) / 15.9));
               gsm[0] = getMagProps().xlim;
               dir[1] = gsm[1] = sigy * r * Math.cos(tail_dangle * (m + 1));
               dir[2] = gsm[2] = sigz * r * Math.sin(tail_dangle * (m + 1));
               dir[0] = 0.0;
               Trace.lastline(getMagProps(), getMjd(), gsm, dir, xmin, altc, sense, Const.ERR_MP);
               Log.log("TAIL : ["+is+","+m+"] "+gsm[0]+"\t"+gsm[1]+"\t"+gsm[2]+"\n",DEBUG);
	       step = sigz * stepin;
               f_l = Trace.traceline(getMagProps(), getMjd(), gsm, step, Trace.SQRT_STEP_SIZE);
	       n = f_l.size();
               Log.log("["+m+"] -> "+ n +" points!",DEBUG);
               for (i=0; i<3; i++) dir[i] = f_l.lastPoint().gsm[i];
               tail_fl[m][is] = f_l;
           }
	/*
         if (Vect.absv(dir) > 1.2 + getMagProps().alt / Const.RE) {
            Log.log("What's that shitty???");
            f_l = Trace.traceline(getMagProps(), getMjd(), gsm, step, zero);
        } */
    }


    // make corresponding fieldlines from north and south hemisphere match each other

    // fix y_eq_zero_fl's
    // 0,2 - north hem, 1,3 - south hem
    makeFlsStartFromOnePoint(y_eq_zero_fl[0], y_eq_zero_fl[1]);

    // fix  front_fl and lim_fl
    Fieldline magn_parts[][][] = new Fieldline[][][]{ front_fl, lim_fl};

    for (int part = 0; part<magn_parts.length; part++) {
      Fieldline fl[][] = magn_parts[part];
      int n_of_fls = fl.length;
        for (is=0; is<2; is++)
          for (m = 0; m < n_of_fls; m++)
            makeFlsStartFromOnePoint(fl[m][is], fl[m][3-is]);
    }


  System.out.println(" Done.");
  getCore().setStatus("Computation of magnetosphere boundary done.");

}

private void makeFlsStartFromOnePoint(Fieldline north_hem_fl, Fieldline south_hem_fl) {
    double gsm_n[] = new double[3], gsm_s[] = new double[3];
    double stepin = .5;
    Fieldline f_l;

    for (int i = 0; i < 3; i++) {
        gsm_n[i] = north_hem_fl.firstPoint().gsm[i];
        gsm_s[i] = south_hem_fl.firstPoint().gsm[i];
    }

    double x = Vect.absv(gsm_n) - Vect.absv(gsm_s);
    if (x > 0.10) {  // north hemisphere exceeds south hemisphere
        //Log.log("North hem exceeds south hemisohere");
        f_l = Trace.traceline(getMagProps(), getMjd(), gsm_s, stepin, Trace.SQRT_STEP_SIZE);
        north_hem_fl.removeAllElements();
        north_hem_fl.add(f_l);
    } else if (x < -0.10) {
        //Log.log("South hem exceeds north hem");
        f_l = Trace.traceline(getMagProps(), getMjd(), gsm_n, -1.*stepin, Trace.SQRT_STEP_SIZE);
        south_hem_fl.removeAllElements();
        south_hem_fl.add(f_l);
    }

}


// Has to be included when in update() method
protected void prepareData() {
  System.out.print(" Creating visual representation ...");

	int i, j, k, count, linesize;
	double [] p;

	//Fieldline[] f_l = new Fieldline[MAXFL];

        // clean data
	points  = new vtkPoints();
	scalars = new vtkFloatArray();
	lines   = new vtkCellArray();

	// Fill vtkPoints & scalars
	double b;
	bmax = Double.MIN_VALUE; bmin = Double.MAX_VALUE;
        MagPoint magPoint;
        count = 0;
        for (i = 0; i<4; i++) {
	    linesize = y_eq_zero_fl[i].size();
            lines.InsertNextCell(linesize);
		//System.out.println("Y = 0 NEW LINE ["+linesize+"]---------------------");
	    for (j = 0; j<linesize; j++) {
                lines.InsertCellPoint(count+j);
                magPoint = y_eq_zero_fl[i].point(j);
	        p = magPoint.gsm;
		points.InsertPoint(count+j, p[0], p[1], p[2]);
		//System.out.println((count+j)+"\t"+p[0]+"\t"+p[1]+"\t"+p[2]);
		b = Vect.absv(magPoint.bv);
		scalars.InsertValue(count+j, b);
		if (b > bmax) bmax = b;
		if (b < bmin) bmin = b;
	     }
	     count+=linesize;
        }
        Fieldline magn_parts[][][] = new Fieldline[][][]{ front_fl, lim_fl, tail_fl };

        for (int is=0; is<4; is++) {
          for (int part = 0; part<3; part++) {
            Fieldline fl[][] = magn_parts[part];
            int n_of_fls = fl.length;
	    for (i = 0; i<n_of_fls; i++) {
		linesize = fl[i][is].size();
		//System.out.println("-------------- NEW LINE ["+linesize+"]---------------------");
                lines.InsertNextCell(linesize);
		for (j = 0; j<linesize; j++) {
                  lines.InsertCellPoint(count+j);
                  magPoint = fl[i][is].point(j);
	          p = magPoint.gsm;
		  points.InsertPoint(count+j, p[0], p[1], p[2]);
		  //System.out.println((count+j)+"\t"+p[0]+"\t"+p[1]+"\t"+p[2]);
		  b = Vect.absv(magPoint.bv);
		  scalars.InsertValue(count+j, b);
		  if (b > bmax) bmax = b;
		  if (b < bmin) bmin = b;
		}
		count+=linesize;
            }
          }
        }
	//System.out.println("bmin="+bmin+"  bmax="+bmax);

  System.out.println(" Done.");
}




private double getFieldlineRadius() {
    return MagnetosphereActor.NORMAL_FL_RADIUS*fieldlineRadiusScale;
}

public MagnetosphereActor getActor(double mjd) {
  if (!isValid()) {
      validate();
  }
  Characteristics charact = getMagProps().getMagFieldCharacteristics(mjd);
  MagnetosphereActor m_act = actorCollection.getActor(charact);

  if (m_act == null) {
      setMjd(mjd);

      m_act = new MagnetosphereActor(points, lines, scalars);

      // add actor to actor collection
      actorCollection.put(charact, m_act);
  }

  // check for acror's fieldline's radius size
  if (Math.abs(m_act.getFieldlineRadius() - getFieldlineRadius()) > 0.0001) {
          m_act.setFieldlineRadius(getFieldlineRadius());
  }
  // check if the actor is wireframe
  if (m_act.isWireframe() != wireframe) {
      m_act.setWireframe(wireframe);
  }

  return m_act;
}



public void show() {
  actor = getActor(getMjd());
  getRenderer().AddActor(actor);
  rotate();
  actorsMjd = getMjd();
  firePropertyChange("updated", new Boolean(true), new Boolean(false));
}

public void hide() {
  getRenderer().RemoveActor(actor);
  actorsMjd = -1;
}

public void setVisible(boolean visible) {
    if (isVisible() != visible) {
      if (visible) show();
      else hide();
      super.setVisible(visible);
    }
  }

  public void rotate() {
    Matrix3x3 m3x3 = getTrans(getMjd()).gsm_trans_matrix(getCS());
    if (actor !=null) actor.SetUserMatrix(m3x3.getVTKMatrix());
  }

  public void timeChanged(TimeEvent evt) {
      if (evt.timeSetChanged()) actorCollection.clear();
      if (isVisible()) {
          double mjd = getMjd();
          Characteristics ch = getMagProps().getMagFieldCharacteristics(mjd);
          vtkActor newActor = actorCollection.getActor(ch);
          if (newActor == null || newActor != actor) {
                invalidate(); hide(); show();
          } else rotate();
          if (!isUpdated()) firePropertyChange("updated", new Boolean(false), new Boolean(false));
      }
  }

  public void coordinateSystemChanged(CoordinateSystemEvent evt) {
    if (isVisible()) rotate();
  }

  public void magPropsChanged(MagPropsEvent evt) {
    if (isVisible()) {
      hide();
      show();
    }
  }

  public void update() {
      if (!isUpdated()) {
        //invalidate();
        if (isVisible()) {
            hide();
            show();
        }
        firePropertyChange("updated", new Boolean(true), new Boolean(false));
      }
  }

  public boolean isUpdated() {
      return (actorsMjd == getMjd());
  }


  /** Getter for property fieldlineRadiusScale.
   * @return Value of property fieldlineRadiusScale.
   */
  public double getFieldlineRadiusScale() {
      return fieldlineRadiusScale;
  }

  /** Setter for property fieldlineRadiusScale.
   * @param fieldlineRadiusScale New value of property fieldlineRadiusScale.
   */
  public void setFieldlineRadiusScale(double fieldlineRadiusScale) {
      double oldFieldlineRadiusScale = this.fieldlineRadiusScale;
      this.fieldlineRadiusScale = fieldlineRadiusScale;
      if (actor != null) {
          if (Math.abs(actor.getFieldlineRadius() - getFieldlineRadius()) > 0.0001) {
            actor.setFieldlineRadius(getFieldlineRadius());
          }
      }
      firePropertyChange("fieldlineRadiusScale", oldFieldlineRadiusScale, fieldlineRadiusScale);
  }

  /** Getter for property wireframe.
   * @return Value of property wireframe.
   */
  public boolean isWireframe() {
      return wireframe;
  }

  /** Setter for property wireframe.
   * @param wireframe New value of property wireframe.
   */
  public void setWireframe(boolean wireframe) {
      final boolean oldWireframe = this.wireframe;
      this.wireframe = wireframe;
      if (actor != null) {
        if (actor.isWireframe() != wireframe) {
            actor.setWireframe(wireframe);
        }
      }
      propertyChangeSupport.firePropertyChange("wireframe", oldWireframe, wireframe);
  }

  public boolean isScalarcolor() {
      return scalarcolor;
  }
    
public void setScalarcolor(boolean scalarcolor) {
      boolean oldScalarcolor = this.scalarcolor;
      this.scalarcolor = scalarcolor;
      if (actor != null)
        if (actor.isScalarcolor() != scalarcolor) actor.setScalarcolor(scalarcolor);
      propertyChangeSupport.firePropertyChange("scalarcolor", oldScalarcolor, scalarcolor);
  }
  //copied from SingleActorSatModule FKJN
  /** Getter for property color.
   * @return Value of property color.
   */
      public Color getColor() {
          return color;
      }

  /** Setter for property color.
   * @param color New value of property color.
   *
   * @throws PropertyVetoException
   */
      public void setColor(Color color) {
          Color oldColor = this.color;
          boolean oldsetscalar=this.scalarcolor;

          this.color = color;
          if (actor != null) {
              float[] rgb = ovt.util.Utils.getRGB(getColor());
              actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
              actor.setColor(color);

            }
            //firePropertyChange("color", oldColor, color);
          this.setScalarcolor(false);

          propertyChangeSupport.firePropertyChange("scalarcolor", oldsetscalar, false);          
          propertyChangeSupport.firePropertyChange ("color", oldColor, color);
      }

  public Descriptors getDescriptors() {
        if (descriptors == null) {
            try {
                descriptors = super.getDescriptors();

                BasicPropertyDescriptor pd = new BasicPropertyDescriptor("fieldlineRadiusScale", this);
                pd.setLabel("Fieldline Thickness (non-wireframe)...");
                pd.setDisplayName("Magnetospheric Fieldline Thickness");
                ExponentialSliderPropertyEditor fieldlineRadiusScaleEditor = new ExponentialSliderPropertyEditor(pd,
                    1./8., 8., new double[]{1./8.,1./2., 1, 2, 8});
                addPropertyChangeListener("fieldlineRadiusScale", fieldlineRadiusScaleEditor);
                fieldlineRadiusScaleEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                    public void editingFinished(GUIPropertyEditorEvent evt) {
                        Render();
                    }
                });
                pd.setPropertyEditor(new WindowedPropertyEditor(fieldlineRadiusScaleEditor, getCore().getXYZWin()));
                fieldlineRadiusScaleEditor.setToolTipText(
                        "Set the fieldline thickness that applies when the \"Wireframe\" option is disabled.");
                descriptors.put(pd);


                // FKJN copy from Satellite colour 13May 2015
                pd = new BasicPropertyDescriptor("color", this);
                pd.setLabel("Color...");
                pd.setDisplayName(getParent().getName()+" : "+ getName() +" color");
                ComponentPropertyEditor colorEditor = new ColorPropertyEditor(pd);
                colorEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                    public void editingFinished(GUIPropertyEditorEvent evt) {
                        Render();
                    }
                });
                addPropertyChangeListener("color", colorEditor);
                pd.setPropertyEditor(new WindowedPropertyEditor(colorEditor, getCore().getXYZWin(), "Close"));
                descriptors.put(pd);

                pd = new BasicPropertyDescriptor("wireframe", this);
                pd.setLabel("Wireframe");
                //pd.setDisplayName("Keep Fieldlines");
                MenuPropertyEditor wireframeEditor = new BooleanEditor(pd, MenuPropertyEditor.CHECKBOX);
                addPropertyChangeListener("wireframe", wireframeEditor);
                pd.setPropertyEditor(wireframeEditor);
                getDescriptors().put(pd);


                pd = new BasicPropertyDescriptor("scalarcolor", this);
                pd.setLabel("Scalar Colors");
                //pd.setDisplayName("Keep Fieldlines");
                MenuPropertyEditor scalarcolorEditor = new BooleanEditor(pd, MenuPropertyEditor.CHECKBOX);
                addPropertyChangeListener("scalarcolor", scalarcolorEditor);
                pd.setPropertyEditor(scalarcolorEditor);
                getDescriptors().put(pd);


            } catch (IntrospectionException e2) {
                System.out.println(getClass().getName() + " -> " + e2.toString());
                System.exit(0);
            }
        }
        return descriptors;
    }
} // - end of Magnetosphere class

/**
 *  Class <<<<<<<< M A G N E T O S P H E R E     A C T O R  >>>>>>>>>>>
 *
 */

class MagnetosphereActor extends vtkActor {

    public static final double NORMAL_FL_RADIUS = 0.01;
    private vtkPolyData polyData = new vtkPolyData();
    private vtkTubeFilter tubeFilter = new vtkTubeFilter();
    private vtkPolyDataMapper mapper = new vtkPolyDataMapper();
    private boolean wireframe;
    private Color color;
    private boolean scalarcolor;
    private double fieldlineRadius;

    /** Constructs wireframe actor by default */
    MagnetosphereActor(vtkPoints points, vtkCellArray lines, vtkFloatArray scalars) {
        super();
        polyData.SetPoints(points);
        polyData.SetLines(lines);
        polyData.GetPointData().SetScalars(scalars);

        tubeFilter.SetInputData(polyData);
        tubeFilter.SetRadius(0.01);

        vtkLogLookupTable lut  = new vtkLogLookupTable();
          lut.SetHueRange(0.6667, 0);

        setWireframe(true);    // Not the default value in the application GUI.
        setFieldlineRadius(NORMAL_FL_RADIUS);
	mapper.SetScalarModeToUsePointData();
        mapper.ScalarVisibilityOn();
	mapper.SetScalarRange(MagProps.BMIN, MagProps.BMAX);
	mapper.SetLookupTable(lut);
        SetMapper(mapper);
    }

    public void setWireframe(boolean wireframe) {
        this.wireframe = wireframe;
        if (wireframe) mapper.SetInputData(polyData);
        //else mapper.SetInputData(tubeFilter.GetOutput());
        else mapper.SetInputConnection(tubeFilter.GetOutputPort());
    }

    public boolean isWireframe() {
        return wireframe;
    }

    /** by default NORMAL_FL_RADIUS*/
    public void setFieldlineRadius(double radius) {
        this.fieldlineRadius = radius;
        tubeFilter.SetRadius(radius);
    }

    public double getFieldlineRadius() {
        return fieldlineRadius;
    }

    
   public boolean isScalarcolor() {
        return scalarcolor;
    }

    public void setScalarcolor(boolean scalarcolor) {
        this.scalarcolor = scalarcolor;
        if (scalarcolor) mapper.ScalarVisibilityOn();
        else mapper.ScalarVisibilityOff();
    }
  //copied from SingleActorSatModule FKJN
  /** Getter for property color.
   * @return Value of property color.
   */
      public Color getColor() {
          return color;
      }

  /** Setter for property color.
   * @param color New value of property color.
   *
   * @throws PropertyVetoException
   */
      public void setColor(Color color) {
          Color oldColor = this.color;
          //this.setScalarcolor(false);
          this.color = color;

          float[] rgb = ovt.util.Utils.getRGB(getColor());
          this.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
          mapper.ScalarVisibilityOff();

      }
}
// ------------------------------_------------------------------

class MagnetosphereActorCollection extends Hashtable {

  public MagnetosphereActorCollection() {
      super();
  }

  /*public vtkActor getActor(Characteristics ch) {
    return (vtkActor)get(ch);
  }*/

  public MagnetosphereActor getActor(Characteristics ch) {
      Enumeration e = keys();
      while (e.hasMoreElements()) {
          Characteristics obj = (Characteristics)e.nextElement();
          if (ch.equals(obj)) return (MagnetosphereActor)get(obj);
      }
    return null;
  }

}
/*
class MagnetosphereActorCollection extends Vector {

    private Vector characteristics = new Vector();

  public MagnetosphereActorCollection() {
  }

  public void put(vtkActor actor, Characteristics ch) {
    addElement(actor);
    Log.log("MagnetosphereActorCollection.size="+size(), 0);
    characteristics.addElement(ch);
    Log.log("characteristics.size="+characteristics.size(), 0);
  }

  public vtkActor getActor(Characteristics ch) {
    int index = characteristics.indexOf(ch);
    Log.log("index="+index, 0);
    if (index == -1) return null;
    return (vtkActor)elementAt(index);
  }

  public void clear() {
    removeAllElements();
    characteristics.removeAllElements();
  }

  /*
  public double closest(double mjd) {
    Object obj = getElement(mjd);
    if (obj != null) return mjd;
    double res = Double.MIN_VALUE;
    double mjd1;
    Enumeration e = keys.elements();
    while (e.hasMoreElements()) {
      mjd1 = ((Double)e.nextElement()).doubleValue();
      if (Math.abs(res-mjd1) < Math.abs(res-mjd)) res = mjd1;
    }
    return res;
  }

}*/
