// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    /** Represent a node in the blocktree (i.e. blockchain with forks) */
    private static class Node {
        public final Instant ts;
        public Block block;
        public int height;
        public UTXOPool utxoPool;
        public Node parentNode;

        public Node(Instant ts, int height, Block block, UTXOPool utxoPool, Node parentNode) {
            this.ts = ts;
            this.height = height;
            this.block = block;
            this.utxoPool = utxoPool;
            this.parentNode = parentNode;
        }
    }

    private final Map<ByteArrayWrapper, Node> nodeMap;
    private Node maxHeightNode;
    private final TransactionPool transactionPool;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        this.nodeMap = new HashMap<>();

        this.transactionPool = new TransactionPool();
        this.transactionPool.addTransaction(genesisBlock.getCoinbase());
        for (Transaction tx : genesisBlock.getTransactions()) {
            this.transactionPool.addTransaction(tx);
        }

        UTXOPool utxoPool = new UTXOPool();
        Transaction genesisCoinbaseTx = genesisBlock.getCoinbase();
        for (int id = 0; id < genesisCoinbaseTx.numOutputs(); ++id) {
            UTXO utxo = new UTXO(genesisCoinbaseTx.getHash(), id);
            utxoPool.addUTXO(utxo, genesisCoinbaseTx.getOutput(id));
        }
        for (Transaction tx : genesisBlock.getTransactions()) {
            for (int id = 0; id < tx.numOutputs(); ++id) {
                UTXO utxo = new UTXO(tx.getHash(), id);
                utxoPool.addUTXO(utxo, tx.getOutput(id));
            }
        }

        this.maxHeightNode = null;
        this.addNewNode(genesisBlock, utxoPool, null);
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        return this.maxHeightNode == null ? null : maxHeightNode.block;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return this.maxHeightNode == null ? null : maxHeightNode.utxoPool;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return this.transactionPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     *
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     *
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS

        // Verify Genesis Block
        if (block.getPrevBlockHash() == null) {
            return false;
        }

        // Verify prevBlockHash
        Node parentNode = null;
        for (Node node : this.nodeMap.values()) {
            if (new ByteArrayWrapper(block.getPrevBlockHash()).equals(new ByteArrayWrapper(node.block.getHash()))) {
                parentNode = node;
                break;
            }
        }
        if (parentNode == null) {
            return false;
        }

        // Verify height condition
        if (this.maxHeightNode.height > parentNode.height + CUT_OFF_AGE) {
            return false;
        }

        // Add transactions and verify accepted transactions
        List<Transaction> possibleTxs = new ArrayList<>(block.getTransactions());
        TxHandler txHandler = new TxHandler(parentNode.utxoPool);
        Transaction[] acceptedTxs = txHandler.handleTxs(possibleTxs.toArray(new Transaction[0]));
        if (acceptedTxs.length != possibleTxs.size()) {
            return false;
        }

        // Add the coinbase transaction of the block to
        UTXOPool newUTXOPool = txHandler.getUTXOPool();
        Transaction coinbaseTx = block.getCoinbase();
        for (int id = 0; id < coinbaseTx.numOutputs(); ++id) {
            UTXO utxo = new UTXO(coinbaseTx.getHash(), id);
            newUTXOPool.addUTXO(utxo, coinbaseTx.getOutput(id));
        }

        // Add a new node
        this.addNewNode(block, newUTXOPool, parentNode);

        // Remove accepted transactions from pool
        for (Transaction tx : acceptedTxs) {
            this.transactionPool.removeTransaction(tx.getHash());
        }

        return true;
    }

    /** Add a new node to the tree */
    private void addNewNode(Block block, UTXOPool utxoPool, Node parentNode) {
        Node newNode = null;
        if (parentNode == null) {
            newNode = new Node(Instant.now(),1, block, utxoPool, null);
        } else {
            newNode = new Node(Instant.now(),parentNode.height + 1, block, utxoPool, parentNode);
        }

        this.nodeMap.put(new ByteArrayWrapper(block.getHash()), newNode);

        // Update maxHeightNode
        // Magically, it should be updated right here!
        if (this.maxHeightNode == null || this.maxHeightNode.height < newNode.height || (this.maxHeightNode.height == newNode.height && this.maxHeightNode.ts.isAfter(newNode.ts))) {
            maxHeightNode = newNode;
        }
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        this.transactionPool.addTransaction(tx);
    }
}