import java.util.ArrayList;
import java.util.List;


class QueryPlan {
	long bitmask;
	int numberOfBT;
	double productOfSelectivities;
	boolean noBranchFlag;
	double cost;
	QueryPlan left;
	QueryPlan right;

	public QueryPlan(long bitmask, int numberOfBT, double productOfSelectivities,
			boolean noBranchFlag, double cost) {
		this.bitmask = bitmask;
		this.numberOfBT = numberOfBT;
		this.productOfSelectivities = productOfSelectivities;
		this.noBranchFlag = noBranchFlag;
		this.cost = cost;
	}

	public void setChildren(QueryPlan left, QueryPlan right) {
	    this.left = left;
	    this.right = right;
	    this.cost = left.cost + right.cost;
	}

	public boolean intersects(QueryPlan plan) {
	    return (this.bitmask & plan.bitmask) == 0;
	}

	public long unionBitmask(QueryPlan plan) {
	    return (this.bitmask | plan.bitmask);
	}

	public long unionIndex(QueryPlan plan) {
	    return unionBitmask(plan) - 1;
	}

	public List<String> getFormattedTerms() {
	    List<String> terms = new ArrayList<String>();
	    if (this.left != null && this.right != null) {
	        terms = left.getFormattedTerms();
	        terms.addAll(right.getFormattedTerms());
	    } else {
	        terms.add(getLocalTerm());
	    }
	    return terms;
	}

	public String getLocalTerm() {
	    String term = "";
        for (short atom : getAtoms()) {
            term = term.equals("") ? formatAtom(atom) : String.format(TERM_FMT, term, formatAtom(atom));
        }
        return term;
    }

    public List<Short> getAtoms() {
        return atomsOfBitmask(this.bitmask);
    }

    private static List<Short> atomsOfBitmask(long bitmask) {
        List<Short> terms = new ArrayList<Short>();
        for (short i = 1; i <= BITS_PER_BITMASK; i++){
            if ((bitmask & 0x0001) == 0x0001) {
                terms.add(i);
            }
            bitmask >>>= 0x0001;
        }
        return terms;
    }

    public String getFormattedCode() {
        return getFormattedCode(getFormattedTerms(), this.noBranchFlag);
    }

    private static String getFormattedCode(List<String> terms, boolean noBranch) {
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

    private static String formatAtom(short atom) {
        return String.format(ATOM_FMT, atom, atom);
    }

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
    public static final short BITS_PER_BITMASK = 64;


}