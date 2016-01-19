/*=========================================================================
 
 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/util/SegmentsCache.java $
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
 
 OVT Team (http://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
 Khotyaintsev, E. P. G. Johansson, F. Johansson
 
 =========================================================================*/
package ovt.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Cache for a function that<BR>
 * (1) orders/organizes/distributes some form of data on a continuous 1D axis,
 * called "t",<BR>
 * (2) returns data corresponding to an arbitrarily specified segment on the t
 * axis, t_a-t_b ({@code t_a<t_b}; a "data segment"), <BR>
 * (3) always returns equivalent data for a given interval on the t axis.<BR>
 * (4) that returns a segment t_a-t_c equal to merging the segments t_a-t_b and
 * t_b-t_c {@code (t_a <= t_b <= t_c)}<BR>
 *
 * The cached function, and the function for merging data segments is accessed
 * through an implementation of DataSource.
 *
 * NOTE: The cached function determines whether upper/lower t boundaries are
 * inclusive/exclusive or not. This class does not care, but note that a
 * DataSource must treat at least one of the boundaries as inclusive to satisfy
 * the requirements above. Note also that a DataSource may treat both boundaries
 * as inclusive and remove the overlap when merging data segments.
 *
 * NOTE: The cache is left intact if DataSegment, DataSource throw exceptions
 * which propagate through a call to the cache.
 *
 * NOTE: This class is effectively a (mostly) better successor to
 * ovt.util.IndexedSegmentsCache but without some of its features, features
 * which probably should have been implemented some other way anyway.
 * IndexedSegmentsCache has "index searching" (this class does not have a notion
 * of indices), load from/save to stream, ability to replace cached data with
 * newer data of higher "quality" (e.g. better resolution) (there may be more
 * differences). This class has the ability for a data source to add more data
 * to the cache than asked for ("data source-initiated proactive caching").
 *
 * IMPLEMENTATION NOTE: The class does not support zero-length data segments.
 * Implementing support for zero-length intervals is hard because of the
 * ambiguous behaviour for set differences without distinguishing between
 * open/closed/halfopen intervals which the class tries to avoid. Example: What
 * is the set difference between a zero-length interval and itself?
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015
 */
// NOTE: There is no easy way of implementing selectively clearing cached data, e.g. data that has not been used for a long time.
// PROPOSAL: SearchFunction, (SearchFunction) limits.
// PROPOSAL: Permit DataSource to throw IOException.
// PROPOSAL: Get segment (transverse/orthogonal) subset.
public class SegmentsCache {

    private final static int DEBUG = 4;

    /**
     * Class which models data for a finite t interval. Instances are stored
     * inside the cache.
     *
     * NOTE: INSTANCES MUST MUST BE TREATED AS IMMUTABLE BY CODE OUTSIDE OF THE
     * CACHE.
     */
    public interface DataSegment {

        /**
         * Returns the t interval which the object covers. Should preferably not
         * return an internal copy.
         *
         * @return Length 2. [0]=beginning of interval, [1]=end of interval.
         */
        public double[] getInterval();


        /**
         * Return DataSegment corresponding to the specified subset t interval.
         * Some exception if the interval does not descrive a subset (e.g.
         * IllegalArgumentException).
         */
        public DataSegment selectSubset(double t_begin, double t_end);
    }
    //##########################################################################

    /**
     * Interface which supplies data for the cache and the ability to merge
     * DataSets.
     */
    public interface DataSource {

        /**
         * Return DataSegment corresponding to AT LEAST the specified interval.
         * It may return a larger DataSegment if the implementation by its
         * nature has more data avilable anyway, e.g. because it has to parse a
         * large file just for finding a small interval.
         *
         * NOTE: Must not throw exception merely because there is no data for
         * the given t interval. Must instead return a DataSegment that
         * represents the absence of data.
         */
        public DataSegment getSupersetSegment(double t_begin, double t_end) throws IOException;


        /**
         * Return a DataSegment corresponding to a set of smaller DataSegments.
         *
         * @segments Non-empty list, with adjacent segments sorted in t order.
         */
        public DataSegment mergeAdjacent(List<DataSegment> segments);

    }

    //##########################################################################
    public enum SearchDirection {

        DOWN, UP
    }

    public interface SearchFunction {

        /**
         * @param t_start Where to start searching. NOTE: The value is
         * guaranteed to be within the segment interval INCLUDING its
         * boundaries.
         * @param seg DataSegment to searchDataSegment.
         *
         * @return Non-null reference if successful, otherwise null.
         */
        public Object searchDataSegment(DataSegment seg, double t_start, SearchDirection dir);
    }

    //##########################################################################
    private static final Comparator DATASEGMENT_COMPARATOR = new Comparator() {
        @Override
        public int compare(Object s1, Object s2) {
            final double[] a1 = ((DataSegment) s1).getInterval();
            final double[] a2 = ((DataSegment) s2).getInterval();
            int d0_compare = Double.compare(a1[0], a2[0]);
            if (d0_compare != 0) {
                return d0_compare;
            } else {
                // Only needed for zero-length intervals.
                return Double.compare(a1[1], a2[1]);
            }
            // Not entirely synced with equals, but only for length-2 arrays.
        }
    };

    private final DataSource dataSrc;
    private final List<DataSegment> cachedSegments = new ArrayList();   // NOTE: Not sorted by default.

    /**
     * Positive(!) number defining an approximate t interval size for the
     * minimum amount of data to ask for when calling the data source for more
     * data anyway. This is to facilitate "proactive caching" and avoid many
     * calls for small amounts of data.
     */
    private final double minDataSourceTScale;
    private final double searchTMin, searchTMax;

    //##########################################################################

    public SegmentsCache(DataSource mDataSrc, double mMinDataSourceTScale, double mSearchTMin, double mSearchTMax) {
        if ((mDataSrc == null) || (mMinDataSourceTScale <= 0) || (mSearchTMax <= mSearchTMin) || (mMinDataSourceTScale < 0)) {
            throw new IllegalArgumentException();
        }
        dataSrc = mDataSrc;
        minDataSourceTScale = mMinDataSourceTScale;
        searchTMin = mSearchTMin;
        searchTMax = mSearchTMax;
    }


    /**
     * Get DataSegment for specific t interval. Uses data in the cache if it
     * can, otherwise it fills in the missing pieces with calls to DataSource.
     */
    public DataSegment getSegment(double t_begin, double t_end) throws IOException {

        // NOTE: getSegmentSuperset does the argument check.
        final DataSegment seg = getSegmentSuperset(t_begin, t_end);

        // Extract the data.
        return seg.selectSubset(t_begin, t_end);
    }


    /**
     * Get DataSegment that covers a superset of specific t interval. Uses data
     * in the cache if it can, otherwise it fills in the missing pieces with
     * calls to DataSource.
     *
     * This method is only public because it can be more efficient in cases
     * where using a superset is OK.
     */
    public DataSegment getSegmentSuperset(double t_begin, double t_end) throws IOException {
        // NOTE: Exclude zero-length intervals.
        if (t_end - t_begin <= 0) {
            throw new IllegalArgumentException("Trying to use non-positive interval length.");
        }

        DataSegment seg = findCachedSegmentSuperset(t_begin, t_end);
        if (seg == null) {
            double tMargin = (minDataSourceTScale - (t_end - t_begin)) / 2.0;
            tMargin = Math.max(tMargin, 0.0);       // Make sure it is positive.

            ensureIntervalIsCachedInSingleSegment(t_begin - tMargin, t_end + tMargin);
            seg = findCachedSegmentSuperset(t_begin, t_end);
        }
        return seg;
    }


    /**
     * Search the cached function in a direction. Starts searching from a given
     * point in a given direction. A user-supplied function searches a given
     * DataSegment and defines what is searched for.
     *
     * The method is needed/useful since the cache works with t intervals which
     * are different from e.g. underlying data points that are not spread evenly
     * on the t axis. Examples of uses: (1) Search for next/previous data point
     * where data points are spread out unevenly, (2) search DataSegment for the
     * next non-fill value.
     *
     * NOTE: Will search DataSegment inside the specified search limits in t
     * (set in constructor), and an undetermined but finite distance outside
     * depending on cached segments.
     *
     * NOTE: This function can not handle searching for the NEAREST matching
     * position (whether at a higher or lower t). Only clean way of doing
     * searching both up and down and then choose the nearest one. Note that
     * this requires returning the t value and not just the result.
     *
     * IMPLEMENTATION NOTE: Implementation is not intended to be efficient when
     * searching long distances (in t), although it does work. It is intended
     * for short searches where it will usually succeed on the first DataSegment
     * it searches. Note that one search may lead to caching data, which leads
     * to subsequent searches maybe finding data in the first segment.
     *
     * IMPLEMENTATION NOTE: This feature could in principle be implemented
     * separately from this class since it really only _needs_ public methods
     * (slight modification to only use #getSegmentSuperset).
     *
     * @return The (non-null) reference that was returned from the search
     * function if successful. Null iff could not find what was sought.
     */
    public Object search(double t_start, SearchDirection dir, SearchFunction searchFunc) throws IOException {
        Log.log(
                this.getClass().getSimpleName() + " # searchDataSegment"
                + "(t_start=" + t_start + ", dir=" + dir + ", searchFunc=...)",
                DEBUG);
        //Log.log("   Cached interval sum = " + getCachedTIntervalSum(), DEBUG);

        if (!Double.isFinite(t_start) | (searchFunc == null)) {
            throw new IllegalArgumentException();
        }

        // Move t_start to within the permitted searchDataSegment interval.
        t_start = Math.max(t_start, searchTMin);
        t_start = Math.min(t_start, searchTMax);

        double t_step;   // Change name t_step?
        int i_segmentFarBoundary;
        if (dir == SearchDirection.DOWN) {
            t_step = -minDataSourceTScale;
            i_segmentFarBoundary = 0;
        } else {
            t_step = +minDataSourceTScale;
            i_segmentFarBoundary = 1;
        }

        // t1,t2 = Minimum interval limits for segment to request, without specifying which is lower/upper boundary.
        double t1 = t_start;
        double t2 = t_start + t_step;  // NOTE: Can be higher OR LOWER than t1.

        /**
         * IMPLEMENTATION NOTE: Needs to limit search to boundaries to avoid
         * searching to infinity. For example, a specific implementation of
         * DataSegments may not have data points to search outside of a given t
         * range.
         */
        while ((searchTMin <= t_start) && (t_start <= searchTMax)) {
            // Make sure t1<t2.
            if (t2 < t1) {
                final double temp = t2;
                t2 = t1;
                t1 = temp;
            }

            ensureIntervalIsCachedInSingleSegment(t1, t2);   // Contains call to mergeAdjacentSegments.
            final DataSegment seg = findCachedSegmentSuperset(t1, t2);
            //Log.log("   Cached interval sum = " + getCachedTIntervalSum(), DEBUG);
            Log.log("Call searchFunc.searchDataSegment", DEBUG);
            Log.log("   seg.getInterval()=" + Arrays.toString(seg.getInterval()) + ", t_start=" + t_start + ", dir=" + dir, DEBUG);

            final Object searchResults = searchFunc.searchDataSegment(seg, t_start, dir);
            if (searchResults != null) {
                return searchResults;
            }
            t_start = seg.getInterval()[i_segmentFarBoundary];
            t1 = t_start;
            t2 = t1 + t_step;
        }
        return null;
    }


    //##########################################################################
    /**
     * Makes sure that a given interval is cached and is contained in one single
     * DataSegment.
     */
    private void ensureIntervalIsCachedInSingleSegment(double t_begin, double t_end) throws IOException {
        final double[] requestedInterval = new double[]{t_begin, t_end};

        while (true) {
            // When the loop adds a new DataSegment to the cache, one OR SEVERAL
            // holes in the cache may be filled (entirely or partially) with data.
            // Therefore, one must call getCachedTIntervals() again in every loop
            // rather than call it once and iterate over the holes in the cache (intervalsToGet).
            final List<double[]> intervalsToGet = removeIntervals(requestedInterval, getCachedTIntervals());

            if (intervalsToGet.isEmpty()) {
                break;
            }

            // NOTE: Obtain ONE DataSegment and add it to the cache. This operation may fill SEVERAL holes in the cache.
            // Should therefore not use the other intervals as they may be obsoleted by this one operation.
            final double[] intervalToGet = intervalsToGet.get(0);
            final DataSegment newSeg = dataSrc.getSupersetSegment(intervalToGet[0], intervalToGet[1]);
            addToCacheFromNewDataSegment(newSeg);
        }

        mergeAdjacentCachedSegments();
    }


    /**
     * Merges all adjacent DataSegments in the cache.
     */
    private void mergeAdjacentCachedSegments() {

        // NOTE: The algorithm needs the cached DataSegments to be sorted.
        cachedSegments.sort(DATASEGMENT_COMPARATOR);

        final List<DataSegment> newCachedSegments = new ArrayList();

        final List<DataSegment> segmentsToMerge = new ArrayList();    // Must be List, not Set, since the order matters.
        double[] prevInterval = null;
        for (DataSegment seg : cachedSegments) {

            if (!segmentsToMerge.isEmpty()) {
                // CASE: This is not the first segment in a sequence of adjacent segments.
                if (prevInterval[1] != seg.getInterval()[0]) {
                    newCachedSegments.add(dataSrc.mergeAdjacent(segmentsToMerge));
                    segmentsToMerge.clear();
                }
            }
            segmentsToMerge.add(seg);
            prevInterval = seg.getInterval();
        }

        // NOTE: Must not call DataSource#mergeAdjacent for empty list.
        // segmentsToMerge is empty for an entirely empty cache. Therefore must check first.
        if (!segmentsToMerge.isEmpty()) {
            newCachedSegments.add(dataSrc.mergeAdjacent(segmentsToMerge));
        }

        // Add all segments in one almost "atomic" operation.
        cachedSegments.clear();
        cachedSegments.addAll(newCachedSegments);
    }


    /**
     * Adds the information in one DataSegment to the cache.
     *
     * NOTE: Does not merge adjacent DataSegments afterwards since one may want
     * to add multiple segments at a time before merging (more efficient).
     */
    private void addToCacheFromNewDataSegment(DataSegment seg) {

        final List<double[]> intervalsToAdd = removeIntervals(seg.getInterval(), getCachedTIntervals());

        for (double[] intervalToAdd : intervalsToAdd) {
            DataSegment segToAdd = seg.selectSubset(intervalToAdd[0], intervalToAdd[1]);
            cachedSegments.add(segToAdd);
        }
    }

    //##########################################################################

    /**
     * Try to find an already existing cached DataSegment that is a superset
     * (not necessarily strict superset) of a given interval.
     *
     * NOTE: Usually means that cached DataSegments have been merged first.<BR>
     * NOTE: Will not modify the cache.<BR>
     *
     * @return Null iff there is no such cached DataSegment.
     */
    private DataSegment findCachedSegmentSuperset(double t_begin, double t_end) {
        for (DataSegment seg : cachedSegments) {
            final double[] ci = seg.getInterval();  // ci = cached interval

            if (intervalIsSuperset(ci[0], ci[1], t_begin, t_end)) {
                return seg;
            }
        }
        return null;
    }


    /**
     * Get list of all intervals that are covered by DataSegments in the cache.
     *
     * NOTE: The method is PUBLIC only so that it can be used a as diagnostic
     * for testing purposes. It is also used internally.
     *
     * @return List of length-2 arrays. The intervals may be adjacent to each
     * other. They are not necessarily sorted.
     */
    public List<double[]> getCachedTIntervals() {
        final List<double[]> cachedIntervals = new ArrayList();
        for (final DataSegment seg : cachedSegments) {
            cachedIntervals.add(seg.getInterval());
        }
        return cachedIntervals;
    }


    /**
     * Get the sum of the length of all cached DataSegments.
     *
     * NOTE: The method exists only so that it can be used as a diagnostic for
     * testing purposes.
     *
     * @return List of length-2 arrays. The intervals may be adjacent to each
     * other. They are not necessarily sorted.
     */
    public double getCachedTIntervalSum() {
        double t_sum = 0;
        for (final DataSegment seg : cachedSegments) {
            final double[] interval = seg.getInterval();
            t_sum += interval[1] - interval[0];
        }
        return t_sum;
    }//*/


    //##########################################################################
    /**
     * Remove one interval from another as if they were sets (set-theoretic
     * difference) of numbers.
     *
     * NOTE: Pure utility function independent of the actual cache.
     *
     * NOTE: Can NOT handle zero-length intervals.<BR>
     * It is hard to find what the meaningful behaviour for zero length
     * intervals should be without distinguishing open/closed/halfopen intervals
     * which this class tries to avoid. Therefore this function does not handle
     * zero-length intervals. Example: a and b are both identical zero-length
     * intervals.
     *
     * @return List with zero, one, or two intervals. Every component is a
     * length-2 array holding the interval min-max values.
     */
    // Move to Utils?
    public static List<double[]> removeInterval(double[] a, double[] b) {
        final double[] c1 = {a[0], Math.min(a[1], b[0])};  // Part of a that is lower than b.
        final double[] c2 = {Math.max(b[1], a[0]), a[1]};  // Part of a that is higher than b.

        final List<double[]> c = new ArrayList();
        if (c1[0] < c1[1]) {
            c.add(c1);
        }
        if (c2[0] < c2[1]) {
            c.add(c2);
        }
        return c;
    }


    /**
     * Given an interval, remove multiple intervals from it as if it was a
     * set-theoretic difference. Return a list with the intervals that describe
     * what remains.
     *
     * NOTE: Pure utility function independent of the actual cache.
     *
     * IMPLEMENTATION NOTE: Implementation uses a potentially slow algorithm.
     * Time ~ O(N^2), N=nbr of cached data segments. Probably alright in
     * realistic cases.
     */
    // Move to Utils?
    public static List<double[]> removeIntervals(double[] a, List<double[]> B) {
        // NOTE: There is no need to adjacent intervals afterwards.

        List<double[]> difference = new ArrayList();
        difference.add(a);

        for (double[] b : B) {
            final List<double[]> newDifference = new ArrayList();
            for (double[] d : difference) {
                newDifference.addAll(removeInterval(d, b));
            }
            difference = newDifference;
        }

        return difference;
    }


    /**
     * NOTE: Pure utility function independent of the actual cache.
     *
     * @return True iff the first interval is a superset (not necessarily
     * strict) of the latter.
     */
    // Move to Utils?
    public static boolean intervalIsSuperset(
            double t1_begin, double t1_end,
            double t2_begin, double t2_end) {
        return (t1_begin <= t2_begin) && (t2_end <= t1_end);
    }

}
