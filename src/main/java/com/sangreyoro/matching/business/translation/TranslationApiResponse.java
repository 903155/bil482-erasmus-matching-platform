package com.sangreyoro.matching.business.translation;

/** Response format of the third-party translation API (status code + payload), not of our system. */
public record TranslationApiResponse(int statusCode, String translatedPayload, String errorMessage) {
}
