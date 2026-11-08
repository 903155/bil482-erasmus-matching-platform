package com.sangreyoro.matching;

import com.sangreyoro.matching.business.translation.MockTranslationApiClient;
import com.sangreyoro.matching.business.translation.ProfileDescriptionService;
import com.sangreyoro.matching.business.translation.TranslationAdapter;
import com.sangreyoro.matching.presentation.MatchController;
import com.sangreyoro.matching.presentation.WebServer;
import com.sangreyoro.matching.testkit.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.sangreyoro.matching.testkit.Assert.assertEquals;
import static com.sangreyoro.matching.testkit.Assert.assertTrue;

/** End-to-end test through HTTP: Presentation → Business → Data, on a random free port. */
public class ApiTest {

    @Test(id = "UC2-T18", covers = "AT-UC2-01/02 end to end, UC2-E1 → HTTP 409, SDD 11.1 API")
    void fullFlowThroughTheHttpApi() throws Exception {
        Fixture f = new Fixture();
        var descriptions = new ProfileDescriptionService(f.users, new TranslationAdapter(new MockTranslationApiClient()));
        WebServer server = new WebServer(0, new MatchController(f.service, descriptions, f.users, f.notifications));
        server.start();
        try {
            HttpClient http = HttpClient.newHttpClient();
            String base = "http://localhost:" + server.port();

            HttpResponse<String> matches = http.send(HttpRequest.newBuilder(URI.create(base
                    + "/api/matches?studentId=E01&criterion=LANGUAGE_EXCHANGE")).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, matches.statusCode(), "GET /api/matches");
            assertTrue(matches.body().contains("\"id\":\"L01\"") && matches.body().contains("\"score\":100"), matches.body());

            HttpResponse<String> created = http.send(HttpRequest.newBuilder(URI.create(base + "/api/requests"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("requesterId=E01&candidateId=L01&criterion=LANGUAGE_EXCHANGE")).build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(201, created.statusCode(), "POST /api/requests");
            assertTrue(created.body().contains("\"status\":\"PENDING\""), created.body());

            HttpResponse<String> incomplete = http.send(HttpRequest.newBuilder(URI.create(base
                    + "/api/matches?studentId=E07&criterion=HOBBIES")).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(409, incomplete.statusCode(), "UC2-E1 mapped to 409");
            assertTrue(incomplete.body().contains("UC2-E1"), incomplete.body());

            HttpResponse<String> page = http.send(HttpRequest.newBuilder(URI.create(base + "/")).build(), HttpResponse.BodyHandlers.ofString());
            assertTrue(page.statusCode() == 200 && page.body().contains("Find a Match"), "web UI served");
        } finally {
            server.stop();
        }
    }
}
