package ovt.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * JUnit test class.
 *
 * @author Erik P G Johansson, erik.johansson@irfu.se
 */
public class DiscreteIntervalToListCacheTest {

    //##########################################################################
    private static class TestDataSource implements DiscreteIntervalToListCache.DataSource {

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

    //##########################################################################
    // PROPOSAL: Method for making the call.
    //    PRO: Can print arguments.
    private static class Call_getList {

        int a, b;
        List result;


        Call_getList(int a, int b, Object... mResult) {
            this.a = a;
            this.b = b;
            this.result = new ArrayList();
            result.addAll(Arrays.asList(mResult));
        }
    }
    //##########################################################################

    // Seems unused
    //@Rule
    //public TestName name = new TestName();


    @Test
    public void test_CachingIntegrity() throws IOException {

        final List<Call_getList> testCalls = getCallSequence_getList();
        final DiscreteIntervalToListCache cache = new DiscreteIntervalToListCache(new TestDataSource(), 0);

        for (Call_getList call : testCalls) {
            //System.out.println("cache.getIndexedObjects(" + call.a + ", " + call.b + ", " + null + ", " + null + ");");

            final List<Integer> actualResult = cache.getList(call.a, call.b, null, null);
            assertEquals(actualResult, call.result);
        }
    }


    //@Ignore
    /**
     * Test of writeToStream method, of class DiscreteIntervalToListCache.
     *
     * Presently really the same as testCachingIntegrity but with continuous
     * serialization/deserialization.
     */
    @Test
    public void test_Serialization() throws IOException {
        final List<Call_getList> testCalls = getCallSequence_getList();
        DiscreteIntervalToListCache cache = new DiscreteIntervalToListCache(new TestDataSource(), 0);

        for (Call_getList call : testCalls) {
            //System.out.println("cache.getIndexedObjects(" + call.a + ", " + call.b + ", " + null + ", " + null + ");");
            cache = saveToLoadFromStream(cache);
            final List<Integer> actualResult = cache.getList(call.a, call.b, null, null);
            assertEquals(actualResult, call.result);
        }

        // TODO review the generated test code and remove the default call to fail.
    }


    /**
     * Utility function to be used by other test code.
     */
    public static List<Call_getList> getCallSequence_getList() {
        final List<Call_getList> testCalls = new ArrayList();
        testCalls.add(new Call_getList(3, 5, 1003, 1004));
        testCalls.add(new Call_getList(2, 4, 1002, 1003));
        testCalls.add(new Call_getList(0, 6, 1000, 1001, 1002, 1003, 1004, 1005));
        testCalls.add(new Call_getList(3, 5, 1003, 1004));
        testCalls.add(new Call_getList(5, 10, 1005, 1006, null, 1008, 1009));  // Test that can use null as data.
        testCalls.add(new Call_getList(-5, -3, 995, 996));
        return testCalls;
    }


    /**
     * Utility function to be used by other test code.
     */
    public static DiscreteIntervalToListCache saveToLoadFromStream(
            DiscreteIntervalToListCache cache)
            throws IOException {

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(os);
        cache.writeToStream(oos);

        oos.close();
        os.close();
        //######################################################################
        final byte[] data = os.toByteArray();
        //######################################################################
        final InputStream is = new ByteArrayInputStream(data);
        final ObjectInputStream ois = new ObjectInputStream(is);
        final DiscreteIntervalToListCache readCache = new DiscreteIntervalToListCache(ois, new TestDataSource(), 2);
        is.close();
        ois.close();
        return readCache;
    }

}
