package com.community.jpremium.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordHashService {
    private static final SaltedDigestHasher SHA256_HASHER = new SaltedDigestHasher(HashAlgorithm.SHA256);
    private static final SaltedDigestHasher SHA512_HASHER = new SaltedDigestHasher(HashAlgorithm.SHA512);
    private static final BcryptHasher BCRYPT_HASHER = new BcryptHasher();

    private PasswordHashService() {
        throw new AssertionError();
    }

    public static String hashPassword(HashAlgorithm algorithm, String rawPassword) {
        switch (algorithm) {
            case SHA256: {
                return SHA256_HASHER.hashWithSalt(rawPassword);
            }
            case SHA512: {
                return SHA512_HASHER.hashWithSalt(rawPassword);
            }
            case BCRYPT: {
                return BCRYPT_HASHER.hash(rawPassword);
            }
        }
        throw new IllegalArgumentException();
    }

    public static boolean verifyPassword(String rawPassword, String storedHash) {
        HashAlgorithm storedAlgorithm = HashAlgorithm.valueOf(storedHash.split("\\$")[0]);
        switch (storedAlgorithm) {
            case SHA256: {
                return SHA256_HASHER.verifySaltedDigest(rawPassword, storedHash);
            }
            case SHA512: {
                return SHA512_HASHER.verifySaltedDigest(rawPassword, storedHash);
            }
            case BCRYPT: {
                return BCRYPT_HASHER.verify(rawPassword, storedHash);
            }
        }
        throw new IllegalArgumentException();
    }

    public static boolean needsRehash(HashAlgorithm expectedAlgorithm, String storedHash) {
        HashAlgorithm currentAlgorithm = HashAlgorithm.valueOf(storedHash.split("\\$")[0]);
        return expectedAlgorithm != currentAlgorithm;
    }

    public static enum HashAlgorithm {
        SHA256("SHA-256", 64),
        SHA512("SHA-512", 128),
        BCRYPT("BCRYPT", -1);

        private final String algorithmName;
        private final int hashBits;

        private HashAlgorithm(String algorithmName, int hashBits) {
            this.algorithmName = algorithmName;
            this.hashBits = hashBits;
        }
    }

    private static class SaltedDigestHasher {
        private final HashAlgorithm algorithm;
        private final SecureRandom secureRandom;
        private final MessageDigest messageDigest;

        private SaltedDigestHasher(HashAlgorithm algorithm) {
            try {
                this.algorithm = algorithm;
                this.secureRandom = new SecureRandom();
                this.messageDigest = MessageDigest.getInstance(algorithm.algorithmName);
            }
            catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                throw new RuntimeException(noSuchAlgorithmException);
            }
        }

        private String generateSalt() {
            byte[] saltBytes = new byte[16];
            this.secureRandom.nextBytes(saltBytes);
            return String.format("%016x", new BigInteger(1, saltBytes));
        }

        private String digest(String text) {
            byte[] inputBytes = text.getBytes();
            byte[] digestBytes = this.messageDigest.digest(inputBytes);
            return String.format("%0" + this.algorithm.hashBits + "x", new BigInteger(1, digestBytes));
        }

        private String hashWithSalt(String rawPassword) {
            String salt = this.generateSalt();
            String firstDigest = this.digest(rawPassword);
            String saltedDigest = this.digest(firstDigest + salt);
            return this.algorithm + "$" + salt + "$" + saltedDigest;
        }

        private boolean verifySaltedDigest(String rawPassword, String storedHash) {
            String[] hashParts = storedHash.split("\\$");
            String salt = hashParts[1];
            String expectedDigest = hashParts[2];
            String computedDigest = this.digest(this.digest(rawPassword) + salt);
            return computedDigest.equals(expectedDigest);
        }
    }

    private static class BcryptHasher {
        private BcryptHasher() {
        }

        private String hash(String rawPassword) {
            String bcryptSalt = BCrypt.gensalt();
            String bcryptHash = BCrypt.hashpw(rawPassword, bcryptSalt);
            return bcryptHash.replace("$2a", "BCRYPT");
        }

        private boolean verify(String rawPassword, String storedHash) {
            String bcryptHash = storedHash.replace("BCRYPT", "$2a");
            return BCrypt.checkpw(rawPassword, bcryptHash);
        }
    }
}
