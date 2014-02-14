package vng.ubase.rtstats.counter;

import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractCounterFactory implements ICounterFactory {

    private ConcurrentHashMap<String, ICounter> counters = new ConcurrentHashMap<String, ICounter>();

    /**
     * {@inheritDoc}
     */
    @Override
    public ICounter getCounter(String name) {
        return counters.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICounter getCounter(String name, long resolution) {
        ICounter counter = counters.get(name);
        if (counter == null) {
            counter = createCounter(name, resolution);
            counters.putIfAbsent(name, counter);
        }
        return counter;
    }

    protected abstract ICounter createCounter(String name, long resolution);
}
