package com.community.jpremium.common.runtime;

import java.util.Base64;
import java.util.List;
import java.util.Objects;

public enum RuntimeDependency {
    ASM(
            "org.ow2.asm",
            "asm",
            "9.7",
            "rfRtXjSUC98Ujs3Sap7o7qlElqcgNP9xQQZrPupcTp0=",
            List.of()
    ),
    ASM_COMMONS(
            "org.ow2.asm",
            "asm-commons",
            "9.7",
            "OJvCR5WOBJ/JoECNOYySxtNwwYA1EgOV1Muh2dkwS3o=",
            List.of()
    ),
    JAR_RELOCATOR(
            "me.lucko",
            "jar-relocator",
            "1.7",
            "b30RhOF6kHiHl+O5suNLh/+eAr1iOFEFLXhwkHHDu4I=",
            List.of()
    ),
    SLF4J_API(
            "org.slf4j",
            "slf4j-api",
            "2.0.16",
            "oSV43eG6AL2bgW04iguHmSjQC6s8g8JA9wE79BlsV5o=",
            List.of()
    ),
    SLF4J_SIMPLE(
            "org.slf4j",
            "slf4j-simple",
            "2.0.16",
            "7/wyAYZYvqCdHgjH0QYMytRsCGlg9YPQfdf/6cEXKkc=",
            List.of()
    ),
    SQLITE_JDBC(
            "org.xerial",
            "sqlite-jdbc",
            "3.46.0.1",
            "NUEGZh3+r3IWRYEZ4r6crEahCEMnzF7uAgKRaDcAbh4=",
            List.of()
    ),
    MYSQL_DRIVER(
            "com.mysql",
            "mysql-connector-j",
            "9.0.0",
            "oiHEEGt/5opFkSzb+DUfG0OtPFOkPDvJZhgcwU+G+jA=",
            List.of(relocateToLibrary("com.mysql"))
    ),
    MARIADB_DRIVER(
            "org.mariadb.jdbc",
            "mariadb-java-client",
            "3.4.1",
            "9g5LKC8fS9t08KJkNrpweKXkgLb2cC9qe0XZul5gSiQ=",
            List.of(relocateToLibrary("org.mariadb"))
    ),
    POSTGRESQL_DRIVER(
            "org.postgresql",
            "postgresql",
            "42.7.3",
            "omRMv7obqhRf9+jI71gqbu16fsTKeS9/BUEivex1Ymg=",
            List.of(relocateToLibrary("org.postgresql"))
    ),
    HIKARI_CP(
            "com.zaxxer",
            "HikariCP",
            "5.1.0",
            "pHpu5iN5aU7lLDADbwkxty+a7iqAHVkDQe2CvYOeITQ=",
            List.of(relocateToLibrary("com.zaxxer.hikari"))
    ),
    CAFFEINE_CACHE(
            "com.github.ben-manes.caffeine",
            "caffeine",
            "3.1.8",
            "fdFfnfG+I4/6o2fOb1VnN6iAMd5ClNrRju9XxHTd8dM=",
            List.of(relocateToLibrary("com.github.benmanes.caffeine"))
    ),
    JAKARTA_ACTIVATION(
            noReloc("com.sun", ".activation"),
            noReloc("jakarta", ".activation"),
            "2.0.1",
            "ueJLfdbgdJVWLqllMb4xMMltuk144d/Yitu96/QzKHE=",
            List.of(
                    relocateToLibrary("jakarta"),
                    relocateToLibrary(noReloc("com.sun", ".activation"))
            )
    ),
    JAKARTA_MAIL(
            noReloc("com.sun", ".mail"),
            noReloc("jakarta", ".mail"),
            "2.0.1",
            "iYi9veki7hc9txeeIzk90iWPO2T3CPQQguA/DgSUzCM=",
            List.of(
                    relocateToLibrary("jakarta"),
                    relocateToLibrary(noReloc("com.sun", ".mail"))
            )
    );

    public static final String MAVEN_CENTRAL_BASE_URL = "https://repo.maven.apache.org/maven2/";
    public static final String LIBRARY_RELOCATION_PREFIX = "com.community.jpremium.library.";

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String sha256Base64;
    private final List<RelocationRule> relocations;

    RuntimeDependency(
            String groupId,
            String artifactId,
            String version,
            String sha256Base64,
            List<RelocationRule> relocations
    ) {
        this.groupId = Objects.requireNonNull(groupId, "groupId");
        this.artifactId = Objects.requireNonNull(artifactId, "artifactId");
        this.version = Objects.requireNonNull(version, "version");
        this.sha256Base64 = Objects.requireNonNull(sha256Base64, "sha256Base64");
        this.relocations = List.copyOf(Objects.requireNonNull(relocations, "relocations"));
    }

    public String getFileName() {
        return this.artifactId + "-" + this.version + ".jar";
    }

    public String getMavenPath() {
        return this.groupId.replace('.', '/') + "/" + this.artifactId + "/" + this.version + "/" + this.getFileName();
    }

    public byte[] getExpectedSha256() {
        return Base64.getDecoder().decode(this.sha256Base64);
    }

    public List<RelocationRule> getRelocations() {
        return this.relocations;
    }

    public boolean shouldRelocate() {
        return !this.relocations.isEmpty();
    }

    private static RelocationRule relocateToLibrary(String pattern) {
        String cleaned = Objects.requireNonNull(pattern, "pattern").replace("{}", ".");
        return new RelocationRule(cleaned, LIBRARY_RELOCATION_PREFIX + cleaned);
    }

    private static String noReloc(String... parts) {
        return String.join("", parts);
    }
}

