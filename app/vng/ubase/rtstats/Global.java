package vng.ubase.rtstats;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import play.Application;
import play.Configuration;
import play.GlobalSettings;
import play.Logger;
import play.Play;
import vng.ubase.Constants;
import vng.ubase.kafka.ConsumerThread;
import vng.ubase.rtstats.counter.ICounter;
import vng.ubase.rtstats.counter.ICounterFactory;
import vng.ubase.rtstats.counter.cassandra.CassandraCounterFactory;
import vng.ubase.rtstats.counter.inmem.CounterFactory_AtomicLongMap;
import vng.ubase.zookeeper.ZooKeeperClient;

import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.commons.utils.JsonUtils;

public class Global extends GlobalSettings {
	public final static Map<String, Object> PRODUCT_CONFIG = new HashMap<String, Object>();

	public static String[] getProductNames() {
		return PRODUCT_CONFIG.keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
	}

	@SuppressWarnings("unchecked")
	synchronized public static boolean addCounterNameForProduct(String product,
			String counterName) {
		Object productConfig = PRODUCT_CONFIG.get(product);
		if (productConfig == null) {
			return false;
		}
		List<String> productCounters = DPathUtils.getValue(productConfig,
				"counter_names", List.class);
		if (productCounters == null) {
			productCounters = new ArrayList<String>();
		}
		if (!productCounters.contains(counterName)) {
			productCounters.add(counterName);
		}
		return true;
	}

	public static String[] getCounterNamesForProduct(String product) {
		Object productConfig = PRODUCT_CONFIG.get(product);
		if (productConfig == null) {
			return null;
		}
		List<?> productCounters = DPathUtils.getValue(productConfig,
				"counter_names", List.class);
		return productCounters != null ? productCounters
				.toArray(ArrayUtils.EMPTY_STRING_ARRAY) : null;
	}

	public static String getTopicNameForProduct(String product) {
		Object productConfig = PRODUCT_CONFIG.get(product);
		return DPathUtils.getValue(productConfig, "kafka_topic", String.class);
	}

	public static ICounterFactory counterFactory;
	public static ZooKeeperClient zkClient;
	public static ConsumerConnector consumerConnector;
	public static ExecutorService executorService;
	public static Map<String, Object> kafkaBrokerConfig;
	public static List<Thread> RUNNING_THREADS = new ArrayList<Thread>();

	static {
		System.setProperty("java.awt.headless", "true");
	}

	private static ConsumerConfig createConsumerConfig(String zkConnectString,
			String groupId) {
		Properties props = new Properties();
		props.put("zookeeper.connect", zkConnectString);
		props.put("group.id", groupId);
		props.put("zookeeper.session.timeout.ms", "3600000");
		props.put("zookeeper.sync.time.ms", "1000");
		props.put("auto.commit.interval.ms", "1000");
		return new ConsumerConfig(props);
	}

	@SuppressWarnings("unchecked")
	private void initKafkaConsumers() {
		// load broker configurations
		try {
			String configStr = zkClient.read("/brokers/ids/0");
			kafkaBrokerConfig = JsonUtils.fromJsonString(configStr, Map.class);
			Logger.info(configStr);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		executorService = Executors.newCachedThreadPool();

		String counterType = vng.ubase.Application
				.staticConfigString(Constants.CONF_COUNTER_STORAGE);
		boolean isCassandraCounter = StringUtils.equalsIgnoreCase(counterType,
				"CASSANDRA");

		// init the ConsumerConnector
		String groupId = vng.ubase.Application
				.staticConfigString(Constants.CONF_KAFKA_CONSUMER_GROUP_ID);
		if (!isCassandraCounter) {
			try {
				groupId += "_" + InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
			}
		}
		String zkConnectString = vng.ubase.Application
				.staticConfigString(Constants.CONF_KAFKA_ZOOKEEPER_CONNECT_STRING);
		consumerConnector = Consumer
				.createJavaConsumerConnector(createConsumerConfig(
						zkConnectString, groupId));

		List<String> topicList = new ArrayList<String>();
		String[] productNames = getProductNames();
		for (String product : productNames) {
			String topicName = getTopicNameForProduct(product);
			if (!StringUtils.isEmpty(topicName)) {
				topicList.add(topicName);
			}
		}
		try {
			int totalNumPartitions = 0;
			Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
			for (String topic : topicList) {
				String configStr = zkClient.read("/brokers/topics/" + topic);
				Logger.info("Topic [" + topic + "]: " + configStr);
				Map<String, Object> config = JsonUtils.fromJsonString(
						configStr, Map.class);
				Map<?, ?> partitions = DPathUtils.getValue(config,
						"partitions", Map.class);
				int numPartitions = partitions.size();
				totalNumPartitions += numPartitions;
				Logger.info("\tNumber of partitions: " + numPartitions + "/"
						+ totalNumPartitions);

				if (!isCassandraCounter) {
					// rollback offset on startup to have smooth graph
					long rollbackValue = Constants.KAFKA_ROLLBACK_OFFSET_ON_STARTUP;
					for (int i = 0; i < numPartitions; i++) {
						String path = "/consumers/" + groupId + "/offsets/"
								+ topic + "/" + i;
						long offset = 0;
						try {
							offset = Long.parseLong(zkClient.read(path));
							offset -= rollbackValue;
						} catch (Exception e) {
							offset = 0;
						}
						if (offset < 0) {
							offset = 0;
						}
						try {
							zkClient.write(path, String.valueOf(offset));
						} catch (Exception e) {
						}

						topicCountMap.put(topic, new Integer(numPartitions));
					}
				}
			}
			Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector
					.createMessageStreams(topicCountMap);
			for (String topic : topicList) {
				List<KafkaStream<byte[], byte[]>> streams = consumerMap
						.get(topic);
				for (final KafkaStream<byte[], byte[]> stream : streams) {
					RUNNING_THREADS.add(new ConsumerThread(counterFactory,
							stream));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// RUNNING_THREADS.add(new DemoStatsThead(counterFactory));

		for (Thread t : RUNNING_THREADS) {
			executorService.submit(t);
		}
	}

	private void destroyKafkaConsumers() {
		try {
			if (consumerConnector != null) {
				consumerConnector.shutdown();
				consumerConnector = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if (executorService != null) {
				executorService.shutdown();
				executorService = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initZooKeeperClient() {
		String zkConnectString = vng.ubase.Application
				.staticConfigString(Constants.CONF_KAFKA_ZOOKEEPER_CONNECT_STRING);
		if (StringUtils.isEmpty(zkConnectString)) {
			throw new IllegalStateException("Configuration ["
					+ Constants.CONF_KAFKA_ZOOKEEPER_CONNECT_STRING
					+ "] is missing!");
		}
		zkClient = new ZooKeeperClient(zkConnectString, 3600000);
		try {
			zkClient.init();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void destroyZooKeeperClient() {
		try {
			if (zkClient != null) {
				zkClient.destroy();
				zkClient = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadProductConfigurations() {
		PRODUCT_CONFIG.clear();
		Configuration confProducts = Play.application().configuration()
				.getConfig("products");
		PRODUCT_CONFIG.putAll(confProducts.asMap());
		Logger.info(PRODUCT_CONFIG.toString());
	}

	private void cleanup() {
		for (Thread t : RUNNING_THREADS) {
			try {
				t.interrupt();
			} catch (Exception e) {
			}
		}
		RUNNING_THREADS.clear();

		destroyKafkaConsumers();
		destroyZooKeeperClient();
	}

	private void initCounterFactory() {
		String counterType = vng.ubase.Application
				.staticConfigString(Constants.CONF_COUNTER_STORAGE);
		if (StringUtils.equalsIgnoreCase(counterType, "CASSANDRA")) {
			// store counter data to Cassandra
			String cassandraHost = vng.ubase.Application
					.staticConfigString(Constants.CONF_COUNTER_CASSANDRA_HOST);
			if (StringUtils.isEmpty(cassandraHost)) {
				cassandraHost = "localhost";
			}
			counterFactory = new CassandraCounterFactory(cassandraHost);
		} else {
			// store counter data in memory
			counterFactory = new CounterFactory_AtomicLongMap();
		}
		Set<String> counterKeys = new HashSet<String>();
		String[] productNames = getProductNames();
		for (String product : productNames) {
			String[] counterNames = getCounterNamesForProduct(product);
			for (String counterName : counterNames) {
				String[] keys = vng.ubase.Application.createCouterKeys(product,
						counterName);
				for (String key : keys) {
					counterKeys.add(key);
				}
			}
		}
		for (String counterKey : counterKeys) {
			@SuppressWarnings("unused")
			ICounter couter = counterFactory.getCounter(counterKey,
					Constants.BASE_RESOLUTION);
		}

	}

	@Override
	public void onStart(Application app) {
		super.onStart(app);

		loadProductConfigurations();
		cleanup();

		initCounterFactory();
		initZooKeeperClient();
		initKafkaConsumers();
	}

	@Override
	public void onStop(Application app) {
		cleanup();

		super.onStop(app);
	}

}
