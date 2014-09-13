package de.vkoop.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BoardlistResult extends SearchResult<Board>{

	@JsonProperty("boards")
	protected List<Card> items;

}