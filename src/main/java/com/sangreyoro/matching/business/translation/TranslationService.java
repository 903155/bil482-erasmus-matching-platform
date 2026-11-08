package com.sangreyoro.matching.business.translation;

import com.sangreyoro.matching.exception.TranslationUnavailableException;

/**
 * ADAPTER pattern – role: Target. The only translation interface the Business Layer knows (SRS 5.2).
 */
public interface TranslationService {
    /**
     * Translates {@code text} from one ISO 639-1 language code to another (e.g. "tr" → "es").
     *
     * @throws TranslationUnavailableException if the external service fails (UC2-E4)
     */
    String translate(String text, String fromLanguage, String toLanguage);
}
