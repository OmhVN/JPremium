package com.community.jpremium.security;

public enum UniqueIdMode {
    FIXED,
    REAL,
    OFFLINE;

    public boolean usesOnlineUuid() {
        return this == FIXED || this == REAL;
    }

    public boolean usesOfflineUuid() {
        return this == FIXED || this == OFFLINE;
    }
}
