package vng.ubase.demo;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

public class SimplePartitioner implements Partitioner<String> {
    public SimplePartitioner(VerifiableProperties props) {
    }

    public int partition(String key, int numPartitions) {
        return key.hashCode() % numPartitions;
    }
}