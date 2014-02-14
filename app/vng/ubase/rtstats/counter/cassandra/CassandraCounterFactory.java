package vng.ubase.rtstats.counter.cassandra;

import vng.ubase.rtstats.counter.AbstractCounterFactory;
import vng.ubase.rtstats.counter.ICounter;

public class CassandraCounterFactory extends AbstractCounterFactory {
	
	private String node = "127.0.0.1";
	
	public String getNode() {
		return node;
	}


	public void setNode(String node) {
		this.node = node;
	}
	
	public CassandraCounterFactory(String node) {
		super();
		this.node = node;
	}

	@Override
	protected ICounter createCounter(String name, long resolution) {
		CassandraCounter counter = new CassandraCounter(name, resolution);
		counter.setNode(node);
		return counter;
	}

}
