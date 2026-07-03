package com.jacksam.productfilter.dto;

import com.jacksam.productfilter.enums.AccessLevel;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record GrantAccessRequest(
        @NotEmpty List<Long> userIds,
        @NotEmpty List<Long> productIds,
        @NotNull AccessLevel accessLevel
) {}
