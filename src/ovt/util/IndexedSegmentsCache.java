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
 * Uses IndexedObjectsCache to implement a cache for "array-like data
 * structures". Data consists of one data point for every "t value" (double). t
 * values are assumed to be sorted and monotonically increasing. Every cache
 * unit (IndexObjectsCache) covers a specific itnerval of t values. For the
 * purposes of OVT, "t" likely represents time, but it can in principle
 * represent anything. The user of the class has to implement methods for
 * splitting up data in cache units and putting data together from cache units.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se
 */
public class IndexedSegmentsCache {

    private static final int DEBUG = 1;   // Set the minimum log message level for this class.
    private final IndexedObjectsCache objectsCache;
    //private final LocalCacheDataSource localCacheDataSource;
    private final int i_unitMinInclusive;      // Lowest possible unit for which there may be data.
    private final int i_unitMaxInclusive;      // Highest possible unit for which there may be data.
    private final DataSource dataSource;

    //##########################################################################
    public interface DataSource {

        /**
         * Get index for the cache unit that covers/contains this t value.
         */
        public int getCacheUnit(double t);


        /**
         * Take multiple cache units and extract and put together the requested
         * data.
         */
        public Object extractCacheUnitContents(List<SegmentCacheUnit> requestedUnits, int i_beginUnitArrayInclusive, int i_endUnitArrayExclusive);


        /**
         * Create cache units from data.
         */
        public List<SegmentCacheUnit> createNewCacheUnits(int i_beginInclusive, int i_endExclusive, Object newUnitsArgument) throws IOException;
    }

    //##########################################################################
    public interface SegmentCacheUnit {

        public double[] getTArray();
    }

    //##########################################################################
    private class InternalCacheDataSource implements IndexedObjectsCache.DataSource {

        @Override
        public List<SegmentCacheUnit> createNewCacheUnits(int i_beginInclusive, int i_endExclusive, Object newUnitsArgument) throws IOException {
            return IndexedSegmentsCache.this.dataSource.createNewCacheUnits(i_beginInclusive, i_endExclusive, newUnitsArgument);
        }
    }
    //##########################################################################


    /**
     * Constructor.
     */
    public IndexedSegmentsCache(DataSource mDataSource, double tMinInclusive, double tMaxInclusive, int proactiveFillMargin) {
        this.dataSource = mDataSource;
        this.i_unitMinInclusive = dataSource.getCacheUnit(tMinInclusive);
        this.i_unitMaxInclusive = dataSource.getCacheUnit(tMaxInclusive);
        this.objectsCache = new IndexedObjectsCache(new InternalCacheDataSource(), proactiveFillMargin);
    }


    /**
     * Get orbital data in the internal data format.
     *
     * The implementation fetches data from the cache and merges data from the
     * required cache units.<BR>
     *
     * @param beginIndexMargin Number of data points to additionally include
     * BEFORE the stated t interval.
     * @param endIndexMargin Number of data points to additionally include AFTER
     * the stated t interval.
     *
     * @return Object representing the data in the specified t range. NOTE: It
     * is still possible that there is no data for the entire time interval at
     * the pre-cache source so the returned range may still be SMALLER than
     * requested.
     */
    public Object getData(double beginInclusiveT, double endInclusiveT, int beginIndexMargin, int endIndexMargin,
            java.util.function.Predicate acceptCachedUnitFunction, Object newUnitsArgument)
            throws IOException {

        /*if (maxResolutionFactor <= 0) {
         throw new RuntimeException("maxResolutionFactor equal or less than zero. This indicates a pure software bug in OVT.");
         } else*/
        if (beginIndexMargin < 0) {
            throw new RuntimeException("beginIndexMargin < 0. This indicates a pure software bug in OVT.");
        } else if (endIndexMargin < 0) {
            throw new RuntimeException("endIndexMargin < 0. This indicates a pure software bug in OVT.");
            // Good to check for this if the caller thinks it shouls be a non-positive number.
        }
        Log.log("getData(beginInclusiveT=" + beginInclusiveT + ", endInclusiveT=" + endInclusiveT
                + ", beginIndexMargin=" + beginIndexMargin + ", endIndexMargin=" + endIndexMargin
                + ", newUnitsArgument=" + newUnitsArgument + ")", DEBUG);
        //Log.log("      getOrbitData: " + Time.toString(beginInclusiveMjd) + " to " + Time.toString(endInclusiveMjd) + ")", DEBUG);

        {
            // Construct initial guess for which units to load.
            // Add margins to the time interval to make it likely that all needed
            // cache units are included on the first attempt (first request).
            int unitBeginInclusive = dataSource.getCacheUnit(beginInclusiveT);
            int unitEndExclusive = dataSource.getCacheUnit(endInclusiveT) + 1;

            // Request units once to cache as many units in sequence as possible.
            // NOTE: The return value is NOT used.
            this.objectsCache.getCacheUnits(
                    unitBeginInclusive,
                    unitEndExclusive, // Argument should be exclusive upper bound.
                    acceptCachedUnitFunction,
                    newUnitsArgument);
        }

        // Construct initial interval of units to actually use for sure.
        // Could be a larger interval, but it is unlikely if beginEndIndexMargin<<(number of data points per unit).
        int[] junk = getCachePosition(beginInclusiveT, -beginIndexMargin, RoundingMode.CEILING, acceptCachedUnitFunction, newUnitsArgument);
        final int i_unitBeginInclusive = junk[0];
        final int i_beginDataPointInclusive = junk[1];
        junk = getCachePosition(endInclusiveT, endIndexMargin, RoundingMode.FLOOR, acceptCachedUnitFunction, newUnitsArgument);
        final int i_unitEndInclusive = junk[0];
        final int i_endDataPointExclusive = junk[1] + 1;

        // Request units AGAIN to make sure that we get exactly the ones we need.
        final List<SegmentCacheUnit> requestedUnits = this.objectsCache.getCacheUnits(
                i_unitBeginInclusive,
                i_unitEndInclusive + 1, // Argument should be exclusive upper bound.
                acceptCachedUnitFunction,
                newUnitsArgument);

        return dataSource.extractCacheUnitContents(requestedUnits, i_beginDataPointInclusive, i_endDataPointExclusive);
    }


    /**
     * Get the position of a specified data point relative to a t value. This is
     * useful for looking at the (cached) data as a giant array spanning
     * multiple cache units.
     *
     * NOTE: If there is no such cache unit within the data interval, return the
     * closest one.
     *
     * NOTE: This function can not rely on that all necessary data is already in
     * the cache.
     *
     * @return Array of length 2. The first element is the cache unit index. The
     * second element is the data point index within the cache unit. If no
     * element exists, then the first element will point to an existing cache
     * unit, but the second element will point to zero or the last index of the
     * data array.
     */
    // PROPOSAL: Parameter for how to deal with the case of the position not existing.
    // NOTE: acceptCachedUnitFunction as parameter?!!!
    private int[] getCachePosition(double t, int indexSteps, RoundingMode dataIndexRoundingMode,
            java.util.function.Predicate acceptCachedUnitFunction, Object newUnitsArgument) throws IOException {

        // NOTE: The algorithm will at most jump one cache unit higher or lower at a time.
        // Could in principle do something similar to binary search but that is a bit overkill...
        int i_unit = dataSource.getCacheUnit(t);
        List<SegmentCacheUnit> requestedUnits;

        requestedUnits = this.objectsCache.getCacheUnits(
                i_unit,
                i_unit + 1,
                acceptCachedUnitFunction,
                newUnitsArgument);

        double[] unitTArray = requestedUnits.get(0).getTArray();
        int i_data = Utils.findNearestMatch(unitTArray, t, dataIndexRoundingMode) + indexSteps;

        while (true) {

            boolean moveToLowerUnit;
            if ((i_data < 0) && (i_unitMinInclusive < i_unit)) {
                i_unit--;
                moveToLowerUnit = true;
            } else if ((unitTArray.length <= i_data) && (i_unit <= i_unitMaxInclusive)) {
                i_unit++;
                i_data = -unitTArray.length + i_data;
                moveToLowerUnit = false;
            } else {
                break;  // NOTE: Jump out of loop.
            }

            requestedUnits = this.objectsCache.getCacheUnits(
                    i_unit,
                    i_unit + 1,
                    acceptCachedUnitFunction,
                    newUnitsArgument);
            unitTArray = requestedUnits.get(0).getTArray();

            if (moveToLowerUnit) {
                i_data = unitTArray.length + i_data;
            }
        }

        // Prevent index from being outside the data array.
        i_data = Math.max(i_data, 0);
        i_data = Math.min(i_data, unitTArray.length - 1);

        return new int[]{i_unit, i_data};
    }

}
