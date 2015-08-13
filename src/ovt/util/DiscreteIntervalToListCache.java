/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ovt.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * General-purpose class for caching a function that<BR>
 * (1) takes an interval of integers as arguments,<BR>
 * (2) returns a list of objects, one object for every integer in the
 * interval,<BR>
 * (3) always returns the same object for the same integer.
 *
 * NOTE: null also works instead of an object.
 *
 * The term "cache slot" inside the class refers to a "place"/"slot"/"site"
 * where one object returned from the function (correspongin to a specific
 * integer) may or may not already be cached.
 *
 * NOTE: The current implementation does not assume there are any min/max limits
 * on meaningful integer intervals. It just caches returned objects from calls
 * to the DataSource function without regard to index ranges.
 *
 * IMPLEMENTATION NOTE: One could in principle implement a more general-purpose
 * cache for "non-indexed" objects but that would be trivial (a java.util.Map
 * object does most of the work). This class assumes that (1) objects are likely
 * to be requested in a continuous sequence (getList), and (2) it is more
 * efficient to construct (download/derive/read-from-file etc) multiple objects
 * in a continuous sequence rather than one-by-one (getList). The real value
 * that this class provides is that it figures out which continuous sequences of
 * objects need to be added to fulfill a request. Classes implementing the
 * interface DataSource only need to implement how to construct the inexed
 * objects for specified indices (DataSource#getList).
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, 2015
 */
public class DiscreteIntervalToListCache<O> {

    /**
     * Number of extra cache slots to fill (below and above) when completing a
     * request for new indexed objects. Used for proactive caching, i.e.
     * intentionally filling more cache slots than immediately needed to satisfy
     * the current request for data.
     */
    private final int proactiveFillMargin;
    private final DataSource dataSource;
    private final TreeMap<Integer, O> cachedObjects = new TreeMap<>();  // NOTE: HashMap/TreeMap permit null values.

    /**
     * Flag for whether caching is enabled or whether data is retrieved "fresh"
     * every time. For debugging purposes.
     */
    private boolean cachingEnabled = true;

    //##########################################################################
    public interface DataSource {

        /**
         * Method that returns requested objects from the source.
         *
         * NOTE: Must create all objects, even if an objects ends up with no
         * data due to e.g. data gap.
         *
         * @param getListArgument Argument passed on from the call to the cache
         */
        // IMPLEMENTATION NOTE: Java will not permit using "O" for return type (List<U>).
        public List getList(
                int i_beginInclusive, int i_endExclusive,
                Object getListArgument)
                throws IOException;
    }

    //##########################################################################

    /**
     * Constructor.
     */
    public DiscreteIntervalToListCache(DataSource mDataSource, int mProactiveFillMargin) {
        if (mProactiveFillMargin < 0) {
            throw new IllegalArgumentException("mProactiveFillMargin is negative.");
        }
        this.proactiveFillMargin = mProactiveFillMargin;
        this.dataSource = mDataSource;
    }


    public void setCachingEnabled(boolean mCachingEnabled) {
        this.cachingEnabled = mCachingEnabled;
    }


    /**
     * Returns requested sequence of indexed objects.
     *
     * @param i_beginInclusive First object index (inclusive).
     * @param i_endExclusive Index after the last object index (i.e. exclusive).
     * @param acceptCachedObject Function that accepts an already cached object
     * as argument and returns false iff that object should be rejected
     * (replaced by a new one). Null counts as accept every new indexed object.
     * @param getListArgument Object that is passed on in calls to getList.
     */
    public List<O> getList(
            int i_beginInclusive, int i_endExclusive,
            java.util.function.Predicate<O> acceptCachedObject,
            Object getListArgument)
            throws IOException {

        if (!this.cachingEnabled) {
            return dataSource.getList(
                    i_beginInclusive, i_endExclusive,
                    getListArgument);
        } else {

            boolean canSatisfyRequestFromCache = true;
            for (int i = i_beginInclusive; i < i_endExclusive; i++) {
                final boolean hasCachedObject = this.cachedObjects.containsKey(i);
                if (!hasCachedObject) {
                    canSatisfyRequestFromCache = false;
                    break;
                }
            }

            if (!canSatisfyRequestFromCache) {
                /*==============================================================
                 Fill uncached slots in the requested interval with some
                 extra margin for proactive caching.
                 NOTE: One only wants to cache proactively if new objects are to
                 be retrieved anyway. Therefore one must first check if any new
                 objects are to be retrieved at all.
                 =============================================================*/
                fillUncachedSlots(
                        i_beginInclusive - this.proactiveFillMargin, i_endExclusive + this.proactiveFillMargin,
                        acceptCachedObject, getListArgument);
            }
        }

        //============================================
        // Iterate over the requested indexed objects
        // - Check completeness
        // - Compile return list.
        //============================================
        final List<O> requestedObjects = new ArrayList<>();
        for (int i = i_beginInclusive; i < i_endExclusive; i++) {
            final O cachedObject = this.cachedObjects.get(i);
            if (cachedObject == null) {
                throw new RuntimeException("Cache failed to fill up all cache slots. This indicates a pure code bug.");
            }
            requestedObjects.add(cachedObject);
        }

        return requestedObjects;
    }


    //##########################################################################
    /**
     * Fill sequence of cache slots with new objects but only if they are empty.
     *
     * @param getIndexedObjectsArgument Object that is passed on in calls to
     * DataSource#getList.
     */
    private void fillUncachedSlots(
            int i_beginInclusive, int i_endExclusive,
            java.util.function.Predicate<O> acceptCachedObject,
            Object getIndexedObjectsArgument)
            throws IOException {
        /*========================================================================
         Iterate over cache slots that we want to fill:
         - Fill the slots that are empty or whose cached objects are not accepted.
         ========================================================================*/
        final int i_beginFillInclusive = i_beginInclusive;
        final int i_endFillExclusive = i_endExclusive;
        boolean hasSlotsToFill = false;

        // Initial value is never used but must be assigned to avoid compilation error.
        // Using value by mistake (due to bug) ==> Downloading nothing?
        int i_firstToFill = Integer.MAX_VALUE;

        for (int i = i_beginFillInclusive; i < i_endFillExclusive; i++) {
            // Check for existence of cached object. Works for "null" (Map-)values as opposed to Map#get(i)!=null.
            final boolean hasCachedObject = this.cachedObjects.containsKey(i);

            final O cachedObject = this.cachedObjects.get(i);

            // Determine whether the current cache slot needs to be filled.
            boolean fillCurrentSlot;
            if (hasCachedObject) {
                fillCurrentSlot = (acceptCachedObject != null) && (!acceptCachedObject.test(cachedObject));
            } else {
                fillCurrentSlot = true;
            }

            if (fillCurrentSlot) {
                if (!hasSlotsToFill) {
                    hasSlotsToFill = true;
                    i_firstToFill = i;
                }
            }

            if (hasSlotsToFill) {
                if (!fillCurrentSlot) {
                    // CASE: Current slot should not be filled but the previous one should.
                    fillCacheSlots(i_firstToFill, i, getIndexedObjectsArgument);  // NOTE: Do NOT include current slot.
                    hasSlotsToFill = false;
                } else if (i == i_endFillExclusive - 1) {
                    // CASE: Loop has reached last cache slot to check.
                    fillCacheSlots(i_firstToFill, i + 1, getIndexedObjectsArgument);  // NOTE: DO include current slot.
                }

            }
        }
    }


    //##########################################################################
    /**
     * Fill sequence of cache slots with new objects. Replaces old cached
     * objects if there are any.
     *
     * @param getIndexedObjectsArgument Object that is passed on in calls to
     * DataSource#getList.
     */
    private void fillCacheSlots(
            int i_beginInclusive, int i_endExclusive,
            Object getIndexedObjectsArgument)
            throws IOException {
        final List<O> newIndexedObjects = dataSource.getList(
                i_beginInclusive, i_endExclusive, getIndexedObjectsArgument);

        // Check results of call.
        if (newIndexedObjects.size() != i_endExclusive - i_beginInclusive) {
            throw new RuntimeException("dataSource.getIndexedObjects returned an incorrect result. "
                    + "This indicates a pure code bug.");
        }

        // Check results of call and put the results in the relevant cache slots.
        for (int i = i_beginInclusive; i < i_endExclusive; i++) {
            final O newObject = newIndexedObjects.get(i - i_beginInclusive);
            cachedObjects.put(i, newObject);  // Replaces preexisting cache object if there was any.
        }
    }


    //##########################################################################
    /**
     * Informal test code.
     */
    public static void main2(String[] args) throws Exception {
        //======================================================================
        class TestDataSource implements DiscreteIntervalToListCache.DataSource {

            @Override
            public List getList(int i_beginInclusive, int i_endExclusive, Object getIndexedObjectsArgument) {
                System.out.println("getIndexedObjects(" + i_beginInclusive + ", " + i_endExclusive + ", " + getIndexedObjectsArgument + ")");

                final List<Integer> objects = new ArrayList<>();   // ArrayList permits null.
                for (int i = i_beginInclusive; i < i_endExclusive; i++) {
                    if (i == 7) {
                        objects.add(null);
                    } else {
                        objects.add(1000 + i);
                    }
                }
                return objects;
            }
        }
        //======================================================================
        class TestCall {

            int a, b;
            List result;


            TestCall(int a, int b, Object... mResult) {
                this.a = a;
                this.b = b;
                this.result = new ArrayList();
                result.addAll(Arrays.asList(mResult));
            }
        }
        //======================================================================

        final List<TestCall> testCalls = new ArrayList();
        //testCalls.add(new TestCall(3, 5, 1003, 1004));
        //testCalls.add(new TestCall(2, 4, 1002, 1003));
        //testCalls.add(new TestCall(0, 6, 1000, 1001, 1002, 1003, 1004, 1005));
        //testCalls.add(new TestCall(3, 5, 1003, 1004));
        testCalls.add(new TestCall(5, 10, 1005, 1006, null, 1008, 1009));
        //testCalls.add(new TestCall(-5, -3, 995, 996));

        final DiscreteIntervalToListCache cache = new DiscreteIntervalToListCache(new TestDataSource(), 0);
        for (TestCall call : testCalls) {
            System.out.println("cache.getIndexedObjects(" + call.a + ", " + call.b + ", " + null + ", " + null + ");");
            final List<Integer> actualResult = cache.getList(call.a, call.b, null, null);
            if (actualResult.equals(call.result)) {
                System.out.println("== OK ==");
            } else {
                System.out.println("====================================================  ERROR");
                System.out.println("actualResult = " + actualResult);
                System.out.println("call.result  = " + call.result);
            }
        }
    } // main


    public static void test() {
    }
}
