package de.vkoop.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class CardlistResult extends SearchResult<Card> {

    @JsonProperty("cards")
    protected List<Card> items;
}
