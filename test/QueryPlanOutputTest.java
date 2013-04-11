import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;


public class QueryPlanOutputTest {
    /* 10101 */
    private static QueryPlan P10101;
    /* 00100 */
    private static QueryPlan P00100;
    /* 10001 */
    private static QueryPlan P10001;

    private static final Float[] selectivities5 = new Float[]{0.5f, 0.2f, 0.8f, 0.2f, 0.4f};

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
        P10101.setChildren(P00100, P10001);

        List<String>  t = P10101.getFormattedTerms();
        String code = P10101.getFormattedCode();
        System.out.println(t);
        System.out.println(code);

    }


    @Test
    public void test10101NoBranch() {
        P10101.setChildren(P00100, P10001);
        P10101.noBranchFlag = true;
        List<String>  t = P10101.getFormattedTerms();
        String code = P10101.getFormattedCode();
        System.out.println(t);
        System.out.println(code);

    }

}
