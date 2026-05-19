package com.karma.platform.dto;

import jakarta.validation.constraints.NotBlank;

public final class UploadDtos {

    private UploadDtos() {
    }

    public record PresignUploadRequest(
            @NotBlank String fileName,
            @NotBlank String contentType
    ) {
    }

    public record PresignUploadResponse(
            String key,
            String uploadUrl,
            String contentType,
            long maxSizeBytes
    ) {
    }
}
