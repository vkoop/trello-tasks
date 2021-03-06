package de.vkoop;

import com.google.common.collect.Lists;
import de.vkoop.domain.Card;
import de.vkoop.domain.CardlistResult;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class TrelloController {

    @Autowired
    private TrelloConfig trelloConfig;

    @Autowired
    private MyTaskScheduler scheduler;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TaskParser parser;



    @ResponseBody
    @RequestMapping("/start")
    public String checkReturning() throws IOException {
        List<Pair<String, String>> args = Lists.newArrayList(
            Pair.of("query", "qqtemplate"),
            Pair.of("modelTypes", "cards")
        );

        String searchUrl = TrelloUrl.createUrl(TrelloUrl.SEARCH).params(args).toString();

        CardlistResult result = restTemplate.getForObject(searchUrl, CardlistResult.class, trelloConfig.API_KEY, trelloConfig.MY_TOKEN);

        // board get all cards
        result.getItems().stream()
            .map(Card::getDesc)
            .map(parser::parseTask)
            .forEach(this::schedule);

        return "Started the sync.";
    }

    private void schedule(Optional<TrelloCardCopyTask> task) {
        if (task.isPresent() && !scheduler.exists(task.get())) {
            scheduler.schedule(task.get(), task.get().cronString);
        }
    }



    @RequestMapping("/trelloConnect")
    public String trelloConnect(HttpServletRequest request) {
        OAuthService service = new ServiceBuilder()
            .provider(TrelloApi.class)
            .apiKey(trelloConfig.API_KEY)
            .apiSecret(trelloConfig.API_SECRET)
            .callback(trelloConfig.CALLBACK_URL)
            .build();

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
    public String trelloCallback(@RequestParam("oauth_verifier") String verifierString,
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

        try {
            PropertiesConfiguration config = new PropertiesConfiguration("application.properties");
            config.setProperty("TRELLOTOKEN", accessToken.getToken());
            config.save();

            trelloConfig.MY_TOKEN = accessToken.getToken();
        } catch (ConfigurationException ex) {
            Logger.getLogger(TrelloController.class.getName()).log(Level.SEVERE, null, ex);
            return "Failed to write token to properties file!";
        }

        return "You have been connected successfully. Token:" + accessToken.getToken();
    }

}
