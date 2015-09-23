/*=========================================================================
 
 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/util/SegmentsCacheTest.java $
 Date:      $Date: 2015/09/15 12:00:00 $
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
package ovt.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Test code for SegmentsCache.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se, IRF Uppsala, Sweden
 * @since 2015
 */
public class SegmentsCacheTest {

    public static void main(String[] args) throws IOException {
        Log.setDebugLevel(2);
        //test_removeInterval();
        //test_removeIntervals();
        test_cacheIntegrity();
    }

    static class TestSearchFunction implements SegmentsCache.SearchFunction {

        private final double t_find;


        public TestSearchFunction(double t_find) {
            this.t_find = t_find;
        }


        @Override
        /**
         * Simple function which searches for the t value "t_find" (instance
         * variable). Uses inclusive DataSegment intervals.
         */
        public Object searchDataSegment(SegmentsCache.DataSegment seg, double t_start, SegmentsCache.SearchDirection dir) {
            System.out.println(getClass().getSimpleName() + " # search(seg=" + Arrays.toString(seg.getInterval()) + ", " + t_start + ")");
            final double[] si = seg.getInterval();

            if (dir == SegmentsCache.SearchDirection.DOWN) {
                si[1] = t_start;
            } else {
                si[0] = t_start;
            }
            final boolean inInterval = (si[0] <= t_find) & (t_find <= si[1]);

            if (inInterval) {
                return t_find;
            } else {
                return null;
            }
        }
    }


    private static void test_cacheIntegrity() throws IOException {
        //######################################################################        
        class TestDataSegment implements SegmentsCache.DataSegment {

            @Override
            public double[] getInterval() {
                return Arrays.copyOf(interval, 2);
            }


            @Override
            public SegmentsCache.DataSegment select(double t_begin, double t_end) {
                if (!SegmentsCache.intervalIsSuperset(interval[0], interval[1], t_begin, t_end)) {
                    throw new AssertionError();
                }
                return new TestDataSegment(t_begin, t_end);
            }

            private final double[] interval;


            public TestDataSegment(double a, double b) {
                interval = new double[]{a, b};
            }


            // Needed for correct comparisons of results.
            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                } else if (getClass() != obj.getClass()) {
                    return false;
                }
                final TestDataSegment other = (TestDataSegment) obj;
                if (!Arrays.equals(this.interval, other.interval)) {
                    return false;
                }
                return true;
            }


            @Override
            public String toString() {
                return getClass().getSimpleName() + "{" + "interval=" + Arrays.toString(interval) + '}';
            }

        }
        //######################################################################        
        class TestDataSource implements SegmentsCache.DataSource {

            @Override
            public SegmentsCache.DataSegment getSupersetSegment(double t_begin, double t_end) {
                System.out.println(getClass().getSimpleName() + " # getSupersetSegment(" + t_begin + ", " + t_end + ")");

                if (blockSize > 0) {
                    t_begin = Math.floor(t_begin / blockSize) * blockSize;
                    t_end = Math.ceil(t_end / blockSize) * blockSize;
                    System.out.println("   Actually returned segment ==> (" + t_begin + ", " + t_end + ")");
                }
                return getSegment(t_begin, t_end);
            }


            @Override
            public SegmentsCache.DataSegment mergeAdjacent(List<SegmentsCache.DataSegment> segments) {
                TestDataSegment prevSeg = null;
                for (SegmentsCache.DataSegment scseg : segments) {
                    TestDataSegment seg = (TestDataSegment) scseg;
                    if ((prevSeg != null) && (prevSeg.interval[1] != seg.interval[0])) {
                        throw new AssertionError("Not sorter adjacent data segments.");
                    }
                }

                final TestDataSegment firstSeg = (TestDataSegment) segments.get(0);
                final TestDataSegment lastSeg = (TestDataSegment) segments.get(segments.size() - 1);
                return new TestDataSegment(firstSeg.interval[0], lastSeg.interval[1]);
            }


            // Method to compare cache results with.
            public TestDataSegment getSegment(double t_begin, double t_end) {
                return new TestDataSegment(t_begin, t_end);
            }

            private double blockSize;


            public TestDataSource(double mBlockSize) {
                blockSize = mBlockSize;
            }
        }
        //######################################################################        
        final List<Double> dataSrcBlockSizes = new ArrayList();
        dataSrcBlockSizes.add(-1.0);
        dataSrcBlockSizes.add(1.0);
        dataSrcBlockSizes.add(Math.sqrt(2));
        dataSrcBlockSizes.add(5.0);

        final List<Double> minDataSourceTScales = new ArrayList();
        minDataSourceTScales.add(0.1);
        minDataSourceTScales.add(1.0);
        minDataSourceTScales.add(10.0);
        minDataSourceTScales.add(100.0);

        final List<double[]> testGetSegmentCalls = new ArrayList();
        testGetSegmentCalls.add(new double[]{1, 2});
        testGetSegmentCalls.add(new double[]{3, 4});
        testGetSegmentCalls.add(new double[]{1, 2.5});
        testGetSegmentCalls.add(new double[]{2, 4});
        testGetSegmentCalls.add(new double[]{0, 1});
        testGetSegmentCalls.add(new double[]{-1, 0.5});
        testGetSegmentCalls.add(new double[]{4, 5});
        testGetSegmentCalls.add(new double[]{4, 6});
        testGetSegmentCalls.add(new double[]{7, 8});
        testGetSegmentCalls.add(new double[]{9, 10});
        testGetSegmentCalls.add(new double[]{11, 12});
        testGetSegmentCalls.add(new double[]{13, 14});
        testGetSegmentCalls.add(new double[]{49, 51});

        final List<Object[]> searchCalls = new ArrayList();
        // Test case arguments: t_start, Search direction, t_find, t_expected
        searchCalls.add(new Object[]{0.0, SegmentsCache.SearchDirection.DOWN, 2.0, null});
        //searchCalls.add(new Object[]{0.0, SegmentsCache.SearchDirection.UP, 2.0, 2.0});
        //searchCalls.add(new Object[]{0.0, SegmentsCache.SearchDirection.UP, 20.0, 20.0});

        boolean ok = true;
        for (double dataSrcBlockSize : dataSrcBlockSizes) {
            for (Object[] searchCall : searchCalls) {
                for (double minDataSourceTScale : minDataSourceTScales) {

                    final TestDataSource ds = new TestDataSource(dataSrcBlockSize);
                    final SegmentsCache sc = new SegmentsCache(ds, minDataSourceTScale, -100, 100);

                    for (double[] testCall : testGetSegmentCalls) {
                        final double t_begin = testCall[0];
                        final double t_end = testCall[1];
                        System.out.println("... # getSegment(" + t_begin + ", " + t_end + ")");
                        final TestDataSegment cr = (TestDataSegment) sc.getSegment(t_begin, t_end);
                        final TestDataSegment er = (TestDataSegment) ds.getSegment(t_begin, t_end);
                        //System.out.println("cr = "+cr);
                        //System.out.println("er = "+er);
                        ok = ok && cr.equals(er);
                        printCachedIntervals(sc);
                    }

                    {
                        final Object searchResult = sc.search(
                                (double) searchCall[0],
                                (SegmentsCache.SearchDirection) searchCall[1],
                                new TestSearchFunction((double) searchCall[2]));
                        final Object searchExpected = searchCall[3];

                        ok = ok && (Objects.equals(searchResult, searchExpected));   // NOTE: Comparison only works for non-NaN.
                        printCachedIntervals(sc);
                        /*if (!ok) {
                            throw new AssertionError();
                        }*/
                    }
                }
            }
        }
        if (!ok) {
            throw new AssertionError();
        } else {
            System.out.println("test_cacheIntegrity OK");
        }
    }


    public static void printCachedIntervals(SegmentsCache sc) {
        System.out.println("Cached intervals:");
        for (double[] ci : sc.getCachedTIntervals()) {
            System.out.println("   " + Arrays.toString(ci));
        }

    }


    private static void test_removeIntervals() {
        List<double[]> expResult = new ArrayList();
        List<double[]> B = new ArrayList();
        boolean ok = true;

        expResult.clear();
        expResult.add(new double[]{1, 5});
        B.clear();
        B.add(new double[]{0, 1});
        ok = ok && compareIntervalLists(SegmentsCache.removeIntervals(new double[]{1, 5}, B), expResult);

        expResult.clear();
        expResult.add(new double[]{3, 5});
        B.clear();
        B.add(new double[]{1, 3});
        ok = ok && compareIntervalLists(SegmentsCache.removeIntervals(new double[]{1, 5}, B), expResult);

        expResult.clear();
        expResult.add(new double[]{1, 2});
        expResult.add(new double[]{3, 5});
        B.clear();
        B.add(new double[]{2, 3});
        ok = ok && compareIntervalLists(SegmentsCache.removeIntervals(new double[]{1, 5}, B), expResult);

        expResult.clear();
        expResult.add(new double[]{2, 4});
        B.clear();
        B.add(new double[]{-2, -1});
        B.add(new double[]{0, 2});
        B.add(new double[]{4, 6});
        ok = ok && compareIntervalLists(SegmentsCache.removeIntervals(new double[]{1, 5}, B), expResult);

        expResult.clear();
        expResult.add(new double[]{1, 2});
        expResult.add(new double[]{2.1, 3});
        expResult.add(new double[]{3.1, 4});
        B.clear();
        B.add(new double[]{-2, -1});
        B.add(new double[]{2, 2.1});
        B.add(new double[]{3, 3.1});
        B.add(new double[]{4, 6});
        B.add(new double[]{9, 16});
        ok = ok && compareIntervalLists(SegmentsCache.removeIntervals(new double[]{1, 5}, B), expResult);

        if (!ok) {
            throw new AssertionError();
        } else {
            System.out.println("test_removeIntervals OK");
        }
    }


    private static void test_removeInterval() {
        // NOTE: Assumes knowledge of the sorting of the returned intervals.

        boolean ok = true;

        List<double[]> expResult = new ArrayList();
        expResult.add(new double[]{1, 2});
        expResult.add(new double[]{3, 5});
        //ok = ok && expResult.equals(removeInterval(new double[] {1,5}, new double[] {2,3}));   // Does not work for unknown reason.
        ok = ok && Arrays.deepEquals(expResult.toArray(), SegmentsCache.removeInterval(new double[]{1, 5}, new double[]{2, 3}).toArray());

        expResult.clear();
        expResult.add(new double[]{1, 5});
        ok = ok && Arrays.deepEquals(expResult.toArray(), SegmentsCache.removeInterval(new double[]{1, 5}, new double[]{0, 1}).toArray());

        expResult.clear();
        expResult.add(new double[]{1, 5});
        ok = ok && Arrays.deepEquals(expResult.toArray(), SegmentsCache.removeInterval(new double[]{1, 5}, new double[]{5, 6}).toArray());

        expResult.clear();
        expResult.add(new double[]{3, 5});
        ok = ok && Arrays.deepEquals(expResult.toArray(), SegmentsCache.removeInterval(new double[]{1, 5}, new double[]{0, 3}).toArray());

        expResult.clear();
        expResult.add(new double[]{1, 3});
        ok = ok && Arrays.deepEquals(expResult.toArray(), SegmentsCache.removeInterval(new double[]{1, 5}, new double[]{3, 6}).toArray());

        expResult.clear();
        ok = ok && Arrays.deepEquals(expResult.toArray(), SegmentsCache.removeInterval(new double[]{1, 5}, new double[]{1, 5}).toArray());

        expResult.clear();
        ok = ok && Arrays.deepEquals(expResult.toArray(), SegmentsCache.removeInterval(new double[]{1, 5}, new double[]{0, 6}).toArray());

        if (!ok) {
            throw new AssertionError();
        }
    }


    // Utility function
    private static boolean compareIntervalLists(List<double[]> A, List<double[]> B) {
        final Comparator<double[]> c = new Comparator() {
            public int compare(Object s1, Object s2) {
                final double[] a1 = (double[]) s1;
                final double[] a2 = (double[]) s2;
                int d0_compare = Double.compare(a1[0], a2[0]);
                if (d0_compare != 0) {
                    return d0_compare;
                } else {
                    return Double.compare(a1[1], a2[1]);
                }
                // Not entirely synced with equals, but only for length-2 arrays.
            }
        };

        SortedSet<double[]> setA = new TreeSet(c);
        setA.addAll(A);
        SortedSet<double[]> setB = new TreeSet(c);
        setB.addAll(B);

        return setA.equals(setB);
    }
}
