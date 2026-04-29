package com.community.jpremium.common.runtime;

import java.util.Objects;

public record RelocationRule(String pattern, String relocation) {
    public RelocationRule {
        pattern = Objects.requireNonNull(pattern, "pattern");
        relocation = Objects.requireNonNull(relocation, "relocation");
    }
}

