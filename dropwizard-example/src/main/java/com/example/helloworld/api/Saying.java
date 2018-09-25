package com.example.helloworld.api;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class Saying {
	private long id;

	@Length(max = 3)
	private String content;

	public Saying() {
		// Jackson deserialization
	}

	public Saying(long id, String content) {
		this.id = id;
		this.content = content;
	}

	@JsonProperty
	public long getId() {
		return id;
	}

	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("id", id).add("content", content).toString();
	}
}
