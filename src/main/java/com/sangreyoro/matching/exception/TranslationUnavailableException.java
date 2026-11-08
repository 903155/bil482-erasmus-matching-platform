package com.sangreyoro.matching.exception;

/** UC2-E4: the external translation service failed. Callers fall back to the original text. */
public class TranslationUnavailableException extends MatchingException {
    private static final long serialVersionUID = 1L;

    public TranslationUnavailableException(String message) {
        super("UC2-E4", message);
    }

    @Override
    public int httpStatus() { return 503; }
}
