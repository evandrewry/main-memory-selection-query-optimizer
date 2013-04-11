import java.util.ArrayList;
import java.util.List;


public class QueryPlanUtils {
    public static String formatCode(List<String> terms, boolean noBranch) {
        if (noBranch) {
            String noBranchTerm = terms.remove(terms.size() - 1);
            return String.format(NO_BRANCH_CODE_FMT, formatTerms(terms), noBranchTerm);
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
