// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    private static class Fork {
        private int height;
        public Block topBlock;
        public UTXOPool utxoPool;

        public Fork(int height, Block topBlock, UTXOPool utxoPool) {
            this.height = height;
            this.topBlock = topBlock;
            this.utxoPool = utxoPool;
        }
    }

    private List<Fork> forkList;
    private TransactionPool transactionPool;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        this.forkList = new ArrayList<>();
        UTXOPool utxoPool = new UTXOPool();
        Transaction genesisCoinbaseTx = genesisBlock.getCoinbase();
        for (int id = 0; id < genesisCoinbaseTx.numOutputs(); ++id) {
            UTXO utxo = new UTXO(genesisCoinbaseTx.getHash(), id);
            utxoPool.addUTXO(utxo, genesisCoinbaseTx.getOutput(id));
        }
        this.forkList.add(new Fork(1, genesisBlock, utxoPool));

        this.transactionPool = new TransactionPool();
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        return this.getMaxHeightFork().topBlock;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return this.getMaxHeightFork().utxoPool;
    }

    private Fork getMaxHeightFork() {
        Fork maxHeightFork = this.forkList.get(0);
        for (Fork fork : forkList) {
            if (maxHeightFork.height < fork.height) {
                maxHeightFork = fork;
            }
        }
        return maxHeightFork;
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
        int forkIndex = -1;
        for (int i = 0; i < this.forkList.size(); ++i) {
            if (new ByteArrayWrapper(block.getPrevBlockHash()).equals(new ByteArrayWrapper(this.forkList.get(i).topBlock.getHash()))) {
                forkIndex = i;
                break;
            }
        }
        if (forkIndex == -1) {
            return false;
        }

        // Add transactions and verify accepted transactions
        Fork fork = this.forkList.get(0);
        List<Transaction> possibleTxs = new ArrayList<>(block.getTransactions());
        TxHandler txHandler = new TxHandler(fork.utxoPool);
        Transaction[] acceptedTxs = txHandler.handleTxs(possibleTxs.toArray(new Transaction[0]));
        if (acceptedTxs.length != possibleTxs.size()) {
            return false;
        }

        // Update fork
        fork.topBlock = block;
        fork.height += 1;
        fork.utxoPool = txHandler.getUTXOPool();

        // Add the coinbase transaction of the block
        Transaction coinbaseTx = block.getCoinbase();
        for (int id = 0; id < coinbaseTx.numOutputs(); ++id) {
            UTXO utxo = new UTXO(coinbaseTx.getHash(), id);
            fork.utxoPool.addUTXO(utxo, coinbaseTx.getOutput(id));
        }

        // Remove accepted transactions from pool
        for (Transaction tx : acceptedTxs) {
            this.transactionPool.removeTransaction(tx.getHash());
        }

        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        this.transactionPool.addTransaction(tx);
    }
}