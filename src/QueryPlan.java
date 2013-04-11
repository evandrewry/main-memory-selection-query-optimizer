import java.util.ArrayList;
import java.util.List;


class QueryPlan {
	long bitmask;
	int numberOfBT;
	float productOfSelectivities;
	boolean noBranchFlag;
	float cost;
	QueryPlan left;
	QueryPlan right;

	public QueryPlan(long bitmask, int numberOfBT, float productOfSelectivities,
			boolean noBranchFlag, float cost) {
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


    public String getFormattedStatistics(float[] selectivities) {
        return QueryPlanUtils.formatStatistics(selectivities, getFormattedCode(), this.cost);
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
        return QueryPlanUtils.formatTerm(getAtoms());
    }

    public List<Integer> getAtoms() {
        return QueryPlanUtils.atomsOfBitmask(this.bitmask);
    }

    public List<Integer> getIndices() {
        return QueryPlanUtils.indicesOfBitmask(this.bitmask);
    }

    public String getFormattedCode() {
        return QueryPlanUtils.formatCode(getFormattedTerms(), this.noBranchFlag);
    }

}