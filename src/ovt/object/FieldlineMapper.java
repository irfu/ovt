/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/FieldlineMapper.java,v $
  Date:      $Date: 2009/10/27 12:14:36 $
  Version:   $Revision: 2.5 $


Copyright (c) 2000-2003 OVT Team
(Kristof Stasiewicz, Mykola Khotyaintsev, Yuri Khotyaintsev)
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

OVT Team (https://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
Khotyaintsev

=========================================================================*/

package ovt.object;

import ovt.*;
import ovt.gui.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.beans.editor.*;
import ovt.datatype.*;
import ovt.interfaces.*;


import vtk.*;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;


/**
 *
 * @author  ko
 */
public class FieldlineMapper extends SingleActorObject implements
        MagPropsChangeListener, TimeChangeListener, CoordinateSystemChangeListener, MenuItemsSource {
    public static final int DEBUG = 8;

    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;

    /** Holds Fieldline structure objects. Array index represents time. */
    private FLStructure[] fl_structure;
    private Vector startPoints = new Vector();
    /** Holds value of property representation. */
    private int representation = RepresentationEditor.WIREFRAME;

    private boolean customizerVisible = false;
    private FieldlineMapperCustomizerWindow customizer = null;



private PointsDataModel dataModel;
public DefaultTableModel getStartPointsDataModel() {
    return dataModel;
}

/** Holds value of property bindCS. */
private int bindCS = CoordinateSystem.GEO;

/** Creates a new instance of FieldlineMapper  */
public FieldlineMapper(OVTCore core) {
    super(core,"noname");
    try {
        setIcon(new ImageIcon(Utils.findResource("images/fl_mapper.gif")));
    } catch (FileNotFoundException e2) { e2.printStackTrace(System.err); }

    setColor(new Color(227,153,255));
    dataModel = new PointsDataModel();
    fl_structure = new FLStructure[getNumberOfMjdValues()];
    // registering ourselvs as listeners
    // do not forget to unregister when removeing ourselvs (method dispose())
    getCore().getTimeSettings().addTimeChangeListener(this);
    getCore().getMagProps().addMagPropsChangeListener(this);
    getCore().getCoordinateSystem().addCoordinateSystemChangeListener(this);
    setEnabled(false);
}

public Vector getStartPointsVector() {
    return startPoints;
}
/** for XML */
public FieldlineGBStartPoint[] getStartPoints() {
    Object[] objArray = startPoints.toArray();
    FieldlineGBStartPoint[] res = new FieldlineGBStartPoint[objArray.length];
    for (int i=0; i<res.length; i++) res[i] = (FieldlineGBStartPoint)objArray[i];
    return res;
}

/** for XML*/
public void setStartPoints(FieldlineGBStartPoint[] points) {
    //startPoints.removeAllElements();
    dataModel.setRowCount(points.length);
    for (int i=0; i<points.length; i++) {
        FieldlineGBStartPoint point = points[i];
        startPoints.addElement(point);
        dataModel.setValueAt(point.getID(), i, 0);
        dataModel.setValueAt(""+point.getLat(), i, 1);
        dataModel.setValueAt(""+point.getLon(), i, 2);
    }
}

public void fireStartPointsChanged() {
    setEnabled(startPoints.size() > 0);
    dataModel.fireTableDataChanged();
}

protected void validate() {
   Log.log("Fieldline Mapper ...", DEBUG);

   // take start points from dataModel
   // and recalculate only fieldlines for new points!

   FLStructure struct = fl_structure[getMjdIndex()];
   if (struct == null) fl_structure[getMjdIndex()] = new FLStructure(this);
   actor = fl_structure[getMjdIndex()].getActor();
   super.validate();
}

/** Getter for property representation.
 * @return RepresentationEditor.WIREFRAME or RepresentationEditor.SURFACE.
 */
public int getRepresentation() {
  return representation;
}

/** Setter for property representation (RepresentationEditor.WIREFRAME or RepresentationEditor.SURFACE).
 * @param representation New value of property representation.
 * @see ovt.beans.editor.RepresentationEditor
 */
public void setRepresentation(int representation) {
  this.representation = representation;
  if (actor != null) actor.GetProperty().SetRepresentation(representation);
  firePropertyChange("representation", null, null);
}

/** This function compares this.startPoints and dataModel startPoints
 * (those wich were specified by user in GUI). If they are different -
 * this object is invalidated,
 * this.startPoints are re-filled with the dataModel startPoints and
 * all structures are marked as having wrong startPoints.
 * Returns true if startPoints were different */
public boolean updateStartPoints()   {
  boolean data_differs = false;
  //Log.log("getMjdIndex()="+getMjdIndex(),DEBUG);
  FLStructure struct = fl_structure[getMjdIndex()];

  int numberOfDataModelPoints = dataModel.getRowCount();
  Vector dataModelPoints = new Vector();

  for (int i=0; i<numberOfDataModelPoints; i++) {
    String id = (String)dataModel.getValueAt(i,0);
    Object lat_obj = dataModel.getValueAt(i,1);
    Object lon_obj = dataModel.getValueAt(i,2);
    if ("".equals(lat_obj) || "".equals(lon_obj)) continue; // skip empty elements
    double lat = ((Double)lat_obj).doubleValue();
    double lon = ((Double)lon_obj).doubleValue();
    Log.log("adding lat,lon="+lat+","+lon, DEBUG);
    dataModelPoints.addElement(new FieldlineGBStartPoint(lat,lon,id));
  }

  // update numberOfDataModelPoints taking into account that some elements
  // in dataModel were empty
  numberOfDataModelPoints = dataModelPoints.size();

  if (startPoints.size() != numberOfDataModelPoints) {
      // there is no need to compare two vectors if they have different size ;-)
      // They are NOT EQUAL in this case. For sure.!!!
      data_differs = true;
  } else {
      // compare two vectors elementwise
      Enumeration e1 = startPoints.elements();
      Enumeration e2 = dataModelPoints.elements();
      while (e1.hasMoreElements()) {
        PolarCoord pc1 = (PolarCoord)e1.nextElement();
        PolarCoord pc2 = (PolarCoord)e2.nextElement();
        if (!pc1.equals(pc2)) {
            data_differs = true;
            break;
        }
      }
  }

  if (data_differs) {
    startPoints.removeAllElements();
    startPoints.addAll(dataModelPoints);

    invalidate(); // invalidate myself
    int maxMjdIndex = getMaxMjdIndex();
    for (int i=0; i<=maxMjdIndex; i++)
        if (fl_structure[i] != null) fl_structure[i].invalidateStartPoints();
    if (numberOfDataModelPoints != 0  && !isEnabled()) setEnabled(true);
    else if (numberOfDataModelPoints == 0  && isEnabled()) setEnabled(false);

    if (numberOfDataModelPoints == 1) setRepresentation(RepresentationEditor.WIREFRAME);
  }

  return data_differs;
}




public boolean isCustomizerVisible() {
    return this.customizerVisible;
}

public void setCustomizerVisible(boolean customizerVisible)
{
    boolean oldCustomizerVisible = this.customizerVisible;
    this.customizerVisible = customizerVisible;
    if (customizerVisible  &&  customizer == null) {
        customizer = new FieldlineMapperCustomizerWindow();
        customizer.setObject(this);
    }
    if (customizer != null) customizer.setVisible(customizerVisible);
    propertyChangeSupport.firePropertyChange ("customizerVisible", new Boolean (oldCustomizerVisible), new Boolean (customizerVisible));
}


/** Getter for property bindCS.
 * @return Value of property bindCS.
 */
public int getBindCS() {
  return bindCS;
}

/** Setter for property bindCS.
 * @param bindCS New value of property bindCS.
 */
public void setBindCS(int bindCS) {
  if ((bindCS != CoordinateSystem.GEO)  &&  (bindCS != CoordinateSystem.SM))
      throw new IllegalArgumentException("Wrong CS ("+CoordinateSystem.getCoordSystem(bindCS)+")");
  int oldBindCS = this.bindCS;
  this.bindCS = bindCS;
  propertyChangeSupport.firePropertyChange ("bindCS", new Integer (oldBindCS), new Integer (bindCS));
}


  public void magPropsChanged(MagPropsEvent evt) {
    invalidate();
    //Characteristics ch = getMagProps().getMagFieldCharacteristics(mjd);
    if (isVisible()) { hide(); show(); }
  }

  public void timeChanged(TimeEvent evt) {
      invalidate();
      if (evt.timeSetChanged())
          fl_structure = new FLStructure[getNumberOfMjdValues()];

      if (isVisible()) {
        hide(); show();
      } //else rotate();

  }

  public void coordinateSystemChanged(CoordinateSystemEvent evt) {
    if (isVisible()) rotate();
  }

  public void rotate() {
    if (actor !=null) {
        Matrix3x3 m3x3 = getTrans(getMjd()).gsm_trans_matrix(getCS());
        actor.SetUserMatrix(m3x3.getVTKMatrix());
    }
  }

  protected void show() {
    super.show();
    setRepresentation(getRepresentation());
    setColor(getColor());
    rotate();
  }


  public JMenuItem[] getMenuItems() {
        final int ITEM_COUNT = 3;
        JMenuItem[] item = new JMenuItem[ITEM_COUNT];
        int i=-1;

        item[++i] = new JMenuItem("Remove");
        item[i].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                getCore().getChildren().removeChild(FieldlineMapper.this);
                getCore().getChildren().fireChildRemoved(FieldlineMapper.this);
            }
        });

        item[++i] = null; // Separator

        item[++i] = new JMenuItem("Properties...");
        item[i].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setCustomizerVisible(true);
            }
        });

        for (i=0; i<ITEM_COUNT; i++) {
            if (item[i] != null) item[i].setFont(Style.getMenuFont());
        }
        return item;
  }

  public void dispose() {
    Log.log("Disposing.................................", DEBUG);
    getCore().getTimeSettings().removeTimeChangeListener(this);
    getCore().getMagProps().removeMagPropsChangeListener(this);
    getCore().getCoordinateSystem().removeCoordinateSystemChangeListener(this);
    setVisible(false);
    customizer.dispose();
    super.dispose();
  }


public Descriptors getDescriptors() {
    if (descriptors == null) {
        descriptors = super.getDescriptors();
        try {

            // representation property descriptor
            BasicPropertyDescriptor pd = new BasicPropertyDescriptor("representation", this);
            pd.setDisplayName("Representation:");
            MenuPropertyEditor representationEditor = new MenuPropertyEditor(pd,
                new int[]{ RepresentationEditor.POINT, RepresentationEditor.WIREFRAME, RepresentationEditor.SURFACE},
                new String[]{ "Point", "Wireframe", "Surface" }
            );
            // Render each time user changes time by means of gui
            representationEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            pd.setPropertyEditor(representationEditor);
            descriptors.put(pd);
            addPropertyChangeListener("representation", representationEditor);
            addPropertyChangeListener("enabled", representationEditor);

        } catch (IntrospectionException e2) {
            e2.printStackTrace();
            System.exit(0);
        }
    }
    return descriptors;
}

private class PointsDataModel extends javax.swing.table.DefaultTableModel {
    String geo_col_names[] = {"id", "lat [deg]", "lon [deg]"};
    String sm_col_names[] = {"id", "mlat [deg]", "mlt [h]"};
    PointsDataModel() {
        super(0, 3);
        this.insertRow(0, new Object[]{"loc1", "", ""});
    }

    public String getColumnName(int col) {

        switch (bindCS) {
            case CoordinateSystem.GEO : return geo_col_names[col];
            case CoordinateSystem.SM : return sm_col_names[col];
        }
        throw new IllegalArgumentException("Wrong bindCS. Don't know column names!");
    }

    /** 0 colun is point_id (string), 1-static and 2-nd are lat,lon/ mlt,mlon*/
    public void setValueAt(Object value, int row, int col) {
      Log.log("setValueAt("+value+",row="+ row +",col="+ col +") ", DEBUG);
      // this should be implemented in TableCellEditors! They
      // should care about validation of input
      if (col == 1 || col == 2) {
         // "" is also an acceptable value
         if ("".equals(value)) {
            super.setValueAt(value, row, col); return;
         }

         Double doubleObj;
         double doubleVal;
         // check format
         try {
            doubleObj = new Double((String)value);
            doubleVal = doubleObj.doubleValue();
         } catch (NumberFormatException e) {
            Log.log("Invalid number format", DEBUG);
            return;
         }

         if (bindCS == CoordinateSystem.SM) {
            // mlt
            if (col == 2 && (doubleVal <0 || doubleVal > 24) ) {
                Log.log("Specified value is out of range", DEBUG);
                return;
            }
         }
         super.setValueAt(doubleObj, row, col);
    } else // col == 0 (ID)
        super.setValueAt(value, row, col);

  }
}


}



/**
 * CLASS GBFieldline  (GroundBasedFieldline)
 */

class GBFieldline {

    public double[] startPolar;
    public Fieldline fl;

    GBFieldline() {}
}

class FLStructure {

    private vtkActor actor;
    public Hashtable fieldlines = new Hashtable();
    protected boolean startpoints_are_valid = false;
    private boolean mag_field_is_valid = false;

    private FieldlineMapper fl_mapper;

    FLStructure(FieldlineMapper fl_mapper) {
        this.fl_mapper = fl_mapper;
    }

    public void putFL(PolarCoord start, Fieldline fl) {
        fieldlines.put(start, fl);
    }

    public Fieldline getFL(PolarCoord start) {
        return (Fieldline)fieldlines.get(start);
    }

    public int getFLCount() {
        return fieldlines.size();
    }

    /** This method should be executed when magnetic field model was changed */
    public void invalidateMagField() {
        this.mag_field_is_valid = false;
    }


    public boolean isValid() {
        return mag_field_is_valid && startpoints_are_valid;
    }

    /** Marks the structure as it's fieldlines start points
     * are invalid.
     */
    public void invalidateStartPoints() {
        startpoints_are_valid = false;
    }

    /** Checks if we contain unnecessary fieldlines and removes them.
     * Unnesessary fieldlines are such that do not correspond to any start point
     * specified by user.
     * Returns true if something was removed
     */
    public boolean removeUnnecessaryFieldlines() {
        boolean something_was_removed = false;
        Enumeration e = fieldlines.keys();
        Vector startPoints = fl_mapper.getStartPointsVector();
        while (e.hasMoreElements()) {
            PolarCoord pc =  (PolarCoord)e.nextElement();
            if (!startPoints.contains(pc)) {
                Log.log("removeing "+pc);
                fieldlines.remove(pc);
                something_was_removed = true;
            }
        }
        return something_was_removed;
    }

    public vtkActor getActor() {
        if (!isValid()) validate();
        return actor;
    }


    public void validate() {

       if (!mag_field_is_valid) {
           fieldlines.clear();
       } else if (!startpoints_are_valid) {
           removeUnnecessaryFieldlines();
       }

       startpoints_are_valid = true;
       mag_field_is_valid = true;

       Vector startPoints = fl_mapper.getStartPointsVector();

       int minNumOfPoints = Integer.MAX_VALUE;
       int maxNumOfPoints = 0; int X=0,Y=1,Z=2;
       double mjd = fl_mapper.getMjd();
       Trans trans = fl_mapper.getTrans(mjd);
       int n = startPoints.size();

       Fieldline[] fl = new Fieldline[n];
       Enumeration e = startPoints.elements();

       double alt=100; // altitude (km)
       double alt_RE = 0.02;
       int i=0;
       while (e.hasMoreElements()) {
          PolarCoord startPoint = (PolarCoord)e.nextElement();
          fl[i] = getFL(startPoint);

          if (fl[i] == null) {
              //Log.log("fieldline starting from "+startPoint+" not found in fls["+fieldlines.size()+"]");
              double[] gma = null;
              double[] gsm = null;

              if (fl_mapper.getBindCS() == CoordinateSystem.GEO) {
                  double[] geo = Trans.lat_lon2xyz(startPoint.lat, startPoint.lon, alt_RE);
                  gsm = trans.geo2gsm(geo);
                  gma = trans.geo2gma(geo, Trans.ECCENTRIC_DIPOLE);
              } else if ( fl_mapper.getBindCS() == CoordinateSystem.SM ) {
                  gma = Trans.mlat_mlt2xyz(startPoint.lat, startPoint.lon, alt_RE);
                  gsm = trans.sm_gsm_trans_matrix().multiply(gma);
              }

              // determine the direction of tracing on the pole (north Z>0, south Z<0)
              double step = (gma[Z] > 0) ? -1. : 1.;
              Log.log("Step="+step, FieldlineMapper.DEBUG);

              fl[i] = Trace.traceline(fl_mapper.getMagProps(), mjd, gsm, step, Trace.SQRT_STEP_SIZE, alt);
              Log.log("1-static guess fl0.size=" + fl[i].size(), FieldlineMapper.DEBUG);
              // if the line has less than 3 points - may be we have wrongly
              // determined the tracing direction
              if (fl[i].size() < 3) {
                step = -1.*step;
                Fieldline oposit_tracing_direction_fl = Trace.traceline(fl_mapper.getMagProps(), mjd, gsm, step, Trace.SQRT_STEP_SIZE, alt);
                if (oposit_tracing_direction_fl.size() > fl[i].size())
                    fl[i] = oposit_tracing_direction_fl;
                Log.log("2-nd guess fl0.size=" + oposit_tracing_direction_fl.size(), FieldlineMapper.DEBUG);
              }
              fieldlines.put(startPoint, fl[i]);
          }
          if (fl[i].size() < minNumOfPoints) minNumOfPoints = fl[i].size();
          if (fl[i].size() > maxNumOfPoints) maxNumOfPoints = fl[i].size();
          i++;
       }

       //minNumOfPoints = 8;

       vtkPoints points = new vtkPoints();

       MagPoint p; double[] r = null;

       for (i=0; i<=n; i++) {
         if (i!=n) e = fl[i].elements();
         else e = fl[0].elements(); //add 1-static float once more
         for (int j=0; j<minNumOfPoints; j++) {
             p = (MagPoint)e.nextElement();
             r = p.gsm;
             points.InsertNextPoint(r[X], r[Y], r[Z]);
         }
       }

       vtkStructuredGrid grid = new vtkStructuredGrid();
            grid.SetPoints(points);
            grid.SetDimensions(minNumOfPoints, n+1, 1);
            //grid.GetCellData().SetScalars(scalars);
    /*
    for (int i=0; i<n; i++) {
         e = fl[i].elements();
         for (int j=0; j<fl[i].size(); j++) {
             p = (MagPoint)e.nextElement();
             r = p.gsm;
             points.InsertNextPoint(r[X], r[Y], r[Z]);
         }
         // add the last point to fill the length to maxNumOfPoints
         for (int j=fl[i].size(); j<maxNumOfPoints; j++) {
             points.InsertNextPoint(r[X], r[Y], r[Z]);
         }
       }

       vtkStructuredGrid grid = new vtkStructuredGrid();
            grid.SetPoints(points);
            grid.SetDimensions(maxNumOfPoints, n, 1);
    */

       vtkStructuredGridGeometryFilter filter = new vtkStructuredGridGeometryFilter();
            filter.SetInputData(grid);

            //filter.SetExtent(0, dateCount - 1, 0, minNumOfPoints - 1, 1, 1);

        vtkPolyDataMapper mapper = new vtkPolyDataMapper();
        //mapper.SetInputData(filter.GetOutput()); //FKJN 8/5 2015 changed all AddInputData & SetInputData to ***InputConnection
        mapper.SetInputConnection(filter.GetOutputPort());
        //mapper.SetScalarModeToUseCellData();
        //    mapper.ScalarVisibilityOn();
        //mapper.SetScalarRange(min, max);
        //mapper.SetLookupTable(dataModule.getLookupTable());


        actor = new vtkActor();
            actor.SetMapper(mapper);
            actor.GetProperty().SetRepresentationToSurface();
            float[] rgb = ovt.util.Utils.getRGB(fl_mapper.getColor());
            actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);


    }


}
