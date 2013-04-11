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

	public long union(QueryPlan plan) {
	    return (this.bitmask | plan.bitmask);
	}

	public List<String> getTerms() {
	    List<String> terms = new ArrayList<String>();
	    if (this.left != null && this.right != null) {
	        terms = left.getTerms();
	        terms.addAll(right.getTerms());
	    } else {
	        terms.add(getLocalTerms());
	    }
	    return terms;
	}

	private String getLocalTerms() {
        // TODO Auto-generated method stub
        return null;
    }

    private List<Short> termsOfBitmask(long bitmask) {
        List<Short> terms = new ArrayList<Short>();
        for (short i = 1; i <= BITS_PER_BITMASK; i++){
            if ((bitmask & 0x0001) == 0x0001) {
                terms.add(i);
            }
            bitmask >>>= 0x0001;
        }
        return null;
    }

    private String getFormattedCode(String[] terms, boolean noBranch) {
        if (noBranch) {
            return String.format(NO_BRANCH_CODE_FMT, terms);
        } else {
            return String.format(CODE_FMT, terms);
        }
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
    public static final short BITS_PER_BITMASK = 64;


}