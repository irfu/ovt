/*=========================================================================
 
 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/mag/OMNI2Data.java $
 Date:      $Date: 2015/09/15 13:17:00 $
 Version:   $Revision: 1.0 $
 
 
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ovt.util.SegmentsCache;
import ovt.util.Utils;

/**
 * Container for OMNI2 data over a certain period of time. Intended to be
 * TREATED AS IMMUTABLE after its construction (constructor plus
 * #setFieldArray), although it may return references to internal arrays which
 * the caller is then supposed to make sure are not altered.
 *
 * NOTE: An instance covers an explicitly specific period in time, which has to
 * be CONSISTENT with the data points in it, but the specified time period can
 * still be (much) larger than the time period spanned by the data points.
 *
 * IMPLEMENTATION NOTE: The class stores more values than required by OVT
 * (2015-10-13). This is to make it easy to change OVT to using an alternate
 * value if necessary (in particular other coordinate system for IMF, or other
 * Mach number).
 *
 * IMPLEMENTATION NOTE: The class was originally (2015-09-xx) made to handle
 * both double and integer fields (as arrays) and keep them separate but still
 * be able to iterate over both kinds of fields at the same time (using class
 * ArrayData). This was maybe a bit unnecessary in hindsight and the
 * functionality has been removed/commented away to simplify. Everything is now
 * scalar double fields.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015-09-xx
 */
// PROPOSAL: Let every field be an array of double[] arrays, not an array of double scalars.
//    PRO: More similar to how mag. props. are treated elswhere in the code (e.g. MagProps.java).
//       PRO: Can group magnetic field components together.
//          QUESTION: How handle both using GSE and GSM?
//          QUESTION: How handle fill values?
//             PROPOSAL: One fill value for all values (all x,y,z components) together.
//    CON: Less memory-efficient.
//    NOTE: Need new implementation of ArrayData.
public final class OMNI2Data implements SegmentsCache.DataSegment {

    /**
     * Identifiers for various fields in OMNI2 data. GSE, GSM refer to
     * coordinate systems. nT, nP, kms (=km/s) refer to units.
     */
    public enum FieldID {

        time_mjd,
        IMFx_nT_GSM_GSE,
        IMFy_nT_GSE,
        IMFz_nT_GSE,
        IMFy_nT_GSM,
        IMFz_nT_GSM,
        SW_ram_pressure_nP, // Not to be confused with "dynamic pressure".
        SW_velocity_kms,
        SW_M_A, // Alfven Mach number.
        SW_M_ms, // Magnetosonic Mach number
        Kp, // K_p index
        DST // DST=Disturbance Storm Time index
    }

    private final Map<FieldID, DoubleArray> dataFields = new HashMap();
    //private final Map<FieldID, DoubleArray> doubleFields = new HashMap();
    //private final Map<FieldID, DoubleArray2D> doubleFields = new HashMap();

    /**
     * Inclusive beginning of the time interval which the instance is supposed
     * to cover. Note that this value can not be exactly derived from the data.
     */
    private final double begin_mjd;
    /**
     * Exclusive end of the time interval which the instance is supposed to
     * cover. Note that this value can not be exactly derived from the data.
     */
    private final double end_mjd;


    //##########################################################################
    /**
     * Create instance representing specific time interval that is empty of data
     * points.
     */
    public OMNI2Data(double mBegin_mjd, double mEnd_mjd) {
        this(null, mBegin_mjd, mEnd_mjd);
    }


    /**
     * Creates instance with data for specific time interval.
     *
     * @param mDataFields Must contain one key and array for every possible
     * FieldID. All arrays must have the same length.
     */
    public OMNI2Data(double mBegin_mjd, double mEnd_mjd, Map<FieldID, double[]> mDataFields) {
        this(convertFields(mDataFields), mBegin_mjd, mEnd_mjd);
    }


    /**
     * The only "real constructor". Tries to implement all other constructors
     * using this constructor.
     *
     * IMPLEMENTATION NOTE: The only reason for the different order of
     * parameters is to avoid ambiguity with other the constructor due to how
     * type erasure for generics works (workaround for otherwise unavoidable
     * compilation errors).
     *
     * @param mDataFields Must contain one key and array for every possible
     * FieldID. All arrays must have the same length. If NULL, then all fields
     * will be initialized to have length zero.
     */
    private OMNI2Data(Map<FieldID, ArrayData> mDataFields, double mBegin_mjd, double mEnd_mjd) {
        if (!(mBegin_mjd < mEnd_mjd)) {
            throw new IllegalArgumentException("mEnd_mjd <= mBegin_mjd");
        }

        begin_mjd = mBegin_mjd;
        end_mjd = mEnd_mjd;

        if (mDataFields == null) {

            // Initialize empty internal fields.
            for (FieldID fieldID : FieldID.values()) {
                dataFields.put(fieldID, new DoubleArray(new double[0]));
            }

        } else {

            int permittedArrayLength = -1;
            for (FieldID fieldID : FieldID.values()) {

                // Argument checks
                if (!mDataFields.containsKey(fieldID)) {
                    throw new IllegalArgumentException("Argument omits field ID \"" + fieldID + "\".");
                }

                final DoubleArray doubleArray = (DoubleArray) mDataFields.get(fieldID);
                if (permittedArrayLength < 0) {
                    permittedArrayLength = doubleArray.length();
                }

                // Argument check
                if (doubleArray.length() != permittedArrayLength) {
                    throw new IllegalArgumentException("Arrays have different lengths.");
                }

                // Set internal value.
                dataFields.put(fieldID, doubleArray);
            }
        }

        // Argument check. 
        final double[] time_mjd = dataFields.get(FieldID.time_mjd).getArrayInternal();
        for (double t : time_mjd) {
            if ((t < mBegin_mjd) || (mEnd_mjd <= t)) // NOTE: Inclusive beginning boundary, exclusive end boundary.
            {
                throw new IllegalArgumentException("Data contains data points outside the stated time interval.");
            }
        }

        for (int i = 0; i < time_mjd.length; i++) {
            final double t = time_mjd[i];

            if ((i >= 1) && !(time_mjd[i - 1] < time_mjd[i])) {
                throw new IllegalArgumentException("Data points not sorted (monotonically increasing) in time.");
            }
        }

    }


    @Override
    public double[] getInterval() {
        return new double[]{begin_mjd, end_mjd};
    }


    /**
     * NOTE: Returns reference to internal copy for speed reasons but the caller
     * is not supposed to alter the array since that would alter the internal
     * state.
     */
    public final double[] getFieldArrayInternal(FieldID fieldID) {
        //return ((DoubleArray) dataFields.get(fieldID)).getArrayCopy();   // NOTE: Copies entire array.
        return ((DoubleArray) dataFields.get(fieldID)).getArrayInternal();   // NOTE: Returns reference to INTERNAL array.
    }


    /**
     * Create an instance using a subset of time from this existing instance.
     */
    @Override
    public SegmentsCache.DataSegment selectSubset(double mBegin_mjd, double mEnd_mjd) {
        if ((mBegin_mjd < this.begin_mjd) || (this.end_mjd < mEnd_mjd)) {
            throw new IllegalArgumentException("Specified time interval outside the time interval covered by this object.");
        }

        return createNew(mBegin_mjd, mEnd_mjd);
    }


    /**
     * Create an instance using data from this existing instance. Time covered
     * by the new instance, but not in this instance, will automatically be
     * empty in the new instance.
     *
     * NOTE: Useful for testing purposes, since can then use one OMNI2Data
     * instance as the basis for all test data and then extract time segments
     * from that for each request for data.
     */
    // PROPOSAL: Better name. reuseData? createNewUsingOldData?!
    public SegmentsCache.DataSegment createNew(double mBegin_mjd, double mEnd_mjd) {
        if (!(mBegin_mjd < mEnd_mjd)) {
            throw new IllegalArgumentException();
        }

        final int[] indexInterval = Utils.findInterval(getFieldArrayInternal(FieldID.time_mjd), mBegin_mjd, mEnd_mjd, true, false);

        final Map<FieldID, ArrayData> newDataFields = new HashMap();
        for (FieldID fieldID : FieldID.values()) {
            newDataFields.put(
                    fieldID,
                    dataFields.get(fieldID).select(
                            indexInterval[0],
                            indexInterval[1]));
        }

        return new OMNI2Data(newDataFields, mBegin_mjd, mEnd_mjd);
    }


    //##########################################################################
    /**
     * Utility function for being able to more conveniently recycle code in the
     * constructors, when one additional constructor existed. Can probably be
     * rationalized away.
     */
    // PROPOSAL: Rationalize away?
    private static Map<FieldID, ArrayData> convertFields(Map<FieldID, double[]> arrayDataFields) {
        final Map<FieldID, ArrayData> dataFields = new HashMap();

        /**
         * IMPLEMENTATION NOTE: Iterate over the keys in the keySet, NOT all
         * FieldID values. We only want to convert the Map, not do any argument
         * checking. Assertions should be checked elsewhere.
         */
        for (FieldID fieldID : arrayDataFields.keySet()) {
            dataFields.put(fieldID, new DoubleArray(arrayDataFields.get(fieldID)));
        }
        return dataFields;
    }


    /**
     * Merges multiple instances into one. Instances have to be sorted in time
     * and (exactly) adjacent in time. Useful when implementing caching.
     *
     * NOTE: Static method since no (pre-existing) instance of OMNI2Data is at
     * the center of attention.
     */
    public static OMNI2Data mergeAdjacent(List<OMNI2Data> dataList) {
        if (dataList.size() < 1) {
            throw new IllegalArgumentException("dataList is empty.");
        }

        /*=====================================================
         Check assertion: All data blocks are adjacent in time.
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
        final Map<FieldID, ArrayData> newDataFields = new HashMap();
        for (FieldID fieldID : FieldID.values()) {

            final List<ArrayData> arrayDataList = new ArrayList();
            for (OMNI2Data data : dataList) {
                arrayDataList.add(data.dataFields.get(fieldID));
            }
            newDataFields.put(fieldID, ArrayData.merge(arrayDataList));
        }

        return new OMNI2Data(
                newDataFields,
                dataList.get(0).begin_mjd,
                dataList.get(dataList.size() - 1).end_mjd);
    }

    //##########################################################################
    /**
     * Simple class that "models" an array and supplies some operations on the
     * array. Subclasses implement abstract methods which are specific for
     * specific kinds of array-like data, including arrays of primitives.
     * Abstract methods are chosen such that subclasses are easy to implement
     * also for arrays of primitives. Using this class makes it easy to iterate
     * over multiple arrays OF DIFFERENT TYPES and still perform analogues
     * operations on each of them.
     *
     * NOTE: Could (almost?) be an interface plus some static methods instead of
     * a (super)class.
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
                newAD.arrayCopyDeep(ad, 0, newAD, i_newBuf, ad.length());     // NOTE: Choosing to use newAD.arrayCopyDeep.
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
            arrayCopyDeep(this, i_beginIncl, newAD, 0, N_newBuf);
            return newAD;
        }


        /**
         * Analogous to System#arraycopy. System#arraycopy can also be used to
         * implement it for (at least) arrays of primitives. Assumes that both
         * arrays are of the same class as the instance ("this").
         *
         * Should make deep copies of the underlying array components (if they
         * themselves are objects/arrays).
         */
        protected abstract void arrayCopyDeep(ArrayData src, int srcPos, ArrayData dest, int destPos, int length);


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
            if (mArray == null) {
                throw new IllegalArgumentException("Using null as argument.");
            }
            array = mArray;
        }


        @Override
        protected void arrayCopyDeep(ArrayData src, int srcPos, ArrayData dest, int destPos, int length) {
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


        public double[] getArrayInternal() {
            return array;
        }
    }
//##########################################################################

    /**
     * Models an array of double arrays. ArrayData methods refer to operations
     * on the one "root" array.
     */
    private static class DoubleArray2D extends ArrayData {

        private final double[][] array;


        public DoubleArray2D(double[][] mArray) {
            array = mArray;
        }


        @Override
        protected void arrayCopyDeep(ArrayData mSrc, int srcPos, ArrayData mDest, int destPos, int length) {
            if (length < 0) {
                throw new IllegalArgumentException();
            }

            //System.arraycopy(((DoubleArray) src).array, srcPos, ((DoubleArray) dest).array, destPos, length);  // Shallow copy.
            final DoubleArray2D src = (DoubleArray2D) mSrc;
            final DoubleArray2D dest = (DoubleArray2D) mDest;

            for (int i = 0; i < length; i++) {
                final double[] srcRef = src.array[srcPos + i];
                if (srcRef == null) {
                    dest.array[destPos + i] = null;
                } else {
                    dest.array[destPos + i] = Arrays.copyOf(srcRef, srcRef.length);
                }
            }
        }


        @Override
        protected ArrayData newArray(int N) {
            return new DoubleArray2D(new double[N][]);   // NOTE: "new double[N][]" will create array of nulls.
        }


        @Override
        protected int length() {
            return array.length;
        }


        public double[][] getArrayCopy() {
            return Arrays.copyOf(array, array.length);
        }


        public double[][] getArrayInternal() {
            return array;
        }
    }//*/
//##########################################################################

    /*private static class IntArray extends ArrayData {

     private final int[] array;


     public IntArray(int[] mArray) {
     array = mArray;
     }


     @Override
     protected void arrayCopyDeep(ArrayData src, int srcPos, ArrayData dest, int destPos, int length) {
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
