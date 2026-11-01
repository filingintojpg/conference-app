package com.maltsev.conference_app.dto;

import jakarta.validation.constraints.NotNull;

public record TransferApplicationRequest(
        @NotNull(message = "targetDirectionId is required")
        Long targetDirectionId
) {}
