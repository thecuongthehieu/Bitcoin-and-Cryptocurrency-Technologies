import java.util.*;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private final int numRounds;

    private int roundCount;

    private boolean[] followees;
    private final Map<Transaction, Integer> txFreqMap;
    private final Set<Transaction> sentTxSet;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        this.roundCount = 0;
        this.numRounds = numRounds;

        this.followees = new boolean[0];
        this.txFreqMap = new HashMap<>();
        this.sentTxSet = new HashSet<>();
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        for (Transaction transaction : pendingTransactions) {
            this.txFreqMap.put(transaction, 1);
        }
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        this.roundCount += 1;
        if (this.roundCount > this.numRounds) {
            return this.txFreqMap.keySet();
        }

        Set<Transaction> res = new HashSet<>();
        for (Transaction transaction : this.txFreqMap.keySet()) {
            if (!this.sentTxSet.contains(transaction)) {
                res.add(transaction);
                this.sentTxSet.add(transaction);
            }
        }

        return res;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        for (Candidate candidate : candidates) {
            this.txFreqMap.put(candidate.tx, this.txFreqMap.getOrDefault(candidate.tx, 0) + 1);
        }
    }
}
