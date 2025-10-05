import java.util.*;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private static final int NUM_HANDSHAKE_ROUNDS = 2;
    private static final int NUM_HANDSHAKE_SENT_TX = 1;
    private final int numRounds;
    private boolean[] followees;

    private int roundCount;
    private int[] score;
    private final Set<Transaction> receivedTxSet;
    private final Set<Transaction> sentTxSet;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        this.numRounds = numRounds;

        this.roundCount = 0;
        this.receivedTxSet = new HashSet<>();
        this.sentTxSet = new HashSet<>();
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        this.followees = followees;
        this.score = new int[this.followees.length];
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        this.receivedTxSet.addAll(pendingTransactions);
    }

    /**
     * During Handshake Rounds, only send one transaction among initial transactions
     * After which, send unsent transactions normally
     */
    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        this.roundCount += 1;

        Set<Transaction> ret = new HashSet<>();

        if (this.roundCount <= NUM_HANDSHAKE_ROUNDS) {
            for (Transaction transaction : this.receivedTxSet) {
                ret.add(transaction);
                return ret;
            }
        }

        if (this.roundCount <= this.numRounds) {
            for (Transaction transaction : this.receivedTxSet) {
                if (!this.sentTxSet.contains(transaction)) {
                    ret.add(transaction);
                    this.sentTxSet.add(transaction);
                }
            }
            return ret;
        }

        return this.receivedTxSet;
    }

    /**
     * During Handshake Rounds, increase score of each followee by one if the followee follows the rule of sending only NUM_HANDSHAKE_SENT_TX
     * After which, only receive transactions from the valid followees (those with score = NUM_HANDSHAKE_ROUNDS)
     */
    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        if (this.roundCount <= NUM_HANDSHAKE_ROUNDS) {
            Map<Integer, Integer> senderToNumTxsMap = new HashMap<>();
            for (Candidate candidate : candidates) {
                senderToNumTxsMap.put(candidate.sender, senderToNumTxsMap.getOrDefault(candidate.sender, 0) + 1);
            }

            for (Map.Entry<Integer, Integer> entry : senderToNumTxsMap.entrySet()) {
                if (entry.getValue() == NUM_HANDSHAKE_SENT_TX) {
                    score[entry.getKey()] += 1;
                }
            }
        } else {
            for (Candidate candidate : candidates) {
                if (score[candidate.sender] == NUM_HANDSHAKE_ROUNDS) {
                    this.receivedTxSet.add(candidate.tx);
                }
            }
        }
    }
}
