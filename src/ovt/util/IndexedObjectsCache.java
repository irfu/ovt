/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ovt.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * General-purpose class for caching objects that are naturally indexed, i.e.
 * naturally parameterized by an integer (int), e.g. corresponding to
 * discretized time, or a time interval. A "cache unit" is such an object when
 * it is in the cache, or the corresponding "slot" in the cache that can be
 * "filled" with an arbitrary object.
 *
 * NOTE: Current implementation does not permit null as a cache unit because of
 * an explicit check, but there is no other reason for excluding it.
 *
 * IMPLEMENTATION NOTE: One could in principle implement a more general-purpose
 * cache for "non-indexed" objects but that would be trivial (a java.util.Map
 * object does most of the work). This class assumes that (1) objects are likely
 * to be requested in a continuous sequence (getCacheUnits), and (2) it is more
 * efficient to construct (download/calculate/read-from-file etc) multiple
 * objects in a continuous sequence rather than one-by-one
 * (createNewCacheUnits). The real value that this class provides is that it
 * figures out which continuous sequences of objects need to be added to fulfill
 * a request. Classes implementing interface DataSource only need to implement
 * how to construct the cache units for specified indices
 * (DataSource#createNewCacheUnits).
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, 2015
 */
// PROPOSAL: Store limits for the possible range of cache unit indices?
//
public class IndexedObjectsCache<U> {

    // PROPOSAL: Eliminate functionality?
    //    PRO: May lead to filling when no filling would be necessary.
    //    PRO: May lead to extra separate fill calls.
    /**
     * Number of extra cache units to fill (below and above) when completing a
     * request for cache units. Used for proactive caching, i.e. intentionally
     * filling more cache units than needed to satisfy an immediate request for
     * data.
     */
    private final int proactiveFillMargin;
    private final DataSource dataSource;
    private final TreeMap<Integer, U> cacheUnits = new TreeMap<>(); // NOTE: HashMap/TreeMap permit null values.

    //##########################################################################
    public interface DataSource {

        /**
         * Method that creates and returns the requested cache units.
         *
         * NOTE: Must create all cache units, even if a cache unit ends up with
         * no data due to e.g. data gap. Null as a cache unit is not allowed.
         *
         * @param newUnitsArgument Argument passed on from the call to
         * getCacheUnits.
         */
        // IMPLEMENTATION NOTE: Java will not permit using "U" for return type (List<U>).
        public List createNewCacheUnits(int i_beginInclusive, int i_endExclusive, Object newUnitsArgument) throws IOException;
    }

    //##########################################################################


    public IndexedObjectsCache(DataSource mDataSource, int mProactiveFillMargin) {
        if (mProactiveFillMargin < 0) {
            throw new IllegalArgumentException("mProactiveFillMargin is negative.");
        }
        this.proactiveFillMargin = mProactiveFillMargin;
        this.dataSource = mDataSource;
    }


    /**
     * Returns list of requested sequence of cache units.
     *
     * @param i_beginInclusive First cache unit index (inclusive).
     * @param i_endExclusive Index after the last cache unit (i.e. exclusive).
     * @param acceptCachedUnit Function that accepts an already cached object as
     * argument and returns false iff that object should be rejected (replaced
     * by a new one). Null counts as accept every cached unit.
     * @param newUnitsArgument Object that is passed on in calls to
     * createNewCacheUnits.
     */
    public List<U> getCacheUnits(int i_beginInclusive, int i_endExclusive,
            java.util.function.Predicate<U> acceptCachedUnit,
            Object newUnitsArgument)
            throws IOException {

        //================================================
        // Iterate over cache units that we want to fill.
        // - Fill the units that are missing.
        //================================================
        {
            final int i_beginFill = i_beginInclusive - proactiveFillMargin;
            final int i_endFill = i_endExclusive + proactiveFillMargin;
            boolean hasUnitsToFill = false;
            int i_firstToFill = Integer.MAX_VALUE;   // Initial value never used. Must be assigned to avoid compilation error. Using value by mistake (bug) ==> Downloading nothing?        

            for (int i = i_beginFill; i < i_endFill; i++) {
                final boolean hasUnit = this.cacheUnits.containsKey(i);  // Check for existence of unit. Works for "null" values as opposed to Map#get(i)!=null.
                final U unit = this.cacheUnits.get(i);

                // Determine whether the current cache unit needs to be filled.
                boolean fillCurrentUnit;
                if (hasUnit) {
                    fillCurrentUnit = (acceptCachedUnit != null) && (!acceptCachedUnit.test(unit));
                } else {
                    fillCurrentUnit = true;
                }

                if (fillCurrentUnit) {
                    if (!hasUnitsToFill) {
                        hasUnitsToFill = true;
                        i_firstToFill = i;
                    }
                }

                if (hasUnitsToFill) {
                    if (!fillCurrentUnit) {
                        // CASE: Current unit should not be filled but the previous one should.
                        fillCacheUnits(i_firstToFill, i, newUnitsArgument);  // NOTE: Do NOT include current unit.
                        hasUnitsToFill = false;
                    } else if (i == i_endFill - 1) {
                        // CASE: Loop has reached last unit to check.
                        fillCacheUnits(i_firstToFill, i + 1, newUnitsArgument);  // NOTE: Do include current unit.
                    }

                }
            }
        }

        //========================================
        // Iterate over the requested cache units
        // - Check completeness
        // - Compile return list.
        //========================================
        final List<U> requestedCacheUnits = new ArrayList<>();
        for (int i = i_beginInclusive; i < i_endExclusive; i++) {
            final U unit = this.cacheUnits.get(i);
            if (unit == null) {
                throw new RuntimeException("Cache failed to fill up all cache units. This indicates a pure OVT bug.");
            }
            requestedCacheUnits.add(unit);
        }

        return requestedCacheUnits;
    }


    /**
     * Fill sequence of cache units with new objects. This may replace old
     * cached objects.
     *
     * @param newUnitsArgument Object that is passed on in calls to
     * createNewCacheUnits.
     */
    private void fillCacheUnits(int i_beginInclusive, int i_endExclusive, Object newUnitsArgument) throws IOException {
        final List<U> newCacheUnits = dataSource.createNewCacheUnits(i_beginInclusive, i_endExclusive, newUnitsArgument);
        if (newCacheUnits.size() != i_endExclusive - i_beginInclusive) {
            throw new RuntimeException("newCacheUnits returned an incorrect result. This indicates a pure code bug.");
        }

        for (int i = i_beginInclusive; i < i_endExclusive; i++) {
            final U newCacheUnit = newCacheUnits.get(i - i_beginInclusive);
            if (newCacheUnit == null) {
                throw new RuntimeException("newCacheUnits returned null as a new cache unit. This indicates a pure code bug.");
            }
            cacheUnits.put(i, newCacheUnit);  // Replaces preexisting cache unit.
        }
    }


    //##########################################################################
    /**
     * Informal test code.
     */
    public static void main(String[] args) throws Exception {
        //======================================================================
        class TestDataSource implements IndexedObjectsCache.DataSource {

            @Override
            public List createNewCacheUnits(int i_begin, int i_end, Object newUnitsArgument) {
                System.out.println("createNewCacheUnits(" + i_begin + ", " + i_end + ", " + newUnitsArgument + ")");

                final List<Integer> units = new ArrayList<>();
                for (int i = i_begin; i < i_end; i++) {
                    units.add(1000 + i);
                }
                return units;
            }
        }
        //======================================================================
        class TestCall {

            int a, b;
            List<Integer> result;


            TestCall(int a, int b, int... mResult) {
                this.a = a;
                this.b = b;
                this.result = new ArrayList();
                for (int r : mResult) {
                    result.add(r);
                }
            }
        }
        //======================================================================

        final List<TestCall> testCalls = new ArrayList();
        testCalls.add(new TestCall(3, 5, 1003, 1004));
        testCalls.add(new TestCall(2, 4, 1002, 1003));
        testCalls.add(new TestCall(0, 6, 1000, 1001, 1002, 1003, 1004, 1005));
        testCalls.add(new TestCall(3, 5, 1003, 1004));
        testCalls.add(new TestCall(-5, -3, 995, 996));

        final IndexedObjectsCache cache = new IndexedObjectsCache(new TestDataSource(), 0);
        for (TestCall call : testCalls) {
            System.out.println("getCacheUnits(" + call.a + ", " + call.b + ", " + null + ", " + null + ");");
            List<Integer> actualResult = cache.getCacheUnits(call.a, call.b, null, null);
            if (actualResult.equals(call.result)) {
                System.out.println("== OK ==");
            } else {
                System.out.println("====================================================  ERROR");
            }
        }

    } // main
}
