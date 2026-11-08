package com.sangreyoro.matching.business.translation;

import com.sangreyoro.matching.data.UserRepository;
import com.sangreyoro.matching.domain.Student;
import com.sangreyoro.matching.exception.StudentNotFoundException;
import com.sangreyoro.matching.exception.TranslationUnavailableException;

import java.util.logging.Logger;

/**
 * UC2-A2 "Translate profile": shows a candidate's description in the interface language.
 * Uses the Adapter through the {@link TranslationService} interface only.
 * UC2-E4: if translation fails, the original text is returned with a notice; matching is not affected.
 */
public class ProfileDescriptionService {
    private static final Logger LOG = Logger.getLogger(ProfileDescriptionService.class.getName());
    private final UserRepository users;
    private final TranslationService translator;

    public ProfileDescriptionService(UserRepository users, TranslationService translator) {
        this.users = users;
        this.translator = translator;
    }

    public DescriptionView describe(String studentId, String targetLanguage) {
        Student s = users.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        String original = s.description();
        if (original.isBlank() || s.descriptionLanguage().equalsIgnoreCase(targetLanguage)) {
            return new DescriptionView(original, s.descriptionLanguage(), false, null);
        }
        try {
            return new DescriptionView(translator.translate(original, s.descriptionLanguage(), targetLanguage),
                    targetLanguage, true, null);
        } catch (TranslationUnavailableException e) {
            LOG.warning(() -> e.flowId() + " " + e.getMessage());
            return new DescriptionView(original, s.descriptionLanguage(), false,
                    "Translation is not available right now – the original text is shown.");
        }
    }
}
