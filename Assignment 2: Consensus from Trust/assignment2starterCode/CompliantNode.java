import java.util.*;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private final double p_malicious;
    private final int numRounds;
    private boolean[] followees;

    private int roundCount;
    private boolean[] blacklisted;
    private final Map<Transaction, Integer> txFreqMap;
    private final Set<Transaction> sentTxSet;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        this.p_malicious = p_malicious;
        this.numRounds = numRounds;

        this.roundCount = 0;

        this.followees = new boolean[0];
        this.txFreqMap = new HashMap<>();
        this.sentTxSet = new HashSet<>();
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        this.followees = followees;
        this.blacklisted = new boolean[this.followees.length];
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
        Set<Transaction> ret = new HashSet<>();

        if (this.roundCount > this.numRounds) {
            return this.txFreqMap.keySet();
        }

        for (Map.Entry<Transaction, Integer> entry : this.txFreqMap.entrySet()) {
            Transaction transaction = entry.getKey();
            Integer freq = entry.getValue();
            if (!this.sentTxSet.contains(transaction)) {
                ret.add(transaction);
                this.sentTxSet.add(transaction);
            }
        }

        return ret;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        Set<Integer> senderSet = new HashSet<>();
        for (Candidate candidate : candidates) {
            senderSet.add(candidate.sender);
        }

        for (int i = 0; i < this.followees.length; ++i) {
            if (this.followees[i] && !senderSet.contains(i)) {
                this.blacklisted[i] = true;
            }
        }

        for (Candidate candidate : candidates) {
            if (!blacklisted[candidate.sender]) {
                this.txFreqMap.put(candidate.tx, this.txFreqMap.getOrDefault(candidate.tx, 0) + 1);
            }
        }
    }
}
