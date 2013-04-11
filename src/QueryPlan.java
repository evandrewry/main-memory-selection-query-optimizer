import java.util.ArrayList;
import java.util.List;


class QueryPlan {
	long bitmask;
	float productOfSelectivities;
	boolean noBranchFlag;
	float cost;
	QueryPlan left;
	QueryPlan right;

	public QueryPlan(long bitmask, Float[] selectivities) {
	    /* set bitmask and selectivity product */
		this.bitmask = bitmask;
		this.productOfSelectivities = QueryPlanUtils.productOfSelectivities(selectivities, bitmask);

		/* compare to no-branch plan */
	    float branchCost = calculateCost();
		float noBranchCost = calculateNoBranchCost();
        this.noBranchFlag = branchCost > noBranchCost;
        this.cost = this.noBranchFlag ? noBranchCost : branchCost;

	}

	private float calculateNoBranchCost() {
        // TODO Auto-generated method stub
        return 0;
    }

    private float calculateCost() {
        // TODO Auto-generated method stub
        return 0;
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


    public String getFormattedStatistics(Float[] selectivities) {
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