package de.vkoop.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BoardlistResult extends SearchResult<Board> {

    @JsonProperty("boards")
    protected List<Card> items;

}
