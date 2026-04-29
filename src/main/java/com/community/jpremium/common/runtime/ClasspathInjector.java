package com.community.jpremium.common.runtime;

import java.nio.file.Path;

@FunctionalInterface
public interface ClasspathInjector {
    void addToClasspath(Path jarPath);
}

