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

	private QueryOptimizer(Float[] selectivities, Properties config) {
		this.selectivities = selectivities;
		this.config = config;
	}

	public static QueryOptimizer[] fromList(List<Float[]> queries,
			Properties config) {
		QueryOptimizer[] o = new QueryOptimizer[queries.size()];
		for (int i = 0; i < o.length; i++) {
			o[i] = new QueryOptimizer(queries.get(i), config);
		}
		return o;
	}

	private void optimize() {
	    /* set up the search space with power set of selectivities */
		initializeSearchSpace();

		/* optimize */
	}

	private void initializeSearchSpace() {
		this.searchSpace = new QueryPlan[2 ^ selectivities.length];
		for (int i = 0, bitmask = 1; i < searchSpace.length; i++, bitmask++) {
			searchSpace[i] = new QueryPlan(bitmask, selectivities);
		}
	}


	private String getStatistics() {
		return searchSpace[searchSpace.length - 1].getFormattedStatistics(selectivities);
	}

	private static List<Float[]> readQueryFile(String queryFileName) {
		File queryFile = new File(queryFileName);
		BufferedReader queryReader;
		List<Float[]> queries = new ArrayList<Float[]>();

		try {
			InputStream queryIn = new BufferedInputStream(new FileInputStream(
					queryFile));
			queryReader = new BufferedReader(new InputStreamReader(queryIn));

			try {
				while (queryReader.ready()) {
					String line = queryReader.readLine();
					ArrayList<Float> selectivities = new ArrayList<Float>();
					StringTokenizer tokenizer = new StringTokenizer(line, " ");
					while (tokenizer.hasMoreTokens()) {
						String token = tokenizer.nextToken();
						selectivities.add(Float.parseFloat(token));
					}
					queries.add(selectivities.toArray(new Float[selectivities
							.size()]));
				}
			} finally {
				queryReader.close();
			}
		} catch (IOException exception) {
			System.out.println("error");
		}

		return queries;
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage:./stage2.sh query_file config.txt");
			return;
		}

		File configFile = new File(args[2]);
		Properties config = new Properties();
		try {
			config.load(new FileInputStream(configFile));
		} catch (IOException exception) {
			System.out.println(exception.getMessage());
			return;
		}

		QueryOptimizer[] optimizers = fromList(readQueryFile(args[1]), config);
		for (QueryOptimizer o : optimizers) {
			o.optimize();
			System.out.print(o.getStatistics());
		}
	}

}