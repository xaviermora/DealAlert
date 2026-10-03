package com.games_price_tracker.api.steam;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResourceAccessException;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@WireMockTest(httpPort = 8081)
public class SteamClientTest {
    @Autowired
    private ResourceLoader resourceLoader;

    @BeforeEach
    void setupMockApi() throws IOException{
        String dataAppList = new String(resourceLoader.getResource("classpath:some-steam-appdetails-data.json").getInputStream().readAllBytes());
        
        stubFor(get(urlEqualTo("/IStoreService/GetAppList/v1?max_results=10"))
        .willReturn(aResponse().withBody(dataAppList)));

        String dataAppDetails = new String(resourceLoader.getResource("classpath:test-appdetails-response.json").getInputStream().readAllBytes());

        stubFor(
            get(urlPathMatching("/api/appdetails?"))
            .withQueryParam("cc", equalTo("AR"))
            .withQueryParam("appids", equalTo("0,1"))
            .withQueryParam("filters", equalTo("price_overview"))
        .willReturn(aResponse().withBody(dataAppDetails)));
    }

    @Autowired
    SteamClient steamClient;

    @Test
    void shouldGetAndParseAppList(){
        List<AppSteam> games = steamClient.getAppList(10).orElseThrow();

        assertNotNull(games);
        assertEquals(10, games.size());
    }

    @Test
    void shouldGetAndParseMultipleAppDetails() throws ResourceAccessException{
        List<Long> steamIds = List.of(0L,1L);
        List<AppDetailsSteam> appsDetails = steamClient.getMultipleAppDetails(steamIds);            

        assertNotNull(appsDetails);
        assertEquals(2, appsDetails.size());
        assertTrue(appsDetails.get(0).getSuccess());
        assertFalse(appsDetails.get(1).getSuccess());
    }
}
