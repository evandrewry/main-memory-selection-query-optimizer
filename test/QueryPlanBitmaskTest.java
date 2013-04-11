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

    private static final Float[] selectivities5 = new Float[]{0.5f, 0.2f, 0.8f, 0.2f, 0.4f};
    private static final Float[] selectivities6 = new Float[]{0.5f, 0.2f, 0.8f, 0.2f, 0.4f, 0.9f};
    private static final Float[] selectivities7 = new Float[]{0.5f, 0.2f, 0.8f, 0.2f, 0.4f, 0.9f, 0.3f};

    @Before
    public void setUp() {
        /* 10101 */
        P10101 = new QueryPlan(0x0015, selectivities5);
        /* 00100 */
        P00100 = new QueryPlan(0x0004, selectivities5);
        /* 10001 */
        P10001 = new QueryPlan(0x0011, selectivities5);
    }

    @Test
    public void test10101() {
        List<Integer> expected = Arrays.asList(new Integer[]{1, 3, 5});
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
        List<Integer> expected = Arrays.asList(new Integer[]{4, 5, 6});
        QueryPlan p = new QueryPlan(0x0038, selectivities6);
        assertTrue(p.getAtoms().equals(expected));
    }

    @Test
    public void test1010011() {
        /* 1010011 */
        List<Integer> expected = Arrays.asList(new Integer[]{1, 2, 5, 7});
        QueryPlan p = new QueryPlan(0x0053, selectivities7);
        assertTrue(p.getAtoms().equals(expected));
    }

}
