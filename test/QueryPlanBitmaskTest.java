import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;


public class QueryPlanBitmaskTest {
    /* 10101 */
    private static QueryPlan P10101;
    /* 00100 */
    private static QueryPlan P00100;
    /* 10001 */
    private static QueryPlan P10001;

    @Before
    public void setUp() {
        /* 10101 */
        P10101 = new QueryPlan(0x0015, 0, 0, false, 0);
        /* 00100 */
        P00100 = new QueryPlan(0x0004, 0, 0, false, 0);
        /* 10001 */
        P10001 = new QueryPlan(0x0011, 0, 0, false, 0);
    }

    @Test
    public void test10101() {
        List<Short> expected = Arrays.asList(new Short[]{1, 3, 5});
        assertTrue(P10101.getAtoms().equals(expected));
    }

    @Test
    public void testUnion() {
        P10101.setChildren(P00100, P10001);

        long pb = P10101.bitmask;
        long lur = P00100.unionBitmask(P10001);
        long rul = P10001.unionBitmask(P00100);

        assertTrue(pb == lur);
        assertTrue(pb == rul);
        assertTrue(rul == lur);
    }

    @Test
    public void test111000() {
        /* 111000 */
        List<Short> expected = Arrays.asList(new Short[]{4, 5, 6});
        QueryPlan p = new QueryPlan(0x0038, 0, 0, false, 0);
        assertTrue(p.getAtoms().equals(expected));
    }

    @Test
    public void test1010011() {
        /* 1010011 */
        List<Short> expected = Arrays.asList(new Short[]{1, 2, 5, 7});
        QueryPlan p = new QueryPlan(0x0053, 0, 0, false, 0);
        assertTrue(p.getAtoms().equals(expected));
    }

}
