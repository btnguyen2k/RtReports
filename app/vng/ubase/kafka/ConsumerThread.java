package vng.ubase.kafka;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;
import play.Logger;
import vng.ubase.Application;
import vng.ubase.Constants;
import vng.ubase.rtstats.Global;
import vng.ubase.rtstats.counter.ICounter;
import vng.ubase.rtstats.counter.ICounterFactory;

import com.github.ddth.commons.utils.DPathUtils;

public class ConsumerThread extends Thread {

	public final static String KEY_APPLICATION = "a";
	public final static String KEY_COUNTER = "c";
	public final static String KEY_VALUE = "v";
	public final static String KEY_TIMESTAMP = "t";

	private KafkaStream<byte[], byte[]> kafakStream;
	private ICounterFactory counterFactory;

	public ConsumerThread(ICounterFactory counterFactory,
			KafkaStream<byte[], byte[]> kafkaStream) {
		this.counterFactory = counterFactory;
		this.kafakStream = kafkaStream;
	}

	private Map<String, Object> parseMessage(String msg) {
		String tokens[] = msg != null ? msg.split("\\t") : null;
		if (tokens == null || tokens.length != 5) {
			return null;
		}
		try {
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("client_ip", tokens[0]);
			result.put(KEY_APPLICATION, tokens[1] + "_impression");
			result.put(KEY_COUNTER, tokens[2]);
			result.put("userid", tokens[3]);
			result.put(KEY_TIMESTAMP, Long.parseLong(tokens[4]));
			result.put(KEY_VALUE, 1);
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	public void run() {
		Logger.info("Consumer thread [" + this + "] started.");
		ConsumerIterator<byte[], byte[]> it = kafakStream.iterator();
		while (it.hasNext()) {
			MessageAndMetadata<byte[], byte[]> mm = it.next();
			try {
				String message = new String(mm.message(), "UTF-8");
				Map<String, Object> msg = parseMessage(message);
				if (msg == null) {
					Logger.warn("Invalid message: " + message);
				} else {
					String product = DPathUtils.getValue(msg, KEY_APPLICATION,
							String.class);
					String position = DPathUtils.getValue(msg, KEY_COUNTER,
							String.class);
					long timestamp = DPathUtils.getValue(msg, KEY_TIMESTAMP,
							Long.class);
					long value = DPathUtils
							.getValue(msg, KEY_VALUE, Long.class);
					String[] keys = Application.createCouterKeys(product,
							position);
					for (String key : keys) {
						ICounter counter = counterFactory.getCounter(key);
						if (counter == null) {
							Global.addCounterNameForProduct(product, position);
							counter = counterFactory.getCounter(key,
									Constants.BASE_RESOLUTION);
						}
						if (counter != null) {
							counter.add(timestamp, value);
						}
					}
				}
			} catch (UnsupportedEncodingException e) {
			}
		}
		Logger.info("Consumer thread [" + this + "] stopped.");
	}
}
