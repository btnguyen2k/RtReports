package vng.ubase.rtstats.counter.inmem;

import vng.ubase.rtstats.counter.AbstractCounterFactory;
import vng.ubase.rtstats.counter.ICounter;

public class CounterFactory_AtomicLongMap extends AbstractCounterFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICounter createCounter(String name, long resolution) {
        return new Counter_AtomicLongMap(name, resolution);
    }

}
