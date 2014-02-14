package vng.ubase.rtstats.counter.inmem;

import java.util.Arrays;

import vng.ubase.rtstats.counter.AbstractCounter;
import vng.ubase.rtstats.counter.CounterBlock;
import vng.ubase.rtstats.counter.ICounter;

import com.google.common.util.concurrent.AtomicLongMap;

public class Counter_AtomicLongMap extends AbstractCounter {

	private final static Long[] EMPTY_LONG_ARRAY = new Long[0];

	private int BUFFER_NUM_BLOCKS = 20;
	private AtomicLongMap<Long> counter = AtomicLongMap.create();

	public Counter_AtomicLongMap() {
	}

	public Counter_AtomicLongMap(String name, long resolution) {
		super(name, resolution);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {
		super.init();
		BUFFER_NUM_BLOCKS = (int) (getMaxNumBlocks() * 0.1);
	}

	private void reduce() {
		Long[] keys = counter.asMap().keySet().toArray(EMPTY_LONG_ARRAY);
		Arrays.sort(keys);
		for (int i = 0, n = keys.length - getMaxNumBlocks() + BUFFER_NUM_BLOCKS; i < n; i++) {
			counter.remove(keys[i]);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(long timestamp, long value) {
		long key = timestamp / getResolution();
		counter.addAndGet(key, value);
		if (counter.size() > getMaxNumBlocks()) {
			reduce();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CounterBlock[] get(long timestamp, int numBlocks) {
		long key = timestamp / getResolution();
		long blockSize = getResolution();
		CounterBlock[] result = new CounterBlock[numBlocks];
		for (int i = 0; i < numBlocks; i++) {
			long value = counter.get(key);
			CounterBlock block = new CounterBlock(key, value, blockSize);
			result[numBlocks - i - 1] = block;
			key--;
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long[] getValues(long timestamp, int numBlocks) {
		long key = timestamp / getResolution();
		long[] result = new long[numBlocks];
		for (int i = 0; i < numBlocks; i++) {
			long value = counter.get(key);
			result[numBlocks - i - 1] = value;
			key--;
		}
		return result;
	}

	public static void main(String[] args) throws InterruptedException {
		ICounter counter = new Counter_AtomicLongMap("temp", 1000);
		for (int i = 0; i < 100; i++) {
			counter.add(1);
			long[] values = counter.getValues(10);
			// CounterBlock[] blocks = counter.get(10);
			for (long v : values) {
				System.out.print(v);
				System.out.print(" ");
			}
			System.out.println();
			// for (CounterBlock b : blocks) {
			// System.out.print(new Date(b.getTimestamp()));
			// System.out.print(":");
			// System.out.print(b.getValue());
			// System.out.print(" ");
			// }
			// System.out.println();

			Thread.sleep(100);
		}
	}
}
