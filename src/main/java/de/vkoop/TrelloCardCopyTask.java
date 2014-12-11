package de.vkoop;

import de.vkoop.domain.Card;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

public class TrelloCardCopyTask implements Runnable, Identifiable {

    @Component
    public static class Factory {

        @Autowired
        TrelloConfig trelloConfig;

        public TrelloCardCopyTask create(String name, String content,
                String destinationListId, String cronString) {
            TrelloCardCopyTask task = new TrelloCardCopyTask(name, content, destinationListId, cronString);
            task.config = trelloConfig;

            return task;
        }
    }
    
    public final String cronString;

    public final String destinationListId;

    public final String content;

    public final String name;

    @Autowired
    public TrelloConfig config;

    public TrelloCardCopyTask(String name, String content,
            String destinationListId, String cronString) {
        this.destinationListId = destinationListId;
        this.name = name;
        this.content = content;
        this.cronString = cronString;
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

    @Override
    public String getId() {
        return name + destinationListId;
    }
}
