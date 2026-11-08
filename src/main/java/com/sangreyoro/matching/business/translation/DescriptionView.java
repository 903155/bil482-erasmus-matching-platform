package com.sangreyoro.matching.business.translation;

/** Profile description as shown to the user: the text, its language and an optional notice (UC2-E4). */
public record DescriptionView(String text, String language, boolean translated, String notice) {
}
