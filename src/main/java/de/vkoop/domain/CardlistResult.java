package de.vkoop.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class CardlistResult extends SearchResult<Card>{

	@JsonProperty("cards")
	protected List<Card> items;
}
