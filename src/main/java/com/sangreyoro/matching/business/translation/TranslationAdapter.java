package com.sangreyoro.matching.business.translation;

import com.sangreyoro.matching.exception.TranslationUnavailableException;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ============================================================================================
 * DESIGN PATTERN: ADAPTER (structural) – role: Adapter                              [UC2-A2]
 * --------------------------------------------------------------------------------------------
 * Intent:      convert the interface of the third-party translation API into the
 *              {@link TranslationService} interface that our Business Layer expects.
 * Motivation:  the external API uses another method name, another parameter order, upper-case
 *              language codes and status codes instead of exceptions. We cannot change it, and in
 *              this project it is a mock that will later be replaced by a real provider.
 * Improvement: all knowledge about the external API lives in this one class. Replacing the mock
 *              with a real provider (or a second provider) means writing one new adapter; the
 *              services that translate profile texts do not change.
 * Metrics:     - classes that depend on the external API: 1 (this adapter) instead of every caller;
 *              - classes to change when the provider changes: 1;
 *              - repeated translations are cached: the second translation of the same text makes
 *                0 external calls (test UC2-T11 checks the call counter).
 * ============================================================================================
 */
public class TranslationAdapter implements TranslationService {
    private final MockTranslationApiClient api;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public TranslationAdapter(MockTranslationApiClient api) {
        this.api = api;
    }

    @Override
    public String translate(String text, String fromLanguage, String toLanguage) {
        String from = fromLanguage.toLowerCase(Locale.ROOT), to = toLanguage.toLowerCase(Locale.ROOT);
        if (from.equals(to)) return text;
        String key = from + "|" + to + "|" + text;
        String cached = cache.get(key);
        if (cached != null) return cached;

        // Translate our call into the adaptee's call: (target, source, payload) with upper-case codes.
        TranslationApiResponse r = api.translateText(to.toUpperCase(Locale.ROOT), from.toUpperCase(Locale.ROOT), text);

        // Translate the adaptee's status codes into our exception model.
        if (r.statusCode() != 200) {
            throw new TranslationUnavailableException("Translation " + from + "->" + to + " failed: "
                    + r.statusCode() + " " + r.errorMessage());
        }
        cache.put(key, r.translatedPayload());
        return r.translatedPayload();
    }
}
