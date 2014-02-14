package vng.ubase.rtstats.counter.cassandra;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import vng.ubase.rtstats.counter.AbstractCounter;
import vng.ubase.rtstats.counter.CounterBlock;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class CassandraCounter extends AbstractCounter {
	
	private Cluster cluster;
	private Session session;
	private String node =  "127.0.0.1";
	private static final String UPDATE_COUNT_QUERY = "UPDATE upcounter.event_counters set count = count + ? WHERE product = ? AND position = ? AND timestamp = ?";

	private static final String SELECT_COUNT_QUERY = "SELECT * FROM upcounter.event_counters WHERE product = ? AND position = ? AND timestamp <= ? ORDER BY position DESC LIMIT ?";
	
	public CassandraCounter(String name, Long resolution) {
		super(name, resolution);
	}
	
	public boolean connect() {
		if (StringUtils.isEmpty(node)) {
			System.out.println("Empty node!");
			return false;
		}
		cluster = Cluster.builder().addContactPoint(node).build();
		session = cluster.connect();
		return session != null;
	}
	
	public boolean disconnect() {
		if (session != null) {
			session.shutdown();
		}
		if (cluster != null) {
			cluster.shutdown();
		}
		session = null;
		cluster = null;		
		return true;
	}
	
	public boolean closeSession() {
		if (session != null) {
			session.shutdown();
		}
		session = null;
		return true;
	}
	
	@Override
	public void add(long value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add(long timestamp, long value) {
		if (getSession() == null) {
			connect();
		}
		String[] tmp = StringUtils.split(this.getName(), "_") ;
		if (tmp != null && tmp.length > 1) {
			PreparedStatement  stmt = getSession().prepare(UPDATE_COUNT_QUERY);
			BoundStatement boundStatement = new BoundStatement(stmt);
			boundStatement = boundStatement.bind(value, tmp[1], tmp[2], timestamp); 
			try {
				getSession().execute(boundStatement);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
//		closeSession();
	}

	@Override
	public CounterBlock[] get(int numBlocks) {
		return get(System.currentTimeMillis(), numBlocks);
	}

	 /**
     * {@inheritDoc}
     */
    @Override
    public CounterBlock[] get(long timestamp, int numBlocks) {
    	long key = timestamp / getResolution();
//        long[] blockValues = getValues(key, numBlocks);
    	List<Long[]> blockValues = getCounterValues(key, numBlocks);
        if (blockValues != null && blockValues.size() > 0) {
        	CounterBlock[] result = new CounterBlock[blockValues.size()];
        	for (int i = 0; i < blockValues.size(); i++) {
        		result[i] = new CounterBlock(blockValues.get(i)[0], blockValues.get(i)[1], getResolution());
//        		key--;
        	}
        	return result;
        }
        return null;
    }
    
    private List<Long[]> getCounterValues(long timestamp, int numBlocks) {
    	
    	if (getSession() == null) {
			connect();
		}
		String[] tmp = StringUtils.split(this.getName(), "_") ;
		if (tmp != null && tmp.length > 2) {
			// TODO: Get correct query base on the counter name (tmp[0]).
			PreparedStatement  stmt = getSession().prepare(SELECT_COUNT_QUERY);
			BoundStatement boundStatement = new BoundStatement(stmt);
			boundStatement = boundStatement.bind(tmp[1], tmp[2], timestamp, numBlocks); 
			ResultSet rs = null;
			try {
				rs = getSession().execute(boundStatement);
				List<Row> rows = rs.all();
				if (rows != null && !rows.isEmpty()) {
					List<Long[]> result = new ArrayList<>();
					for (Row row: rows) {
						result.add(new Long[]{row.getLong("timestamp"), row.getLong("count")});
					}
					return result;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
//				closeSession();
			}
		}
//		closeSession();
		return null;
    }

	@Override
	public long[] getValues(int numBlocks) {
		return getValues(System.currentTimeMillis(), numBlocks);
	}

	@Override
	public long[] getValues(long timestamp, int numBlocks) {
		if (getSession() == null) {
			connect();
		}
		String[] tmp = StringUtils.split(this.getName(), "_") ;
		if (tmp != null && tmp.length > 2) {
			// TODO: Get correct query base on the counter name (tmp[0]).
			PreparedStatement  stmt = getSession().prepare(SELECT_COUNT_QUERY);
			BoundStatement boundStatement = new BoundStatement(stmt);
			boundStatement = boundStatement.bind(tmp[1], tmp[2], timestamp, numBlocks); 
			ResultSet rs = null;
			try {
				rs = getSession().execute(boundStatement);
				List<Row> rows = rs.all();
				if (rows != null && !rows.isEmpty()) {
					long[] result = new long[rows.size()];
					int i = 0;
					for (Row row: rows) {
						result[i] = row.getLong("count");
						i++;
					}
					return result;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
//				closeSession();
			}
		}
//		closeSession();
		return null;
	}
	
	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
	
	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

}
