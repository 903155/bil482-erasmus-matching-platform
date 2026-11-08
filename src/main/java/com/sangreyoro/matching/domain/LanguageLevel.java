package com.sangreyoro.matching.domain;

/** CEFR level of a language the student wants to practise (UC1-S7, used by UC2 language exchange). */
public enum LanguageLevel {
    A1, A2, B1, B2, C1, C2, NATIVE;

    public static LanguageLevel parse(String s) {
        return valueOf(s.trim().toUpperCase());
    }
}
