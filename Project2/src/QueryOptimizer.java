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

public class QueryOptimizer {

	private final Float[] selectivities;
	private QueryPlan[] searchSpace;

	private QueryOptimizer(Float[] selectivities, Properties config) {
		this.selectivities = selectivities;
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
		initializeSearchSpace();
	}

	private void initializeSearchSpace() {
		this.searchSpace = new QueryPlan[2 ^ selectivities.length];
		short bitmap = 1;
		for (int i = 0; i < searchSpace.length; i++) {
			//searchSpace[i] = new QueryPlan(bitmap, numberOfBT, productOfSelectivities, noBranchFlag, cost)
		}
	}
	

	private char[] getStatistics() {
		// TODO
		return null;
	}

	private class QueryPlan {
		short bitmask;
		int numberOfBT;
		double productOfSelectivities;
		boolean noBranchFlag;
		double cost;
		QueryPlan left;
		QueryPlan right;

		public QueryPlan(short bitmask, int numberOfBT, double productOfSelectivities,
				boolean noBranchFlag, double cost) {
			this.bitmask = bitmask;
			this.numberOfBT = numberOfBT;
			this.productOfSelectivities = productOfSelectivities;
			this.noBranchFlag = noBranchFlag;
			this.cost = cost;
		}

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
	
	private static String getFormattedStatistics(float[] selectivities, String code, float cost) {
		return STATISTICS_FMT.format(STATISTICS_FMT, selectivities, code, cost);
	}

	private static final String STATISTICS_FMT = 
			"==================================================================\n"
			+ "%s\n"
			+ "------------------------------------------------------------------\n"
			+ "%s\n"
			+ "------------------------------------------------------------------\n"
			+ "cost: %s\n"
			+ "==================================================================\n";
}