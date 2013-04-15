import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * <p>
 * Class abstraction of a query optimizer for selection conditions in main memory
 * as described in algorithm 4.11 of Kenneth Ross's <i>Selection Conditions in Main
 * Memory</i> [Columbia University, 2004]. An instance of QueryOptimizer represents
 * an optimizer for a single query containing only simple selection conditions.
 * Multiple queries can be optimized concurrently by instantiating multiple
 * QueryOptimizers, which is made easy by the fromList(queries, config) factory method.
 * </p>
 * <p>
 * The optimization is based on avoiding
 * branch mispredictions by optimizing both the order of and the type of
 * &-conjunction between the elements in a set of input selection conditions. The
 * decisions made by the optimizer are based on the selectivities of each input
 * condition as well as several (user-specified) properties of the machine on which
 * the query will be run on. The types of &-conjunctions we choose between are:
 * <ul>
 * <li>
 * Branching-And: This implementation uses the "&&" (boolean) operator between two
 * selection conditions and creates a branch in the compiled code, which leaves us
 * vulnerable to branch misprediction penalties but saves work when the first term
 * is very selective.
 * <pre>
 * for(i=0; i < number_of_records; i++) {
 *     if(f1(r1[i]) && ... && fk(rk[i])) {
 *         answer[j++] = i;
 *     }
 * }
 * </pre>
 * </li>
 * <li>
 * Logical-And: This implementation uses the "&" (bitwise) operator between two
 * selection conditions and does not create a branch in the compiled code. Because
 * there is no branch created in the resulting compiled code, there is no
 * possibility of branch misprediction. However, it can still perform poorly when
 * the first term is very selective, as we must consider the right operand even
 * when the left operand evaluates to false.
 * <pre>
 * for(i = 0; i < number_of_records; i++) {
 *     if(f1(r1[i]) & ... & fk(rk[i])) {
 *         answer[j++] = i;
 *     }
 * }
 * </pre>
 * </li>
 * <li>
 * No-Branch: This implementation pulls the selection condition out of the if-statement
 * altogether, instead placing a series of "Logical-And" ("&") conjunctions directly in
 * the body of the for-loop. Because there is neither an if-statement nor any branching
 * operators, there is no branching at all in the resulting compiled code.
 * <pre>
 * for(i = 0; i < number_of_records; i++) {
 *     answer[j] = i;
 *     j += (f1(r1[i]) & ... & fk(rk[i]));
 * }
 * </pre>
 * </li>
 * </ul>
 * </p>

 *
 * @author Cody De La Vara, Evan Drewry
 *
 */
public class QueryOptimizer {

    private final Float[] selectivities;
    private QueryPlan[] searchSpace;
    private Properties config;
    private boolean finished;

    /**
     * Constructs a latent QueryOptimizer instance from the input data. No optimization or
     * significant memory usage occurs until the optimize() method is called on the instance.
     *
     * @param selectivities floating-point selectivity values for each input selection condition.
     * @param config java properties with information about the machine we are optimizing for.
     */
    private QueryOptimizer(Float[] selectivities, Properties config) {
        this.selectivities = selectivities;
        this.config = config;
        this.finished = false;
    }

    /**
     * Returns an array of new QueryOptimizer instances, one for each set of selectivities in
     * the input.
     *
     * @param queries list of float arrays, where each array represents a single query to be
     * optimized and contains floating-point selectivity values for each selection condition
     * in the query.
     * @param config java properties with information about the machine we are optimizing for.
     * @return an array of new QueryOptimizer instances, one for each set of selectivities in
     * the input
     */
    public static QueryOptimizer[] fromList(List<Float[]> queries, Properties config) {
        QueryOptimizer[] o = new QueryOptimizer[queries.size()];
        for (int i = 0; i < o.length; i++) {
            o[i] = new QueryOptimizer(queries.get(i), config);
        }
        return o;
    }

    /**
     * Algorithm 4.11
     */
    private void optimize() {

        /* set up the search space with power set of selection conditions */
        initializeSearchSpace();

        /* optimize */

        /* set finished flag */
        this.finished = true;
    }

    /**
     * @return formatted statistics about the optimization
     */
    public String getFormattedStatistics() {
        assert finished;
        return searchSpace[searchSpace.length - 1].getFormattedStatistics(selectivities);
    }

    /**
     * <p>
     * Step (1) of Algorithm 4.11
     * </p>
     * <p>
     * Consider all plans with no &&s:
     * Generate all 2k - 1 plans using only &-terms, one plan for each nonempty subset s
     * of S. Store the computed cost (Example 4.5) in A[s].c. If the cost for the No-Branch
     * algorithm is smaller, replace A[s].c by that cost (Example 4.4) and set A[s].b = 1."
     * </p>
     */
    private void initializeSearchSpace() {
        this.searchSpace = new QueryPlan[2 ^ selectivities.length - 1];
        for (int i = 0, bitmask = 1; i < searchSpace.length; i++, bitmask++) {
            searchSpace[i] = new QueryPlan(bitmask, selectivities);
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage:./stage2.sh query_file config.txt");
            return;
        }

        File configFile = new File(args[1]);
        Properties config = new Properties();
        QueryOptimizerUtils.setConfig(config);
        try {
            config.load(new FileInputStream(configFile));
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
            return;
        }

        QueryOptimizer[] optimizers = fromList(QueryOptimizerUtils.readQueryFile(args[0]), config);
        for (QueryOptimizer o : optimizers) {
            o.optimize();
            System.out.print(o.getFormattedStatistics());
        }
    }

}
