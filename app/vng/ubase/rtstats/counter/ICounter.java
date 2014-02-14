package vng.ubase.rtstats.counter;

public interface ICounter {

    /**
     * Default resolution: 1000 ms
     */
    public final static long DEFAULT_RESOLUTION = 1000;

    public final static int DEFAULT_MAX_NUM_BLOCKS = 86400;

    /**
     * Adds a value, uses {@code System.currentTimeMillis()} as key.
     * 
     * @param value
     */
    public void add(long value);

    /**
     * Adds a value, key specified by the supplied timestamp.
     * 
     * @param timestamp
     *            UNIX timestamp in millisec
     * @param value
     */
    public void add(long timestamp, long value);

    /**
     * Gets last {@code numBlocks} blocks.
     * 
     * @param numBlocks
     * @return
     */
    public CounterBlock[] get(int numBlocks);

    /**
     * Gets last {@code numBlocks} values before the specified timestamp.
     * 
     * @param timestamp
     * @param numBlocks
     * @return
     */
    public CounterBlock[] get(long timestamp, int numBlocks);

    /**
     * Gets last {@code numBlocks} values.
     * 
     * @param numBlocks
     * @return
     */
    public long[] getValues(int numBlocks);

    /**
     * Gets last {@code numBlocks} values before the specified timestamp.
     * 
     * @param timestamp
     * @param numBlocks
     * @return
     */
    public long[] getValues(long timestamp, int numBlocks);
}
