/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ovt.util;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.List;

/**
 * Uses DiscreteIntervalToListCache to implement a cache for "array-like data
 * structures". Data consists of one data point for every "t value" (double). t
 * values are assumed to be sorted and monotonically increasing. Every cache
 * covers (an indexed object in IndexObjectsCache) covers a specific interval of
 * t values. For the purposes of OVT, "t" likely represents time, but it can in
 * principle represent anything. The user of the class has to implement methods
 * for splitting up data into cache slots and putting data together from cache
 * slots.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se
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
    private static final int DEBUG = 3;   // Set the minimum log message level for this class.

    private final DiscreteIntervalToListCache objectsCache;
    private final double t_beginAvailDataInclusive;      // Lowest possible t value for which there may be data.
    private final double t_endAvailDataInclusive;        // Highest possible t value for which there may be data.
    private final int slot_beginAvailDataInclusive;      // Lowest possible slot for which there may be data.
    private final int slot_endAvailDataInclusive;        // Highest possible slot for which there may be data.
    private final DataSource dataSource;

    //##########################################################################
    public interface CacheSlotContents {

        public double[] getTArray();
    }

    //##########################################################################
    public interface DataSource {

        /**
         * Get index for the cache slot that covers/contains this t value. This
         * how the user can split up the t range into cache slots. Cache slots
         * do not have to be constant in (t) width/size but have to be constant
         * in time.
         */
        public int getCacheSlotIndex(double t);


        /**
         * Produce cache slot contents from data.
         */
        public List<CacheSlotContents> getCacheSlotContents(
                int i_beginInclusive,
                int i_endExclusive,
                Object getCacheSlotContentsArgument) throws IOException;


        /**
         * Take contents from multiple cache slots and extract and put together
         * the requested data.
         *
         * Must return contents for arguments (null, 0, 0) corresponding to
         * empty data (no t values). This is used for requests for t values
         * outside of available range of t values. NOTE: This is somewhat
         * limiting on what data the cache can be used for, but is not relevant
         * for OVT currently.
         */
        public Object getDataFromCacheSlotContents(
                List<CacheSlotContents> slotContentsList,
                int i_beginSlotTArrayInclusive,
                int i_endSlotTArrayExclusive);
    }

    //##########################################################################

    /**
     * Constructor.
     */
    public IndexedSegmentsCache(
            DataSource mDataSource,
            double mT_beginAvailDataInclusive, double mT_endAvailDataInclusive,
            int proactiveFillMargin) {

        this.dataSource = mDataSource;
        this.objectsCache = new DiscreteIntervalToListCache(new InternalCacheDataSource(), proactiveFillMargin);

        this.t_beginAvailDataInclusive = mT_beginAvailDataInclusive;
        this.t_endAvailDataInclusive = mT_endAvailDataInclusive;
        this.slot_beginAvailDataInclusive = dataSource.getCacheSlotIndex(mT_beginAvailDataInclusive);
        this.slot_endAvailDataInclusive = dataSource.getCacheSlotIndex(mT_endAvailDataInclusive);

    }


    public void setCachingEnabled(boolean cachingEnabled) {
        this.objectsCache.setCachingEnabled(cachingEnabled);
    }


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
     * @return Object representing the data in the specified t range. NOTE: It
     * is still possible that there is no data for the entire time interval at
     * the pre-cache data source so the returned range may still be SMALLER than
     * requested.
     */
    public Object getData(
            double t_beginInclusive, double t_endInclusive,
            RoundingMode tBeginRoundingMode, RoundingMode tEndRoundingMode,
            int beginIndexMargin, int endIndexMargin,
            java.util.function.Predicate acceptCacheSlotContentsFunction,
            Object newCacheSlotContentsArgument)
            throws IOException {

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
        //Log.log(this.getClass().getSimpleName()+".getOrbitData: " + Time.toString(beginInclusiveMjd) + " to " + Time.toString(endInclusiveMjd) + ")", DEBUG);

        // Handle cases of t interval (without index margins) overlapping with outside of available data t interval.
        // NOTE: Must move both t_begin/end values toward nearest begin/end boundary
        // for available data since index margins have not been applied yet.
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
            final int slot_beginInclusive = dataSource.getCacheSlotIndex(t_beginInclusive - T_REQUEST_MARGIN);
            final int slot_endExclusive = dataSource.getCacheSlotIndex(t_endInclusive + T_REQUEST_MARGIN) + 1;

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
            return dataSource.getDataFromCacheSlotContents(null, 0, 0);   // Return empty value.
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
        public List<CacheSlotContents> getList(int i_beginInclusive, int i_endExclusive, Object getListArgument) throws IOException {
            return IndexedSegmentsCache.this.dataSource.getCacheSlotContents(i_beginInclusive, i_endExclusive, getListArgument);
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
     * @return {slot, i_slotTArray}. null if there is no such t value, e.g. if
     * there is no initial approximate t value.
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
            final int slot = dataSource.getCacheSlotIndex(t);
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

            // Check if actually found first approximate t position.
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
