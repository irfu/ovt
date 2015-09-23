/*=========================================================================
 
 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/mag/OMNI2Data.java $
 Date:      $Date: 2015/09/15 13:17:00 $
 Version:   $Revision: 1.0 $
 
 
 Copyright (c) 2000-2015 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev,
 Yuri Khotyaintsev, Erik P G Johansson, Fredrik Johansson)
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
 Khotyaintsev, E. P. G. Johansson, F. Johansson)
 
 =========================================================================*/
package ovt.mag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ovt.util.SegmentsCache;
import ovt.util.Utils;

/**
 * Container for OMNI2 data.
 *
 * IMPLEMENTATION NOTE: No constructor that initializes final Fields since it
 * would have such a long list of arguments which are easily confused with each
 * other, which is dangerous. On the other hand, a manual initialization may
 * forget to set fields.
 *
 * NOTE: An instance covers a specific period in time, which has to be
 * CONSISTENT with the data points in it, but the time period can still be much
 * larger.
 *
 * IMPLEMENTATION NOTE: The class was originally (2015-09-xx) made to handle
 * both double and integer fields and keep them separate but still be able to
 * iterate over both kinds of fields at the same time. This was maybe a bit
 * unnecessary in hindsight and the functionality has been removed/commented
 * away to simplify. Everything is now scalar double fields.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015-09-xx
 */
// QUESTION: Split x,y,z components of magnetic field to get fewer types of arrays?
// QUESTION: Tie class name to OMNI2?
// Q: Tie class to OMNI2?
// Q: Tie class to one request of OMNI2?
// Units, CS in variable names.
// Q: What coord sys for magnetic field?!
// PROPOSAL: Variables for time limits?
public class OMNI2Data implements SegmentsCache.DataSegment {

    public enum FieldID {

        time_mjd,
        IMFx_nT_GSE,
        IMFy_nT_GSE,
        IMFz_nT_GSE,
        pressure_nP,
        velocity_kms,
        M_A,
        M_ms,
        Kp, DST,
    }

    /*private static final EnumSet<FieldID> DOUBLE_FIELDS = EnumSet.of(
     FieldID.time_mjd,
     FieldID.IMFx_nT_GSE,
     FieldID.IMFy_nT_GSE,
     FieldID.IMFz_nT_GSE,
     FieldID.pressure_nP,
     FieldID.velocity_kms,
     FieldID.M_ms,
     FieldID.M_A,
     FieldID.M_ms,
     );*/
    /*private static final EnumSet<FieldID> INT_FIELDS = EnumSet.of(
     FieldID.Kp,
     FieldID.DST
     );*/
    private final Map<FieldID, ArrayData> dataFields = new HashMap();

    /**
     * Inclusive beginning of the time inteval which the instance is supposed to
     * cover. Note that this value can not be derived from the data.
     */
    private final double begin_mjd;
    /**
     * Exclusive end of the time inteval which the instance is supposed to
     * cover. Note that this valuecan not be derived from the data.
     */
    private final double end_mjd;


    //##########################################################################
    /**
     * Creates instance. Initializes empty data for specific time interval.
     */
    public OMNI2Data(double mBegin_mjd, double mEnd_mjd) {
        if (mEnd_mjd <= mBegin_mjd) {
            throw new IllegalArgumentException();
        }

        begin_mjd = mBegin_mjd;
        end_mjd = mEnd_mjd;

        // Initialize empty fields.
        for (FieldID fieldID : FieldID.values()) {
            dataFields.put(fieldID, new DoubleArray(new double[0]));
        }
        /*for (FieldID fieldID : DOUBLE_FIELDS) {
         dataFields.put(fieldID, new DoubleArray(new double[0]));
         }
         for (FieldID fieldID : INT_FIELDS) {
         dataFields.put(fieldID, new IntArray(new int[0]));
         }*/
    }


    @Override
    public double[] getInterval() {
        return new double[]{begin_mjd, end_mjd};
    }


    public void setFieldArray(FieldID fieldID, double[] array) {
        dataFields.put(fieldID, new DoubleArray(array));
    }

    /*public void setDoubleField(FieldID fieldID, double[] array) {
     if (!DOUBLE_FIELDS.contains(fieldID)) {
     throw new IllegalArgumentException();
     }
     dataFields.put(fieldID, new DoubleArray(array));
     }*/


    /*public void setIntField(FieldID fieldID, int[] array) {
     if (!INT_FIELDS.contains(fieldID)) {
     throw new IllegalArgumentException();
     }
     dataFields.put(fieldID, new IntArray(array));
     }*/
    public double[] getFieldArray(FieldID fieldID) {
        return ((DoubleArray) dataFields.get(fieldID)).getArrayCopy();
    }


    /**
     * Merges multiple instances into one. Instances have to be sorted in time
     * and adjacent in time.
     *
     * NOTE: Static method.
     */
    public static OMNI2Data mergeAdjacent(List<OMNI2Data> dataList) {
        if (dataList.size() < 1) {
            throw new IllegalArgumentException();
        }

        /* ====================================================
         Check assertion: all data blocks are adjacent in time.
         =====================================================*/
        OMNI2Data prevData = null;
        for (OMNI2Data data : dataList) {
            // Argument check
            if ((prevData != null) && (prevData.end_mjd != data.begin_mjd)) {
                throw new IllegalArgumentException("Blocks of data are not adjacent (and sorted) in time.");
            }
            prevData = data;
        }

        /*===================
         Create new instance
         ===================*/
        final OMNI2Data newData = new OMNI2Data(
                dataList.get(0).begin_mjd,
                dataList.get(dataList.size() - 1).end_mjd);
        for (FieldID fieldID : FieldID.values()) {

            final List<ArrayData> arrayDataList = new ArrayList();
            for (OMNI2Data data : dataList) {
                arrayDataList.add(data.dataFields.get(fieldID));
            }
            newData.dataFields.put(fieldID, ArrayData.merge(arrayDataList));
        }
        return newData;
    }


    /*==================================================
     Create new instance which covers a subset of time.
     ==================================================*/
    @Override
    public SegmentsCache.DataSegment select(double t_begin, double t_end) {
        if ((t_begin < begin_mjd) || (end_mjd < t_end)) {
            throw new IllegalArgumentException("Specifying t interval outside the spac covered by this object.");
        }

        final double[] timeArray_mjd = ((DoubleArray) dataFields.get(FieldID.time_mjd)).array;
        final int[] interval = Utils.findInterval(timeArray_mjd, t_begin, t_end, true, false);
        final OMNI2Data newData = new OMNI2Data(t_begin, t_end);

        for (FieldID fieldID : FieldID.values()) {

            newData.dataFields.put(
                    fieldID,
                    dataFields.get(fieldID).select(
                            interval[0],
                            interval[1]));
        }
        return newData;
    }

    //##########################################################################
    /**
     * Simple class that "models" an array and supplies some operations on the
     * array. Subclasses implement abstract methods which are specific for
     * specific kinds of array-like data, including pure Java arrays. Abstract
     * methods are chosen such that subclasses are easy to implement also for
     * arrays of primitives. Using this class makes it easy to iterate over
     * multiple arrays and perform the same operation on each of them despite
     * using different types of arrays.
     *
     * Could almost be an interface instead of a class.
     */
    // Move to Utils?
    private static abstract class ArrayData {

        /**
         * Create new ArrayData equivalent to the contents of several ArrayData
         * objects. Assumes that all ArrayData objects are of the same class.
         */
        public static ArrayData merge(List<ArrayData> adList) {
            if (adList.size() < 1) {
                throw new IllegalArgumentException();
            }

            int N_newAD = 0;
            for (ArrayData ad : adList) {
                N_newAD += ad.length();
            }
            final ArrayData newAD = adList.get(0).newArray(N_newAD);    // NOTE: Choosing to use component 0 for calling newArray.

            int i_newBuf = 0;
            for (ArrayData ad : adList) {
                newAD.arrayCopy(ad, 0, newAD, i_newBuf, ad.length());     // NOTE: Choosing to use newAD.arrayCopy.
                i_newBuf += ad.length();
            }

            return newAD;
        }


        /**
         * Return instance of the same class with a subset of the array
         * components.
         */
        public ArrayData select(int i_beginIncl, int i_endExcl) {
            final int N_newBuf = i_endExcl - i_beginIncl;

            final ArrayData newAD = newArray(N_newBuf);
            arrayCopy(this, i_beginIncl, newAD, 0, N_newBuf);
            return newAD;
        }


        /**
         * Analogous to System#arraycopy. Assumes that both arrays are of the
         * same class. NOTE: Does not work with the instance's internal fields,
         * but should work with instances of the same class as the class where
         * it is implemented.
         */
        protected abstract void arrayCopy(ArrayData src, int srcPos, ArrayData dest, int destPos, int length);


        /**
         * Create new ArrayData of the same class.
         */
        protected abstract ArrayData newArray(int N);


        protected abstract int length();
    }
    //##########################################################################

    private static class DoubleArray extends ArrayData {

        private final double[] array;


        public DoubleArray(double[] mArray) {
            array = mArray;
        }


        @Override
        protected void arrayCopy(ArrayData src, int srcPos, ArrayData dest, int destPos, int length) {
            System.arraycopy(((DoubleArray) src).array, srcPos, ((DoubleArray) dest).array, destPos, length);
        }


        @Override
        protected ArrayData newArray(int N) {
            return new DoubleArray(new double[N]);
        }


        @Override
        protected int length() {
            return array.length;
        }


        public double[] getArrayCopy() {
            return Arrays.copyOf(array, array.length);
        }
    }
    //##########################################################################

    /*private static class IntArray extends ArrayData {

     private final int[] array;


     public IntArray(int[] mArray) {
     array = mArray;
     }


     @Override
     protected void arrayCopy(ArrayData src, int srcPos, ArrayData dest, int destPos, int length) {
     System.arraycopy(((IntArray) src).array, srcPos, ((IntArray) dest).array, destPos, length);
     }


     @Override
     protected ArrayData newArray(int N) {
     return new IntArray(new int[N]);
     }


     @Override
     protected int length() {
     return array.length;
     }
     }*/
}
