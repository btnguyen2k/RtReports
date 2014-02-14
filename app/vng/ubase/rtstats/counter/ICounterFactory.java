package vng.ubase.rtstats.counter;

public interface ICounterFactory {

    /**
     * Gets an existing counter.
     * 
     * @param name
     * @return {@code null} if not exists
     */
    public ICounter getCounter(String name);

    /**
     * Gets or Creates a counter.
     * 
     * @param name
     * @param resolution
     * @return
     */
    public ICounter getCounter(String name, long resolution);
}
