package de.vkoop.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

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
