package com.community.jpremium.common.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class RuntimeDependencyManager implements AutoCloseable {
    private final Logger logger;
    private final Path pluginDirectory;
    private final Path libsDirectory;
    private final Path relocatedDirectory;
    private final ClasspathInjector classpathInjector;

    private final Map<Set<RuntimeDependency>, IsolatedUrlClassLoader> isolatedClassLoaders = new ConcurrentHashMap<>();
    private volatile JarRelocatorBridge jarRelocatorBridge;

    public RuntimeDependencyManager(Path pluginDirectory, Logger logger, ClasspathInjector classpathInjector) {
        this.pluginDirectory = Objects.requireNonNull(pluginDirectory, "pluginDirectory");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.classpathInjector = Objects.requireNonNull(classpathInjector, "classpathInjector");
        this.libsDirectory = pluginDirectory.resolve("libs");
        this.relocatedDirectory = this.libsDirectory.resolve("relocated");
        try {
            Files.createDirectories(this.relocatedDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create runtime dependency directories under: " + this.relocatedDirectory, e);
        }
    }

    public void injectDependencies(Set<RuntimeDependency> dependencies) {
        for (RuntimeDependency dependency : dependencies) {
            Path jarPath = this.resolveDependencyJar(dependency);
            this.classpathInjector.addToClasspath(jarPath);
        }
    }

    public IsolatedUrlClassLoader getIsolatedClassLoader(Set<RuntimeDependency> dependencies) {
        IsolatedUrlClassLoader existing = this.isolatedClassLoaders.get(dependencies);
        if (existing != null) {
            return existing;
        }
        try {
            URL[] urls = dependencies.stream()
                    .map(this::resolveDependencyJar)
                    .map(path -> {
                        try {
                            return path.toUri().toURL();
                        } catch (Exception e) {
                            throw new IllegalStateException("Could not convert dependency jar path to URL: " + path, e);
                        }
                    })
                    .toArray(URL[]::new);
            IsolatedUrlClassLoader created = new IsolatedUrlClassLoader(urls);
            this.isolatedClassLoaders.put(dependencies, created);
            return created;
        } catch (RuntimeException e) {
            throw e;
        }
    }

    private Path resolveDependencyJar(RuntimeDependency dependency) {
        Path originalJarPath = this.libsDirectory.resolve(dependency.getFileName());
        if (Files.notExists(originalJarPath, new LinkOption[0])) {
            this.downloadDependency(dependency, originalJarPath);
        }
        if (!dependency.shouldRelocate()) {
            return originalJarPath;
        }
        Path relocatedJarPath = this.relocatedDirectory.resolve(dependency.getFileName());
        if (Files.notExists(relocatedJarPath, new LinkOption[0])) {
            this.logger.info(() -> "Relocating: " + dependency.getFileName());
            this.getJarRelocatorBridge().relocate(originalJarPath, relocatedJarPath, dependency.getRelocations());
            this.logger.info(() -> "Relocated: " + dependency.getFileName());
        }
        return relocatedJarPath;
    }

    private void downloadDependency(RuntimeDependency dependency, Path targetJarPath) {
        try {
            Files.createDirectories(targetJarPath.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Could not create directory for: " + targetJarPath, e);
        }

        String url = RuntimeDependency.MAVEN_CENTRAL_BASE_URL + dependency.getMavenPath();
        this.logger.info(() -> "Downloading: " + dependency.getFileName());
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            byte[] bytes;
            try (InputStream in = connection.getInputStream()) {
                bytes = in.readAllBytes();
            }

            byte[] actualSha256 = MessageDigest.getInstance("SHA-256").digest(bytes);
            byte[] expectedSha256 = dependency.getExpectedSha256();
            if (!Arrays.equals(actualSha256, expectedSha256)) {
                throw new IOException(
                        "Invalid checksum for " + dependency.getFileName()
                                + " Expected: " + Base64.getEncoder().encodeToString(expectedSha256)
                                + ", Actual: " + Base64.getEncoder().encodeToString(actualSha256)
                );
            }

            Path tempFile = Files.createTempFile(targetJarPath.getParent(), dependency.name().toLowerCase() + "-", ".tmp");
            Files.write(tempFile, bytes, StandardOpenOption.TRUNCATE_EXISTING);
            Files.move(tempFile, targetJarPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            throw new IllegalStateException("Could not download dependency from: " + url, e);
        }
        this.logger.info(() -> "Downloaded: " + dependency.getFileName());
    }

    private JarRelocatorBridge getJarRelocatorBridge() {
        JarRelocatorBridge existing = this.jarRelocatorBridge;
        if (existing != null) {
            return existing;
        }
        synchronized (this) {
            if (this.jarRelocatorBridge != null) {
                return this.jarRelocatorBridge;
            }
            this.jarRelocatorBridge = new JarRelocatorBridge(
                    this.getIsolatedClassLoader(Set.of(RuntimeDependency.ASM, RuntimeDependency.ASM_COMMONS, RuntimeDependency.JAR_RELOCATOR))
            );
            return this.jarRelocatorBridge;
        }
    }

    @Override
    public void close() {
        for (IsolatedUrlClassLoader classLoader : this.isolatedClassLoaders.values()) {
            try {
                classLoader.close();
            } catch (IOException e) {
                this.logger.warning("An error occurred during closing an isolated class loader: " + e.getMessage());
            }
        }
        this.isolatedClassLoaders.clear();
    }

    private static final class JarRelocatorBridge {
        private static final String JAR_RELOCATOR_CLASS = "me.lucko.jarrelocator.JarRelocator";
        private final java.lang.reflect.Constructor<?> constructor;
        private final java.lang.reflect.Method runMethod;

        private JarRelocatorBridge(ClassLoader classLoader) {
            try {
                Class<?> clazz = classLoader.loadClass(JAR_RELOCATOR_CLASS);
                this.constructor = clazz.getDeclaredConstructor(java.io.File.class, java.io.File.class, Map.class);
                this.constructor.setAccessible(true);
                this.runMethod = clazz.getDeclaredMethod("run");
                this.runMethod.setAccessible(true);
            } catch (Exception e) {
                throw new IllegalStateException("Could not initialize jar relocator bridge", e);
            }
        }

        private void relocate(Path inputJar, Path outputJar, Collection<RelocationRule> relocations) {
            Map<String, String> rules = new HashMap<>();
            for (RelocationRule relocation : relocations) {
                rules.put(relocation.pattern(), relocation.relocation());
            }
            try {
                Object relocator = this.constructor.newInstance(inputJar.toFile(), outputJar.toFile(), rules);
                this.runMethod.invoke(relocator);
            } catch (Exception e) {
                throw new IllegalStateException("Could not relocate jar: " + inputJar.getFileName(), e);
            }
        }
    }

    public static final class IsolatedUrlClassLoader extends java.net.URLClassLoader {
        public IsolatedUrlClassLoader(URL[] urls) {
            super(urls, ClassLoader.getSystemClassLoader().getParent());
            ClassLoader.registerAsParallelCapable();
        }
    }
}

