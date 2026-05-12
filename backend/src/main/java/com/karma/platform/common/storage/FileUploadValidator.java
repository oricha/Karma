package com.karma.platform.common.storage;

import com.karma.platform.common.ApiException;
import com.karma.platform.config.KarmaStorageProperties;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

public class FileUploadValidator {

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> DOCUMENT_CONTENT_TYPES = Set.of("application/pdf");

    private final KarmaStorageProperties properties;
    private final MessageSource messageSource;

    public FileUploadValidator(KarmaStorageProperties properties, MessageSource messageSource) {
        this.properties = properties;
        this.messageSource = messageSource;
    }

    public void validate(MultipartFile file, FileCategory category, Locale locale) {
        validate(file.getOriginalFilename(), file.getContentType(), file.getSize(), category, locale);
    }

    public void validate(String fileName, String contentType, long size, FileCategory category, Locale locale) {
        if (fileName == null || fileName.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "storage.file-name-required",
                    messageSource.getMessage("storage.file-name-required", null, locale));
        }
        if (size > properties.maxUploadSizeBytes()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "storage.file-too-large",
                    messageSource.getMessage("storage.file-too-large", new Object[]{properties.maxUploadSizeBytes()}, locale));
        }

        Set<String> allowed = category == FileCategory.IMAGE ? IMAGE_CONTENT_TYPES : DOCUMENT_CONTENT_TYPES;
        if (contentType == null || !allowed.contains(contentType.toLowerCase(Locale.ROOT))) {
            String code = category == FileCategory.IMAGE ? "storage.invalid-image-type" : "storage.invalid-document-type";
            throw new ApiException(HttpStatus.BAD_REQUEST, code, messageSource.getMessage(code, null, locale));
        }
    }
}
