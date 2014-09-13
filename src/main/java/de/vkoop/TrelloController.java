package de.vkoop;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TrelloApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import de.vkoop.domain.BoardlistResult;
import de.vkoop.domain.Card;
import de.vkoop.domain.CardlistResult;
import de.vkoop.domain.TList;

@Controller
public class TrelloController {

	@Autowired
	private TrelloConfig trelloConfig;

	@Autowired
	private MyTaskScheduler scheduler;

    @Autowired
    private RestTemplate restTemplate;

	@ResponseBody
	@RequestMapping("/checkReturning")
	String checkReturning() throws IOException {
		List<Pair<String, String>> args = Lists.newArrayList(
                Pair.of("query", "qqtemplate"),
                Pair.of("modelTypes", "cards"));

		String searchUrl = TrelloUrl.createUrl(TrelloUrl.SEARCH).params(args).toString();

		CardlistResult result = restTemplate.getForObject(searchUrl,
				CardlistResult.class, trelloConfig.API_KEY, trelloConfig.MY_TOKEN);

		// board get all cards
		result.getItems().stream()
            .map(Card::getDesc)
            .forEach(this::parseTask);

		return "success";
	}

	private void parseTask(String taskDescription) {
		try {
			String content = taskDescription;

			String[] stringParts = content.split("@@");
			String header = stringParts[0];
			String body = stringParts[1];

			Properties props = new Properties();
			props.load(new StringReader(header));

			String cronString = (String) props.get("repeated");
			String targetBoard = (String) props.get("targetboard");
			String targetList = (String) props.get("targetlist");
			String title = (String) props.get("title");

			List<Pair<String, String>> args = Lists.newArrayList(
                Pair.of("query", "name:" + targetBoard),
                Pair.of("modelTypes", "boards"));


			String searchUrl = TrelloUrl.createUrl(TrelloUrl.SEARCH)
                    .params(args)
					.toString();
			BoardlistResult result = restTemplate.getForObject(searchUrl,
					BoardlistResult.class, trelloConfig.API_KEY, trelloConfig.MY_TOKEN);

			String boardId = result.getItems().get(0).getId();
			String url = TrelloUrl.createUrl(TrelloUrl.GET_BOARD_LISTS).toString();
			List<TList> result2 = Arrays.asList(
                    restTemplate.getForObject(url, TList[].class, boardId, trelloConfig.API_KEY, trelloConfig.MY_TOKEN)
            );

			Optional<TList> tl2 = result2.stream()
					.filter(tl -> tl.getName().equals(targetList))
                    .findFirst();


            tl2.map(tlist -> {
                String listId = tlist.getId();
                TrelloCardCopyTask copyTask = new TrelloCardCopyTask(title, body,listId);
                copyTask.config = trelloConfig;
                if (!scheduler.exists(copyTask)) {
                    scheduler.schedule(copyTask, cronString);
                }
                return true;
            });

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@RequestMapping("/trelloConnect")
	String trelloConnect(HttpServletRequest request) {
		OAuthService service = new ServiceBuilder().provider(TrelloApi.class)
				.apiKey(trelloConfig.API_KEY)
				.apiSecret(trelloConfig.API_SECRET)
				.callback(trelloConfig.CALLBACK_URL).build();

		Token requestToken = service.getRequestToken();
		String redirectUrl = service.getAuthorizationUrl(requestToken);
		HttpSession session = request.getSession();
		session.setAttribute("token", requestToken.getToken());
		session.setAttribute("secrent", requestToken.getSecret());

		redirectUrl += "&scope=read,write";

		return "redirect:" + redirectUrl;
	}

	@RequestMapping("/trelloCallback")
	@ResponseBody
	String trelloCallback(
			@RequestParam("oauth_verifier") String verifierString,
			HttpServletRequest request) {
		OAuthService service = new ServiceBuilder()
            .provider(TrelloApi.class)
			.apiKey(trelloConfig.API_KEY)
			.apiSecret(trelloConfig.API_SECRET)
            .build();

		HttpSession session = request.getSession();
		String tokenString = (String) session.getAttribute("token");
		String secret = (String) session.getAttribute("secrent");
		Token token = new Token(tokenString, secret);

		Verifier verifier = new Verifier(verifierString);
		Token accessToken = service.getAccessToken(token, verifier);

		return accessToken.getToken();
	}



}
