package com.karma.platform.controller;

import com.karma.platform.common.CurrentUser;
import com.karma.platform.common.storage.FileStorageService;
import com.karma.platform.common.storage.PresignedUpload;
import com.karma.platform.dto.UploadDtos;
import com.karma.platform.common.ApiException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_IMAGE_BYTES = 5L * 1024L * 1024L;
    private static final Duration PRESIGN_TTL = Duration.ofMinutes(5);

    private final FileStorageService fileStorageService;
    private final CurrentUser currentUser;

    public UploadController(FileStorageService fileStorageService, CurrentUser currentUser) {
        this.fileStorageService = fileStorageService;
        this.currentUser = currentUser;
    }

    @PostMapping("/presign")
    public UploadDtos.PresignUploadResponse presign(@Valid @RequestBody UploadDtos.PresignUploadRequest request) {
        String contentType = request.contentType().trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "storage.invalid-image-type", "Invalid image type");
        }

        String extension = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "bin";
        };
        String key = "uploads/" + currentUser.id() + "/" + UUID.randomUUID() + "." + extension;
        PresignedUpload presigned = fileStorageService.generatePresignedUploadUrl(key, contentType, PRESIGN_TTL);
        return new UploadDtos.PresignUploadResponse(
                presigned.key(),
                presigned.uploadUrl(),
                presigned.contentType(),
                Math.min(presigned.maxSizeBytes(), MAX_IMAGE_BYTES)
        );
    }
}
