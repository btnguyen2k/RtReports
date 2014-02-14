package vng.ubase.kafka;

import java.io.UnsupportedEncodingException;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import vng.ubase.Application;
import vng.ubase.Constants;
import vng.ubase.rtstats.Global;
import vng.ubase.rtstats.counter.ICounter;
import vng.ubase.rtstats.counter.ICounterFactory;

import com.github.ddth.commons.utils.JsonUtils;

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

	private Message parseMessage(String msg) {
		try {
			return JsonUtils.fromJsonString(msg, Message.class);
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
				Message msg = parseMessage(message);
				if (msg == null) {
					Logger.warn("Invalid message: " + message);
					continue;
				}
				if (StringUtils.isEmpty(msg.application)) {
					Logger.warn("Missing application name [a]: " + message);
					continue;
				}
				if (StringUtils.isEmpty(msg.counter)) {
					Logger.warn("Missing counter name [c]: " + message);
					continue;
				}
				if (msg.value == null) {
					msg.value = 1.0;
				}
				if (msg.timestamp == null) {
					msg.timestamp = System.currentTimeMillis();
				}

				String product = msg.application;
				String position = msg.counter;
				long timestamp = msg.timestamp.longValue();
				long value = msg.value.longValue();
				String[] keys = Application.createCouterKeys(product, position);
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
			} catch (UnsupportedEncodingException e) {
			}
		}
		Logger.info("Consumer thread [" + this + "] stopped.");
	}
}
