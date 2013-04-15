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


public class QueryOptimizerUtils {


    public static float combinedCost(QueryPlan left, QueryPlan right) {
        float cost = 0;
        cost += left.getFixedCost() + getM() * left.getQ();
        cost += left.productOfSelectivities*right.cost;
        /*
        List<QueryPlan> leaves = left.getLeaves();
        leaves.addAll(right.getLeaves());
        QueryPlan e;
        while (!leaves.isEmpty()) {
            e = leaves.remove(leaves.size() - 1);
            cost += e.getFixedCost() + getM() * e.getQ() + e.productOfSelectivities;
        }
        */
        return cost;
    }


	private static Properties config;

	public static Properties getConfig() {
		return config;
	}

	public static void setConfig(Properties newConfig) {
		config = newConfig;
	}

	public static float getR() {
		return Float.parseFloat(config.getProperty("r"));
	}

	public static float getT() {
		return (float) Integer.parseInt(config.getProperty("t"));
	}

	public static float getL() {
		return (float) Integer.parseInt(config.getProperty("l"));
	}

	public static float getM() {
		return (float) Integer.parseInt(config.getProperty("m"));
	}

	public static float getA() {
		return (float) Integer.parseInt(config.getProperty("a"));
	}

	public static float getF() {
		return (float) Integer.parseInt(config.getProperty("f"));
	}

    public static List<Float[]> readQueryFile(String queryFileName) {
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

    public static String formatCode(List<String> terms, boolean noBranch) {
        if (noBranch) {
            String noBranchTerm = terms.remove(terms.size() - 1);
            if (terms.size() == 0) {
            	return String.format(NO_BRANCH_CODE_FLAT_FMT, noBranchTerm);
            } else {
            	return String.format(NO_BRANCH_CODE_FMT, formatTerms(terms), noBranchTerm);
            }
        } else {
            return String.format(CODE_FMT, formatTerms(terms));
        }
    }

    private static String formatTerms(List<String> terms) {
        if (terms.size() == 1) {
            return terms.get(0);
        } else {
            String fst = terms.remove(0);
            return String.format(TERMS_FMT, fst, formatTerms(terms));
        }
    }

    public static String formatTerm(List<Integer> atoms) {
        String term = "";
        for (int atom : atoms) {
            term = term.equals("") ? formatAtom(atom) : String.format(TERM_FMT, term, formatAtom(atom));
        }
        return atoms.size() > 1 ? "(" + term + ")" : term;
    }

    public static String formatAtom(int atom) {
        return String.format(ATOM_FMT, atom, atom);
    }

    public static String formatSelectivities(Float[] selectivities) {
        String s = (selectivities.length == 0) ? "" : String.valueOf(selectivities[0]);
        for (int i = 1; i < selectivities.length; i++) {
            s = String.format(SELECTIVITIES_FMT, s, selectivities[i]);
        }
        return s;
    }

    public static String formatStatistics(Float[] selectivities, String code, float cost) {
        return String.format(STATISTICS_FMT, formatSelectivities(selectivities), code, cost);
    }


    public static List<Integer> atomsOfBitmask(long bitmask) {
        return indicesOfBitmask(bitmask, 1);
    }

    public static List<Integer> indicesOfBitmask(long bitmask) {
        return indicesOfBitmask(bitmask, 0);
    }

    public static List<Integer> indicesOfBitmask(long bitmask, int startIndex) {
        List<Integer> terms = new ArrayList<Integer>();
        for (int i = startIndex; i <= BITS_PER_BITMASK; i++){
            if ((bitmask & 0x0001) == 0x0001) {
                terms.add(i);
            }
            bitmask >>>= 0x0001;
        }
        return terms;
    }

    public static int numberOfTerms(Float[] selectivities, long bitmask) {
        int count = 0;
        for (Float s : selectivities) {
            if ((bitmask & 0x0001) == 0x0001) {
            	count++;
            }
            bitmask >>>= 0x0001;
        }
        return count;
    }

    public static float productOfSelectivities(Float[] selectivities, long bitmask) {
        float prod = 1;
        for (Float s : selectivities) {
            if ((bitmask & 0x0001) == 0x0001) {
                prod *= s;
            }
            bitmask >>>= 0x0001;
        }
        return prod;
    }

    private static final String STATISTICS_FMT =
        "==================================================================\n"
        + "%s\n"
        + "------------------------------------------------------------------\n"
        + "%s\n"
        + "------------------------------------------------------------------\n"
        + "cost: %s\n"
        + "==================================================================\n";

    private static final String NO_BRANCH_CODE_FLAT_FMT =
            "answer[j] = i;\n" +
            "j += (%s);\n";

    private static final String NO_BRANCH_CODE_FMT =
        "if(%s) {\n" +
        "    answer[j] = i;\n" +
        "    j += (%s);\n" +
        "}\n";
    private static final String CODE_FMT =
        "if(%s) {\n" +
        "    answer[j++] = i;\n" +
        "}\n";
    private static final String ATOM_FMT = "t%d[o%d[i]]";
    private static final String TERM_FMT = "%s & %s";
    private static final String TERMS_FMT = "%s && %s";
    private static final String SELECTIVITIES_FMT = "%s %s";
    public static final short BITS_PER_BITMASK = 64;


}
