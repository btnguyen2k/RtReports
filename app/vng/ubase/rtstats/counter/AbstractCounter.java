package vng.ubase.rtstats.counter;


public abstract class AbstractCounter implements ICounter {

    private long resolution = DEFAULT_RESOLUTION;
    private int maxNumBlocks = DEFAULT_MAX_NUM_BLOCKS;
    private String name;

    public AbstractCounter() {
    }

    public AbstractCounter(String name, long resolution) {
        this.name = name;
        this.resolution = resolution;
    }

    public long getResolution() {
        return resolution;
    }

    public AbstractCounter setResolution(long resolution) {
        this.resolution = resolution;
        return this;
    }

    public int getMaxNumBlocks() {
        return maxNumBlocks;
    }

    public AbstractCounter setMaxNumBlocks(int maxNumBlocks) {
        this.maxNumBlocks = maxNumBlocks;
        return null;
    }

    public String getName() {
        return name;
    }

    public AbstractCounter setName(String name) {
        this.name = name;
        return this;
    }

    public void init() {
        // double temp = DEFAULT_RESOLUTION;
        // temp /= resolution;
        // temp *= maxNumBlocks;
        // maxNumBlocks = (int) temp;
    }

    public void destroy() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(long value) {
        add(System.currentTimeMillis(), value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CounterBlock[] get(int numBlocks) {
        return get(System.currentTimeMillis(), numBlocks);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long[] getValues(int numBlocks) {
        return getValues(System.currentTimeMillis(), numBlocks);
    }
}
