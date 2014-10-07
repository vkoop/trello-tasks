package de.vkoop;

import de.vkoop.domain.Card;
import org.springframework.web.client.RestTemplate;

public class TrelloCardCopyTask implements Runnable, Identifiable {

    public final String destinationListId;

    public final String content;

    public final String name;

    public TrelloConfig config;

    public TrelloCardCopyTask(String name, String content,
        String destinationListId) {
        super();
        this.destinationListId = destinationListId;
        this.name = name;
        this.content = content;
    }

    @Override
    public void run() {
        String url = TrelloUrl.createUrl(TrelloUrl.ADD_CARD_TO_LIST).toString();
        Card card = new Card();
        card.setName(name);
        card.setDesc(content);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForLocation(url, card, destinationListId, config.API_KEY, config.MY_TOKEN);
    }

    public String getId() {
        return name + destinationListId;
    }
}
