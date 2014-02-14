package vng.ubase.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
	@JsonProperty("a")
	public String application;

	@JsonProperty("c")
	public String counter;

	@JsonProperty("v")
	public Double value;

	@JsonProperty("t")
	public Long timestamp;
}
