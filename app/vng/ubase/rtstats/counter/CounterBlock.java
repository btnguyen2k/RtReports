package vng.ubase.rtstats.counter;

public class CounterBlock {
    private long key;
    private long value;
    private long blockSize;

    public CounterBlock(long key, long value, long blockSize) {
        this.key = key;
        this.value = value;
        this.blockSize = blockSize;
    }

    public long getTimestamp() {
        return key * blockSize;
    }

    public long getKey() {
        return key;
    }

    public long getValue() {
        return value;
    }

    public long getBlockSize() {
        return blockSize;
    }
}
