package vng.ubase.demo;

import java.util.Properties;
import java.util.Random;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import vng.ubase.rtstats.counter.ICounterFactory;

public class DemoStatsThead extends Thread {

	private Producer<String, String> producer;
	private String topic = "test";
	private String product = "demo";
	private String[] positions;
	private Random random;

	public DemoStatsThead(ICounterFactory counterFactory) {
		Properties props = new Properties();
		props.put("metadata.broker.list", "10.30.12.79:9092");
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		// props.put("partitioner.class", SimplePartitioner.class.getName());
		props.put("request.required.acks", "1");
		ProducerConfig config = new ProducerConfig(props);
		this.producer = new Producer<String, String>(config);

		// this.positions =
		// Global.POSITIONS.keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
		this.random = new Random(System.currentTimeMillis());
		setDaemon(true);
	}

	public void run() {
		System.out.println("Demo thread [" + this + "] started.");
		while (!interrupted()) {
			try {
				String position = positions[random.nextInt(positions.length)];
				String ip = "10." + random.nextInt(16) + "."
						+ random.nextInt(32) + "." + random.nextInt(256);
				long userId = Math.abs(random.nextInt() / 1000);
				long timestamp = System.currentTimeMillis();
				String log = ip + "\t" + product + "\t" + position + "\t"
						+ userId + "\t" + timestamp;
				KeyedMessage<String, String> data = new KeyedMessage<String, String>(
						topic, ip, log);
				producer.send(data);
				Thread.sleep(random.nextInt(10));
			} catch (InterruptedException e) {
				break;
			}
		}
		producer.close();
		System.out.println("Demo thread [" + this + "] stopped.");
	}
}
