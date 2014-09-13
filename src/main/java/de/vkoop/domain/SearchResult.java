package de.vkoop.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult<A> {
	
	protected List<A> items;

	public List<A> getItems() {
		return items;
	}

	public void setItems(List<A> items) {
		this.items = items;
	}

}
