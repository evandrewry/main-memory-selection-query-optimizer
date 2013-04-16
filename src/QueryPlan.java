import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * Class abstraction of a potential query plan (or plan subset)
 * in our optimization algorithm
 */
class QueryPlan {
	int k;
    long bitmask;
    float productOfSelectivities;
    boolean noBranchFlag;
    float cost;
    QueryPlan left;
    QueryPlan right;

    /**
     * Creates a new QueryPlan from the input bitmask and selectivity array,
     * appropriately calculating the product of selectivities and setting the
     * no-branch flag if the no-branch plan is better
     *
     * @param bitmask bitmask representation of condition subset
     * @param selectivities array of selectivity values of the selection conditions
     */
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

    /**
     * @return value of Q for this plan in cost calculation
     */
    public float getQ() {
        return this.productOfSelectivities <= .5? this.productOfSelectivities : 1 - this.productOfSelectivities;
    }

    /**
     * Sets the left and right children to the input plans
     * @param left new left child
     * @param right new right child
     */
    public void setChildren(QueryPlan left, QueryPlan right) {
        this.left = left;
        this.right = right;
    }

    /**
     * @param plan plan we are comparing to
     * @return true if the conditions in the plans intersect
     */
    public boolean intersects(QueryPlan plan) {
        return (this.bitmask & plan.bitmask) != 0;
    }

    /**
     * @param plan plan we want to union with
     * @return bitmask of plan that is the union of these two plans
     */
    public long unionBitmask(QueryPlan plan) {
        return (this.bitmask | plan.bitmask);
    }

    /**
     * @param plan plan we want to union with
     * @return index of plan that is the union of these two plans
     */
    public int unionIndex(QueryPlan plan) {
        return (int) (unionBitmask(plan) - 1);
    }

    /**
     * @param selectivities array of selectivity values of the selection conditions
     * @return statistics output to be printed
     */
    public String getFormattedStatistics(Float[] selectivities) {
        return QueryOptimizerUtils.formatStatistics(selectivities, getFormattedCode(), this.cost);
    }

    /**
     * @return list of formatted &-terms for this plan
     */
    public List<String> getFormattedTerms() {
        List<String> terms = new ArrayList<String>();
        if (this.left != null && this.right != null) {
            terms = this.left.getFormattedTerms();
            terms.addAll(this.right.getFormattedTerms());
        } else {
            terms.add(getLocalTerm());
        }
        return terms;
    }

    /**
     * @return formatted &-term of all conditions in the bitmask
     */
    public String getLocalTerm() {
        return QueryOptimizerUtils.formatTerm(getAtoms());
    }

    /**
     * @return id's of the conditions included in this plan
     */
    public List<Integer> getAtoms() {
        return QueryOptimizerUtils.atomsOfBitmask(this.bitmask);
    }

    /**
     * @return indices of the conditions included in this plan
     */
    public List<Integer> getIndices() {
        return QueryOptimizerUtils.indicesOfBitmask(this.bitmask);
    }

    /**
     * @return formatted c code for this plan
     */
    public String getFormattedCode() {
        return QueryOptimizerUtils.formatCode(getFormattedTerms(), this.noBranchFlag);
    }

    /**
     * @return fixed cost of this plan
     */
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

    /**
     * @return the left-most leaf in the plan subtree
     */
    public QueryPlan getLeftMostTerm() {
        QueryPlan q = this;
        while (q.left != null) {
            q = q.left;
        }
        return q;
    }

    /**
     * @return cost of plan if we do not use no-branch optimization
     */
    private float calculateCost() {
        float cost = 0;
        /* calculate q */
        float q = getQ();

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

    /**
     * @return cost of plan if we use no-branch optimization
     */
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

    public boolean subOptimalByCMetric(QueryPlan s2) {
        /* if p2 <= p1 and (p2 - 1/fcost(E2)) < (P1 - 1/Fcost(E1)) */
        float p1 = s2.productOfSelectivities;
        float p2 = getLeftMostTerm().productOfSelectivities;
        float cmetric1 = (p2 - 1) / getLeftMostTerm().getFixedCost();
        float cmetric2 = (p1 - 1) / s2.getFixedCost();
        return p2 <= p1 && cmetric1 < cmetric2;
    }

    public boolean subOptimalByDMetric(QueryPlan s2) {
    	float p1 = s2.productOfSelectivities;
        float p2 = getLeftMostTerm().productOfSelectivities;
        float dmetric1 = getLeftMostTerm().getFixedCost();
        float dmetric2 = s2.getFixedCost();
        return p2 <= p1 && dmetric1 < dmetric2;
    }

    public List<QueryPlan> getLeaves() {
        List<QueryPlan> l = new ArrayList<QueryPlan>();
        if (left != null && right != null) {
            l = left.getLeaves();
            l.addAll(right.getLeaves());
            return l;
        } else {
            l.add(this);
            return l;
        }
    }

}
