/*=========================================================================
 
 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/util/DiscreteIntervalToListCache.java $
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
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
 * DEFINITION: "Cache slot", or just "slot" refers to a "place"/"slot"/"site"
 * inside the cache where one object returned from the function (corresponding
 * to a specific integer) may or may not already be cached. This concept is onyl
 * relevant for the implementation and for configuring the cache.
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
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015
 */
// PROPOSAL: Use WeakHashMap as basis for cache.
//    PRO: No need to manually restrict size of cache.
//    CON: Weak references probably do not take the usage of the object into account.
//       Ex: How often a cached object is used, or how recently it was used, does not
//           influence how likely it is to be garbage collected.
public class DiscreteIntervalToListCache<O extends Serializable> {

    /**
     * Number of extra cache slots to fill (below and above) when completing a
     * request for new indexed objects. Used for proactive caching, i.e.
     * intentionally filling more cache slots than immediately needed to satisfy
     * the current request for data.
     */
    private int proactiveCachingFillMargin;
    private final transient DataSource dataSource;
    private final TreeMap<Integer, O> cachedObjects = new TreeMap<>();  // NOTE: HashMap/TreeMap permit null values.

    /**
     * Flag for whether caching is enabled or whether data is retrieved "fresh"
     * every time. For debugging purposes.
     */
    private boolean cachingEnabled = true;

    //##########################################################################
    public interface DataSource {

        /**
         * Method that returns requested objects from the source. The returned
         * objects will be cached.
         *
         * NOTE: An implementing class must NOT throw exception merely because
         * it is known that there is no data for the specified interval, e.g.
         * data gap or outside some natural interval for which there is data.
         * Instead it must return something that represents the absence of data.
         *
         * IMPLEMENTATION NOTE: Java will not permit using generic class "O" for
         * return type (List<O>).
         *
         * @param i_beginInclusive
         * @param i_endExclusive
         * @param getListArgument Argument passed on from the call to the cache.
         * Can be used to e.g. specify extra parameters on the required
         * "quality", e.g. resolution or possibly the age of data.
         * @return List of objects of class O. These objects should be treated
         * as immutable by all code.
         */
        public List getList(
                int i_beginInclusive, int i_endExclusive,
                Object getListArgument)
                throws IOException;
    }

    //##########################################################################

    /**
     * Constructor.
     */
    public DiscreteIntervalToListCache(DataSource mDataSource, int mProactiveCachingFillMargin) {

        if (mProactiveCachingFillMargin < 0) {
            throw new IllegalArgumentException("Proactive caching fill margin is negative.");
        } else if (mDataSource == null) {
            throw new NullPointerException("Data source is null.");
        }

        this.proactiveCachingFillMargin = mProactiveCachingFillMargin;
        this.dataSource = mDataSource;
    }


    /**
     * Constructor. Initializes cache contents (but not other settings) from a stream.
     */
    public DiscreteIntervalToListCache(
            ObjectInput in, DataSource mDataSource, int mProactiveCachingFillMargin)
            throws IOException {

        if (mProactiveCachingFillMargin < 0) {
            throw new IllegalArgumentException("Proactive caching fill margin is negative.");
        } else if (mDataSource == null) {
            throw new NullPointerException("Data source is null.");
        }

        final int N = in.readInt();
        try {
            for (int i = 0; i < N; i++) {
                final int key = in.readInt();        // NOTE: Read primitive "int", not "Integer" object.
                final O value = (O) in.readObject();
                this.cachedObjects.put(key, value);
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("The Java class needed for reading stream could not be found.", e);
        }

        this.dataSource = mDataSource;
        this.proactiveCachingFillMargin = mProactiveCachingFillMargin;
    }


    /**
     * Write the cached data (but not other settings) to a strean.
     * 
     * IMPLEMENTATION NOTE: The writing to stream captures the class of cached
     * objects, not the class specified through Java generics when instantiating
     * this class ("O").
     */
    public void writeToStream(ObjectOutput out) throws IOException {
        out.writeInt(cachedObjects.size());
        for (Map.Entry<Integer, O> entry : cachedObjects.entrySet()) {
            out.writeInt(entry.getKey());     // NOTE: Write primitive "int", not "Integer" object.
            out.writeObject(entry.getValue());
        }
    }


    public void setCachingEnabled(boolean mCachingEnabled) {
        this.cachingEnabled = mCachingEnabled;
    }


    public void setProactiveCachingFillMargin(int mProactiveCachingFillMargin) {
        if (mProactiveCachingFillMargin < 0) {
            throw new IllegalArgumentException("Proactive caching fill margin is negative.");
        }
        this.proactiveCachingFillMargin = mProactiveCachingFillMargin;
    }


    /**
     * Only supplied to give some sort of basic statistic on how full the cache
     * is.
     */
    public int getNbrOfFilledCacheSlots() {
        return this.cachedObjects.size();
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
                 NOTE: One only wants to cache proactively _IF_ new objects are to
                 be retrieved anyway. Therefore one must first check if any new
                 objects are to be retrieved at all.
                 =============================================================*/
                fillUncachedSlots(
                        i_beginInclusive - this.proactiveCachingFillMargin,
                        i_endExclusive + this.proactiveCachingFillMargin,
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
    // PROPOSAL: Reimplement using new generic utility function that identifies sequences of incrementing integers in array/list.
    //    PRO: Would simplify/clarify the algorithm.
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
                // Set to "true" iff there is an acceptCachedObject, AND it rejects the cached data in the slot.
                // No acceptCachedObject ==> Accept cached data.
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

}
