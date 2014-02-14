package vng.ubase.zookeeper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZooKeeperClient implements Watcher {

	private String connectString;
	private int sessionTimeout;
	public ZooKeeper zk;

	public ZooKeeperClient() {
	}

	public ZooKeeperClient(String connectString, int sessionTimeout) {
		this.connectString = connectString;
		this.sessionTimeout = sessionTimeout;
	}

	public String getConnectString() {
		return connectString;
	}

	public ZooKeeperClient setConnectString(String connectString) {
		this.connectString = connectString;
		return this;
	}

	public int getSessionTimeout() {
		return sessionTimeout;
	}

	public ZooKeeperClient setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
		return this;
	}

	public String read(String path) throws KeeperException,
			InterruptedException {
		Stat stat = new Stat();
		byte[] data = zk.getData(path, false, stat);
		try {
			return data != null ? new String(data, "UTF-8") : null;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public void write(String path, String value) throws InterruptedException,
			KeeperException {
		try {
			byte[] data = value.getBytes("UTF-8");
			zk.setData(path, data, -1);
		} catch (UnsupportedEncodingException e) {
		}
	}

	private void _connect() throws IOException {
		zk = new ZooKeeper(connectString, sessionTimeout, this);
	}

	private void _close() throws InterruptedException {
		try {
			if (zk != null) {
				zk.close();
			}
		} finally {
			zk = null;
		}
	}

	@SuppressWarnings("unused")
	private void _reconnect() throws InterruptedException, IOException {
		_close();
		_connect();
	}

	public void init() throws Exception {
		_connect();
	}

	public void destroy() throws Exception {
		_close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(WatchedEvent event) {
	}
}
