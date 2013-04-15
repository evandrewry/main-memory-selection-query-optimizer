import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


class QueryPlan {
	int k;
    long bitmask;
    float productOfSelectivities;
    boolean noBranchFlag;
    float cost;
    QueryPlan left;
    QueryPlan right;

    public QueryPlan(long bitmask, Float[] selectivities) {
        /* set bitmask and selectivity product */
        this.bitmask = bitmask;
        this.k = QueryOptimizerUtils.numberOfTerms(selectivities, bitmask);
        this.productOfSelectivities = QueryOptimizerUtils.productOfSelectivities(selectivities, bitmask);

        /* compare to no-branch plan */
        float noBranchCost = calculateNoBranchCost();
        float branchCost = calculateCost();
        this.noBranchFlag = branchCost > noBranchCost;
        this.cost = this.noBranchFlag ? noBranchCost : branchCost;
    }

    private float calculateNoBranchCost() {
    	float cost = 0;
    	/* kr + (k - 1)l */
    	cost += k * QueryOptimizerUtils.getR() + (k - 1) * QueryOptimizerUtils.getL();
    	/* f1 + ... + fk */
    	cost += k * QueryOptimizerUtils.getF();
    	/* + a */
    	cost += QueryOptimizerUtils.getA();

        return cost;
    }

    private float calculateCost() {
        // TODO Auto-generated method stub
    	float cost = 0;
    	/* calculate q */
    	float q = this.productOfSelectivities <= .5? this.productOfSelectivities : 1 - this.productOfSelectivities;

    	/* kr + (k - 1)l */
    	cost += k * QueryOptimizerUtils.getR() + (k - 1) * QueryOptimizerUtils.getL();
    	/* f1 + ... + fk */
    	cost += k * QueryOptimizerUtils.getF();
    	/* + t ... */
    	cost += QueryOptimizerUtils.getT();
    	/* mq + p1..pka */
    	cost += QueryOptimizerUtils.getM() * QueryOptimizerUtils.getA() + this.productOfSelectivities * QueryOptimizerUtils.getA();
        return cost;
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

    public int unionIndex(QueryPlan plan) {
        return (int) (unionBitmask(plan) - 1);
    }


    public String getFormattedStatistics(Float[] selectivities) {
        return QueryOptimizerUtils.formatStatistics(selectivities, getFormattedCode(), this.cost);
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
        return QueryOptimizerUtils.formatTerm(getAtoms());
    }

    public List<Integer> getAtoms() {
        return QueryOptimizerUtils.atomsOfBitmask(this.bitmask);
    }

    public List<Integer> getIndices() {
        return QueryOptimizerUtils.indicesOfBitmask(this.bitmask);
    }

    public String getFormattedCode() {
        return QueryOptimizerUtils.formatCode(getFormattedTerms(), this.noBranchFlag);
    }

    public int getCMetric() {
        // TODO Auto-generated method stub
        return 0;
    }

    public float getFixedCost() {
        float cost = 0;
        /* kr + (k - 1)l */
        cost += k * QueryOptimizerUtils.getR() + (k - 1) * QueryOptimizerUtils.getL();
        /* f1 + ... + fk */
        cost += k * QueryOptimizerUtils.getF();
        /* + t ... */
        cost += QueryOptimizerUtils.getT();
        return cost;
    }

    public QueryPlan getLeftMostTerm() {
        QueryPlan q = this;
        while (q.left != null) {
            q = q.left;
        }
        return q;
    }

    public boolean subOptimalByCMetric(QueryPlan s2) {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean subOptimalByDMetric(QueryPlan s2) {
        // TODO Auto-generated method stub
        return false;
    }

}
