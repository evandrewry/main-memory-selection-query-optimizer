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
    /**
     * configuration stuff (to be read from input file)
     */
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

    /**
     * @param queryFileName path to input file
     * @return list of float arrays, each representing a set of selectivities for a query
     */
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

    public static float combinedCost(QueryPlan left, QueryPlan right) {
        float cost = 0;
        cost += left.getFixedCost() + getM() * left.getQ();
        cost += left.productOfSelectivities*right.cost;
        return cost;
    }

    /**
     * @param bitmask bitmask representation of condition subset
     * @return list of atoms included in the bitmask
     */
    public static List<Integer> atomsOfBitmask(long bitmask) {
        return indicesOfBitmask(bitmask, 1);
    }

    /**
     * @param bitmask bitmask representation of condition subset
     * @return list of indices of 1's in the bitmask
     */
    public static List<Integer> indicesOfBitmask(long bitmask) {
        return indicesOfBitmask(bitmask, 0);
    }

    /**
     * @param bitmask bitmask representation of condition subset
     * @param startIndex where to start counting from
     * @return list of indices of 1's in the bitmask
     */
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

    /**
     * @param selectivities array of selectivity values of the selection conditions
     * @param bitmask bitmask representation of condition subset
     * @return the number of set bits in the bitmask
     */
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

    /**
     * @param selectivities array of selectivity values of the selection conditions
     * @param bitmask bitmask representation of condition subset
     * @return the product of selectivities for the input bitmask
     */
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

    /**
     * @param terms ordered list of &-terms
     * @param noBranch true if the last term should be no-branch
     * @return formatted c code for the query plan
     */
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

    /**
     * @param terms &-terms to be formatted
     * @return joins terms with && conjunctions
     */
    private static String formatTerms(List<String> terms) {
        if (terms.size() == 1) {
            return terms.get(0);
        } else {
            String fst = terms.remove(0);
            return String.format(TERMS_FMT, fst, formatTerms(terms));
        }
    }

    /**
     * @param atoms single conditions to be joined with &-conjunctions
     * @return formatted &-term
     */
    public static String formatTerm(List<Integer> atoms) {
        String term = "";
        for (int atom : atoms) {
            term = term.equals("") ? formatAtom(atom) : String.format(TERM_FMT, term, formatAtom(atom));
        }
        return atoms.size() > 1 ? "(" + term + ")" : term;
    }

    /**
     * @param atom single selection condition
     * @return representation of the selection condition for output C code
     */
    public static String formatAtom(int atom) {
        return String.format(ATOM_FMT, atom, atom);
    }

    /**
     * @param selectivities array of selectivity values of the selection conditions
     * @return selectivities formatted for statistics output
     */
    public static String formatSelectivities(Float[] selectivities) {
        String s = (selectivities.length == 0) ? "" : String.valueOf(selectivities[0]);
        for (int i = 1; i < selectivities.length; i++) {
            s = String.format(SELECTIVITIES_FMT, s, selectivities[i]);
        }
        return s;
    }

    /**
     * Returns formatted optimization statistics to be printed as output
     *
     * @param selectivities array of selectivity values of the selection conditions
     * @param code the C code to be included in the output
     * @param cost cost of the plan
     * @return
     */
    public static String formatStatistics(Float[] selectivities, String code, float cost) {
        return String.format(STATISTICS_FMT, formatSelectivities(selectivities), code, cost);
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
