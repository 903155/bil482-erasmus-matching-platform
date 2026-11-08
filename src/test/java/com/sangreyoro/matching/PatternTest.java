package com.sangreyoro.matching;

import com.sangreyoro.matching.business.notification.MatchEvent;
import com.sangreyoro.matching.business.notification.MatchEventType;
import com.sangreyoro.matching.business.notification.MatchObserver;
import com.sangreyoro.matching.business.translation.DescriptionView;
import com.sangreyoro.matching.business.translation.MockTranslationApiClient;
import com.sangreyoro.matching.business.translation.ProfileDescriptionService;
import com.sangreyoro.matching.business.translation.TranslationAdapter;
import com.sangreyoro.matching.data.CsvUserRepository;
import com.sangreyoro.matching.domain.MatchCriterion;
import com.sangreyoro.matching.exception.TranslationUnavailableException;
import com.sangreyoro.matching.testkit.Test;
import com.sangreyoro.matching.util.Json;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sangreyoro.matching.testkit.Assert.assertEquals;
import static com.sangreyoro.matching.testkit.Assert.assertThrows;
import static com.sangreyoro.matching.testkit.Assert.assertTrue;

/** Tests of the Adapter and Observer parts of UC2 and of the data validation rules. */
public class PatternTest {

    @Test(id = "UC2-T11", covers = "UC2-A2, Adapter pattern (call translation + caching metric)")
    void adapterTranslatesAndCaches() {
        MockTranslationApiClient api = new MockTranslationApiClient();
        TranslationAdapter adapter = new TranslationAdapter(api);
        String es = "Me encanta la fotografía y cocinar comida española.";
        assertEquals("I love photography and cooking Spanish food.", adapter.translate(es, "es", "en"), "es→en");
        assertEquals(1, api.calls(), "one external call");
        adapter.translate(es, "es", "en");
        assertEquals(1, api.calls(), "cache: the second translation makes 0 external calls");
        assertEquals(es, adapter.translate(es, "es", "es"), "same language is returned unchanged");
        api.setOnline(false);
        TranslationUnavailableException e = assertThrows(TranslationUnavailableException.class,
                () -> adapter.translate("Kutu oyunları ve basketbol oynamayı seviyorum.", "tr", "en"),
                "status 503 is converted into our exception");
        assertEquals("UC2-E4", e.flowId(), "flow id");
    }

    @Test(id = "UC2-T12", covers = "UC2-E4, NFR-R.2 (graceful degradation)")
    void descriptionFallsBackToOriginalWhenTranslationFails() {
        MockTranslationApiClient api = new MockTranslationApiClient();
        var users = CsvUserRepository.fromFile(Path.of("data/students.csv"));
        ProfileDescriptionService service = new ProfileDescriptionService(users, new TranslationAdapter(api));
        DescriptionView ok = service.describe("L01", "es");
        assertTrue(ok.translated() && ok.text().startsWith("Me encanta"), "tr→es translated: " + ok.text());
        api.setOnline(false);
        DescriptionView fallback = service.describe("L03", "en");
        assertTrue(!fallback.translated() && fallback.notice() != null, "original text + notice");
        assertEquals("tr", fallback.language(), "original language kept");
    }

    @Test(id = "UC2-T13", covers = "Observer pattern (attach/detach, fault isolation metric, NFR-R.4)")
    void observersAreNotifiedAndIsolatedFromEachOther() {
        Fixture f = new Fixture();
        List<MatchEventType> received = new ArrayList<>();
        MatchObserver broken = e -> { throw new IllegalStateException("simulated e-mail outage"); };
        MatchObserver recorder = (MatchEvent e) -> received.add(e.type());
        f.events.attach(broken);                    // attached BEFORE the recorder
        f.events.attach(recorder);
        assertEquals(3, f.events.observerCount(), "in-app + broken + recorder");

        var r = f.service.sendRequest("E02", "L03", MatchCriterion.HOBBIES);
        f.service.respond(r.id(), "L03", true);
        assertEquals(List.of(MatchEventType.MATCH_REQUESTED, MatchEventType.MATCH_CONFIRMED), received,
                "recorder received both events although the previous observer failed");
        assertEquals(2, f.notifications.findByStudent("L03").size(), "in-app notifications were not lost");

        f.events.detach(recorder);
        f.service.sendRequest("E02", "L01", MatchCriterion.HOBBIES);
        assertEquals(2, received.size(), "a detached observer receives nothing");
    }

    @Test(id = "UC2-T15", covers = "DV-06 (malformed data lines are skipped)")
    void csvLoaderSkipsMalformedLines() throws Exception {
        Path tmp = Files.createTempFile("students", ".csv");
        Files.writeString(tmp, String.join("\n",
                "# comment",
                "X1;Ok Student;LOCAL;Economics;turkish;english:B1;music;;weekends;true;;en;Hi",
                "X2;Bad level;LOCAL;Economics;turkish;english:Z9;music;;weekends;true;;en;Hi",
                "X3;Too few fields;LOCAL",
                "X1;Duplicate id;LOCAL;Economics;turkish;english:B1;music;;weekends;true;;en;Hi"));
        var repo = CsvUserRepository.fromFile(tmp);
        assertEquals(1, repo.findAll().size(), "only the valid line is loaded");
        Files.deleteIfExists(tmp);
    }

    @Test(id = "UC2-T17", covers = "API output safety (JSON escaping)")
    void jsonIsEscaped() {
        String json = Json.write(Map.of("text", "He said \"hi\"\n<script>"));
        assertEquals("{\"text\":\"He said \\\"hi\\\"\\n\\u003cscript>\"}", json, "quotes, new lines and < are escaped");
    }
}
