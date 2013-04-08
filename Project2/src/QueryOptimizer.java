import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;


public class QueryOptimizer {
	
	public static void Main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage:./stage2.sh query_file config.txt");
			return;
		}

		File configFile = new File(args[2]);
		
		try {
		Properties configProps = new Properties();
		configProps.load(new FileInputStream(configFile));
		} catch (IOException exception) {
			System.out.println(exception.getMessage());
			return;
		}
		
		ArrayList<ArrayList<Integer>> queries = readQueryFile(args[1]);
		
		
	}
	
	private static ArrayList<ArrayList<Integer>> readQueryFile(String queryFileName) {
		File queryFile = new File(queryFileName);
		BufferedReader queryReader;
		ArrayList<ArrayList<Integer>> queries = new ArrayList<ArrayList<Integer>>();

		try {
			InputStream queryIn = new BufferedInputStream(
					new FileInputStream(queryFile));
			queryReader = new BufferedReader(new InputStreamReader(queryIn));	

			try {
				while(queryReader.ready()) {
					String line = queryReader.readLine();
					ArrayList<Integer> pValues = new ArrayList<Integer>();
					StringTokenizer tokenizer = new StringTokenizer(line, " ");
					while(tokenizer.hasMoreTokens()) {
						String token = tokenizer.nextToken();
						pValues.add(Integer.parseInt(token));
					}
					queries.add(pValues);
				}
			}
			finally {
					queryReader.close();
			}
		} catch (IOException exception) {
			System.out.println("error");
		}
		
		return queries;
	}
	
	private void optimizeQuery(ArrayList<Integer> selectivities, Properties configProp) {
		
	}
}