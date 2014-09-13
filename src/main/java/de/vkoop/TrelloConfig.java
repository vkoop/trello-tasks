package de.vkoop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class TrelloConfig {
	
	@Value("${API_KEY}")
	public   String API_KEY;
	
	@Value("${API_SECRET}")
	public  String API_SECRET;
	
	@Value("${CALLBACK_URL}")
	public  String CALLBACK_URL;

    @Value("${MY_TOKEN}")
    public String MY_TOKEN;

}
