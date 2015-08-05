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
 * discretized time, or a time interval. The term "cache slot" inside the class
 * refers to a "place"/"slot"/"site" where one indexed object corresponding
 * index value may or may not already be cached.
 *
 * NOTE: The current implementation does not permit null as an indexed object
 * because of an explicit check, but there is no other reason for excluding it.
 *
 * IMPLEMENTATION NOTE: One could in principle implement a more general-purpose
 * cache for "non-indexed" objects but that would be trivial (a java.util.Map
 * object does most of the work). This class assumes that (1) objects are likely
 * to be requested in a continuous sequence (getIndexedObjects), and (2) it is
 * more efficient to construct (download/derive/read-from-file etc) multiple
 * objects in a continuous sequence rather than one-by-one (getIndexedObjects).
 * The real value that this class provides is that it figures out which
 * continuous sequences of objects need to be added to fulfill a request.
 * Classes implementing the interface DataSource only need to implement how to
 * construct the inexed objects for specified indices
 * (DataSource#getIndexedObjects).
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, 2015
 */
// PROPOSAL: Store limits for the possible range of object indices?
//
public class IndexedObjectsCache<O> {

    // PROPOSAL: Eliminate functionality proactiveFillMargin?
    //    PRO: May lead to filling when no filling would be necessary.
    //    PRO: May lead to extra separate fill calls.
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
         * data due to e.g. data gap. Null as an indexed object is not allowed.
         *
         * @param getIndexedObjectsArgument Argument passed on from the call to
         * getIndexedObjects.
         */
        // IMPLEMENTATION NOTE: Java will not permit using "O" for return type (List<U>).
        public List getIndexedObjects(
                int i_beginInclusive, int i_endExclusive,
                Object getIndexedObjectsArgument)
                throws IOException;
    }

    //##########################################################################

    /**
     * Constructor.
     */
    public IndexedObjectsCache(DataSource mDataSource, int mProactiveFillMargin) {
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
     * @param getIndexedObjectsArgument Object that is passed on in calls to
     * getIndexedObjects.
     */
    public List<O> getIndexedObjects(int i_beginInclusive, int i_endExclusive,
            java.util.function.Predicate<O> acceptCachedObject,
            Object getIndexedObjectsArgument)
            throws IOException {

        if (!this.cachingEnabled) {
            return dataSource.getIndexedObjects(
                    i_beginInclusive, i_endExclusive,
                    getIndexedObjectsArgument);
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
                        acceptCachedObject, getIndexedObjectsArgument);
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
                throw new RuntimeException("Cache failed to fill up all cache slots. This indicates a pure OVT bug.");
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
     * DataSource#getIndexedObjects.
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
     * DataSource#getIndexedObjects.
     */
    private void fillCacheSlots(
            int i_beginInclusive, int i_endExclusive,
            Object getIndexedObjectsArgument)
            throws IOException {
        final List<O> newIndexedObjects = dataSource.getIndexedObjects(
                i_beginInclusive, i_endExclusive, getIndexedObjectsArgument);

        // Check results of call.
        if (newIndexedObjects.size() != i_endExclusive - i_beginInclusive) {
            throw new RuntimeException("dataSource.getIndexedObjects returned an incorrect result. "
                    + "This indicates a pure code bug.");
        }

        // Check results of call and put the results in the relevant cache slots.
        for (int i = i_beginInclusive; i < i_endExclusive; i++) {
            final O newObject = newIndexedObjects.get(i - i_beginInclusive);
            if (newObject == null) {
                throw new RuntimeException("dataSource.getIndexedObjects returned null as a new indexed object. "
                        + "This indicates a pure code bug.");
            }
            cachedObjects.put(i, newObject);  // Replaces preexisting cache object if there was any.
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
            public List getIndexedObjects(int i_begin, int i_end, Object getIndexedObjectsArgument) {
                System.out.println("getIndexedObjects(" + i_begin + ", " + i_end + ", " + getIndexedObjectsArgument + ")");

                final List<Integer> objects = new ArrayList<>();
                for (int i = i_begin; i < i_end; i++) {
                    objects.add(1000 + i);
                }
                return objects;
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
            System.out.println("getIndexedObjects(" + call.a + ", " + call.b + ", " + null + ", " + null + ");");
            List<Integer> actualResult = cache.getIndexedObjects(call.a, call.b, null, null);
            if (actualResult.equals(call.result)) {
                System.out.println("== OK ==");
            } else {
                System.out.println("====================================================  ERROR");
            }
        }

    } // main
}
