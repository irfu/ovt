/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/util/IndexedSegmentsCache.java $
 Date:      $Date: 2015/09/15 11:54: $
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

 OVT Team (https://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
 Khotyaintsev, E. P. G. Johansson, F. Johansson

 =========================================================================*/
package ovt.util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.math.RoundingMode;
import java.util.List;

/**
 * Uses DiscreteIntervalToListCache to implement a cache for (1D) "array-like
 * data structures". Data consists of one data point for every "t value"
 * (double) and a cache slot cover an range t values. t values are assumed to be
 * sorted and monotonically increasing. Every cache slot (an indexed object in
 * IndexObjectsCache) covers a specific interval of t values. For the purposes
 * of OVT, "t" likely represents time, but it can in principle represent
 * anything. The user of the class has to implement methods for splitting up
 * data into cache slots and putting data together from cache slots.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015
 */
public class IndexedSegmentsCache {

    /**
     * Effectively extra t margin when requesting for a t interval of data from
     * the underlying (internal) cache. Meant to be temporary. Setting it to
     * zero should in principle work but it is known to trigger bugs.
     *
     * NOTE: This constant is not intended for configuring proactive caching.
     * Use the right constructor parameter instead.
     *
     * Remove?
     */
    private static final double T_REQUEST_MARGIN = 0.0001;
    private static final int DEBUG = 4;   // Set the log message level for this class.

    private final DiscreteIntervalToListCache<CacheSlotContents> objectsCache;
    private final double t_slotSize;
    private final double t_beginAvailDataInclusive;      // Lowest possible t value for which there may be data.
    private final double t_endAvailDataInclusive;        // Highest possible t value for which there may be data.
    private final int slot_beginAvailDataInclusive;      // Lowest possible slot for which there may be data.
    private final int slot_endAvailDataInclusive;        // Highest possible slot for which there may be data.
    private final transient DataSource dataSource;

    //##########################################################################
    /**
     * Contents to put in a cache slot. Instances should be treated as IMMUTABLE
     * even if they are technically not.
     */
    public interface CacheSlotContents extends Serializable {

        public double[] getTArray();
    }

    //##########################################################################
    public interface DataSource {

        /**
         * Produce cache slot contents from data.
         *
         * NOTE: An implementing class must NOT throw exception merely because
         * it is known that there is no data for the specified interval, e.g.
         * data gap or outside some natural interval for which there is data.
         * Instead it must return something that represents the absence of data.
         *
         * @return
         */
        public List<CacheSlotContents> getCacheSlotContents(
                int i_beginInclusive,
                int i_endExclusive,
                Object getCacheSlotContentsArgument) throws IOException;


        /**
         * Take contents from multiple cache slots and extract and put together
         * the requested data.
         */
        public Object getDataFromCacheSlotContents(
                List<CacheSlotContents> slotContentsList,
                int i_beginSlotTArrayInclusive,
                int i_endSlotTArrayExclusive);
    }

    //##########################################################################
    /**
     * Exception thrown when asking for a non-existent t position in a cache.
     */
    public static class NoSuchTPositionException extends Exception {

        public NoSuchTPositionException(String msg) {
            super(msg);
        }
    }

    //##########################################################################

    /**
     * Constructor.
     */
    public IndexedSegmentsCache(
            DataSource mDataSource,
            double mT_beginAvailDataInclusive, double mT_endAvailDataInclusive,
            double t_slotSize,
            int proactiveFillMargin) {

        if (mDataSource == null) {
            throw new NullPointerException("Data source is null.");
        } else if (t_slotSize <= 0) {
            throw new IllegalArgumentException();
        } else if (!(mT_beginAvailDataInclusive < mT_endAvailDataInclusive)) {
            throw new IllegalArgumentException();
        }

        this.dataSource = mDataSource;
        this.objectsCache = new DiscreteIntervalToListCache(new InternalCacheDataSource(), proactiveFillMargin);

        this.t_slotSize = t_slotSize;
        this.t_beginAvailDataInclusive = mT_beginAvailDataInclusive;
        this.t_endAvailDataInclusive = mT_endAvailDataInclusive;
        this.slot_beginAvailDataInclusive = getCacheSlotIndex(mT_beginAvailDataInclusive);
        this.slot_endAvailDataInclusive = getCacheSlotIndex(mT_endAvailDataInclusive);
    }


    /**
     * Constructor. NOTE: Reuses old slot size.
     */
    public IndexedSegmentsCache(
            ObjectInput in,
            DataSource mDataSource,
            int proactiveFillMargin) throws IOException {

        if (mDataSource == null) {
            throw new NullPointerException("Data source is null.");
        }

        this.dataSource = mDataSource;
        this.objectsCache = new DiscreteIntervalToListCache(in, new InternalCacheDataSource(), proactiveFillMargin);
        t_slotSize = in.readDouble();
        t_beginAvailDataInclusive = in.readDouble();
        t_endAvailDataInclusive = in.readDouble();
        slot_beginAvailDataInclusive = in.readInt();
        slot_endAvailDataInclusive = in.readInt();
    }


    public void writeToStream(ObjectOutput out) throws IOException {
        objectsCache.writeToStream(out);
        //out.writeObject(objectsCache) should NOT work since we are not using serialization.
        out.writeDouble(t_slotSize);
        out.writeDouble(t_beginAvailDataInclusive);
        out.writeDouble(t_endAvailDataInclusive);
        out.writeInt(slot_beginAvailDataInclusive);
        out.writeInt(slot_endAvailDataInclusive);
    }


    /**
     * Return the t interval period for which this cache slot MAY contain data.
     * The method should be consistent with int getCacheSlotIndex(double
     * mjd).<BR>
     *
     * NOTE: The returned values do NOT necessarily refer to the time span of
     * the data that is actually in the cache slot. There might not be any data
     * for the given time interval data or only partially because it is at the
     * beginning/end of the available time series (at SSC Web Services). This is
     * what "Span" refers to in the method name, as opposed to e.g. "data". <BR>
     *
     * NOTE: The min value should be regarded as inclusive, while the max value
     * is exclusive.<BR>
     *
     * NOTE: The method is independent of any preexisting cache slots since it
     * needs to be called when creating them.<BR>
     */
    public double[] getCacheSlotSpanMjd(int i) {
        return new double[]{i * t_slotSize, (i + 1) * t_slotSize};
    }


    public int getCacheSlotIndex(double t) {
        if ((t < (Integer.MIN_VALUE / t_slotSize)) || ((Integer.MAX_VALUE / t_slotSize) < t)) {
            throw new RuntimeException("Can not convert modified Julian Day (mjd) value to int."
                    + "This (probably) indicates a bug.");
        }
        return (int) Math.floor(t / t_slotSize);  // NOTE: Typecasting with (int) implies rounding toward zero, not negative infinity.
    }


    public double getSlotSize() {
        return t_slotSize;
    }


    public void setProactiveCachingFillMargin(int mProactiveCachingFillMargin) {
        this.objectsCache.setProactiveCachingFillMargin(mProactiveCachingFillMargin);
    }


    public int getNbrOfFilledCacheSlots() {
        return this.objectsCache.getNbrOfFilledCacheSlots();
    }


    /*public void setCachingEnabled(boolean cachingEnabled) {
     this.objectsCache.setCachingEnabled(cachingEnabled);
     }*/
    //
    //
    /**
     * Get data in the pre-cache data format.
     *
     * The implementation fetches data from the cache and merges data from the
     * required cache slots.<BR>
     *
     * @param beginIndexMargin Number of data points to additionally include
     * BEFORE the stated t interval.
     * @param endIndexMargin Number of data points to additionally include AFTER
     * the stated t interval.
     *
     * @return Object representing the data in the specified t range.
     *
     * @throws If the requested start/stop positions do not exist), then
     * NoSuchTPositionException will be thrown.
     */
    public Object getData(
            double t_beginInclusive, double t_endInclusive,
            RoundingMode tBeginRoundingMode, RoundingMode tEndRoundingMode,
            int beginIndexMargin, int endIndexMargin,
            java.util.function.Predicate acceptCacheSlotContentsFunction,
            Object newCacheSlotContentsArgument)
            throws IOException, NoSuchTPositionException {

        // Argument check
        if (beginIndexMargin < 0) {
            throw new RuntimeException("beginIndexMargin < 0. This indicates a pure software bug in OVT.");
        } else if (endIndexMargin < 0) {
            throw new RuntimeException("endIndexMargin < 0. This indicates a pure software bug in OVT.");
            // Good to check for beginIndexMargin in particular in case
            // the caller thinks it should be a non-positive number.
        }

        Log.log(this.getClass().getSimpleName()
                + ".getData("
                + "t_beginInclusive=" + t_beginInclusive
                + ", t_endInclusive=" + t_endInclusive
                + ", beginIndexMargin=" + beginIndexMargin
                + ", endIndexMargin=" + endIndexMargin
                + ", newCacheSlotContentsArgument=" + newCacheSlotContentsArgument + ")", DEBUG);

        /*======================================================================
         Handle cases of t interval (without index margins) overlapping with
         outside of available data t interval.
         NOTE: Must move both t_begin/end values toward nearest begin/end boundary
         for available data since index margins have not been applied yet.
         ======================================================================*/
        t_beginInclusive = Math.max(t_beginInclusive, t_beginAvailDataInclusive);
        t_beginInclusive = Math.min(t_beginInclusive, t_endAvailDataInclusive);
        t_endInclusive = Math.max(t_endInclusive, t_beginAvailDataInclusive);
        t_endInclusive = Math.min(t_endInclusive, t_endAvailDataInclusive);

        {
            /*=================================================================
             Construct initial GUESS for which cache slots to load.
             Add margins to the time interval to make it likely that all needed
             cache slots are included on the first attempt (first request).
             =================================================================*/
            final int slot_beginInclusive = getCacheSlotIndex(t_beginInclusive - T_REQUEST_MARGIN);
            final int slot_endExclusive = getCacheSlotIndex(t_endInclusive + T_REQUEST_MARGIN) + 1;

            // Request slot contents once to trigger the caching of as many slots in sequence as possible.
            // NOTE: The return value is NOT used.
            this.objectsCache.getList(
                    slot_beginInclusive,
                    slot_endExclusive, // Argument should be exclusive upper bound.
                    acceptCacheSlotContentsFunction,
                    newCacheSlotContentsArgument);
        }

        /*=============================================================
         Construct initial interval of slots to actually use for sure.
         Could be a larger interval, but it is unlikely if
         beginEndIndexMargin<<(number of data points per cache slot).
         =============================================================*/
        final int[] tpos1 = getCacheTPosition(
                t_beginInclusive, -beginIndexMargin, tBeginRoundingMode,
                acceptCacheSlotContentsFunction, newCacheSlotContentsArgument);
        final int[] tpos2 = getCacheTPosition(
                t_endInclusive, endIndexMargin, tEndRoundingMode,
                acceptCacheSlotContentsFunction, newCacheSlotContentsArgument);

        if ((tpos1 == null) | (tpos2 == null)) {
            throw new NoSuchTPositionException(
                    "Can not satisfy the request. "
                            + "The requested positions do not exist (rounding to "
                            + "non-existing position, or index margins are too great).");
        }

        final int slot_beginInclusive = tpos1[0];
        final int i_beginSlotTArrayInclusive = tpos1[1];
        final int slot_endInclusive = tpos2[0];
        final int i_endSlotTArrayExclusive = tpos2[1] + 1;

        /*===============================================================================
         Request slots contents AGAIN to make sure that we get exactly the ones we need.
         ===============================================================================*/
        final List<CacheSlotContents> requestedSlotContents = this.objectsCache.getList(
                slot_beginInclusive,
                slot_endInclusive + 1, // Argument should be exclusive upper bound.
                acceptCacheSlotContentsFunction,
                newCacheSlotContentsArgument);

        return dataSource.getDataFromCacheSlotContents(requestedSlotContents, i_beginSlotTArrayInclusive, i_endSlotTArrayExclusive);
    }

    //##########################################################################
    private class InternalCacheDataSource implements DiscreteIntervalToListCache.DataSource {

        @Override
        public List<CacheSlotContents> getList(
                int i_beginInclusive,
                int i_endExclusive,
                Object getListArgument) throws IOException {
            return IndexedSegmentsCache.this.dataSource.getCacheSlotContents(
                    i_beginInclusive,
                    i_endExclusive,
                    getListArgument);
        }
    }

    //##########################################################################

    /**
     * Find data position/indices (slot index and index into the cache slot's t
     * array).
     *
     * First (1) finds the position with the t value nearest the specified t
     * (given the specified rounding). Then (2) finds the position a number of
     * steps away from that position.
     *
     * @return {slot, i_slotTArray}. null if there is no such t value, i.e. if
     * (1) t is outside the available t interval and then trying to round "away"
     * (up/down), moving you away from the available t interval, or (2) one can
     * not step to the requested index because one goes outside the available t
     * interval.
     */
    private int[] getCacheTPosition(double t,
            int tIndexSteps,
            RoundingMode dataIndexRoundingMode,
            java.util.function.Predicate acceptCacheSlotContentsFunction,
            Object newCacheSlotContentsArgument)
            throws IOException {

        int[] tpos;
        {
            // Find the approximate (slot & t array index) position of t.
            // NOTE: The nearest t value might not be in the cache slot that covers t.
            final int slot = getCacheSlotIndex(t);
            final List<CacheSlotContents> requestedSlotContents = this.objectsCache.getList(
                    slot,
                    slot + 1,
                    acceptCacheSlotContentsFunction,
                    newCacheSlotContentsArgument);
            final double[] slotTArray = requestedSlotContents.get(0).getTArray();

            // NOTE: Utils.findNearestMatch returns -1 or slotTArray.length (e.g. 0 for empty array) if
            // there is no match (depending on rounding mode).
            final int i_slotTArray = Utils.findNearestMatch(slotTArray, t, dataIndexRoundingMode);

            // NOTE: Should work also for the results from i_slotTArray = Utils.findNearestMatch for an empty array.
            final int d = Utils.distanceFromInterval(0, slotTArray.length, i_slotTArray);   // Interval of TArray indices, not t values.

            if ((d != 0)) {
                tpos = findNextCachePositionOutsideSlot(slot, Integer.signum(d), acceptCacheSlotContentsFunction, newCacheSlotContentsArgument);
            } else {
                tpos = new int[]{slot, i_slotTArray};
            }

            // Check if actually found the first approximate t position.
            if (tpos == null) {
                return null;    // EXIT FUNCTION.
            }
        }

        // CASE: Found the first approximate t position.
        {
            final int slot = tpos[0];
            final int i_slotTArray = tpos[1] + tIndexSteps;
            final List<CacheSlotContents> requestedSlotContents = this.objectsCache.getList(
                    slot,
                    slot + 1,
                    acceptCacheSlotContentsFunction,
                    newCacheSlotContentsArgument);
            final double[] slotTArray = requestedSlotContents.get(0).getTArray();

            int d = Utils.distanceFromInterval(0, slotTArray.length, i_slotTArray);
            if (d == 0) {
                return new int[]{slot, i_slotTArray};
            } else {
                return findNextCachePositionOutsideSlot(slot, d, acceptCacheSlotContentsFunction, newCacheSlotContentsArgument);
            }
        }
    }


    /**
     * Find the location of the next t value relative to a given slot, ignoring
     * thespecified slot. Useful for implementing algorithms.
     *
     * @param tIndexSteps Number of T indices to iterate over. The sign
     * determines the search direction. +1/-1 finds the first t position as one
     * progresses to in higher/lower slots. Must not be zero.
     *
     * @return {slot, i_slotTArray}. null if there is no such position.
     */
    private int[] findNextCachePositionOutsideSlot(
            int slot,
            int tIndexSteps,
            java.util.function.Predicate acceptCacheSlotContentsFunction,
            Object newCacheSlotContentsArgument)
            throws IOException {

        // Argument check.
        if (tIndexSteps == 0) {
            throw new IllegalArgumentException("Illegal argument tIndexSteps = 0.");
        }

        slot = Math.max(slot, slot_beginAvailDataInclusive - 1);
        slot = Math.min(slot, slot_endAvailDataInclusive + 1);

        final int direction = Integer.signum(tIndexSteps);
        int i_slotTArray = tIndexSteps;  // Preliminary value / name change.
        if (i_slotTArray > 0) {
            // Needed to make array indices work out right.
            // For example, tIndexSteps = 1 refers to the first index
            // (index zero) in the first non-empty array in a higher slot.
            i_slotTArray--;
        }

        do {
            slot = slot + direction;

            // Check if there are any (slots with) t values in this search direction.
            // NOTE: Take direction into account in case code starts the
            //       iteration outside of the available data t interval.
            if (((direction == -1) & (slot < slot_beginAvailDataInclusive)) | ((direction == 1) & (slot_endAvailDataInclusive < slot))) {
                return null;   // Could find any t value. EXIT FUNCTION.
            }

            final List<CacheSlotContents> requestedSlotContents = this.objectsCache.getList(
                    slot,
                    slot + 1,
                    acceptCacheSlotContentsFunction,
                    newCacheSlotContentsArgument);
            final double[] slotTArray = requestedSlotContents.get(0).getTArray();

            // NOTE: If moved to a higher slot, then i_slotTArray is already adjusted to (potentially) mean index into t array.
            if (i_slotTArray < 0) {
                i_slotTArray += slotTArray.length;
            }

            if (Utils.distanceFromInterval(0, slotTArray.length, i_slotTArray) == 0) {
                return new int[]{slot, i_slotTArray};
            }

            if (slotTArray.length <= i_slotTArray) {
                i_slotTArray -= slotTArray.length;
            }
            // NOTE: i_slotTArray is now adjusted to (potentially) mean index in the next (higher) slot.

        } while (true);

    }

}
