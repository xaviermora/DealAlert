package com.games_price_tracker.api.steam;

import java.io.IOException;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/appdetails")
@Profile("dev")
public class MockSteamApiController { 
    private final ResourceLoader resourceLoader;

    public MockSteamApiController(ResourceLoader resourceLoader){
        this.resourceLoader = resourceLoader;
    }

    @GetMapping()
    public String getAppDetails() {
        String response=""; 
        try {
            response = new String(resourceLoader.getResource("classpath:some-steam-appdetails-data.json").getInputStream().readAllBytes()); 
        } catch (IOException e) {
            System.out.println(e);
        }

        return response;
    }
    
}
