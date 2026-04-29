package com.community.jpremium.storage;

import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.storage.hikari.MariaDbHikariConnectionFactory;
import com.community.jpremium.storage.hikari.MySqlHikariConnectionFactory;
import com.community.jpremium.storage.sqlite.SqliteConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class SqlUserProfileRepository implements UserProfileRepository {
    private static final String SELECT_BY_UNIQUE_ID_SQL = "SELECT * FROM user_profiles WHERE uniqueId = ?";
    private static final String SELECT_BY_PREMIUM_ID_SQL = "SELECT * FROM user_profiles WHERE premiumId = ?";
    private static final String SELECT_BY_NICKNAME_SQL = "SELECT * FROM user_profiles WHERE lastNickname = ?";
    private static final String SELECT_BY_ADDRESS_SQL = "SELECT * FROM user_profiles WHERE lastAddress = ? OR firstAddress = ?";
    private static final String DELETE_BY_UNIQUE_ID_SQL = "DELETE FROM user_profiles WHERE uniqueId = ?";
    private static final String INSERT_USER_SQL = "INSERT INTO user_profiles (lastNickname, mailAddress, hashedPassword, verificationToken, lastServer, lastAddress, firstAddress, sessionExpire, lastSeen, firstSeen, premiumId, uniqueId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_USER_SQL = "UPDATE user_profiles SET lastNickname = ?, mailAddress = ?, hashedPassword = ?, verificationToken = ?, lastServer = ?, lastAddress = ?, firstAddress = ?, sessionExpire = ?, lastSeen = ?, firstSeen = ?, premiumId = ? WHERE uniqueId = ?";
    private static final Map<Class<?>, String> SCHEMA_BY_FACTORY = Map.of(
            SqliteConnectionFactory.class, "CREATE TABLE IF NOT EXISTS user_profiles(uniqueId TEXT NOT NULL, premiumId TEXT, lastNickname TEXT COLLATE NOCASE, hashedPassword TEXT, verificationToken TEXT, mailAddress TEXT, sessionExpire DATETIME, lastServer TEXT, lastAddress TEXT, lastSeen DATETIME, firstAddress TEXT, firstSeen DATETIME, PRIMARY KEY (uniqueId), UNIQUE (premiumId), UNIQUE(lastNickname));",
            MySqlHikariConnectionFactory.class, "CREATE TABLE IF NOT EXISTS user_profiles(uniqueId VARCHAR(32) NOT NULL, premiumId VARCHAR(32), lastNickname VARCHAR(16), hashedPassword VARCHAR(256), verificationToken VARCHAR(32), mailAddress VARCHAR(128), sessionExpire TIMESTAMP NULL DEFAULT NULL, lastServer VARCHAR(32), lastAddress VARCHAR(40), lastSeen TIMESTAMP NULL DEFAULT NULL, firstAddress VARCHAR(40), firstSeen TIMESTAMP NULL DEFAULT NULL, PRIMARY KEY (uniqueId), UNIQUE (premiumId), UNIQUE(lastNickname))",
            MariaDbHikariConnectionFactory.class, "CREATE TABLE IF NOT EXISTS user_profiles(uniqueId VARCHAR(32) NOT NULL, premiumId VARCHAR(32), lastNickname VARCHAR(16), hashedPassword VARCHAR(256), verificationToken VARCHAR(32), mailAddress VARCHAR(128), sessionExpire TIMESTAMP NULL DEFAULT NULL, lastServer VARCHAR(32), lastAddress VARCHAR(40), lastSeen TIMESTAMP NULL DEFAULT NULL, firstAddress VARCHAR(40), firstSeen TIMESTAMP NULL DEFAULT NULL, PRIMARY KEY (uniqueId), UNIQUE (premiumId), UNIQUE(lastNickname))"
    );

    private final ConnectionFactory connectionFactory;

    public SqlUserProfileRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory, "connectionFactory");
    }

    @Override
    public Optional<UserProfileData> findByUniqueId(UUID uniqueId) {
        return this.queryOptional(SELECT_BY_UNIQUE_ID_SQL, ProfileDataUtils.toUuidWithoutDashes(uniqueId));
    }

    @Override
    public Optional<UserProfileData> findByPremiumId(UUID premiumId) {
        return this.queryOptional(SELECT_BY_PREMIUM_ID_SQL, ProfileDataUtils.toUuidWithoutDashes(premiumId));
    }

    @Override
    public Optional<UserProfileData> findByNickname(String nickname) {
        return this.queryOptional(SELECT_BY_NICKNAME_SQL, nickname);
    }

    private Optional<UserProfileData> queryOptional(String sqlQuery, String queryParameter) {
        try (Connection connection = this.connectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, queryParameter);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(this.mapRowToUser(resultSet));
                }
                return Optional.empty();
            }
        }
        catch (SQLException sQLException) {
            throw new RuntimeException(sQLException);
        }
    }

    private UserProfileData mapRowToUser(ResultSet resultSet) {
        try {
            UserProfileData userProfile = new UserProfileData();
            userProfile.setUniqueId(ProfileDataUtils.parseUuidWithoutDashes(resultSet.getString("uniqueId")));
            userProfile.setPremiumId(ProfileDataUtils.parseUuidWithoutDashes(resultSet.getString("premiumId")));
            userProfile.setLastNickname(resultSet.getString("lastNickname"));
            userProfile.setEmailAddress(resultSet.getString("mailAddress"));
            userProfile.setHashedPassword(resultSet.getString("hashedPassword"));
            userProfile.setVerificationToken(resultSet.getString("verificationToken"));
            userProfile.setSessionExpires(ProfileDataUtils.toInstant(resultSet.getTimestamp("sessionExpire")));
            userProfile.setLastServer(resultSet.getString("lastServer"));
            userProfile.setLastAddress(resultSet.getString("lastAddress"));
            userProfile.setLastSeen(ProfileDataUtils.toInstant(resultSet.getTimestamp("lastSeen")));
            userProfile.setFirstAddress(resultSet.getString("firstAddress"));
            userProfile.setFirstSeen(ProfileDataUtils.toInstant(resultSet.getTimestamp("firstSeen")));
            return userProfile;
        }
        catch (SQLException sQLException) {
            throw new RuntimeException(sQLException);
        }
    }

    @Override
    public Collection<UserProfileData> findByAddress(String address) {
        try (Connection connection = this.connectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_ADDRESS_SQL)) {
            preparedStatement.setString(1, address);
            preparedStatement.setString(2, address);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                Collection<UserProfileData> collection = new LinkedList<UserProfileData>();
                while (resultSet.next()) {
                    collection.add(this.mapRowToUser(resultSet));
                }
                return Collections.unmodifiableCollection(collection);
            }
        }
        catch (SQLException sQLException) {
            throw new RuntimeException(sQLException);
        }
    }

    @Override
    public void insert(UserProfileData userProfile) {
        this.executeUserUpsert(INSERT_USER_SQL, userProfile);
    }

    @Override
    public void update(UserProfileData userProfile) {
        this.executeUserUpsert(UPDATE_USER_SQL, userProfile);
    }

    private void executeUserUpsert(String sqlQuery, UserProfileData userProfile) {
        try (Connection connection = this.connectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, userProfile.getLastNickname());
            preparedStatement.setString(2, userProfile.getEmailAddress());
            preparedStatement.setString(3, userProfile.getHashedPassword());
            preparedStatement.setString(4, userProfile.getVerificationToken());
            preparedStatement.setString(5, userProfile.getLastServer());
            preparedStatement.setString(6, userProfile.getLastAddress());
            preparedStatement.setString(7, userProfile.getFirstAddress());
            preparedStatement.setTimestamp(8, ProfileDataUtils.toTimestamp(userProfile.getSessionExpires()));
            preparedStatement.setTimestamp(9, ProfileDataUtils.toTimestamp(userProfile.getLastSeen()));
            preparedStatement.setTimestamp(10, ProfileDataUtils.toTimestamp(userProfile.getFirstSeen()));
            preparedStatement.setString(11, ProfileDataUtils.toUuidWithoutDashes(userProfile.getPremiumId()));
            preparedStatement.setString(12, ProfileDataUtils.toUuidWithoutDashes(userProfile.getUniqueId()));
            preparedStatement.executeUpdate();
        }
        catch (SQLException sQLException) {
            throw new RuntimeException(sQLException);
        }
    }

    @Override
    public void delete(UserProfileData userProfile) {
        try (Connection connection = this.connectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_BY_UNIQUE_ID_SQL)) {
            preparedStatement.setString(1, ProfileDataUtils.toUuidWithoutDashes(userProfile.getUniqueId()));
            preparedStatement.executeUpdate();
        }
        catch (SQLException sQLException) {
            throw new RuntimeException(sQLException);
        }
    }

    @Override
    public void initialize() {
        this.connectionFactory.open();
        try (Connection connection = this.connectionFactory.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(SCHEMA_BY_FACTORY.get(this.connectionFactory.getClass()));
        }
        catch (SQLException sQLException) {
            throw new RuntimeException(sQLException);
        }
    }

    @Override
    public void shutdown() {
        this.connectionFactory.close();
    }
}
