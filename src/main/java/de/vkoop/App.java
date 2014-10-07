package de.vkoop;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;


@EnableAutoConfiguration
@ComponentScan
@Configuration
public class App {

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(App.class).run(args);
    }

    @Bean
    public TaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
