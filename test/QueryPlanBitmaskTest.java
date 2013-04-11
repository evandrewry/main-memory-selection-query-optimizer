import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;


public class QueryPlanBitmaskTest {

    @Test
    public void test10101() {
        /* 10101 */
        List<Short> expected = Arrays.asList(new Short[]{1, 3, 5});
        QueryPlan p = new QueryPlan(0x0015, 0, 0, false, 0);
        assertTrue(p.getAtoms().equals(expected));
    }

    @Test
    public void testUnion() {
        /* 10101 */
        QueryPlan p = new QueryPlan(0x0015, 0, 0, false, 0);
        /* 00100 */
        QueryPlan l = new QueryPlan(0x0004, 0, 0, false, 0);
        /* 10001 */
        QueryPlan r = new QueryPlan(0x0011, 0, 0, false, 0);
        p.setChildren(l, r);

        long pb = p.bitmask;
        long lur = l.unionBitmask(r);
        long rul = r.unionBitmask(l);

        assertTrue(pb == lur);
        assertTrue(pb == rul);
        assertTrue(rul == lur);


        System.out.println();
        System.out.println();
        System.out.println(r.unionBitmask(l));
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
