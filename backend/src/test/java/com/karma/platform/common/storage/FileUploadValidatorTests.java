package com.karma.platform.common.storage;

import com.karma.platform.common.ApiException;
import com.karma.platform.config.KarmaStorageProperties;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileUploadValidatorTests {

    private final FileUploadValidator validator = new FileUploadValidator(properties(), messages());

    @Test
    void acceptsValidImage() {
        assertDoesNotThrow(() -> validator.validate("poster.webp", "image/webp", 1024, FileCategory.IMAGE, Locale.ENGLISH));
    }

    @Test
    void rejectsOversizedDocument() {
        ApiException exception = assertThrows(ApiException.class,
                () -> validator.validate("guide.pdf", "application/pdf", 4097, FileCategory.DOCUMENT, Locale.ENGLISH));
        assertEquals("storage.file-too-large", exception.getMessageCode());
    }

    @Test
    void rejectsInvalidContentType() {
        ApiException exception = assertThrows(ApiException.class,
                () -> validator.validate("guide.txt", "text/plain", 512, FileCategory.DOCUMENT, Locale.ENGLISH));
        assertEquals("storage.invalid-document-type", exception.getMessageCode());
    }

    private StaticMessageSource messages() {
        StaticMessageSource source = new StaticMessageSource();
        source.addMessage("storage.file-name-required", Locale.ENGLISH, "File name is required");
        source.addMessage("storage.file-too-large", Locale.ENGLISH, "File too large");
        source.addMessage("storage.invalid-document-type", Locale.ENGLISH, "Invalid document");
        source.addMessage("storage.invalid-image-type", Locale.ENGLISH, "Invalid image");
        return source;
    }

    private KarmaStorageProperties properties() {
        return new KarmaStorageProperties(false, "bucket", "eu-west-1", "", "", "", "", 4096);
    }
}
