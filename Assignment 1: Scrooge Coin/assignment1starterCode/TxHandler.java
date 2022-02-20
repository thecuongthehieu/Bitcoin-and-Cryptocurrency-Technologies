import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TxHandler {
    public class Ledger {
        public UTXOPool utxoPool;

        Ledger(UTXOPool utxoPool) {
            this.utxoPool = new UTXOPool(utxoPool);
        }
    }
    public Ledger ledger;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        ledger = new Ledger(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code t x}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS

        // Get all current UTXO in pool
        List<UTXO> allUTXO = ledger.utxoPool.getAllUTXO();

        /** (1) all outputs claimed by {@code tx} are in the current UTXO pool */
        for (Transaction.Input input : tx.getInputs()) {
            UTXO prevUTXO = new UTXO(input.prevTxHash, input.outputIndex);
            if (!ledger.utxoPool.contains(prevUTXO)) {
                return false;
            }
        }

        /** (2) the signatures on each input of {@code tx} are valid */
        for (int index = 0; index < tx.numInputs(); ++index) {
            Transaction.Input input = tx.getInput(index);
            // Get signature
            byte[] signature = input.signature;

            // Get message
            byte[] message = tx.getRawDataToSign(index);

            // Get public key
            UTXO prevUTXO = new UTXO(input.prevTxHash, input.outputIndex);
            Transaction.Output prevOutput = null;
            for (UTXO utxo : allUTXO) {
                if (prevUTXO.equals(utxo)) {
                    prevOutput = ledger.utxoPool.getTxOutput(utxo);
                    break;
                }
            }
            if (prevOutput == null) {
                return false;
            }
            PublicKey publicKey = prevOutput.address;

            // Verify input
            if (!Crypto.verifySignature(publicKey, message, signature)) {
                return false;
            }
        }

        /** (3) no UTXO is claimed multiple times by {@code tx} */
        Set<UTXO> claimedUTXOSet = new HashSet<>();
        for (Transaction.Input input : tx.getInputs()) {
            // Get claimed UTXO
            UTXO prevUTXO = new UTXO(input.prevTxHash, input.outputIndex);
            if (claimedUTXOSet.contains(prevUTXO)) {
                return false;
            }
            claimedUTXOSet.add(prevUTXO);
        }

        /** (4) all of {@code tx}s output values are non-negative */
        for (Transaction.Output output : tx.getOutputs()) {
            if (output.value < 0) {
                return false;
            }
        }

        /** (5) the sum of {@code t x}s input values is greater than or equal to the sum of its output values; and false otherwise. */
        // Get sum of input values
        double sumInputs = 0;
        for (Transaction.Input input : tx.getInputs()) {
            // Get respective output
            UTXO prevUTXO = new UTXO(input.prevTxHash, input.outputIndex);
            Transaction.Output respectiveOutput = null;
            for (UTXO utxo : allUTXO) {
                if (prevUTXO.equals(utxo)) {
                    respectiveOutput = ledger.utxoPool.getTxOutput(utxo);
                    break;
                }
            }
            sumInputs += respectiveOutput.value;
        }

        // Get sum of output values
        double sumOutputs = 0;
        for (Transaction.Output output : tx.getOutputs()) {
            sumOutputs += output.value;
        }

        // Check condition
        if (sumInputs < sumOutputs) {
            return false;
        }

        /** Finally */
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        List<Transaction> validTxs = new ArrayList<>();
        for (Transaction tx : possibleTxs) {
            if (isValidTx(tx)) {
                validTxs.add(tx);

                // Remove oldUTXO
                for (Transaction.Input input : tx.getInputs()) {
                    UTXO prevUTXO = new UTXO(input.prevTxHash, input.outputIndex);
                    ledger.utxoPool.removeUTXO(prevUTXO);
                }

                // Add newUTXO
                for (int index = 0; index < tx.numOutputs(); ++index) {
                    UTXO newUTXO = new UTXO(tx.getHash(), index);
                    ledger.utxoPool.addUTXO(newUTXO, tx.getOutput(index));
                }
            }
        }

        Transaction[] validTxsArr = new Transaction[validTxs.size()];
        validTxsArr = validTxs.toArray(validTxsArr);
        return validTxsArr;
    }
}
