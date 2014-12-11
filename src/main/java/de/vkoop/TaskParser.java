package de.vkoop;

import com.google.common.collect.Lists;
import de.vkoop.domain.BoardlistResult;
import de.vkoop.domain.TList;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Component
public class TaskParser {

    @Autowired
    private TrelloConfig trelloConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TrelloCardCopyTask.Factory taskFactory;

    public Optional<TrelloCardCopyTask> parseTask(String taskDescription) {

        String[] stringParts = taskDescription.split("@@");
        String header = stringParts[0];
        String body = stringParts[1];

        Properties props = new Properties();
        try {
            props.load(new StringReader(header));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }

        String cronString = (String) props.get("repeated");
        String targetBoard = (String) props.get("targetboard");
        String targetList = (String) props.get("targetlist");
        String title = (String) props.get("title");

        List<Pair<String, String>> args = Lists.newArrayList(
            Pair.of("query", "name:" + targetBoard),
            Pair.of("modelTypes", "boards")
        );


        String searchUrl = TrelloUrl.createUrl(TrelloUrl.SEARCH).params(args).toString();
        BoardlistResult result = restTemplate.getForObject(searchUrl, BoardlistResult.class, trelloConfig.API_KEY, trelloConfig.MY_TOKEN);

        String boardId = result.getItems().get(0).getId();
        String url = TrelloUrl.createUrl(TrelloUrl.GET_BOARD_LISTS).toString();
        List<TList> result2 = Arrays.asList(
            restTemplate.getForObject(url, TList[].class, boardId, trelloConfig.API_KEY, trelloConfig.MY_TOKEN)
        );


        Optional<TList> tl2 = result2.stream()
            .filter(tl -> tl.getName().equals(targetList))
            .findFirst();


        return tl2.map(tlist -> {
            String listId = tlist.getId();
            return taskFactory.create(title, body, listId, cronString);
        });
    }
}
